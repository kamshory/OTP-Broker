package com.planetbiru;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.planetbiru.config.ConfigAPI;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.constant.ResponseCode;
import com.planetbiru.gsm.GSMNullException;
import com.planetbiru.gsm.SMSUtil;
import com.planetbiru.util.MailUtil;
import com.planetbiru.util.Utility;

@RestController
public class ServerWebAPI {
	
	@Value("${otpbroker.path.setting.api}")
	private String userAPISettingPath;
	
	@PostConstruct
	public void init()
	{
		ConfigAPI.load(userAPISettingPath);
	}
	
	@PostMapping(path="/api/email**")
	public ResponseEntity<String> sendEmail(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode;
		JSONObject responseJSON;
		if(this.validRequest(headers))
		{
			statusCode = HttpStatus.OK;
			responseJSON = new JSONObject();
			try 
			{
				responseJSON = this.processEmailRequest(requestBody);
			} 
			catch (MessagingException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			statusCode = HttpStatus.UNAUTHORIZED;
			responseJSON = this.unauthorized(requestBody);					
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		String responseBody = responseJSON.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/api/sms**")
	public ResponseEntity<String> sendSMS(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		
		HttpStatus statusCode;
		JSONObject responseJSON;
		if(this.validRequest(headers))
		{
			statusCode = HttpStatus.OK;
			responseJSON = this.processMessageRequest(requestBody);
		}
		else
		{
			statusCode = HttpStatus.UNAUTHORIZED;
			responseJSON = this.unauthorized(requestBody);			
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		String responseBody = responseJSON.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@PostMapping(path="/api/ussd**")
	public ResponseEntity<String> sendUSSD(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode;
		JSONObject responseJSON;
		if(this.validRequest(headers))
		{
			statusCode = HttpStatus.OK;
			responseJSON = new JSONObject();
			Map<String, String> query = Utility.parseURLEncoded(requestBody);
			String ussd = query.getOrDefault("ussd", "");
			String message = "";
			String responseCode = ResponseCode.SUCCESS;
			String responseText = "";
			if(ussd != null && !ussd.isEmpty())
			{
				try 
				{
					message = SMSUtil.executeUSSD(ussd);
				} 
				catch (GSMNullException e) 
				{
					responseCode = e.getErrorCode();
					responseText = "<strong>Error: "+e.getErrorCode()+"</strong> "+e.getMessage()+". <a href=\"error-"+e.getErrorCode()+".html\">Detail</a>";
				}		
			}
			JSONObject data = new JSONObject();
			data.put(JsonKey.MESSAGE, message);
			responseJSON.put(JsonKey.RESPONSE_CODE, responseCode);
			responseJSON.put(JsonKey.RESPONSE_TEXT, responseText);
			responseJSON.put(JsonKey.DATA, data);		
			responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
			responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		}
		else
		{
			statusCode = HttpStatus.UNAUTHORIZED;
			responseJSON = this.unauthorized(requestBody);
		}
		String responseBody = responseJSON.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	private JSONObject unauthorized(String requestBody) {
		JSONObject requestJSON = new JSONObject();
		String command = "";
		try
		{
			requestJSON = new JSONObject(requestBody);
			command = requestJSON.optString(JsonKey.COMMAND, "");
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}
		JSONArray data = new JSONArray();
		requestJSON.put(JsonKey.DATA, data);
		requestJSON.put(JsonKey.COMMAND, command);
		requestJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.UNAUTHORIZED);
		return requestJSON;
	}

	private boolean validRequest(HttpHeaders headers) {
		return this.parseBasicAuth(headers);
	}
	private boolean parseBasicAuth(Map<String, List<String>> requestHeaders) 
    {
		String username = "";
		String password = "";
    	for (Map.Entry<String, List<String>> headers : requestHeaders.entrySet()) 
    	{
			String key = headers.getKey();
			List<String> valueList = headers.getValue();
			for (String value : valueList) 
			{
				if(key.equalsIgnoreCase("authorization") && value.startsWith("Basic "))
				{
					String auth = value.substring(6);
					String decoded = Utility.base64Decode(auth);
					String[] arr = decoded.split(":", 3);
					username = arr[0];
					if(arr.length > 1)
					{
						password = arr[1];
						return ConfigAPI.checkUserAuth(username, password);
					}
				}
			}
    	}
    	return false;
	}
	
	
	private JSONObject processMessageRequest(String requestBody) 
	{
		JSONObject requestJSON = new JSONObject();
		try
		{
			requestJSON = new JSONObject(requestBody);
			String command = requestJSON.optString(JsonKey.COMMAND, "");
			if(command.equals(ConstantString.SEND_MESSAGE))
			{
				JSONArray data = requestJSON.optJSONArray(JsonKey.DATA);
				if(data != null && !data.isEmpty())
				{
					int length = data.length();
					int i;
					for(i = 0; i<length; i++)
					{
						this.sendMessage(data.getJSONObject(i));					
					}
				}
			}
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}
		return requestJSON;
	}
	private JSONObject processEmailRequest(String requestBody) throws MessagingException 
	{
		JSONObject requestJSON = new JSONObject();
		try
		{
			requestJSON = new JSONObject(requestBody);
			String command = requestJSON.optString(JsonKey.COMMAND, "");
			if(command.equals(ConstantString.SEND_MESSAGE))
			{
				JSONArray data = requestJSON.optJSONArray(JsonKey.DATA);
				if(data != null && !data.isEmpty())
				{
					int length = data.length();
					int i;
					for(i = 0; i<length; i++)
					{
						this.sendMail(data.getJSONObject(i));					
					}
				}
			}
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}
		return requestJSON;
	}
	
	private void sendMail(JSONObject data) throws MessagingException {
		if(data != null)
		{
			String receiver = data.optString(JsonKey.RECEIVER, "");
			String textMessage = data.optString(JsonKey.MESSAGE, "");
			String subject = data.optString(JsonKey.SUBJECT, "");
			try 
			{
				MailUtil mailUtil = new MailUtil();
				mailUtil.send(receiver, subject, textMessage);
			} 
			catch (MessagingException e) 
			{
				
				throw new MessagingException(e.getMessage());
			}
		}	
	}

	private void sendMessage(JSONObject data)
	{
		if(data != null)
		{
			String receiver = data.optString(JsonKey.RECEIVER, "");
			String textMessage = data.optString(JsonKey.MESSAGE, "");
			try 
			{
				SMSUtil.sendSMS(receiver, textMessage);
			} 
			catch (GSMNullException e) 
			{
				
				e.printStackTrace();
			}
		}		
	}
	
}

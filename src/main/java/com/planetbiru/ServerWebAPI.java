package com.planetbiru;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.gsm.GSMErrorCode;
import com.planetbiru.gsm.GSMNullException;
import com.planetbiru.gsm.SMSUtil;
import com.planetbiru.util.Utility;

@RestController
public class ServerWebAPI {
	@PostMapping(path="/api/sms**")
	public ResponseEntity<String> sendSMS(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		JSONObject responseJSON = this.processMessageRequest(requestBody);
		
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		String responseBody = responseJSON.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/api/ussd**")
	public ResponseEntity<String> sendUSSD(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		JSONObject responseJSON = new JSONObject();
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		String ussd = query.getOrDefault("ussd", "");
		String message = "";
		String responseCode = GSMErrorCode.SUCCESS;
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
		String responseBody = responseJSON.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	private JSONObject processMessageRequest(String requestBody) 
	{
		JSONObject requestJSON = new JSONObject();
		try
		{
			requestJSON = new JSONObject(requestBody);
			String command = requestJSON.optString(JsonKey.COMMAND, "");
			if(command.equals(JsonKey.SEND_MESSAGE))
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

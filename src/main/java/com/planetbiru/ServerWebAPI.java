package com.planetbiru;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.planetbiru.api.RESTAPI;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.constant.ResponseCode;
import com.planetbiru.gsm.GSMNullException;
import com.planetbiru.gsm.SMSUtil;
import com.planetbiru.user.APIUserAccount;
import com.planetbiru.util.Utility;

@RestController
public class ServerWebAPI {
	
	@Value("${otpbroker.path.setting.api.user}")
	private String userAPISettingPath;
	
	@PostConstruct
	public void init()
	{
		APIUserAccount.load(userAPISettingPath);
	}
	
	@PostMapping(path="/api/email**")
	public ResponseEntity<String> sendEmail(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode;
		JSONObject responseJSON;
		if(RESTAPI.validRequest(headers))
		{
			statusCode = HttpStatus.OK;
			responseJSON = new JSONObject();
			responseJSON = RESTAPI.processEmailRequest(requestBody);
		}
		else
		{
			statusCode = HttpStatus.UNAUTHORIZED;
			responseJSON = RESTAPI.unauthorized(requestBody);					
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
		if(RESTAPI.validRequest(headers))
		{
			statusCode = HttpStatus.OK;
			responseJSON = RESTAPI.processMessageRequest(requestBody);
		}
		else
		{
			statusCode = HttpStatus.UNAUTHORIZED;
			responseJSON = RESTAPI.unauthorized(requestBody);			
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
		if(RESTAPI.validRequest(headers))
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
					responseText = ""+e.getMessage()+". <a href=\"error-1000.html\">Detail</a>";
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
			responseJSON = RESTAPI.unauthorized(requestBody);
		}
		String responseBody = responseJSON.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	
}

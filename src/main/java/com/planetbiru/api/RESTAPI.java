package com.planetbiru.api;

import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;

import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.constant.ResponseCode;
import com.planetbiru.gsm.GSMNullException;
import com.planetbiru.gsm.SMSUtil;
import com.planetbiru.user.APIUserAccount;
import com.planetbiru.util.MailUtil;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;

public class RESTAPI {
	public static JSONObject unauthorized(String requestBody) {
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

	public static boolean validRequest(HttpHeaders headers) 
	{
		return RESTAPI.parseBasicAuth(headers);
	}
	public static boolean validRequest(Headers headers) 
	{
		return RESTAPI.parseBasicAuth(headers);
	}
	public static boolean parseBasicAuth(Map<String, List<String>> requestHeaders) 
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
						return APIUserAccount.checkUserAuth(username, password);
					}
				}
			}
    	}
    	return false;
	}
	
	
	public static JSONObject processMessageRequest(String requestBody) 
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
						RESTAPI.sendMessage(data.getJSONObject(i));					
					}
				}
			}
			else if(command.equals(ConstantString.SEND_MAIL))
			{
				JSONArray data = requestJSON.optJSONArray(JsonKey.DATA);
				if(data != null && !data.isEmpty())
				{
					int length = data.length();
					int i;
					for(i = 0; i<length; i++)
					{
						RESTAPI.sendMail(data.getJSONObject(i));					
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
	public static JSONObject processEmailRequest(String requestBody) 
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
						RESTAPI.sendMail(data.getJSONObject(i));					
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
	
	public static void sendMail(JSONObject data) {
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
				
			}
		}	
	}

	public static void sendMessage(JSONObject data)
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

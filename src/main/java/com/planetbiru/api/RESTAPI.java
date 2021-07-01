package com.planetbiru.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;

import com.planetbiru.config.ConfigAPIUser;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.constant.ResponseCode;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.mail.MailUtil;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class RESTAPI {
	private RESTAPI()
	{
		
	}
	
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

	public static boolean isValidRequest(HttpHeaders headers) 
	{
		return RESTAPI.checkValidRequest(headers);
	}
	
	public static boolean isValidRequest(Headers headers) 
	{
		return RESTAPI.checkValidRequest(headers);
	}
	
	public static boolean checkValidRequest(Map<String, List<String>> requestHeaders) 
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
						return ConfigAPIUser.checkUserAuth(username, password);
					}
				}
			}
    	}
    	return false;
	}	
	
	public static JSONObject processEmailRequest(String requestBody) 
	{
		JSONObject requestJSON = new JSONObject();
		try
		{
			requestJSON = new JSONObject(requestBody);
			String command = requestJSON.optString(JsonKey.COMMAND, "");
			if(command.equals(ConstantString.SEND_SMS))
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
	
	public static void sendMail(JSONObject data) 
	{
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
				/**
				 * Do nothing
				 */
			}
		}	
	}

	public static JSONObject sendMessage(String command, JSONObject data)
	{
		JSONObject responseJSON = new JSONObject();
		JSONObject jsonData = new JSONObject();
		if(data != null)
		{
			String receiver = data.optString(JsonKey.MSISDN, "");
			String textMessage = data.optString(JsonKey.MESSAGE, "");
			try 
			{
				jsonData = GSMUtil.sendSMS(receiver, textMessage);
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
				
			} 
			catch (GSMException e) 
			{
				e.printStackTrace();
				/**
				 * Do nothing
				 */
				responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.NO_DEVICE_CONNECTED);
				responseJSON.put("error", e.getMessage());
			}
		}
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.DATA, jsonData);
		return responseJSON;		
	}
	
	public static byte[] getRequestBody(HttpExchange httpExchange)
	{
        Headers requestHeaders = httpExchange.getRequestHeaders();
		String cl = requestHeaders.getFirst("Content-length");
		byte[] requestBody = "".getBytes();
        if(cl != null)
        {
            try
            {
	            int contentLength = Utility.atoi(cl);	
	            requestBody = new byte[contentLength];
	            for(int j = 0; j < contentLength; j++)
	            {
	            	requestBody[j] = (byte) httpExchange.getRequestBody().read();
	            }
            }
            catch(NumberFormatException | IOException e)
            {
            	/**
            	 * Do nothing
            	 */
            }
            return requestBody;
        }
        else
        {
        	return "".getBytes();
        }
	}
	
	public static JSONObject processRequest(String requestBody) {
		JSONObject requestJSON = new JSONObject();
		JSONObject responseJSON = new JSONObject();
		try
		{
			requestJSON = new JSONObject(requestBody);
			String command = requestJSON.optString(JsonKey.COMMAND, "");
			if(command.equals(ConstantString.SEND_SMS))
			{
				JSONObject data = requestJSON.optJSONObject(JsonKey.DATA);
				responseJSON = RESTAPI.sendMessage(command, data);		
			}
			else if(command.equals(ConstantString.SEND_MAIL))
			{
				JSONObject data = requestJSON.optJSONObject(JsonKey.DATA);
				responseJSON = RESTAPI.sendEmail(command, data);					
			}
			else if(command.equals(ConstantString.BLOCK_MSISDN))
			{
				JSONObject data = requestJSON.optJSONObject(JsonKey.DATA);
				if(data != null)
				{
					responseJSON = RESTAPI.blockMSISDN(command, data.optString("msisdn", ""));					
				}
			}
			else if(command.equals(ConstantString.UNBLOCK_MSISDN))
			{
				JSONObject data = requestJSON.optJSONObject(JsonKey.DATA);
				if(data != null)
				{
					responseJSON = RESTAPI.unblockMSISDN(command, data.optString("msisdn", ""));					
				}
			}
		}
		catch(JSONException | GSMException e)
		{
			/**
			 * Do nothing
			 */
		}
		return responseJSON;
	}
	
	private static JSONObject sendEmail(String command, JSONObject data) {
		JSONObject responseJSON = new JSONObject();
		MailUtil mailUtil = new MailUtil();
		String to = data.optString("recipient", "");
		String subject = data.optString("subject", "");
		String message = data.optString("message", "");
		String result = "";
		try 
		{
			mailUtil.send(to, subject, message);
			result = "The message was sent successfuly";
			responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
			responseJSON.put(JsonKey.MESSAGE, result);
		} 
		catch (MessagingException e) 
		{
			result = e.getMessage();
			responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
			responseJSON.put(JsonKey.MESSAGE, result);
		}	
		responseJSON.put(JsonKey.COMMAND, command);
		return responseJSON;	
	}
	
	private static JSONObject blockMSISDN( String command, String msisdn) throws GSMException {
		ConfigBlocking.block(msisdn);
		ConfigBlocking.save();
		JSONObject responseJSON = new JSONObject();
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
		return responseJSON;	
	}
	
	private static JSONObject unblockMSISDN(String command, String msisdn) throws GSMException {
		ConfigBlocking.unblock(msisdn);
		ConfigBlocking.save();
		JSONObject responseJSON = new JSONObject();
		responseJSON.put(JsonKey.COMMAND, command);
		responseJSON.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
		return responseJSON;		
	}
	
}

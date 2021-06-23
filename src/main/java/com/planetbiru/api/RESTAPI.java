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
import com.planetbiru.gsm.SMSUtil;
import com.planetbiru.util.MailUtil;
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
			catch (GSMException e) 
			{
				e.printStackTrace();
				/**
				 * Do nothing
				 */
			}
		}		
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
			if(command.equals(ConstantString.SEND_MESSAGE))
			{
				JSONObject data = requestJSON.optJSONObject(JsonKey.DATA);
				RESTAPI.sendMessage(data);		
				responseJSON.put("response_code", ResponseCode.SUCCESS);
			}
			else if(command.equals(ConstantString.SEND_MAIL))
			{
				JSONObject data = requestJSON.optJSONObject(JsonKey.DATA);
				RESTAPI.sendEmail(data);					
			}
			else if(command.equals(ConstantString.BLOCK_MSISDN))
			{
				JSONObject data = requestJSON.optJSONObject(JsonKey.DATA);
				if(data != null)
				{
					RESTAPI.blockMSISDN(data.optString("msisdn", ""));					
				}
			}
			else if(command.equals(ConstantString.UNBLOCK_MSISDN))
			{
				JSONObject data = requestJSON.optJSONObject(JsonKey.DATA);
				if(data != null)
				{
					RESTAPI.unblockMSISDN(data.optString("msisdn", ""));					
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
	private static void sendEmail(JSONObject data) {
		// TODO Auto-generated method stub
		
	}
	private static void blockMSISDN(String msisdn) throws GSMException {
		ConfigBlocking.block(msisdn);
		
	}
	private static void unblockMSISDN(String msisdn) throws GSMException {
		ConfigBlocking.unblock(msisdn);
		
	}
	
}

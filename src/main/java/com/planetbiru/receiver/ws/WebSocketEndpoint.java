package com.planetbiru.receiver.ws;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.api.RESTAPI;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.util.ServerInfo;

public class WebSocketEndpoint extends Endpoint {
	private static Logger logger = LogManager.getLogger(WebSocketEndpoint.class);
	private WebSocketClient webSocketClient;
	private Session session;
	
	public WebSocketEndpoint(WebSocketClient webSocketClient) {
		this.webSocketClient = webSocketClient;
	}
	@Override
	public void onOpen(Session ses, EndpointConfig config) {
		this.session = ses;		
		this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
            	onReceiveMessage(message);       	
            }
        });
		ServerInfo.sendWSStatus(true);
	}
	public void onReceiveMessage(String message)
	{
		try
		{
			JSONObject requestJSON = new JSONObject(message);
			this.processRequest(requestJSON);			
		}
		catch(JSONException e)
		{
			logger.error(e.getMessage());
		}
	}
	
	
	private void processRequest(JSONObject jsonObj) 
	{
		if(jsonObj != null && !jsonObj.isEmpty())
		{
			String command = jsonObj.optString("command", "");
			if(command.equals("send-sms"))
			{
				this.sendSMS(jsonObj);
			}
			else if(command.equals("block-msisdn"))
			{
				this.blockMSISDN(jsonObj);
			}
			else if(command.equals("unblock-msisdn"))
			{
				this.unblockMSISDN(jsonObj);
			}
			
		}
	}
	
	private void blockMSISDN(JSONObject jsonObj) {
		String command = jsonObj.optString("command", "");
		JSONObject data = jsonObj.optJSONObject("data");
		String msisdn = data.optString("msisdn", "");
		try 
		{
			RESTAPI.blockMSISDN(command, msisdn);
		} 
		catch (GSMException e) 
		{
			e.printStackTrace();
		}
	}
	private void unblockMSISDN(JSONObject jsonObj) {
		String command = jsonObj.optString("command", "");
		JSONObject data = jsonObj.optJSONObject("data");
		String msisdn = data.optString("msisdn", "");
		try 
		{
			RESTAPI.unblockMSISDN(command, msisdn);
		} 
		catch (GSMException e) 
		{
			e.printStackTrace();
		}
	}
	private void sendSMS(JSONObject jsonObj) {
		JSONObject data = jsonObj.optJSONObject("data");
		String msisdn = data.optString("msisdn", "");
		String message = data.optString("message", "");
		try 
		{
			this.sendSMS(msisdn, message);
		} 
		catch (GSMException e) 
		{
			e.printStackTrace();
		}
		
	}
	private void sendSMS(String receiver, String textMessage) throws GSMException 
	{
		GSMUtil.sendSMS(receiver, textMessage);	
	}
	
	public void sendText(String text) throws IOException
	{
		this.session.getBasicRemote().sendText(text);		
	}
	
	@Override
	public void onError(Session session, Throwable throwable)
	{
		ServerInfo.sendWSStatus(false, throwable.getMessage());
		this.webSocketClient.getWebSocketTool().restartThread();
	}
	
	@Override
	public void onClose(Session ses, CloseReason closeReason) 
	{
		ServerInfo.sendWSStatus(false, closeReason.getReasonPhrase());
		this.webSocketClient.getWebSocketTool().restartThread();
    }

}

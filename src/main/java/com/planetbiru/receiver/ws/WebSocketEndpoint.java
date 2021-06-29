package com.planetbiru.receiver.ws;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
		try 
		{
			this.login();
			ServerInfo.sendWSStatus(true);
		}
		catch (IOException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	public void onReceiveMessage(String message)
	{
		try
		{
			JSONObject requestJSON = new JSONObject(message);
			String command = requestJSON.optString("command", "");
			if(command.equals("send-message"))
			{
				JSONArray data = requestJSON.optJSONArray("data");
				if(data != null && !data.isEmpty())
				{
					int length = data.length();
					int i;
					for(i = 0; i<length; i++)
					{
						this.sendSMS(data.getJSONObject(i));
						
					}
				}
			}
		}
		catch(JSONException e)
		{
			logger.error(e.getMessage());
		}
	}
	
	private void sendSMS(JSONObject data) {
		if(data != null)
		{
			String receiver = data.optString("receiver", "");
			String textMessage = data.optString("message", "");
			try 
			{
				this.sendSMS(receiver, textMessage);
			} 
			catch (GSMException e) 
			{
				logger.error(e.getMessage());
			}
		}
	}
	private void sendSMS(String receiver, String textMessage) throws GSMException {
		GSMUtil.sendSMS(receiver, textMessage);
		
	}
	private void login() throws IOException {
		String text = "";	
		JSONObject requestJSON = new JSONObject();
		requestJSON.put("command", "receive-message");
		requestJSON.put("channel", "sms");
		requestJSON.put("data", new JSONObject());
		text = requestJSON.toString(4);
		this.sendText(text);
		
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
	public void onClose(Session ses, CloseReason closeReason) {
		ServerInfo.sendWSStatus(false, closeReason.getReasonPhrase());
		this.webSocketClient.getWebSocketTool().restartThread();
    }

}

package com.planetbiru.receiver.ws;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.config.Config;
import com.planetbiru.gsm.GSMNotInitalizedException;
import com.planetbiru.gsm.SMSInstance;

public class ClientEndpoint extends Endpoint {
	private WebSocketClient webSocketClient;
	private SMSInstance smsService;
	private Session session;
	public ClientEndpoint(WebSocketClient webSocketClient, SMSInstance smsService) {
		this.webSocketClient = webSocketClient;
		this.smsService = smsService;
	}
	@Override
	public void onOpen(Session ses, EndpointConfig config) {
		this.session = ses;		
		this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
            	try {
					onReceiveMessage(message);
				} catch (GSMNotInitalizedException e) {
					e.printStackTrace();
				}
            }
        });
		try 
		{
			this.login();
		}
		catch (IOException e) 
		{
		}
	}
	public void onReceiveMessage(String message) throws GSMNotInitalizedException
	{
		System.out.println("=============================================");
		System.out.println("Receive Message "+message);
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
						JSONObject dt = data.getJSONObject(i);
						if(dt != null)
						{
							String receiver = dt.optString("receiver", "");
							String textMessage = dt.optString("message", "");
							this.smsService.sendSMS(receiver, textMessage);
						}
					}
				}
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
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
	public void onClose(Session ses, CloseReason closeReason) {
		System.out.println("on close : "+closeReason.getReasonPhrase());
		try 
		{
			Thread.sleep(Config.getReconnectDelay());
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
		this.webSocketClient.reconnect();
    }

}

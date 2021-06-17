package com.planetbiru.receiver.ws;


import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Configurator;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.ServerWebSocketManager;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.gsm.SMSUtil;
import com.planetbiru.util.Utility;

public class WebSocketClient extends Thread
{
	private Logger logger = LogManager.getLogger(WebSocketClient.class);
	private static Object waitLock = new Object();
	private Session session = null;
	private WebSocketContainer container;
	private static boolean stoped = false;
	
	public WebSocketClient() {
		logger.info("Default constructor");
	}

	@Override
	public void run()
	{
		boolean connected = false;
		do
		{
			logger.info("Connecting...");
			try 
			{
				this.initWSClient();
				connected = true;
				ConfigFeederWS.setConnected(true);
				sendServerStatus(true);
				System.out.println("Connected...");
			} 
			catch (WebSocketConnectionException e) 
			{
				connected = false;
				sleep(Config.getReconnectDelay());			
				try 
				{
					this.initWSClient();
				} 
				catch (WebSocketConnectionException e1) 
				{
					e1.printStackTrace();
				}
			}
		}
		while(!connected && !isStoped());
	}
	public static void sleep(long interval)
	{
		try 
		{
			Thread.sleep(interval);
		} 
		catch (InterruptedException e1) 
		{
			Thread.currentThread().interrupt();
		}
	}
	
	public void initWSClient() throws WebSocketConnectionException
	{
		setSession(null);
		setContainer(null);
		try
		{
			String url = Config.getSsClientEndpoint();
			setContainer(ContainerProvider.getWebSocketContainer()); 	
			
			ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
			configBuilder.configurator(new Configurator() {
			    @Override
			    public void beforeRequest(Map<String, List<String>> headers) 
			    {
			        headers.put("Authorization", Utility.asList(Utility.basicAuth(Config.getWsClientUsername(), Config.getWsClientPassword())));
			    }
			});
			ClientEndpointConfig clientConfig = configBuilder.build();
			
			setSession(getContainer().connectToServer(new WebSocketEndpoint(this), clientConfig, URI.create(url))); 
			ConfigFeederWS.setConnected(true);
			sendServerStatus(true);
			System.out.println("Connected...");
			wait4TerminateSignal();
			
		} 
		catch (DeploymentException | IOException e) 
		{
			throw new WebSocketConnectionException(e);
		}
		finally
		{
			if(getSession() != null)
			{
				try 
				{
					getSession().close();
				} 
				catch (IOException e) 
				{     
					e.printStackTrace();
				}
			}         
		} 
	}
	
	public void close() {
		try 
		{
			getSession().close();
			ConfigFeederWS.setConnected(false);
			sendServerStatus(false);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}		
	}
	
	 void sendServerStatus(boolean connected) 
	    {
			JSONArray data = new JSONArray();
			JSONObject info = new JSONObject();
			
			JSONObject ws = new JSONObject();
			ws.put("name", "otp_ws_status");
			ws.put("connected", connected);
			data.put(ws);
			
			info.put("command", "server-info");
			info.put("data", data);
		
			ServerWebSocketManager.broadcast(info.toString(4));
			
		}
	private static void wait4TerminateSignal()
	{
		synchronized(waitLock)
		{
			try 
			{
				waitLock.wait();
			} 
			catch (InterruptedException e) 
			{
				Thread.currentThread().interrupt();
			}
		}
	}

	public void reconnect() 
	{
		logger.info("Reconnect...");
		try 
		{
			initWSClient();
		} 
		catch (WebSocketConnectionException e) 
		{
			sleep(Config.getReconnectDelay());
			reconnect();
		}
	}

	

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public WebSocketContainer getContainer() {
		return container;
	}

	public void setContainer(WebSocketContainer container) {
		this.container = container;
	}

	public static boolean isStoped() {
		return stoped;
	}

	public static void setStoped(boolean stoped) {
		WebSocketClient.stoped = stoped;
	}

	
	
	
}

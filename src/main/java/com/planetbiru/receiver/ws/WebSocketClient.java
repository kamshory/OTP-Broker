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
import org.json.JSONObject;

import com.planetbiru.ServerWebSocketManager;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.util.Utility;

public class WebSocketClient extends Thread
{
	private Logger logger = LogManager.getLogger(WebSocketClient.class);
	private static Object waitLock = new Object();
	private Session session = null;
	private WebSocketContainer container;
	private WebSocketTool webSocketTool;
	private boolean stoped = false;
	
	public WebSocketClient(WebSocketTool webSocketTool) {
		this.webSocketTool = webSocketTool;
	}

	
	@Override
	public void run()
	{
		this.container = null;
		this.session = null;
		stoped = false;
		
		
		boolean connected = false;
		do
		{
			try 
			{
				this.initWSClient();
				connected = true;
				ConfigFeederWS.setConnected(true);
				sendWSStatus(true);
			} 
			catch (WebSocketConnectionException e) 
			{
				connected = false;
				try 
				{
					this.initWSClient();
					connected = true;
					ConfigFeederWS.setConnected(true);
					sendWSStatus(true);
				} 
				catch (WebSocketConnectionException e1) 
				{
					e1.printStackTrace();
				}
			}
			sleep(Config.getReconnectDelay());			
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
		this.session = null;
		this.container = null;
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
			
			this.session = getContainer().connectToServer(new WebSocketEndpoint(this), clientConfig, URI.create(url)); 
			ConfigFeederWS.setConnected(true);
			sendWSStatus(true);
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
			ConfigFeederWS.setConnected(false);
			sendWSStatus(false);
			getSession().close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}		
	}
	
	void sendWSStatus(boolean connected, String message) 
    {
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		
		JSONObject ws = new JSONObject();
		ws.put("name", "ws_connected");
		ws.put("connected", connected);
		ws.put("message", message);
		data.put(ws);
		
		info.put("command", "server-info");
		info.put("data", data);
	
		ServerWebSocketManager.broadcast(info.toString(4));
		
		
	}
	
	
	public void sendWSStatus(boolean connected) {
		this.sendWSStatus(connected, "");
			
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

	public boolean isStoped() {
		return stoped;
	}

	public void setStoped(boolean stoped) {
		this.stoped = stoped;
	}

	public WebSocketTool getWebSocketTool() {
		return webSocketTool;
	}

	public void setWebSocketTool(WebSocketTool webSocketTool) {
		this.webSocketTool = webSocketTool;
	}

	

	
	
	
}

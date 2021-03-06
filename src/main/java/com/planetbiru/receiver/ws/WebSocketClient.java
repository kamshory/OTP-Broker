package com.planetbiru.receiver.ws;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
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
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.util.ServerInfo;
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
		this.stoped = false;	
		boolean connected = false;
		do
		{
			try 
			{
				this.initWSClient();
				connected = true;
				ConfigFeederWS.setConnected(true);
				ServerInfo.sendWSStatus(true);
			} 
			catch (WebSocketConnectionException e) 
			{
				connected = false;
				try 
				{
					this.initWSClient();
					connected = true;
					ConfigFeederWS.setConnected(true);
					ServerInfo.sendWSStatus(true, e.getMessage());
				} 
				catch (WebSocketConnectionException e1) 
				{
					/**
					 * Do nothing
					 */
				}
			}
			sleep(Config.getReconnectDelay());			
		}
		while(!connected && !isStoped());
	}
	
	public void stopThreade()
	{
		this.stoped = true;
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
			String endpoint = this.createWSEndpoint();
			endpoint = this.fixWSEndpoint(endpoint);
			setContainer(ContainerProvider.getWebSocketContainer()); 	
			
			ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
			configBuilder.configurator(new Configurator() {
			    @Override
			    public void beforeRequest(Map<String, List<String>> headers) 
			    {
			        headers.put("Authorization", Utility.asList(Utility.basicAuth(ConfigFeederWS.getFeederWsUsername(), ConfigFeederWS.getFeederWsPassword())));
			    }
			});
			ClientEndpointConfig clientConfig = configBuilder.build();
			
			this.session = getContainer().connectToServer(new WebSocketEndpoint(this), clientConfig, URI.create(endpoint)); 
			ConfigFeederWS.setConnected(true);
			ServerInfo.sendWSStatus(true);
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
					logger.error(e.getMessage());     
				}
			}         
		} 
	}
	
	private String fixWSEndpoint(String endpoint) {
		String channel = Utility.urlEncode(ConfigFeederWS.getFeederWsChannel());
		String path = endpoint;
		Map<String, List<String>> params = new HashMap<>();
		String query = "";
		if(endpoint.contains("?"))
		{
			String[] arr = endpoint.split("\\?", 2);
			if(arr.length > 1)
			{
				path = arr[0];
				try 
				{
					params = Utility.splitQuery(arr[1]);
					if(params.containsKey("channel"))
					{
						params.remove("channel");
					}
					
					
				} 
				catch (UnsupportedEncodingException e) 
				{
					/**
					 * Do nothing
					 */
				}				
			}
		}
		params.put("channel", Utility.asList(channel));
		query = Utility.buildQuery(params);
		
		endpoint = path + "?"+query;
		return endpoint;	
	}

	private String createWSEndpoint() {
		String protocol = "";
		String host = ConfigFeederWS.getFeederWsAddress();
		String port = "";
		String contextPath = ConfigFeederWS.getFeederWsPath();
		if(!contextPath.startsWith("/"))
		{
			contextPath = "/"+contextPath;
		}
		if(ConfigFeederWS.isFeederWsSSL())
		{
			protocol = "wss://";
			if(ConfigFeederWS.getFeederWsPort() != 443)
			{
				port = ":"+ConfigFeederWS.getFeederWsPort();
			}
		}
		else
		{
			protocol = "ws://";
			if(ConfigFeederWS.getFeederWsPort() != 80)
			{
				port = ":"+ConfigFeederWS.getFeederWsPort();
			}
		}	
		return String.format("%s%s%s%s", protocol, host, port, contextPath);
	}

	public void close() {
		try 
		{
			ConfigFeederWS.setConnected(false);
			ServerInfo.sendWSStatus(false);
			getSession().close();
		} 
		catch (IOException e) 
		{
			/**
			 * Do nothing
			 */
		}		
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

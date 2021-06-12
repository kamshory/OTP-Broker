package com.planetbiru.receiver.ws;


import java.io.IOException;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Configurator;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.planetbiru.config.Config;
import com.planetbiru.util.Utility;

public class WebSocketClient extends Thread implements WebSocket
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
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
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

	

	@Override
	public void abort() {
		/**
		 * Do nothing
		 */
	}

	@Override
	public String getSubprotocol() {
		return null;
	}

	@Override
	public boolean isInputClosed() {
		return false;
	}

	@Override
	public boolean isOutputClosed() {
		return false;
	}

	@Override
	public void request(long arg0) {
		/**
		 * Do nothing
		 */
	}

	@Override
	public CompletableFuture<WebSocket> sendBinary(ByteBuffer arg0, boolean arg1) {
		return null;
	}

	@Override
	public CompletableFuture<WebSocket> sendClose(int arg0, String arg1) {
		return null;
	}

	@Override
	public CompletableFuture<WebSocket> sendPing(ByteBuffer arg0) {
		return null;
	}

	@Override
	public CompletableFuture<WebSocket> sendPong(ByteBuffer arg0) {
		return null;
	}

	@Override
	public CompletableFuture<WebSocket> sendText(CharSequence arg0, boolean arg1) {
		return null;
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

package com.planetbiru.receiver.ws;

import java.io.IOException;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.ClientEndpointConfig.Configurator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.planetbiru.config.Config;
import com.planetbiru.gsm.SMSInstance;
import com.planetbiru.util.Utility;

@Service
public class WebSocketClient extends Thread{
	private static Object waitLock = new Object();
    private static Logger logger = LogManager.getLogger(WebSocketClient.class);
	private WebSocketContainer container;
	private SMSInstance smsService;
	private Session session = null;
	private boolean stoped = false;

	public WebSocketClient(SMSInstance smsService) {
		this.smsService = smsService;
	}

	public WebSocketClient() {
		logger.info("Default constructor");
	}

	public void initWSClient() throws WSConnectionException {
		
		initWSClient(null);
	}
	
	
	public void stopService()
	{
		try 
		{
			if(this.session != null && this.session.isOpen())
			{
				this.session.close();
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		this.stoped = true;
	}
	public void setSMSService(SMSInstance smsService) {
		this.smsService = smsService;
	}
	
	public void initWSClient(SMSInstance smss) throws WSConnectionException
	{
		try
		{
			if(smss != null)
			{
				this.smsService = smss;
			}
			String url = Config.getSsClientEndpoint();
			this.container = ContainerProvider.getWebSocketContainer(); 	

			ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
			configBuilder.configurator(new Configurator() {
			    @Override
			    public void beforeRequest(Map<String, List<String>> headers) 
			    {
			        headers.put("Authorization", Utility.asList(basicAuth(Config.getWsClientUsername(), Config.getWsClientPassword())));
			    }
			});
			ClientEndpointConfig clientConfig = configBuilder.build();
			this.session = container.connectToServer(new ClientEndpoint(this, smsService), clientConfig, URI.create(url)); 
			wait4TerminateSignal();
		} 
		catch (DeploymentException | IOException e) 
		{
			throw new WSConnectionException(e);
		}
		finally
		{
			if(this.session!=null)
			{
				try {
					this.session.close();
				} 
				catch (Exception e) {     
					e.printStackTrace();
				}
			}         
		}
	}
	private static String basicAuth(String username, String password)
	{
		return "Basic " + Utility.base64Encode(username+":"+password);
	}

	private static void  wait4TerminateSignal()
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
				logger.error(e.getMessage());
			}
		}
	}
	@Override
	public void run()
	{
		logger.info("Reconnect...");
		this.container = null;
		this.session = null;
		boolean connected = false;
		do
		{
			try 
			{
				this.initWSClient(this.smsService);
				connected = true;
			} 
			catch (WSConnectionException e) 
			{
				connected = false;
				e.printStackTrace();
				try 
				{
					Thread.sleep(Config.getReconnectDelay());
				} 
				catch (InterruptedException e1) 
				{
					e1.printStackTrace();
					Thread.currentThread().interrupt();
				}
				try 
				{
					this.initWSClient(this.smsService);
				} 
				catch (WSConnectionException e1) 
				{
					e1.printStackTrace();
				}
			}
		}
		while(!connected && !this.stoped);
	}

	public void reconnect() {
		try {
			this.initWSClient(this.smsService);
		} catch (WSConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

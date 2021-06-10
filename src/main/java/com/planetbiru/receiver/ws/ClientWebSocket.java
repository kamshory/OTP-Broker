package com.planetbiru.receiver.ws;

import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.planetbiru.OTPApplication;

@ClientEndpoint
public class ClientWebSocket {
	private static Object waitLock = new Object();
    private static Logger logger = LogManager.getLogger(ClientWebSocket.class);

	public void init()
	{
		WebSocketContainer container=null;
		Session session = null;
		try
		{
			//Tyrus is plugged via ServiceLoader API. See notes above
			container = ContainerProvider.getWebSocketContainer(); 
			
			//WS1 is the context-root of my web.app 
			//ratesrv is the  path given in the ServerEndPoint annotation on server implementation
			session = container.connectToServer(OTPApplication.class, URI.create("ws://localhost:8888/ws")); 
					
			wait4TerminateSignal();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if(session!=null)
			{
				try {
					session.close();
				} 
				catch (Exception e) {     
					e.printStackTrace();
				}
			}         
		}
	}
	@OnMessage
	public void onMessage(String message) {
		System.out.println(message);
		logger.info("Received Message : {}", message);        
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

}

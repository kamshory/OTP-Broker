package com.planetbiru;

import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


import com.planetbiru.config.Config;

@ClientEndpoint
@SpringBootApplication
public class OTPApplication implements ApplicationContextAware {
	

	private static Logger logger = LogManager.getLogger(OTPApplication.class);
	private static Object waitLock = new Object();
	private static ApplicationContext appContentx;
	public static void main(String[] args) {
		SpringApplication.run(OTPApplication.class);
		WebSocketContainer container = null;
		Session session = null;
		try
		{
			String url = Config.getWebsocketURL();
			container = ContainerProvider.getWebSocketContainer(); 			
			session=container.connectToServer(OTPApplication.class, URI.create(url)); 
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
				catch (Exception e) 
				{     
					e.printStackTrace();
				}
			}         
		} 
	}
	
	
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        appContentx = applicationContext;
    }
    
    public static ApplicationContext getApplicationContext() {
		return appContentx;
	}
	
	@OnMessage
	public void onMessage(String message) 
	{
		/**
		 * Do nothing
		 */
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
				logger.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
		}
	}
	
	
}

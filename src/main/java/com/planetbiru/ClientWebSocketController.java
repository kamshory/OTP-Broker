package com.planetbiru;

import java.io.IOException;

import javax.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.planetbiru.receiver.ws.WebSocketClient;

@Service
public class ClientWebSocketController {
	private Logger logger = LogManager.getLogger(ClientWebSocketController.class);
	private WebSocketClient client = new WebSocketClient();
	@PostConstruct
	public void init()
	{
		this.client.start();
	}

	public void destroy()
	{
		if(this.client.getSession() != null)
		{
			try 
			{
				this.client.getSession().close();
			} 
			catch (IOException e) 
			{
				logger.error(e.getMessage());
			}
		}
		this.client.setContainer(null);
		WebSocketClient.setStoped(true);
	}
	
	
}

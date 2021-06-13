package com.planetbiru;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.planetbiru.receiver.ws.WebSocketClient;

@Service
public class WebSocketReceiver {
	private WebSocketClient client = new WebSocketClient();
	@PostConstruct
	public void init()
	{
		this.client.start();
	}
	
}

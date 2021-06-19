package com.planetbiru.receiver.ws;

import com.planetbiru.config.ConfigFeederWS;

public class WebSocketTool extends Thread{
	private WebSocketClient client;
	private long reconnectDelay = 10000;
	
	public WebSocketTool(long reconnectDelay)
	{
		this.reconnectDelay = reconnectDelay;
		this.client = new WebSocketClient(this);
	}
	
	@Override
	public void run()
	{
		if(ConfigFeederWS.isFeederWsEnable())
		{
			this.client = new WebSocketClient(this);
			this.client.start();
		}
	}

	public void restartThread() {
		try 
		{
			Thread.sleep(this.reconnectDelay);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
		this.client = new WebSocketClient(this);
		this.client.setStoped(false);
		this.client.start();
		
	}
}


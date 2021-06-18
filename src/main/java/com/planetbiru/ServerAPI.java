package com.planetbiru;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Service;

import com.planetbiru.api.HandlerAPIMessage;
import com.planetbiru.config.ConfigAPI;
import com.sun.net.httpserver.HttpServer;

@Service
public class ServerAPI {
	private int port = 8088;
	private String pathSendSMS = "/send-sms";

	@PostConstruct
	public void init()
	{
		if(ConfigAPI.isEenable())
		{
			try 
			{
				ServiceHTTP.httpServer = HttpServer.create(new InetSocketAddress(this.port), 0);
		        ServiceHTTP.httpServer.createContext(this.pathSendSMS, new HandlerAPIMessage());
		        ServiceHTTP.httpServer.start();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	@PreDestroy
	public void destroy()
	{
		ServiceHTTP.httpServer.start();
	}
}

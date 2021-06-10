package com.planetbiru.receiver.amqp;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class RabbitMQReceiver implements MessageListener{
	
	public void onMessage(Message message) 
	{
		String msg = new String(message.getBody());
		System.out.println("RECEIVE : "+msg);
	}

}

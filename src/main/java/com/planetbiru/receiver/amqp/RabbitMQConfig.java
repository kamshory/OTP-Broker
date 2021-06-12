package com.planetbiru.receiver.amqp;

import javax.annotation.PostConstruct;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

	@Value("${sms.rabbitmq.username}")
	String username;

	@Value("${sms.rabbitmq.password}")
	private String password;

	@Value("${sms.rabbitmq.host}")
	private String rabbitMQHost;

	@Value("${sms.rabbitmq.port}")
	private int rabbitMQPort;

	@Value("${sms.mq.queue}")
	private String normalQueueName;

	@Bean
	Queue normalQueue() {
		return new Queue(normalQueueName, false);
	}

	@PostConstruct
    private void init()
    {
    }
	
	

	@Bean
	ConnectionFactory normalConnectionFactory() {
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
		cachingConnectionFactory.setAddresses(this.rabbitMQHost);
		cachingConnectionFactory.setPort(this.rabbitMQPort);
		cachingConnectionFactory.setUsername(this.username);
		cachingConnectionFactory.setPassword(this.password);
		return cachingConnectionFactory;
	}
	
	@Bean
	MessageListenerContainer normalMessageListenerContainer() {
		SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
		simpleMessageListenerContainer.setConnectionFactory(normalConnectionFactory());
		simpleMessageListenerContainer.setQueues(normalQueue());		
		simpleMessageListenerContainer.setMessageListener(normalCreateHandler());
		return simpleMessageListenerContainer;
	}
	
	@Bean
	RabbitMQReceiver normalCreateHandler() {
		return new RabbitMQReceiver();
	}
	
}


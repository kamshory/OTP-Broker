package com.planetbiru;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.receiver.amqp.RabbitMQReceiver;


@Configuration
public class ClientReceiverAMQP {

	@Value("${otpbroker.rabbitmq.username}")
	private String username;

	@Value("${otpbroker.rabbitmq.password}")
	private String password;

	@Value("${otpbroker.rabbitmq.host}")
	private String rabbitMQHost;

	@Value("${otpbroker.rabbitmq.port}")
	private int rabbitMQPort;

	@Value("${otpbroker.rabbitmq.queue}")
	private String queueName;

	@Value("${otpbroker.path.setting.feeder.amqp}")
	private String feederAMQPSettingPath;

	@Value("${otpbroker.rabbitmq.enable}")
	private boolean enable;
	
	@Value("${otpbroker.rabbitmq.ssl}")
	private boolean rabbitMQSSL;

	
	@Bean
	MessageListenerContainer messageListenerContainer() {
		
		boolean lEnable = this.enable;
		if(ConfigFeederAMQP.isLoaded())
		{
			lEnable = ConfigFeederAMQP.isFeederAmqpEnable();
		}
		if(lEnable)
		{
			SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
			
			simpleMessageListenerContainer.setConnectionFactory(connectionFactory());
			simpleMessageListenerContainer.setQueues(queue());		
			simpleMessageListenerContainer.setMessageListener(createHandler());
			ConfigFeederAMQP.setFactory(simpleMessageListenerContainer.getConnectionFactory());
			return simpleMessageListenerContainer;
		}
		else
		{
			return null;
		}
	}
	
	@Bean
	RabbitMQReceiver createHandler() {
		return new RabbitMQReceiver();
	}
	
	@Bean
	Queue queue() {
		String lQueue = this.queueName;
		if(ConfigFeederAMQP.isLoaded())
		{
			lQueue = ConfigFeederAMQP.getFeederAmqpChannel();
		}
		return new Queue(lQueue, false);
	}

	@PostConstruct
    private void init()
    {
		Config.setFeederAMQPSettingPath(feederAMQPSettingPath);
		ConfigFeederAMQP.load(Config.getFeederAMQPSettingPath());
    }	

	@Bean
	ConnectionFactory connectionFactory() {
		com.rabbitmq.client.ConnectionFactory  rabbitmqConnectionfactory = new com.rabbitmq.client.ConnectionFactory();		
		boolean ssl = this.rabbitMQSSL;
		String lRabbitMQHost = this.rabbitMQHost;
		int lRabbitMQPort = this.rabbitMQPort;
		String lUsername = this.username;
		String lPassword = this.password;
		
		if(ConfigFeederAMQP.isLoaded())
		{
			lRabbitMQHost = ConfigFeederAMQP.getFeederAmqpAddress();
			lRabbitMQPort = ConfigFeederAMQP.getFeederAmqpPort();
			lUsername = ConfigFeederAMQP.getFeederAmqpUsername();
			lPassword = ConfigFeederAMQP.getFeederAmqpPassword();
			ssl = ConfigFeederAMQP.isFeederAmqpSSL();
			ConfigFeederAMQP.setConnected(true);
		}
		
		if(ssl)
		{
			SSLContext sslContext;		
    		try {
    			sslContext = SSLContext.getInstance("TLS");
    			sslContext.init(null,null,null);
    			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();    	  		
    			rabbitmqConnectionfactory.setSocketFactory(sslSocketFactory); 		
    		} catch (Exception e) {
    			/**
    			 * Do nothing
    			 */
    		}
		}
		
		rabbitmqConnectionfactory.setHost(lRabbitMQHost);
		rabbitmqConnectionfactory.setPort(lRabbitMQPort);
		rabbitmqConnectionfactory.setUsername(lUsername);
		rabbitmqConnectionfactory.setPassword(lPassword);

		return new CachingConnectionFactory(rabbitmqConnectionfactory);
	}
	
}


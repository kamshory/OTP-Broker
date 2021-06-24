package com.planetbiru;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.exception.FatalListenerStartupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
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
	
	@Value("${otpbroker.path.base.setting}")
	private String baseDirConfig;

	@PostConstruct
    private void init()
    {
		/**
		 * This configuration must be loaded first
		 */
		Config.setBaseDirConfig(baseDirConfig);

		Config.setFeederAMQPSettingPath(feederAMQPSettingPath);
		ConfigFeederAMQP.load(Config.getFeederAMQPSettingPath());
    }
	
	@Bean
	MessageListenerContainer messageListenerContainer() {
		boolean lEnable = this.enable;
		if(ConfigFeederAMQP.isLoaded())
		{
			lEnable = ConfigFeederAMQP.isFeederAmqpEnable();
		}
		
		if(lEnable)
		{
			boolean test = this.canConnect();
			if(!test)
			{
				ConfigFeederAMQP.setConnected(false);
				return null;
			}
			SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();	
			ConnectionFactory con = connectionFactory();		
			simpleMessageListenerContainer.setConnectionFactory(con);
			simpleMessageListenerContainer.setQueues(queue());	
			try
			{
				simpleMessageListenerContainer.setMessageListener(createHandler());
			}
			catch(FatalListenerStartupException e)
			{
				e.printStackTrace();
				return null;
			}
			ConfigFeederAMQP.setFactory(simpleMessageListenerContainer.getConnectionFactory());
			ConfigFeederAMQP.setConnected(true);
			return simpleMessageListenerContainer;
		}
		else
		{
			return null;
		}
	}
	
	@Bean
	ApplicationRunner runner(ConnectionFactory cf) {
	    return args -> {
	        boolean open = false;
	        while(!open) {
	            try {
	                cf.createConnection().close();
	                open = true;
	            }
	            catch (Exception e) {
	                Thread.sleep(5000);
	            }
	        }
	    };
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
	
	private boolean canConnect()
	{
		com.rabbitmq.client.ConnectionFactory  factory = new com.rabbitmq.client.ConnectionFactory();				
		factory.setHost(ConfigFeederAMQP.getFeederAmqpAddress());
		factory.setPort(ConfigFeederAMQP.getFeederAmqpPort());
		factory.setUsername(ConfigFeederAMQP.getFeederAmqpUsername());
		factory.setPassword(ConfigFeederAMQP.getFeederAmqpPassword());	
		
		if(ConfigFeederAMQP.isFeederAmqpSSL())
		{
			SSLContext sslContext;		
    		try {
    			sslContext = SSLContext.getInstance("TLS");
    			sslContext.init(null,null,null);
    			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();    	  		
    			factory.setSocketFactory(sslSocketFactory); 		
    		} 
    		catch (Exception e) 
    		{
    			/**
    			 * Do nothing
    			 */
    		}
		}
		
		try(
				com.rabbitmq.client.Connection connection = factory.newConnection();
				com.rabbitmq.client.Channel channel = connection.createChannel()
		) 
		{
			String queueTestName = "__test__";
			channel.queueDeclare(queueTestName, true, false, false, null);
			/**
			 * String message = "TEST";
			 * channel.basicPublish("", queueTestName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
			 */		
			return true;
		} 
		catch (IOException | TimeoutException e) 
		{
			return false;
		}
		
	}
	
}


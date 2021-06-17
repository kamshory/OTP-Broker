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

import com.planetbiru.config.ConfigFeederAMQP;


@Configuration
public class RabbitMQConfig {
	//private Logger logger = LogManager.getLogger(RabbitMQConfig.class);	

	@Value("${otpbroker.rabbitmq.username}")
	String username;

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
		ConfigFeederAMQP.load(feederAMQPSettingPath);
    }
	

	@Bean
	ConnectionFactory connectionFactory() {
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
		
		
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
			ConfigFeederAMQP.setConnected(true);
		}
		
		cachingConnectionFactory.setAddresses(lRabbitMQHost);
		cachingConnectionFactory.setPort(lRabbitMQPort);
		cachingConnectionFactory.setUsername(lUsername);
		cachingConnectionFactory.setPassword(lPassword);
		return cachingConnectionFactory;
	}
	
}


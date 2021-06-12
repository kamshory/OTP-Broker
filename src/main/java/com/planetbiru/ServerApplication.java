package com.planetbiru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

@SpringBootApplication
public class ServerApplication {
	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		context = SpringApplication.run(ServerApplication.class);
		
		
	}
	
	public static void restart() {
	    Thread thread = new Thread(() -> {
	        if(context != null)
	        {
	        	context.close();
	        }
	        context = SpringApplication.run(ServerApplication.class);
	    });

	    AbstractApplicationContext appContext = new AnnotationConfigApplicationContext();
	    appContext.registerShutdownHook();
	    appContext.refresh();
	    appContext.close();

	    thread.setDaemon(false);
	    thread.start();
	}
	
	
	
}

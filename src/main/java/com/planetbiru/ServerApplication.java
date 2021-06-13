package com.planetbiru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.planetbiru.util.ProcessKiller;

@SpringBootApplication
public class ServerApplication {
	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		
		context = SpringApplication.run(ServerApplication.class);
	}
	public static void stop()
	{
		System.out.println("Stop app");
		ProcessKiller killer = new ProcessKiller("java.exe", true);
		killer.stop();		
	}
	public static void restart() {
	    Thread thread = new Thread(() -> {
	        if(context != null)
	        {
	        	context.close();
	        }
	        context = SpringApplication.run(ServerApplication.class);
	    });

	    thread.setDaemon(false);	    
	    thread.start();

	}
	
	
	
}


package com.planetbiru;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;
import com.planetbiru.util.ProcessKiller;

@SpringBootApplication
@EnableAutoConfiguration
@EnableConfigurationProperties
public class ServerApplication {
	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		resetConfig();
		context = SpringApplication.run(ServerApplication.class);
	}
	
	private static void resetConfig() 
	{
		if(usbPluged())
		{
			String defaultConfigDHCP = "/static/default-config/dhcp.json";
			String defaultConfigWLAN = "/static/default-config/wlan.json";
			String defaultConfigEthernet = "/static/default-config/ethernet.json";
	
			String configDHCP = "/static/config/dhcp.json";
			String configWLAN = "/static/config/wlan.json";
			String configEthernet = "/static/config/ethernet.json";
			
			ConfigNetDHCP.load(defaultConfigDHCP);
			ConfigNetWLAN.load(defaultConfigWLAN);
			ConfigNetEthernet.load(defaultConfigEthernet);
			
			ConfigNetDHCP.save(configDHCP);
			ConfigNetWLAN.save(configWLAN);
			ConfigNetEthernet.save(configEthernet);
			
			ConfigNetDHCP.apply();
			ConfigNetWLAN.apply();
			ConfigNetEthernet.apply();
		}
		
	}
	private static boolean usbPluged() {
		return false;
	}
	public static void stop()
	{
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


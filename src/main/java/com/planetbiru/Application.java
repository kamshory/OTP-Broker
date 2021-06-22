package com.planetbiru;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;
import com.planetbiru.util.ProcessKiller;

@SpringBootApplication
@EnableAutoConfiguration
@EnableConfigurationProperties
public class Application {
	
	@Value("${otpbroker.path.setting.dhcp}")
	private String dhcpSettingPath;

	@Value("${otpbroker.path.setting.wlan}")
	private String wlanSettingPath;

	@Value("${otpbroker.path.setting.ethernet}")
	private String ethernetSettingPath;

	@Value("${otpbroker.path.setting.dhcp.default}")
	private String dhcpSettingPathDefault;

	@Value("${otpbroker.path.setting.wlan.default}")
	private String wlanSettingPathDefault;

	@Value("${otpbroker.path.setting.ethernet.default}")
	private String ethernetSettingPathDefault;

	@Value("${otpbroker.path.os.wlan}")
	private String osWLANConfigPath;

	@Value("${otpbroker.path.os.ssid.key}")
	private String osSSIDKey;

	@Value("${otpbroker.path.os.ethernet}")
	private String osEthernetConfigPath;

	@Value("${otpbroker.path.os.dhcp}")
	private String osDHCPConfigPath;

	private static Logger logger = LogManager.getLogger(Application.class);
	
	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		logger.info("public static void main(String[] args)");
		context = SpringApplication.run(Application.class);
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
	        context = SpringApplication.run(Application.class);
	    });

	    thread.setDaemon(false);	    
	    thread.start();

	}
	
	@PostConstruct
	public void init()
	{
		
		Config.setDhcpSettingPath(dhcpSettingPath);
		Config.setWlanSettingPath(wlanSettingPath);
		Config.setEthernetSettingPath(ethernetSettingPath);
		
		Config.setDhcpSettingPathDefault(dhcpSettingPathDefault);
		Config.setWlanSettingPathDefault(wlanSettingPathDefault);
		Config.setEthernetSettingPathDefault(ethernetSettingPathDefault);		
		
		
		Config.setOsWLANConfigPath(osWLANConfigPath);
		Config.setOsSSIDKey(osSSIDKey);
		Config.setOsEthernetConfigPath(osEthernetConfigPath);
		Config.setOsDHCPConfigPath(osDHCPConfigPath);

		
		resetConfig();
	}
	
	
	private static void resetConfig() 
	{
		if(usbPluged())
		{
			String defaultConfigDHCP = Config.getDhcpSettingPathDefault();
			String defaultConfigWLAN = Config.getWlanSettingPathDefault();
			String defaultConfigEthernet = Config.getEthernetSettingPathDefault();
	
			String configDHCP = Config.getDhcpSettingPath();
			String configWLAN = Config.getWlanSettingPath();
			String configEthernet = Config.getEthernetSettingPath();
			
			ConfigNetDHCP.load(defaultConfigDHCP);
			ConfigNetWLAN.load(defaultConfigWLAN);
			ConfigNetEthernet.load(defaultConfigEthernet);
			
			ConfigNetDHCP.save(configDHCP);
			ConfigNetWLAN.save(configWLAN);
			ConfigNetEthernet.save(configEthernet);
			
			ConfigNetDHCP.apply(Config.getOsDHCPConfigPath());
			ConfigNetWLAN.apply(Config.getOsWLANConfigPath(), Config.getOsWLANConfigPath());
			ConfigNetEthernet.apply(Config.getOsEthernetConfigPath());
		}
		
	}
	
	private static boolean usbPluged() {
		return false;
	}
	
}


package com.planetbiru;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;

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
	
	@Value("${otpbroker.path.base.setting}")
	private String baseDirConfig;

	@Value("${otpbroker.ssh.username}")
	private String sshUsername;

	@Value("${otpbroker.ssh.password}")
	private String sshPassword;

	@Value("${otpbroker.ssh.host}")
	private String sshHost;

	@Value("${otpbroker.ssh.port}")
	private int sshPort;

	@Value("${otpbroker.ssh.sleep}")
	private long sshSleep;

	@Value("${otpbroker.ssh.enable}")
	private boolean sshEnable;

	@Value("${otpbroker.ssh.reboot.command}")
	private String rebootCommand;

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

	@PostConstruct
	public void init()
	{
		/**
		 * This configuration must be loaded first
		 */
		Config.setBaseDirConfig(baseDirConfig);
		
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
		
		Config.setSshUsername(sshUsername);
		Config.setSshPassword(sshPassword);
		Config.setSshHost(sshHost);
		Config.setSshPort(sshPort);
		Config.setSshSleep(sshSleep);
		Config.setSshEnable(sshEnable);
		Config.setRebootCommand(rebootCommand);
		
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


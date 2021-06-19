package com.planetbiru;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.receiver.ws.WebSocketTool;

@Service
public class ClientReceiverWS {
	private WebSocketTool tool;

	@Value("${otpbroker.path.setting.feeder.ws}")
	private String feederWSSettingPath;

	@Value("${otpbroker.ws.enable}")
	private boolean feederWsEnable;

	@Value("${otpbroker.ws.ssl}")
	private boolean feederWsSSL;

	@Value("${otpbroker.ws.address}")
	private String feederWsAddress;

	@Value("${otpbroker.ws.port}")
	private int feederWsPort;

	@Value("${otpbroker.ws.path}")
	private String feederWsPath;

	@Value("${otpbroker.ws.username}")
	private String feederWsUsername;

	@Value("${otpbroker.ws.password}")
	private String feederWsPassword;

	@Value("${otpbroker.ws.channel}")
	private String feederWsChannel;

	@Value("${otpbroker.ws.timeout}")
	private long feederWsTimeout;

	@Value("${otpbroker.ws.refresh.delay}")
	private long feederWsRefresh;

	@Value("${otpbroker.ws.reconnect.delay}")
	private long feederWsReconnectDelay;
	
	@PostConstruct
	public void init()
	{
		
		ConfigFeederWS.setFeederWsEnable(feederWsEnable);
		ConfigFeederWS.setFeederWsSSL(feederWsSSL);
		ConfigFeederWS.setFeederWsAddress(feederWsAddress);
		ConfigFeederWS.setFeederWsPort(feederWsPort);
		ConfigFeederWS.setFeederWsPath(feederWsPath);
		ConfigFeederWS.setFeederWsUsername(feederWsUsername);
		ConfigFeederWS.setFeederWsPassword(feederWsPassword);
		ConfigFeederWS.setFeederWsChannel(feederWsChannel);
		ConfigFeederWS.setFeederWsTimeout(feederWsTimeout);
		ConfigFeederWS.setFeederWsReconnectDelay(feederWsReconnectDelay);
		ConfigFeederWS.setFeederWsRefresh(feederWsRefresh);
		
		Config.setFeederWSSettingPath(feederWSSettingPath);

		ConfigFeederWS.load(Config.getFeederWSSettingPath());
		this.tool = new WebSocketTool(ConfigFeederWS.getFeederWsReconnectDelay());
		this.tool.start();
	}
	
}

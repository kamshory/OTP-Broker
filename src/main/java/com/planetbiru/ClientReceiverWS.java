package com.planetbiru;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.receiver.ws.WebSocketTool;

@Service
public class ClientReceiverWS {
	@Value("${otpbroker.ws.reconnect.delay}")
	private int reconnectDelay;

	@Value("${otpbroker.path.setting.feeder.ws}")
	private String feederWSSettingPath;

	private WebSocketTool tool;
	
	@PostConstruct
	public void init()
	{
		Config.setFeederWSSettingPath(feederWSSettingPath);
		ConfigFeederWS.load(Config.getFeederWSSettingPath());
		this.tool = new WebSocketTool(reconnectDelay);
		this.tool.start();
	}
	
}

package com.planetbiru;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.receiver.ws.WebSocketClient;

@Service
public class WebSocketReceiver {
	@Value("${otpbroker.ws.reconnect.delay}")
	private int reconnectDelay;

	@Value("${otpbroker.path.setting.feeder.ws}")
	private String feederWSSettingPath;

	private WebSocketClient client = new WebSocketClient();
	@PostConstruct
	public void init()
	{
		ConfigFeederWS.load(feederWSSettingPath);
		if(ConfigFeederWS.isFeederWsEnable())
		{
			this.client.start();
		}
	}
	
}

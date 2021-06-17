package com.planetbiru;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.planetbiru.receiver.ws.WebSocketTool;

@Service
public class WebSocketReceiver {
	@Value("${otpbroker.ws.reconnect.delay}")
	private int reconnectDelay;

	@Value("${otpbroker.path.setting.feeder.ws}")
	private String feederWSSettingPath;

	private WebSocketTool tool = new WebSocketTool();
	
	@PostConstruct
	public void init()
	{
		this.tool = new WebSocketTool(reconnectDelay);
		tool.start();
	}
	
}

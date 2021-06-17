package com.planetbiru.wstools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.planetbiru.ServerWebSocketManager;

@Configuration
public class WebSocketConfigurator {
	@Bean
    public ServerWebSocketManager serverEndpoint() {
        return new ServerWebSocketManager();
    }

	@Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}

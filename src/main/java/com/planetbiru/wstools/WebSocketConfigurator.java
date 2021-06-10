package com.planetbiru.wstools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.planetbiru.ServerWebSocket;

@Configuration
public class WebSocketConfigurator {
	@Bean
    public ServerWebSocket serverEndpoint() {
        return new ServerWebSocket();
    }

	@Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}

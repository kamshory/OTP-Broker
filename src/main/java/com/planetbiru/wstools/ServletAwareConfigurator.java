package com.planetbiru.wstools;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServletAwareConfigurator extends ServerEndpointConfig.Configurator{

	private static Logger logger = LogManager.getLogger(ServletAwareConfigurator.class);
	@Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpServletRequest httpservletRequest = getField(request, HttpServletRequest.class);
        String sClientIP = "";
        if(httpservletRequest != null)
        {
        	sClientIP = httpservletRequest.getRemoteAddr();
        }
        Map<String, List<String>> requestHeader = request.getHeaders();
        Map<String, List<String>> responseHeader = response.getHeaders();
        Map<String, List<String>> parameters = request.getParameterMap();
        config.getUserProperties().put("remote_address", sClientIP);
        config.getUserProperties().put("parameter", parameters);
        config.getUserProperties().put("request_header", requestHeader);
        config.getUserProperties().put("response_header", responseHeader);
    }

    //hacking reflector to expose fields...
    @SuppressWarnings("unchecked")
	private static < I, F > F getField(I instance, Class < F > fieldType) {
        try {
            for (Class < ? > type = instance.getClass(); type != Object.class; type = type.getSuperclass()) {
                for (Field field: type.getDeclaredFields()) {
                    if (fieldType.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        return (F) field.get(instance);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }
}

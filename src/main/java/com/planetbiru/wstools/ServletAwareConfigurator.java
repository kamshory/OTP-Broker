package com.planetbiru.wstools;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class ServletAwareConfigurator extends ServerEndpointConfig.Configurator{

	@Override
    public void modifyHandshake(ServerEndpointConfig serverEndpointConfig, HandshakeRequest request, HandshakeResponse response) {
        HttpServletRequest httpservletRequest = getField(request, HttpServletRequest.class);
        String sClientIP = "";
        if(httpservletRequest != null)
        {
        	sClientIP = httpservletRequest.getRemoteAddr();
        }
        Map<String, List<String>> requestHeader = request.getHeaders();
        Map<String, List<String>> responseHeader = response.getHeaders();
        Map<String, List<String>> parameters = request.getParameterMap();
        serverEndpointConfig.getUserProperties().put("remote_address", sClientIP);
        serverEndpointConfig.getUserProperties().put("parameter", parameters);
        serverEndpointConfig.getUserProperties().put("request_header", requestHeader);
        serverEndpointConfig.getUserProperties().put("response_header", responseHeader);
    }

    @SuppressWarnings("unchecked")
	private static < I, F > F getField(I instance, Class < F > fieldType) {
        try 
        {
            for (Class < ? > type = instance.getClass(); type != Object.class; type = type.getSuperclass()) 
            {
                for (Field field: type.getDeclaredFields()) 
                {
                    if (fieldType.isAssignableFrom(field.getType())) 
                    {
                        field.setAccessible(true);
                        return (F) field.get(instance);
                    }
                }
            }
        } 
        catch (SecurityException | IllegalArgumentException | IllegalAccessException e) 
        {
            /**
             * Do nothing
             */
        }
        return null;
    }
}

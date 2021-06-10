package com.planetbiru.wstools;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class MessageDecoder implements Decoder.Text<String> {
	@Override
    public String decode(String s) throws DecodeException {
        return s;
    }

    @Override
    public boolean willDecode(String s) {
        return (s != null);
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
    	/**
    	 * Do nothing
    	 */
    }

    @Override
    public void destroy() {
    	/**
    	 * Do nothing
    	 */
    }

}

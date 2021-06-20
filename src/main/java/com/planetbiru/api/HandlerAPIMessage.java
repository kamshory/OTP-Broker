package com.planetbiru.api;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.planetbiru.ServerScheduler;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.util.Utility;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HandlerAPIMessage implements HttpHandler {
	
	private Logger logger = LogManager.getLogger(HandlerAPIMessage.class);

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		Headers requestHeaders = httpExchange.getRequestHeaders();
        Headers responseHeaders = httpExchange.getResponseHeaders();
       	
       	RESTAPI.validRequest(requestHeaders);
           	
        if(
           	httpExchange.getRequestMethod().equalsIgnoreCase("POST")
        	|| 
        	httpExchange.getRequestMethod().equalsIgnoreCase("PUT")
        	)
        {
            byte[] requestBody = this.getRequestBody(httpExchange);
            String requestBodyStr = new String(requestBody);
            
            JSONObject result = RESTAPI.processMessageRequest(requestBodyStr);
            
            String response = result.toString(4);
            
            responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());	 
            httpExchange.getResponseBody().write(response.getBytes());
        }
        else
        {
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);	        	
        }
        httpExchange.close();
	}
	private byte[] getRequestBody(HttpExchange httpExchange)
	{
        Headers requestHeaders = httpExchange.getRequestHeaders();
		String cl = requestHeaders.getFirst("Content-length");
		byte[] requestBody = "".getBytes();
        if(cl != null)
        {
            try
            {
	            int contentLength = Utility.atoi(cl);	
	             requestBody = new byte[contentLength];
	            for(int j = 0; j < contentLength; j++)
	            {
	            	requestBody[j] = (byte) httpExchange.getRequestBody().read();
	            }
            }
            catch(NumberFormatException | IOException e)
            {
            	e.printStackTrace();
            }
            return requestBody;
        }
        else
        {
        	return "".getBytes();
        }
	}

}

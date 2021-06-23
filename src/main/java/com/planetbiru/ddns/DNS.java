package com.planetbiru.ddns;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.planetbiru.config.Config;
import com.planetbiru.util.ResponseEntityCustom;
import com.planetbiru.util.Utility;

public class DNS {
	
	private static final Logger logger = LoggerFactory.getLogger(DNS.class);

	public String getIP() 
	{
		String ip = "";
		try 
		{
			ip = this.getIP("ipv4");
		} 
		catch (Exception e) 
		{
			logger.error(e.getMessage());
		}
		return ip;
	}

	public String getIP(String protocol) throws DDNSException
	{
		List<String> allowedTypes = new ArrayList<>();
		allowedTypes.add("ipv4");
		allowedTypes.add("ipv6");
		allowedTypes.add("auto");
		if(!allowedTypes.contains(protocol))
		{
			throw new DDNSException("Invalid \"protocol\" config value. Allowed : " + allowedTypes.toString());
		}
		
		String prefix = protocol.equals("auto")?"":(protocol+".");
		String url = "http://"+prefix+"icanhazip.com/";
		
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add(DDNSKey.HEADER_USER_AGENT, "OTP Broker");
		int timeout = 10000;
		ResponseEntityCustom response = this.httpExchange(HttpMethod.GET, url, requestHeaders, "", timeout);
		return response.getBody();		
	}

	/**
	* Request to HTTP server
	* @param method Request method
	* @param url URL
	* @param requestHeaders Request headers
	* @param body Request body
	* @return ResponseEntityCustom Custom response entity
	*/
	public ResponseEntityCustom httpExchange(HttpMethod method, String url, HttpHeaders requestHeaders, String body, int timeout)
	{
		RestTemplate restTemplate = this.customRestTemplate(timeout);
		HttpEntity<String> entity = new HttpEntity<>(body, requestHeaders);	
		ResponseEntity<String> responseEntity;
		ResponseEntityCustom result = new ResponseEntityCustom();
		try
		{
			responseEntity = restTemplate.exchange(url, method, entity, String.class);
			result = new ResponseEntityCustom(responseEntity.getBody(), responseEntity.getStatusCodeValue(), responseEntity.getHeaders());
		}
		catch(RestClientResponseException e)
		{
			result = new ResponseEntityCustom(e.getResponseBodyAsString(), e.getRawStatusCode(), e.getResponseHeaders());
			logger.error(e.getMessage());
		}
		catch(ResourceAccessException e)
		{
			if(e.getCause() instanceof SocketTimeoutException)
			{
				result = new ResponseEntityCustom("", 408);
			}
			else if(e.getCause() instanceof UnknownHostException)
			{
				result = new ResponseEntityCustom("", 503);
			}
			else
			{
				result = new ResponseEntityCustom("", 504);
			}
			logger.error(e.getMessage());
		}
		return result;
	}

	/**
	* Create customer rest template
	* @param timeout Request timeout
	* @return RestTemplate
	*/
	public RestTemplate customRestTemplate(int timeout)
	{
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setConnectionRequestTimeout(timeout);
		httpRequestFactory.setConnectTimeout(timeout);
		httpRequestFactory.setReadTimeout(timeout);
		RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8));      
		if(Config.isProxyEnable()) 
		{
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Config.getProxyHost(), Config.getProxyPort()));
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setProxy(proxy);
			restTemplate.setRequestFactory(requestFactory);
		}    
		return restTemplate;
	}

	public String buildQuery(Map<String, String> params) 
	{
		return Utility.buildQuery(params);
	}


	public JSONObject update(DDNSRecord ddnsRecord) {
		return ddnsRecord.toJSONObject();
	}

	

}

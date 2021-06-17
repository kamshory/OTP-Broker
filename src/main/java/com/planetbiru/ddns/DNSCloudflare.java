package com.planetbiru.ddns;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
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

public class DNSCloudflare extends DNS{
	
	private static final Logger logger = LoggerFactory.getLogger(DNSCloudflare.class);

	private String authEmail = "";
	private String accountId = "";
	private String endpoint = "https://api.cloudflare.com/client/v4";
	private String authApiKey = "";
	private String authToken = "";
	
	@Override
	public void setConfig(String endpoint, String accountId, String authEmail, String authApiKey, String authToken)
	{
		this.endpoint = endpoint;
		this.accountId = accountId;
		this.authEmail = authEmail;
		this.authApiKey = authApiKey;
		this.authToken = authToken;
	}
	
	
	@Override
	public JSONObject createZoneIfNotExists(DDNSRecord ddnsRecord) {
		if(this.getZone(ddnsRecord.getZone()) == null)
		{
			return this.createZone(ddnsRecord);
		}
		return null;
	}
	
	@Override
	public JSONObject createZone(DDNSRecord ddnsRecord)
	{
		return this.createZone(ddnsRecord.getZone(), this.accountId);
	}
	
	public JSONObject createZone(String name)
	{
		return this.createZone(name, this.accountId);
	}
	
	/**
	 * Create Zone <br>
	 * URL : https://api.cloudflare.com/#zone-create-zone
	 * @param name The domain name
	 * @param params 
	 * @return
	 */
	public JSONObject createZone(String name, String accountId)
	{
		JSONObject json = new JSONObject();
		JSONObject account = new JSONObject();
		account.put("id", accountId);
		json.put("account", account);
		json.put("name", name);
		json.put("jump_start", true);
		json.put("type", "full");		
	
		String url = endpoint + "/zones";
		String body = json.toString();
		int timeout = 1000;
		HttpHeaders requestHeaders = this.createRequestHeader();
		requestHeaders.add(DDNSKey.HEADER_CONTENT_TYPE, "application/json");
		ResponseEntityCustom response = httpExchange(HttpMethod.POST, url, requestHeaders, body, timeout);
		JSONObject resp = new JSONObject();
		try
		{
		   resp = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}
		return resp;
	}
	
	/**
	 * List Zones <br>
	 * URL https://api.cloudflare.com/#zone-list-zones
	 * @param params List Zone Parameters <br>
	 * match (string) : enum(any, all) <br>
	 * name (string(253)) : valid domain name <br>
	 * account.name (string(100)) : Account name <br>
	 * order (string) : enum(name, status, account.id, account.name) <br>
	 * page (number) <br>
	 * per_page (number)
	 * @return
	 */
	public JSONArray listZones(Map<String, String> params)
	{
		String url = this.endpoint + "/zones";
		HttpHeaders requestHeaders = this.createRequestHeader();
		ResponseEntityCustom response = httpExchange(HttpMethod.GET, url, requestHeaders, null, 10000);
		if(response.getBody().length() > 20)
		{
			JSONObject resp = new JSONObject(response.getBody());
			return resp.optJSONArray(DDNSKey.RESULT);
		}
		else
		{
			return null;
		}
	}

	public JSONObject getZone(String name)
	{
		Map<String, String> params = new HashMap<>();
		params.put(DDNSKey.NAME, name);
		JSONArray zones = this.listZones(params);
		JSONObject zone = null;
		if(zones != null)
		{
			for(int i = 0; i<zones.length(); i++)
			{
				zone = zones.optJSONObject(i);
				if(zone.optString(DDNSKey.NAME, "").equals(name))
				{
					break;
				}
			}
		}
		return zone;
	}

	public JSONObject deleteZoneByName(String name) throws DDNSException
	{
		JSONObject zone = this.getZone(name);
		if(zone == null || zone.isEmpty())
		{
			throw new DDNSException("Domain "+name+" not found");
		}
		String zoneId = zone.optString("id", "");
		
		String url = this.endpoint + "/zones/"+zoneId;
		HttpHeaders requestHeaders = this.createRequestHeader();
		
		ResponseEntityCustom response = httpExchange(HttpMethod.DELETE, url, requestHeaders, null, 10000);
		
		JSONObject resp = new JSONObject();
		
		try
		{
			resp = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do noting
			 */
		}
		return resp;
	}

	/**
	 * Delete Zone
	 * @param zoneId Zone ID
	 * @return
	 */
	public JSONObject deleteZone(String zoneId)
	{
		String url = this.endpoint + "/zones/"+zoneId;
		HttpHeaders requestHeaders = this.createRequestHeader();
		
		ResponseEntityCustom response = httpExchange(HttpMethod.DELETE, url, requestHeaders, null, 10000);
		
		logger.info(response.getBody());
		JSONObject resp = new JSONObject();
		
		try
		{
			resp = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do noting
			 */
		}
		return resp;
	}


	public JSONObject getZoneDnsRecords(String zoneId, Map<String, String> params)
	{
		ResponseEntityCustom response = this.get("/zones/" + zoneId + "/dns_records", params, "application/x-www-form-urlencoded");
		JSONObject resp = new JSONObject();
				
		try
		{
			resp = new JSONObject(response.getBody());
		}
		catch(JSONException e)
		{
			/**
			 * Do noting
			 */
		}
		return resp;
	}
	
	private JSONObject createDnsRecord(String zoneId, String type, String name, String content, int ttl, boolean proxied)
	{		
		JSONObject json = new JSONObject();
	
		json.put("type", type);
		json.put(DDNSKey.NAME, name);
		json.put("content", content);
		json.put("ttl", ttl);
		json.put("proxied", proxied);
	
		String url = endpoint + "/zones/" + zoneId + "/dns_records";
		HttpHeaders requestHeaders = this.createRequestHeader("application/json");
		String body = json.toString();
		int timeout = 1000;
		ResponseEntityCustom response = httpExchange(HttpMethod.POST, url, requestHeaders, body, timeout);
				
		logger.info("Create Record Result {}", response.getBody());
		
		JSONObject resp = new JSONObject(response.getBody());
		return resp.optJSONObject(DDNSKey.RESULT);
	}
	
	public JSONObject update(DDNSRecord ddnsRecord)  
	{
		String ip = this.getIP();
		String domain = ddnsRecord.getZone();
		JSONObject zone = this.getZone(domain);
		JSONObject res = new JSONObject();
		if(zone == null)
		{
			zone = this.createZone(domain, this.accountId);
		}
		if(zone.has("id"))
		{
			logger.info("Zone Info {}", zone);
			String zoneId = zone.optString("id", "");
			String recordName = ddnsRecord.getRecordName().trim();
			boolean proxied = ddnsRecord.isProxied();
			if(!recordName.isEmpty())
			{
				Map<String, String> params1 = new HashMap<>();
				params1.put(DDNSKey.NAME, recordName);
				JSONObject records = this.getZoneDnsRecords(zoneId, params1);
				
				if(this.isRecordExists(records, recordName))
				{
					String recordId = this.getRecordId(records, recordName);
					res = this.updateDnsRecord(zoneId, ddnsRecord.getType(), ddnsRecord.getRecordName(), ip, ddnsRecord.getTtl(), proxied, recordId);
				}
				else
				{
					res = this.createDnsRecord(zoneId, ddnsRecord.getType(), ddnsRecord.getRecordName(), ip, ddnsRecord.getTtl(), proxied);
				}
			}
		}
		return res;
	}
	
	
	public String getRecordId(JSONObject records, String recordName) {
		if(records != null && records.has(DDNSKey.RESULT))
		{
			JSONArray recs = records.optJSONArray(DDNSKey.RESULT);
			if(recs != null && !recs.isEmpty())
			{
				for(int i = 0; i<recs.length(); i++)
				{
					JSONObject rec = recs.getJSONObject(i);
					if(rec != null && !recordName.isEmpty() && rec.optString(DDNSKey.NAME, "").equals(recordName))
					{
						return rec.optString("id");
					}
				}
			}
		}
		return "";
	}


	public boolean isRecordExists(JSONObject records, String recordName) {
		if(records != null && records.has(DDNSKey.RESULT))
		{
			JSONArray recs = records.optJSONArray(DDNSKey.RESULT);
			if(recs != null && !recs.isEmpty())
			{
				for(int i = 0; i<recs.length(); i++)
				{
					JSONObject rec = recs.getJSONObject(i);
					if(rec != null && !recordName.isEmpty() && rec.optString(DDNSKey.NAME, "").equals(recordName))
					{
						return true;
					}
				}
			}
		}
		return false;
	}


	public String getIP() 
	{
		String ip = "";
		try 
		{
			ip = this.getIP("ipv4");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
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
		logger.info("Send message to  : {}", url);
		logger.info("Request Headers  : {}", requestHeaders);
		logger.info("Request Body     : {}", body);
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

	/**
	* Issues an HTTPS request and returns the result
	*
	* @param string String method
	* @param string String endpoint
	* @param array  String params
	*
	* @throws Exception
	*
	* @return mixed
	*/
	public ResponseEntityCustom request(HttpMethod method, String endpoint, Map<String, String> params, String contentType)
	{
		int timeout = 10000;
		HttpHeaders headers = this.createRequestHeader(contentType);
		headers.add(DDNSKey.HEADER_CONTENT_TYPE, contentType);
		String body = "";
		if(contentType.contains("urlencode") && (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT) || method.equals(HttpMethod.PATCH)  || method.equals(HttpMethod.DELETE)))
		{
			body = this.buildQuery(params);
		}
		else if(contentType.contains("json") && (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT) || method.equals(HttpMethod.PATCH)  || method.equals(HttpMethod.DELETE)))
		{
			JSONObject obj = new JSONObject(params);
			body = obj.toString();
		}
		
		return this.httpExchange(method, endpoint, headers, body, timeout);
	}

	public String buildQuery(Map<String, String> params) 
	{
		return Utility.buildQuery(params);
	}

	public ResponseEntityCustom get(String endpoint, Map<String, String> params, String contentType)
	{
		String url = endpoint + endpoint;
		return this.request(HttpMethod.GET, url, params, contentType);
	}
	public ResponseEntityCustom post(String endpoint, Map<String, String> params, String contentType)
	{
		String url = endpoint + endpoint;
		return this.request(HttpMethod.POST, url, params, contentType);
	}
	public ResponseEntityCustom put(String endpoint, Map<String, String> params, String contentType)
	{
		String url = endpoint + endpoint;
		return this.request(HttpMethod.PUT, url, params, contentType);
	}
	public ResponseEntityCustom patch(String endpoint, Map<String, String> params, String contentType)
	{
		String url = endpoint + endpoint;
		return this.request(HttpMethod.PATCH, url, params, contentType);
	}
	public ResponseEntityCustom delete(String endpoint, Map<String, String> params, String contentType)
	{
		String url = endpoint + endpoint;
		return this.request(HttpMethod.DELETE, url, params, contentType);
	}
	public ResponseEntityCustom delete(String endpoint, String contentType)
	{
		String url = endpoint + endpoint;
		Map<String, String> params = new HashMap<>();
		return this.request(HttpMethod.DELETE, url, params, contentType);
	}
	
	public HttpHeaders createRequestHeader(String contentType)
	{
		HttpHeaders requestHeaders = this.createRequestHeader();
		requestHeaders.add(DDNSKey.HEADER_CONTENT_TYPE, contentType);
		return requestHeaders;
	}
	public HttpHeaders createRequestHeader() {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add(DDNSKey.HEADER_X_AUTH_EMAIL, this.authEmail);
		requestHeaders.add(DDNSKey.HEADER_X_AUTH_KEY, this.authApiKey);
		requestHeaders.add(DDNSKey.HEADER_USER_AGENT, "cloudflare-php");
		requestHeaders.add(DDNSKey.HEADER_CONNECTION, "close");
		return requestHeaders;
	}

	/**
	* Creates a zone if it doesn"t already exist.
	*
	* Returns information about the zone
	*/
	public JSONObject registerDnsZone(String name, String accountId)
	{
		JSONObject zone = this.getZone(name);
		if(zone != null)
		{
			return zone;
		}
		else
		{
			zone = this.createZone(name, accountId);
		}
		return zone;
	}

	public JSONObject setDnsZoneSsl(String zoneId, String type) throws DDNSException
	{
		List<String> allowedTypes = new ArrayList<>();
		allowedTypes.add("off");
		allowedTypes.add("flexible");
		allowedTypes.add("full");
		allowedTypes.add("full_strict");
		if(!allowedTypes.contains(type))
		{
			throw new DDNSException("SSL type not allowed. valid types are " + allowedTypes.toString());
		}
		Map<String, String> params = new HashMap<>();
		params.put(DDNSKey.VALUE, type);
	
		ResponseEntityCustom response = this.patch("/zones/" + zoneId + "/settings/ssl", params, "application/x-www-form-urlencoded");
		JSONObject resp = new JSONObject(response.getBody());
		return resp.optJSONObject(DDNSKey.RESULT);
	}

	public JSONObject setDnsZoneCache(String zoneId, String type) throws DDNSException
	{
		List<String> allowedTypes = new ArrayList<>();
		allowedTypes.add("aggressive");
		allowedTypes.add("basic");
		allowedTypes.add("simplified");
		if(!allowedTypes.contains(type))
		{
			throw new DDNSException("Cache type not allowed. valid types are " + allowedTypes.toString());
		}
	
		Map<String, String> params = new HashMap<>();
		params.put(DDNSKey.VALUE, type);
	
		ResponseEntityCustom response = this.patch("/zones/" + zoneId + "/settings/cache_level", params, "application/x-www-form-urlencoded");
		JSONObject resp = new JSONObject(response.getBody());
		return resp.optJSONObject(DDNSKey.RESULT);
	}

	public ResponseEntityCustom clearZoneCache(String zoneId)
	{
		Map<String, String> params = new HashMap<>();
		params.put("purge_everything", Boolean.toString(true));
		return this.delete("/zones/" + zoneId + "/purge_cache", params, "application/x-www-form-urlencoded");
	}

	public ResponseEntityCustom setDnsZoneMinify(String zoneId, String settings)
	{
		Map<String, String> params = new HashMap<>();
		params.put(DDNSKey.VALUE, settings);
		return this.patch("/zones/" + zoneId + "/settings/minify", params, "application/x-www-form-urlencoded");
	}

	public JSONObject updateDnsRecord(String zoneId, String type, String name, String content, int ttl, boolean proxied, String recordId)
	{
		JSONObject json = new JSONObject();
		
		json.put("type", type);
		json.put(DDNSKey.NAME, name);
		json.put("content", content);
		json.put("ttl", ttl);
		json.put("proxied", proxied);
	
		String url = endpoint + "/zones/" + zoneId + "/dns_records/" + recordId;
		HttpHeaders requestHeaders = this.createRequestHeader("application/json");
		String body = json.toString();
		int timeout = 1000;
		ResponseEntityCustom response = httpExchange(HttpMethod.PUT, url, requestHeaders, body, timeout);		
		
		logger.info("Update Record Result {}", response.getBody());
		
		JSONObject resp = new JSONObject(response.getBody());
		return resp.optJSONObject(DDNSKey.RESULT);

	}

	public ResponseEntityCustom deleteDnsRecord(String zoneId, String recordId)
	{
		return this.delete("/zones/" + zoneId + "/dns_records/" + recordId, "application/x-www-form-urlencoded");
	}


}

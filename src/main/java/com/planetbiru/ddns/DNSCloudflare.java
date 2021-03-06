package com.planetbiru.ddns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.planetbiru.constant.ConstantString;
import com.planetbiru.util.ResponseEntityCustom;
import com.planetbiru.util.Utility;

public class DNSCloudflare extends DNS{
	
	private static final Logger logger = LoggerFactory.getLogger(DNSCloudflare.class);

	private String authEmail = "";
	private String accountId = "";
	private String endpoint = "https://api.cloudflare.com/client/v4";
	private String authApiKey = "";
	private String authToken = "";
	
	public void setConfig(String endpoint, String accountId, String authEmail, String authApiKey, String authToken)
	{
		this.endpoint = endpoint;
		this.accountId = accountId;
		this.authEmail = authEmail;
		this.authApiKey = authApiKey;
		this.authToken = authToken;
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
	public ResponseEntityCustom request(HttpMethod method, String endpoint, Map<String, List<String>> params, String contentType)
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
	
	public JSONObject createZoneIfNotExists(DDNSRecord ddnsRecord) 
	{
		if(this.getZone(ddnsRecord.getZone()) == null)
		{
			return this.createZone(ddnsRecord);
		}
		return null;
	}
	
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
		requestHeaders.add(DDNSKey.HEADER_CONTENT_TYPE, ConstantString.APPLICATION_JSON);
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
		
		String url = this.endpoint + DDNSKey.ZONES+zoneId;
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
		String url = this.endpoint + DDNSKey.ZONES+zoneId;
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


	public JSONObject getZoneDnsRecords(String zoneId, Map<String, List<String>> params)
	{
		ResponseEntityCustom response = this.get(DDNSKey.ZONES + zoneId + "/dns_records", params, ConstantString.URL_ENCODE);
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
	
		String url = endpoint + DDNSKey.ZONES + zoneId + "/dns_records";
		HttpHeaders requestHeaders = this.createRequestHeader(ConstantString.APPLICATION_JSON);
		String body = json.toString();
		int timeout = 1000;
		ResponseEntityCustom response = httpExchange(HttpMethod.POST, url, requestHeaders, body, timeout);
		JSONObject resp = new JSONObject(response.getBody());
		return resp.optJSONObject(DDNSKey.RESULT);
	}
	
	@Override
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
			String zoneId = zone.optString("id", "");
			String recordName = ddnsRecord.getRecordName().trim();
			boolean proxied = ddnsRecord.isProxied();
			if(!recordName.isEmpty())
			{
				Map<String, List<String>> params1 = new HashMap<>();
				params1.put(DDNSKey.NAME, Utility.asList(recordName));
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


	

	public ResponseEntityCustom get(String path, Map<String, List<String>> params, String contentType)
	{
		String url = this.endpoint + path;
		return this.request(HttpMethod.GET, url, params, contentType);
	}
	public ResponseEntityCustom post(String path, Map<String, List<String>> params, String contentType)
	{
		String url = this.endpoint + path;
		return this.request(HttpMethod.POST, url, params, contentType);
	}
	public ResponseEntityCustom put(String path, Map<String, List<String>> params, String contentType)
	{
		String url = this.endpoint + path;
		return this.request(HttpMethod.PUT, url, params, contentType);
	}
	public ResponseEntityCustom patch(String path, Map<String, List<String>> params, String contentType)
	{
		String url = this.endpoint + path;
		return this.request(HttpMethod.PATCH, url, params, contentType);
	}
	public ResponseEntityCustom delete(String path, Map<String, List<String>> params, String contentType)
	{
		String url = this.endpoint + path;
		return this.request(HttpMethod.DELETE, url, params, contentType);
	}
	public ResponseEntityCustom delete(String path, String contentType)
	{
		String url = this.endpoint + path;
		Map<String, List<String>> params = new HashMap<>();
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
		requestHeaders.add(DDNSKey.HEADER_USER_AGENT, "OTP Broker");
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
		Map<String, List<String>> params = new HashMap<>();
		params.put(DDNSKey.VALUE, Utility.asList(type));
	
		ResponseEntityCustom response = this.patch(DDNSKey.ZONES + zoneId + "/settings/ssl", params, ConstantString.URL_ENCODE);
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
	
		Map<String, List<String>> params = new HashMap<>();
		params.put(DDNSKey.VALUE, Utility.asList(type));
	
		ResponseEntityCustom response = this.patch(DDNSKey.ZONES + zoneId + "/settings/cache_level", params, ConstantString.URL_ENCODE);
		JSONObject resp = new JSONObject(response.getBody());
		return resp.optJSONObject(DDNSKey.RESULT);
	}

	public ResponseEntityCustom clearZoneCache(String zoneId)
	{
		Map<String, List<String>> params = new HashMap<>();
		params.put("purge_everything", Utility.asList(Boolean.toString(true)));
		return this.delete(DDNSKey.ZONES + zoneId + "/purge_cache", params, ConstantString.URL_ENCODE);
	}

	public ResponseEntityCustom setDnsZoneMinify(String zoneId, String settings)
	{
		Map<String, List<String>> params = new HashMap<>();
		params.put(DDNSKey.VALUE, Utility.asList(settings));
		return this.patch(DDNSKey.ZONES + zoneId + "/settings/minify", params, ConstantString.URL_ENCODE);
	}

	public JSONObject updateDnsRecord(String zoneId, String type, String name, String content, int ttl, boolean proxied, String recordId)
	{
		JSONObject json = new JSONObject();
		
		json.put("type", type);
		json.put(DDNSKey.NAME, name);
		json.put("content", content);
		json.put("ttl", ttl);
		json.put("proxied", proxied);
	
		String url = endpoint + DDNSKey.ZONES + zoneId + "/dns_records/" + recordId;
		HttpHeaders requestHeaders = this.createRequestHeader(ConstantString.APPLICATION_JSON);
		String body = json.toString();
		int timeout = 1000;
		ResponseEntityCustom response = httpExchange(HttpMethod.PUT, url, requestHeaders, body, timeout);		
		
		logger.info("Update Record Result {}", response.getBody());
		
		JSONObject resp = new JSONObject(response.getBody());
		return resp.optJSONObject(DDNSKey.RESULT);

	}

	public ResponseEntityCustom deleteDnsRecord(String zoneId, String recordId)
	{
		return this.delete(DDNSKey.ZONES + zoneId + "/dns_records/" + recordId, ConstantString.URL_ENCODE);
	}
	public String getAuthEmail() {
		return authEmail;
	}
	public void setAuthEmail(String authEmail) {
		this.authEmail = authEmail;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public String getAuthApiKey() {
		return authApiKey;
	}
	public void setAuthApiKey(String authApiKey) {
		this.authApiKey = authApiKey;
	}
	public String getAuthToken() {
		return authToken;
	}
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}
	


}

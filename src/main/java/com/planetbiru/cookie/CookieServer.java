package com.planetbiru.cookie;

import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class CookieServer {
	private static final Logger logger = LoggerFactory.getLogger(CookieServer.class);
	private String sessionName = "SMSSESSID";
	private String sessionID = Utility.md5(System.currentTimeMillis()+"");
	private Map<String, CookieItem> cookieItem = new HashMap<>();
	private long sessionLifetime = 1440000;
	private JSONObject sessionData = new JSONObject();
	
	public CookieServer()
	{
		
	}
	public CookieServer(Map<String, List<String>> headers) {
		this.parseCookie(headers);
		this.updateSessionID();
	}
	public CookieServer(HttpHeaders headers)
	{
		this.parseCookie(headers);
		this.updateSessionID();
	}
	public CookieServer(HttpHeaders headers, String sessionName)
	{
		this.sessionName = sessionName;
		this.parseCookie(headers);
		this.updateSessionID();
	}
	public CookieServer(String rawCookie)
	{
		this.parseCookie(rawCookie);
		this.updateSessionID();
	}
	public CookieServer(String rawCookie, String sessionName)
	{
		this.sessionName = sessionName;
		this.parseCookie(rawCookie);
		this.updateSessionID();
	}
	private void updateSessionID() {
		if(!this.cookieItem.containsKey(this.sessionName))
		{
			CookieItem cookie = new CookieItem(this.sessionName, this.sessionID);
			this.cookieItem.put(this.sessionName, cookie);
		}	
	}
	
	public void destroySession()
	{
		this.sessionData = new JSONObject();
		String sessionFile = this.getSessionFile();
		File file = new File(sessionFile);
		Path path = Paths.get(file.getPath());
		try 
		{
			Files.delete(path);
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage());
		}
	}
	private void parseCookie(String rawCookie) {
		URLCodec urlCodec = new URLCodec();
		Map<String, CookieItem> list = new HashMap<>();
		String[] rawCookieParams = rawCookie.split("\\; ");
		
		for(int j = 0; j<rawCookieParams.length; j++)
		{
			String cookiePair = rawCookieParams[j];
			String[] arr = cookiePair.split("=");
	        String cookieName = arr[0];
	        String cookieValue = "";
	        try 
	        {
	        	cookieValue = urlCodec.decode(arr[1]);
	        }
	        catch (DecoderException e) 
	        {
	        	e.printStackTrace();
	        }
	        CookieItem cookie = new CookieItem(cookieName, cookieValue);
	        list.put(cookieName, cookie);
		}
		this.setCookie(list);
	}
	private void parseCookie(Map<String, List<String>> headers) {
		List<String> rawCookies = headers.get("cookie");
		this.parseCookie(rawCookies);
	}
	private void parseCookie(HttpHeaders headers)
	{
		List<String> rawCookies = headers.get("cookie");
		this.parseCookie(rawCookies);
	}
	private void parseCookie(List<String> rawCookies)
	{
		URLCodec urlCodec = new URLCodec();
		Map<String, CookieItem> list = new HashMap<>();
		if(rawCookies != null)
		{
			for(int i = 0; i<rawCookies.size(); i++)
			{
				String rawCookie = rawCookies.get(i);
				String[] rawCookieParams = rawCookie.split("\\; ");
				
				for(int j = 0; j<rawCookieParams.length; j++)
				{
					String cookiePair = rawCookieParams[j];
					String[] arr = cookiePair.split("=");
			        String cookieName = arr[0];
			        String cookieValue = "";
			        try 
			        {
			        	cookieValue = urlCodec.decode(arr[1]);
			        }
			        catch (DecoderException e) 
			        {
			        	e.printStackTrace();
			        }
			        CookieItem cookie = new CookieItem(cookieName, cookieValue);
			        list.put(cookieName, cookie);
				}
			}
		}
		this.setCookie(list);
	}
	private void setCookie(Map<String, CookieItem> list)
	{
		this.cookieItem = list;
		if(this.cookieItem.containsKey(this.sessionName))
		{
			this.sessionID = this.cookieItem.get(this.sessionName).getValue();
		}
		this.setSessionData(this.readSessionData());		
	}

	public void setCookieItem(Map<String, CookieItem> cookieItem) 
	{
		this.cookieItem = cookieItem;	
	}
	public void setValue(String name, String value)
	{
		for(Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) 
		{
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setValue(value);
			}
		}
	}
	public void setDomain(String name, String domain)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setDomain(domain);
			}
		}
	}
	public void setPath(String name, String path)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setPath(path);
			}
		}
	}
	public void setExpires(String name, Date expires)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setExpires(expires);
			}
		}
	}

	public void setSecure(String name, boolean secure)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setSecure(secure);
			}
		}
	}

	public void setHttpOnly(String name, boolean httpOnly)
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(name))
			{
				((CookieItem) entry.getValue()).setHttpOnly(httpOnly);
			}
		}
	}
	public void putToHeaders(HttpHeaders responseHeaders) 
	{
		for (Map.Entry<String, CookieItem> entry : this.cookieItem.entrySet()) {
			String key = entry.getKey();
			if(key.equals(this.sessionName))
			{
				((CookieItem) entry.getValue()).setExpires(new Date(System.currentTimeMillis() + this.sessionLifetime));
			}
			responseHeaders.add("Set-Cookie", ((CookieItem) entry.getValue()).toString());
		}
	}
	public void clearFile(File directory)
    {
        File[] list = directory.listFiles();
        if(list != null)
        {
	        for(File file : list)
	        {
	            if(file.isDirectory())
	            {
	            	clearFile(file);
	            }
	            else 
	            {
	            	long lasModifued = file.lastModified();
	            	if(lasModifued < (System.currentTimeMillis() - this.sessionLifetime))
	            	{
	            		Path path = Paths.get(file.getPath());
	            		try 
	            		{
							Files.delete(path);
						} 
	            		catch (IOException e) 
	            		{
							logger.error(e.getMessage());
						}
	            	}
	            }
	        }
        }
    }
	
	public void saveSessionData() {
		String sessionFile = this.getSessionFile();
		try 
		{
			File file = new File(sessionFile);
			String directory1 = file.getParent();
			File file2 = new File(directory1);
			String directory2 = file2.getParent();
			
			File d1 = new File(directory1);
			File d2 = new File(directory2);		

			if(!d2.exists())
			{
				d2.mkdir();
			}
			if(!d1.exists())
			{
				d1.mkdir();
			}

			FileUtil.write(sessionFile, this.getSessionData().toString().getBytes());
		} 
		catch (IOException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	private JSONObject readSessionData() {
		File dir = new File(this.getSessionDir());
		this.clearFile(dir);
		JSONObject jsonData = new JSONObject();
		String sessionFile = this.getSessionFile();
		try 
		{
			byte[] data = FileUtil.read(sessionFile);
			if(data != null)
			{
				String text = new String(data);
				jsonData = new JSONObject(text);
			}
		} 
		catch (JSONException | FileNotFoundException e) 
		{
			/**
			 * Do nothing
			 */
		}
		return jsonData;
	}
	
	private String getSessionFile() {
		String dir = FileUtil.class.getResource("/").getFile();
		return dir+"/static/session/"+this.sessionID;
	}
	
	private String getSessionDir() {
		String dir = FileUtil.class.getResource("/").getFile();
		return dir+"/static/session";
	}
	
	public void setSessionValue(String sessionKey, Object sessionValue) {
		this.getSessionData().put(sessionKey, sessionValue);		
	}
	
	public Object getSessionValue(String sessionKey, Object defaultValue)
	{
		Object value = null;
		try 
		{
			value = this.getSessionData().get(sessionKey);
			if(value == null)
			{
				value = defaultValue;
			}
		}
		catch(JSONException e)
		{
			value = defaultValue;
		}
		return value;
	}
	
	public JSONObject getSessionData() {
		return sessionData;
	}
	
	public void setSessionData(JSONObject sessionData) {
		this.sessionData = sessionData;
	}
}

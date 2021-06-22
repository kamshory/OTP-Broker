package com.planetbiru.user;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;

import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class APIUserAccount {

	private static Map<String, User> users = new HashMap<>();
	
	private APIUserAccount()
	{
		
	}
	public static void load(String path)
	{
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		APIUserAccount.prepareDir(fileName);
		try 
		{
			byte[] data = FileUtil.readResource(fileName);
			if(data != null)
			{
				String text = new String(data);
				JSONObject jsonObject = new JSONObject(text);
				Iterator<String> keys = jsonObject.keys();
				while(keys.hasNext()) {
				    String username = keys.next();
				    JSONObject user = jsonObject.optJSONObject(username);
				    APIUserAccount.addUser(username, user);
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	private static void prepareDir(String fileName) 
	{
		File file = new File(fileName);
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
	}
		
	public static void update(String text) {
		if(text != null)
		{
			APIUserAccount.users = new HashMap<>();
			JSONObject jsonObject = new JSONObject(text);
			Iterator<String> keys = jsonObject.keys();
			while(keys.hasNext()) {
			    String username = keys.next();
			    JSONObject user = jsonObject.optJSONObject(username);
			    APIUserAccount.addUser(username, user);
			}
		}	
	}
	public static void addUser(User user)
	{
		APIUserAccount.users.put(user.getUsername(), user);
	}
	
	public static void addUser(String username, JSONObject jsonObject) 
	{
		User user = new User(jsonObject);
		APIUserAccount.users.put(username, user);
	}
	
	public static void addUser(JSONObject jsonObject) 
	{
		User user = new User(jsonObject);
		APIUserAccount.users.put(jsonObject.optString(JsonKey.USERNAME, ""), user);
	}
	
	public static boolean checkUserAuth(Map<String, List<String>> headers) 
	{
		CookieServer cookie = new CookieServer(headers);
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		return APIUserAccount.checkUserAuth(username, password);
	}
	
	public boolean checkUserAuth(HttpHeaders headers)
	{
		CookieServer cookie = new CookieServer(headers);
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		return APIUserAccount.checkUserAuth(username, password);
	}
	
	public static boolean checkUserAuth(String username, String password) 
	{
		if(username.isEmpty())
		{
			return false;
		}
		User user = APIUserAccount.getUser(username);
		return user.getPassword().equals(password) && user.isActive() && !user.isBlocked();
	}
	
	public static User getUser(String username)
	{		
		return APIUserAccount.users.getOrDefault(username, new User());
	}
}

package com.planetbiru.user;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class WebUserAccount {
	private static final String USER_FILE = "/static/data/user/urses.json";
	private static final Logger logger = LoggerFactory.getLogger(WebUserAccount.class);
	private String path = USER_FILE;
	
	private Map<String, User> users = new HashMap<>();
	
	public WebUserAccount(String userSettingPath) {
		this.path = userSettingPath;
		this.init();
	}
	public void init(String userSettingPath)
	{
		this.path = userSettingPath;
		this.load();
	}
	public void init()
	{
		this.load();
	}
	public WebUserAccount() {
	}
	public boolean isEmpty()
	{
		if(this.users.isEmpty())
		{
			this.load();
		}
		return this.users.isEmpty();
	}
	public void addUser(User user)
	{
		this.users.put(user.getUsername(), user);
	}
	public void addUser(String username, JSONObject jsonObject) 
	{
		User user = new User(jsonObject);
		this.users.put(username, user);
	}
	
	public void addUser(JSONObject jsonObject) {
		User user = new User(jsonObject);
		this.users.put(jsonObject.optString(JsonKey.USERNAME, ""), user);
	}	
	
	public User getUser(String username) throws NoUserRegisteredException
	{
		if(this.users.isEmpty())
		{
			throw new NoUserRegisteredException("No user registered");
		}
	
		return this.users.getOrDefault(username, new User());
	}
	
	public void activate(String username) throws NoUserRegisteredException 
	{
		User user = this.getUser(username);
		user.setActive(true);
		this.updateUser(user);
	}
	
	public void deactivate(String username) throws NoUserRegisteredException 
	{
		User user = this.getUser(username);
		user.setActive(false);
		this.updateUser(user);
	}
	
	public void block(String username) throws NoUserRegisteredException 
	{
		User user = this.getUser(username);
		user.setBlocked(true);
		this.updateUser(user);
	}
	
	public void unblock(String username) throws NoUserRegisteredException 
	{
		User user = this.getUser(username);
		user.setBlocked(false);
		this.updateUser(user);
	}
	
	public void updateLastActive(String username) throws NoUserRegisteredException 
	{
		User user = this.getUser(username);
		user.setLastActive(System.currentTimeMillis());
		this.updateUser(user);
	}
	
	public void updateUser(User user)
	{
		this.users.put(user.getUsername(), user);
	}
	
	public void deleteUser(User user)
	{
		this.users.remove(user.getUsername());
	}
	
	public void deleteUser(String username) 
	{
		this.users.remove(username);
	}
	
	public boolean checkUserAuth(Map<String, List<String>> headers) throws NoUserRegisteredException 
	{
		CookieServer cookie = new CookieServer(headers);
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		return this.checkUserAuth(username, password);
	}
	
	public boolean checkUserAuth(HttpHeaders headers) throws NoUserRegisteredException
	{
		CookieServer cookie = new CookieServer(headers);
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		return this.checkUserAuth(username, password);
	}
	
	public boolean checkUserAuth(String username, String password) throws NoUserRegisteredException 
	{
		if(username.isEmpty())
		{
			return false;
		}
		User user = this.getUser(username);
		return user.getPassword().equals(password) && user.isActive() && !user.isBlocked();
	}
	
	private void prepareDir(String fileName) 
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
	
	
	public void load()
	{
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = dir + path;
		this.prepareDir(fileName);
		try 
		{
			byte[] data = FileUtil.read(fileName);
			if(data != null)
			{
				String text = new String(data);
				JSONObject jsonObject = new JSONObject(text);
				Iterator<String> keys = jsonObject.keys();
				while(keys.hasNext()) {
				    String username = keys.next();
				    JSONObject user = jsonObject.optJSONObject(username);
				    this.addUser(username, user);
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
	
	public void save()
	{
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = dir + path;
		String userData = this.toString();
		try 
		{
			if(userData.length() > 20)
			{
				FileUtil.write(fileName, userData.getBytes());
			}
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage());
		}
	}
	
	public String toString()
	{
		return this.toJSONObject().toString();
	}
	
	public JSONObject toJSONObject()
	{
		JSONObject json = new JSONObject();
		for (Map.Entry<String, User> entry : this.users.entrySet())
		{
			String username = entry.getKey();
			JSONObject user = ((User) entry.getValue()).toJSONObject();
			json.put(username, user);
		}
		return json;
	}
	
	public String listAsString() 
	{
		return this.toString();
	}
	
	public User getUserByPhone(String userID) {
		for (Map.Entry<String, User> entry : this.users.entrySet())
		{
			if(!userID.isEmpty() && entry.getValue().getPhone().equals(userID))
			{
				return entry.getValue();
			}
		}
		return new User();
	}
	public User getUserByEmail(String userID) {
		for (Map.Entry<String, User> entry : this.users.entrySet())
		{
			if(!userID.isEmpty() && entry.getValue().getEmail().equals(userID))
			{
				return entry.getValue();
			}
		}
		return new User();
	}

}

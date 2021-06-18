package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigAPI {
	
	private ConfigAPI()
	{
		
	}

	private static int port = 587;
	private static String path = "";
	private static boolean enable = true;	

	private static String mailSenderPassword;
	private static boolean mailStartTLS = true;
	private static boolean mailSSL = false;
	private static String mailHost = "";
	
	public static String getpath() {
		return path;
	}
	public static void setpath(String path) {
		ConfigAPI.path = path;
	}
	public static String getMailSenderPassword() {
		return mailSenderPassword;
	}
	public static void setMailSenderPassword(String mailSenderPassword) {
		ConfigAPI.mailSenderPassword = mailSenderPassword;
	}
	public static boolean isEenable() {
		return enable;
	}
	public static void setEnable(boolean enable) {
		ConfigAPI.enable = enable;
	}
	public static boolean isMailStartTLS() {
		return mailStartTLS;
	}
	public static void setMailStartTLS(boolean mailStartTLS) {
		ConfigAPI.mailStartTLS = mailStartTLS;
	}
	public static boolean isMailSSL() {
		return mailSSL;
	}
	public static void setMailSSL(boolean mailSSL) {
		ConfigAPI.mailSSL = mailSSL;
	}
	public static String getMailHost() {
		return mailHost;
	}
	public static void setMailHost(String mailHost) {
		ConfigAPI.mailHost = mailHost;
	}
	public static int getport() {
		return port;
	}
	public static void setport(int port) {
		ConfigAPI.port = port;
	}
	
	
	public static void load(String path) {
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = dir + path;
		
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					JSONObject json = new JSONObject(text);
					String lpath = json.optString("path", "");
					String lMailSenderPassword = json.optString("mailSenderPassword", "");
					boolean lenable = json.optBoolean("enable", false);
					boolean lMailStartTLS  = json.optBoolean("mailStartTLS", false);
					boolean lMailSSL = json.optBoolean("mailSSL", false);
					String lMailHost = json.optString("mailHost", "");
					int lport = json.optInt("port", 0);
					
					ConfigAPI.port = lport;
					ConfigAPI.path = lpath;
					ConfigAPI.enable = lenable;

					ConfigAPI.mailSenderPassword = lMailSenderPassword;
					ConfigAPI.mailStartTLS = lMailStartTLS;
					ConfigAPI.mailSSL = lMailSSL;
					ConfigAPI.mailHost = lMailHost;
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			e.printStackTrace();
		}
		
	}	

	public static void save(String path) {
		JSONObject config = getJSONObject();
		save(path, config);
	}

	public static void save(String path, JSONObject config) {		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = dir + path;
		prepareDir(fileName);
		
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static void prepareDir(String fileName) {
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
	
	public static JSONObject getJSONObject() {
		JSONObject config = new JSONObject();

		config.put("port", ConfigAPI.port);
		config.put("path", ConfigAPI.path);
		config.put("enable", ConfigAPI.enable);

		config.put("mailHost", ConfigAPI.mailHost);
		config.put("mailSenderPassword", ConfigAPI.mailSenderPassword);
		config.put("mailSSL", ConfigAPI.mailSSL);
		config.put("mailStartTLS", ConfigAPI.mailStartTLS);
		return config;
	}
	
	
}

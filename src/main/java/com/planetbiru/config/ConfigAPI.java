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

	private static int httpPort = 587;
	private static int httpsPort = 587;
	private static boolean httpEnable = true;	
	private static boolean httpsEnable = true;	
	private static String messagePath = "/";
	private static String blockinPath = "/";
	private static String unblockinPath = "/";
	
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

					int lHttpPort = json.optInt("httpPort", 0);
					int lHttpsPort = json.optInt("httpsPort", 0);
					boolean lHttpEnable = json.optBoolean("httpEnable", false);	
					boolean lHttpsEnable = json.optBoolean("httpsEnable", false);	
					String lMessagePath = json.optString("messagePath", "");
					String lBlockinPath = json.optString("blockinPath", "");
					String lUnblockinPath = json.optString("unblockinPath", "");

					
					ConfigAPI.httpPort = lHttpPort;
					ConfigAPI.httpsPort = lHttpsPort;
					ConfigAPI.httpEnable = lHttpEnable;
					ConfigAPI.httpsEnable = lHttpsEnable;
					
					ConfigAPI.messagePath = lMessagePath;
					ConfigAPI.blockinPath = lBlockinPath;
					ConfigAPI.unblockinPath = lUnblockinPath;
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
			System.out.println(fileName.toString());
			System.out.println(config.toString());
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

		config.put("httpPort", ConfigAPI.httpPort);
		config.put("httpsPort", ConfigAPI.httpsPort);
		config.put("httpEnable", ConfigAPI.httpEnable);
		config.put("httpsEnable", ConfigAPI.httpsEnable);

		config.put("messagePath", ConfigAPI.messagePath);
		config.put("blockinPath", ConfigAPI.blockinPath);
		config.put("unblockinPath", ConfigAPI.unblockinPath);
		return config;
	}

	public static int getHttpPort() {
		return httpPort;
	}

	public static void setHttpPort(int httpPort) {
		ConfigAPI.httpPort = httpPort;
	}

	public static int getHttpsPort() {
		return httpsPort;
	}

	public static void setHttpsPort(int httpsPort) {
		ConfigAPI.httpsPort = httpsPort;
	}

	public static boolean isHttpEnable() {
		return httpEnable;
	}

	public static void setHttpEnable(boolean httpEnable) {
		ConfigAPI.httpEnable = httpEnable;
	}

	public static boolean isHttpsEnable() {
		return httpsEnable;
	}

	public static void setHttpsEnable(boolean httpsEnable) {
		ConfigAPI.httpsEnable = httpsEnable;
	}

	public static String getMessagePath() {
		return messagePath;
	}

	public static void setMessagePath(String messagePath) {
		ConfigAPI.messagePath = messagePath;
	}

	public static String getBlockinPath() {
		return blockinPath;
	}

	public static void setBlockinPath(String blockinPath) {
		ConfigAPI.blockinPath = blockinPath;
	}

	public static String getUnblockinPath() {
		return unblockinPath;
	}

	public static void setUnblockinPath(String unblockinPath) {
		ConfigAPI.unblockinPath = unblockinPath;
	}

	public static JSONObject toJSONObject() {
		return getJSONObject();
	}
	
	
}

package com.planetbiru.config;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigGeneral {
	private static String configPath = "";
	private static Date nextValid = new Date();
	private static Logger logger = LogManager.getLogger(ConfigGeneral.class);
	private ConfigGeneral()
	{
		
	}

	private static String deviceName = "";
	private static String deviceTimeZone = "";
	private static String ntpServer = "";
	private static String ntpUpdateInterval = "";

	public static void load(String path) {
		ConfigGeneral.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					JSONObject json = new JSONObject(text);

					ConfigGeneral.setDeviceName(json.optString("deviceName", "").trim());
					ConfigGeneral.setDeviceTimeZone(json.optString("deviceTimeZone", "").trim());
					ConfigGeneral.setNtpServer(json.optString("ntpServer", "").trim());
					ConfigGeneral.setNtpUpdateInterval(json.optString("ntpUpdateInterval", "").trim());
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			logger.error(e.getMessage());
		}
		
	}	

	public static void save() {
		ConfigGeneral.save(ConfigGeneral.configPath);
	}
	public static void save(String path) {
		JSONObject config = getJSONObject();
		ConfigGeneral.save(path, config);
	}

	public static void save(String path, JSONObject config) {		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		ConfigGeneral.prepareDir(fileName);
		
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
		config.put("deviceName", ConfigGeneral.getDeviceName());
		config.put("deviceTimeZone", ConfigGeneral.getDeviceTimeZone());
		config.put("ntpServer", ConfigGeneral.getNtpServer());
		config.put("ntpUpdateInterval", ConfigGeneral.getNtpUpdateInterval());
		return config;
	}

	public static JSONObject toJSONObject() {
		return getJSONObject();
	}

	public static String getDeviceName() {
		return deviceName;
	}

	public static void setDeviceName(String deviceName) {
		ConfigGeneral.deviceName = deviceName;
	}

	public static String getDeviceTimeZone() {
		return deviceTimeZone;
	}

	public static void setDeviceTimeZone(String deviceTimeZone) {
		ConfigGeneral.deviceTimeZone = deviceTimeZone;
	}

	public static String getNtpServer() {
		return ntpServer;
	}

	public static void setNtpServer(String ntpServer) {
		ConfigGeneral.ntpServer = ntpServer;
	}

	public static String getNtpUpdateInterval() {
		return ntpUpdateInterval;
	}

	public static void setNtpUpdateInterval(String ntpUpdateInterval) {
		ConfigGeneral.ntpUpdateInterval = ntpUpdateInterval;
	}

	public static Date getNextValid() {
		return nextValid;
	}

	public static void setNextValid(Date nextValid) {
		ConfigGeneral.nextValid = nextValid;
	}
	
	
}

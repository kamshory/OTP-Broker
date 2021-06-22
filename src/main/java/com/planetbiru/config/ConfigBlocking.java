package com.planetbiru.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.gsm.GSMException;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigBlocking {
	
	private static Logger logger = LogManager.getLogger(ConfigBlocking.class);
	
	private static String countryCode = "62";
	private static Map<String, Boolean> blockList = new HashMap<>();
	
	private ConfigBlocking()
	{
		
	}
	public static String canonical(String msisdn) throws GSMException
	{
		if(msisdn.isEmpty())
		{
			throw new GSMException("MSISDN can not be null or empty");
		}
		msisdn = msisdn.trim();
		if(msisdn.startsWith("+"))
		{
			msisdn = msisdn.substring(1);
		}
		if(msisdn.startsWith("0"))
		{
			msisdn = ConfigBlocking.getCountryCode() + msisdn.substring(1);
		}
		return msisdn;
	}
	public static void block(String msisdn) throws GSMException {
		msisdn = ConfigBlocking.canonical(msisdn);
		ConfigBlocking.blockList.put(msisdn, Boolean.valueOf(true));
	}
	public static void unblock(String msisdn) throws GSMException {
		msisdn = ConfigBlocking.canonical(msisdn);
		ConfigBlocking.blockList.put(msisdn, Boolean.valueOf(false));
	}
	public static void block(String msisdn, boolean block) throws GSMException {
		msisdn = ConfigBlocking.canonical(msisdn);
		ConfigBlocking.blockList.put(msisdn, Boolean.valueOf(block));
	}
	public static void add(String msisdn) throws GSMException {
		if(!msisdn.isEmpty())
		{
			msisdn = ConfigBlocking.canonical(msisdn);
			ConfigBlocking.blockList.put(msisdn, Boolean.valueOf(true));
		}
	}
	public static Boolean isBlocked(String msisdn) throws GSMException
	{
		msisdn = ConfigBlocking.canonical(msisdn);
		return ConfigBlocking.blockList.getOrDefault(msisdn, Boolean.valueOf(false));
	}
	
	public static void load(String path) {
		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		fileName = FileConfigUtil.fixFileName(fileName);
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					ConfigBlocking.blockList = new HashMap<>();
					
					JSONObject json = new JSONObject(text);

					JSONArray keys = json.names();
					for(int i = 0; i<keys.length(); i++)
					{
						String id = keys.optString(i);
						boolean block  = json.optBoolean(id, false);
						ConfigBlocking.blockList.put(id, Boolean.valueOf(block));
					}
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			logger.error(e.getMessage());
			//e.printStackTrace();
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
		String fileName = FileConfigUtil.fixFileName(dir + path);
		fileName = FileConfigUtil.fixFileName(fileName);
		prepareDir(fileName);
		
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage());
			//e.printStackTrace();
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
		File file3 = new File(fileName);
		file3.getParentFile().mkdirs();
	}
	
	public static JSONObject getJSONObject() {
		JSONObject config = new JSONObject();
		for (Map.Entry<String, Boolean> set : ConfigBlocking.blockList.entrySet()) 
		{
			 config.put(set.getKey(), set.getValue());
        }
		return config;
	}

	public static JSONObject toJSONObject() {
		return getJSONObject();
	}

	public static void remove(String value) {	
		if(ConfigBlocking.blockList.containsKey(value))
		{
			ConfigBlocking.blockList.remove(value);
		}		
	}
	public static String getCountryCode() {
		return countryCode;
	}
	public static void setCountryCode(String countryCode) {
		ConfigBlocking.countryCode = countryCode;
	}
	


	
	

	

	
	
	
	
	
}

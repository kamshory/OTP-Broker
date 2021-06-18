package com.planetbiru.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class ConfigModem {
    private static Map<String, ModemData> modemData = new HashMap<>();
	private static String configPath;
	
	private ConfigModem()
	{
		
	}
	public static Map<String, ModemData> getModemData()
	{
		return ConfigModem.modemData;
	}
	public static void load(String path)
	{
		ConfigModem.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = dir + path;
		System.out.println(fileName);
		ConfigModem.prepareDir(fileName);
		try 
		{
			byte[] data = FileUtil.read(fileName);
			if(data != null)
			{
				String text = new String(data);
				JSONObject jsonObject = new JSONObject(text);
				Iterator<String> keys = jsonObject.keys();
				while(keys.hasNext()) {
				    String id = keys.next();
				    JSONObject modem = jsonObject.optJSONObject(id);
				    System.out.println(modem);
				    ConfigModem.addModemData(id, modem);
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
			ConfigModem.modemData = new HashMap<>();
			JSONObject jsonObject = new JSONObject(text);
			Iterator<String> keys = jsonObject.keys();
			while(keys.hasNext()) {
			    String id = keys.next();
			    JSONObject modem = jsonObject.optJSONObject(id);
			    ConfigModem.addModemData(id, modem);
			}
		}	
	}
	public static void addUser(ModemData modem)
	{
		ConfigModem.modemData.put(modem.id, modem);
	}
	
	public static void addModemData(String username, JSONObject jsonObject) 
	{
		ModemData user = new ModemData(jsonObject);
		ConfigModem.modemData.put(username, user);
	}
	
	public static void addModemData(JSONObject jsonObject) 
	{
		ModemData modem = new ModemData(jsonObject);
		ConfigModem.modemData.put(jsonObject.optString(JsonKey.USERNAME, ""), modem);
	}
	
	
	public static ModemData geModemData(String id)
	{		
		return ConfigModem.modemData.getOrDefault(id, new ModemData());
	}
	
	public static void save(String path) {
		JSONObject config = toJSONObject();
		save(path, config);
	}

	
	
	public static void save(String path, JSONObject config) {	
		ConfigModem.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = dir + path;
		System.out.println(fileName);
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
	
	public static void save() {
		save(ConfigModem.configPath, toJSONObject());
		
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject json = new JSONObject();
		for (Map.Entry<String, ModemData> entry : modemData.entrySet())
		{
			String id = entry.getKey();
			JSONObject modem = ((ModemData) entry.getValue()).toJSONObject();
			json.put(id, modem);
		}
		return json;
	}
	
	public static String getConfigPath() {
		return configPath;
	}

	public static void setConfigPath(String configPath) {
		ConfigModem.configPath = configPath;
	}
	public static void deleteRecord(String id) {
		ConfigModem.modemData.remove(id);
		
	}
	public static void deactivate(String id) {
		ModemData modem = ConfigModem.modemData.getOrDefault(id, new ModemData());
		modem.active = false;
		ConfigModem.modemData.put(id, modem);
		
	}
	public static void activate(String id) {
		ModemData modem = ConfigModem.modemData.getOrDefault(id, new ModemData());
		modem.active = true;
		ConfigModem.modemData.put(id, modem);		
	}
	public static void update(String id, ModemData modem) {
		ConfigModem.modemData.put(id, modem);		
	}
	
	
	
}
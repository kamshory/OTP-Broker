package com.planetbiru.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jcraft.jsch.JSchException;
import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.Utility;

public class ConfigFirewall {
	private static Logger logger = LogManager.getLogger(ConfigFirewall.class);
	private static JSONArray records = new JSONArray();
	private static String configPath = "";
	private ConfigFirewall()
	{
		
	}
	
	public static void load(String path) {
		ConfigFirewall.configPath = path;
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		prepareDir(fileName);	
		
		
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);		
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					JSONArray list = new JSONArray(text);				
					ConfigFirewall.setRecords(list);
				}
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			logger.error(e.getMessage());
		}	
	}	
	public static void add(int port, String protocol) {
		String id = String.format("%s%d", protocol, port);
		JSONObject record = new JSONObject();
		record.put("id", id);
		record.put("port", port);
		record.put("protocol", protocol);
		record.put("active", true);
		record.put("lastUpdate", System.currentTimeMillis());
		List<Integer> servicePorts = ConfigFirewall.getServicePorts();
		if(!servicePorts.contains(record.optInt("port")))
		{
			try 
			{
				ConfigFirewall.records.put(record);		
				ConfigFirewall.activate(record);
			} 
			catch (JSchException e) 
			{
				/**
				 * Do nothing
				 */
			}
		}
	}
	
	public static void save()
	{
		ConfigFirewall.save(ConfigFirewall.configPath);
	}
	
	public static void save(String path) {
		save(path, ConfigFirewall.getRecords());
	}

	public static JSONObject get(String id)
	{
		for(int i = 0; i<ConfigFirewall.getRecords().length(); i++)
		{
			if(ConfigFirewall.getRecords().get(i) != null && ConfigFirewall.getRecords().optJSONObject(i).optString("id").equals(id))
			{
				return ConfigFirewall.getRecords().optJSONObject(i);
			}
		}
		return new JSONObject();
	}
	
	public static void remove(String id) {
		JSONArray list = new JSONArray();
		for(int i = 0; i<ConfigFirewall.getRecords().length(); i++)
		{
			if(!ConfigFirewall.getRecords().optJSONObject(i).optString("id").equals(id))
			{
				list.put(ConfigFirewall.getRecords().optJSONObject(i));
			}
		}	
		ConfigFirewall.records = list;
	}
	
	public static void activate(String id)
	{
		JSONObject record = ConfigFirewall.get(id);
		List<Integer> servicePorts = ConfigFirewall.getServicePorts();
		if(!servicePorts.contains(record.optInt("port")))
		{
			try 
			{
				ConfigFirewall.activate(record);
			} 
			catch (JSchException e) 
			{
				/**
				 * Do nothing
				 */
			}
			ConfigFirewall.get(id).put("active", true);
			ConfigFirewall.get(id).put("lastUpdate", System.currentTimeMillis());
		}
	}
	
	public static void deactivate(String id)
	{
		JSONObject record = ConfigFirewall.get(id);
		List<Integer> servicePorts = ConfigFirewall.getServicePorts();
		if(!servicePorts.contains(record.optInt("port")))
		{
			try 
			{
				ConfigFirewall.deactivate(record);
			} 
			catch (JSchException e) 
			{
				/**
				 * Do nothing
				 */
			}
			ConfigFirewall.get(id).put("active", false);
			ConfigFirewall.get(id).put("lastUpdate", System.currentTimeMillis());

		}
	}
	
	private static void activate(JSONObject record) throws JSchException 
	{
		String command1 = String.format("firewall-cmd --permanent --add-port=%d/%s", record.optInt("port", 0), record.optString("protocol", ""));
		String command2 = "firewall-cmd --reload"; 
		CommandLineExecutor.execSSH(command1, 10);
		CommandLineExecutor.execSSH(command2, 10);
	}

	private static void deactivate(JSONObject record) throws JSchException 
	{
		String command1 = String.format("firewall-cmd --permanent --remove-port=%d/%s", record.optInt("port", 0), record.optString("protocol", ""));
		String command2 = "firewall-cmd --reload"; 
		CommandLineExecutor.execSSH(command1, 10);
		CommandLineExecutor.execSSH(command2, 10);
	}

	public static List<Integer> getServicePorts()
	{
		List<Integer> servicePorts = new ArrayList<>();
		servicePorts.add(Config.getPortManager());
		servicePorts.add(ConfigAPI.getHttpPort());
		servicePorts.add(ConfigAPI.getHttpsPort());
		return servicePorts;
	}

	public static void save(String path, JSONArray config) {
		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		prepareDir(fileName);
		
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			logger.error(e.getMessage());
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

	public static JSONArray getRecords() {
		return records;
	}

	public static void setRecords(JSONArray records) {
		ConfigFirewall.records = records;
	}

}

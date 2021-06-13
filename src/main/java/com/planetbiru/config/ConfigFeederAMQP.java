package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.user.UserAccount;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;

public class ConfigFeederAMQP {


	private static boolean feederAmqpEnable = false;
	private static boolean feederAmqpSSL = false;
	private static String feederAmqpAddress = "";
	private static int feederAmqpPort = 0;
	private static String feederAmqpPath = "";
	private static String feederAmqpUsername = "";
	private static String feederAmqpPassword = "";
	private static String feederAmqpChannel = "";
	private static int feederAmqpTimeout = 0;
	private static int feederAmqpRefresh = 0;
	
	private ConfigFeederAMQP()
	{
		
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("feederAmqpEnable", ConfigFeederAMQP.feederAmqpEnable);
		setting.put("feederAmqpSSL", ConfigFeederAMQP.feederAmqpSSL);
		setting.put("feederAmqpAddress", ConfigFeederAMQP.feederAmqpAddress);
		setting.put("feederAmqpPort", ConfigFeederAMQP.feederAmqpPort);
		setting.put("feederAmqpPath", ConfigFeederAMQP.feederAmqpPath);
		setting.put("feederAmqpUsername", ConfigFeederAMQP.feederAmqpUsername);
		setting.put("feederAmqpPassword", ConfigFeederAMQP.feederAmqpPassword);
		setting.put("feederAmqpChannel", ConfigFeederAMQP.feederAmqpChannel);
		setting.put("feederAmqpTimeout", ConfigFeederAMQP.feederAmqpTimeout);
		setting.put("feederAmqpRefresh", ConfigFeederAMQP.feederAmqpRefresh);
		return setting;
	}
	
	public static void save(String path) {
		String fileName = ConfigFeederAMQP.getBaseDir() + path;
		ConfigFeederAMQP.prepareDir(fileName);	
		try 
		{
			FileUtil.write(fileName, ConfigFeederAMQP.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	public static void load(String path)
	{
		String fileName = ConfigFeederAMQP.getBaseDir() + path;
		ConfigFeederAMQP.prepareDir(fileName);
		byte[] data = null;
		try 
		{
			data = FileConfigUtil.read(fileName);
		} 
		catch (FileNotFoundException e1) 
		{
			/**
			 * Do nothing
			 */
		}
		if(data != null)
		{
			String text = new String(data);
			try
			{
				JSONObject setting = new JSONObject(text);

				ConfigFeederAMQP.feederAmqpEnable = setting.optBoolean("feederAmqpEnable", false);
				ConfigFeederAMQP.feederAmqpSSL = setting.optBoolean("feederAmqpSSL", false);
				ConfigFeederAMQP.feederAmqpAddress = setting.optString("feederAmqpAddress", "");
				ConfigFeederAMQP.feederAmqpPort = setting.optInt("feederAmqpPort", 0);
				ConfigFeederAMQP.feederAmqpPath = setting.optString("feederAmqpPath", "");
				ConfigFeederAMQP.feederAmqpUsername = setting.optString("feederAmqpUsername", "");
				ConfigFeederAMQP.feederAmqpPassword = setting.optString("feederAmqpPassword", "");
				ConfigFeederAMQP.feederAmqpChannel = setting.optString("feederAmqpChannel", "");
				ConfigFeederAMQP.feederAmqpTimeout = setting.optInt("feederAmqpTimeout", 0);
				ConfigFeederAMQP.feederAmqpRefresh = setting.optInt("feederAmqpRefresh", 0);

			}
			catch(JSONException e)
			{
				/**
				 * Do nothing
				 */
			}
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
	private static String getBaseDir()
	{
		return UserAccount.class.getResource("/").getFile();
	}

	public static boolean isFeederAmqpEnable() {
		return feederAmqpEnable;
	}

	public static void setFeederAmqpEnable(boolean feederAmqpEnable) {
		ConfigFeederAMQP.feederAmqpEnable = feederAmqpEnable;
	}

	public static boolean isFeederAmqpSSL() {
		return feederAmqpSSL;
	}

	public static void setFeederAmqpSSL(boolean feederAmqpSSL) {
		ConfigFeederAMQP.feederAmqpSSL = feederAmqpSSL;
	}

	public static String getFeederAmqpAddress() {
		return feederAmqpAddress;
	}

	public static void setFeederAmqpAddress(String feederAmqpAddress) {
		ConfigFeederAMQP.feederAmqpAddress = feederAmqpAddress;
	}

	public static int getFeederAmqpPort() {
		return feederAmqpPort;
	}

	public static void setFeederAmqpPort(int feederAmqpPort) {
		ConfigFeederAMQP.feederAmqpPort = feederAmqpPort;
	}

	public static String getFeederAmqpPath() {
		return feederAmqpPath;
	}

	public static void setFeederAmqpPath(String feederAmqpPath) {
		ConfigFeederAMQP.feederAmqpPath = feederAmqpPath;
	}

	public static String getFeederAmqpUsername() {
		return feederAmqpUsername;
	}

	public static void setFeederAmqpUsername(String feederAmqpUsername) {
		ConfigFeederAMQP.feederAmqpUsername = feederAmqpUsername;
	}

	public static String getFeederAmqpPassword() {
		return feederAmqpPassword;
	}

	public static void setFeederAmqpPassword(String feederAmqpPassword) {
		ConfigFeederAMQP.feederAmqpPassword = feederAmqpPassword;
	}

	public static String getFeederAmqpChannel() {
		return feederAmqpChannel;
	}

	public static void setFeederAmqpChannel(String feederAmqpChannel) {
		ConfigFeederAMQP.feederAmqpChannel = feederAmqpChannel;
	}

	public static int getFeederAmqpTimeout() {
		return feederAmqpTimeout;
	}

	public static void setFeederAmqpTimeout(int feederAmqpTimeout) {
		ConfigFeederAMQP.feederAmqpTimeout = feederAmqpTimeout;
	}

	public static int getFeederAmqpRefresh() {
		return feederAmqpRefresh;
	}

	public static void setFeederAmqpRefresh(int feederAmqpRefresh) {
		ConfigFeederAMQP.feederAmqpRefresh = feederAmqpRefresh;
	}

	
	
	
}
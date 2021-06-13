package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.user.UserAccount;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;

public class ConfigFeeder {

	private static boolean feederWsEnable = false;
	private static boolean feederWsSSL = false;
	private static String feederWsAddress = "";
	private static int feederWsPort = 0;
	private static String feederWsPath = "";
	private static String feederWsUsername = "";
	private static String feederWsPassword = "";
	private static String feederWsChannel = "";
	private static int feederWsTimeout = 0;
	private static int feederWsReconnectDelay = 0;
	private static int feederWsRefresh = 0;

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
	
	private ConfigFeeder()
	{
		
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("feederWsEnable", ConfigFeeder.feederWsEnable);
		setting.put("feederWsSSL", ConfigFeeder.feederWsSSL);
		setting.put("feederWsAddress", ConfigFeeder.feederWsAddress);
		setting.put("feederWsPort", ConfigFeeder.feederWsPort);
		setting.put("feederWsPath", ConfigFeeder.feederWsPath);
		setting.put("feederWsUsername", ConfigFeeder.feederWsUsername);
		setting.put("feederWsPassword", ConfigFeeder.feederWsPassword);
		setting.put("feederWsChannel", ConfigFeeder.feederWsChannel);
		setting.put("feederWsTimeout", ConfigFeeder.feederWsTimeout);
		setting.put("feederWsReconnectDelay", ConfigFeeder.feederWsReconnectDelay);
		setting.put("feederWsRefresh", ConfigFeeder.feederWsRefresh);
		setting.put("feederAmqpEnable", ConfigFeeder.feederAmqpEnable);
		setting.put("feederAmqpSSL", ConfigFeeder.feederAmqpSSL);
		setting.put("feederAmqpAddress", ConfigFeeder.feederAmqpAddress);
		setting.put("feederAmqpPort", ConfigFeeder.feederAmqpPort);
		setting.put("feederAmqpPath", ConfigFeeder.feederAmqpPath);
		setting.put("feederAmqpUsername", ConfigFeeder.feederAmqpUsername);
		setting.put("feederAmqpPassword", ConfigFeeder.feederAmqpPassword);
		setting.put("feederAmqpChannel", ConfigFeeder.feederAmqpChannel);
		setting.put("feederAmqpTimeout", ConfigFeeder.feederAmqpTimeout);
		setting.put("feederAmqpRefresh", ConfigFeeder.feederAmqpRefresh);
		return setting;
	}
	
	public static void save(String path) {
		String fileName = ConfigFeeder.getBaseDir() + path;
		ConfigFeeder.prepareDir(fileName);	
		try 
		{
			FileUtil.write(fileName, ConfigFeeder.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	public static void load(String path)
	{
		String fileName = ConfigFeeder.getBaseDir() + path;
		ConfigFeeder.prepareDir(fileName);
		byte[] data = null;
		try 
		{
			data = FileUtil.read(fileName);
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
				ConfigFeeder.feederWsEnable = setting.optBoolean("feederWsEnable", false);
				ConfigFeeder.feederWsSSL = setting.optBoolean("feederWsSSL", false);
				ConfigFeeder.feederWsAddress = setting.optString("feederWsAddress", "");
				ConfigFeeder.feederWsPort = setting.optInt("feederWsPort", 0);
				ConfigFeeder.feederWsPath = setting.optString("feederWsPath", "");
				ConfigFeeder.feederWsUsername = setting.optString("feederWsUsername", "");
				ConfigFeeder.feederWsPassword = setting.optString("feederWsPassword", "");
				ConfigFeeder.feederWsChannel = setting.optString("feederWsChannel", "");
				ConfigFeeder.feederWsTimeout = setting.optInt("feederWsTimeout", 0);
				ConfigFeeder.feederWsReconnectDelay = setting.optInt("feederWsReconnectDelay", 0);
				ConfigFeeder.feederWsRefresh = setting.optInt("feederWsRefresh", 0);

				ConfigFeeder.feederAmqpEnable = setting.optBoolean("feederAmqpEnable", false);
				ConfigFeeder.feederAmqpSSL = setting.optBoolean("feederAmqpSSL", false);
				ConfigFeeder.feederAmqpAddress = setting.optString("feederAmqpAddress", "");
				ConfigFeeder.feederAmqpPort = setting.optInt("feederAmqpPort", 0);
				ConfigFeeder.feederAmqpPath = setting.optString("feederAmqpPath", "");
				ConfigFeeder.feederAmqpUsername = setting.optString("feederAmqpUsername", "");
				ConfigFeeder.feederAmqpPassword = setting.optString("feederAmqpPassword", "");
				ConfigFeeder.feederAmqpChannel = setting.optString("feederAmqpChannel", "");
				ConfigFeeder.feederAmqpTimeout = setting.optInt("feederAmqpTimeout", 0);
				ConfigFeeder.feederAmqpRefresh = setting.optInt("feederAmqpRefresh", 0);

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

	public static boolean isFeederWsEnable() {
		return feederWsEnable;
	}

	public static void setFeederWsEnable(boolean feederWsEnable) {
		ConfigFeeder.feederWsEnable = feederWsEnable;
	}

	public static boolean isFeederWsSSL() {
		return feederWsSSL;
	}

	public static void setFeederWsSSL(boolean feederWsSSL) {
		ConfigFeeder.feederWsSSL = feederWsSSL;
	}

	public static String getFeederWsAddress() {
		return feederWsAddress;
	}

	public static void setFeederWsAddress(String feederWsAddress) {
		ConfigFeeder.feederWsAddress = feederWsAddress;
	}

	public static int getFeederWsPort() {
		return feederWsPort;
	}

	public static void setFeederWsPort(int feederWsPort) {
		ConfigFeeder.feederWsPort = feederWsPort;
	}

	public static String getFeederWsPath() {
		return feederWsPath;
	}

	public static void setFeederWsPath(String feederWsPath) {
		ConfigFeeder.feederWsPath = feederWsPath;
	}

	public static String getFeederWsUsername() {
		return feederWsUsername;
	}

	public static void setFeederWsUsername(String feederWsUsername) {
		ConfigFeeder.feederWsUsername = feederWsUsername;
	}

	public static String getFeederWsPassword() {
		return feederWsPassword;
	}

	public static void setFeederWsPassword(String feederWsPassword) {
		ConfigFeeder.feederWsPassword = feederWsPassword;
	}

	public static String getFeederWsChannel() {
		return feederWsChannel;
	}

	public static void setFeederWsChannel(String feederWsChannel) {
		ConfigFeeder.feederWsChannel = feederWsChannel;
	}

	public static int getFeederWsTimeout() {
		return feederWsTimeout;
	}

	public static void setFeederWsTimeout(int feederWsTimeout) {
		ConfigFeeder.feederWsTimeout = feederWsTimeout;
	}

	public static int getFeederWsReconnectDelay() {
		return feederWsReconnectDelay;
	}

	public static void setFeederWsReconnectDelay(int feederWsReconnectDelay) {
		ConfigFeeder.feederWsReconnectDelay = feederWsReconnectDelay;
	}

	public static int getFeederWsRefresh() {
		return feederWsRefresh;
	}

	public static void setFeederWsRefresh(int feederWsRefresh) {
		ConfigFeeder.feederWsRefresh = feederWsRefresh;
	}

	public static boolean isFeederAmqpEnable() {
		return feederAmqpEnable;
	}

	public static void setFeederAmqpEnable(boolean feederAmqpEnable) {
		ConfigFeeder.feederAmqpEnable = feederAmqpEnable;
	}

	public static boolean isFeederAmqpSSL() {
		return feederAmqpSSL;
	}

	public static void setFeederAmqpSSL(boolean feederAmqpSSL) {
		ConfigFeeder.feederAmqpSSL = feederAmqpSSL;
	}

	public static String getFeederAmqpAddress() {
		return feederAmqpAddress;
	}

	public static void setFeederAmqpAddress(String feederAmqpAddress) {
		ConfigFeeder.feederAmqpAddress = feederAmqpAddress;
	}

	public static int getFeederAmqpPort() {
		return feederAmqpPort;
	}

	public static void setFeederAmqpPort(int feederAmqpPort) {
		ConfigFeeder.feederAmqpPort = feederAmqpPort;
	}

	public static String getFeederAmqpPath() {
		return feederAmqpPath;
	}

	public static void setFeederAmqpPath(String feederAmqpPath) {
		ConfigFeeder.feederAmqpPath = feederAmqpPath;
	}

	public static String getFeederAmqpUsername() {
		return feederAmqpUsername;
	}

	public static void setFeederAmqpUsername(String feederAmqpUsername) {
		ConfigFeeder.feederAmqpUsername = feederAmqpUsername;
	}

	public static String getFeederAmqpPassword() {
		return feederAmqpPassword;
	}

	public static void setFeederAmqpPassword(String feederAmqpPassword) {
		ConfigFeeder.feederAmqpPassword = feederAmqpPassword;
	}

	public static String getFeederAmqpChannel() {
		return feederAmqpChannel;
	}

	public static void setFeederAmqpChannel(String feederAmqpChannel) {
		ConfigFeeder.feederAmqpChannel = feederAmqpChannel;
	}

	public static int getFeederAmqpTimeout() {
		return feederAmqpTimeout;
	}

	public static void setFeederAmqpTimeout(int feederAmqpTimeout) {
		ConfigFeeder.feederAmqpTimeout = feederAmqpTimeout;
	}

	public static int getFeederAmqpRefresh() {
		return feederAmqpRefresh;
	}

	public static void setFeederAmqpRefresh(int feederAmqpRefresh) {
		ConfigFeeder.feederAmqpRefresh = feederAmqpRefresh;
	}

	
	
	
}

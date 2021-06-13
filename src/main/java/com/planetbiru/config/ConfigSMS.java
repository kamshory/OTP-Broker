package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.constant.JsonKey;
import com.planetbiru.user.UserAccount;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;

public class ConfigSMS {
	
	private static String connectionType = "";
	private static String smsCenter = "";
	private static int incommingInterval = 0;
	private static int timeRange = 0;
	private static int maxPerTimeRange = 0;
	private static String imei = "";
	private static String simCardPIN = "";
	
	private ConfigSMS()
	{
		
	}
	
	public static JSONObject toJSONObject()
	{
		JSONObject smsSetting = new JSONObject();
		smsSetting.put(JsonKey.CONNECTION_TYPE, ConfigSMS.connectionType);
		smsSetting.put(JsonKey.SMS_CENTER, ConfigSMS.smsCenter);
		smsSetting.put(JsonKey.INCOMMING_INTERVAL, ConfigSMS.incommingInterval);
		smsSetting.put(JsonKey.TIME_RANGE, ConfigSMS.timeRange);
		smsSetting.put(JsonKey.MAX_PER_TIME_RANGE, ConfigSMS.maxPerTimeRange);
		smsSetting.put(JsonKey.IMEI, ConfigSMS.getImei());
		smsSetting.put(JsonKey.SIM_CARD_PIN, ConfigSMS.getSimCardPIN());
		return smsSetting;
	}
	
	public static void save(String path) {
		String fileName = ConfigSMS.getBaseDir() + path;
		ConfigSMS.prepareDir(fileName);	
		try 
		{
			FileUtil.write(fileName, ConfigSMS.toJSONObject().toString().getBytes());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void load(String path)
	{
		String fileName = ConfigSMS.getBaseDir() + path;
		ConfigSMS.prepareDir(fileName);
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
				JSONObject smsSetting = new JSONObject(text);
				ConfigSMS.connectionType = smsSetting.optString(JsonKey.CONNECTION_TYPE, "");
				ConfigSMS.smsCenter = smsSetting.optString(JsonKey.SMS_CENTER, "");
				ConfigSMS.setImei(smsSetting.optString(JsonKey.IMEI, ""));
				ConfigSMS.setSimCardPIN(smsSetting.optString(JsonKey.SIM_CARD_PIN, ""));
				ConfigSMS.incommingInterval = smsSetting.optInt(JsonKey.INCOMMING_INTERVAL, 0);
				ConfigSMS.timeRange = smsSetting.optInt(JsonKey.TIME_RANGE, 0);
				ConfigSMS.maxPerTimeRange = smsSetting.optInt(JsonKey.MAX_PER_TIME_RANGE, 0);
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

	public static String getConnectionType() {
		return connectionType;
	}

	public static void setConnectionType(String connectionType) {
		ConfigSMS.connectionType = connectionType;
	}

	public static String getSmsCenter() {
		return smsCenter;
	}

	public static void setSmsCenter(String smsCenter) {
		ConfigSMS.smsCenter = smsCenter;
	}

	public static int getIncommingInterval() {
		return incommingInterval;
	}

	public static void setIncommingInterval(int incommingInterval) {
		ConfigSMS.incommingInterval = incommingInterval;
	}

	public static int getTimeRange() {
		return timeRange;
	}

	public static void setTimeRange(int timeRange) {
		ConfigSMS.timeRange = timeRange;
	}

	public static int getMaxPerTimeRange() {
		return maxPerTimeRange;
	}

	public static void setMaxPerTimeRange(int maxPerTimeRange) {
		ConfigSMS.maxPerTimeRange = maxPerTimeRange;
	}

	public static String getImei() {
		return imei;
	}

	public static void setImei(String imei) {
		ConfigSMS.imei = imei;
	}

	public static String getSimCardPIN() {
		return simCardPIN;
	}

	public static void setSimCardPIN(String simCardPIN) {
		ConfigSMS.simCardPIN = simCardPIN;
	}

	
	
	
}

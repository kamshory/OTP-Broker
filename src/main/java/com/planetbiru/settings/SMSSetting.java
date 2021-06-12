package com.planetbiru.settings;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.constant.JsonKey;
import com.planetbiru.user.UserAccount;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;

public class SMSSetting {
	
	private String connectionType = "";
	private String smsCenter = "";
	private int incommingInterval = 0;
	private int timeRange = 0;
	private int maxPerTimeRange = 0;
	private String imei = "";
	private String simCardPIN = "";
	
	public JSONObject toJSONObject()
	{
		JSONObject smsSetting = new JSONObject();
		smsSetting.put(JsonKey.CONNECTION_TYPE, this.connectionType);
		smsSetting.put(JsonKey.SMS_CENTER, this.smsCenter);
		smsSetting.put(JsonKey.INCOMMING_INTERVAL, this.incommingInterval);
		smsSetting.put(JsonKey.TIME_RANGE, this.timeRange);
		smsSetting.put(JsonKey.MAX_PER_TIME_RANGE, this.maxPerTimeRange);
		smsSetting.put(JsonKey.IMEI, this.getImei());
		smsSetting.put(JsonKey.SIM_CARD_PIN, this.getSimCardPIN());
		return smsSetting;
	}
	
	public String toString()
	{
		return this.toJSONObject().toString();
	}
	
	public void save(String path) {
		String fileName = this.getBaseDir() + path;
		this.prepareDir(fileName);	
		try 
		{
			FileUtil.write(fileName, this.toString().getBytes());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public void load(String path)
	{
		String fileName = this.getBaseDir() + path;
		this.prepareDir(fileName);
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
				this.connectionType = smsSetting.optString(JsonKey.CONNECTION_TYPE, "");
				this.smsCenter = smsSetting.optString(JsonKey.SMS_CENTER, "");
				this.setImei(smsSetting.optString(JsonKey.IMEI, ""));
				this.setSimCardPIN(smsSetting.optString(JsonKey.SIM_CARD_PIN, ""));
				this.incommingInterval = smsSetting.optInt(JsonKey.INCOMMING_INTERVAL, 0);
				this.timeRange = smsSetting.optInt(JsonKey.TIME_RANGE, 0);
				this.maxPerTimeRange = smsSetting.optInt(JsonKey.MAX_PER_TIME_RANGE, 0);
			}
			catch(JSONException e)
			{
				/**
				 * Do nothing
				 */
			}
		}
	}
	
	private void prepareDir(String fileName) {
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
	
	private String getBaseDir()
	{
		return UserAccount.class.getResource("/").getFile();
	}

	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) {
		this.connectionType = connectionType;
	}

	public String getSmsCenter() {
		return smsCenter;
	}

	public void setSmsCenter(String smsCenter) {
		this.smsCenter = smsCenter;
	}

	public int getIncommingInterval() {
		return incommingInterval;
	}

	public void setIncommingInterval(int incommingInterval) {
		this.incommingInterval = incommingInterval;
	}

	public int getTimeRange() {
		return timeRange;
	}

	public void setTimeRange(int timeRange) {
		this.timeRange = timeRange;
	}

	public int getMaxPerTimeRange() {
		return maxPerTimeRange;
	}

	public void setMaxPerTimeRange(int maxPerTimeRange) {
		this.maxPerTimeRange = maxPerTimeRange;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getSimCardPIN() {
		return simCardPIN;
	}

	public void setSimCardPIN(String simCardPIN) {
		this.simCardPIN = simCardPIN;
	}

	
	
	
}

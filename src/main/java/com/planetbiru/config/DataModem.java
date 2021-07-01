package com.planetbiru.config;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class DataModem {
	private String id = "";
	private String name = "";
	private String connectionType = "";
	private String smsCenter = "";
	private int incommingInterval = 0;
	private int timeRange = 0;
	private int maxPerTimeRange = 0;
	private String provider = "";
	private String imei = "";
	private String msisdn = "";
	private String imsi = "";
	private String recipientPrefix = "";
	private String simCardPIN = "";
	private int baudRate = 9600;
	private String parityBit = "";
	private String startBits = "";
	private String stopBits = "";
	private boolean defaultModem = false;
	private boolean active = false;

	public DataModem() {
	}
	
	public DataModem(JSONObject jsonObject) {
		this.id = jsonObject.optString("id", "");
		this.name = jsonObject.optString("name", "");
		this.connectionType = jsonObject.optString("connectionType", "");
		this.smsCenter = jsonObject.optString("smsCenter", "");
		this.incommingInterval = jsonObject.optInt("incommingInterval", 0);
		this.timeRange = jsonObject.optInt("timeRange", 0);
		this.maxPerTimeRange = jsonObject.optInt("maxPerTimeRange", 0);
		this.provider = jsonObject.optString("provider", "");
		this.imei = jsonObject.optString("imei", "");
		this.msisdn = jsonObject.optString("msisdn", "");
		this.imsi = jsonObject.optString("imsi", "");
		this.setRecipientPrefix(jsonObject.optString("recipientPrefix", ""));
		this.simCardPIN = jsonObject.optString("simCardPIN", "");
		this.baudRate = jsonObject.optInt("baudRate", 0);
		this.parityBit = jsonObject.optString("parityBit", "");
		this.startBits = jsonObject.optString("startBits", "");
		this.stopBits = jsonObject.optString("stopBits", "");
		this.defaultModem = jsonObject.optBoolean("defaultModem", false);
		this.active = jsonObject.optBoolean("active", false);
	}
	
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", this.id);
		jsonObject.put("name", this.name);
		jsonObject.put("connectionType", this.connectionType);
		jsonObject.put("smsCenter", this.smsCenter);
		jsonObject.put("incommingInterval", this.incommingInterval);
		jsonObject.put("timeRange", this.timeRange);
		jsonObject.put("maxPerTimeRange", this.maxPerTimeRange);
		jsonObject.put("provider", this.provider);
		jsonObject.put("imei", this.imei);
		jsonObject.put("msisdn", this.msisdn);
		jsonObject.put("imsi", this.imsi);
		jsonObject.put("recipientPrefix", this.getRecipientPrefix());
		jsonObject.put("simCardPIN", this.simCardPIN);
		jsonObject.put("baudRate", this.baudRate);
		jsonObject.put("parityBit", this.parityBit);
		jsonObject.put("startBits", this.startBits);
		jsonObject.put("stopBits", this.stopBits);
		jsonObject.put("defaultModem", this.defaultModem);
		jsonObject.put("active", this.active);
		return jsonObject;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getSimCardPIN() {
		return simCardPIN;
	}

	public void setSimCardPIN(String simCardPIN) {
		this.simCardPIN = simCardPIN;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public String getParityBit() {
		return parityBit;
	}

	public void setParityBit(String parityBit) {
		this.parityBit = parityBit;
	}

	public String getStartBits() {
		return startBits;
	}

	public void setStartBits(String startBits) {
		this.startBits = startBits;
	}

	public String getStopBits() {
		return stopBits;
	}

	public void setStopBits(String stopBits) {
		this.stopBits = stopBits;
	}

	public boolean isDefaultModem() {
		return defaultModem;
	}

	public void setDefaultModem(boolean defaultModem) {
		this.defaultModem = defaultModem;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getRecipientPrefix() {
		return recipientPrefix;
	}

	public void setRecipientPrefix(String recipientPrefix) {
		this.recipientPrefix = recipientPrefix;
	}

	public List<String> getRecipientPrefixList() {
		List<String> perfixes = new ArrayList<>();
		if(this.recipientPrefix.length() > 0)
		{
			String[] arr = recipientPrefix.split(",");
			for(int i = 0; i<arr.length; i++)
			{
				String str = arr[i].trim();
				if(!str.isEmpty())
				{
					perfixes.add(str);
				}
			}
		}
		return perfixes;
	}
	
	
	
}



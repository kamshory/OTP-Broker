package com.planetbiru.config;

import org.json.JSONObject;

public class DataModem {
	private String id = "";
	private String name = "";
	private String connectionType = "";
	private String smsCenter = "";
	private int incommingInterval = 0;
	private int timeRange = 0;
	private int maxPerTimeRange = 0;
	private String imei = "";
	private String simCardPIN = "";
	private boolean connected = false;
	private boolean active = false;

	public DataModem(JSONObject jsonObject) {
		this.id = jsonObject.optString("id", "");
		this.name = jsonObject.optString("name", "");
		this.connectionType = jsonObject.optString("connectionType", "");
		this.smsCenter = jsonObject.optString("smsCenter", "");
		this.incommingInterval = jsonObject.optInt("incommingInterval", 0);
		this.timeRange = jsonObject.optInt("timeRange", 0);
		this.maxPerTimeRange = jsonObject.optInt("maxPerTimeRange", 0);
		this.imei = jsonObject.optString("imei", "");
		this.simCardPIN = jsonObject.optString("simCardPIN", "");
		this.active = jsonObject.optBoolean("active", false);
	}
	public DataModem() {
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
		jsonObject.put("imei", this.imei);
		jsonObject.put("simCardPIN", this.simCardPIN);
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
	public boolean isConnected() {
		return connected;
	}
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
}



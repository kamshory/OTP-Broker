package com.planetbiru.config;

import org.json.JSONObject;

public class ModemData {
	public String id = "";
	public String name = "";
	public String connectionType = "";
	public String smsCenter = "";
	public int incommingInterval = 0;
	public int timeRange = 0;
	public int maxPerTimeRange = 0;
	public String imei = "";
	public String simCardPIN = "";
	public boolean active;

	public ModemData(JSONObject jsonObject) {
		this.id = jsonObject.optString("id", "");
		this.name = jsonObject.optString("name", "");
		this.connectionType = jsonObject.optString("connectionType", "");
		this.smsCenter = jsonObject.optString("smsCenter", "");
		this.incommingInterval = jsonObject.optInt("incommingInterval", 0);
		this.timeRange = jsonObject.optInt("timeRange", 0);
		this.maxPerTimeRange = jsonObject.optInt("maxPerTimeRange", 0);
		this.imei = jsonObject.optString("imei", "");
		this.simCardPIN = jsonObject.optString("simCardPIN", "");
	}
	public ModemData() {
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
		return jsonObject;
	}
}

package com.planetbiru.util;

import org.json.JSONArray;
import org.json.JSONObject;

public class ServerStatus {
	private static JSONArray status = new JSONArray();
	private static int maxRecord = 10;
	private ServerStatus()
	{
		
	}
	public static void append(JSONObject data)
	{
		JSONArray ja = new JSONArray();
		int lastLength = ServerStatus.getStatus().length();
		int start = 0;
		if(lastLength >= maxRecord)
		{
			start = lastLength - maxRecord + 1;
		}
		else
		{
			start = 0;
		}
		for(int i = start; i<lastLength; i++)
		{
			ja.put(ServerStatus.getStatus().get(i));
		}
		ja.put(data);
		ServerStatus.status = ja;
	}
	public static JSONArray getStatus() {
		return status;
	}

}

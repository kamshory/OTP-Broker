package com.planetbiru.task;

import org.json.JSONObject;

public class TaskExecutor {

	public JSONObject execute(String jobID, String cronExpression, String currentTimeStr, String prevFireTimeStr,
			String nextValidTimeAfterStr) {
		
		return new JSONObject();
	}

	public void sleep(long sleep)
	{
		try 
		{
			Thread.sleep(sleep);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}
	}
}

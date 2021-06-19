package com.planetbiru.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class TaskExecutor {

	private Logger logger = LogManager.getLogger(TaskExecutor.class);

	public JSONObject execute(String jobID, String cronExpression, String currentTimeStr, String prevFireTimeStr,
			String nextValidTimeAfterStr) {
		
		return new JSONObject();
	}

	protected void markJobStarted(String jobID, String currentTimeStr, String prevFireTimeStr,
			String nextValidTimeAfterStr) {
	}

	protected void markJobFinishied(String jobID, String currentTimeStr, String prevFireTimeStr,
			String nextValidTimeAfterStr) {
	}
	
	public void sleep(long sleep)
	{
		try 
		{
			Thread.sleep(sleep);
		} 
		catch (InterruptedException e) 
		{
			logger.error(e.getMessage());
			Thread.currentThread().interrupt();
		}
	}
}

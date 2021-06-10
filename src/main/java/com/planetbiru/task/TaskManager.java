package com.planetbiru.task;

import org.json.JSONObject;

public class TaskManager {

	private String name = "";
	private String cronExpression = "";
	private String lastStarted = "";
	private String lastFinish = "";
	private String lastSchedule = "";
	private String lastScheduleDone = "";
	private boolean finish = false;

	public TaskManager(String jobName) {
		this.load(jobName);
	}

	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getCronExpression() {
		return cronExpression;
	}


	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}


	public String getLastStarted() {
		return lastStarted;
	}


	public void setLastStarted(String lastStarted) {
		this.lastStarted = lastStarted;
	}


	public String getLastFinish() {
		return lastFinish;
	}


	public void setLastFinish(String lastFinish) {
		this.lastFinish = lastFinish;
	}


	public String getLastSchedule() {
		return lastSchedule;
	}


	public void setLastSchedule(String lastSchedule) {
		this.lastSchedule = lastSchedule;
	}


	public String getLastScheduleDone() {
		return lastScheduleDone;
	}


	public void setLastScheduleDone(String lastScheduleDone) {
		this.lastScheduleDone = lastScheduleDone;
	}


	public boolean isFinish() {
		return finish;
	}


	public void setFinish(boolean finish) {
		this.finish = finish;
	}


	public void load(String jobID) {
		
	}
	
	
	public boolean isStarted(String prevFireTimeStr, String resolution) 
	{
		String time1 = prevFireTimeStr;
		String time2 = this.lastSchedule;
		if(resolution.equals("minute"))
		{
			time1 = (time1.length() > 15)?time1.substring(0, 15):time1;
			time2 = (time2.length() > 15)?time2.substring(0, 15):time2;
		}
		return (time1.compareToIgnoreCase(time2) <= 0);
	}

	public JSONObject execute(String jobID, String cronExpression, String currentTimeStr, String prevFireTimeStr, String nextValidTimeAfterStr) 
	{
		TaskExecutor executor = new TaskExecutor();
		return executor.execute(jobID, cronExpression, currentTimeStr, prevFireTimeStr, nextValidTimeAfterStr);
	}

}

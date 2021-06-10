package com.planetbiru.task;

public class ThreadExecutor extends Thread {

	private String jobID;
	private String cronExpression;
	private String currentTimeStr;
	private String prevFireTimeStr;
	private String nextValidTimeAfterStr;
	private String resolution;
	public ThreadExecutor(String jobID, String cronExpression, String currentTimeStr, String prevFireTimeStr,
			String nextValidTimeAfterStr, String resolution) {
		this.jobID = jobID;
		this.cronExpression = cronExpression;
		this.currentTimeStr = currentTimeStr;
		this.prevFireTimeStr = prevFireTimeStr;
		this.nextValidTimeAfterStr = nextValidTimeAfterStr;
		this.resolution = resolution;
	}
	@Override
	public void run()
	{
		TaskManager task = new TaskManager(this.jobID);
		if(!task.isStarted(this.prevFireTimeStr, this.resolution))
		{
			task.execute(this.jobID, this.cronExpression, this.currentTimeStr, this.prevFireTimeStr, this.nextValidTimeAfterStr);
		}
	}

}

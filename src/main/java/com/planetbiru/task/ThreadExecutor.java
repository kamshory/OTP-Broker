package com.planetbiru.task;

import com.planetbiru.ddns.DDNSUpdater;

public class ThreadExecutor extends Thread {

	private String jobID;
	private String cronExpression;
	private String currentTimeStr;
	private String prevFireTimeStr;
	private String nextValidTimeAfterStr;
	private String resolution;
	private String zone;
	private String recordName;
	public ThreadExecutor(String jobID, String zone, String recordName, String currentTimeStr, String prevFireTimeStr,
			String nextValidTimeAfterStr, String resolution) {
		this.jobID = jobID;
		this.zone = zone;
		this.recordName = recordName;
		this.currentTimeStr = currentTimeStr;
		this.prevFireTimeStr = prevFireTimeStr;
		this.nextValidTimeAfterStr = nextValidTimeAfterStr;
		this.resolution = resolution;
	}
	@Override
	public void run()
	{
		//DDNSUpdater ddns = new DDNSUpdater();
	}

}

package com.planetbiru;

import java.text.ParseException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.CronExpression;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ExpressionException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.task.ThreadExecutor;
import com.planetbiru.util.Utility;


@EnableScheduling
@Component
public class ServerScheduler {

	private Logger logger = LogManager.getLogger(ServerScheduler.class);
	
	@Value("${otpbroker.cron.time.resolution:minute}")
	private String timeResolution;
		
	@Scheduled(cron = "${otpbroker.cron.expression}")
	public void task() 
	{	
		logger.info("Run...");
		String cronExpression = "";
		String jobID = "";
		CronExpression exp;
		Date currentTime = new Date();
		
		JSONArray list = getTaskList();
		for(int i = 0; i<list.length(); i++)
		{
			JSONObject jo = list.getJSONObject(i);
			cronExpression = jo.optString(JsonKey.CRON_EXPRESSION, "");
			jobID = jo.optString(JsonKey.JOB_ID, "");
			try
			{
				exp = new CronExpression(cronExpression);
				Date prevFireTime = exp.getPrevFireTime(currentTime);
				Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
				
				String prevFireTimeStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, prevFireTime);
				String currentTimeStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, currentTime);
				String nextValidTimeAfterStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, nextValidTimeAfter);
				
				ThreadExecutor exec = new ThreadExecutor(jobID, cronExpression, currentTimeStr, prevFireTimeStr, nextValidTimeAfterStr, this.timeResolution);
				exec.start();
			}
			catch(ExpressionException | ParseException | JSONException e)
			{
				logger.error(e.getMessage());
			}
		}
	}
	
	public JSONArray getTaskList() 
	{
		JSONArray list = new JSONArray();
		return list;
	}
	
	
    
}

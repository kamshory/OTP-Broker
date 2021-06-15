package com.planetbiru;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.CronExpression;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ExpressionException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.planetbiru.config.ConfigCloudflare;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.ddns.DDNSUpdater;
import com.planetbiru.ddns.DDNSRecord;
import com.planetbiru.util.Utility;


@EnableScheduling
@Component
public class ServerScheduler {

	private Logger logger = LogManager.getLogger(ServerScheduler.class);
	
	@Value("${otpbroker.cron.time.resolution:minute}")
	private String timeResolution;
	
	@Value("${otpbroker.ddns.provider}")
	private String ddnsProvider;
	
	@Value("${otpbroker.path.setting.ddns}")
	private String ddnsSettingPath;
	
	@Value("${otpbroker.path.setting.cloudflare}")
	private String cloudflareSettingPath;
	
	@PostConstruct
	public void init()
	{
		ConfigDDNS.load(ddnsSettingPath);
		ConfigCloudflare.load(cloudflareSettingPath);
	}
		
	@Scheduled(cron = "${otpbroker.cron.expression}")
	public void task() 
	{	
		logger.info("Run...");

		CronExpression exp;
		Date currentTime = new Date();
		
		
		Map<String, DDNSRecord> list = ConfigDDNS.getRecords();
		for(Entry<String, DDNSRecord> set : list.entrySet())
		{
			DDNSRecord ddnsRecord = set.getValue();
			String cronExpression = ddnsRecord.getRecordName();		
			try
			{
				exp = new CronExpression(cronExpression);
				Date prevFireTime = exp.getPrevFireTime(currentTime);
				Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);
				
				String prevFireTimeStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, prevFireTime);
				String currentTimeStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, currentTime);
				String nextValidTimeAfterStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, nextValidTimeAfter);
				
				DDNSUpdater ddns = new DDNSUpdater(ddnsProvider, ddnsRecord, prevFireTimeStr, currentTimeStr, nextValidTimeAfterStr);
				ddns.start();
				
			}
			catch(ExpressionException | ParseException | JSONException e)
			{
				logger.error(e.getMessage());
			}
			
		}

		
	}

    
}

package com.planetbiru;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

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

import com.planetbiru.config.ConfigCloudflare;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.ddns.DDNSUpdater;
import com.planetbiru.gsm.SMSUtil;
import com.planetbiru.ddns.DDNSRecord;
import com.planetbiru.util.Utility;


@EnableScheduling
@Component
public class ServerScheduler {

	private Logger logger = LogManager.getLogger(ServerScheduler.class);
	
	@Value("${otpbroker.cron.enable.ddns}")
	private boolean ddnsUpdate;

	@Value("${otpbroker.cron.enable.device}")
	private boolean cronDeviceEnable;

	@Value("${otpbroker.cron.time.resolution:minute}")
	private String timeResolution;
	
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
	
	
	@Scheduled(cron = "${otpbroker.cron.expression.device}")
	public void inspectDevice()
	{
		if(cronDeviceEnable)
		{					
			modemCheck();
			
			if(ConfigFeederAMQP.isFeederAmqpEnable())
			{
				amqpCheck();
				this.sendAMQPStatus(ConfigFeederAMQP.isConnected());
			}
		}
	}
	
	private void modemCheck()
	{
		if(SMSUtil.isConnected())
		{	
			String alert = ConstantString.MODEM_NOT_CONNECTED;
			JSONObject messageJSON = new JSONObject();
			messageJSON.put(JsonKey.COMMAND, "broadcast-message");
			JSONArray data = new JSONArray();
			JSONObject itemData = new JSONObject();
			String uuid = UUID.randomUUID().toString();
			itemData.put(JsonKey.ID, uuid);
			itemData.put(JsonKey.MESSAGE, alert);
			itemData.put(JsonKey.DATE_TIME, Utility.now(ConstantString.ISO_DATE_TIME_FORMAT, ConstantString.UTC));
			data.put(itemData);
			messageJSON.put(JsonKey.DATA, data);		
			ServerWebSocketManager.broadcast(messageJSON.toString());
		}
	}
	
	private void amqpCheck()
	{
		boolean connected = ConfigFeederAMQP.echoTest();
		ConfigFeederAMQP.setConnected(connected);		
	}
	
	private void sendAMQPStatus(boolean connected)
	{
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		
		JSONObject ws = new JSONObject();
		ws.put(JsonKey.NAME, "amqp_connected");
		ws.put(JsonKey.VALUE, connected);
		data.put(ws);
		
		info.put(JsonKey.COMMAND, "server-info");
		info.put(JsonKey.DATA, data);
	
		ServerWebSocketManager.broadcast(info.toString(4));
	}
	
		
	@Scheduled(cron = "${otpbroker.cron.expression.ddns}")
	public void updateDNS() 
	{
		
		if(ddnsUpdate)
		{
			int countUpdate = 0;	
			Map<String, DDNSRecord> list = ConfigDDNS.getRecords();
			for(Entry<String, DDNSRecord> set : list.entrySet())
			{
				String ddnsId = set.getKey();
				DDNSRecord ddnsRecord = set.getValue();
				if(ddnsRecord.isActive())
				{
					boolean update = updateDNS(ddnsRecord, ddnsId);
					if(update)
					{
						countUpdate++;
					}
				}
			}
			if(countUpdate > 0)
			{
				ConfigDDNS.save(ddnsSettingPath);
			}	
		}
	}
	private boolean updateDNS(DDNSRecord ddnsRecord, String ddnsId) 
	{
		boolean update = false;
		String cronExpression = ddnsRecord.getCronExpression();		
		CronExpression exp;		
		try
		{
			exp = new CronExpression(cronExpression);
			Date currentTime = new Date();
			Date prevFireTime = exp.getPrevFireTime(currentTime);
			Date nextValidTimeAfter = exp.getNextValidTimeAfter(currentTime);

			String prevFireTimeStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, prevFireTime);
			String currentTimeStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, currentTime);
			String nextValidTimeAfterStr = Utility.date(ConstantString.MYSQL_DATE_TIME_FORMAT_MS, nextValidTimeAfter);
			
			if(currentTime.getTime() > ddnsRecord.getNextValid().getTime())
			{
				DDNSUpdater ddns = new DDNSUpdater(ddnsRecord, prevFireTimeStr, currentTimeStr, nextValidTimeAfterStr);
				ddns.start();
				
				ConfigDDNS.getRecords().get(ddnsId).setNextValid(nextValidTimeAfter);		
				ConfigDDNS.getRecords().get(ddnsId).setLastUpdate(currentTime);
				update = true;
			}
		}
		catch(ExpressionException | ParseException | JSONException e)
		{
			logger.error(e.getMessage());
		}
		return update;	
	}
    
}

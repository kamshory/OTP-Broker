package com.planetbiru;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
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

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigVendorAfraid;
import com.planetbiru.config.ConfigVendorCloudflare;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigVendorDynu;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ConfigVendorNoIP;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.ddns.DDNSUpdater;
import com.planetbiru.gsm.SMSUtil;
import com.planetbiru.ddns.DDNSRecord;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.ServerStatus;
import com.planetbiru.util.Utility;

@EnableScheduling
@Component
public class ServerScheduler {

	private Logger logger = LogManager.getLogger(ServerScheduler.class);
	
	@Value("${otpbroker.path.base.setting}")
	private String baseDirConfig;
	
	@Value("${otpbroker.cron.enable.ddns}")
	private boolean ddnsUpdate;
	
	@Value("${otpbroker.cron.enable.ntp}")
	private boolean timeUpdate;

	@Value("${otpbroker.cron.enable.device}")
	private boolean cronDeviceEnable;

	@Value("${otpbroker.cron.enable.amqp}")
	private boolean cronAMQPEnable;

	@Value("${otpbroker.cron.time.resolution:minute}")
	private String timeResolution;
	
	@Value("${otpbroker.path.setting.ddns}")
	private String ddnsSettingPath;
	
	@Value("${otpbroker.path.setting.ddns.cloudflare}")
	private String cloudflareSettingPath;
	
	@Value("${otpbroker.path.setting.ddns.noip}")
	private String noIPSettingPath;

	@Value("${otpbroker.path.setting.ddns.afraid}")
	private String afraidSettingPath;
	
	@Value("${otpbroker.path.setting.ddns.dynu}")
	private String dynuSettingPath;

	@Value("${otpbroker.path.setting.server.status}")
	private String serverStatusSettingPath;

	@Value("${otpbroker.path.setting.general}")
	private String generalSettingPath;

	@PostConstruct
	public void init()
	{
		/**
		 * This configuration must be loaded first
		 */
		Config.setBaseDirConfig(baseDirConfig);

		Config.setDdnsSettingPath(ddnsSettingPath);
		
		Config.setCloudflareSettingPath(cloudflareSettingPath);
		Config.setNoIPSettingPath(noIPSettingPath);
		Config.setDynuSettingPath(dynuSettingPath);
		Config.setAfraidSettingPath(afraidSettingPath);	
		Config.setGeneralSettingPath(generalSettingPath);
		
		ConfigDDNS.load(Config.getDdnsSettingPath());

		ConfigVendorCloudflare.load(Config.getCloudflareSettingPath());
		ConfigVendorNoIP.load(Config.getNoIPSettingPath());
		ConfigVendorDynu.load(Config.getDynuSettingPath());
		ConfigVendorAfraid.load(Config.getAfraidSettingPath());		
		ConfigGeneral.load(Config.getGeneralSettingPath());
		
		ServerStatus.load(serverStatusSettingPath);
	}
	
	@Scheduled(cron = "${otpbroker.cron.expression.server.status}")
	public void updateServerStatus()
	{
		JSONObject data = new JSONObject();
		data.put("datetime", System.currentTimeMillis());
		
		JSONObject memory = ServerInfo.memoryInfo();
		JSONObject cpu = ServerInfo.cpuUsage();
		JSONObject storage = ServerInfo.storageInfo();

		data.put("storage", storage.optDouble("percentUsed", 0));
		data.put("cpu", cpu.optDouble("percentUsed", 0));
		data.put("ram", memory.optJSONObject("ram").optDouble("percentUsed", 0));
		data.put("swap", memory.optJSONObject("swap").optDouble("percentUsed", 0));
		data.put("modem", SMSUtil.isConnected());
		data.put("ws", ConfigFeederWS.isConnected());
		data.put("amqp", ConfigFeederAMQP.isConnected());

		ServerStatus.append(data);
		ServerStatus.save();
	}
	
	
	public void updateTime()
	{
		String cronExpression = ConfigGeneral.getNtpUpdateInterval();		
		String ntpServer = ConfigGeneral.getNtpServer();		
		if(!cronExpression.isEmpty() && !ntpServer.isEmpty())
		{
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
				
				if(currentTime.getTime() > ConfigGeneral.getNextValid().getTime())
				{
					NTPUDPClient client = new NTPUDPClient();
				    // We want to timeout if a response takes longer than 10 seconds
				    client.setDefaultTimeout(10000);
			
				    InetAddress inetAddress;
					try 
					{
						inetAddress = InetAddress.getByName(ntpServer);
					    TimeInfo timeInfo = client.getTime(inetAddress);
					    timeInfo.computeDetails();
					    Long offset = Long.getLong("0");
						if (timeInfo.getOffset() != null) 
					    {
					        offset  = timeInfo.getOffset();
					    }
			
					    // Calculate the remote server NTP time
					    long currentTimeMils = System.currentTimeMillis();
					    TimeStamp atomicNtpTime = TimeStamp.getNtpTime(currentTimeMils + offset);
			
					    Date date = new Date(atomicNtpTime.getTime());
					    System.out.println("LOCAL DATE : "+Utility.date("yyyy-MM-dd HH:mm:ss.SSS", new Date()));
					    System.out.println("NTP DATE   : "+Utility.date("yyyy-MM-dd HH:mm:ss.SSS", date));
					
					    /**
					     * URL : https://www.thegeekstuff.com/2013/08/hwclock-examples/
					     */
					    String command = "hwclock --set --date \""+Utility.date("MM/dd/yyyy HH:mm:ss", date)+"\"";
					    System.out.println("COMMAND : "+command);
					    ConfigGeneral.setNextValid(nextValidTimeAfter);
					} 
					catch (IOException e) 
					{
						/**
						 * 
						 */
					}
				}
			}
			catch(ExpressionException | ParseException | JSONException e)
			{
				logger.error(e.getMessage());
			}
		}
	}
	
	@Scheduled(cron = "${otpbroker.cron.expression.device}")
	public void inspectDevice()
	{
		if(cronDeviceEnable)
		{
			modemCheck();
		}
	}
	
	@Scheduled(cron = "${otpbroker.cron.expression.amqp}")
	public void inspectAMQP()
	{
		if(cronAMQPEnable && ConfigFeederAMQP.isFeederAmqpEnable())
		{					
			amqpCheck();
		}
	}
	
	private void modemCheck()
	{
		JSONArray data = new JSONArray();
		JSONObject modem = new JSONObject();
		modem.put(JsonKey.NAME, "otp-modem-connected");
		modem.put(JsonKey.VALUE, SMSUtil.isConnected());
		modem.put(JsonKey.DATA, ConfigModem.getStatus());
		data.put(modem);
		JSONObject serverInfo = new JSONObject();
		serverInfo.put(JsonKey.DATA, data);
		serverInfo.put(JsonKey.COMMAND, "server-info");
		ServerWebSocketManager.broadcast(serverInfo.toString());
	}

	private void amqpCheck()
	{
		boolean connected = ConfigFeederAMQP.echoTest();
		ConfigFeederAMQP.setConnected(connected);	
		ServerInfo.sendAMQPStatus(ConfigFeederAMQP.isConnected());
	}
	
	@Scheduled(cron = "${otpbroker.cron.expression.general}")
	public void generalCronChecker() 
	{
		if(timeUpdate)
		{
			updateTime();
		}		
		if(ddnsUpdate)
		{
			updateDNS();
		}
	}
	
	private void updateDNS()
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
			ConfigDDNS.save();
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


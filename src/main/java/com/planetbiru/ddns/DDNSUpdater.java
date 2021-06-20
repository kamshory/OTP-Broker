package com.planetbiru.ddns;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.planetbiru.config.ConfigCloudflare;
import com.planetbiru.config.ConfigNoIP;

public class DDNSUpdater extends Thread{
	
	private static Logger logger = LogManager.getLogger(DDNSUpdater.class);   

	private DDNSRecord ddnsRecord;
	private String prevFireTimeStr;
	private String currentTimeStr;
	private String nextValidTimeAfterStr;

	public DDNSUpdater(DDNSRecord ddnsRecord, String prevFireTimeStr, String currentTimeStr, String nextValidTimeAfterStr) {
		this.ddnsRecord = ddnsRecord;
		this.prevFireTimeStr = prevFireTimeStr;
		this.currentTimeStr = currentTimeStr;
		this.nextValidTimeAfterStr = nextValidTimeAfterStr;
	}

	@Override
	public void run()
	{
		if(this.ddnsRecord.getProvider().equals("cloudflare"))
		{
			logger.info("Executing update DDNS");
			DNSCloudflare ddns = new DNSCloudflare();
			
			String endpoint = ConfigCloudflare.getEndpoint();
			String accountId = ConfigCloudflare.getAccountId();
			String authEmail = ConfigCloudflare.getAuthEmail();
			String authApiKey = ConfigCloudflare.getAuthApiKey();
			String authToken = ConfigCloudflare.getAuthToken();
			ddns.setConfig(endpoint, accountId, authEmail, authApiKey, authToken);
			
			if(this.ddnsRecord.isForceCreateZone())
			{
				JSONObject res1 = ddns.createZoneIfNotExists(ddnsRecord);
				logger.info("RECORD                  : {}", ddnsRecord.toJSONObject().toString(4));
				if(res1 != null)
				{
					logger.info("res1                    : {}", res1.toString(4));
				}
				logger.info("prevFireTimeStr         : {}", prevFireTimeStr);
				logger.info("currentTimeStr          : {}", currentTimeStr);
				logger.info("nextValidTimeAfterStr   : {}", nextValidTimeAfterStr);
			}
			JSONObject res2 = ddns.update(ddnsRecord);		
			if(res2 != null)
			logger.info("res2                    : {}", res2.toString(4));
		}
		else if(this.ddnsRecord.getProvider().equals("noip"))
		{
			System.out.println("Executing update DDNS");
			logger.info("Executing update DDNS");
			DNSNoIP ddns = new DNSNoIP();
			
			String endpoint = ConfigNoIP.getEndpoint();
			String username = ConfigNoIP.getUsername();
			String password = ConfigNoIP.getPassword();
			String company = ConfigNoIP.getCompany();
			String email = ConfigNoIP.getEmail();
			
			ddns.setConfig(endpoint, username, password, email, company);

			JSONObject res2 = ddns.update(ddnsRecord);		
			if(res2 != null)
			logger.info("res2                    : {}", res2.toString(4));
		}
	}
}

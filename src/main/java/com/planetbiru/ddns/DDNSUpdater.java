package com.planetbiru.ddns;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.planetbiru.config.ConfigVendorAfraid;
import com.planetbiru.config.ConfigVendorCloudflare;
import com.planetbiru.config.ConfigVendorDynu;
import com.planetbiru.config.ConfigVendorNoIP;

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
			
			String endpoint = ConfigVendorCloudflare.getEndpoint();
			String accountId = ConfigVendorCloudflare.getAccountId();
			String authEmail = ConfigVendorCloudflare.getAuthEmail();
			String authApiKey = ConfigVendorCloudflare.getAuthApiKey();
			String authToken = ConfigVendorCloudflare.getAuthToken();
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
			{
				logger.info("res2                    : {}", res2.toString(4));
			}
		}
		else if(this.ddnsRecord.getProvider().equals("noip"))
		{
			System.out.println("Executing update DDNS");
			logger.info("Executing update DDNS");
			DNSNoIP ddns = new DNSNoIP();
			
			String endpoint = ConfigVendorNoIP.getEndpoint();
			String username = ConfigVendorNoIP.getUsername();
			String password = ConfigVendorNoIP.getPassword();
			String company = ConfigVendorNoIP.getCompany();
			String email = ConfigVendorNoIP.getEmail();
			
			ddns.setConfig(endpoint, username, password, email, company);

			JSONObject res2 = ddns.update(ddnsRecord);		
			if(res2 != null)
			{
				logger.info("res2                    : {}", res2.toString(4));
			}
		}
		else if(this.ddnsRecord.getProvider().equals("afraid"))
		{
			System.out.println("Executing update DDNS");
			logger.info("Executing update DDNS");
			DNSAfraid ddns = new DNSAfraid();
			
			String endpoint = ConfigVendorAfraid.getEndpoint();
			String username = ConfigVendorAfraid.getUsername();
			String password = ConfigVendorAfraid.getPassword();
			String company = ConfigVendorAfraid.getCompany();
			String email = ConfigVendorAfraid.getEmail();
			
			ddns.setConfig(endpoint, username, password, email, company);

			JSONObject res2 = ddns.update(ddnsRecord);		
			if(res2 != null)
			{
				logger.info("res2                    : {}", res2.toString(4));
			}
		}
		else if(this.ddnsRecord.getProvider().equals("dynu"))
		{
			System.out.println("Executing update DDNS");
			logger.info("Executing update DDNS");
			DNSDynu ddns = new DNSDynu();
			
			String endpoint = ConfigVendorDynu.getEndpoint();
			String username = ConfigVendorDynu.getUsername();
			String password = ConfigVendorDynu.getPassword();
			String company = ConfigVendorDynu.getCompany();
			String email = ConfigVendorDynu.getEmail();
			
			ddns.setConfig(endpoint, username, password, email, company);

			JSONObject res2 = ddns.update(ddnsRecord);		
			if(res2 != null)
			{
				logger.info("res2                    : {}", res2.toString(4));
			}
		}
	}
}

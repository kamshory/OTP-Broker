package com.planetbiru.ddns;

import org.json.JSONObject;

import com.planetbiru.config.ConfigCloudflare;

public class DDNSUpdater extends Thread{

	private DDNSRecord ddnsRecord;
	private String ddnsProvider;
	private String prevFireTimeStr;
	private String currentTimeStr;
	private String nextValidTimeAfterStr;

	public DDNSUpdater(String ddnsProvider, DDNSRecord ddnsRecord, String prevFireTimeStr, String currentTimeStr, String nextValidTimeAfterStr) {
		this.ddnsProvider = ddnsProvider;
		this.ddnsRecord = ddnsRecord;
		this.prevFireTimeStr = prevFireTimeStr;
		this.currentTimeStr = currentTimeStr;
		this.nextValidTimeAfterStr = nextValidTimeAfterStr;
	}

	@Override
	public void run()
	{
		DNS ddns;
		if(this.ddnsProvider.equals("cloudflare"))
		{
			ddns = new DNSCloudflare();
			
			String endpoint = ConfigCloudflare.getEndpoint();
			String accountId = ConfigCloudflare.getAccountId();
			String authEmail = ConfigCloudflare.getAuthEmail();
			String authApiKey = ConfigCloudflare.getAuthApiKey();
			
			ddns.setConfig(endpoint, accountId, authEmail, authApiKey);
			
			if(this.ddnsRecord.isForceCreateZone())
			{
				JSONObject res1 = ddns.createZoneIfNotExists(ddnsRecord);
			}
			JSONObject res2 = ddns.update(ddnsRecord);		
		}
	}
}

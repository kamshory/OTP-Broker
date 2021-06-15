package com.planetbiru.ddns;

import org.json.JSONObject;

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
		DNS ddns = new DNS();
		if(this.ddnsProvider.equals("cloudflare"))
		{
			ddns = new DNSCloudflare();
			if(this.ddnsRecord.isForceCreateZone())
			{
				JSONObject res1 = ddns.createZoneIfNotExists(ddnsRecord);
			}
			JSONObject res2 = ddns.update(ddnsRecord);		
		}
	}
}

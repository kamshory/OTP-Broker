package com.planetbiru.gsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ModemData;

public class SMSUtil {
	private SMSUtil()
	{
		/**
		 * Do nothing
		 */
	}
	
	
	private static boolean initialized = false;
	private static List<SMSInstance> smsInstance = new ArrayList<>();
	public static void init()
	{
		smsInstance = new ArrayList<>();
		Map<String, ModemData> modemData = ConfigModem.getModemData();	
		
		for (Map.Entry<String, ModemData> entry : modemData.entrySet())
		{
			ModemData modem = entry.getValue();
			String port = modem.connectionType;
			
			SMSInstance instance = new SMSInstance();
			instance.init(port);
			if(!instance.isClosed())
			{
				instance.setConnected(true);
			}
			smsInstance.add(instance);
		}
		SMSUtil.initialized = true;
	}
	public static void sendSMS(String receiver, String message) throws GSMNullException {
		if(smsInstance.isEmpty())
		{
			throw new GSMNullException("No device connected");
		}
		int index = getModemIndex();
		smsInstance.get(index).sendSMS(receiver, message);
		
	}
	public static String executeUSSD(String ussd) throws GSMNullException {
		if(smsInstance.isEmpty())
		{
			throw new GSMNullException("No device connected");
		}
		int index = getModemIndex();
		return smsInstance.get(index).executeUSSD(ussd);
	}
	public static boolean isClosed()
	{
		if(smsInstance.isEmpty())
		{
			return true;
		}
		int index = getModemIndex();
		return smsInstance.get(index).isClosed();
	}
	public static boolean isConnected() {
		if(smsInstance.isEmpty())
		{
			return false;
		}
		return SMSUtil.initialized && !SMSUtil.isClosed();
	}

	private static int getModemIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

}





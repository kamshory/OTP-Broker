package com.planetbiru.gsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ModemData;

public class SMSUtil {
	private static final String NO_DEVICE_CONNECTED = "No device connected";
	private static boolean initialized = false;
	private static List<SMSInstance> smsInstance = new ArrayList<>();
	private static List<Integer> connectedDevices = new ArrayList<>();
	private static int cnt = -1;

	private SMSUtil()
	{
		/**
		 * Do nothing
		 */
	}
	
	
	public static void init()
	{
		SMSUtil.smsInstance = new ArrayList<>();
		Map<String, ModemData> modemData = ConfigModem.getModemData();	
		
		for (Map.Entry<String, ModemData> entry : modemData.entrySet())
		{
			ModemData modem = entry.getValue();
			String port = modem.getConnectionType();			
			SMSInstance instance = new SMSInstance();
			instance.connect(port);
			if(!instance.isClosed())
			{
				instance.setConnected(true);
				modem.setConnected(true);
			}
			SMSUtil.smsInstance.add(instance);
		}
		SMSUtil.initialized = true;
		SMSUtil.updateConnectedDevice();
	}

	public static void connect(String modemID)
	{
		ModemData modem = ConfigModem.getModemData(modemID);
		for(int i = 0; i<SMSUtil.smsInstance.size(); i++)
		{
			SMSInstance instance =  SMSUtil.smsInstance.get(i);
			if(instance.getId().endsWith(modemID))
			{
				instance.connect(modem.getConnectionType());
				ConfigModem.getModemData(modemID).setConnected(instance.isConnected());
			}
		}
		SMSUtil.updateConnectedDevice();
	}
	
	public static void sendSMS(String receiver, String message) throws GSMNullException {
		if(SMSUtil.smsInstance.isEmpty())
		{
			throw new GSMNullException(SMSUtil.NO_DEVICE_CONNECTED);
		}
		int index = SMSUtil.getModemIndex();
		SMSUtil.smsInstance.get(index).sendSMS(receiver, message);
		
	}
	public static String executeUSSD(String ussd) throws GSMNullException {
		if(smsInstance.isEmpty())
		{
			throw new GSMNullException(SMSUtil.NO_DEVICE_CONNECTED);
		}
		int index = SMSUtil.getModemIndex();
		return SMSUtil.smsInstance.get(index).executeUSSD(ussd);
	}


	public static boolean isConnected() {
		if(SMSUtil.smsInstance.isEmpty())
		{
			return false;
		}
		return SMSUtil.initialized && countConnected() > 0;
	}
	
	public static int countConnected()
	{
		int connected = 0;
		Map<String, ModemData> modemData = ConfigModem.getModemData();	
		for (Map.Entry<String, ModemData> entry : modemData.entrySet())
		{
			ModemData modem = entry.getValue();
			if(modem.isConnected())
			{
				connected++;
			}
		}
		return connected;
	}
	
	public static boolean isConnected(String modemID)
	{
		Map<String, ModemData> modemData = ConfigModem.getModemData();	
		for (Map.Entry<String, ModemData> entry : modemData.entrySet())
		{
			ModemData modem = entry.getValue();
			if(modem.isConnected() && modem.getId().equals(modemID))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean isConnected(int index)
	{
		if(SMSUtil.smsInstance.isEmpty())
		{
			return false;
		}
		return SMSUtil.smsInstance.get(index).isConnected();
	}
	
	private static void updateConnectedDevice() {
		List<Integer> connectedDev = new ArrayList<>();
		for(int i = 0; i<SMSUtil.smsInstance.size(); i++)
		{
			if(SMSUtil.isConnected(i))
			{
				connectedDev.add(i);
			}
		}
		SMSUtil.connectedDevices = connectedDev;
	}
	private static int getModemIndex() throws GSMNullException {
		SMSUtil.cnt++;
		if(SMSUtil.cnt > SMSUtil.countConnected())
		{
			SMSUtil.cnt = 0;
		}
		if(SMSUtil.connectedDevices.contains(SMSUtil.cnt))
		{
			return SMSUtil.connectedDevices.get(SMSUtil.cnt);
		}
		else
		{
			throw new GSMNullException(SMSUtil.NO_DEVICE_CONNECTED);
		}
	}

}





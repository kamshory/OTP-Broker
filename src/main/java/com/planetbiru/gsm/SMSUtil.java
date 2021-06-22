package com.planetbiru.gsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.DataModem;

public class SMSUtil {
	
	private static Logger logger = LogManager.getLogger(SMSUtil.class);
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
		Map<String, DataModem> modemData = ConfigModem.getModemData();		
		for (Map.Entry<String, DataModem> entry : modemData.entrySet())
		{
			DataModem modem = entry.getValue();
			String port = modem.getConnectionType();			
			SMSInstance instance = new SMSInstance();
			try {
				instance.connect(port);
				if(!instance.isClosed())
				{
					instance.setConnected(true);
					modem.setConnected(true);
				}
			} 
			catch (GSMException e) 
			{
				logger.error(e.getMessage());
				//e.printStackTrace();
			}
			
			SMSUtil.smsInstance.add(instance);
		}
		SMSUtil.initialized = true;
		SMSUtil.updateConnectedDevice();
	}

	public static void connect(String modemID) throws GSMException
	{
		DataModem modem = ConfigModem.getModemData(modemID);
		for(int i = 0; i<SMSUtil.smsInstance.size(); i++)
		{
			SMSInstance instance =  SMSUtil.smsInstance.get(i);
			if(instance.getId().equals(modemID))
			{
				instance.connect(modem.getConnectionType());
				ConfigModem.getModemData(modemID).setConnected(instance.isConnected());
			}
		}
		SMSUtil.updateConnectedDevice();
	}
	public static void disconnect(String modemID) throws GSMException {

		for(int i = 0; i<SMSUtil.smsInstance.size(); i++)
		{
			SMSInstance instance =  SMSUtil.smsInstance.get(i);
			if(instance.getId().equals(modemID))
			{
				instance.disconnect();
				ConfigModem.getModemData(modemID).setConnected(instance.isConnected());
			}
		}
		SMSUtil.updateConnectedDevice();
		
	}
	
	public static void sendSMS(String receiver, String message, String modemID) throws GSMException {
		if(SMSUtil.smsInstance.isEmpty())
		{
			throw new GSMException(SMSUtil.NO_DEVICE_CONNECTED);
		}
		SMSUtil.get(modemID).sendSMS(receiver, message);		
	}
	
	public static void sendSMS(String receiver, String message) throws GSMException {
		System.out.println(receiver);
		System.out.println(message);
		if(SMSUtil.smsInstance.isEmpty())
		{
			throw new GSMException(SMSUtil.NO_DEVICE_CONNECTED);
		}
		int index = SMSUtil.getModemIndex();
		System.out.println(index);
		SMSUtil.smsInstance.get(index).sendSMS(receiver, message);
		
	}
	public static String executeUSSD(String ussd, String modemID) throws GSMException {
		if(smsInstance.isEmpty())
		{
			throw new GSMException(SMSUtil.NO_DEVICE_CONNECTED);
		}
		SMSInstance instance = SMSUtil.get(modemID);		
		if(instance.isConnected())
		{
			return instance.executeUSSD(ussd);
		}
		else
		{
			throw new GSMException("The selected device is not connected");
		}
	}

	private static SMSInstance get(String modemID) throws GSMException {
		for(int i = 0; i<SMSUtil.smsInstance.size(); i++)
		{
			SMSInstance instance =  SMSUtil.smsInstance.get(i);
			if(instance.getId().equals(modemID))
			{
				return instance;
			}
		}
		throw new GSMException(SMSUtil.NO_DEVICE_CONNECTED);
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
		Map<String, DataModem> modemData = ConfigModem.getModemData();	
		for (Map.Entry<String, DataModem> entry : modemData.entrySet())
		{
			DataModem modem = entry.getValue();
			if(modem.isConnected())
			{
				connected++;
			}
		}
		return connected;
	}
	
	public static boolean isConnected(String modemID)
	{
		Map<String, DataModem> modemData = ConfigModem.getModemData();	
		for (Map.Entry<String, DataModem> entry : modemData.entrySet())
		{
			DataModem modem = entry.getValue();
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
	private static int getModemIndex() throws GSMException {
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
			throw new GSMException(SMSUtil.NO_DEVICE_CONNECTED);
		}
	}


	

}





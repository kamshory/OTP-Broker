package com.planetbiru.gsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.planetbiru.ServerWebSocketManager;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.DataModem;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.util.Utility;

public class SMSUtil {
	
	private static Logger logger = LogManager.getLogger(SMSUtil.class);
	private static final String NO_DEVICE_CONNECTED = "No device connected";
	private static boolean initialized = false;
	private static List<SMSInstance> smsInstance = new ArrayList<>();
	private static List<Integer> connectedDevices = new ArrayList<>();
	private static int cnt = -1;
	
	private static Map<String, String> callerType = new HashMap<>();

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
			SMSInstance instance = new SMSInstance(modem);
			try 
			{
				instance.connect(port);
				SMSUtil.smsInstance.add(instance);
			} 
			catch (GSMException e) 
			{
				logger.error(e.getMessage());
			}			
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
			}
		}
		SMSUtil.updateConnectedDevice();
		
	}
	
	public static List<SMS> readSMS(String modemID) throws GSMException
	{
		return SMSUtil.get(modemID).readSMS();
	}
	
	public static JSONArray readSMSJSON(String modemID) throws GSMException
	{
		JSONArray arr = new JSONArray();
		List<SMS> sms = SMSUtil.get(modemID).readSMS();
		for(int i = 0; i<sms.size(); i++)
		{
			arr.put(sms.get(i).toJSONObject());
		}
		return arr;
	}
	
	public static JSONObject sendSMS(String receiver, String message, String modemID) throws GSMException {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		if(SMSUtil.smsInstance.isEmpty())
		{
			SMSUtil.sendTraffic(receiver, ste);
			throw new GSMException(SMSUtil.NO_DEVICE_CONNECTED);
		}		
		
		/**
		 * Begin
		 */
		DataModem modemData = ConfigModem.getModemData(modemID);
		SMSUtil.sendTraffic(receiver, ste, modemData);
        /**
         * End
         */
		
		String result = SMSUtil.get(modemID).sendSMS(receiver, message);	
		
		JSONObject jo = new JSONObject();
		jo.put("modemID", modemData.getId());
		jo.put("result", result);
		return jo;
	}
	
	public static JSONObject sendSMS(String receiver, String message) throws GSMException {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];      
		if(SMSUtil.smsInstance.isEmpty())
		{
			SMSUtil.sendTraffic(receiver, ste);
			throw new GSMException(SMSUtil.NO_DEVICE_CONNECTED);
		}
		int index = SMSUtil.getModemIndex();	
		
		SMSInstance instance = SMSUtil.smsInstance.get(index);

		
		String result = instance.sendSMS(receiver, message);

		/**
		 * Begin
		 */
		DataModem modemData = ConfigModem.getModemData(instance.getId());        
		SMSUtil.sendTraffic(receiver, ste, modemData);
        /**
         * End
         */
		JSONObject jo = new JSONObject();
		jo.put("modemID", modemData.getId());
		jo.put("result", result);
		return jo;
		
	}
	
	private static void sendTraffic(String receiver, StackTraceElement ste)
	{
		String callerClass = ste.getClassName();
        JSONObject monitor = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("senderType", SMSUtil.getSenderType(callerClass));
        data.put("receiver", receiver);
               
        monitor.put(JsonKey.COMMAND, "sms-traffic");
        monitor.put(JsonKey.DATA, data);      
        ServerWebSocketManager.broadcast(monitor.toString(), "", "monitor.html");
	}
	
	private static void sendTraffic(String receiver, StackTraceElement ste, DataModem modemData)
	{
		String modemID = modemData.getId();
		String modemName = modemData.getName();
		String modemIMEI = modemData.getImei();

		String callerClass = ste.getClassName();
        JSONObject monitor = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("modemID", modemID);
        data.put("modemName", modemName);
        data.put("modemIMEI", modemIMEI);
        data.put("senderType", SMSUtil.getSenderType(callerClass));
        data.put("receiver", receiver);
        monitor.put(JsonKey.COMMAND, "sms-traffic");
        monitor.put(JsonKey.DATA, data);      
        ServerWebSocketManager.broadcast(monitor.toString(), "", "monitor.html");
	}
	
	private static String getSenderType(String callerClass) {
		String key = Utility.getClassName(callerClass);
		return SMSUtil.getCallerType().getOrDefault(key, "");
	}

	public static String executeUSSD(String ussd, String modemID) throws GSMException {
		if(SMSUtil.smsInstance.isEmpty())
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
		for(int i = 0; i < SMSUtil.smsInstance.size(); i++)
		{
			if(SMSUtil.smsInstance.get(i).isConnected())
			{
				connected++;
			}
		}
		return connected;
	}
	
	public static boolean isConnected(String modemID)
	{
		for(int i = 0; i < SMSUtil.smsInstance.size(); i++)
		{
			if(SMSUtil.smsInstance.get(i).getId().equals(modemID) && SMSUtil.smsInstance.get(i).isConnected())
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
	
	public static void updateConnectedDevice() {
		SMSUtil.updateConnection();
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
		if(SMSUtil.cnt >= SMSUtil.countConnected())
		{
			SMSUtil.cnt = 0;
		}
		if(!SMSUtil.connectedDevices.isEmpty() && SMSUtil.connectedDevices.size() >= (SMSUtil.cnt -1))
		{
			return SMSUtil.connectedDevices.get(SMSUtil.cnt);
		}
		else
		{
			throw new GSMException(SMSUtil.NO_DEVICE_CONNECTED);
		}
	}

	public static String getModemName(String modemID) {
		DataModem modemData = ConfigModem.getModemData(modemID);
		return modemData.getName();
	}

	public static Map<String, String> getCallerType() {
		return callerType;
	}

	public static void setCallerType(Map<String, String> callerType) {
		SMSUtil.callerType = callerType;
	}

	public static void updateConnection() {
		for(int i = 0; i < SMSUtil.smsInstance.size(); i++)
		{
			if(SMSUtil.smsInstance.get(i).isConnected() && !ConfigModem.getModemData(SMSUtil.smsInstance.get(i).getId()).isActive())
			{
				try 
				{
					SMSUtil.smsInstance.get(i).disconnect();
				} 
				catch (GSMException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}		
	}
}





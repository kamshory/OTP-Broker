package com.planetbiru.gsm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jcraft.jsch.JSchException;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.DataModem;
import com.planetbiru.util.CommandLineExecutor;
import com.planetbiru.util.FileConfigUtil;

public class DialUtil {
	
	private static Map<String, Boolean> internetAccess = new HashMap<>();
	private static String configPath = "";
	private static String wvdialCommand = "";
	
	private DialUtil()
	{
		
	}
	

	public static void init(String path, String wvdialCommand) {
		DialUtil.configPath = path;
		DialUtil.wvdialCommand = wvdialCommand;
		for (Map.Entry<String, DataModem> entry : ConfigModem.getModemData().entrySet())
		{
			String modemID = entry.getKey();
			boolean connected = true;
			connected = connect(modemID);
			DialUtil.internetAccess.put(modemID, connected);
			if(connected)
			{
				break;
			}
		}
	}
	
	public static boolean connect(String modemID)
	{
		DataModem modemData = ConfigModem.getModemData(modemID);
		try 
		{
			GSMUtil.disconnect(modemID);
		} 
		catch (GSMException e) 
		{
			e.printStackTrace();
		}
		try 
		{
			DialUtil.apply(modemData);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		try {
			CommandLineExecutor.execSSH(wvdialCommand, 500);
			return true;
		} catch (JSchException e) {
			e.printStackTrace();
		}
		return true;
	}

	public static boolean isConnected(String modemID) {
		if(DialUtil.internetAccess.isEmpty())
		{
			return false;
		}
		if(DialUtil.internetAccess.containsKey(modemID))
		{
			return DialUtil.internetAccess.get(modemID).booleanValue();
		}
		return false;
	}

	private static void apply(DataModem modemData) throws IOException {
		/**
		 * [Dialer Defaults]
			Modem = /dev/ttyS2
			Baud = 57600
			Init = ATZ
			Init2 = AT S11=50
			Phone = 555-4242
			Username = apenwarr
			Password = my-password
			
			[Dialer phone2]
			Phone = 555-4243
			
			[Dialer shh]
			Init3 = ATM0
			
			[Dialer pulse]
			Dial Command = ATDP
		 */
		
		String configStr = ""
				+ "[Dialer Defaults]\r\n"
				+ "Modem = "+modemData.getPort()+"\r\n"
				+ "Baud = "+modemData.getBaudRate()+"\r\n"
				+ "Init = "+modemData.getInitDial1()+"\r\n"
				+ "Init2 = "+modemData.getInitDial2()+"\r\n"
				+ "Phone = "+modemData.getMsisdn()+"\r\n"
				+ "Username = "+modemData.getApnUsername()+"\r\n"
				+ "Password = "+modemData.getApnPassword()+"\r\n"
				+ "\r\n"
				+ "[Dialer phone2]\r\n"
				+ "Phone = "+modemData.getMsisdn()+"\r\n"
				+ "\r\n"
				+ "[Dialer shh]\r\n"
				+ "Init3 = "+modemData.getInitDial3()+"\r\n"
				+ "\r\n"
				+ "[Dialer pulse]\r\n"
				+ "Dial Command = "+modemData.getDialCommand()+"";
		
		String fileName = FileConfigUtil.fixFileName(DialUtil.configPath);
		FileConfigUtil.write(fileName, configStr.getBytes());
	}



}

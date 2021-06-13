package com.planetbiru.config;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileNotFoundException;

public class ConfigEmail {
	
	private ConfigEmail()
	{
		
	}

	private static String mailSenderAddress = "user@domain.com";
	private static String mailSenderPassword;
	private static boolean mailAuth = true;	
	private static boolean mailStartTLS = true;
	private static boolean mailSSL = false;
	private static String mailHost = "smtp.gmail.com";
	private static int mailPort = 587;
	public static String getMailSenderAddress() {
		return mailSenderAddress;
	}
	public static void setMailSenderAddress(String mailSenderAddress) {
		ConfigEmail.mailSenderAddress = mailSenderAddress;
	}
	public static String getMailSenderPassword() {
		return mailSenderPassword;
	}
	public static void setMailSenderPassword(String mailSenderPassword) {
		ConfigEmail.mailSenderPassword = mailSenderPassword;
	}
	public static boolean getMailAuth() {
		return mailAuth;
	}
	public static void setMailAuth(boolean mailAuth) {
		ConfigEmail.mailAuth = mailAuth;
	}
	public static boolean isMailStartTLS() {
		return mailStartTLS;
	}
	public static void setMailStartTLS(boolean mailStartTLS) {
		ConfigEmail.mailStartTLS = mailStartTLS;
	}
	public static boolean isMailSSL() {
		return mailSSL;
	}
	public static void setMailSSL(boolean mailSSL) {
		ConfigEmail.mailSSL = mailSSL;
	}
	public static String getMailHost() {
		return mailHost;
	}
	public static void setMailHost(String mailHost) {
		ConfigEmail.mailHost = mailHost;
	}
	public static int getMailPort() {
		return mailPort;
	}
	public static void setMailPort(int mailPort) {
		ConfigEmail.mailPort = mailPort;
	}
	
	
	public static void load(String path) {
		String dir = getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = dir + path;
		
		try 
		{
			byte[] data = FileConfigUtil.read(fileName);
			
			if(data != null)
			{
				String text = new String(data);
				if(text.length() > 7)
				{
					try
					{
						JSONObject json = new JSONObject(text);
						String lMailSenderAddress = json.optString("mailSenderAddress", "");
						String lMailSenderPassword = json.optString("mailSenderPassword", "");
						boolean lMailAuth = json.optBoolean("mailAuth", false);
						boolean lMailStartTLS  = json.optBoolean("mailStartTLS", false);
						boolean lMailSSL = json.optBoolean("mailSSL", false);
						String lMailHost = json.optString("mailHost", "");
						int lMailPort = json.optInt("mailPort", 0);
						
						ConfigEmail.mailSenderAddress = lMailSenderAddress;
						ConfigEmail.mailSenderPassword = lMailSenderPassword;
						ConfigEmail.mailAuth = lMailAuth;
						ConfigEmail.mailStartTLS = lMailStartTLS;
						ConfigEmail.mailSSL = lMailSSL;
						ConfigEmail.mailHost = lMailHost;
						ConfigEmail.mailPort = lMailPort;
					}
					catch(JSONException e)
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		
	}	

	public static void save(String path) {
		JSONObject config = getJSONObject();
		save(path, config);
	}

	public static void save(String path, JSONObject config) {
		
		String dir = getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = dir + path;
		prepareDir(fileName);
		
		try 
		{
			FileConfigUtil.write(fileName, config.toString().getBytes());
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		
		
	}
	
	private static void prepareDir(String fileName) {
		File file = new File(fileName);
		String directory1 = file.getParent();
		File file2 = new File(directory1);
		String directory2 = file2.getParent();
		
		File d1 = new File(directory1);
		File d2 = new File(directory2);		

		if(!d2.exists())
		{
			d2.mkdir();
		}
		if(!d1.exists())
		{
			d1.mkdir();
		}		
	}
	
	private static String getBaseDir()
	{
		return ConfigEmail.class.getResource("/").getFile();
	}
	
	public static JSONObject getJSONObject() {
		JSONObject config = new JSONObject();

		config.put("mailAuth", ConfigEmail.mailAuth);
		config.put("mailHost", ConfigEmail.mailHost);
		config.put("mailPort", ConfigEmail.mailPort);
		config.put("mailSenderAddress", ConfigEmail.mailSenderAddress);
		config.put("mailSenderPassword", ConfigEmail.mailSenderPassword);
		config.put("mailSSL", ConfigEmail.mailSSL);
		config.put("mailStartTLS", ConfigEmail.mailStartTLS);
		return config;
	}
	
	
}

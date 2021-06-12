package com.planetbiru.gsm;

public class SMSUtil {
	private SMSUtil()
	{
		/**
		 * Do nothing
		 */
	}
	private static SMSInstance smsInstance = null;
	public static void init(String port)
	{
		smsInstance = new SMSInstance();
		smsInstance.init(port);
	}
	public static void sendSMS(String receiver, String message) throws GSMNullException {
		smsInstance.sendSMS(receiver, message);
		
	}
	public static String executeUSSD(String ussd) throws GSMNullException {
		return smsInstance.executeUSSD(ussd);
	}
}

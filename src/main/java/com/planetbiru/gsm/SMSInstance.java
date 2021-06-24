package com.planetbiru.gsm;

import java.util.List;

import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.planetbiru.config.Config;
import com.planetbiru.constant.ConstantString;

public class SMSInstance {
	private GSM gsm;
	private boolean connected = false;
	private String id = "";
	public SMSInstance()
	{
		/**
		 * Constructor
		 */
		this.gsm = new GSM();
	}
	public boolean connect(String port) throws GSMException 
	{
		try
		{
			return this.gsm.connect(port);
		}
		catch(SerialPortInvalidPortException e)
		{
			throw new GSMException(e);
		}
	}
	public void disconnect() throws GSMException {
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMException(ConstantString.SERIAL_PORT_NULL);
		}
		this.gsm.closePort();
	}
	public String sendSMS(String receiver, String message) throws GSMException
	{
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMException(ConstantString.SERIAL_PORT_NULL);
		}
		this.waitUntilReady();
		return this.gsm.sendSMS(receiver, message);
	}
	
	private void waitUntilReady() {
		long maxWaith = Config.getMaxWaitModemReady();
		long ellapsed = 0;
		long startTime = System.currentTimeMillis();
		while(!this.gsm.isReady() && ellapsed < maxWaith)
		{
			this.sleep(Config.getWaithModemReady());
			ellapsed = System.currentTimeMillis() - startTime;
		}
	}
	
	public List<SMS> readSMS() throws GSMException
	{
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMException(ConstantString.SERIAL_PORT_NULL);
		}
		this.waitUntilReady();
		return this.gsm.readSMS();
	}
	
	public String executeUSSD(String ussd) throws GSMException {
		if(this.gsm.getSerialPort() == null)
		{
			throw new GSMException(ConstantString.SERIAL_PORT_NULL);
		}
		this.waitUntilReady();
		return this.gsm.executeUSSD(ussd);	
	}
	
	public boolean isClosed()
	{
		return this.gsm.isClosed();
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	private void sleep(long sleep) 
	{
		try 
		{
			Thread.sleep(sleep);
		} 
		catch (InterruptedException e) 
		{
			Thread.currentThread().interrupt();
		}		
	}

}

package com.planetbiru;

import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.comm.CommPortIdentifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

@Service
public class PortUtils {
	private Logger logger = LogManager.getLogger(PortUtils.class);
	private static SerialPort serialPort;
	
	private PortUtils()
	{
		
	}
	@PostConstruct
	private void init()
	{
		for(int i = 0; i<100; i++)
		{
			PortUtils.serialPort = new SerialPort("COM"+i); 
		    try {
		        serialPort.openPort();//Open port
		        serialPort.setParams(9600, 8, 1, 0);//Set params
		        int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
		        serialPort.setEventsMask(mask);//Set mask
		        serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
		        
		        logger.error("SUKSES COM{}", i);
		    }
		    catch (SerialPortException ex) {
		        logger.error(ex);
		    }
		}
	}
	
	private void listSerial() {
		 String[] portNames = SerialPortList.getPortNames();
		 logger.info("portNames = {}", portNames.length);
		    for(int i = 0; i < portNames.length; i++)
		    {
		        System.out.println(portNames[i]);
		        logger.info("Serial Port : {}", portNames[i]);
		    }
		
	}
	protected void list() {
		// get list of ports available on this particular computer,
		// by calling static method in CommPortIdentifier.
		Enumeration pList = CommPortIdentifier.getPortIdentifiers();
		logger.info("Content : {}", pList.toString());
		

		// Process the list.
		if(pList.hasMoreElements())
		{
			while (pList.hasMoreElements()) 
			{
				CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
				logger.info("CommPortIdentifier : {}", cpi.getName());
				System.out.print("Port " + cpi.getName() + " ");
				logger.info("pList : {}", cpi.getName());
				
				if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) 
				{
					System.out.println("is a Serial Port: " + cpi);
				} 
				else if (cpi.getPortType() == CommPortIdentifier.PORT_PARALLEL) 
				{
					System.out.println("is a Parallel Port: " + cpi);
				} 
				else 
				{
				System.out.println("is an Unknown Port: " + cpi);
				}
			}
		}
		else
		{
			logger.info("No device connected");
		}
	}
	
	/*
	 * In this class must implement the method serialEvent, through it we learn about 
	 * events that happened to our port. But we will not report on all events but only 
	 * those that we put in the mask. In this case the arrival of the data and change the 
	 * status lines CTS and DSR
	 */
	static class SerialPortReader implements SerialPortEventListener {

	    public void serialEvent(SerialPortEvent event) {
	        if(event.isRXCHAR()){//If data is available
	            if(event.getEventValue() == 10){//Check bytes count in the input buffer
	                //Read data, if 10 bytes available 
	                try {
	                    byte buffer[] = serialPort.readBytes(10);
	                }
	                catch (SerialPortException ex) {
	                    System.out.println(ex);
	                }
	            }
	        }
	        else if(event.isCTS()){//If CTS line has changed state
	            if(event.getEventValue() == 1){//If line is ON
	                System.out.println("CTS - ON");
	            }
	            else {
	                System.out.println("CTS - OFF");
	            }
	        }
	        else if(event.isDSR()){///If DSR line has changed state
	            if(event.getEventValue() == 1){//If line is ON
	                System.out.println("DSR - ON");
	            }
	            else {
	                System.out.println("DSR - OFF");
	            }
	        }
	    }
	}

}

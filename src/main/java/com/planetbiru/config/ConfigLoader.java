package com.planetbiru.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import com.planetbiru.util.FileNotFoundException;

public class ConfigLoader {
	public static byte[] read(String fileName) throws FileNotFoundException
	{
		byte[] allBytes = null;
		try 
		(
				InputStream inputStream = new FileInputStream(fileName);
		) 
		{
			File resource = new File(fileName);		
			long fileSize = resource.length();
			allBytes = new byte[(int) fileSize];
			int length = inputStream.read(allBytes);
			if(length == 0)
			{
				allBytes = null;
			}
		 } 
		 catch (IOException ex) 
		 {
			 throw new FileNotFoundException(ex);
		 }
		 return allBytes;
	 }
	public static void write(String fileName, byte[] data) throws IOException
	{
		try 
		(
			OutputStream os = new FileOutputStream(fileName);
		)
		{
	        final PrintStream printStream = new PrintStream(os);
	        printStream.write(data);
	        printStream.close();
		}
		 catch (IOException ex) 
		 {
			 throw new IOException(ex);
		 }
	}
}

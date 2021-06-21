package com.planetbiru.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import com.planetbiru.util.OSUtil.OS;

public class FileConfigUtil {
	private FileConfigUtil()
	{
		
	}
	
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
	public static String fixFileName(String fileName) {
		if(OSUtil.getOS().equals(OS.WINDOWS))
		{
			fileName = fileName.replace("/", "\\");
			fileName = fileName.replace("\\\\", "\\");
		}
		else
		{
			fileName = fileName.replace("\\", "/");		
			fileName = fileName.replace("//", "/");
		}
		return fileName;
	}
	
	public static String getFileExtension(String fileName) 
	{
		String extension = fileName;
		int index = fileName.lastIndexOf('.');
		if (index > 0) {
		      extension = fileName.substring(index + 1);
		}
		return extension;
	}
}

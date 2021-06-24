package com.planetbiru.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.json.JSONArray;
import org.json.JSONObject;

public class FileUtil {
	private FileUtil()
	{
		
	}
	
	public static byte[] readResource(String fileName) throws FileNotFoundException
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
	
	public static JSONArray listFile(File directory)
    {
		JSONArray files = new JSONArray();
	    File[] list = directory.listFiles();
        if(list != null)
        {
	        for(File file : list)
	        {
	            if(file.isDirectory())
	            {
	            	JSONObject obj = new JSONObject();
	            	JSONArray list2 = listFile(file);
	            	obj.put("name", file.getName());
	            	obj.put("type", "dir");
	            	obj.put("child", list2);
	            	JSONArray ja = new JSONArray();
	            	ja.put(obj);
	            	files.put(obj);
	            	
	            }
	            else 
	            {
	            	JSONObject obj = new JSONObject();
	            	obj.put("type", "file");
	            	obj.put("name", file.getName());
	            	obj.put("size", file.length());
	            	files.put(obj);
	            }
	        }
        }
        return files;
    }
}

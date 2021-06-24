package com.planetbiru.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.Utility;

public class GeneralConfig {
	private Pattern mSection = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
	private Pattern mKeyValue = Pattern.compile("\\s*([^=]*)=(.*)");
	private Map <String, Map<String, String>> mEntries = new HashMap<>();

	public GeneralConfig(String path) throws IOException {
		
		String dir = Utility.getBaseDir();
		if(dir.endsWith("/") && path.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		String fileName = FileConfigUtil.fixFileName(dir + path);
		
		try 
		{
			byte[] data = FileUtil.readResource(fileName);
			if(data != null)
			{
				String text = new String(data);
				text = fixingRawData(text);
				String[] lines = text.split("\\r?\\n");
				List<String> list = Arrays.asList(lines);
				this.load(list);
				
			}
		} 
		catch (FileNotFoundException | JSONException e) 
		{
			e.printStackTrace();
			/**
			 * Do nothing
			 */
		}
		
	   
	}
	
	public static String fixingRawData(String result)
	{
		result = result.replace("\n", "\r\n");
		result = result.replace("\r\r\n", "\r\n");
		result = result.replace("\r", "\r\n");
		result = result.replace("\r\n\n", "\r\n");
		return result;
	}

	public GeneralConfig() {
		/**
		 * Do nothing
		 */
	}
	
	public void load(List<String> lines) {
        String section = null;
		for(int i = 0; i<lines.size(); i++)
		{
			String line = lines.get(i);
            Matcher m = mSection.matcher(line);
            if(m.matches()) 
            {
                section = m.group(1).trim();
            } 
            else if (section != null) 
            {
            	System.out.println(section);
                m = mKeyValue.matcher(line);
                if(m.matches()) 
                {
                    String key = m.group(1).trim();
                    String value = m.group(2).trim();
                    if(mEntries != null && mEntries.containsKey(section))
                    {
                    	System.out.println("MATCH ===========");
	                    Map<String, String> kv = mEntries.get(section);
	                    if (kv == null) 
	                    {
	                    	kv = new HashMap<>();
	                        mEntries.put(section, kv);
	                    }
	                    kv.put(key, value);
                    }
                }
                else
                {
                	System.out.println("NOT MATCH");
                }
            }
		}
	}

	public void load(String fileName) throws IOException {
		InputStream resourceStream = GeneralConfig.class.getResourceAsStream(fileName);
		if(resourceStream != null)
		{
		    try (
		    		BufferedReader br = new BufferedReader(new InputStreamReader(resourceStream))
		    	) 
		    {
		        String line;
		        String section = null;
		        while ((line = br.readLine()) != null) 
		        {
		            Matcher m = mSection.matcher(line);
		            if(m.matches()) 
		            {
		                section = m.group(1).trim();
		            } 
		            else if (section != null) 
		            {
		                m = mKeyValue.matcher(line);
		                if(m.matches()) 
		                {
		                    String key = m.group(1).trim();
		                    String value = m.group(2).trim();
		                    if(mEntries != null && mEntries.containsKey(section))
		                    {
			                    Map<String, String> kv = mEntries.get(section);
			                    if (kv == null) 
			                    {
			                    	kv = new HashMap<>();
			                        mEntries.put(section, kv);
			                    }
			                    kv.put(key, value);
		                    }
		                }
		            }
		        }
		    }
		}
	}

	public String getString(String section, String key, String defaultvalue) {
	    Map<String, String> kv = mEntries.get(section);
	    if (kv == null) {
	        return defaultvalue;
	    }
	    return kv.get(key);
	}

	public int getInt(String section, String key, int defaultvalue) {
	    Map<String, String> kv = mEntries.get(section);
	    if (kv == null) {
	        return defaultvalue;
	    }
	    return Integer.parseInt(kv.get(key));
	}

	public float getFloat(String section, String key, float defaultvalue) {
	    Map<String, String> kv = mEntries.get(section);
	    if (kv == null) {
	        return defaultvalue;
	    }
	    return Float.parseFloat(kv.get(key));
	}

	public double getDouble(String section, String key, double defaultvalue) {
	    Map<String, String> kv = mEntries.get(section);
	    if (kv == null) {
	        return defaultvalue;
	    }
	    return Double.parseDouble(kv.get(key));
	}
}

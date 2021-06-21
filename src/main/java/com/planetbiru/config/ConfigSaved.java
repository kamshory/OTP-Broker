package com.planetbiru.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigSaved {
	private static Logger logger = LogManager.getLogger(ConfigSaved.class);
	
	private Pattern mSection = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
	private Pattern mKeyValue = Pattern.compile("\\s*([^=]*)=(.*)");
	private Map <String, Map<String, String>> mEntries = new HashMap<>();

	public ConfigSaved(String path) throws IOException {
	    load(path);
	}

	public ConfigSaved() {
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
                m = mKeyValue.matcher(line);
                if(m.matches()) 
                {
                    String key = m.group(1).trim();
                    String value = m.group(2).trim();
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

	public void load(String fileName) throws IOException {
		InputStream resourceStream = ConfigSaved.class.getResourceAsStream(fileName);
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

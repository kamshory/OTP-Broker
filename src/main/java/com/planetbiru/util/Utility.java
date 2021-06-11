package com.planetbiru.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.core.codec.EncodingException;
import org.springframework.http.HttpHeaders;

import com.planetbiru.constant.ConstantString;

public class Utility {

	private static Logger logger = LogManager.getLogger(Utility.class);
	private Utility()
	{
		
	}
	/**
	 * Get current time with specified format
	 * @return Current time with format yyyy-MM-dd
	 */
	public static String now()
	{
		String result = "";
		try
		{
			DateFormat dateFormat = new SimpleDateFormat(ConstantString.SQL_FULL_FORMAT);
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Get current time with specified format
	 * @param precission Decimal precision
	 * @return Current time with format yyyy-MM-dd
	 */
	public static String now(int precission)
	{
		if(precission > 6)
		{
			precission = 6;
		}
		if(precission < 0)
		{
			precission = 0;
		}
		long decimal = 0;
		long nanoSecond = System.nanoTime();
		if(precission == 6)
		{
			decimal = nanoSecond % 1000000;
		}
		else if(precission == 5)
		{
			decimal = nanoSecond % 100000;
		}
		else if(precission == 4)
		{
			decimal = nanoSecond % 10000;
		}
		else if(precission == 3)
		{
			decimal = nanoSecond % 1000;
		}
		else if(precission == 2)
		{
			decimal = nanoSecond % 100;
		}
		else if(precission == 1)
		{
			decimal = nanoSecond % 10;
		}
		String result = "";
		try
		{
			DateFormat dateFormat = new SimpleDateFormat(ConstantString.SQL_FULL_FORMAT);
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject)+"."+decimal;
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Current time with precision and time zone
	 * @param precission Precision
	 * @param timezone Time zone
	 * @return MySQL format date time with time zone
	 */
	public static String now(int precission, String timezone)
	{
		if(precission > 6)
		{
			precission = 6;
		}
		if(precission < 0)
		{
			precission = 0;
		}
		long decimal = 0;
		long nanoSecond = System.nanoTime();
		if(precission == 6)
		{
			decimal = nanoSecond % 1000000;
		}
		else if(precission == 5)
		{
			decimal = nanoSecond % 100000;
		}
		else if(precission == 4)
		{
			decimal = nanoSecond % 10000;
		}
		else if(precission == 3)
		{
			decimal = nanoSecond % 1000;
		}
		else if(precission == 2)
		{
			decimal = nanoSecond % 100;
		}
		else if(precission == 1)
		{
			decimal = nanoSecond % 10;
		}
		String result = "";
		try
		{
			DateFormat dateFormat = new SimpleDateFormat(ConstantString.SQL_FULL_FORMAT);
			dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject)+"."+decimal;
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Current time with mili second precision
	 * @return MySQL format date time with mili second precision
	 */
	public static String now3()
	{
		String result = "";
		try
		{
			long miliSecond = System.nanoTime() % 1000;
			DateFormat dateFormat = new SimpleDateFormat(ConstantString.SQL_FULL_FORMAT);
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject)+"."+miliSecond;
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Current time with micro second precision
	 * @return MySQL format date time with micro second precision
	 */
	public static String now6()
	{
		String result = "";
		try
		{
			long microSecond = System.nanoTime() % 1000000;
			DateFormat dateFormat = new SimpleDateFormat(ConstantString.SQL_FULL_FORMAT);
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject)+"."+microSecond;
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Get current time with specified format
	 * @param format Time format
	 * @return Current time with specified format
	 */
	public static String now(String format)
	{
		String result = "";
		try
		{
			DateFormat dateFormat = new SimpleDateFormat(format);
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Get current time with specified format and time zone
	 * @param format Time format
	 * @param timezone Time zone
	 * @return Current time with specified format and time zone
	 */
	public static String now(String format, String timezone)
	{
		String result = "";
		try
		{
			DateFormat dateFormat = new SimpleDateFormat(format);
			dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
		    Date dateObject = new Date();
		    result = dateFormat.format(dateObject);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Get current time with MMddHHmmss format
	 * @return Current time with MMddHHmmss format
	 */
	public static String date10()
	{
		return now("MMddHHmmss");
	}
	/**
	 * ISO 8583 standard date time with time zone
	 * @param timezone Time zone
	 * @return ISO 8583 standard date time with time zone
	 */
	public static String date10(String timezone)
	{
		return now("MMddHHmmss", timezone);
	}
	/**
	 * Get current time with MMdd format
	 * @return Current time with MMdd format
	 */
	public static String date4()
	{
		return now("MMdd");
	}
	/**
	 * Get current time with HHmmss format
	 * @return Current time with HHmmss format
	 */
	public static String time6()
	{
		return now("HHmmss");
	}
	/**
	 * Get current time with HHmm format
	 * @return Current time with HHmm format
	 */
	public static String time4()
	{
		return now("HHmm");
	}
	/**
	 * Date time
	 * @param format Date time format
	 * @return String contains current date time
	 */
	public static String date(String format)
	{
		String result = "";
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			Date dateObject = new Date();
			result = dateFormat.format(dateObject);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Date time
	 * @param format Date time format
	 * @param date Date time
	 * @return String contains current date time
	 */
	public static String date(String format, Date date)
	{
		String result = "";
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			result = dateFormat.format(date);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Date time
	 * @param format Date time format
	 * @param date Date time
	 * @return String contains current date time
	 */
	public static String date(String format, Date date, String timeZone)
	{
		String result = "";
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
			result = dateFormat.format(date);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Date time
	 * @param format Date time format
	 * @param time Unix Timestamp
	 * @return String contains current date time
	 */
	public static String date(String format, long time)
	{
		String result = "";
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			Date dateObject = new Date(time);
			result = dateFormat.format(dateObject);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		return result;
	}
	/**
	 * Date yesterday
	 * @return Date yesterday
	 */
	public static Date yesterday() 
	{
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);
	    return cal.getTime();
	}
	/**
	 * Date before yesterday
	 * @return Date before yesterday
	 */
	public static Date beforeYesterday() 
	{
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -2);
	    return cal.getTime();
	}
	/**
	 * Date tomorrow
	 * @return Date tomorrow
	 */
	public static Date tomorrow()
	{
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, +1);
	    return cal.getTime();		
	}
	public static Date dateBefore(String dateTime, String format, int nDay) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
	    Date date = sdf.parse(dateTime);
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    calendar.add(Calendar.DATE, -nDay);
	    return calendar.getTime();
	}
	/**
	 * Date after tomorrow
	 * @return Date after tomorrow
	 */
	public static Date afterTomorrow()
	{
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, +2);
	    return cal.getTime();		
	}
	/**
	/**
	 * Convert array byte to string contains hexadecimal number
	 * @param b array byte
	 * @return String contains hexadecimal number
	 */
	public static String byteArrayToHexString(byte[] b) 
	{
		String result = "";
		StringBuilder str = new StringBuilder(); 
		for (int i=0; i < b.length; i++) 
		{
			str.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		}
		result = str.toString();
		return result;
	}
	/**
	 * Escape JSON
	 * @param input Input string
	 * @return Escaped string
	 */
	public static String escapeJSON(String input) 
	{
		String output = "";
		if(input != null)
		{
			output = input.replace("\"", "\\\"");
			output = output.replace("/", "\\/");
		}
		return output;
	}
	/**
	 * Strip characters from the beginning of a string
	 * @param input String to be stripped
	 * @param mask Character mask to strip string
	 * @return Stripped string
	 */
	public static String lTrim(String input, String mask)
	{
		int lastLen;
		int curLen;
		do
		{
			lastLen = input.length();
			input = input.replaceAll("^"+mask, "");
			curLen = input.length();
		}
		while (curLen < lastLen);
		return input;
	}
	/**
	 * Strip characters from the end of a string
	 * @param input String to be stripped
	 * @param mask Character mask to strip string
	 * @return Stripped string
	 */
	public static String rTrim(String input, String mask)
	{
		int lastLen;
		int curLen;
		do
		{
			lastLen = input.length();
			input = input.replaceAll(mask+"$", "");
			curLen = input.length();
		}
		while (curLen < lastLen);
		return input;
	}
	/**
	 * Get N right string
	 * @param input Input string
	 * @param length Expected length
	 * @return N right string
	 */
	public static String right(String input, int length)
	{
		if(length >= input.length())
		{
			return input;
		}
		else
		{
			return input.substring(input.length() - length, input.length());
		}
	}
	/**
	 * Get N left string
	 * @param input Input string
	 * @param length Expected length
	 * @return N left string
	 */
	public static String left(String input, int length)
	{
		if(length >= input.length())
		{
			return input;
		}
		else
		{
			return input.substring(0, length);
		}
	}
	/**
	 * Padding on left side
	 * @param input Input string
	 * @param length Desired length
	 * @param car Character to pad
	 * @return Padded string
	 */
	public static String lPad(String input, int length, char car) 
	{
		if(input.length() > length)
		{
			return input;
		}
		String fmt = "%" + length + "s";
		return (input + String.format(fmt, "").replace(" ", String.valueOf(car))).substring(0, length);
	}

	/**
	 * Padding on right side
	 * @param input Input string
	 * @param length Desired length
	 * @param car Character to pad
	 * @return Padded string
	 */
	public static String rPad(String input, int length, char car) 
	{
		if(input.length() > length)
		{
			return input;
		}
		String fmt = "%" + length + "s";
		return (String.format(fmt, "").replace(" ", String.valueOf(car)) + input).substring(input.length(), length + input.length());
	}
	/**
	 * Encode URL
	 * @param input Clear URL
	 * @return Decoded URL
	 */
	public static String urlEncode(String input) 
	{
	   	String result = "";
		try 
		{
			result = java.net.URLEncoder.encode(input, ConstantString.UTF8);
		} 
		catch (UnsupportedEncodingException e) 
		{
			logger.error(e.getMessage());
		}
    	return result;
	}
	/**
	 * Decode URL
	 * @param input Decoded URL
	 * @return Clear URL
	 */
	public static String urlDecode(String input)
    {
    	String result = "";
		try 
		{
			result = java.net.URLDecoder.decode(input, ConstantString.UTF8);
		} 
		catch (UnsupportedEncodingException e) 
		{
			logger.error(e.getMessage());
		}
    	return result;
    }
	/**
	 * Parse query string into map
	 * @param query Query string
	 * @return Map contains query parsed
	 * @throws UnsupportedEncodingException if character encoding is not supported
	 */
	public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException 
	{
	    Map<String, String> queryPairs = new LinkedHashMap<>();
	    String[] pairs = query.split("&");
	    for (String pair : pairs) 
	    {
	        int idx = pair.indexOf('=');
	        queryPairs.put(URLDecoder.decode(pair.substring(0, idx), ConstantString.UTF8), URLDecoder.decode(pair.substring(idx + 1), ConstantString.UTF8));
	    }
	    return queryPairs;
	}
	/**
	 * Parse query string into JSON object
	 * @param query Query string
	 * @return JSONObject contains query parsed
	 * @throws JSONException if any JSON errors
	 */
    public static JSONObject parseQuery(String query)
    {
    	JSONObject json = new JSONObject();
    	if(query == null)
    	{
    		query = "";
    	}
    	if(query.length() > 0)
    	{
	    	String[] args;
	    	int i;
	    	String arg = "";
	    	String[] arr;
	    	String key = "";
	    	String value = "";
    		if(query.contains("&"))
    		{
    			args = query.split("&");
    		}
    		else
    		{
    			args = new String[1];
    			args[0] = query;
    		}
    		for(i = 0; i<args.length; i++)
    		{
    			arg = args[i];
    			if(arg.contains("="))
    			{
    				arr = arg.split("=", 2);
    				key = arr[0];
    				value = Utility.urlDecode(arr[1]);
    				json.put(key, value);
    			}
    		}
    	}
    	return json;
    }
    /**
     * Build query string
     * @param query JSONObject contains query information
     * @return Clear query string
     * @throws JSONException if any JSON errors
     */
    public static String buildQuery(JSONObject query)
    {
    	String result = "";
    	
    	Iterator<?> keys = query.keys();
    	String key = "";
    	String value = "";
    	int i = 0;
    	StringBuilder bld = new StringBuilder();
    	while( keys.hasNext() ) 
    	{
    	    key = (String) keys.next();
			if(query.get(key) instanceof JSONObject) 
			{
				value = query.optString(key, "");
				value = Utility.urlEncode(value);
				if(i > 0)
				{
					bld.append("&");
				}
				bld.append(key+"="+value);
			}
			i++;
    	}
    	result = bld.toString();
    	return result;
    }
    /**
     * Build query
     * @param query Map string
     * @return Query string on GET URL
     * @throws NullPointerException if any null pointer
     */
    public static String buildQuery(Map<String, String> query)
    {
    	String result = "";
    	int i = 0;
    	String key = "";
    	String value = "";
    	StringBuilder bld = new StringBuilder();
    	for(Map.Entry<String, String> entry : query.entrySet())
    	{
    	    if(i > 0)
    	    {
    	    	bld.append("&");
    	    }
    	    key = entry.getKey();
    	    value = entry.getValue();
    	    value = Utility.urlEncode(value);
    	    bld.append(key+"="+value);
    	    i++;
    	}
    	result = bld.toString();
    	return result;
   	
    }
	/**
	 * Encode byte array with base 64 encoding
	 * @param input Byte array to be encoded
	 * @return Encoded string
	 */
	public static String base64Encode(byte[] input)
	{
		byte[] encodedBytes = Base64.getEncoder().encode(input);
		return new String(encodedBytes);
	}
	/**
	 * Encode string with base 64 encoding
	 * @param input String to be encoded
	 * @return Encoded string
	 */
	public static String base64Encode(String input)
	{
		byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes());
		return new String(encodedBytes);
	}
	/**
	 * Decode string with base 64 encoding
	 * @param input String to be decoded
	 * @return Decoded string
	 */
	public static String base64Decode(String input)
	{
		byte[] decodedBytes = Base64.getDecoder().decode(input.getBytes());
		return new String(decodedBytes);
	}
	/**
	 * Decode string with base 64 encoding
	 * @param input String to be decoded
	 * @return Decoded string
	 */
	public static byte[] base64DecodeRaw(String input)
	{
		return Base64.getDecoder().decode(input.getBytes());
	}
	/**
	 * Rotate 13 of string
	 * @param input Input string
	 * @return Rotated 13
	 */
	public static String rot13(String input)
	{
		StringBuilder bld = new StringBuilder();
        for (int i = 0; i < input.length(); i++) 
        {
            char c = input.charAt(i);
            if (c >= 'a' && c <= 'm')
            {
            	c += 13;
            }
            else if(c >= 'A' && c <= 'M') 
            {
            	c += 13;
            }
            else if  (c >= 'n' && c <= 'z') 
            {
            	c -= 13;
            }
            else if  (c >= 'N' && c <= 'Z') 
            {
            	c -= 13;
            }
            bld.append(String.format("%c", c));
        }
        return bld.toString();
	}
	/**
	 * Escape XML
	 * @param input Input string 
	 * @return Escaped string
	 */
	public static String escapeXML(String input) 
	{
		return input.replace("<", "&lt;").replace(">", "&gt").replace("\"", "&quot;");
	}
	/**
	 * Escape HTML
	 * @param input Input string 
	 * @return Escaped string
	 */
	public static CharSequence escapeHTML(String input) 
	{
		return input.replace("<", "&lt;").replace(">", "&gt").replace("\"", "&quot;");
	}
	/**
	 * Convert double to big decimal value as string
	 * @param input Double value
	 * @return Big decimal value as string
	 */
	public static String doubleToBigDecimal(double input) 
	{		
		return BigDecimal.valueOf(input)+"";
	}
	/**
	 * Convert long to big decimal value as string
	 * @param input Double value
	 * @return Big decimal value as string
	 */
	public static String longToBigDecimal(long input) 
	{		
		return input+"";
	}
	/**
	 * Get object from JSON with case insensitive key
	 * @param jobj JSONObject
	 * @param key Case insensitive key
	 * @return Object
	 */
	public static Object getIgnoreCase(JSONObject jobj, String key) 
	{
	    Iterator<String> iter = jobj.keySet().iterator();
	    while(iter.hasNext()) 
	    {
	        String key1 = iter.next();
	        if(key1.equalsIgnoreCase(key)) 
	        {
	            return jobj.get(key1);
	        }
	    }
	    return null;
	}

	/**
	 * Convert header to string
	 * @param header Header
	 * @return String from header
	 */
	public static String headerToString(HttpHeaders header) 
	{
		StringBuilder result = new StringBuilder();
        int size = 0;
        int i = 0;
        int j = 0;
        String value = "";
        String key = "";
        for (Map.Entry<String, List<String>> hdr : header.entrySet()) 
        {
        	size = hdr.getValue().size();
        	for(i = 0; i < size; i++, j++)
        	{
        		if(j > 0)
        		{
        			result.append("\r\n");
        		}
           		key = hdr.getKey();
           		value = hdr.getValue().get(i);
        		if(key.length() > 0)
        		{
        			key = key.substring(0, 1).toUpperCase() + key.substring(1);
        		}
        		result.append(key+": "+value);
        	}
        }
        return result.toString();
    }
	public static String capitalFirst(String input)
	{
		String output = "";
		output = input;
		return output;
	}
	/**
	 * Convert Headers to Map
	 * @param headers Headers
	 * @return Map
	 */
	public static Map<String, String> headersToMap(HttpHeaders headers)
	{
		Map<String, String> mapHeader = new HashMap<>();
		int size = 0;
        int i = 0;
        for (Map.Entry<String, List<String>> hdr : headers.entrySet()) 
        {
        	size = hdr.getValue().size();
        	for(i = 0; i < size; i++)
        	{
        		mapHeader.put(hdr.getKey(), hdr.getValue().get(i));
        	}
        }
        return mapHeader;
	}
	/**
	 * Convert Map to Headers
	 * @param map Map
	 * @return Header
	 */
	public static HttpHeaders mapToHeaders(Map<String, String> map)
	{
		HttpHeaders headers = new HttpHeaders();
	    Iterator<Entry<String, String>> it = map.entrySet().iterator();
	    while (it.hasNext()) 
	    {
	        Entry<String, String> pair = it.next();            
	        headers.add(pair.getKey(), pair.getValue());
	    }
	    return headers;
	}
	public static String changeTimeZone(String timeString, String dateFormat, String fromTimeZone, String toTimeZone) 
	{
		String result = "";
        DateFormat sourceTime = new SimpleDateFormat(dateFormat);
        sourceTime.setTimeZone(TimeZone.getTimeZone(fromTimeZone));
        DateFormat destinationTime = new SimpleDateFormat(dateFormat);
        destinationTime.setTimeZone(TimeZone.getTimeZone(toTimeZone));
        Date dateTime;
        try 
        {
        	dateTime = sourceTime.parse(timeString);
            result = destinationTime.format(dateTime);
        } 
        catch (ParseException e) 
        {
        	logger.error(e.getMessage());
        }
        return result;
	}
	public static Date parseTime(String timeString, String dateFormat, String fromTimeZone) 
	{
		DateFormat sourceTime = new SimpleDateFormat(dateFormat);
        sourceTime.setTimeZone(TimeZone.getTimeZone(fromTimeZone));
        Date dateTime = null;
        try 
        {
        	dateTime = sourceTime.parse(timeString);
        } 
        catch (ParseException e) 
        {
        	logger.error(e.getMessage());
        }
        return dateTime;
	}
	public static String splitChunk(String text, int size) {
        List<String> parts = new ArrayList<>();

        int length = text.length();
        for (int i = 0; i < length; i += size) {
            parts.add(text.substring(i, Math.min(length, i + size)));
        }
        return String.join("\r\n", parts.toArray(new String[0]));
    }
	/**
	 * Capitalize
	 * @param input String input
	 * @return Capitalized words
	 */
	public static String capitalize(String input)
	{
		Objects.requireNonNull(input, "The input parameter may not be null.");
		input = input.replace("_", " ").replace("   ", " ").replace("  ", " ").toLowerCase();
	    final StringBuilder output = new StringBuilder(input.length());
	    boolean lastCharacterWasWhitespace = true;

	    for (final char currentCharacter : input.toCharArray()) 
	    {
	        if (lastCharacterWasWhitespace) 
	        {
	            output.append(Character.toTitleCase(currentCharacter));
	        } 
	        else 
	        {
	            output.append(currentCharacter);
	        }
	        lastCharacterWasWhitespace = Character.isWhitespace(currentCharacter);
	    }
	    return output.toString();
	}
	public static String jsonArrayLines(JSONArray jsonArray)
	{
		List<String> list = new ArrayList<>();
		for(int i = 0; i < jsonArray.length(); i++)
		{
		    list.add(jsonArray.getString(i));
		}
		return String.join("\r\n", list);
	}
 	public static String jsonToXml(JSONObject jsonObject)
	{
		String xml = "";
		try 
		{
			xml = XML.toString(jsonObject);
		} 
		catch (JSONException e) 
		{
			logger.error(e.getMessage());
		}
		return xml;
	}
	public static JSONObject xmlToJSON(String xml)
	{
		JSONObject jsonObject = new JSONObject();
		try 
		{
			jsonObject = XML.toJSONObject(xml);
		} 
		catch(JSONException e) 
		{
			logger.error(e.getMessage());
		}
		return jsonObject;
	}
	public static String number36Encode(long input)
	{
		String key = ConstantString.BASE_36;
		String chr = "";
		int val = 0;
		long inp = input;
		String ret = "";
		StringBuilder bld = new StringBuilder();
		while(inp > 0)
		{
			val = (int) (inp) % 36;
			chr = key.substring(val, val+1);
			inp = inp/36;
			bld.append(chr+ret);
		}
		ret = bld.toString();
		if(ret.substring(0, 1).equals("0"))
		{
			ret = ret.substring(1);
		}
		return ret;
	}
	public static String number36Encode(BigInteger input)
	{
		String key = ConstantString.BASE_36;
		String chr = "";
		int val;
		BigInteger inp = input;
		String ret = "";
		BigInteger div = BigInteger.valueOf(36);
		StringBuilder bld = new StringBuilder();
		while(inp.longValue() > 0)
		{
			val = inp.mod(div).intValue();
			chr = key.substring(val, val+1);
			inp = inp.divide(div);
			bld.append(chr+ret);
		}
		ret = bld.toString();
		if(ret.substring(0, 1).equals("0"))
		{
			ret = ret.substring(1);
		}
		return ret;
	}
	public static long number36Decode(String input)
	{
		String key = ConstantString.BASE_36;
		String inp = input;
		String chr = "";
		long val = 0;
		long ret = 0;
		int exponent = 0;
		while(inp.length() > 0)
		{
			chr = inp.substring(inp.length() - 1);
			val = key.indexOf(chr);
			ret += val * Math.pow(36, exponent);
			exponent++;
			inp = inp.substring(0, inp.length() - 1);
		}
		return ret;
	}	
	public static BigInteger number36DecodeBigint(String input)
	{
		String key = ConstantString.BASE_36;
		String inp = input;
		String chr = "";
		BigInteger val;
		BigInteger ret = BigInteger.valueOf(0);
		BigInteger div = BigInteger.valueOf(36);
		int exponent = 0;
		while(inp.length() > 0)
		{
			chr = inp.substring(inp.length() - 1);
			val = BigInteger.valueOf(key.indexOf(chr));
			ret = ret.add(val.multiply(div.pow(exponent)));
			exponent++;
			inp = inp.substring(0, inp.length() - 1);
		}
		return ret;
	}
	public static String convertDateTime(String dateTimeFrom, String formatFrom, String timeZoneFrom, String formatTo, String timeZoneTo) throws ParseException
	{
		SimpleDateFormat format = new SimpleDateFormat(formatFrom);
		String dateTimeTo = "";
		try 
		{
			format.setTimeZone(TimeZone.getTimeZone(timeZoneFrom));
			Date date = format.parse(dateTimeFrom);
			dateTimeTo = Utility.date(formatTo, date, timeZoneTo);
		} 
		catch (ParseException e) 
		{
			throw new ParseException("Transaction date time format is invalid", 0);
		}
		return dateTimeTo; 
	}
	public static String fixDateTime19(String transmissionDateTime) 
	{
		if(transmissionDateTime.length() > 19)
		{
			transmissionDateTime = transmissionDateTime.substring(0, 19);
		}
		return transmissionDateTime;
	}

	/**
	 * Get MySQL format of current time
	 * @return Current time with MySQL format
	 */
	public static String mySQLDate()
	{
		return now(ConstantString.MYSQL_DATE_TIME_FORMAT);
	}
	/**
	 * Get PgSQL format of current time
	 * @return Current time with PgSQL format
	 */
	public static String pgSQLDate()
	{
		return now("yyyy-MM-dd HH:mm:ss.SSS");
	}
	
	/**
	 * Convert Date10 to MySQL date format (MMddHHmmss to yyyy-MM-dd HH:mm:ss) 
	 * @param datetime Date10
	 * @return MySQL date format
	 */
	public static String date10ToMySQLDate(String datetime)
	{
		return date10ToFullDate(datetime, ConstantString.MYSQL_DATE_TIME_FORMAT);
	}
	/**
	 * Convert Date10 to PgSQL date format (MMddHHmmss to yyyy-MM-dd HH:mm:ss.SSS) 
	 * @param datetime Date10
	 * @return PgSQL date format
	 */
	public static String date10ToPgSQLDate(String datetime)
	{
		return date10ToFullDate(datetime, "yyyy-MM-dd HH:mm:ss.SSS");
	}
	/**
	 * Convert Date10 to full date time
	 * @param datetime Date10
	 * @param format Expected format
	 * @return Full date time format
	 */
	public static String date10ToFullDate(String datetime, String format)
	{
		StringBuilder bld = new StringBuilder();
		int len = datetime.length();
		while(len < 10)
		{
			bld.append("0");
			len++;
		}
		bld.append(datetime);
		datetime = bld.toString();
		String yyyy = now("yyyy");
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String result = "";
	    try 
	    {
	    	// debug year transition
	    	if(datetime.length() > 4)
	    	{
	    		String month1 = datetime.substring(0, 4);
	    		if(month1.equals("1231"))
	    		{
		    		String month2 = now("MMdd");
		    		if(!month2.equals("0101"))
		    		{
		    			int yyyyInt = Integer.parseInt(yyyy) - 1;
		    			yyyy = yyyyInt+"";
		    		}
	    		}
	    	}
			Date dateObject = dateFormat.parse(yyyy+datetime);
			dateFormat = new SimpleDateFormat(format);
			result = dateFormat.format(dateObject);
		} 
	    catch (ParseException e) 
	    {
			result = mySQLDate();
		}
	    return result;
	}
	
	
	/**
	 * Generate SHA-256 hash code from a string
	 * @param input Input string
	 * @return SHA-256 hash code
	 */
	public static String sha256(String input)
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance(ConstantString.HASH_SHA256);
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			output = Utility.bytesToHex(encodedhash);
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	/**
	 * Generate SHA-256 hash code from a string with specified encoding
	 * @param input Input string
	 * @param encode Encoding used
	 * @return SHA-256 hash code
	 * @throws EncodingException if encoding is invalid
	 */
	public static String sha256(String input, String encode) throws EncodingException
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance(ConstantString.HASH_SHA256);
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			if(encode.equals(ConstantString.BASE_64))
			{
				output = Utility.base64Encode(encodedhash);
			}
			else if(encode.equals("hexa"))
			{
				output = Utility.bytesToHex(encodedhash);
			}
			else
			{
				throw new EncodingException(ConstantString.INVALID_ENCODING);
			}
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	/**
	 * Generate SHA-1 hash code from a string
	 * @param input Input string
	 * @return SHA-1 hash code
	 */
	public static String sha1(String input)
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			output = Utility.bytesToHex(encodedhash);
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	/**
	 * Generate SHA-1 hash code from a string with specified encoding
	 * @param input Input string
	 * @param encode Encoding used
	 * @return SHA-1 hash code
	 * @throws EncodingException if encoding is invalid
	 */
	public static String sha1(String input, String encode) throws EncodingException
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			if(encode.equals(ConstantString.BASE_64))
			{
				output = Utility.base64Encode(encodedhash);
			}
			else if(encode.equals("hexa"))
			{
				output = Utility.bytesToHex(encodedhash);
			}
			else
			{
				throw new EncodingException(ConstantString.INVALID_ENCODING);
			}
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	/**
	 * Generate SHA-1 with RSA hash code from a string with specified encoding
	 * @param input Input string
	 * @param encode Encoding used
	 * @return SHA-1 with RSA hash code
	 * @throws EncodingException if encoding is invalid
	 */
	public static String sha1WithRSA(String input, String encode) throws EncodingException
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
			PrivateKey privateKey = keyPair.getPrivate();
			Signature instance = Signature.getInstance("SHA1withRSA");
			instance.initSign(privateKey);
			instance.update((input).getBytes());
			byte[] signature = instance.sign();			
			if(encode.equals(ConstantString.BASE_64))
			{
				output = Utility.base64Encode(signature);
			}
			else if(encode.equals("hexa"))
			{
				output = Utility.bytesToHex(signature);
			}
			else
			{
				throw new EncodingException(ConstantString.INVALID_ENCODING);
			}
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	/**
	 * Generate MD5 hash code from a string
	 * @param input Input string
	 * @return MD5 hash code
	 */
	public static String md5(String input)
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			output = Utility.bytesToHex(encodedhash);
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	/**
	 * Generate MD5 hash code from a string with specified encoding
	 * @param input Input string
	 * @param encode Encoding used
	 * @return MD5 hash code
	 * @throws EncodingException if encoding is invalid
	 */
	public static String md5(String input, String encode) throws EncodingException
	{
		String output = "";
		if(input == null)
		{
			input = "";
		}
		try
		{
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			if(encode.equals(ConstantString.BASE_64))
			{
				output = Utility.base64Encode(encodedhash);
			}
			else if(encode.equals("hexa"))
			{
				output = Utility.bytesToHex(encodedhash);
			}
			else
			{
				throw new EncodingException(ConstantString.INVALID_ENCODING);
			}
			return output;
		}
		catch(Exception e)
		{
			return "";
		}
	}
	/**
	 * Convert byte to hexadecimal number
	 * @param hash Byte to be converted
	 * @return String containing hexadecimal number
	 */
	public static String bytesToHex(byte[] hash) 
	{
		StringBuilder hexString = new StringBuilder();
		String hex;
	    for (int i = 0; i < hash.length; i++) 
	    {
		    hex = Integer.toHexString(0xff & hash[i]);
		    if(hex.length() == 1)
		    {
		    	hexString.append('0');
		    }
	    	hexString.append(hex);
	    }
	    return hexString.toString();
	}

	/**
	 * hMac 256
	 * @param algorithm Algorithm
	 * @param data Data
	 * @param secret Password
	 * @return array byte contains hMac of data
	 * @throws IllegalArgumentException if any invalid arguments
	 * @throws NoSuchAlgorithmException if algorithm not found
	 * @throws InvalidKeyException if key is invalid
	 */
	public static byte[] hMac(String algorithm, String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException
	{
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(),"Hmac"+algorithm);
        Mac mac =  Mac.getInstance("Hmac"+algorithm);
        mac.init(keySpec);
        return mac.doFinal(data.getBytes());
    }
	
	public static String changeDateFormat(String oldDateString, String oldFormat, String newFormat) 
	{
		String newDateString = oldDateString;
		SimpleDateFormat sdf = new SimpleDateFormat(oldFormat);
		Date d;
		try 
		{
			d = sdf.parse(oldDateString);
			sdf.applyPattern(newFormat);
			newDateString = sdf.format(d);
		} 
		catch (ParseException e) 
		{
			logger.error(e.getMessage());
		}
		return newDateString;
	}
	
	public static String formatAmount(double amount, String format, int decimal) {
		if(decimal > 0)
		{
			String fmt = "%."+decimal+"f";
			String dec = String.format(fmt, amount);
			String[] arr = dec.split("\\.");
		   	String str = String.format("%,d", (long) amount);
	    	return String.format(format, str.replace(",", "."))+","+arr[1];
		}
		else
		{
		   	String str = String.format("%,d", (long) amount);
	    	return String.format(format, str.replace(",", "."));			
		}
	}
	public static Date getDate(int i) 
	{
		final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, i);
	    return cal.getTime();
	}
	public static String getSameDate(String tanggalMinimum, String dateFormat, int nMonth)
	{
		/**
		 * Tanggal yang sama pada bulan sebelumnya
		 */
		
		String result = tanggalMinimum;
        DateFormat sourceTime = new SimpleDateFormat(dateFormat);
        DateFormat destinationTime = new SimpleDateFormat(dateFormat);
        Date dateTime;
        try 
        {
        	dateTime = sourceTime.parse(tanggalMinimum);
    		Calendar c = Calendar.getInstance(); 
    		c.setTime(dateTime); 
    		c.add(Calendar.MONTH, nMonth);
    		Date dateTime2 = c.getTime();
        	result = destinationTime.format(dateTime2);
        } 
        catch (ParseException e) 
        {
        	logger.error(e.getMessage());
        }
        return result;
	}
	public static String prevMonth(String tanggalMinimum, String dateFormat) {
		return Utility.getSameDate(tanggalMinimum, dateFormat, -1);
	}
	
	public static Map<String, String> parseURLEncoded(String data)
	{
		Map<String, String> queryPairs = new LinkedHashMap<>();
		String[] pairs = data.split("&");
		int index = 0;
	    for (String pair : pairs) 
	    {
	        int idx = pair.indexOf("=");
	        try 
	        {
	        	String key = Utility.fixURLEncodeKey(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), index);
				queryPairs.put(key, URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
			} 
	        catch (UnsupportedEncodingException e) 
	        {
				e.printStackTrace();
			}
	        index++;
	    }
		return queryPairs;
	}

	private static String fixURLEncodeKey(String key, int index) 
	{
		return key.replace("[]", "["+index+"]");
	}

	public static List<String> asList(String input) 
	{
		List<String> list = new ArrayList<>();
		list.add(input);
		return list;
	}
	public static String basicAuth(String username, String password)
	{
		return "Basic " + Utility.base64Encode(username+":"+password);
	}
}

package com.planetbiru.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class RSUtil {
	private static Logger logger = LogManager.getLogger(RSUtil.class);	
	
	private RSUtil()
	{
		
	}
	/**
	 * Convert ResultSet to JSONObject
	 * @param rs ResultSet
	 * @return JSONObject
	 * @throws JSONException if any JSON errors
	 * @throws SQLException if any SQL errors
	 */
	public static JSONObject resultSetToJSONObject(ResultSet rs) throws SQLException
	{
		ResultSetMetaData rsmd = rs.getMetaData();
		JSONObject obj = new JSONObject();
		int numColumns = rsmd.getColumnCount();
		for (int i=1; i<numColumns+1; i++) 
		{
			String columnName = rsmd.getColumnName(i);
			int columnType = rsmd.getColumnType(i);
	
			if(columnType==java.sql.Types.ARRAY)
			{
			    obj.put(columnName, rs.getArray(columnName));
			}
			else if(columnType==java.sql.Types.BIGINT)
			{
			    obj.put(columnName, rs.getLong(columnName));
			}
			else if(columnType==java.sql.Types.BOOLEAN)
			{
			    obj.put(columnName, rs.getBoolean(columnName));
			}
			else if(columnType==java.sql.Types.BLOB)
			{
			    obj.put(columnName, rs.getBlob(columnName));
			}
			else if(columnType==java.sql.Types.DOUBLE)
			{
			    obj.put(columnName, rs.getDouble(columnName)); 
			}
			else if(columnType==java.sql.Types.FLOAT)
			{
			    obj.put(columnName, rs.getFloat(columnName));
			}
			else if(columnType==java.sql.Types.INTEGER
					|| columnType==java.sql.Types.TINYINT
					|| columnType==java.sql.Types.SMALLINT
					)
			{
			    obj.put(columnName, rs.getInt(columnName));
			}
			else if(columnType==java.sql.Types.NVARCHAR)
			{
			    obj.put(columnName, rs.getNString(columnName));
			}
			else if(columnType==java.sql.Types.VARCHAR)
			{
			    obj.put(columnName, rs.getString(columnName));
			}
			else if(columnType==java.sql.Types.DATE)
			{
			    obj.put(columnName, rs.getDate(columnName));
			}
			else if(columnType==java.sql.Types.TIMESTAMP)
			{
			    obj.put(columnName, rs.getTimestamp(columnName));   
			}
			else
			{
			    obj.put(columnName, rs.getObject(columnName));
			}
	    }
		return obj;
	}
	public static boolean getBoolean(ResultSet rs, String column) 
	{
		boolean value = false;
		try 
		{
			value = rs.getBoolean(column);
		} 
		catch (SQLException e) 
		{
			logger.error(e.getMessage());
			value = false;
		}
		return value;
	}

	public static String getString(ResultSet rs, String column)
	{
		String value = "";
		try 
		{
			value = rs.getString(column);
			if(value == null)
			{
				value = "";
			}
		} 
		catch (SQLException e) 
		{
			logger.error(e.getMessage());
			value = "";
		}
		return value;
	}
}

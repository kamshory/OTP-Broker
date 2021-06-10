package com.planetbiru.settings;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.user.UserAccount;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;

public class FeederSetting {

	private String feederType = "";
	private boolean feederSSL = false;
	private String feederAddress = "";
	private int feederPort = 0;
	private String feederPath = "";
	private String feederUsername = "";
	private String feederPassword = "";
	private String feederChannel = "";
	private int feederTimeout = 0;
	private int feederRefresh = 0;
	
	public JSONObject toJSONObject()
	{
		JSONObject feederSetting = new JSONObject();
		feederSetting.put("feederType", this.feederType);
		feederSetting.put("feederSSL", this.feederSSL);
		feederSetting.put("feederAddress", this.feederAddress);
		feederSetting.put("feederPort", this.feederPort);
		feederSetting.put("feederPath", this.feederPath);
		feederSetting.put("feederUsername", this.feederUsername);
		feederSetting.put("feederPassword", this.feederPassword);
		feederSetting.put("feederChannel", this.feederChannel);
		feederSetting.put("feederTimeout", this.feederTimeout);
		feederSetting.put("feederRefresh", this.feederRefresh);
		return feederSetting;
	}
	
	public String toString()
	{
		return this.toJSONObject().toString();
	}
	
	public void save(String path) {
		String fileName = this.getBaseDir() + path;
		this.prepareDir(fileName);	
		try 
		{
			FileUtil.write(fileName, this.toString().getBytes());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	public void load(String path)
	{
		String fileName = this.getBaseDir() + path;
		this.prepareDir(fileName);
		byte[] data = null;
		try 
		{
			data = FileUtil.read(fileName);
		} 
		catch (FileNotFoundException e1) 
		{
			/**
			 * Do nothing
			 */
		}
		if(data != null)
		{
			String text = new String(data);
			try
			{
				JSONObject feederSetting = new JSONObject(text);
				this.feederType = feederSetting.optString("feederType", "");
				this.feederSSL = feederSetting.optBoolean("feederSSL", false);
				this.feederAddress = feederSetting.optString("feederAddress", "");
				this.feederPort = feederSetting.optInt("feederPort", 0);
				this.feederPath = feederSetting.optString("feederPath", "");
				this.feederUsername = feederSetting.optString("feederUsername", "");
				this.feederPassword = feederSetting.optString("feederPassword", "");
				this.feederChannel = feederSetting.optString("feederChannel", "");
				this.feederTimeout = feederSetting.optInt("feederTimeout", 0);
				this.feederRefresh = feederSetting.optInt("feederRefresh", 0);
			}
			catch(JSONException e)
			{
				/**
				 * Do nothing
				 */
			}
		}
	}
	private void prepareDir(String fileName) {
		File file = new File(fileName);
		String directory1 = file.getParent();
		File file2 = new File(directory1);
		String directory2 = file2.getParent();
		
		File d1 = new File(directory1);
		File d2 = new File(directory2);		

		if(!d2.exists())
		{
			d2.mkdir();
		}
		if(!d1.exists())
		{
			d1.mkdir();
		}		
	}
	private String getBaseDir()
	{
		return UserAccount.class.getResource("/").getFile();
	}
	public String getFeederType() {
		return feederType;
	}
	public void setFeederType(String feederType) {
		this.feederType = feederType;
	}
	public boolean isFeederSSL() {
		return feederSSL;
	}
	public void setFeederSSL(boolean feederSSL) {
		this.feederSSL = feederSSL;
	}
	public String getFeederAddress() {
		return feederAddress;
	}
	public void setFeederAddress(String feederAddress) {
		this.feederAddress = feederAddress;
	}
	public int getFeederPort() {
		return feederPort;
	}
	public void setFeederPort(int feederPort) {
		this.feederPort = feederPort;
	}
	public String getFeederPath() {
		return feederPath;
	}
	public void setFeederPath(String feederPath) {
		this.feederPath = feederPath;
	}
	public String getFeederUsername() {
		return feederUsername;
	}
	public void setFeederUsername(String feederUsername) {
		this.feederUsername = feederUsername;
	}
	public String getFeederPassword() {
		return feederPassword;
	}
	public void setFeederPassword(String feederPassword) {
		this.feederPassword = feederPassword;
	}
	public String getFeederChannel() {
		return feederChannel;
	}
	public void setFeederChannel(String feederChannel) {
		this.feederChannel = feederChannel;
	}
	public int getFeederTimeout() {
		return feederTimeout;
	}
	public void setFeederTimeout(int feederTimeout) {
		this.feederTimeout = feederTimeout;
	}
	public int getFeederRefresh() {
		return feederRefresh;
	}
	public void setFeederRefresh(int feederRefresh) {
		this.feederRefresh = feederRefresh;
	}
	
}

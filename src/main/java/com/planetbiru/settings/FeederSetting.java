package com.planetbiru.settings;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.planetbiru.user.UserAccount;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;

public class FeederSetting {

	private boolean feederWsEnable = false;
	private boolean feederWsSSL = false;
	private String feederWsAddress = "";
	private int feederWsPort = 0;
	private String feederWsPath = "";
	private String feederWsUsername = "";
	private String feederWsPassword = "";
	private String feederWsChannel = "";
	private int feederWsTimeout = 0;
	private int feederWsReconnectDelay = 0;
	private int feederWsRefresh = 0;

	private boolean feederAmqpEnable = false;
	private boolean feederAmqpSSL = false;
	private String feederAmqpAddress = "";
	private int feederAmqpPort = 0;
	private String feederAmqpPath = "";
	private String feederAmqpUsername = "";
	private String feederAmqpPassword = "";
	private String feederAmqpChannel = "";
	private int feederAmqpTimeout = 0;
	private int feederAmqpRefresh = 0;
	
	public JSONObject toJSONObject()
	{
		JSONObject setting = new JSONObject();
		setting.put("feederWsEnable", this.feederWsEnable);
		setting.put("feederWsSSL", this.feederWsSSL);
		setting.put("feederWsAddress", this.feederWsAddress);
		setting.put("feederWsPort", this.feederWsPort);
		setting.put("feederWsPath", this.feederWsPath);
		setting.put("feederWsUsername", this.feederWsUsername);
		setting.put("feederWsPassword", this.feederWsPassword);
		setting.put("feederWsChannel", this.feederWsChannel);
		setting.put("feederWsTimeout", this.feederWsTimeout);
		setting.put("feederWsReconnectDelay", this.feederWsReconnectDelay);
		setting.put("feederWsRefresh", this.feederWsRefresh);
		setting.put("feederAmqpEnable", this.feederAmqpEnable);
		setting.put("feederAmqpSSL", this.feederAmqpSSL);
		setting.put("feederAmqpAddress", this.feederAmqpAddress);
		setting.put("feederAmqpPort", this.feederAmqpPort);
		setting.put("feederAmqpPath", this.feederAmqpPath);
		setting.put("feederAmqpUsername", this.feederAmqpUsername);
		setting.put("feederAmqpPassword", this.feederAmqpPassword);
		setting.put("feederAmqpChannel", this.feederAmqpChannel);
		setting.put("feederAmqpTimeout", this.feederAmqpTimeout);
		setting.put("feederAmqpRefresh", this.feederAmqpRefresh);
		return setting;
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
				JSONObject setting = new JSONObject(text);
				this.feederWsEnable = setting.optBoolean("feederWsEnable", false);
				this.feederWsSSL = setting.optBoolean("feederWsSSL", false);
				this.feederWsAddress = setting.optString("feederWsAddress", "");
				this.feederWsPort = setting.optInt("feederWsPort", 0);
				this.feederWsPath = setting.optString("feederWsPath", "");
				this.feederWsUsername = setting.optString("feederWsUsername", "");
				this.feederWsPassword = setting.optString("feederWsPassword", "");
				this.feederWsChannel = setting.optString("feederWsChannel", "");
				this.feederWsTimeout = setting.optInt("feederWsTimeout", 0);
				this.feederWsReconnectDelay = setting.optInt("feederWsReconnectDelay", 0);
				this.feederWsRefresh = setting.optInt("feederWsRefresh", 0);

				this.feederAmqpEnable = setting.optBoolean("feederAmqpEnable", false);
				this.feederAmqpSSL = setting.optBoolean("feederAmqpSSL", false);
				this.feederAmqpAddress = setting.optString("feederAmqpAddress", "");
				this.feederAmqpPort = setting.optInt("feederAmqpPort", 0);
				this.feederAmqpPath = setting.optString("feederAmqpPath", "");
				this.feederAmqpUsername = setting.optString("feederAmqpUsername", "");
				this.feederAmqpPassword = setting.optString("feederAmqpPassword", "");
				this.feederAmqpChannel = setting.optString("feederAmqpChannel", "");
				this.feederAmqpTimeout = setting.optInt("feederAmqpTimeout", 0);
				this.feederAmqpRefresh = setting.optInt("feederAmqpRefresh", 0);

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

	public boolean isFeederWsEnable() {
		return feederWsEnable;
	}

	public void setFeederWsEnable(boolean feederWsEnable) {
		this.feederWsEnable = feederWsEnable;
	}

	public boolean isFeederWsSSL() {
		return feederWsSSL;
	}

	public void setFeederWsSSL(boolean feederWsSSL) {
		this.feederWsSSL = feederWsSSL;
	}

	public String getFeederWsAddress() {
		return feederWsAddress;
	}

	public void setFeederWsAddress(String feederWsAddress) {
		this.feederWsAddress = feederWsAddress;
	}

	public int getFeederWsPort() {
		return feederWsPort;
	}

	public void setFeederWsPort(int feederWsPort) {
		this.feederWsPort = feederWsPort;
	}

	public String getFeederWsPath() {
		return feederWsPath;
	}

	public void setFeederWsPath(String feederWsPath) {
		this.feederWsPath = feederWsPath;
	}

	public String getFeederWsUsername() {
		return feederWsUsername;
	}

	public void setFeederWsUsername(String feederWsUsername) {
		this.feederWsUsername = feederWsUsername;
	}

	public String getFeederWsPassword() {
		return feederWsPassword;
	}

	public void setFeederWsPassword(String feederWsPassword) {
		this.feederWsPassword = feederWsPassword;
	}

	public String getFeederWsChannel() {
		return feederWsChannel;
	}

	public void setFeederWsChannel(String feederWsChannel) {
		this.feederWsChannel = feederWsChannel;
	}

	public int getFeederWsTimeout() {
		return feederWsTimeout;
	}

	public void setFeederWsTimeout(int feederWsTimeout) {
		this.feederWsTimeout = feederWsTimeout;
	}

	public int getFeederWsReconnectDelay() {
		return feederWsReconnectDelay;
	}

	public void setFeederWsReconnectDelay(int feederWsReconnectDelay) {
		this.feederWsReconnectDelay = feederWsReconnectDelay;
	}

	public int getFeederWsRefresh() {
		return feederWsRefresh;
	}

	public void setFeederWsRefresh(int feederWsRefresh) {
		this.feederWsRefresh = feederWsRefresh;
	}

	public boolean isFeederAmqpEnable() {
		return feederAmqpEnable;
	}

	public void setFeederAmqpEnable(boolean feederAmqpEnable) {
		this.feederAmqpEnable = feederAmqpEnable;
	}

	public boolean isFeederAmqpSSL() {
		return feederAmqpSSL;
	}

	public void setFeederAmqpSSL(boolean feederAmqpSSL) {
		this.feederAmqpSSL = feederAmqpSSL;
	}

	public String getFeederAmqpAddress() {
		return feederAmqpAddress;
	}

	public void setFeederAmqpAddress(String feederAmqpAddress) {
		this.feederAmqpAddress = feederAmqpAddress;
	}

	public int getFeederAmqpPort() {
		return feederAmqpPort;
	}

	public void setFeederAmqpPort(int feederAmqpPort) {
		this.feederAmqpPort = feederAmqpPort;
	}

	public String getFeederAmqpPath() {
		return feederAmqpPath;
	}

	public void setFeederAmqpPath(String feederAmqpPath) {
		this.feederAmqpPath = feederAmqpPath;
	}

	public String getFeederAmqpUsername() {
		return feederAmqpUsername;
	}

	public void setFeederAmqpUsername(String feederAmqpUsername) {
		this.feederAmqpUsername = feederAmqpUsername;
	}

	public String getFeederAmqpPassword() {
		return feederAmqpPassword;
	}

	public void setFeederAmqpPassword(String feederAmqpPassword) {
		this.feederAmqpPassword = feederAmqpPassword;
	}

	public String getFeederAmqpChannel() {
		return feederAmqpChannel;
	}

	public void setFeederAmqpChannel(String feederAmqpChannel) {
		this.feederAmqpChannel = feederAmqpChannel;
	}

	public int getFeederAmqpTimeout() {
		return feederAmqpTimeout;
	}

	public void setFeederAmqpTimeout(int feederAmqpTimeout) {
		this.feederAmqpTimeout = feederAmqpTimeout;
	}

	public int getFeederAmqpRefresh() {
		return feederAmqpRefresh;
	}

	public void setFeederAmqpRefresh(int feederAmqpRefresh) {
		this.feederAmqpRefresh = feederAmqpRefresh;
	}

	
	
	
}

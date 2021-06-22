package com.planetbiru.config;

public class Config {


	private static String otpSalt = "IJUjhrfytR^&r7jkjojO";
	private static String secretKey = "planetsecret";
	private static String apiUsername = "planet";
	private static String apiPassword = "planetpass";
	
	private static boolean otpViaWebSocket = true;
	private static boolean otpViaRabbitMQ = true;
	private static long feederWSReconnectDelay = 5000;

	private static String wsClientUsername = "qa";
	private static String wsClientPassword = "4lt0@1234";
	private static String portName = "usbtty";
	private static String defaultFile = "/index.html";
	
	private static boolean proxyEnable = false;
	private static String proxyHost = "127.0.0.1";
	private static int proxyPort = 8080;
	private static String baseDirConfig = "";
	private static String sessionName = "SMSSESSID";
	private static long sessionLifetime = 1440000;
	private static String emailSettingPath = "";
	private static String feederAMQPSettingPath = "";
	private static String feederWSSettingPath = "";
	private static String wlanSettingPath = "";
	private static String ddnsSettingPath = "";
	private static String cloudflareSettingPath = "";
	private static String apiSettingPath = "";
	private static String smsSettingPath = "";
	private static String dhcpSettingPath = "";
	private static String ethernetSettingPath = "";
	private static String modemSettingPath = "";
	private static long feederWSRecoonectDelay;
	private static String noIPDevice = "OTP Broker/version 1.0";
	private static String noIPSettingPath = "";
	private static String deviceName = "OTP Broker";
	private static String deviceVersion = "version 1.0";
	private static String keystoreSettingPath = "";
	private static String keystoreDataSettingPath = "";
	private static String blockingSettingPath = "";

	private static String defaultSMTPHost = "localhost";
	private static String defaultSMTPPort = "25";
	private static String defaultSMTPUsername = "";
	private static String defaultSMTPPassword = "";
	private static String defaultSMTPAuth = "false";
	private static String defaultSMTPEnable = "false";
	private static String defaultStarttlsEnable = "false";
	private static String defaultSMTPSSLEnable = "false";
	private static String userAPISettingPath = "";
	private static String dhcpSettingPathDefault = "";
	private static String wlanSettingPathDefault = "";
	private static String ethernetSettingPathDefault = "";
	
	private static String osWLANConfigPath = "";
	private static String osSSIDKey = "";
	private static String osEthernetConfigPath = "";
	private static String osDHCPConfigPath = "";
	private static String documentRoot = "/static/www";
	private static String userSettingPath = "";
	private static String afraidSettingPath = "";
	private static String dynuSettingPath = ""; 
	
	private Config()
	{
		
	}
	
	
	public static String getOtpSalt() {
		return otpSalt;
	}
	public static void setOtpSalt(String otpSalt) {
		Config.otpSalt = otpSalt;
	}
	public static String getApiUsername() {
		return apiUsername;
	}
	public static void setApiUsername(String apiUsername) {
		Config.apiUsername = apiUsername;
	}
	public static String getApiPassword() {
		return apiPassword;
	}
	public static void setApiPassword(String apiPassword) {
		Config.apiPassword = apiPassword;
	}
	public static String getSecretKey() {
		return secretKey;
	}
	public static void setSecretKey(String secretKey) {
		Config.secretKey = secretKey;
	}
	
	

	public static boolean isOtpViaWebSocket() {
		return otpViaWebSocket;
	}

	public static void setOtpViaWebSocket(boolean otpViaWebSocket) {
		Config.otpViaWebSocket = otpViaWebSocket;
	}

	public static boolean isOtpViaRabbitMQ() {
		return otpViaRabbitMQ;
	}

	public static void setOtpViaRabbitMQ(boolean otpViaRabbitMQ) {
		Config.otpViaRabbitMQ = otpViaRabbitMQ;
	}

	public static long getReconnectDelay() {
		return getFeederWSReconnectDelay();
	}

	public static void setReconnectDelay(long reconnectDelay) {
		Config.setFeederWSReconnectDelay(reconnectDelay);
	}

	public static String getPortName() {
		return portName;
	}

	public static void setPortName(String portName) {
		Config.portName = portName;
	}

	public static String getDefaultFile() {
		return defaultFile;
	}

	public static void setDefaultFile(String efaultFile) {
		Config.defaultFile = efaultFile;
	}

	public static String getWsClientUsername() {
		return wsClientUsername;
	}

	public static void setWsClientUsername(String wsClientUsername) {
		Config.wsClientUsername = wsClientUsername;
	}

	public static String getWsClientPassword() {
		return wsClientPassword;
	}

	public static void setWsClientPassword(String wsClientPassword) {
		Config.wsClientPassword = wsClientPassword;
	}


	public static boolean isProxyEnable() {
		return proxyEnable;
	}


	public static void setProxyEnable(boolean proxyEnable) {
		Config.proxyEnable = proxyEnable;
	}


	public static String getProxyHost() {
		return proxyHost;
	}


	public static void setProxyHost(String proxyHost) {
		Config.proxyHost = proxyHost;
	}


	public static int getProxyPort() {
		return proxyPort;
	}


	public static void setProxyPort(int proxyPort) {
		Config.proxyPort = proxyPort;
	}


	public static String getBaseDirConfig() {
		return baseDirConfig;
	}


	public static void setBaseDirConfig(String baseDirConfig) {
		Config.baseDirConfig = baseDirConfig;
	}


	public static String getSessionName() {
		return sessionName;
	}


	public static void setSessionName(String sessionName) {
		Config.sessionName = sessionName;
	}


	public static long getSessionLifetime() {
		return sessionLifetime;
	}


	public static void setSessionLifetime(long sessionLifetime) {
		Config.sessionLifetime = sessionLifetime;
	}


	public static String getEmailSettingPath() {
		return emailSettingPath;
	}


	public static void setEmailSettingPath(String emailSettingPath) {
		Config.emailSettingPath = emailSettingPath;
	}


	public static String getFeederAMQPSettingPath() {
		return feederAMQPSettingPath;
	}


	public static void setFeederAMQPSettingPath(String feederAMQPSettingPath) {
		Config.feederAMQPSettingPath = feederAMQPSettingPath;
	}


	public static String getFeederWSSettingPath() {
		return feederWSSettingPath;
	}


	public static void setFeederWSSettingPath(String feederWSSettingPath) {
		Config.feederWSSettingPath = feederWSSettingPath;
	}


	public static String getWlanSettingPath() {
		return wlanSettingPath;
	}


	public static void setWlanSettingPath(String wlanSettingPath) {
		Config.wlanSettingPath = wlanSettingPath;
	}

	public static String getDdnsSettingPath() {
		return ddnsSettingPath;
	}


	public static void setDdnsSettingPath(String ddnsSettingPath) {
		Config.ddnsSettingPath = ddnsSettingPath;
	}


	public static String getCloudflareSettingPath() {
		return cloudflareSettingPath;
	}


	public static void setCloudflareSettingPath(String cloudflareSettingPath) {
		Config.cloudflareSettingPath = cloudflareSettingPath;
	}


	public static String getApiSettingPath() {
		return apiSettingPath;
	}


	public static void setApiSettingPath(String apiSettingPath) {
		Config.apiSettingPath = apiSettingPath;
	}


	public static String getSmsSettingPath() {
		return smsSettingPath;
	}


	public static void setSmsSettingPath(String smsSettingPath) {
		Config.smsSettingPath = smsSettingPath;
	}


	public static String getDhcpSettingPath() {
		return dhcpSettingPath;
	}


	public static void setDhcpSettingPath(String dhcpSettingPath) {
		Config.dhcpSettingPath = dhcpSettingPath;
	}


	public static String getEthernetSettingPath() {
		return ethernetSettingPath;
	}


	public static void setEthernetSettingPath(String ethernetSettingPath) {
		Config.ethernetSettingPath = ethernetSettingPath;
	}


	public static String getModemSettingPath() {
		return modemSettingPath;
	}


	public static void setModemSettingPath(String modemSettingPath) {
		Config.modemSettingPath = modemSettingPath;
	}


	public static long getFeederWSReconnectDelay() {
		return feederWSReconnectDelay;
	}


	public static void setFeederWSReconnectDelay(long feederWSReconnectDelay) {
		Config.feederWSReconnectDelay = feederWSReconnectDelay;
	}


	public static long getFeederWSRecoonectDelay() {
		return feederWSRecoonectDelay;
	}


	public static void setFeederWSRecoonectDelay(long feederWSRecoonectDelay) {
		Config.feederWSRecoonectDelay = feederWSRecoonectDelay;
	}


	public static String getNoIPSettingPath() {
		return noIPSettingPath;
	}


	public static void setNoIPSettingPath(String noIPSettingPath) {
		Config.noIPSettingPath = noIPSettingPath;
	}


	public static String getNoIPDevice() {
		return noIPDevice;
	}


	public static void setNoIPDevice(String noIPDevice) {
		Config.noIPDevice = noIPDevice;
	}


	public static String getDeviceName() {
		return deviceName;
	}


	public static void setDeviceName(String deviceName) {
		Config.deviceName = deviceName;
	}


	public static String getDeviceVersion() {
		return deviceVersion;
	}


	public static void setDeviceVersion(String deviceVersion) {
		Config.deviceVersion = deviceVersion;
	}


	public static String getKeystoreSettingPath() {
		return keystoreSettingPath;
	}


	public static void setKeystoreSettingPath(String keystoreSettingPath) {
		Config.keystoreSettingPath = keystoreSettingPath;
	}


	public static String getKeystoreDataSettingPath() {
		return keystoreDataSettingPath;
	}


	public static void setKeystoreDataSettingPath(String keystoreDataSettingPath) {
		Config.keystoreDataSettingPath = keystoreDataSettingPath;
	}


	public static String getDefaultSMTPHost() {
		return defaultSMTPHost;
	}


	public static void setDefaultSMTPHost(String defaultSMTPHost) {
		Config.defaultSMTPHost = defaultSMTPHost;
	}


	public static String getDefaultSMTPPort() {
		return defaultSMTPPort;
	}


	public static void setDefaultSMTPPort(String defaultSMTPPort) {
		Config.defaultSMTPPort = defaultSMTPPort;
	}


	public static String getDefaultSMTPUsername() {
		return defaultSMTPUsername;
	}


	public static void setDefaultSMTPUsername(String defaultSMTPUsername) {
		Config.defaultSMTPUsername = defaultSMTPUsername;
	}


	public static String getDefaultSMTPPassword() {
		return defaultSMTPPassword;
	}


	public static void setDefaultSMTPPassword(String defaultSMTPPassword) {
		Config.defaultSMTPPassword = defaultSMTPPassword;
	}


	public static String getDefaultSMTPAuth() {
		return defaultSMTPAuth;
	}


	public static void setDefaultSMTPAuth(String defaultSMTPAuth) {
		Config.defaultSMTPAuth = defaultSMTPAuth;
	}


	public static String getDefaultSMTPEnable() {
		return defaultSMTPEnable;
	}


	public static void setDefaultSMTPEnable(String defaultSMTPEnable) {
		Config.defaultSMTPEnable = defaultSMTPEnable;
	}


	public static String getDefaultStarttlsEnable() {
		return defaultStarttlsEnable;
	}


	public static void setDefaultStarttlsEnable(String defaultStarttlsEnable) {
		Config.defaultStarttlsEnable = defaultStarttlsEnable;
	}


	public static String getDefaultSMTPSSLEnable() {
		return defaultSMTPSSLEnable;
	}


	public static void setDefaultSMTPSSLEnable(String defaultSMTPSSLEnable) {
		Config.defaultSMTPSSLEnable = defaultSMTPSSLEnable;
	}


	public static String getBlockingSettingPath() {
		return blockingSettingPath;
	}


	public static void setBlockingSettingPath(String blockingSettingPath) {
		Config.blockingSettingPath = blockingSettingPath;
	}


	public static String getUserAPISettingPath() {
		return userAPISettingPath;
	}


	public static void setUserAPISettingPath(String userAPISettingPath) {
		Config.userAPISettingPath = userAPISettingPath;
	}


	public static String getDhcpSettingPathDefault() {
		return dhcpSettingPathDefault;
	}


	public static void setDhcpSettingPathDefault(String dhcpSettingPathDefault) {
		Config.dhcpSettingPathDefault = dhcpSettingPathDefault;
	}


	public static String getWlanSettingPathDefault() {
		return wlanSettingPathDefault;
	}


	public static void setWlanSettingPathDefault(String wlanSettingPathDefault) {
		Config.wlanSettingPathDefault = wlanSettingPathDefault;
	}


	public static String getEthernetSettingPathDefault() {
		return ethernetSettingPathDefault;
	}


	public static void setEthernetSettingPathDefault(String ethernetSettingPathDefault) {
		Config.ethernetSettingPathDefault = ethernetSettingPathDefault;
	}


	public static String getOsWLANConfigPath() {
		return osWLANConfigPath;
	}


	public static void setOsWLANConfigPath(String osWLANConfigPath) {
		Config.osWLANConfigPath = osWLANConfigPath;
	}


	public static String getOsSSIDKey() {
		return osSSIDKey;
	}


	public static void setOsSSIDKey(String osSSIDKey) {
		Config.osSSIDKey = osSSIDKey;
	}


	public static String getOsEthernetConfigPath() {
		return osEthernetConfigPath;
	}


	public static void setOsEthernetConfigPath(String osEthernetConfigPath) {
		Config.osEthernetConfigPath = osEthernetConfigPath;
	}


	public static String getOsDHCPConfigPath() {
		return osDHCPConfigPath;
	}


	public static void setOsDHCPConfigPath(String osDHCPConfigPath) {
		Config.osDHCPConfigPath = osDHCPConfigPath;
	}


	public static String getDocumentRoot() {
		return documentRoot;
	}


	public static void setDocumentRoot(String documentRoot) {
		Config.documentRoot = documentRoot;
	}


	public static String getUserSettingPath() {
		return userSettingPath;
	}


	public static void setUserSettingPath(String userSettingPath) {
		Config.userSettingPath = userSettingPath;
	}


	public static String getAfraidSettingPath() {
		return afraidSettingPath;
	}


	public static void setAfraidSettingPath(String afraidSettingPath) {
		Config.afraidSettingPath = afraidSettingPath;
	}


	public static String getDynuSettingPath() {
		return dynuSettingPath;
	}


	public static void setDynuSettingPath(String dynuSettingPath) {
		Config.dynuSettingPath = dynuSettingPath;
	}



	
	

}

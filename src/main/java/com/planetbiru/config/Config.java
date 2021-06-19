package com.planetbiru.config;

public class Config {


	private static String otpSalt = "IJUjhrfytR^&r7jkjojO";
	private static String secretKey = "planetsecret";
	private static String apiUsername = "planet";
	private static String apiPassword = "planetpass";
	
	private static boolean otpViaWebSocket = true;
	private static boolean otpViaRabbitMQ = true;
	private static long reconnectDelay = 5000;
	private static String ssClientEndpoint = "ws://localhost:8888/ws";
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
		return reconnectDelay;
	}

	public static void setReconnectDelay(long reconnectDelay) {
		Config.reconnectDelay = reconnectDelay;
	}

	public static String getSsClientEndpoint() {
		return ssClientEndpoint;
	}

	public static void setSsClientEndpoint(String ssClientEndpoint) {
		Config.ssClientEndpoint = ssClientEndpoint;
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



	
	

}

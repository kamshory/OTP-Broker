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
	private static String baseDir = "";
	
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


	public static String getBaseDir() {
		return baseDir;
	}


	public static void setBaseDir(String baseDir) {
		Config.baseDir = baseDir;
	}

	
	

}

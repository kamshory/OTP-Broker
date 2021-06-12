package com.planetbiru.config;

public class Config {

	private static String databaseType = "mariadb";
	private static boolean keepBalamceDorman = true;
	private static boolean dormanCostMonthly = true;
	private static String websocketURL = "ws://localhost:8888/ws";
	private static String sessid = "sessid";
	private static boolean saveUnsentMessage = true;
	private static String tablePrefix = "mq_";
	private static String otpSalt = "IJUjhrfytR^&r7jkjojO";
	private static String otpFormat = "OTP Anda adalah >> %s <<";
	private static String secretKey = "planetsecret";
	private static String apiUsername = "planet";
	private static String apiPassword = "planetpass";
	
	
	private static String mailSenderAddress = "user@domain.com";
	private static String mailSenderPassword;
	private static String mailAuth = "true";
	private static String mailStartTLS = "true";
	private static String mailHost = "smtp.gmail.com";
	private static String mailPort = "587";
	private static boolean otpViaWebSocket = true;
	private static boolean otpViaRabbitMQ = true;
	private static long reconnectDelay = 5000;
	private static String ssClientEndpoint = "ws://localhost:8888/ws";
	private static String wsClientUsername = "qa";
	private static String wsClientPassword = "4lt0@1234";
	private static String portName = "usbtty";
	private static String defaultFile = "/index.html";
	
	private Config()
	{
		
	}
	
	public static String getDatabaseType() {
		return databaseType;
	}
	public static void setDatabaseType(String databaseType) {
		Config.databaseType = databaseType;
	}
	public static boolean isKeepBalamceDorman() {
		return keepBalamceDorman;
	}
	public static void setKeepBalamceDorman(boolean keepBalamceDorman) {
		Config.keepBalamceDorman = keepBalamceDorman;
	}
	public static boolean isDormanCostMonthly() {
		return dormanCostMonthly;
	}
	public static void setDormanCostMonthly(boolean dormanCostMonthly) {
		Config.dormanCostMonthly = dormanCostMonthly;
	}
	public static String getWebsocketURL() {
		return websocketURL;
	}
	public static void setWebsocketURL(String websocketURL) {
		Config.websocketURL = websocketURL;
	}
	public static String getSessid() {
		return sessid;
	}
	public static void setSessid(String sessid) {
		Config.sessid = sessid;
	}
	public static boolean isSaveUnsentMessage() {
		return saveUnsentMessage;
	}
	public static void setSaveUnsentMessage(boolean saveUnsentMessage) {
		Config.saveUnsentMessage = saveUnsentMessage;
	}
	public static String getOtpFormat() {
		return otpFormat;
	}
	public static void setOtpFormat(String otpFormat) {
		Config.otpFormat = otpFormat;
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
	public static String getMailSenderAddress() {
		return mailSenderAddress;
	}
	public static void setMailSenderAddress(String mailSenderAddress) {
		Config.mailSenderAddress = mailSenderAddress;
	}
	public static String getMailSenderPassword() {
		return mailSenderPassword;
	}
	public static void setMailSenderPassword(String mailSenderPassword) {
		Config.mailSenderPassword = mailSenderPassword;
	}
	public static String getMailAuth() {
		return mailAuth;
	}
	public static void setMailAuth(String mailAuth) {
		Config.mailAuth = mailAuth;
	}
	public static String getMailStartTLS() {
		return mailStartTLS;
	}
	public static void setMailStartTLS(String mailStartTLS) {
		Config.mailStartTLS = mailStartTLS;
	}
	public static String getMailHost() {
		return mailHost;
	}
	public static void setMailHost(String mailHost) {
		Config.mailHost = mailHost;
	}
	public static String getMailPort() {
		return mailPort;
	}
	public static void setMailPort(String mailPort) {
		Config.mailPort = mailPort;
	}

	public static String getTablePrefix() {
		return tablePrefix;
	}

	public static void setTablePrefix(String tablePrefix) {
		Config.tablePrefix = tablePrefix;
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

}

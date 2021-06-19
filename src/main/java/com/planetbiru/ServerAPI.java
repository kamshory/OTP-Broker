package com.planetbiru;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.planetbiru.api.HandlerAPIMessage;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.gsm.SMSUtil;
import com.planetbiru.util.ServiceHTTP;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

@Service
public class ServerAPI {

	private Logger logger = LogManager.getLogger(ServerAPI.class);
	
	private String keystorePassword = "4lt0@1234!";
	private String keystoreFile = "C:/static/keystore.jks";
	
	@Value("${otpbroker.path.setting.api.service}")
	private String apiSettingPath;

	@Value("${otpbroker.path.setting.email}")
	private String emailSettingPath;
	
	@Value("${otpbroker.mail.sender.address}")
	private String mailSenderAddress;

	@Value("${otpbroker.mail.sender.password}")
	private String mailSenderPassword;
	
	@Value("${otpbroker.mail.auth}")
	private boolean mailAuth;
	
	@Value("${otpbroker.mail.start.tls}")
	private boolean mailStartTLS;
	
	@Value("${otpbroker.mail.ssl}")
	private boolean mailSSL;
	
	@Value("${otpbroker.mail.host}")
	private String mailHost;
	
	@Value("${otpbroker.mail.port}")
	private int mailPort;
	
	@Value("${otpbroker.api.http.port}")
	private int httpPort;
	
	@Value("${otpbroker.api.https.port}")
	private int httpsPort;
	
	@Value("${otpbroker.api.http.enable}")
	private boolean httpEnable;
	
	@Value("${otpbroker.api.https.enable}")
	private boolean httpsEnable;
	
	@Value("${otpbroker.api.path.message}")
	private String messagePath;
	
	@Value("${otpbroker.api.path.block}")
	private String blockinPath;
	
	@Value("${otpbroker.api.path.unblock}")
	private String unblockinPath;


	@PostConstruct
	public void init()
	{
		SMSUtil.init();
		
		this.loadConfigHttp();
		this.loadConfigEmail();
		this.initHttp();
		this.initHttps();
		
	}
	
	private void loadConfigHttp() {
		Config.setApiSettingPath(apiSettingPath);
		
		ConfigAPI.setHttpPort(httpPort);
		ConfigAPI.setHttpsPort(httpsPort);
		ConfigAPI.setHttpEnable(httpEnable);	
		ConfigAPI.setHttpsEnable(httpsEnable);	
		ConfigAPI.setMessagePath(messagePath);
		ConfigAPI.setBlockinPath(blockinPath);
		ConfigAPI.setUnblockinPath(unblockinPath);
		
		ConfigAPI.load(Config.getApiSettingPath());
	}

	private void loadConfigEmail()
	{
		Config.setEmailSettingPath(emailSettingPath);
		
		ConfigEmail.setMailSenderAddress(mailSenderAddress);
		ConfigEmail.setMailSenderPassword(mailSenderPassword);
		ConfigEmail.setMailAuth(mailAuth);
		ConfigEmail.setMailSSL(mailSSL);
		ConfigEmail.setMailStartTLS(mailStartTLS);
		ConfigEmail.setMailHost(mailHost);
		ConfigEmail.setMailPort(mailPort);	
		
		/**
		 * Override email setting if exists
		 */
		ConfigEmail.load(Config.getEmailSettingPath());
	}	
	
	private void initHttps() {
		if(ConfigAPI.isHttpsEnable())
		{
			try {
				ServiceHTTP.setHttpsServer(HttpsServer.create(new InetSocketAddress(ConfigAPI.getHttpsPort()), 0));
				try (FileInputStream fileInputStream = new FileInputStream(keystoreFile))
				{
					char[] password = keystorePassword.toCharArray();
				    KeyStore keyStore;
					keyStore = KeyStore.getInstance("JKS");	
					SSLContext sslContext = SSLContext.getInstance("TLS");
					keyStore.load(fileInputStream, password);
				    KeyManagerFactory keyManagementFactory = KeyManagerFactory.getInstance("SunX509");
				    keyManagementFactory.init (keyStore, password);
				    TrustManagerFactory trustFactory = TrustManagerFactory.getInstance("SunX509");
				    trustFactory.init(keyStore);
				    sslContext.init(keyManagementFactory.getKeyManagers(), trustFactory.getTrustManagers(), null);		
					HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext);
					ServiceHTTP.getHttpsServer().setHttpsConfigurator(httpsConfigurator);		
			        ServiceHTTP.getHttpsServer().createContext(ConfigAPI.getMessagePath(), new HandlerAPIMessage());
			        ServiceHTTP.getHttpsServer().start();
				} 
				catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | KeyManagementException | UnrecoverableKeyException e) 
				{
					logger.info(e.getMessage());
				}
			} catch (IOException e1) {
				logger.info(e1.getMessage());
			}
			
			
		}
		
	}
	private void initHttp() {
		if(ConfigAPI.isHttpsEnable())
		{
			try 
			{
				ServiceHTTP.setHttpServer(HttpServer.create(new InetSocketAddress(ConfigAPI.getHttpPort()), 0));
		        ServiceHTTP.getHttpServer().createContext(ConfigAPI.getMessagePath(), new HandlerAPIMessage());
		        ServiceHTTP.getHttpServer().start();
			} 
			catch (IOException e) 
			{
				logger.info(e.getMessage());
			}
		}
		
	}
	@PreDestroy
	public void destroy()
	{
		if(ConfigAPI.isHttpsEnable() && ServiceHTTP.getHttpServer() != null)
		{
			ServiceHTTP.getHttpServer().stop(0);
		}
		if(ConfigAPI.isHttpsEnable() && ServiceHTTP.getHttpsServer() != null)
		{
			ServiceHTTP.getHttpsServer().stop(0);
		}
	}
}

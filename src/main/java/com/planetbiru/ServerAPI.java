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
	private int httpPort = 8088;
	private int httpsPort = 8089;
	private String pathSendSMS = "/send-sms";	
	private String jksPassword = "auewfiuwehfiwehfewfewf";
	private String keystoreFile = "auewfiuwehfiwehfewfewf.jks";

	
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

	@Value("${otpbroker.path.setting.email}")
	private String emailSettingPath;


	@PostConstruct
	public void init()
	{
		SMSUtil.init();
		
		this.loadConfigEmail();
		this.initHttp();
		this.initHttps();
		
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
		if(ConfigAPI.isHttpEnable())
		{
			try (FileInputStream fileInputStream = new FileInputStream(keystoreFile))
			{
				char[] password = jksPassword.toCharArray();
			    KeyStore keyStore;

				keyStore = KeyStore.getInstance("JKS");
				

				SSLContext sslContext = SSLContext.getInstance("TLS");
				keyStore.load(fileInputStream, password);
			    KeyManagerFactory keyManagementFactory = KeyManagerFactory.getInstance("SunX509");
			    keyManagementFactory.init (keyStore, password);
			    TrustManagerFactory trustFactory = TrustManagerFactory.getInstance("SunX509");
			    trustFactory.init (keyStore);
			    sslContext.init(keyManagementFactory.getKeyManagers(), trustFactory.getTrustManagers(), null);							    
				HttpsConfigurator httpsConfigurator = new HttpsConfigurator(sslContext);
				ServiceHTTP.getHttpsServer().setHttpsConfigurator(httpsConfigurator);		
				
				ServiceHTTP.setHttpsServer(HttpsServer.create(new InetSocketAddress(this.httpsPort), 0));
		        ServiceHTTP.getHttpsServer().createContext(this.pathSendSMS, new HandlerAPIMessage());
		        ServiceHTTP.getHttpsServer().start();
			} 
			catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException | KeyManagementException | UnrecoverableKeyException e) 
			{
				e.printStackTrace();
			}
		}
		
	}
	private void initHttp() {
		if(ConfigAPI.isHttpsEnable())
		{
			try 
			{
				ServiceHTTP.setHttpServer(HttpServer.create(new InetSocketAddress(this.httpPort), 0));
		        ServiceHTTP.getHttpServer().createContext(this.pathSendSMS, new HandlerAPIMessage());
		        ServiceHTTP.getHttpServer().start();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
	}
	@PreDestroy
	public void destroy()
	{
		ServiceHTTP.getHttpServer().start();
	}
}

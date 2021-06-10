package com.planetbiru.util;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.planetbiru.config.Config;

public class MailUtil {
	private static Logger logger = LogManager.getLogger(MailUtil.class);	
	private MailUtil()
	{
		
	}
	public static void sendmail(String to, String subject, String message, String contentType) throws MessagingException
	{
		String from = Config.getMailSenderAddress();
		MailUtil.sendmail(to, subject, message, contentType, from);
	}
	public static void sendmail(String to, String subject, String message, String contentType, String from) throws MessagingException 
	{
		Properties props = new Properties();
		
		String mailAuth = Config.getMailAuth();
		String mailStartTLS = Config.getMailStartTLS();
		String mailHost = Config.getMailHost();
		String mailPort = Config.getMailPort();
		
		props.put("mail.smtp.auth", mailAuth);
		props.put("mail.smtp.starttls.enable", mailStartTLS);
		props.put("mail.smtp.host", mailHost);
		props.put("mail.smtp.port", mailPort);
		
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				String sender = Config.getMailSenderAddress();
				String password = Config.getMailSenderPassword();
				return new PasswordAuthentication(sender, password);
			}
		});
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from, false));
		
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		msg.setSubject(subject);
		msg.setContent(message, contentType);
		msg.setSentDate(new Date());
		
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(message, contentType);
		
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);
		/**
		MimeBodyPart attachPart = new MimeBodyPart();
		attachPart.attachFile("/var/tmp/image19.png");
		multipart.addBodyPart(attachPart);
		msg.setContent(multipart);
		*/
		Transport.send(msg);  
		logger.info("Send message");
	}
}

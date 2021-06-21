package com.planetbiru.util;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
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

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigEmail;

public class MailUtil {
	private Session session;
	private String smtpHost = "smtp.gmail.com";
	private int smtpPort = 587;
    private String smtpUser = "";
    private String smtpPassword = "";
    private boolean mailAuth = false;
    private boolean ssl = false;
    private boolean starttls = true;   
    private boolean debug = false;
    private boolean active = false;
    
    public MailUtil(String smtpHost, int smtpPort, String smtpUser, String smtpPassword, boolean ssl, boolean starttls, boolean debug)
    {
    	this.smtpHost = smtpHost;
    	this.smtpPort = smtpPort;
    	this.smtpUser = smtpUser;
    	this.smtpPassword = smtpPassword;
    	this.ssl = ssl;
    	this.starttls = starttls;
    	this.debug = debug;
    	this.mailAuth = true;
    	this.active = true;
    	this.init();
    }   
    
	public MailUtil() {
		this.smtpHost = ConfigEmail.getMailHost();
    	this.smtpPort = ConfigEmail.getMailPort();
    	this.smtpUser = ConfigEmail.getMailSenderAddress();
    	this.smtpPassword = ConfigEmail.getMailSenderPassword();
    	this.ssl = ConfigEmail.isMailSSL();
    	this.starttls = ConfigEmail.isMailStartTLS();
    	this.mailAuth = ConfigEmail.getMailAuth();
    	this.active = ConfigEmail.isMailActive();
    	this.init();
	}

	public void init()
	{
		Properties properties = new Properties();
 		if(this.active)
        {
 			properties.put("mail.smtp.auth", Boolean.toString(this.mailAuth));
	        if(this.starttls)
 			{
	        	properties.put("mail.smtp.starttls.enable", Boolean.toString(this.starttls));
 			}
	        if(this.ssl)
 			{
	        	properties.put("mail.smtp.ssl.enable", Boolean.toString(this.ssl));
 			}
 			properties.put("mail.smtp.host", this.smtpHost);
 			properties.put("mail.smtp.port", Integer.toString(this.smtpPort));	
	        properties.put("mail.smtp.socketFactory.port", this.smtpPort+"");
        }
        else
        {
        	properties.put("mail.smtp.host", Config.getDefaultSMTPHost());
	        properties.put("mail.smtp.port", Config.getDefaultSMTPPort());
	        properties.put("mail.smtp.auth", Config.getDefaultSMTPAuth());        
	        if(this.ssl)
 			{
	        	properties.put("mail.smtp.ssl.enable", Config.getDefaultSMTPSSLEnable());
 			}
	        if(Config.getDefaultStarttlsEnable().equals("true"))
	        {
	        	properties.put("mail.smtp.starttls.enable", Config.getDefaultStarttlsEnable());        
	        }
	        this.smtpUser = Config.getDefaultSMTPUsername();
	        this.smtpPassword = Config.getDefaultSMTPPassword();     
        }
 		
        String localSmtpUser = this.smtpUser;
        String localSmtpPassword = this.smtpPassword;
        
        session = Session.getInstance(properties, new Authenticator() {
        	@Override
			protected PasswordAuthentication getPasswordAuthentication() 
        	{
                return new PasswordAuthentication(localSmtpUser, localSmtpPassword);
            }
        });
        session.setDebug(debug);
	}
	public boolean send(String to, String subject, String message) throws MessagingException
	{
 		return this.send(to, subject, message, this.smtpUser);
	}
	
	public boolean send(String to, String subject, String message, String from) throws MessagingException
	{
		boolean sent = false;

        // Create a default MimeMessage object.
        MimeMessage mimeMessage = new MimeMessage(session);

		mimeMessage.setFrom(new InternetAddress(from, false));

        // Set From: header field of the header.
        mimeMessage.setFrom(new InternetAddress(this.smtpUser));

        // Set To: header field of the header.
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

        // Set Subject: header field
        mimeMessage.setSubject(subject);

        // Now set the actual message
        mimeMessage.setText(message, "utf-8", "html");
        
        mimeMessage.setSentDate(new Date());

        // Send message
        Transport.send(mimeMessage);
        sent = true;
        return sent;
	}
	
	
	public void sendmail(String to, String subject, String message, String contentType, String from, List<String> files) throws MessagingException, IOException 
	{
		
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
		
		if(files != null)
		{
			for(int i = 0; i<files.size(); i++)
			{
				String path = files.get(i);
				MimeBodyPart attachPart = new MimeBodyPart();
				attachPart.attachFile(path);
				multipart.addBodyPart(attachPart);
				msg.setContent(multipart);
				
			}
		}
		
		Transport.send(msg);  
	}
	
	public boolean send(String to, String subject, String message, String from, String contentType, List<String> files) throws MessagingException, IOException
	{
 		boolean sent = false;

 		Message mimeMessage = new MimeMessage(session);
		mimeMessage.setFrom(new InternetAddress(from, false));
		
		mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		mimeMessage.setSubject(subject);
		mimeMessage.setContent(message, contentType);
		mimeMessage.setSentDate(new Date());
		
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(message, contentType);
		
		if(files != null)
		{
			for(int i = 0; i<files.size(); i++)
			{
				String path = files.get(i);
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
		
				MimeBodyPart attachPart = new MimeBodyPart();
				attachPart.attachFile(path);
				multipart.addBodyPart(attachPart);
				mimeMessage.setContent(multipart);
				
			}
		}

		Transport.send(mimeMessage);  
		sent = true;
		return sent;
	}
	
}

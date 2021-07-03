package com.planetbiru;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.subethamail.smtp.server.SMTPServer;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigSMTP;
import com.planetbiru.mail.PlanetMessageHandlerFactory;

@Service
public class ServerSMTPLocal {

	private Logger logger = LogManager.getLogger(ServerSMTPLocal.class);

	@Value("${otpbroker.path.setting.smtp}")
	private String smtpSettingPath;

	private PlanetMessageHandlerFactory handlerFactory = new PlanetMessageHandlerFactory();
	
	private String serverAddress = "localhost";
	private int port = 25;
	private String softwareName = "";
	
	private SMTPServer server = null;
	
	@PostConstruct
	public void init()
	{
		Config.setSmtpSettingPath(smtpSettingPath);
		ConfigSMTP.load(Config.getSmtpSettingPath());
		
		if(ConfigSMTP.isActive())
		{
			this.port = ConfigSMTP.getServerPort();
			this.serverAddress = ConfigSMTP.getServerAddress();
			this.softwareName = ConfigSMTP.getSoftwareName();
			this.start();
		}
	}
	
	@PreDestroy
	public void destroy()
	{
		this.stop();
	}
	
	public void stop() 
	{
		this.server.stop();	
	}
	
	public void start()
    {
        this.server = new SMTPServer(this.handlerFactory);
        this.server.setHostName(this.serverAddress);
        this.server.setSoftwareName(this.softwareName);
        this.server.setPort(this.port);

        try
        {
            server.start();
        }
        catch (Exception e)
        {
            logger.error("Could not start MockMock. Maybe port {} is already in use?\r\n{}", this.port, e.getMessage());
        }
    }
}

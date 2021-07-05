package com.planetbiru;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.planetbiru.api.RESTAPI;
import com.planetbiru.config.ConfigAPIUser;
import com.planetbiru.config.ConfigBlocking;
import com.planetbiru.config.ConfigVendorAfraid;
import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigVendorCloudflare;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigVendorDynu;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.config.ConfigFirewall;
import com.planetbiru.config.ConfigGeneral;
import com.planetbiru.config.ConfigKeystore;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;
import com.planetbiru.config.ConfigVendorNoIP;
import com.planetbiru.config.DataEmail;
import com.planetbiru.config.ConfigSMS;
import com.planetbiru.config.ConfigSMTP;
import com.planetbiru.config.PropertyLoader;
import com.planetbiru.config.DataModem;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.constant.ResponseCode;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.ddns.DDNSRecord;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.gsm.InvalidPortException;
import com.planetbiru.mail.MailUtil;
import com.planetbiru.mail.NoEmailAccountException;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.User;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.FileConfigUtil;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.ServerStatus;
import com.planetbiru.util.Utility;
import com.planetbiru.util.WebManagerContent;
import com.planetbiru.util.WebManagerTool;

@RestController
public class ServerWebManager {
	
	private Logger logger = LogManager.getLogger(ServerWebManager.class);	
	
	@Value("${otpbroker.device.name}")
	private String deviceName;

	@Value("${otpbroker.device.version}")
	private String deviceVersion;

	@Value("${otpbroker.web.session.name}")
	private String sessionName;

	@Value("${otpbroker.web.session.lifetime}")
	private long sessionLifetime;
	
	@Value("${otpbroker.web.session.file.path}")
	private String sessionFilePath;

	@Value("${otpbroker.web.document.root}")
	private String documentRoot;

	@Value("${otpbroker.path.setting.feeder.ws}")
	private String feederWSSettingPath;

	@Value("${otpbroker.path.setting.feeder.amqp}")
	private String feederAMQPSettingPath;
	
	@Value("${otpbroker.path.setting.all}")
	private String mimeSettingPath;	
	
	@Value("${otpbroker.path.setting.user}")
	private String userSettingPath;

	@Value("${otpbroker.path.setting.ddns}")
	private String ddnsSettingPath;
	
	@Value("${otpbroker.path.setting.ddns.cloudflare}")
	private String cloudflareSettingPath;

	@Value("${otpbroker.path.setting.ddns.noip}")
	private String noIPSettingPath;

	@Value("${otpbroker.path.setting.ddns.afraid}")
	private String afraidSettingPath;
	
	@Value("${otpbroker.path.setting.ddns.dynu}")
	private String dynuSettingPath;

	@Value("${otpbroker.path.setting.modem}")
	private String modemSettingPath;

	@Value("${otpbroker.path.setting.general}")
	private String generalSettingPath;

	@Value("${otpbroker.path.setting.smtp}")
	private String smtpSettingPath;

	@Value("${otpbroker.path.base.setting}")
	private String baseDirConfig;
	
	@Value("${otpbroker.path.setting.blocking}")
	private String blockingSettingPath;	

	@Value("${otpbroker.path.setting.firewall}")
	private String firewallSettingPath;

	@Value("${otpbroker.ssh.restart.command}")
	private String restartCommand;

	@Value("${otpbroker.ssh.cleanup.command}")
	private String cleanupCommand;

	@Value("${otpbroker.log.dir}")
	private String logDir;	

	@Value("${otpbroker.storage.dir}")
	private String storageDir;	

	@Value("${otpbroker.server.hmac}")
	private String hmac;
	
	@Value("${server.port}")
	private int portManager;

	@Value("${otpbroker.show.trafic}")
	private boolean showTraffic;
	
	private ServerWebManager()
    {
    }
	
	@PostConstruct
	public void init()
	{
		/**
		 * This configuration must be loaded first
		 */
		Config.setBaseDirConfig(baseDirConfig);		

		Config.setUserSettingPath(userSettingPath);
		Config.setDocumentRoot(documentRoot);
		Config.setDeviceName(deviceName);
		Config.setDeviceVersion(deviceVersion);
		Config.setNoIPDevice(deviceName+"/"+deviceVersion);	
		
		Config.setBlockingSettingPath(blockingSettingPath);
		Config.setModemSettingPath(modemSettingPath);
		
		Config.setFeederWSSettingPath(feederWSSettingPath);
		Config.setFeederWSSettingPath(feederWSSettingPath);
		Config.setFeederAMQPSettingPath(feederAMQPSettingPath);

		Config.setSessionFilePath(sessionFilePath);
		Config.setSessionName(sessionName);
		Config.setSessionLifetime(sessionLifetime);
		
		Config.setDdnsSettingPath(ddnsSettingPath);
		Config.setCloudflareSettingPath(cloudflareSettingPath);
		Config.setNoIPSettingPath(noIPSettingPath);
		Config.setDynuSettingPath(dynuSettingPath);
		Config.setAfraidSettingPath(afraidSettingPath);
		Config.setGeneralSettingPath(generalSettingPath);
		Config.setSmtpSettingPath(smtpSettingPath);
		
		Config.setRestartCommand(restartCommand);
		Config.setCleanupCommand(cleanupCommand);
		
		Config.setMimeSettingPath(mimeSettingPath);
		
		Config.setLogDir(logDir);
		Config.setStorageDir(storageDir);
		
		Config.setShowTraffic(showTraffic);
		
		Config.setPortManager(portManager);
		Config.setFirewallSettingPath(firewallSettingPath);
		
		ConfigDDNS.load(Config.getDdnsSettingPath());
		ConfigVendorCloudflare.load(Config.getCloudflareSettingPath());
		ConfigVendorNoIP.load(Config.getNoIPSettingPath());
		ConfigGeneral.load(Config.getGeneralSettingPath());
		ConfigAPI.load(Config.getApiSettingPath());

		WebUserAccount.load(Config.getUserSettingPath());		
		
		PropertyLoader.load(Config.getMimeSettingPath());
		
		Config.setValidDevice(ServerInfo.cpuSerialNumberHmac().equals(hmac));
	}
	
	@PostMapping(path="/api/device/**")
	public ResponseEntity<String> modemConnect(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode;
		JSONObject responseJSON = new JSONObject();
		statusCode = HttpStatus.OK;
		try 
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				String action = queryPairs.getOrDefault("action", "");
				String modemID = queryPairs.getOrDefault("id", "");
				if(!modemID.isEmpty())
				{
					try 
					{
						if(action.equals("connect"))
						{
							GSMUtil.connect(modemID);						
						}
						else
						{
							GSMUtil.disconnect(modemID);
						} 
						ServerInfo.sendModemStatus();
					}
					catch (GSMException | InvalidPortException e) 
					{
						/**
						 * 
						 */
						this.broardcastWebSocket(e.getMessage());
					}
				}
			} 
			else 
			{
				statusCode = HttpStatus.UNAUTHORIZED;
				responseJSON = RESTAPI.unauthorized(requestBody);					
			}
		} 
		catch (NoUserRegisteredException e) 
		{
			statusCode = HttpStatus.UNAUTHORIZED;
			responseJSON = RESTAPI.unauthorized(requestBody);					
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		String responseBody = responseJSON.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@PostMapping(path="/api/email**")
	public ResponseEntity<String> sendEmail(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		JSONObject response = new JSONObject();
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode;
		statusCode = HttpStatus.OK;
		try 
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigEmail.load(Config.getEmailSettingPath());
				Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);		
				String to = queryPairs.getOrDefault("recipient", "").trim();
				String subject = queryPairs.getOrDefault("subject", "").trim();
				String message = queryPairs.getOrDefault(JsonKey.MESSAGE, "").trim();
				String result = "";

				try 
				{
					MailUtil.send(to, subject, message);
					result = "The message was sent successfuly";
					response.put(JsonKey.SUCCESS, true);
				} 
				catch (MessagingException | NoEmailAccountException e) 
				{
					result = e.getMessage();
					this.broardcastWebSocket(result);
					response.put(JsonKey.SUCCESS, false);
				}
				this.broardcastWebSocket(result);
				response.put(JsonKey.MESSAGE, result);
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;
				response.put(JsonKey.SUCCESS, false);	
				response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
			}
		} 
		catch (NoUserRegisteredException e) 
		{
			response.put(JsonKey.SUCCESS, false);
			response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		String responseBody = response.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/api/sms**")
	public ResponseEntity<String> sendSMS(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode;
		statusCode = HttpStatus.OK;
		JSONObject response = new JSONObject();
		try 
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
				String modemID = queryPairs.getOrDefault("modem_id", "");
				String receiver = queryPairs.getOrDefault("receiver", "");
				String message = queryPairs.getOrDefault(JsonKey.MESSAGE, "");
				String result = "";
				try 
				{
					GSMUtil.sendSMS(receiver, message, modemID);
					result = "The message was sent via device "+modemID;
					response.put(JsonKey.SUCCESS, false);
				} 
				catch (GSMException e) 
				{
					result = e.getMessage();
					response.put(JsonKey.SUCCESS, false);
				}
				response.put(JsonKey.MESSAGE, result);
			}
			else
			{
				response.put(JsonKey.SUCCESS, false);
				response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
				statusCode = HttpStatus.UNAUTHORIZED;
			}
		} 
		catch (NoUserRegisteredException e) 
		{
			statusCode = HttpStatus.UNAUTHORIZED;
			response.put(JsonKey.SUCCESS, false);
			response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		String responseBody = response.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@PostMapping(path="/api/ussd**")
	public ResponseEntity<String> sendUSSD(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode;
		statusCode = HttpStatus.OK;
		JSONObject response = new JSONObject();
		try 
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				response = new JSONObject();
				Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
				String ussd = queryPairs.getOrDefault("ussd", "");
				String modemID = queryPairs.getOrDefault("modem_id", "");
				String message = "";
				if(ussd != null && !ussd.isEmpty())
				{
					try 
					{
						message = GSMUtil.executeUSSD(ussd, modemID);
						response.put(JsonKey.SUCCESS, true);		
					} 
					catch (GSMException e) 
					{
						message = e.getMessage();
						response.put(JsonKey.SUCCESS, false);	
					}		
				}
				response.put(JsonKey.MESSAGE, message);
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;
				response.put(JsonKey.SUCCESS, false);	
				response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
				
			}
		} 
		catch (JSONException e)
		{
			response.put(JsonKey.MESSAGE, e.getMessage());
		}
		catch(NoUserRegisteredException e)
		{
			statusCode = HttpStatus.UNAUTHORIZED;		
			response.put(JsonKey.MESSAGE, ConstantString.UNAUTHORIZED);
			response.put(JsonKey.SUCCESS, false);	
		}
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		String responseBody = response.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/send-token")
	public ResponseEntity<byte[]> sendTokenResetPassword1(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);	
		String userID = queryPairs.getOrDefault("userid", "");		
		return this.sendTokenResetPassword(userID);
	}

	private ResponseEntity<byte[]> sendTokenResetPassword(String userID) {
		byte[] responseBody = "".getBytes();
		HttpHeaders responseHeaders = new HttpHeaders();
		WebUserAccount.load(Config.getUserSettingPath());
		HttpStatus statusCode = HttpStatus.OK;
		try 
		{
			User user = WebUserAccount.getUser(userID);
			if(user.getUsername().isEmpty())
			{
				/**
				 * User not found
				 */
				user = WebUserAccount.getUserByPhone(userID);
				if(user.getUsername().isEmpty())
				{
					user = WebUserAccount.getUserByEmail(userID);
				}
			}
			if(!user.getUsername().isEmpty())
			{
				String phone = user.getPhone();
				String email = user.getEmail();
				if(!email.isEmpty() && userID.equalsIgnoreCase(email))
				{
					String message = "Username : "+user.getUsername()+"\r\nPassword : "+user.getPassword();
					ConfigEmail.load(Config.getEmailSettingPath());
					try 
					{
						MailUtil.send(email, "Account Information", message);
					} 
					catch (MessagingException | NoEmailAccountException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
				else if(!phone.isEmpty())
				{
					String message = "Username : "+user.getUsername()+"\r\nPassword : "+user.getPassword();
					try 
					{
						GSMUtil.sendSMS(phone, message);
					} 
					catch (GSMException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			responseHeaders.add(ConstantString.LOCATION, "/");
			responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
			statusCode = HttpStatus.MOVED_PERMANENTLY;
		} 
		catch (NoUserRegisteredException e1) 
		{
			responseHeaders.add(ConstantString.LOCATION, "/admin-init.html");
			responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
			statusCode = HttpStatus.MOVED_PERMANENTLY;
		}
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@GetMapping(path="/broadcast-message")
	public ResponseEntity<byte[]> broadcast(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;		
		String message = "";
		this.broardcastWebSocket(message);
		
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/api/reboot")
	public ResponseEntity<byte[]> reboot(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		JSONObject jo = new JSONObject();
		jo.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
		byte[] responseBody = jo.toString().getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		DeviceAPI.reboot();		
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/api/restart")
	public ResponseEntity<byte[]> restart(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		JSONObject jo = new JSONObject();
		jo.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
		byte[] responseBody = jo.toString().getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		DeviceAPI.restart();		
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/api/cleanup")
	public ResponseEntity<byte[]> cleanup(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		JSONObject jo = new JSONObject();
		jo.put(JsonKey.RESPONSE_CODE, ResponseCode.SUCCESS);
		byte[] responseBody = jo.toString().getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		DeviceAPI.cleanup();		
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	public void broardcastWebSocket(String message)
	{
		JSONObject messageJSON = new JSONObject();
		messageJSON.put(JsonKey.COMMAND, "broadcast-message");
		JSONArray data = new JSONArray();
		JSONObject itemData = new JSONObject();
		String uuid = UUID.randomUUID().toString();
		itemData.put(JsonKey.ID, uuid);
		itemData.put(JsonKey.MESSAGE, message);
		data.put(itemData);
		messageJSON.put(JsonKey.DATA, data);		
		ServerWebSocketManager.broadcast(messageJSON.toString(4));	
	}
	
	@PostMapping(path="/login.html")
	public ResponseEntity<byte[]> handleLogin(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
	    
	    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
	    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
	    String next = queryPairs.getOrDefault(JsonKey.NEXT, "");
	    
	    if(next.isEmpty())
		{
	    	next = "/";
		}
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
	    responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
	    
	    JSONObject res = new JSONObject();
	    JSONObject payload = new JSONObject();
	    
		cookie.setSessionValue(JsonKey.USERNAME, username);
		cookie.setSessionValue(JsonKey.PASSWORD, password);
		
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			WebUserAccount.load(Config.getUserSettingPath());
			if(WebUserAccount.checkUserAuth(username, password))
			{
				WebUserAccount.updateLastActive(username);
				WebUserAccount.save();
			    payload.put(JsonKey.NEXT_URL, next);
			    res.put(JsonKey.CODE, 0);
			    res.put(JsonKey.PAYLOAD, payload);
				responseBody = res.toString().getBytes();
			}
			else
			{
			    payload.put(JsonKey.NEXT_URL, "/");
			    res.put(JsonKey.CODE, 0);
			    res.put(JsonKey.PAYLOAD, payload);
				responseBody = res.toString().getBytes();				
			}
			cookie.saveSessionData();
			cookie.putToHeaders(responseHeaders);		
		}
		catch(NoUserRegisteredException e)
		{
		    payload.put(JsonKey.NEXT_URL, "/admin-init.html");
		    res.put(JsonKey.CODE, 0);
		    res.put(JsonKey.PAYLOAD, payload);
			responseBody = res.toString().getBytes();				
		}	
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	
	@GetMapping(path="/logout.html")
	public ResponseEntity<byte[]> handleLogout(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		
		byte[] responseBody = "".getBytes();
		cookie.destroySession();
		cookie.putToHeaders(responseHeaders);
		WebUserAccount.load(Config.getUserSettingPath());
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		responseHeaders.add(ConstantString.LOCATION, "/");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@GetMapping(path="/account/self")
	public ResponseEntity<byte[]> handleSelfAccount(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
				String list = WebUserAccount.getUser(loggedUsername).toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}		
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/gereral-setting/get")
	public ResponseEntity<byte[]> handleGeneralSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				String list = ConfigGeneral.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/smtp-setting/get")
	public ResponseEntity<byte[]> handleSMTPSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				String list = ConfigSMTP.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@GetMapping(path="/feeder-ws-setting/get")
	public ResponseEntity<byte[]> handleFeederWSSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigFeederWS.load(Config.getFeederWSSettingPath());
				String list = ConfigFeederWS.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@GetMapping(path="/log/list")
	public ResponseEntity<byte[]> handleLogFile(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				File directory = new File(Config.getLogDir());
				JSONArray list = FileUtil.listFile(directory);
				responseBody = list.toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	
	@GetMapping(path="/storage/content")
	public ResponseEntity<byte[]> handleLogStorage(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				System.out.println(Config.getStorageDir());
				File directory = new File(Config.getStorageDir());
				JSONArray list = FileUtil.listFile(directory);
				responseBody = list.toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/log/download/**")
	public ResponseEntity<byte[]> handleDownloadLogFile(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		String path = request.getServletPath();
		path = path.substring("/log/download".length());
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				String fullname = Config.getLogDir() + "/" + path;
				fullname = FileConfigUtil.fixFileName(fullname);	
				byte[] list = "".getBytes();
				try 
				{
					list = FileUtil.readResource(fullname);
					responseBody = list;
					String contentType = this.getMIMEType(path);
					String baseName = this.getBaseName(path);
					responseHeaders.add(ConstantString.CONTENT_TYPE, contentType);
					responseHeaders.add("Content-disposition", "attachment; filename=\""+baseName+"\"");
				} 
				catch (FileNotFoundException e) 
				{
					statusCode = HttpStatus.NOT_FOUND;
				}
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	@GetMapping(path="/report/sms/list")
	public ResponseEntity<byte[]> handleSMSLog(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				File directory = new File(Config.getSmsLogPath());
				JSONArray list = FileUtil.listFile(directory);
				responseBody = list.toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/report/sms/download/**")
	public ResponseEntity<byte[]> handleDownloadReportFile(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		String path = request.getServletPath();
		path = path.substring("/report/sms/download".length());
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				String fullname = Config.getSmsLogPath() + "/" + path;
				fullname = FileConfigUtil.fixFileName(fullname);	
				byte[] list = "".getBytes();
				try 
				{
					list = FileUtil.readResource(fullname);
					responseBody = list;
					String contentType = this.getMIMEType(path);
					String baseName = this.getBaseName(path);
					responseHeaders.add(ConstantString.CONTENT_TYPE, contentType);
					responseHeaders.add("Content-disposition", "attachment; filename=\""+baseName+"\"");
				} 
				catch (FileNotFoundException e) 
				{
					statusCode = HttpStatus.NOT_FOUND;
				}
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/block-list/list")
	public ResponseEntity<byte[]> handleBlockList(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigBlocking.load(Config.getBlockingSettingPath());
				String list = ConfigBlocking.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/feeder-amqp-setting/get")
	public ResponseEntity<byte[]> handleFeederAMQPSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigFeederAMQP.load(Config.getFeederAMQPSettingPath());
				String list = ConfigFeederAMQP.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/sms-setting/get")
	public ResponseEntity<byte[]> handleSMSSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigSMS.load(Config.getSmsSettingPath());
				String list = ConfigSMS.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/api-setting/get")
	public ResponseEntity<byte[]> handleAPISetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigAPI.load(Config.getApiSettingPath());
				String list = ConfigAPI.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	
	
	
	@GetMapping(path="/email-account/list")
	public ResponseEntity<byte[]> handleEmailAccount(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigEmail.load(Config.getEmailSettingPath());				
				responseBody = ConfigEmail.toJSONArray().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	
	@GetMapping(path="/email-account/detail/{id}")
	public ResponseEntity<byte[]> handleEmailAccountDetail(@RequestHeader HttpHeaders headers, @PathVariable(value=JsonKey.ID) String id, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigEmail.load(Config.getEmailSettingPath());				
				responseBody = ConfigEmail.getAccount(id).toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/network-dhcp-setting/get")
	public ResponseEntity<byte[]> handleDHCPSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigNetDHCP.load(Config.getDhcpSettingPath());		
				responseBody = ConfigNetDHCP.toJSONObject().toString().getBytes();
				
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}	
	
	@GetMapping(path="/network-wlan-setting/get")
	public ResponseEntity<byte[]> handleWLANSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigNetWLAN.load(Config.getWlanSettingPath());		
				responseBody = ConfigNetWLAN.toJSONObject().toString().getBytes();
				
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/network-ethernet-setting/get")
	public ResponseEntity<byte[]> handleEthernetSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigNetEthernet.load(Config.getEthernetSettingPath());
				responseBody = ConfigNetEthernet.toJSONObject().toString().getBytes();				
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/server-info/get")
	public ResponseEntity<byte[]> handleServerInfo(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				responseBody = ServerInfo.getInfo().getBytes();	
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/server-status/get")
	public ResponseEntity<byte[]> handleServerStatus(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				String timeStr = request.getParameter("time");
				long from = 0;
				long to = System.currentTimeMillis();
				if(timeStr.equals("1h"))
				{
					from = System.currentTimeMillis() - 3600000;
				}
				else if(timeStr.equals("2h"))
				{
					from = System.currentTimeMillis() - (3600000 * 2);
				}
				else if(timeStr.equals("3h"))
				{
					from = System.currentTimeMillis() - (3600000 * 3);
				}
				else if(timeStr.equals("6h"))
				{
					from = System.currentTimeMillis() - (3600000 * 6);
				}
				else if(timeStr.equals("12h"))
				{
					from = System.currentTimeMillis() - (3600000 * 12);
				}
				else if(timeStr.equals("24h"))
				{
					from = System.currentTimeMillis() - (3600000 * 24);
				}
				else if(timeStr.contains(","))
				{
					String[] tm = timeStr.split(",");
					from = Utility.atol(tm[0]);
					to = Utility.atol(tm[1]);
				}
				responseBody = ServerStatus.load(from, to).toString(4).getBytes();	
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/cloudflare/get")
	public ResponseEntity<byte[]> handleCloudflareSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigVendorCloudflare.load(Config.getCloudflareSettingPath());
				
				responseBody = ConfigVendorCloudflare.toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/noip/get")
	public ResponseEntity<byte[]> handleNoIPSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigVendorNoIP.load(Config.getNoIPSettingPath());				
				responseBody = ConfigVendorNoIP.toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	
	@GetMapping(path="/afraid/get")
	public ResponseEntity<byte[]> handleAfraidSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigVendorAfraid.load(Config.getAfraidSettingPath());				
				responseBody = ConfigVendorAfraid.toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	@GetMapping(path="/dynu/get")
	public ResponseEntity<byte[]> handleDynuSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigVendorDynu.load(Config.getDynuSettingPath());				
				responseBody = ConfigVendorDynu.toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/keystore/list")
	public ResponseEntity<byte[]> handleKeystoreList(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigKeystore.load(Config.getKeystoreSettingPath());				
				responseBody = ConfigKeystore.toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/keystore/detail/{id}")
	public ResponseEntity<byte[]> handleKeystoreDetail(@RequestHeader HttpHeaders headers, @PathVariable(value=JsonKey.ID) String id, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigKeystore.load(Config.getKeystoreSettingPath());				
				responseBody = ConfigKeystore.get(id).toJSONObject().toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/user/list")
	public ResponseEntity<byte[]> handleUserList(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				String list = WebUserAccount.listAsString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/user/detail/{username}")
	public ResponseEntity<byte[]> handleUserGet(@RequestHeader HttpHeaders headers, @PathVariable(value=JsonKey.USERNAME) String username, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				String data = WebUserAccount.getUser(username).toString();
				responseBody = data.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@GetMapping(path="/ddns-record/detail/{id}")
	public ResponseEntity<byte[]> handleDDNSRecordGet(@RequestHeader HttpHeaders headers, @PathVariable(value=JsonKey.ID) String id, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				String data = ConfigDDNS.getJSONObject(id).toString();
				responseBody = data.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/api-user/list")
	public ResponseEntity<byte[]> handleUserAPIList(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigAPIUser.load(Config.getUserAPISettingPath());
				String list = ConfigAPIUser.listAsString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@GetMapping(path="/api-user/detail/{username}")
	public ResponseEntity<byte[]> handleUserAPIGet(@RequestHeader HttpHeaders headers, @PathVariable(value=JsonKey.USERNAME) String username, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigAPIUser.load(Config.getUserAPISettingPath());
				String data = ConfigAPIUser.getUser(username).toString();
				responseBody = data.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@PostMapping(path="/user/init**")
	public ResponseEntity<byte[]> userInit(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		if(WebUserAccount.isEmpty())
		{
			Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);		
		    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "").trim();
		    String email = queryPairs.getOrDefault(JsonKey.EMAIL, "").trim();
		    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "").trim();
		    String name = queryPairs.getOrDefault(JsonKey.NAME, "").trim();
		    String phone = queryPairs.getOrDefault(JsonKey.PHONE, "").trim();

			if(!username.isEmpty() && !name.isEmpty() && !phone.isEmpty() && password.length() >= 6)
			{
			    JSONObject jsonObject = new JSONObject();
				jsonObject.put(JsonKey.USERNAME, username);
				jsonObject.put(JsonKey.NAME, name);
				jsonObject.put(JsonKey.EMAIL, email);
				jsonObject.put(JsonKey.PASSWORD, password);
				jsonObject.put(JsonKey.PHONE, phone);
				jsonObject.put(JsonKey.BLOCKED, false);
				jsonObject.put(JsonKey.ACTIVE, true);
				
				WebUserAccount.addUser(new User(jsonObject));		
				WebUserAccount.save();				
				
				cookie.setSessionValue(JsonKey.USERNAME, username);
				cookie.setSessionValue(JsonKey.PASSWORD, password);
				try
				{
					WebUserAccount.load(Config.getUserSettingPath());
					if(WebUserAccount.checkUserAuth(username, password))
					{
						WebUserAccount.updateLastActive(username);
						WebUserAccount.save();
					}
				}
				catch(NoUserRegisteredException e)
				{
					/**
					 * Do nothing
					 */
				}			
				cookie.saveSessionData();
				cookie.putToHeaders(responseHeaders);
				
			}		    
		}
		
		responseHeaders.add(ConstantString.LOCATION, ConstantString.ADMIN_FILE_LEVEL_3);
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/user/add**")
	public ResponseEntity<byte[]> userAdd(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);		
			    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
			    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
			    String email = queryPairs.getOrDefault(JsonKey.EMAIL, "");
			    String name = queryPairs.getOrDefault(JsonKey.NAME, "");
			    String phone = queryPairs.getOrDefault(JsonKey.PHONE, "");
		
			    JSONObject jsonObject = new JSONObject();
				jsonObject.put(JsonKey.USERNAME, username);
				jsonObject.put(JsonKey.NAME, name);
				jsonObject.put(JsonKey.EMAIL, email);
				jsonObject.put(JsonKey.PASSWORD, password);
				jsonObject.put(JsonKey.PHONE, phone);
				jsonObject.put(JsonKey.BLOCKED, false);
				jsonObject.put(JsonKey.ACTIVE, true);
				
				if(!username.isEmpty())
				{
					WebUserAccount.addUser(new User(jsonObject));		
					WebUserAccount.save();
				}		    
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		responseHeaders.add(ConstantString.LOCATION, ConstantString.ADMIN_FILE_LEVEL_3);
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/ddns-record/list")
	public ResponseEntity<byte[]> handleDDNSRecordList(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigDDNS.load(Config.getDdnsSettingPath());
				String list = ConfigDDNS.toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/firewall/list")
	public ResponseEntity<byte[]> handleFirewallList(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigFirewall.load(Config.getFirewallSettingPath());
				String list = ConfigFirewall.getRecords().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/modem/detail/{id}")
	public ResponseEntity<byte[]> handleModemGet(@RequestHeader HttpHeaders headers, @PathVariable(value=JsonKey.ID) String id, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigModem.load(Config.getModemSettingPath());
				String list = ConfigModem.getModemData(id).toJSONObject().toString();
				responseBody = list.getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/modem/list")
	public ResponseEntity<byte[]> handleModemSRecordList(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigModem.load(Config.getModemSettingPath());
				JSONObject modemInfo = ConfigModem.getStatus();				
				responseBody = modemInfo.toString().getBytes();
			}
			else
			{
				statusCode = HttpStatus.UNAUTHORIZED;			
			}
		}
		catch(NoUserRegisteredException e)
		{
			statusCode = HttpStatus.UNAUTHORIZED;
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	
	@PostMapping(path="/api-user/add**")
	public ResponseEntity<byte[]> userAPIAdd(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigAPIUser.load(Config.getUserAPISettingPath());
				Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);		
			    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
			    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
			    String email = queryPairs.getOrDefault(JsonKey.EMAIL, "");
			    String name = queryPairs.getOrDefault(JsonKey.NAME, "");
			    String phone = queryPairs.getOrDefault(JsonKey.PHONE, "");
		
			    JSONObject jsonObject = new JSONObject();
				jsonObject.put(JsonKey.USERNAME, username);
				jsonObject.put(JsonKey.NAME, name);
				jsonObject.put(JsonKey.EMAIL, email);
				jsonObject.put(JsonKey.PASSWORD, password);
				jsonObject.put(JsonKey.PHONE, phone);
				jsonObject.put(JsonKey.BLOCKED, false);
				jsonObject.put(JsonKey.ACTIVE, true);
				
				if(!username.isEmpty())
				{
					ConfigAPIUser.addUser(new User(jsonObject));		
					ConfigAPIUser.save();
				}		    
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		responseHeaders.add(ConstantString.LOCATION, ConstantString.API_USER_FILE_LEVEL_3);
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/user/update**")
	public ResponseEntity<byte[]> userUpdate(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);				
			    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
			    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
			    String name = queryPairs.getOrDefault(JsonKey.NAME, "");
			    String email = queryPairs.getOrDefault(JsonKey.EMAIL, "");
			    String phone = queryPairs.getOrDefault(JsonKey.PHONE, "");
			    boolean blocked = queryPairs.getOrDefault(JsonKey.BLOCKED, "").equals("1");
			    boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");
		
			    JSONObject jsonObject = new JSONObject();
				jsonObject.put(JsonKey.USERNAME, username);
				jsonObject.put(JsonKey.NAME, name);
				jsonObject.put(JsonKey.EMAIL, email);
				jsonObject.put(JsonKey.PHONE, phone);
				jsonObject.put(JsonKey.BLOCKED, blocked);
				jsonObject.put(JsonKey.ACTIVE, active);
				if(!username.isEmpty())
				{
					jsonObject.put(JsonKey.USERNAME, username);
				}
				if(!password.isEmpty())
				{
					jsonObject.put(JsonKey.PASSWORD, password);
				}
				WebUserAccount.updateUser(new User(jsonObject));		
				WebUserAccount.save();		    
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		responseHeaders.add(ConstantString.LOCATION, ConstantString.ADMIN_FILE_LEVEL_3);
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/api-user/update**")
	public ResponseEntity<byte[]> userAPIUpdate(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				ConfigAPIUser.load(Config.getUserAPISettingPath());
				Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);				
			    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");
			    String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
			    String name = queryPairs.getOrDefault(JsonKey.NAME, "");
			    String email = queryPairs.getOrDefault(JsonKey.EMAIL, "");
			    String phone = queryPairs.getOrDefault(JsonKey.PHONE, "");
			    boolean blocked = queryPairs.getOrDefault(JsonKey.BLOCKED, "").equals("1");
			    boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");
		
			    JSONObject jsonObject = new JSONObject();
				jsonObject.put(JsonKey.USERNAME, username);
				jsonObject.put(JsonKey.NAME, name);
				jsonObject.put(JsonKey.EMAIL, email);
				jsonObject.put(JsonKey.PHONE, phone);
				jsonObject.put(JsonKey.BLOCKED, blocked);
				jsonObject.put(JsonKey.ACTIVE, active);
				if(!username.isEmpty())
				{
					jsonObject.put(JsonKey.USERNAME, username);
				}
				if(!password.isEmpty())
				{
					jsonObject.put(JsonKey.PASSWORD, password);
				}
				ConfigAPIUser.updateUser(new User(jsonObject));		
				ConfigAPIUser.save();	
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		responseHeaders.add(ConstantString.LOCATION, ConstantString.API_USER_FILE_LEVEL_3);
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/user/remove**")
	public ResponseEntity<byte[]> userRemove(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{			
				Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);			
			    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");	
			    WebUserAccount.deleteUser(username);		
				WebUserAccount.save();
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		responseHeaders.add(ConstantString.LOCATION, ConstantString.ADMIN_FILE_LEVEL_3);
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/ddns-record/add**")
	public ResponseEntity<byte[]> ddnsAdd(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(WebUserAccount.checkUserAuth(headers))
			{
				Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);		
			    String provider = queryPairs.getOrDefault(JsonKey.PROVIDER, "").trim();
			    String zone = queryPairs.getOrDefault(JsonKey.ZONE, "").trim();
			    String recordName = queryPairs.getOrDefault(JsonKey.RECORD_NAME, "").trim();
			    String cronExpression = queryPairs.getOrDefault("cron_expression", "").trim();
			    boolean proxied = queryPairs.getOrDefault(JsonKey.PROXIED, "").trim().equals("1");
			    boolean forceCreateZone = queryPairs.getOrDefault(JsonKey.FORCE_CREATE_ZONE, "").trim().equals("1");
			    boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
			    
				String ttls = queryPairs.getOrDefault(JsonKey.TTL, "0");
			    int ttl = Utility.atoi(ttls);
			    String type = queryPairs.getOrDefault(JsonKey.TYPE, "0");
			    String id = Utility.md5(zone+":"+recordName);
				DDNSRecord record = new DDNSRecord(id, zone, recordName, type, proxied, ttl, forceCreateZone, provider, active, cronExpression);
				if(!zone.isEmpty() && !recordName.isEmpty())
				{
					ConfigDDNS.getRecords().put(id, record);	
					ConfigDDNS.save();
				}		    
			}
		}
		catch(NoUserRegisteredException e)
		{
			/**
			 * Do nothing
			 */
		}
		responseHeaders.add(ConstantString.LOCATION, ConstantString.DDNS_FILE_LEVEL_3);
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	
	@GetMapping(path="/**")
	public ResponseEntity<byte[]> handleDocumentRootGet(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		if(!Config.isValidDevice() && (request.getServletPath().contains(".html") || request.getServletPath().equals("/")))
		{
			return this.invalidDevice();
		}
		else
		{
			return this.serveDocumentRoot(headers, request);
		}
	}
	
	@PostMapping(path="/**")
	public ResponseEntity<byte[]> handleDocumentRootPost(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		this.processFeedbackPost(headers, requestBody, request);
		return this.serveDocumentRoot(headers, request);
	}

	private ResponseEntity<byte[]> invalidDevice() {
		String fileName = WebManagerTool.getFileName("/invalid-device.html");
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try 
		{
			responseBody = FileUtil.readResource(fileName);
		} 
		catch (FileNotFoundException e) 
		{
			statusCode = HttpStatus.NOT_FOUND;
			if(fileName.endsWith(ConstantString.EXT_HTML))
			{
				try 
				{
					responseBody = FileUtil.readResource(WebManagerTool.getFileName("/404.html"));
				} 
				catch (FileNotFoundException e1) 
				{
					logger.error(e1.getMessage());
				}
			}
		}
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-type", "text/html");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}


	public ResponseEntity<byte[]> serveDocumentRoot(HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;		
		String fileName = WebManagerTool.getFileName(request);
		byte[] responseBody = "".getBytes();
		try 
		{
			responseBody = FileUtil.readResource(fileName);
		} 
		catch (FileNotFoundException e) 
		{
			statusCode = HttpStatus.NOT_FOUND;
			if(fileName.endsWith(ConstantString.EXT_HTML))
			{
				try 
				{
					responseBody = FileUtil.readResource(WebManagerTool.getFileName("/404.html"));
				} 
				catch (FileNotFoundException e1) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());		
		WebManagerContent newContent = this.updateContent(fileName, responseHeaders, responseBody, statusCode, cookie);	
		
		responseBody = newContent.getResponseBody();
		responseHeaders = newContent.getResponseHeaders();
		statusCode = newContent.getStatusCode();
		String contentType = this.getMIMEType(fileName);
		
		responseHeaders.add(ConstantString.CONTENT_TYPE, contentType);
		
		if(fileName.endsWith(ConstantString.EXT_HTML))
		{
			cookie.saveSessionData();
		}
		else
		{
			int lifetime = this.getCacheLifetime(fileName);
			if(lifetime > 0)
			{
				responseHeaders.add(ConstantString.CACHE_CONTROL, "public, max-age="+lifetime+", immutable");				
			}
		}
		
		cookie.putToHeaders(responseHeaders);
		
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}	
	
	private int getCacheLifetime(String fileName) {
		int lifetime = 0;
		if(fileName.contains("."))
		{
			String[] arr = fileName.split("\\.");
			String ext = arr[arr.length - 1];
			String lt = PropertyLoader.getString("CACHE", ext, "0");
			lifetime = Utility.atoi(lt);
		}
		return lifetime;
	}
	
	private void processFeedbackPost(HttpHeaders headers, String requestBody, HttpServletRequest request) 
	{		
		try {
			if(WebUserAccount.checkUserAuth(headers))
			{
				CookieServer cookie = new CookieServer(headers, Config.getSessionName(), Config.getSessionLifetime());
				String path = request.getServletPath();
				if(path.equals("/keystore.html"))
				{
					this.processKeystore(requestBody);
				}
				if(path.equals("/keystore-update.html"))
				{
					this.processKeystore(requestBody);
				}
				if(path.equals("/admin.html"))
				{
					this.processAdmin(requestBody, cookie);
				}
				if(path.equals("/admin-update.html"))
				{
					this.processAdmin(requestBody, cookie);
				}
				if(path.equals("/account-update.html"))
				{
					this.processAccount(requestBody, cookie);
				}
				if(path.equals("/ddns-record.html"))
				{
					this.processDDNS(requestBody, cookie);
				}
				if(path.equals("/ddns-record-update.html"))
				{
					this.processDDNS(requestBody, cookie);
				}
				if(path.equals("/api-user.html"))
				{
					this.processAPIUser(requestBody);
				}
				if(path.equals("/api-user-update.html"))
				{
					this.processAPIUser(requestBody);
				}
				if(path.equals("/feeder-setting.html"))
				{
					this.processFeederSetting(requestBody);
				}
				if(path.equals("/sms-setting.html"))
				{
					this.processSMSSetting(requestBody);
				}
				if(path.equals("/modem.html") || path.equals("/modem-add.html") || path.equals("/modem-update.html"))
				{
					this.processModemSetting(requestBody);
				}
				if(path.equals("/email-account.html"))
				{
					this.processEmailAccount(requestBody);
				}
				if(path.equals("/sms.html"))
				{
					this.processSMS(requestBody);
				}
				if(path.equals("/api-setting.html"))
				{
					this.processAPISetting(requestBody);
				}
				if(path.equals("/general-setting.html"))
				{
					this.processGeneralSetting(requestBody);
				}
				if(path.equals("/date-time-setting.html"))
				{
					this.processDateTimeSetting(requestBody);
				}
				if(path.equals("/cloudflare.html"))
				{
					this.processCloudflareSetting(requestBody);
				}
				if(path.equals("/noip.html"))
				{
					this.processNoIPSetting(requestBody);
				}
				if(path.equals("/afraid.html"))
				{
					this.processAfraidSetting(requestBody);
				}
				if(path.equals("/dynu.html"))
				{
					this.processDynuSetting(requestBody);
				}
				if(path.equals("/network-setting.html"))
				{
					this.processNetworkSetting(requestBody);
				}
				if(path.equals("/block-list.html"))
				{
					this.processBlockList(requestBody);
				}
				if(path.equals("/firewall.html"))
				{
					this.processFirewall(requestBody);
				}
				if(path.equals("/logs.html"))
				{
					this.processDeleteLog(requestBody);
				}
				if(path.equals("/sms-report.html"))
				{
					this.processDeleteReport(requestBody);
				}
				if(path.equals("/smtp-setting.html"))
				{
					this.processSMTPSetting(requestBody);
				}
			}
		} 
		catch (NoUserRegisteredException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}
	
	private void processSMTPSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey("update"))
		{
			ConfigSMTP.load(Config.getSmtpSettingPath());
			String softwareName = queryPairs.getOrDefault("software_name", "").trim();
			String serverName = queryPairs.getOrDefault("server_name", "").trim();
			String serverAddress = queryPairs.getOrDefault("server_address", "").trim();
			String port = queryPairs.getOrDefault("server_port", "0").trim();
			int serverPort = Utility.atoi(port);
			boolean active = queryPairs.getOrDefault("active", "").trim().equals("1");
			ConfigSMTP.setSoftwareName(softwareName);
			ConfigSMTP.setServerName(serverName);
			ConfigSMTP.setServerAddress(serverAddress);
			ConfigSMTP.setServerPort(serverPort);
			ConfigSMTP.setActive(active);
			ConfigSMTP.save();
		}
	}
	
	private void processDeleteLog(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					String path = FileConfigUtil.removeParentWithDot("/"+value);
					String dir = Config.getLogDir();
					String fileName = FileConfigUtil.fixFileName(dir+path);
					File file = new File(fileName);
					try 
					{
						FileConfigUtil.deleteDirectoryWalkTree(file.toPath());
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void processDeleteReport(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					String path = FileConfigUtil.removeParentWithDot("/"+value);
					String dir = Config.getSmsLogPath();
					String fileName = FileConfigUtil.fixFileName(dir+path);
					File file = new File(fileName);
					try 
					{
						FileConfigUtil.deleteDirectoryWalkTree(file.toPath());
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void processFirewall(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		String portStr = queryPairs.getOrDefault("port", "0").trim();
		int port = Utility.atoi(portStr);
		String protocol = queryPairs.getOrDefault("protocol", "tcp").trim();
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			ConfigFirewall.load(Config.getFirewallSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigFirewall.remove(value);
				}
			}
			ConfigFirewall.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			ConfigFirewall.load(Config.getFirewallSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigFirewall.activate(value);
				}
			}
			ConfigFirewall.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			ConfigFirewall.load(Config.getFirewallSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigFirewall.deactivate(value);
				}
			}
			ConfigFirewall.save();
		}		
		if(queryPairs.containsKey(JsonKey.ADD))
		{
			ConfigFirewall.load(Config.getFirewallSettingPath());
			ConfigFirewall.add(port, protocol);
			ConfigFirewall.save();
		}
	}
	
	private void processBlockList(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		String msisdn = queryPairs.getOrDefault("msisdn", "").trim();
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			ConfigBlocking.load(Config.getBlockingSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigBlocking.remove(value);
				}
			}
			ConfigBlocking.save();
		}
		if(queryPairs.containsKey(JsonKey.BLOCK))
		{
			ConfigBlocking.load(Config.getBlockingSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					try 
					{
						ConfigBlocking.block(value);
					} 
					catch (GSMException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			ConfigBlocking.save();
		}
		if(queryPairs.containsKey(JsonKey.UNBLOCK))
		{
			ConfigBlocking.load(Config.getBlockingSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					try 
					{
						ConfigBlocking.unblock(value);
					} 
					catch (GSMException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			ConfigBlocking.save();
		}		
		if(queryPairs.containsKey(JsonKey.ADD))
		{
			ConfigBlocking.load(Config.getBlockingSettingPath());
			try 
			{
				ConfigBlocking.block(msisdn);
			} 
			catch (GSMException e) 
			{
				/**
				 * Do nothing
				 */
			}
			ConfigBlocking.save();
		}
	}
	
	private void processKeystore(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			ConfigKeystore.load(Config.getKeystoreSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigKeystore.remove(value);
				}
			}
			ConfigKeystore.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			ConfigKeystore.load(Config.getKeystoreSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigKeystore.deactivate(value);
				}
			}
			ConfigKeystore.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			ConfigKeystore.load(Config.getKeystoreSettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigKeystore.activate(value);
				}
			}
			ConfigKeystore.save();
		}
		if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			String id = queryPairs.getOrDefault("id", "");
			if(!id.isEmpty())
			{
				ConfigKeystore.load(Config.getKeystoreSettingPath());
				String fileName = queryPairs.getOrDefault("file_name", "").trim();
				String filePassword = queryPairs.getOrDefault("file_password", "").trim();
				boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
				JSONObject data = ConfigKeystore.get(id).toJSONObject();
				
				data.put("id", id);
				data.put("fileName", fileName);
				if(!filePassword.isEmpty())
				{
					data.put("filePassword", filePassword);
				}
				data.put(JsonKey.ACTIVE, active);
				
				ConfigKeystore.update(id, data);
				ConfigKeystore.save();
			}
		}
		
		if(queryPairs.containsKey(JsonKey.ADD))
		{
			String id = Utility.md5(String.format("%d", System.nanoTime()));
			String fileName = queryPairs.getOrDefault("file_name", "").trim();
			String filePassword = queryPairs.getOrDefault("file_password", "").trim();
			if(!fileName.isEmpty() && !filePassword.isEmpty())
			{
				boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
				JSONObject data = new JSONObject();
				
				String fileExtension = FileConfigUtil.getFileExtension(fileName);
				
				data.put("id", id);
				data.put("fileName", fileName);
				data.put("fileExtension", fileExtension);
				data.put("filePassword", filePassword);
				data.put(JsonKey.ACTIVE, active);
				byte[] binaryData = Utility.base64DecodeRaw(queryPairs.getOrDefault(JsonKey.DATA, ""));
				data.put("fileSize", binaryData.length);
				
				String fn = id + "." + fileExtension;
				
				ConfigKeystore.writeFile(Config.getKeystoreDataSettingPath(), fn, binaryData);
				ConfigKeystore.load(Config.getKeystoreSettingPath());
				ConfigKeystore.add(data);
				ConfigKeystore.save();
			}
		}
	}

	private void processSMSSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey("save_sms_setting"))
		{
			ConfigSMS.load(Config.getSmsSettingPath());
			boolean lSendIncommingSMS = queryPairs.getOrDefault("send_incomming_sms", "").trim().equals("1");
			String v1 = queryPairs.getOrDefault("incomming_interval", "0").trim();
			int lIncommingInterval = Utility.atoi(v1);
			
			String v2 = queryPairs.getOrDefault("time_range", "0").trim();
			int lTimeRange = Utility.atoi(v2);
			
			String v3 = queryPairs.getOrDefault("max_per_time_range", "0").trim();
			int lMaxPerTimeRange = Utility.atoi(v3);
			String countryCode = queryPairs.getOrDefault("country_code", "").trim();
			String prefix = queryPairs.getOrDefault("recipient_prefix_length", "0").trim();
			int recipientPrefixLength = Utility.atoi(prefix);
			boolean logSMS = queryPairs.getOrDefault("log_sms", "").trim().equals("1");
			
			ConfigSMS.setCountryCode(countryCode);
			ConfigSMS.setSendIncommingSMS(lSendIncommingSMS);
			ConfigSMS.setIncommingInterval(lIncommingInterval);
			ConfigSMS.setTimeRange(lTimeRange);
			ConfigSMS.setMaxPerTimeRange(lMaxPerTimeRange);
			ConfigSMS.setRecipientPrefixLength(recipientPrefixLength);
			ConfigSMS.setLogSMS(logSMS);
			
			ConfigSMS.save();
		}	
	}

	private void processDateTimeSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey("save_date_time_setting"))
		{
			ConfigGeneral.load(Config.getGeneralSettingPath());
			
			String deviceTime = queryPairs.getOrDefault("device_time", "").trim();
			String deviceTimeZone = queryPairs.getOrDefault("device_time_zone", "").trim();
			String ntpServer = queryPairs.getOrDefault("ntp_server", "").trim();
			String ntpUpdateInterval = queryPairs.getOrDefault("ntp_update_interval", "").trim();
			
			ConfigGeneral.setDeviceTimeZone(deviceTimeZone);
			ConfigGeneral.setNtpServer(ntpServer);
			ConfigGeneral.setNtpUpdateInterval(ntpUpdateInterval);

			ConfigGeneral.save();
			DeviceAPI.setTimeZone(deviceTimeZone);
			try 
			{
				Date date = Utility.stringToTime(deviceTime, "yyyy-MM-dd HH:mm:ss");
				DeviceAPI.setHardwareClock(date);
			} 
			catch (ParseException e) {
				/**
				 * Do nothing
				 */
			}
		}
	}

	private void processGeneralSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey("save_general_setting"))
		{
			ConfigGeneral.load(Config.getGeneralSettingPath());
			
			String deviceName2 = queryPairs.getOrDefault("device_name", "").trim();
			String deviceTimeZone = queryPairs.getOrDefault("device_time_zone", "").trim();
			String ntpServer = queryPairs.getOrDefault("ntp_server", "").trim();
			String ntpUpdateInterval = queryPairs.getOrDefault("ntp_update_interval", "").trim();
			
			ConfigGeneral.setDeviceName(deviceName2);
			ConfigGeneral.setDeviceTimeZone(deviceTimeZone);
			ConfigGeneral.setNtpServer(ntpServer);
			ConfigGeneral.setNtpUpdateInterval(ntpUpdateInterval);

			ConfigGeneral.save();
			DeviceAPI.setTimeZone(deviceTimeZone);
		}
	}

	private void processAPISetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey("save_api_setting"))
		{
			ConfigAPI.load(Config.getApiSettingPath());
			String v1 = queryPairs.getOrDefault("http_port", "0").trim();
			int lHttpPort = Utility.atoi(v1);
			
			String v2 = queryPairs.getOrDefault("https_port", "0").trim();
			int lHttpsPort = Utility.atoi(v2);

			boolean lHttpEnable = queryPairs.getOrDefault("http_enable", "").trim().equals("1");
			boolean lHttpsEnable = queryPairs.getOrDefault("https_enable", "").trim().equals("1");
	
			
			String lMessagePath = queryPairs.getOrDefault("message_path", "").trim();
			String lBlockingPath = queryPairs.getOrDefault("blocking_path", "").trim();
			String lUnblockingPath = queryPairs.getOrDefault("unblocking_path", "").trim();
			
			JSONObject config = new JSONObject();			
			config.put("httpPort", lHttpPort);
			config.put("httpsPort", lHttpsPort);

			config.put("httpEnable", lHttpEnable);
			config.put("httpsEnable", lHttpsEnable);

			config.put("messagePath", lMessagePath);
			config.put("blockingPath", lBlockingPath);
			config.put("unblockingPath", lUnblockingPath);
			
			ConfigAPI.save(Config.getApiSettingPath(), config);
		}
	}

	private void processNetworkSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey("save_dhcp"))
		{
			String domainName = queryPairs.getOrDefault("domainName", "").trim();
			String domainNameServersStr = queryPairs.getOrDefault("domainNameServers", "").trim();
			String ipRouter = queryPairs.getOrDefault("ipRouter", "").trim();
			String netmask = queryPairs.getOrDefault("netmask", "").trim();
			String subnetMask = queryPairs.getOrDefault("subnetMask", "").trim();
			String domainNameServersAddress = queryPairs.getOrDefault("domainNameServersAddress", "").trim();
			String defaultLeaseTime = queryPairs.getOrDefault("defaultLeaseTime", "").trim();
			String maxLeaseTime = queryPairs.getOrDefault("maxLeaseTime", "").trim();
			String ranges = queryPairs.getOrDefault("ranges", "").trim();
			
			JSONArray nsList = new JSONArray();
			
			String[] arr1 = domainNameServersStr.split("\\,");
			for(int i = 0; i<arr1.length; i++)
			{
				String str1 = arr1[i].trim();
				if(!str1.isEmpty())
				{
					nsList.put(str1);
				}
			}
			JSONArray rangeList = new JSONArray();
			String[] arr2 = ranges.split("\\,");
			for(int i = 0; i<arr2.length; i++)
			{
				String str2 = arr2[i].trim();
				if(!str2.isEmpty())
				{
					String[] arr3 = str2.split("\\-");
					String str3 = arr3[0].trim();
					String str4 = arr3[1].trim();
					if(!str3.isEmpty() && !str4.isEmpty())
					{
						JSONObject obj1 = new JSONObject();
						obj1.put("begin", str3);
						obj1.put("end", str4);
						rangeList.put(obj1);
					}
				}
			}
			
			ConfigNetDHCP.load(Config.getDhcpSettingPath());
			ConfigNetDHCP.setDomainName(domainName);
			ConfigNetDHCP.setIpRouter(ipRouter);
			ConfigNetDHCP.setNetmask(netmask);
			ConfigNetDHCP.setSubnetMask(subnetMask);
			ConfigNetDHCP.setDomainNameServersAddress(domainNameServersAddress);
			ConfigNetDHCP.setDefaultLeaseTime(defaultLeaseTime);
			ConfigNetDHCP.setMaxLeaseTime(maxLeaseTime);
			ConfigNetDHCP.setRanges(rangeList);
			ConfigNetDHCP.setDomainNameServers(nsList);
			ConfigNetDHCP.save();	
			ConfigNetDHCP.apply(Config.getOsDHCPConfigPath());
		}
		
		if(queryPairs.containsKey("save_wlan"))
		{
			ConfigNetWLAN.load(Config.getWlanSettingPath());
			ConfigNetWLAN.setEssid(queryPairs.getOrDefault("essid", "").trim());
			ConfigNetWLAN.setKey(queryPairs.getOrDefault("key", "").trim());
			ConfigNetWLAN.setKeyMgmt(queryPairs.getOrDefault("keyMgmt", "").trim());
			ConfigNetWLAN.setIpAddress(queryPairs.getOrDefault("ipAddress", "").trim());
			ConfigNetWLAN.setPrefix(queryPairs.getOrDefault("prefix", "").trim());
			ConfigNetWLAN.setNetmask(queryPairs.getOrDefault("netmask", "").trim());
			ConfigNetWLAN.setGateway(queryPairs.getOrDefault("gateway", "").trim());
			ConfigNetWLAN.setDns1(queryPairs.getOrDefault("dns1", "").trim());
			ConfigNetWLAN.save();
			ConfigNetWLAN.apply(Config.getOsWLANConfigPath(), Config.getOsSSIDKey());
		}

		if(queryPairs.containsKey("save_ethernet"))
		{
			ConfigNetEthernet.load(Config.getEthernetSettingPath());
			ConfigNetEthernet.setIpAddress(queryPairs.getOrDefault("ipAddress", "").trim());
			ConfigNetEthernet.setPrefix(queryPairs.getOrDefault("prefix", "").trim());
			ConfigNetEthernet.setNetmask(queryPairs.getOrDefault("netmask", "").trim());
			ConfigNetEthernet.setGateway(queryPairs.getOrDefault("gateway", "").trim());
			ConfigNetEthernet.setDns1(queryPairs.getOrDefault("dns1", "").trim());
			ConfigNetEthernet.setDns2(queryPairs.getOrDefault("dns2", "").trim());
			ConfigNetEthernet.save();
			ConfigNetEthernet.apply(Config.getOsEthernetConfigPath());
		}
	}

	private void processCloudflareSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		String endpoint = queryPairs.getOrDefault("endpoint", "").trim();
		String accountId = queryPairs.getOrDefault("account_id", "").trim();
		String authEmail = queryPairs.getOrDefault("auth_email", "").trim();
		String authApiKey = queryPairs.getOrDefault("auth_api_key", "").trim();
		String authToken = queryPairs.getOrDefault("auth_token", "").trim();
		
		if(!endpoint.isEmpty())
		{
			ConfigVendorCloudflare.load(Config.getCloudflareSettingPath());
			ConfigVendorCloudflare.setEndpoint(endpoint);
			ConfigVendorCloudflare.setAccountId(accountId);
			ConfigVendorCloudflare.setAuthEmail(authEmail);
			ConfigVendorCloudflare.setAuthApiKey(authApiKey);
			ConfigVendorCloudflare.setAuthToken(authToken);
			ConfigVendorCloudflare.save();
		}
	}
	
	private void processNoIPSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		String endpoint = queryPairs.getOrDefault("endpoint", "").trim();
		String username = queryPairs.getOrDefault("username", "").trim();
		String email = queryPairs.getOrDefault("email", "").trim();
		String password = queryPairs.getOrDefault("password", "").trim();
		String company = queryPairs.getOrDefault("company", "").trim();
		
		if(!endpoint.isEmpty())
		{
			ConfigVendorNoIP.load(Config.getNoIPSettingPath());
			ConfigVendorNoIP.setEndpoint(endpoint);
			ConfigVendorNoIP.setUsername(username);
			if(!password.isEmpty())
			{
				ConfigVendorNoIP.setPassword(password);
			}
			ConfigVendorNoIP.setCompany(company);
			ConfigVendorNoIP.setEmail(email);		
			ConfigVendorNoIP.save();
		}
	}
	
	private void processAfraidSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		String endpoint = queryPairs.getOrDefault("endpoint", "").trim();
		String username = queryPairs.getOrDefault("username", "").trim();
		String email = queryPairs.getOrDefault("email", "").trim();
		String password = queryPairs.getOrDefault("password", "").trim();
		String company = queryPairs.getOrDefault("company", "").trim();
		
		if(!endpoint.isEmpty())
		{
			ConfigVendorAfraid.load(Config.getAfraidSettingPath());
			ConfigVendorAfraid.setEndpoint(endpoint);
			ConfigVendorAfraid.setUsername(username);
			if(!password.isEmpty())
			{
				ConfigVendorAfraid.setPassword(password);
			}
			ConfigVendorAfraid.setCompany(company);
			ConfigVendorAfraid.setEmail(email);		
			ConfigVendorAfraid.save();
		}
	}
	
	private void processDynuSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		String apiVersion = queryPairs.getOrDefault("api_version", "").trim();
		String apiKey = queryPairs.getOrDefault("api_key", "").trim();
		String endpoint = queryPairs.getOrDefault("endpoint", "").trim();
		String username = queryPairs.getOrDefault("username", "").trim();
		String email = queryPairs.getOrDefault("email", "").trim();
		String password = queryPairs.getOrDefault("password", "").trim();
		String company = queryPairs.getOrDefault("company", "").trim();
		
		if(!endpoint.isEmpty())
		{
			ConfigVendorDynu.load(Config.getDynuSettingPath());
			ConfigVendorDynu.setEndpoint(endpoint);
			ConfigVendorDynu.setUsername(username);
			ConfigVendorDynu.setApiVersion(apiVersion);
			ConfigVendorDynu.setApiKey(apiKey);
			if(!password.isEmpty())
			{
				ConfigVendorDynu.setPassword(password);
			}
			ConfigVendorDynu.setCompany(company);
			ConfigVendorDynu.setEmail(email);		
			ConfigVendorDynu.save();
		}
	}
	
	private void processEmailAccount(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigEmail.deleteRecord(value);
				}
			}
			ConfigEmail.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigEmail.deactivate(value);
				}
			}
			ConfigEmail.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigEmail.activate(value);
				}
			}
			ConfigEmail.save();
		}
		if(queryPairs.containsKey("update"))
		{
			ConfigEmail.load(Config.getEmailSettingPath());
			
			String id = queryPairs.getOrDefault("id", "").trim();
			
			DataEmail dataEmail = ConfigEmail.getAccount(id);
			if(dataEmail == null)
			{
				dataEmail = new DataEmail();
			}		
			
			boolean auth = queryPairs.getOrDefault("mail_auth", "").trim().equals("1");
			String host = queryPairs.getOrDefault("smtp_host", "").trim();
	
			String v1 = queryPairs.getOrDefault("smtp_port", "0").trim();
			int port = Utility.atoi(v1);
			String senderAddress = queryPairs.getOrDefault("sender_address", "").trim();
			String senderPassword = queryPairs.getOrDefault("sender_password", "").trim();
			if(senderPassword.isEmpty())
			{
				senderPassword = dataEmail.getSenderPassword();
			}
			boolean ssl = queryPairs.getOrDefault("ssl", "").trim().equals("1");
			boolean startTLS = queryPairs.getOrDefault("start_tls", "").trim().equals("1");
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
			
			dataEmail.setId(id);
			dataEmail.setAuth(auth);;
			dataEmail.setHost(host);
			dataEmail.setPort(port);
			dataEmail.setSenderAddress(senderAddress);
			dataEmail.setSenderPassword(senderPassword);
			dataEmail.setSsl(ssl);
			dataEmail.setStartTLS(startTLS);
			dataEmail.setActive(active);
			
			ConfigEmail.put(dataEmail);
			ConfigEmail.save();
		}
		if(queryPairs.containsKey("add"))
		{
			ConfigEmail.load(Config.getEmailSettingPath());
			
			String id = Utility.md5(System.nanoTime()+"");
			
			DataEmail dataEmail = new DataEmail();
			
			boolean auth = queryPairs.getOrDefault("mail_auth", "").trim().equals("1");
			String host = queryPairs.getOrDefault("smtp_host", "").trim();
	
			String v1 = queryPairs.getOrDefault("smtp_port", "0").trim();
			int port = Utility.atoi(v1);
			String senderAddress = queryPairs.getOrDefault("sender_address", "").trim();
			String senderPassword = queryPairs.getOrDefault("sender_password", "").trim();

			boolean ssl = queryPairs.getOrDefault("ssl", "").trim().equals("1");
			boolean startTLS = queryPairs.getOrDefault("start_tls", "").trim().equals("1");
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");
			
			dataEmail.setId(id);
			dataEmail.setAuth(auth);;
			dataEmail.setHost(host);
			dataEmail.setPort(port);
			dataEmail.setSenderAddress(senderAddress);
			dataEmail.setSenderPassword(senderPassword);
			dataEmail.setSsl(ssl);
			dataEmail.setStartTLS(startTLS);
			dataEmail.setActive(active);
		
			ConfigEmail.add(dataEmail);
			ConfigEmail.save();
		}	
	}
	
	private void processModemSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		ConfigModem.load(Config.getModemSettingPath());
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigModem.deleteRecord(value);
				}
			}
			ConfigModem.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigModem.deactivate(value);
				}
			}
			ConfigModem.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigModem.activate(value);
				}
			}
			ConfigModem.save();
		}
		
		if(queryPairs.containsKey(JsonKey.ADD))
		{
			this.processModemUpdate(queryPairs, JsonKey.ADD);
		}	
		if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			this.processModemUpdate(queryPairs, JsonKey.UPDATE);
		}
		GSMUtil.updateConnectedDevice();
	}
	
	private void processModemUpdate(Map<String, String> queryPairs, String action) {		
		
		String id = queryPairs.getOrDefault("id", "").trim();		
		String connectionType = queryPairs.getOrDefault("connection_type", "").trim();
		boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").trim().equals("1");	
		boolean defaultModem = queryPairs.getOrDefault("default_modem", "").trim().equals("1");
		boolean smsAPI = queryPairs.getOrDefault("sms_api", "").trim().equals("1");

		String smsCenter = queryPairs.getOrDefault("sms_center", "").trim();		
		
		String incommingIntervalS = queryPairs.getOrDefault("incomming_interval", "0").trim();		
		int incommingInterval = Utility.atoi(incommingIntervalS);	
		
		String timeRangeS = queryPairs.getOrDefault("time_range", "").trim();		
		int timeRange = Utility.atoi(timeRangeS);

		String maxPerTimeRangeS = queryPairs.getOrDefault("maxPer_time_range", "0").trim();
		int maxPerTimeRange = Utility.atoi(maxPerTimeRangeS);

		String baudRateS = queryPairs.getOrDefault("baud_rate", "0").trim();
		int baudRate = Utility.atoi(baudRateS);

		String imei = queryPairs.getOrDefault("imei", "").trim();
		String msisdn = queryPairs.getOrDefault("msisdn", "").trim();
		String imsi = queryPairs.getOrDefault("imsi", "").trim();
		String name = queryPairs.getOrDefault("name", "").trim();
		String recipientPrefix = queryPairs.getOrDefault("recipient_prefix", "").trim();
		
		if(!recipientPrefix.isEmpty())
		{
			recipientPrefix = recipientPrefix.replace("\n", "\r\n");
			recipientPrefix = recipientPrefix.replace("\r\r\n", "\r\n");
			recipientPrefix = recipientPrefix.replace("\r", "\r\n");
			recipientPrefix = recipientPrefix.replace("\r\n\n", "\r\n");
			recipientPrefix = recipientPrefix.replace(" ", "");
			String[] arr = recipientPrefix.split(",");
			List<String> pref = new ArrayList<>();
			for(int i = 0; i<arr.length; i++)
			{
				if(!arr[i].isEmpty())
				{
					try 
					{
						pref.add(Utility.canonicalMSISDN(arr[i]));
					} 
					catch (GSMException e) 
					{
						/**
						 * Do nothing
						 */
					}
					
				}
			}
			recipientPrefix = String.join(",", pref);
		}	
		
		String simCardPIN = queryPairs.getOrDefault("sim_card_pin", "").trim();

		String parityBit = queryPairs.getOrDefault("parity_bit", "").trim();
		String startBits = queryPairs.getOrDefault("start_bits", "").trim();
		String stopBits = queryPairs.getOrDefault("stop_bits", "").trim();

		DataModem modem = ConfigModem.getModemData(id);
		if(action.equals(JsonKey.ADD) || id.isEmpty())
		{
			id = Utility.md5(String.format("%d", System.nanoTime()));
			modem.setId(id);
		}
		
		modem.setName(name);
		modem.setConnectionType(connectionType);
		modem.setSmsCenter(smsCenter);
		modem.setIncommingInterval(incommingInterval);
		modem.setTimeRange(timeRange);
		modem.setMaxPerTimeRange(maxPerTimeRange);
		modem.setImei(imei);
		modem.setMsisdn(msisdn);
		modem.setImsi(imsi);
		modem.setRecipientPrefix(recipientPrefix);
		if(!simCardPIN.isEmpty())
		{
			modem.setSimCardPIN(simCardPIN);
		}
		modem.setBaudRate(baudRate);
		modem.setParityBit(parityBit);
		modem.setStartBits(startBits);
		modem.setStopBits(stopBits);
		modem.setSmsAPI(smsAPI);
		modem.setDefaultModem(defaultModem);
		modem.setActive(active);

		ConfigModem.update(id, modem);
		ConfigModem.save();	
	}

	
	private void processFeederSetting(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey("save_feeder_ws_setting"))
		{
			ConfigFeederWS.load(Config.getFeederWSSettingPath());
			boolean feederWsEnable = queryPairs.getOrDefault("feeder_ws_enable", "").equals("1");		
			boolean feederWsSSL = queryPairs.getOrDefault("feeder_ws_ssl", "").equals("1");		
			String feederWsAddress = queryPairs.getOrDefault("feeder_ws_address", "");		
			String port = queryPairs.getOrDefault("feeder_ws_port", "0");
			int feederWsPort = Utility.atoi(port);
			String feederWsPath = queryPairs.getOrDefault("feeder_ws_path", "");		
			String feederWsUsername = queryPairs.getOrDefault("feeder_ws_username", "");		
			String feederWsPassword = queryPairs.getOrDefault("feeder_ws_password", "");		
			String feederWsChannel = queryPairs.getOrDefault("feeder_ws_channel", "");
			
			String timeout = queryPairs.getOrDefault("feeder_ws_timeout", "0");
			int feederWsTimeout = Utility.atoi(timeout);	
			String reconnect = queryPairs.getOrDefault("feeder_ws_reconnect_delay", "0");
			int feederWsReconnectDelay = Utility.atoi(reconnect);
			String refresh = queryPairs.getOrDefault("feeder_ws_refresh", "0");
			int feederWsRefresh = Utility.atoi(refresh);
			
			ConfigFeederWS.setFeederWsEnable(feederWsEnable);
			ConfigFeederWS.setFeederWsSSL(feederWsSSL);
			ConfigFeederWS.setFeederWsAddress(feederWsAddress);
			ConfigFeederWS.setFeederWsPort(feederWsPort);
			ConfigFeederWS.setFeederWsPath(feederWsPath);
			ConfigFeederWS.setFeederWsUsername(feederWsUsername);
			ConfigFeederWS.setFeederWsPassword(feederWsPassword);
			ConfigFeederWS.setFeederWsChannel(feederWsChannel);
			ConfigFeederWS.setFeederWsTimeout(feederWsTimeout);
			ConfigFeederWS.setFeederWsReconnectDelay(feederWsReconnectDelay);
			ConfigFeederWS.setFeederWsRefresh(feederWsRefresh);		
			
			ConfigFeederWS.save();
		}
		
		if(queryPairs.containsKey("save_feeder_amqp_setting"))
		{
			ConfigFeederAMQP.load(Config.getFeederAMQPSettingPath());
			boolean feederAmqpEnable = queryPairs.getOrDefault("feeder_amqp_enable", "").equals("1");		
			boolean feederAmqpSSL = queryPairs.getOrDefault("feeder_amqp_ssl", "").equals("1");		
			String feederAmqpAddress = queryPairs.getOrDefault("feeder_amqp_address", "");		
			String port = queryPairs.getOrDefault("feeder_amqp_port", "0");
			int feederAmqpPort = Utility.atoi(port);
			String feederAmqpPath = queryPairs.getOrDefault("feeder_amqp_path", "");		
			String feederAmqpUsername = queryPairs.getOrDefault("feeder_amqp_username", "");		
			String feederAmqpPassword = queryPairs.getOrDefault("feeder_amqp_password", "");		
			String feederAmqpChannel = queryPairs.getOrDefault("feeder_amqp_channel", "");
			
			String timeout = queryPairs.getOrDefault("feeder_amqp_timeout", "0");
			int feederAmqpTimeout = Utility.atoi(timeout);	
			String refresh = queryPairs.getOrDefault("feeder_amqp_refresh", "0");
			int feederAmqpRefresh = Utility.atoi(refresh);
			
			ConfigFeederAMQP.setFeederAmqpEnable(feederAmqpEnable);
			ConfigFeederAMQP.setFeederAmqpSSL(feederAmqpSSL);
			ConfigFeederAMQP.setFeederAmqpAddress(feederAmqpAddress);
			ConfigFeederAMQP.setFeederAmqpPort(feederAmqpPort);
			ConfigFeederAMQP.setFeederAmqpPath(feederAmqpPath);
			ConfigFeederAMQP.setFeederAmqpUsername(feederAmqpUsername);
			ConfigFeederAMQP.setFeederAmqpPassword(feederAmqpPassword);
			ConfigFeederAMQP.setFeederAmqpChannel(feederAmqpChannel);
			ConfigFeederAMQP.setFeederAmqpTimeout(feederAmqpTimeout);
			ConfigFeederAMQP.setFeederAmqpRefresh(feederAmqpRefresh);		

			ConfigFeederAMQP.save();			
		}		
	}
	
	private void processSMS(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey("send"))
		{
			String receiver = queryPairs.getOrDefault(JsonKey.RECEIVER, "").trim();			
			String message = queryPairs.getOrDefault(JsonKey.MESSAGE, "").trim();	
			String modemID = queryPairs.getOrDefault("modem", "").trim();	
			if(!receiver.isEmpty() && !message.isEmpty())
			{
				try 
				{
					GSMUtil.sendSMS(receiver, message, modemID);
					String modemName = GSMUtil.getModemName(modemID);
					this.broardcastWebSocket("Sending a message to "+receiver+" via "+modemName);
				} 
				catch (GSMException e) 
				{
					this.broardcastWebSocket(e.getMessage());
				}
			}
		}		
	}
	
	private void processAccount(String requestBody, CookieServer cookie) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
		String phone = queryPairs.getOrDefault(JsonKey.PHONE, "");
		String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "");
		String email = queryPairs.getOrDefault(JsonKey.EMAIL, "");
		String name = queryPairs.getOrDefault(JsonKey.NAME, "");
		if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			User user;
			try 
			{
				user = WebUserAccount.getUser(loggedUsername);
				user.setName(name);
				user.setPhone(phone);
				user.setEmail(email);
				if(!password.isEmpty())
				{
					user.setPassword(password);
				}
				WebUserAccount.updateUser(user);
				WebUserAccount.save();
			} 
			catch (NoUserRegisteredException e) 
			{
				/**
				 * Do nothing
				 */
			}
		}		
	}
	
	private void processAdmin(String requestBody, CookieServer cookie) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			/**
			 * Delete
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id[") && !value.equals(loggedUsername))
				{
					WebUserAccount.deleteUser(value);
				}
			}
			WebUserAccount.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id[") && !value.equals(loggedUsername))
				{
					try 
					{
						WebUserAccount.deactivate(value);
					} 
					catch (NoUserRegisteredException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			WebUserAccount.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					try 
					{
						WebUserAccount.activate(value);
					} 
					catch (NoUserRegisteredException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			WebUserAccount.save();
		}
		if(queryPairs.containsKey("block"))
		{
			/**
			 * Block
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id[") && !value.equals(loggedUsername))
				{
					try 
					{
						WebUserAccount.block(value);
					} 
					catch (NoUserRegisteredException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			WebUserAccount.save();		
		}
		if(queryPairs.containsKey("unblock"))
		{
			/**
			 * Unblock
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					try 
					{
						WebUserAccount.unblock(value);
					} 
					catch (NoUserRegisteredException e) 
					{
						/**
						 * Do nothing
						 */
					}
				}
			}
			WebUserAccount.save();
		}
		if(queryPairs.containsKey("update-data"))
		{
			String pkID = queryPairs.getOrDefault("pk_id", "");
			String field = queryPairs.getOrDefault("field", "");
			String value = queryPairs.getOrDefault("value", "");
			if(!field.equals(JsonKey.USERNAME))
			{
				User user;
				try 
				{
					user = WebUserAccount.getUser(pkID);
					if(field.equals(JsonKey.PHONE))
					{
						user.setPhone(value);
					}
					if(field.equals(JsonKey.NAME))
					{
						user.setName(value);
					}
					WebUserAccount.updateUser(user);
					WebUserAccount.save();
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			String username = queryPairs.getOrDefault(JsonKey.USERNAME, "").trim();
			String name = queryPairs.getOrDefault(JsonKey.NAME, "").trim();
			String phone = queryPairs.getOrDefault(JsonKey.PHONE, "").trim();
			String email = queryPairs.getOrDefault(JsonKey.EMAIL, "").trim();
			String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "").trim();
			boolean blocked = queryPairs.getOrDefault(JsonKey.BLOCKED, "").equals("1");
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");

			if(!username.isEmpty())
			{
				User user;
				try 
				{
					user = WebUserAccount.getUser(username);
					if(user.getUsername().isEmpty())
					{
						user.setUsername(username);
					}
					if(!name.isEmpty())
					{
						user.setName(name);
					}
					user.setPhone(phone);
					user.setEmail(email);
					if(!password.isEmpty())
					{
						user.setPassword(password);
					}
					user.setBlocked(blocked);
					user.setActive(active);
					WebUserAccount.updateUser(user);
					WebUserAccount.save();
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
	}
	private void processAPIUser(String requestBody) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			/**
			 * Delete
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String value = entry.getValue();
				ConfigAPIUser.deleteUser(value);
			}
			ConfigAPIUser.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigAPIUser.deactivate(value);
				}
			}
			ConfigAPIUser.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigAPIUser.activate(value);
				}
			}
			ConfigAPIUser.save();
		}
		if(queryPairs.containsKey("block"))
		{
			/**
			 * Block
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigAPIUser.block(value);
				}
			}
			ConfigAPIUser.save();
			
		}
		if(queryPairs.containsKey("unblock"))
		{
			/**
			 * Unblock
			 */
			ConfigAPIUser.load(Config.getUserAPISettingPath());
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigAPIUser.unblock(value);
				}
			}
			ConfigAPIUser.save();
		}
		if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			String username = queryPairs.getOrDefault(JsonKey.USERNAME, "").trim();
			String name = queryPairs.getOrDefault(JsonKey.NAME, "").trim();
			String phone = queryPairs.getOrDefault(JsonKey.PHONE, "").trim();
			String email = queryPairs.getOrDefault(JsonKey.EMAIL, "").trim();
			String password = queryPairs.getOrDefault(JsonKey.PASSWORD, "").trim();
			boolean blocked = queryPairs.getOrDefault(JsonKey.BLOCKED, "").equals("1");
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");

			if(!username.isEmpty())
			{
				ConfigAPIUser.load(Config.getUserAPISettingPath());
				User user;
				user = ConfigAPIUser.getUser(username);
				if(user.getUsername().isEmpty())
				{
					user.setUsername(username);
				}
				if(!name.isEmpty())
				{
					user.setName(name);
				}
				user.setPhone(phone);
				user.setEmail(email);
				if(!password.isEmpty())
				{
					user.setPassword(password);
				}
				user.setBlocked(blocked);
				user.setActive(active);
				ConfigAPIUser.updateUser(user);
				ConfigAPIUser.save();
			}
		}
	}

	private void processDDNS(String requestBody, CookieServer cookie) {
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);
		if(queryPairs.containsKey(JsonKey.DELETE))
		{
			/**
			 * Delete
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigDDNS.deleteRecord(value);
				}
			}
			ConfigDDNS.save();
		}
		if(queryPairs.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigDDNS.deactivate(value);
				}
			}
			ConfigDDNS.save();
		}
		if(queryPairs.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigDDNS.activate(value);
				}
			}
			ConfigDDNS.save();
		}
		if(queryPairs.containsKey(JsonKey.PROXIED))
		{
			/**
			 * Proxied
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigDDNS.proxied(value);
				}
			}
			ConfigDDNS.save();
		}
		if(queryPairs.containsKey(JsonKey.UNPROXIED))
		{
			/**
			 * Unproxied
			 */
			for (Map.Entry<String, String> entry : queryPairs.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id["))
				{
					ConfigDDNS.unproxied(value);
				}
			}
			ConfigDDNS.save();
		}
		if(queryPairs.containsKey(JsonKey.UPDATE))
		{
			String id = queryPairs.getOrDefault(JsonKey.ID, "").trim();
			String provider = queryPairs.getOrDefault(JsonKey.PROVIDER, "").trim();
			String zone = queryPairs.getOrDefault(JsonKey.ZONE, "").trim();
			String recordName = queryPairs.getOrDefault(JsonKey.RECORD_NAME, "").trim();
			String ttls = queryPairs.getOrDefault(JsonKey.TTL, "").trim();
			String cronExpression = queryPairs.getOrDefault(JsonKey.CRON_EXPRESSION, "").trim();
			boolean proxied = queryPairs.getOrDefault(JsonKey.PROXIED, "").equals("1");
			boolean forceCreateZone = queryPairs.getOrDefault(JsonKey.FORCE_CREATE_ZONE, "").equals("1");
			boolean active = queryPairs.getOrDefault(JsonKey.ACTIVE, "").equals("1");
			String type = queryPairs.getOrDefault(JsonKey.TYPE, "0");
			int ttl = Utility.atoi(ttls);
			
			if(!id.isEmpty())
			{
				DDNSRecord record = ConfigDDNS.getRecords().getOrDefault(id, new DDNSRecord());
				if(!id.isEmpty())
				{
					record.setId(id);
				}
				if(!zone.isEmpty())
				{
					record.setZone(zone);
				}
				if(!recordName.isEmpty())
				{
					record.setRecordName(recordName);
				}
				record.setProvider(provider);
				record.setProxied(proxied);
				record.setForceCreateZone(forceCreateZone);
				record.setCronExpression(cronExpression);
				record.setTtl(ttl);
				record.setActive(active);		
				record.setType(type);
				ConfigDDNS.updateRecord(record);
				ConfigDDNS.save();
			}
		}
	}

	private String getMIMEType(String fileName) 
	{
		String[] arr = fileName.split("\\.");	
		String ext = arr[arr.length - 1];
		return PropertyLoader.getString("MIME", ext, "");
	}
	
	private String getBaseName(String fileName) 
	{
		String[] arr = fileName.split("\\/");	
		if(arr.length > 1)
		{
			return arr[arr.length - 1];
		}
		else
		{
			return fileName;
		}
	}
	
	private WebManagerContent updateContent(String fileName, HttpHeaders responseHeaders, byte[] responseBody, HttpStatus statusCode, CookieServer cookie) 
	{
		String contentType = this.getMIMEType(fileName);
		WebManagerContent webContent = new WebManagerContent(fileName, responseHeaders, responseBody, statusCode, cookie, contentType);
		boolean requireLogin = false;
		String fileSub = "";
		
		if(fileName.toLowerCase().endsWith(ConstantString.EXT_HTML))
		{
			JSONObject authFileInfo = WebManagerTool.processAuthFile(responseBody);
			requireLogin = authFileInfo.optBoolean(JsonKey.CONTENT, false);
			fileSub = WebManagerTool.getFileName(authFileInfo.optString("data-file", ""));
		}
		
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		if(requireLogin)
		{
			responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
			webContent.setResponseHeaders(responseHeaders);
			try
			{
				if(!WebUserAccount.checkUserAuth(username, password))	
				{
					try 
					{
						responseBody = FileUtil.readResource(fileSub);
						return this.updateContent(fileSub, responseHeaders, responseBody, statusCode, cookie);
					} 
					catch (FileNotFoundException e) 
					{
						statusCode = HttpStatus.NOT_FOUND;
						webContent.setStatusCode(statusCode);
					}	
				}
				responseBody = WebManagerTool.removeMeta(responseBody);
			}
			catch(NoUserRegisteredException e)
			{
				/**
				 * Do nothing
				 */
				statusCode = HttpStatus.PERMANENT_REDIRECT;
				webContent.setStatusCode(statusCode);
				
				responseHeaders.add(ConstantString.LOCATION, ConstantString.ADMIN_INIT);
				webContent.setResponseHeaders(responseHeaders);
				
				responseBody = "".getBytes();
			}
			webContent.setResponseBody(responseBody);
		}
		return webContent;
	}
}

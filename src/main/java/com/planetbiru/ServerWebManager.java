package com.planetbiru;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
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

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigCloudflare;
import com.planetbiru.config.ConfigEmail;
import com.planetbiru.config.ConfigSaved;
import com.planetbiru.config.ConfigDDNS;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.config.ConfigNetDHCP;
import com.planetbiru.config.ConfigNetEthernet;
import com.planetbiru.config.ConfigNetWLAN;
import com.planetbiru.config.ConfigSMS;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.ddns.DDNSRecord;
import com.planetbiru.gsm.GSMNullException;
import com.planetbiru.gsm.SMSUtil;
import com.planetbiru.receiver.ws.WebSocketContent;
import com.planetbiru.user.APIUserAccount;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.User;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.MailUtil;
import com.planetbiru.util.ServerInfo;
import com.planetbiru.util.Utility;

@RestController
public class ServerWebManager {
	
	private ConfigSaved configSaved = new ConfigSaved();
	private Logger logger = LogManager.getLogger(ServerWebManager.class);	
	
	private WebUserAccount userAccount;
	private WebUserAccount userAPIAccount;

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

	@Value("${otpbroker.ws.endpoint}")
	private String wsClientEndpoint;

	@Value("${otpbroker.ws.username}")
	private String wsClientUsername;

	@Value("${otpbroker.ws.password}")
	private String wsClientPassword;

	@Value("${otpbroker.web.session.name}")
	private String sessionName;

	@Value("${otpbroker.web.session.lifetime}")
	private int cacheLifetime;

	@Value("${otpbroker.web.document.root}")
	private String documentRoot;

	@Value("${otpbroker.path.setting.feeder.ws}")
	private String feederWSSettingPath;

	@Value("${otpbroker.path.setting.feeder.amqp}")
	private String feederAMQPSettingPath;

	@Value("${otpbroker.path.setting.sms}")
	private String smsSettingPath;
	
	@Value("${otpbroker.path.setting.all}")
	private String mimeSettingPath;	
	
	@Value("${otpbroker.path.setting.user}")
	private String userSettingPath;

	@Value("${otpbroker.path.setting.api}")
	private String userAPISettingPath;

	@Value("${otpbroker.path.setting.email}")
	private String emailSettingPath;

	@Value("${otpbroker.device.connection.type}")
	private String portName;
	
	@Value("${otpbroker.path.setting.ddns}")
	private String ddnsSettingPath;
	
	@Value("${otpbroker.path.setting.cloudflare}")
	private String cloudflareSettingPath;

	@Value("${otpbroker.path.setting.dhcp}")
	private String dhcpSettingPath;

	@Value("${otpbroker.path.setting.wlan}")
	private String wlanSettingPath;

	@Value("${otpbroker.path.setting.ethernet}")
	private String ethernetSettingPath;

	
	private ServerWebManager()
    {
    }
	
	
	@PostConstruct
	public void init()
	{
		ConfigDDNS.load(ddnsSettingPath);
		ConfigCloudflare.load(cloudflareSettingPath);

		logger.info("Init...");	
		Config.setPortName(portName);		
		userAccount = new WebUserAccount(userSettingPath);		
		userAPIAccount = new WebUserAccount(userAPISettingPath);		
		this.loadConfigEmail();		
		this.initSerial();		
		
		try 
		{
			configSaved = new ConfigSaved(mimeSettingPath);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();			
		}
	}
	
	private void loadConfigEmail()
	{
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
		ConfigEmail.load(emailSettingPath);
	}
	
	
	
	private void initSerial() 
	{
		String port = Config.getPortName();
		SMSUtil.init(port);
	}	
	
	@PostMapping(path="/send-token")
	public ResponseEntity<byte[]> sendTokenResetPassword1(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);	
		String userID = queryPairs.getOrDefault("userid", "");		
		return this.sendTokenResetPassword(userID);
	}
	
	@GetMapping(path="/send-token")
	public ResponseEntity<byte[]> sendTokenResetPassword2(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{	
		String userID = request.getParameter("userid");
		return this.sendTokenResetPassword(userID);
	}
	
	private ResponseEntity<byte[]> sendTokenResetPassword(String userID) {
		byte[] responseBody = "".getBytes();
		HttpHeaders responseHeaders = new HttpHeaders();
		userAccount.load();
		HttpStatus statusCode = HttpStatus.OK;
		try 
		{
			User user = userAccount.getUser(userID);
			if(user.getUsername().isEmpty())
			{
				/**
				 * User not found
				 */
				user = userAccount.getUserByPhone(userID);
				if(user.getUsername().isEmpty())
				{
					user = userAccount.getUserByEmail(userID);
				}
			}
			if(!user.getUsername().isEmpty())
			{
				String phone = user.getPhone();
				String email = user.getEmail();
				if(!email.isEmpty() && userID.equalsIgnoreCase(email))
				{
					String message = "Username : "+user.getUsername()+"\r\nPassword : "+user.getPassword();
					String smtpHost = ConfigEmail.getMailHost();
					int smtpPort = ConfigEmail.getMailPort();
				    String smtpUser = ConfigEmail.getMailSenderAddress();
				    String smtpPassword = ConfigEmail.getMailSenderPassword();
				    boolean ssl = ConfigEmail.isMailSSL();
				    boolean starttls = ConfigEmail.isMailStartTLS();   
				    boolean debug = false;
					
					MailUtil senEmail = new MailUtil(smtpHost, smtpPort, smtpUser, smtpPassword, ssl, starttls, debug);
					try 
					{
						senEmail.send(email, "Account Information", message);
					} 
					catch (MessagingException e) 
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
						SMSUtil.sendSMS(phone, message);
					} 
					catch (GSMNullException e) 
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
		
		String message = Utility.date("yyyy-MM-dd HH:mm:ss.SSS")+" This page uses the non standard property “zoom”. Consider using calc() in the relevant property values, or using “transform” along with “transform-origin: 0 0...";
		this.broardcastWebSocket(message);
		
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/restart")
	public ResponseEntity<byte[]> restart(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		ServerApplication.restart();
		
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
		messageJSON.put("data", data);		
		ServerWebSocketManager.broadcast(messageJSON.toString(4));	
	}
	
	@PostMapping(path="/login.html")
	public ResponseEntity<byte[]> handleLogin(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		
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
			userAccount.load();
			if(userAccount.checkUserAuth(username, password))
			{
				userAccount.updateLastActive(username);
				userAccount.save();
			    payload.put(JsonKey.NEXT_URL, next);
			    res.put("code", 0);
			    res.put(JsonKey.PAYLOAD, payload);
				responseBody = res.toString().getBytes();
			}
			else
			{
			    payload.put(JsonKey.NEXT_URL, "/");
			    res.put("code", 0);
			    res.put(JsonKey.PAYLOAD, payload);
				responseBody = res.toString().getBytes();				
			}
			cookie.saveSessionData();
			cookie.putToHeaders(responseHeaders);
			
		}
		catch(NoUserRegisteredException e)
		{
		    payload.put(JsonKey.NEXT_URL, "/admin-init.html");
		    res.put("code", 0);
		    res.put(JsonKey.PAYLOAD, payload);
			responseBody = res.toString().getBytes();				
		}	
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/logout.html")
	public ResponseEntity<byte[]> handleLogout(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		
		byte[] responseBody = "".getBytes();
		cookie.destroySession();
		cookie.putToHeaders(responseHeaders);
		userAccount.load();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		responseHeaders.add(ConstantString.LOCATION, "/");
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}

	@GetMapping(path="/account/self")
	public ResponseEntity<byte[]> handleSelfAccount(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
				String list = userAccount.getUser(loggedUsername).toString();
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
	
	@GetMapping(path="/feeder-ws-setting/get")
	public ResponseEntity<byte[]> handleFeederWSSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				ConfigFeederWS.load(feederWSSettingPath);
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
	
	@GetMapping(path="/feeder-amqp-setting/get")
	public ResponseEntity<byte[]> handleFeederAMQPSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				ConfigFeederAMQP.load(feederAMQPSettingPath);
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				ConfigSMS.load(smsSettingPath);
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
	
	
	
	@GetMapping(path="/email-setting/get")
	public ResponseEntity<byte[]> handleEmailSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				ConfigEmail.load(emailSettingPath);				
				responseBody = ConfigEmail.getJSONObject().toString().getBytes();
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				ConfigNetDHCP.load(dhcpSettingPath);		
				responseBody = ConfigNetDHCP.getJSONObject().toString().getBytes();
				
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				ConfigNetWLAN.load(wlanSettingPath);;		
				responseBody = ConfigNetWLAN.getJSONObject().toString().getBytes();
				
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				ConfigNetEthernet.load(ethernetSettingPath);;		
				responseBody = ConfigNetEthernet.getJSONObject().toString().getBytes();				
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
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
	
	
	@GetMapping(path="/cloudflare/get")
	public ResponseEntity<byte[]> handleCloudflareSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				ConfigCloudflare.load(emailSettingPath);
				
				responseBody = ConfigCloudflare.getJSONObject().toString().getBytes();
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				String list = userAccount.listAsString();
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				String data = userAccount.getUser(username).toString();
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				String list = userAPIAccount.listAsString();
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				String data = userAPIAccount.getUser(username).toString();
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		if(userAccount.isEmpty())
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
				
				userAccount.addUser(new User(jsonObject));		
				userAccount.save();				
				
				cookie.setSessionValue(JsonKey.USERNAME, username);
				cookie.setSessionValue(JsonKey.PASSWORD, password);
				try
				{
					userAccount.load();
					if(userAccount.checkUserAuth(username, password))
					{
						userAccount.updateLastActive(username);
						userAccount.save();
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(userAccount.checkUserAuth(headers))
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
					userAccount.addUser(new User(jsonObject));		
					userAccount.save();
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				ConfigDDNS.load(ddnsSettingPath);
				String list = ConfigDDNS.getJSONObject().toString();
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
	
	
	@PostMapping(path="/api-user/add**")
	public ResponseEntity<byte[]> userAPIAdd(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(userAccount.checkUserAuth(headers))
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
					userAPIAccount.addUser(new User(jsonObject));		
					userAPIAccount.save();
					APIUserAccount.update(userAPIAccount.toJSONObject().toString());
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(userAccount.checkUserAuth(headers))
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
				userAccount.updateUser(new User(jsonObject));		
				userAccount.save();		    
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(userAccount.checkUserAuth(headers))
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
				userAPIAccount.updateUser(new User(jsonObject));		
				userAPIAccount.save();	
				APIUserAccount.update(userAPIAccount.toJSONObject().toString());
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{			
				Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);			
			    String username = queryPairs.getOrDefault(JsonKey.USERNAME, "");	
			    userAccount.deleteUser(username);		
				userAccount.save();
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
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.MOVED_PERMANENTLY;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				Map<String, String> queryPairs = Utility.parseURLEncoded(requestBody);		
			    String provider = queryPairs.getOrDefault(JsonKey.PROVIDER, "").trim();
			    String zone = queryPairs.getOrDefault(JsonKey.ZONE, "").trim();
			    String recordName = queryPairs.getOrDefault(JsonKey.RECORD_NAME, "").trim();
			    String cronExpression = queryPairs.getOrDefault("cron_expression", "").trim();
			    boolean proxied = queryPairs.getOrDefault(JsonKey.PROXIED, "").trim().equals("1");
			    boolean forceCreateZone = queryPairs.getOrDefault(JsonKey.FORCE_CREATE_ZONE, "").trim().equals("1");
			    boolean active = queryPairs.getOrDefault("active", "").trim().equals("1");
			    
				String ttls = queryPairs.getOrDefault(JsonKey.TTL, "0");
			    int ttl = Utility.atoi(ttls);
			    String type = "A";
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
		return this.serveDocumentRoot(headers, request);
	}
	
	@PostMapping(path="/**")
	public ResponseEntity<byte[]> handleDocumentRootPost(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{
		this.processFeedbackPost(headers, requestBody, request);
		return this.serveDocumentRoot(headers, request);
	}
	
	public ResponseEntity<byte[]> serveDocumentRoot(HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		
		String fileName = this.getFileName(request);
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
					responseBody = FileUtil.readResource(this.getFileName("/404.html"));
				} 
				catch (FileNotFoundException e1) 
				{
					e1.printStackTrace();
				}
			}
		}
		CookieServer cookie = new CookieServer(headers);		
		WebSocketContent newContent = this.updateContent(fileName, responseHeaders, responseBody, statusCode, cookie);	
		
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
			String lt = configSaved.getString("CACHE", ext, "0");
			lifetime = Utility.atoi(lt);
		}
		return lifetime;
	}

	
	private void processFeedbackPost(HttpHeaders headers, String requestBody, HttpServletRequest request) 
	{		
		try {
			if(userAccount.checkUserAuth(headers))
			{
				CookieServer cookie = new CookieServer(headers);
				String path = request.getServletPath();
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
				if(path.equals("/email-setting.html"))
				{
					this.processEmailSetting(requestBody);
				}
				if(path.equals("/sms.html"))
				{
					this.processSMS(requestBody);
				}
				if(path.equals("/cloudflare.html"))
				{
					this.processCloudflareSetting(requestBody);
				}
				if(path.equals("/network-setting.html"))
				{
					this.processNetworkSetting(requestBody);
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
	
	private void processNetworkSetting(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		if(query.containsKey("save_dhcp"))
		{
			String domainName = query.getOrDefault("domainName", "").trim();
			String domainNameServersStr = query.getOrDefault("domainNameServers", "").trim();
			String ipRouter = query.getOrDefault("ipRouter", "").trim();
			String netmask = query.getOrDefault("netmask", "").trim();
			String subnetMask = query.getOrDefault("subnetMask", "").trim();
			String domainNameServersAddress = query.getOrDefault("domainNameServersAddress", "").trim();
			String defaultLeaseTime = query.getOrDefault("defaultLeaseTime", "").trim();
			String maxLeaseTime = query.getOrDefault("maxLeaseTime", "").trim();
			String ranges = query.getOrDefault("ranges", "").trim();
			
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
			
			ConfigNetDHCP.load(dhcpSettingPath);
			ConfigNetDHCP.setDomainName(domainName);
			ConfigNetDHCP.setIpRouter(ipRouter);
			ConfigNetDHCP.setNetmask(netmask);
			ConfigNetDHCP.setSubnetMask(subnetMask);
			ConfigNetDHCP.setDomainNameServersAddress(domainNameServersAddress);
			ConfigNetDHCP.setDefaultLeaseTime(defaultLeaseTime);
			ConfigNetDHCP.setMaxLeaseTime(maxLeaseTime);
			ConfigNetDHCP.setRanges(rangeList);
			ConfigNetDHCP.setDomainNameServers(nsList);
			ConfigNetDHCP.save(dhcpSettingPath);	
			ConfigNetDHCP.apply();
		}
		
		if(query.containsKey("save_wlan"))
		{
			ConfigNetWLAN.load(wlanSettingPath);
			ConfigNetWLAN.setEssid(query.getOrDefault("essid", "").trim());
			ConfigNetWLAN.setKey(query.getOrDefault("key", "").trim());
			ConfigNetWLAN.setKeyMgmt(query.getOrDefault("keyMgmt", "").trim());
			ConfigNetWLAN.setIpAddress(query.getOrDefault("ipAddress", "").trim());
			ConfigNetWLAN.setPrefix(query.getOrDefault("prefix", "").trim());
			ConfigNetWLAN.setNetmask(query.getOrDefault("netmask", "").trim());
			ConfigNetWLAN.setGateway(query.getOrDefault("gateway", "").trim());
			ConfigNetWLAN.setDns1(query.getOrDefault("dns1", "").trim());
			ConfigNetWLAN.save(wlanSettingPath);
			ConfigNetWLAN.apply();
		}

		if(query.containsKey("save_ethernet"))
		{
			ConfigNetEthernet.load(ethernetSettingPath);
			ConfigNetEthernet.setIpAddress(query.getOrDefault("ipAddress", "").trim());
			ConfigNetEthernet.setPrefix(query.getOrDefault("prefix", "").trim());
			ConfigNetEthernet.setNetmask(query.getOrDefault("netmask", "").trim());
			ConfigNetEthernet.setGateway(query.getOrDefault("gateway", "").trim());
			ConfigNetEthernet.setDns1(query.getOrDefault("dns1", "").trim());
			ConfigNetEthernet.setDns2(query.getOrDefault("dns2", "").trim());
			ConfigNetEthernet.save(ethernetSettingPath);
			ConfigNetEthernet.apply();
		}
	}

	private void processCloudflareSetting(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		String endpoint = query.getOrDefault("endpoint", "").trim();
		String accountId = query.getOrDefault("account_id", "").trim();
		String authEmail = query.getOrDefault("auth_email", "").trim();
		String authApiKey = query.getOrDefault("auth_api_key", "").trim();
		String authToken = query.getOrDefault("auth_token", "").trim();
		
		if(!endpoint.isEmpty())
		{
			ConfigCloudflare.load(cloudflareSettingPath);
			ConfigCloudflare.setEndpoint(endpoint);
			ConfigCloudflare.setAccountId(accountId);
			ConfigCloudflare.setAuthEmail(authEmail);
			ConfigCloudflare.setAuthApiKey(authApiKey);
			ConfigCloudflare.setAuthToken(authToken);
			ConfigCloudflare.save(cloudflareSettingPath);
		}
	}
	
	private void processEmailSetting(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		if(query.containsKey("save_email_setting"))
		{
			boolean lMailAuth = query.getOrDefault("mail_auth", "").trim().equals("1");
			String lMailHost = query.getOrDefault("smtp_host", "").trim();
	
			String v1 = query.getOrDefault("smtp_port", "0").trim();
			int lMailPort = Utility.atoi(v1);
			String lMailSenderAddress = query.getOrDefault("sender_address", "").trim();
			String lMailSenderPassword = query.getOrDefault("sender_password", "").trim();
			if(lMailSenderPassword.isEmpty())
			{
				lMailSenderPassword = ConfigEmail.getMailSenderPassword();
			}
			boolean lMailSSL = query.getOrDefault("ssl", "").trim().equals("1");
			boolean lMailStartTLS = query.getOrDefault("start_tls", "").trim().equals("1");
			
			JSONObject config = new JSONObject();
			
			config.put("mailAuth", lMailAuth);
			config.put("mailHost", lMailHost);
			config.put("mailPort", lMailPort);
			config.put("mailSenderAddress", lMailSenderAddress);
			config.put("mailSenderPassword", lMailSenderPassword);
			config.put("mailSSL", lMailSSL);
			config.put("mailStartTLS", lMailStartTLS);
			
			saveConfigEmail(config);
		}
		
	}

	private void saveConfigEmail(JSONObject config)
	{
		ConfigEmail.save(emailSettingPath, config);
	}
	
	private void processSMSSetting(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		if(query.containsKey("save_sms_setting"))
		{
			String connectionType = query.getOrDefault("connection_type", "");			
			String smsCenter = query.getOrDefault("sms_center", "");		
			String incommingInt = query.getOrDefault("incomming_interval", "0");
			int incommingInterval = Utility.atoi(incommingInt);
			String tmRange = query.getOrDefault("time_range", "0");
			int timeRange = Utility.atoi(tmRange);	
			String maxInRange = query.getOrDefault("max_per_time_range", "0");
			int maxPerTimeRange = Utility.atoi(maxInRange);
			String imei = query.getOrDefault("imei", "");		
			String simCardPIN = query.getOrDefault("sim_card_pin", "");		
			
			ConfigSMS.setConnectionType(connectionType);
			ConfigSMS.setImei(imei);
			ConfigSMS.setSimCardPIN(simCardPIN);
			ConfigSMS.setSmsCenter(smsCenter);
			ConfigSMS.setIncommingInterval(incommingInterval);
			ConfigSMS.setTimeRange(timeRange);
			ConfigSMS.setMaxPerTimeRange(maxPerTimeRange);			
			
			ConfigSMS.save(smsSettingPath);			
		}		
	}
	
	private void processFeederSetting(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		if(query.containsKey("save_feeder_ws_setting"))
		{

			boolean feederWsEnable = query.getOrDefault("feeder_ws_enable", "").equals("1");		
			boolean feederWsSSL = query.getOrDefault("feeder_ws_ssl", "").equals("1");		
			String feederWsAddress = query.getOrDefault("feeder_ws_address", "");		
			String port = query.getOrDefault("feeder_ws_port", "0");
			int feederWsPort = Utility.atoi(port);
			String feederWsPath = query.getOrDefault("feeder_ws_path", "");		
			String feederWsUsername = query.getOrDefault("feeder_ws_username", "");		
			String feederWsPassword = query.getOrDefault("feeder_ws_password", "");		
			String feederWsChannel = query.getOrDefault("feeder_ws_channel", "");
			
			String timeout = query.getOrDefault("feeder_ws_timeout", "0");
			int feederWsTimeout = Utility.atoi(timeout);	
			String reconnect = query.getOrDefault("feeder_ws_reconnect_delay", "0");
			int feederWsReconnectDelay = Utility.atoi(reconnect);
			String refresh = query.getOrDefault("feeder_ws_refresh", "0");
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
			
			ConfigFeederWS.save(feederWSSettingPath);
		}
		if(query.containsKey("save_feeder_amqp_setting"))
		{
			boolean feederAmqpEnable = query.getOrDefault("feeder_amqp_enable", "").equals("1");		
			boolean feederAmqpSSL = query.getOrDefault("feeder_amqp_ssl", "").equals("1");		
			String feederAmqpAddress = query.getOrDefault("feeder_amqp_address", "");		
			String port = query.getOrDefault("feeder_amqp_port", "0");
			int feederAmqpPort = Utility.atoi(port);
			String feederAmqpPath = query.getOrDefault("feeder_amqp_path", "");		
			String feederAmqpUsername = query.getOrDefault("feeder_amqp_username", "");		
			String feederAmqpPassword = query.getOrDefault("feeder_amqp_password", "");		
			String feederAmqpChannel = query.getOrDefault("feeder_amqp_channel", "");
			
			String timeout = query.getOrDefault("feeder_amqp_timeout", "0");
			int feederAmqpTimeout = Utility.atoi(timeout);	
			String refresh = query.getOrDefault("feeder_amqp_refresh", "0");
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

			ConfigFeederAMQP.save(feederAMQPSettingPath);			
		}		
	}
	
	private void processSMS(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		if(query.containsKey("send"))
		{
			String receiver = query.getOrDefault(JsonKey.RECEIVER, "");			
			String message = query.getOrDefault(JsonKey.MESSAGE, "");	
			try 
			{
				this.broardcastWebSocket("Sending a message to "+receiver);
				SMSUtil.sendSMS(receiver, message);
			} 
			catch (GSMNullException e) 
			{
				e.printStackTrace();
			}
		}		
	}
	
	private void processAccount(String requestBody, CookieServer cookie) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
		String phone = query.getOrDefault(JsonKey.PHONE, "");
		String password = query.getOrDefault(JsonKey.PASSWORD, "");
		String email = query.getOrDefault(JsonKey.EMAIL, "");
		String name = query.getOrDefault(JsonKey.NAME, "");
		if(query.containsKey(JsonKey.UPDATE))
		{
			User user;
			try 
			{
				user = userAccount.getUser(loggedUsername);
				user.setName(name);
				user.setPhone(phone);
				user.setEmail(email);
				if(!password.isEmpty())
				{
					user.setPassword(password);
				}
				userAccount.updateUser(user);
				userAccount.save();
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
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
		if(query.containsKey(JsonKey.DELETE))
		{
			/**
			 * Delete
			 */
			for (Map.Entry<String, String> entry : query.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id[") && !value.equals(loggedUsername))
				{
					userAccount.deleteUser(value);
				}
			}
			userAccount.save();
		}
		if(query.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			this.processAdminDeactivate(query, loggedUsername);
		}
		if(query.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			this.processAdminActivate(query);
		}
		if(query.containsKey("block"))
		{
			/**
			 * Block
			 */
			this.processAdminBlock(query, loggedUsername);
			
		}
		if(query.containsKey("unblock"))
		{
			/**
			 * Unblock
			 */
			this.processAdminUnblock(query);
		}
		if(query.containsKey("update-data"))
		{
			this.processAdminUpdateData(query);
		}
		if(query.containsKey(JsonKey.UPDATE))
		{
			this.processAdminUpdate(query);
		}
	}
	private void processAPIUser(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		if(query.containsKey(JsonKey.DELETE))
		{
			/**
			 * Delete
			 */
			for (Map.Entry<String, String> entry : query.entrySet()) 
			{
				String value = entry.getValue();
				userAPIAccount.deleteUser(value);
			}
			userAPIAccount.save();
			APIUserAccount.update(userAPIAccount.toJSONObject().toString());
		}
		if(query.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			this.processAPIUserDeactivate(query);
		}
		if(query.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			this.processAPIUserActivate(query);
		}
		if(query.containsKey("block"))
		{
			/**
			 * Block
			 */
			this.processAPIUserBlock(query);
			
		}
		if(query.containsKey("unblock"))
		{
			/**
			 * Unblock
			 */
			this.processAPIUserUnblock(query);
		}
		if(query.containsKey(JsonKey.UPDATE))
		{
			this.processAPIUserUpdate(query);
		}
	}
	
	private void processAdminDeactivate(Map<String, String> query, String loggedUsername)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id[") && !value.equals(loggedUsername))
			{
				try {
					userAccount.deactivate(value);
				} catch (NoUserRegisteredException e) {
					/**
					 * Do nothing
					 */
				}
			}
		}
		userAccount.save();
	}
	
	private void processAdminActivate(Map<String, String> query)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				try 
				{
					userAccount.activate(value);
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		userAccount.save();
	}
	
	private void processAdminBlock(Map<String, String> query, String loggedUsername)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id[") && !value.equals(loggedUsername))
			{
				try 
				{
					userAccount.block(value);
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		userAccount.save();
	}
	
	private void processAdminUnblock(Map<String, String> query)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				try 
				{
					userAccount.unblock(value);
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		userAccount.save();
	}
	
	private void processAdminUpdateData(Map<String, String> query)
	{
		String pkID = query.getOrDefault("pk_id", "");
		String field = query.getOrDefault("field", "");
		String value = query.getOrDefault("value", "");
		if(!field.equals(JsonKey.USERNAME))
		{
			User user;
			try 
			{
				user = userAccount.getUser(pkID);
				if(field.equals(JsonKey.PHONE))
				{
					user.setPhone(value);
				}
				if(field.equals(JsonKey.NAME))
				{
					user.setName(value);
				}
				userAccount.updateUser(user);
				userAccount.save();
			} 
			catch (NoUserRegisteredException e) 
			{
				/**
				 * Do nothing
				 */
			}
		}
	}
	
	private void processAdminUpdate(Map<String, String> query)
	{
		String username = query.getOrDefault(JsonKey.USERNAME, "").trim();
		String name = query.getOrDefault(JsonKey.NAME, "").trim();
		String phone = query.getOrDefault(JsonKey.PHONE, "").trim();
		String email = query.getOrDefault(JsonKey.EMAIL, "").trim();
		String password = query.getOrDefault(JsonKey.PASSWORD, "").trim();
		boolean blocked = query.getOrDefault(JsonKey.BLOCKED, "").equals("1");
		boolean active = query.getOrDefault(JsonKey.ACTIVE, "").equals("1");

		if(!username.isEmpty())
		{
			User user;
			try 
			{
				user = userAccount.getUser(username);
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
				userAccount.updateUser(user);
				userAccount.save();
			} 
			catch (NoUserRegisteredException e) 
			{
				/**
				 * Do nothing
				 */
			}
		}
	}
	
	private void processAPIUserDeactivate(Map<String, String> query)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				try {
					userAPIAccount.deactivate(value);
				} catch (NoUserRegisteredException e) {
					/**
					 * Do nothing
					 */
				}
			}
		}
		userAPIAccount.save();
		APIUserAccount.update(userAPIAccount.toJSONObject().toString());
	}
	private void processAPIUserActivate(Map<String, String> query)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				try 
				{
					userAPIAccount.activate(value);
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		userAPIAccount.save();
		APIUserAccount.update(userAPIAccount.toJSONObject().toString());
	}
	private void processAPIUserBlock(Map<String, String> query)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				try 
				{
					userAPIAccount.block(value);
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		userAPIAccount.save();
		APIUserAccount.update(userAPIAccount.toJSONObject().toString());
	}
	private void processAPIUserUnblock(Map<String, String> query)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
		{
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.startsWith("id["))
			{
				try 
				{
					userAPIAccount.unblock(value);
				} 
				catch (NoUserRegisteredException e) 
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		userAPIAccount.save();
		APIUserAccount.update(userAPIAccount.toJSONObject().toString());
	}
	
	private void processAPIUserUpdate(Map<String, String> query)
	{
		String username = query.getOrDefault(JsonKey.USERNAME, "").trim();
		String name = query.getOrDefault(JsonKey.NAME, "").trim();
		String phone = query.getOrDefault(JsonKey.PHONE, "").trim();
		String email = query.getOrDefault(JsonKey.EMAIL, "").trim();
		String password = query.getOrDefault(JsonKey.PASSWORD, "").trim();
		boolean blocked = query.getOrDefault(JsonKey.BLOCKED, "").equals("1");
		boolean active = query.getOrDefault(JsonKey.ACTIVE, "").equals("1");

		if(!username.isEmpty())
		{
			User user;
			try 
			{
				user = userAPIAccount.getUser(username);
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
				userAPIAccount.updateUser(user);
				userAPIAccount.save();
				APIUserAccount.update(userAPIAccount.toJSONObject().toString());
			} 
			catch (NoUserRegisteredException e) 
			{
				/**
				 * Do nothing
				 */
			}
		}
	}
	
	
	
	
	
	private void processDDNS(String requestBody, CookieServer cookie) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		String loggedUsername = (String) cookie.getSessionValue(JsonKey.USERNAME, "");
		if(query.containsKey(JsonKey.DELETE))
		{
			/**
			 * Delete
			 */
			for (Map.Entry<String, String> entry : query.entrySet()) 
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if(key.startsWith("id[") && !value.equals(loggedUsername))
				{
					ConfigDDNS.deleteRecord(value);
				}
			}
			ConfigDDNS.save();
		}
		if(query.containsKey(JsonKey.DEACTIVATE))
		{
			/**
			 * Deactivate
			 */
			this.processDDNSDeactivate(query);
		}
		if(query.containsKey(JsonKey.ACTIVATE))
		{
			/**
			 * Activate
			 */
			this.processDDNSActivate(query);
		}
		if(query.containsKey(JsonKey.PROXIED))
		{
			/**
			 * Proxied
			 */
			this.processDDNSProxied(query);
		}
		if(query.containsKey(JsonKey.UNPROXIED))
		{
			/**
			 * Unproxied
			 */
			this.processDDNSUnproxied(query);
		}
		if(query.containsKey(JsonKey.UPDATE))
		{
			this.processDDNSUpdate(query);
		}
	}

	private void processDDNSUpdate(Map<String, String> query) {
		String id = query.getOrDefault(JsonKey.ID, "").trim();
		String provider = query.getOrDefault(JsonKey.PROVIDER, "").trim();
		String zone = query.getOrDefault(JsonKey.ZONE, "").trim();
		String recordName = query.getOrDefault(JsonKey.RECORD_NAME, "").trim();
		String ttls = query.getOrDefault(JsonKey.TTL, "").trim();
		String cronExpression = query.getOrDefault(JsonKey.CRON_EXPRESSION, "").trim();
		boolean proxied = query.getOrDefault(JsonKey.PROXIED, "").equals("1");
		boolean forceCreateZone = query.getOrDefault(JsonKey.FORCE_CREATE_ZONE, "").equals("1");
		boolean active = query.getOrDefault(JsonKey.ACTIVE, "").equals("1");
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
			ConfigDDNS.updateRecord(record);
			ConfigDDNS.save();
		}
	}

	private void processDDNSDeactivate(Map<String, String> query)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
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
	
	private void processDDNSActivate(Map<String, String> query)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
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
	
	private void processDDNSProxied(Map<String, String> query)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
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
		
	private void processDDNSUnproxied(Map<String, String> query)
	{
		for (Map.Entry<String, String> entry : query.entrySet()) 
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
	
	private String getMIMEType(String fileName) 
	{
		String[] arr = fileName.split("\\.");	
		String ext = arr[arr.length - 1];
		return 	configSaved.getString("MIME", ext, "");
	}

	private WebSocketContent updateContent(String fileName, HttpHeaders responseHeaders, byte[] responseBody, HttpStatus statusCode, CookieServer cookie) 
	{
		String contentType = this.getMIMEType(fileName);
		WebSocketContent webContent = new WebSocketContent(fileName, responseHeaders, responseBody, statusCode, cookie, contentType);
		boolean requireLogin = false;
		String fileSub = "";
		
		if(fileName.toLowerCase().endsWith(ConstantString.EXT_HTML))
		{
			JSONObject authFileInfo = this.processAuthFile(responseBody);
			requireLogin = authFileInfo.optBoolean(JsonKey.CONTENT, false);
			fileSub = this.getFileName(authFileInfo.optString("data-file", ""));
		}
		
		String username = cookie.getSessionData().optString(JsonKey.USERNAME, "");
		String password = cookie.getSessionData().optString(JsonKey.PASSWORD, "");
		if(requireLogin)
		{
			responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
			webContent.setResponseHeaders(responseHeaders);
			try
			{
				if(!userAccount.checkUserAuth(username, password))	
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
				responseBody = this.removeMeta(responseBody);
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
	
	private JSONObject processAuthFile(byte[] responseBody) 
	{
		String responseString = new String(responseBody);
		int start = 0;
		int end = 0;
		do 
		{
			start = responseString.toLowerCase().indexOf("<meta ", end);
			end = responseString.toLowerCase().indexOf(">", start);
			if(start >-1 && end >-1 && end < responseString.length())
			{
				String meta = responseString.substring(start, end+1);
				meta = this.fixMeta(meta);
				try
				{
					JSONObject metaObj = XML.toJSONObject(meta);
					JSONObject metaObjFixed = this.lowerCaseJSONKey(metaObj);
					if(requireLogin(metaObjFixed))
					{
						return metaObjFixed.optJSONObject(JsonKey.META);
					}
				}
				catch(JSONException e)
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		while(start > -1);
		return new JSONObject();
	}
	
	private byte[] removeMeta(byte[] responseBody) 
	{
		String responseString = new String(responseBody);
		int start = 0;
		int end = 0;
		String metaOri = "";
		boolean found = false;
		do 
		{
			start = responseString.toLowerCase().indexOf("<meta ", end);
			end = responseString.toLowerCase().indexOf(">", start);
			if(start >-1 && end >-1 && end < responseString.length())
			{
				metaOri = responseString.substring(start, end+1);
				String meta = this.fixMeta(metaOri);
				try
				{
					JSONObject metaObj = XML.toJSONObject(meta);
					JSONObject metaObjFixed = this.lowerCaseJSONKey(metaObj); 
					if(requireLogin(metaObjFixed))
					{
						found = true;
						break;
					}
				}
				catch(JSONException e)
				{
					/**
					 * Do nothing
					 */
				}
			}
		}
		while(start > -1);
		String content = "";
		if(found && responseBody != null)
		{
			content = new String(responseBody);
			return content.replace(metaOri, "").getBytes();
		}
		return responseBody;
	}

	private boolean requireLogin(JSONObject metaObj) {
		if(metaObj != null && metaObj.has(JsonKey.META))
		{
			JSONObject metaData = metaObj.optJSONObject(JsonKey.META);
			if(metaData != null)
			{
				String name = metaData.optString(JsonKey.NAME, "");
				boolean content = metaData.optBoolean(JsonKey.CONTENT, false);
				if(name.equals(JsonKey.REQUIRE_LOGIN) && content)
				{
					return true;
				}
			}
		}
		return false;
	}

	private String fixMeta(String input)
	{
		if(input.indexOf("</meta>") == -1 && input.indexOf("/>") == -1)
		{
			input = input.replace(">", "/>");
		}
		return input;
	}
	
	private JSONObject lowerCaseJSONKey(Object object) 
	{
		JSONObject newMetaObj = new JSONObject();
		JSONArray keys = ((JSONObject) object).names();
		for (int i = 0; i < keys.length (); ++i) 
		{
		   String key = keys.getString(i); 
		   if(((JSONObject) object).get(key) instanceof JSONObject)
		   {
			   newMetaObj.put(key.toLowerCase(), this.lowerCaseJSONKey(((JSONObject) object).get(key)));
		   }
		   else
		   {
			   newMetaObj.put(key.toLowerCase(), ((JSONObject) object).get(key));
		   }
		}
		return newMetaObj;
	}

	private String getFileName(HttpServletRequest request) 
	{
		String file = request.getServletPath();
		if(file == null || file.isEmpty() || file.equals("/"))
		{
			file = Config.getDefaultFile();
		}		
		String dir = "";
		return dir + documentRoot+file;		
	}
	
	private String getFileName(String request) 
	{
		return documentRoot+request;
	}
	
}

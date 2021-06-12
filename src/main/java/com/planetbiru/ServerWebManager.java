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
import org.springframework.beans.factory.annotation.Autowired;
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
import com.planetbiru.config.ResourceConfig;
import com.planetbiru.constant.ConstantString;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.cookie.CookieServer;
import com.planetbiru.gsm.ErrorCode;
import com.planetbiru.gsm.GSMNotInitalizedException;
import com.planetbiru.gsm.PortUtils;
import com.planetbiru.gsm.SMSInstance;
import com.planetbiru.receiver.ws.WebSocketClient;
import com.planetbiru.settings.FeederSetting;
import com.planetbiru.settings.SMSSetting;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.user.User;
import com.planetbiru.user.UserAccount;
import com.planetbiru.util.FileNotFoundException;
import com.planetbiru.util.FileUtil;
import com.planetbiru.util.SendMail;
import com.planetbiru.util.Utility;
import com.planetbiru.util.WebContent;

@RestController
public class ServerWebManager {
	
	private ResourceConfig mime = new ResourceConfig();
	private Logger logger = LogManager.getLogger(ServerWebManager.class);	
	
	@Autowired
	WebSocketClient wsClient;

	SMSInstance smsService = new SMSInstance();
	private UserAccount userAccount;

	@Value("${otpbroker.secret.key}")
	private String secretKey;
	
	@Value("${otpbroker.mail.sender.address}")
	private String mailSenderAddress;

	@Value("${otpbroker.mail.sender.password}")
	private String mailSenderPassword;
	
	@Value("${otpbroker.mail.auth}")
	private String mailAuth;
	
	@Value("${otpbroker.mail.start.tls}")
	private boolean mailStartTLS;
	
	@Value("${otpbroker.mail.ssl}")
	private boolean mailSSL;
	
	@Value("${otpbroker.mail.host}")
	private String mailHost;
	
	@Value("${otpbroker.mail.port}")
	private String mailPort;


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

	@Value("${otpbroker.path.setting.feeder}")
	private String feederSettingPath;

	@Value("${otpbroker.path.setting.sms}")
	private String smsSettingPath;
	
	@Value("${otpbroker.path.setting.all}")
	private String mimeSettingPath;	

	
	@Value("${otpbroker.path.setting.user}")
	private String userSettingPath;

	@Value("${otpbroker.connection.type}")
	private String portName;

	@Autowired
	PortUtils portUtils;
	
	private ServerWebManager()
    {
    }
	
	@PostConstruct
	public void init()
	{
		logger.info("Init...");
		
		
		Config.setMailSenderAddress(mailSenderAddress);
		Config.setMailSenderPassword(mailSenderPassword);
		Config.setMailAuth(mailAuth);
		Config.setMailSSL(mailSSL);
		Config.setMailStartTLS(mailStartTLS);
		Config.setMailHost(mailHost);
		Config.setMailPort(mailPort);
		Config.setPortName(portName);
		
		
		
		userAccount = new UserAccount(userSettingPath);
		
		this.initWSClient();
		this.initSerial();
		
		
		try 
		{
			mime = new ResourceConfig(mimeSettingPath);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			
		}
	}
	
	private void initWSClient() 
	{
		wsClient.setSMSService(smsService);
		wsClient.start();	
	}
	
	
	private void initSerial() 
	{
		String port = Config.getPortName();
		smsService.init(port);
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
					String smtpHost = Config.getMailHost();
					String smtpPort = Config.getMailPort();
				    String smtpUser = Config.getMailSenderAddress();
				    String smtpPassword = Config.getMailSenderPassword();
				    boolean ssl = Config.isMailSSL();
				    boolean starttls = Config.getMailStartTLS();   
				    boolean debug = false;
					
					SendMail senEmail = new SendMail(smtpHost, smtpPort, smtpUser, smtpPassword, ssl, starttls, debug);
					try 
					{
						senEmail.send(email, "Account Information", message);
					} 
					catch (MessagingException e) 
					{
						e.printStackTrace();
					}
				}
				else if(!phone.isEmpty())
				{
					String message = "Username : "+user.getUsername()+"\r\nPassword : "+user.getPassword();
					try 
					{
						smsService.sendSMS(phone, message);
					} 
					catch (GSMNotInitalizedException e) 
					{
						e.printStackTrace();
					}
				}
			}

		} 
		catch (NoUserRegisteredException e1) 
		{
			e1.printStackTrace();
		}
		

		
		HttpStatus statusCode = HttpStatus.OK;
		
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
	
	
	public void broardcastWebSocket(String message)
	{
		JSONObject messageJSON = new JSONObject();
		messageJSON.put(JsonKey.COMMAND, "broadcast-message");
		JSONArray data = new JSONArray();
		JSONObject itemData = new JSONObject();
		String uuid = UUID.randomUUID().toString();
		itemData.put("id", uuid);
		itemData.put(JsonKey.MESSAGE, message);
		data.put(itemData);
		messageJSON.put("data", data);
		
		ServerWebSocket.broadcast(messageJSON.toString(4));
		
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
	    payload.put("nextURL", next);
	    res.put("code", 0);
	    res.put("payload", payload);
	    
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
		byte[] responseBody = res.toString().getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		
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
		}
		
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@GetMapping(path="/feeder-setting/get")
	public ResponseEntity<byte[]> handleFeederSetting(@RequestHeader HttpHeaders headers, HttpServletRequest request)
	{
		HttpHeaders responseHeaders = new HttpHeaders();
		CookieServer cookie = new CookieServer(headers);
		byte[] responseBody = "".getBytes();
		HttpStatus statusCode = HttpStatus.OK;
		try
		{
			if(userAccount.checkUserAuth(headers))
			{
				FeederSetting feederSetting = new FeederSetting();
				feederSetting.load(feederSettingPath);
				String list = feederSetting.toString();
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
				SMSSetting smsSetting = new SMSSetting();
				smsSetting.load(smsSettingPath);
				String list = smsSetting.toString();
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
				String list = userAccount.list();
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
		}
		cookie.saveSessionData();
		cookie.putToHeaders(responseHeaders);
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
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
			}		    
		}
		
		responseHeaders.add(ConstantString.LOCATION, ConstantString.ADMIN_FILE_LEVEL_3);
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
	
	@PostMapping(path="/api/sms**")
	public ResponseEntity<String> sendSMS(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		JSONObject responseJSON = this.processMessageRequest(requestBody);
		
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		String responseBody = responseJSON.toString(4);
		return (new ResponseEntity<>(responseBody, responseHeaders, statusCode));	
	}
	
	@PostMapping(path="/api/ussd**")
	public ResponseEntity<String> sendUSSD(@RequestHeader HttpHeaders headers, @RequestBody String requestBody, HttpServletRequest request)
	{		
		HttpHeaders responseHeaders = new HttpHeaders();
		HttpStatus statusCode = HttpStatus.OK;
		JSONObject responseJSON = new JSONObject();
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		String ussd = query.getOrDefault("ussd", "");
		String message = "";
		String responseCode = ErrorCode.SUCCESS;
		String responseText = "";
		if(ussd != null && !ussd.isEmpty())
		{
			try 
			{
				message = smsService.executeUSSD(ussd);
			} 
			catch (GSMNotInitalizedException e) 
			{
				responseCode = e.getErrorCode();
				responseText = "<strong>Error: "+e.getErrorCode()+"</strong> "+e.getMessage()+". <a href=\"error-"+e.getErrorCode()+".html\">Detail</a>";
			}		
		}
		JSONObject data = new JSONObject();
		data.put(JsonKey.MESSAGE, message);
		responseJSON.put(JsonKey.RESPONSE_CODE, responseCode);
		responseJSON.put(JsonKey.RESPONSE_TEXT, responseText);
		responseJSON.put(JsonKey.DATA, data);		
		responseHeaders.add(ConstantString.CONTENT_TYPE, ConstantString.APPLICATION_JSON);
		responseHeaders.add(ConstantString.CACHE_CONTROL, ConstantString.NO_CACHE);
		String responseBody = responseJSON.toString(4);
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
		System.out.println("AAAAAAAAAAAAAA");
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
		}
		CookieServer cookie = new CookieServer(headers);
		
		WebContent newContent = this.updateContent(fileName, responseHeaders, responseBody, statusCode, cookie);	
		
		responseBody = newContent.getResponseBody();
		responseHeaders = newContent.getResponseHeaders();
		statusCode = newContent.getStatusCode();
		String contentType = this.getMIMEType(fileName);
		
		responseHeaders.add(ConstantString.CONTENT_TYPE, contentType);
		
		if(fileName.endsWith(".html"))
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
			String lt = mime.getString("CACHE", ext, "0");
			lt = lt.replaceAll("[^\\d]", "");
			if(!lt.isEmpty())
			{
				try
				{
					lifetime = Integer.parseInt(lt);
				}
				catch(NumberFormatException e)
				{
					/**
					 * Do nothing
					 */
				}
			}
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
				if(path.equals("/feeder-setting.html"))
				{
					this.processFeederSetting(requestBody);
				}
				if(path.equals("/sms-setting.html"))
				{
					this.processSMSSetting(requestBody);
				}
				if(path.equals("/sms.html"))
				{
					this.processSMS(requestBody);
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
	
	private void processSMSSetting(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		if(query.containsKey("save_sms_setting"))
		{
			String connectionType = query.getOrDefault("connection_type", "");			
			String smsCenter = query.getOrDefault("sms_center", "");		
			int incommingInterval = 0;
			try
			{
				String incommingInt = query.getOrDefault("incomming_interval", "0");
				incommingInt = incommingInt.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(incommingInt.isEmpty())
				{
					incommingInt = "0";
				}
				incommingInterval = Integer.parseInt(incommingInt);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			int timeRange = 0;	
			try
			{
				String tmRange = query.getOrDefault("time_range", "0");
				tmRange = tmRange.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(tmRange.isEmpty())
				{
					tmRange = "0";
				}
				timeRange = Integer.parseInt(tmRange);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			int maxPerTimeRange = 0;
			try
			{
				String maxInRange = query.getOrDefault("max_per_time_range", "0");
				maxInRange = maxInRange.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(maxInRange.isEmpty())
				{
					maxInRange = "0";
				}
				maxPerTimeRange = Integer.parseInt(maxInRange);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			SMSSetting smsSetting = new SMSSetting();
			smsSetting.setConnectionType(connectionType);
			smsSetting.setSmsCenter(smsCenter);
			smsSetting.setIncommingInterval(incommingInterval);
			smsSetting.setTimeRange(timeRange);
			smsSetting.setMaxPerTimeRange(maxPerTimeRange);			
			
			smsSetting.save(smsSettingPath);			
		}		
	}
	
	private void processFeederSetting(String requestBody) {
		Map<String, String> query = Utility.parseURLEncoded(requestBody);
		if(query.containsKey("save_feeder_setting"))
		{

			boolean feederWsEnable = query.getOrDefault("feeder_ws_enable", "").equals("1");		
			boolean feederWsSSL = query.getOrDefault("feeder_ws_ssl", "").equals("1");		
			String feederWsAddress = query.getOrDefault("feeder_ws_address", "");		
			int feederWsPort = 0;
			try
			{
				String port = query.getOrDefault("feeder_ws_port", "0");
				port = port.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(port.isEmpty())
				{
					port = "0";
				}
				feederWsPort = Integer.parseInt(port);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			String feederWsPath = query.getOrDefault("feeder_ws_path", "");		
			String feederWsUsername = query.getOrDefault("feeder_ws_username", "");		
			String feederWsPassword = query.getOrDefault("feeder_ws_password", "");		
			String feederWsChannel = query.getOrDefault("feeder_ws_channel", "");
			
			int feederWsTimeout = 0;	
			try
			{
				String timeout = query.getOrDefault("feeder_ws_timeout", "0");
				timeout = timeout.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(timeout.isEmpty())
				{
					timeout = "0";
				}
				feederWsTimeout = Integer.parseInt(timeout);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			int feederWsReconnectDelay = 0;
			try
			{
				String reconnect = query.getOrDefault("feeder_ws_reconnect_delay", "0");
				reconnect = reconnect.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(reconnect.isEmpty())
				{
					reconnect = "0";
				}
				feederWsReconnectDelay = Integer.parseInt(reconnect);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			int feederWsRefresh = 0;
			try
			{
				String refresh = query.getOrDefault("feeder_ws_refresh", "0");
				refresh = refresh.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(refresh.isEmpty())
				{
					refresh = "0";
				}
				feederWsRefresh = Integer.parseInt(refresh);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			boolean feederAmqpEnable = query.getOrDefault("feeder_amqp_enable", "").equals("1");		
			boolean feederAmqpSSL = query.getOrDefault("feeder_amqp_ssl", "").equals("1");		
			String feederAmqpAddress = query.getOrDefault("feeder_amqp_address", "");		
			int feederAmqpPort = 0;
			try
			{
				String port = query.getOrDefault("feeder_amqp_port", "0");
				port = port.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(port.isEmpty())
				{
					port = "0";
				}
				feederAmqpPort = Integer.parseInt(port);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			String feederAmqpPath = query.getOrDefault("feeder_amqp_path", "");		
			String feederAmqpUsername = query.getOrDefault("feeder_amqp_username", "");		
			String feederAmqpPassword = query.getOrDefault("feeder_amqp_password", "");		
			String feederAmqpChannel = query.getOrDefault("feeder_amqp_channel", "");
			
			int feederAmqpTimeout = 0;	
			try
			{
				String timeout = query.getOrDefault("feeder_amqp_timeout", "0");
				timeout = timeout.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(timeout.isEmpty())
				{
					timeout = "0";
				}
				feederAmqpTimeout = Integer.parseInt(timeout);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			int feederAmqpRefresh = 0;
			try
			{
				String refresh = query.getOrDefault("feeder_amqp_refresh", "0");
				refresh = refresh.replaceAll(ConstantString.FILTER_INTEGER, "");
				if(refresh.isEmpty())
				{
					refresh = "0";
				}
				feederAmqpRefresh = Integer.parseInt(refresh);		
			}
			catch(NumberFormatException e)
			{
				/**
				 * Do nothing
				 */
			}
			
			FeederSetting setting = new FeederSetting();
			setting.setFeederWsEnable(feederWsEnable);
			setting.setFeederWsSSL(feederWsSSL);
			setting.setFeederWsAddress(feederWsAddress);
			setting.setFeederWsPort(feederWsPort);
			setting.setFeederWsPath(feederWsPath);
			setting.setFeederWsUsername(feederWsUsername);
			setting.setFeederWsPassword(feederWsPassword);
			setting.setFeederWsChannel(feederWsChannel);
			setting.setFeederWsTimeout(feederWsTimeout);
			setting.setFeederWsReconnectDelay(feederWsReconnectDelay);
			setting.setFeederWsRefresh(feederWsRefresh);		

			setting.setFeederAmqpEnable(feederAmqpEnable);
			setting.setFeederAmqpSSL(feederAmqpSSL);
			setting.setFeederAmqpAddress(feederAmqpAddress);
			setting.setFeederAmqpPort(feederAmqpPort);
			setting.setFeederAmqpPath(feederAmqpPath);
			setting.setFeederAmqpUsername(feederAmqpUsername);
			setting.setFeederAmqpPassword(feederAmqpPassword);
			setting.setFeederAmqpChannel(feederAmqpChannel);
			setting.setFeederAmqpTimeout(feederAmqpTimeout);
			setting.setFeederAmqpRefresh(feederAmqpRefresh);		

			setting.save(feederSettingPath);			
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
				smsService.sendSMS(receiver, message);
			} 
			catch (GSMNotInitalizedException e) 
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
		if(query.containsKey("update"))
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
		if(query.containsKey("delete"))
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
		if(query.containsKey("deactivate"))
		{
			/**
			 * Deactivate
			 */
			this.processAdminDeactivate(query, loggedUsername);
		}
		if(query.containsKey("activate"))
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
		if(query.containsKey("update"))
		{
			this.processAdminUpdate(query);
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
	
	
	private String getMIMEType(String fileName) 
	{
		String[] arr = fileName.split("\\.");	
		String ext = arr[arr.length - 1];
		return 	mime.getString("MIME", ext, "");
	}

	private WebContent updateContent(String fileName, HttpHeaders responseHeaders, byte[] responseBody, HttpStatus statusCode, CookieServer cookie) 
	{
		String contentType = this.getMIMEType(fileName);
		WebContent webContent = new WebContent(fileName, responseHeaders, responseBody, statusCode, cookie, contentType);
		boolean requireLogin = false;
		String fileSub = "";
		
		if(fileName.toLowerCase().endsWith(".html"))
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
		/**
		dir = FileUtil.class.getResource("/").getFile();
		if(dir.endsWith("/") && documentRoot.startsWith("/"))
		{
			dir = dir.substring(0, dir.length() - 1);
		}
		*/
		return dir + documentRoot+file;
		
	}
	
	private String getFileName(String request) 
	{
		return documentRoot+request;
	}
	
	private JSONObject processMessageRequest(String requestBody) 
	{
		JSONObject requestJSON = new JSONObject();
		try
		{
			requestJSON = new JSONObject(requestBody);
			String command = requestJSON.optString(JsonKey.COMMAND, "");
			if(command.equals(JsonKey.SEND_MESSAGE))
			{
				JSONArray data = requestJSON.optJSONArray(JsonKey.DATA);
				if(data != null && !data.isEmpty())
				{
					int length = data.length();
					int i;
					for(i = 0; i<length; i++)
					{
						this.sendMessage(data.getJSONObject(i));					
					}
				}
			}
		}
		catch(JSONException e)
		{
			/**
			 * Do nothing
			 */
		}
		return requestJSON;
	}
	
	public void sendMessage(JSONObject data)
	{
		if(data != null)
		{
			String receiver = data.optString(JsonKey.RECEIVER, "");
			String textMessage = data.optString(JsonKey.MESSAGE, "");
			try 
			{
				this.smsService.sendSMS(receiver, textMessage);
			} 
			catch (GSMNotInitalizedException e) 
			{
				
				e.printStackTrace();
			}
		}		
	}
	
	
	
	
}

package com.planetbiru;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.PostConstruct;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.planetbiru.config.Config;
import com.planetbiru.config.ConfigAPI;
import com.planetbiru.config.ConfigFeederAMQP;
import com.planetbiru.config.ConfigFeederWS;
import com.planetbiru.config.ConfigModem;
import com.planetbiru.config.PropertyLoader;
import com.planetbiru.constant.JsonKey;
import com.planetbiru.gsm.GSMUtil;
import com.planetbiru.user.WebUserAccount;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.util.Utility;
import com.planetbiru.wstools.MessageDecoder;
import com.planetbiru.wstools.MessageEncoder;
import com.planetbiru.wstools.ServletAwareConfigurator;

@Component
@ServerEndpoint(value = "/websocket/manager", 
	configurator = ServletAwareConfigurator.class,
	decoders = MessageDecoder.class, 
	encoders = MessageEncoder.class)
public class ServerWebSocketManager {
	
	@Value("${otpbroker.path.setting.user}")
	private String userSettingPath;

	@Value("${otpbroker.path.base.setting}")
	private String baseDirConfig;

	@Value("${otpbroker.web.session.name}")
	private String sessionName;

	@Value("${otpbroker.web.session.lifetime}")
	private long sessionLifetime;
	
	@Value("${otpbroker.web.session.file.path}")
	private String sessionFilePath;
	
	@Value("${otpbroker.path.setting.all}")
	private String mimeSettingPath;	
	
	@Value("${otpbroker.path.setting.modem}")
	private String modemSettingPath;

	private Session session;
	private String clientIP = "";
	private Map<String, List<String>> requestHeader = new HashMap<>();
	private Map<String, List<String>> responseHeader = new HashMap<>();
	private Map<String, List<String>> parameter = new HashMap<>();
	private String sessionID = "";
    private String username = "";
    private String channel = "";	
	
	private static Set<ServerWebSocketManager> listeners = new CopyOnWriteArraySet<>();
    
    private Random rand = new Random();  
    
    private String path = "";
    
    @PostConstruct
    public void init()
    {
		/**
		 * This configuration must be loaded first
		 */
		Config.setBaseDirConfig(baseDirConfig);
		
		Config.setSessionFilePath(sessionFilePath);
		Config.setSessionName(sessionName);
		Config.setSessionLifetime(sessionLifetime);
		Config.setMimeSettingPath(mimeSettingPath);
		
		Config.setUserSettingPath(userSettingPath);
		
		Config.setModemSettingPath(modemSettingPath);
		
		ConfigModem.load(Config.getModemSettingPath());
		PropertyLoader.load(Config.getMimeSettingPath());
    	WebUserAccount.load(Config.getUserSettingPath());
    }
    
	@SuppressWarnings("unchecked")
	@OnOpen
    public void onOpen(Session session, EndpointConfig config) {
		this.session = session;
        this.clientIP = (String) config.getUserProperties().get("remote_address");
        Map<String, List<String>> requestHdr = (Map<String, List<String>>) config.getUserProperties().get("request_header");
        Map<String, List<String>> responseHdr = (Map<String, List<String>>) config.getUserProperties().get("response_header");
        Map<String, List<String>> param = (Map<String, List<String>>) config.getUserProperties().get("parameter");       
        
        List<String> localPath = param.getOrDefault("path", new ArrayList<>());
        String pathStr = "";
		if(!localPath.isEmpty())
        {
        	pathStr = localPath.get(0);
        }
	    this.path = pathStr;         
        this.requestHeader = requestHdr;
        this.responseHeader = responseHdr;
        this.parameter = param;
        this.sessionID = this.createSessionID();
        
        boolean auth = true;
        try 
        {
        	auth = WebUserAccount.checkUserAuth(requestHdr);
            if(auth)
            {
                listeners.add(this);
                this.sendServerStatus();
            }
		} 
        catch (NoUserRegisteredException e) 
        {
        	/**
        	 * Do nothing
        	 */
		}
	}
	
    private String createSessionID() 
    {
    	return Utility.sha1(""+System.currentTimeMillis()+rand.nextInt(1000000000));
	}

	private void sendServerStatus() 
    {
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		
		JSONObject modem = new JSONObject();
		modem.put(JsonKey.NAME, "otp-modem-connected");
		modem.put(JsonKey.VALUE, GSMUtil.isConnected());
		modem.put(JsonKey.DATA, ConfigModem.getStatus());
		data.put(modem);
		JSONObject wsEnable = new JSONObject();
		wsEnable.put(JsonKey.NAME, "otp-ws-enable");
		wsEnable.put(JsonKey.VALUE, ConfigFeederWS.isFeederWsEnable());
		data.put(wsEnable);
		
		JSONObject wsConnected = new JSONObject();
		wsConnected.put(JsonKey.NAME, "otp-ws-connected");
		wsConnected.put(JsonKey.VALUE, ConfigFeederWS.isConnected());
		data.put(wsConnected);
		
		JSONObject amqpEnable = new JSONObject();
		amqpEnable.put(JsonKey.NAME, "otp-amqp-enable");
		amqpEnable.put(JsonKey.VALUE, ConfigFeederAMQP.isFeederAmqpEnable());
		data.put(amqpEnable);
		
		JSONObject amqpConnected = new JSONObject();
		amqpConnected.put(JsonKey.NAME, "otp-amqp-connected");
		amqpConnected.put(JsonKey.VALUE, ConfigFeederAMQP.isConnected());
		data.put(amqpConnected);
		
		JSONObject httpEnable = new JSONObject();
		httpEnable.put(JsonKey.NAME, "otp-http-enable");
		httpEnable.put(JsonKey.VALUE, ConfigAPI.isHttpEnable() || ConfigAPI.isHttpsEnable());
		data.put(httpEnable);
		
		info.put("command", "server-info");
		info.put("data", data);
		
		try 
		{
			this.sendMessage(info.toString());
		} 
		catch (JSONException | IOException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}	

	public JSONObject createWelcomeMessage() 
	{
		JSONObject msg = new JSONObject();
		JSONArray data = new JSONArray();
		JSONObject itemData = new JSONObject();
		itemData.put(JsonKey.ID, System.nanoTime());
		itemData.put(JsonKey.TIME, System.currentTimeMillis()/1000L);
		itemData.put(JsonKey.MESSAGE, "Welcome!");
		data.put(itemData);
		msg.put(JsonKey.COMMAND, "welcome");
		msg.put(JsonKey.DATA, data);
		msg.put(JsonKey.SESSION_ID, this.sessionID);
		return msg;
	}

    @OnMessage 
    public void onMessage(String messageReceived) 
    {	
    	broadcast(messageReceived, this.sessionID);
    }

    @OnClose
    public void onClose(Session session) 
    {
        listeners.remove(this);
    }

    @OnError
    public void onError(Session session, Throwable throwable) 
    {
    	listeners.remove(this);
    }
    
    public static void broadcast(String message) 
    {
    	broadcast(message, "");	
	}

    public static void broadcast(String message, String senderID) 
    {
        for (ServerWebSocketManager listener : listeners) 
        {
            try 
            {
            	if(!listener.sessionID.equals(senderID))
				{
            		listener.sendMessage(message);
				}
			} 
            catch (IOException e) 
            {
            	listeners.remove(listener);
			}
        }
    } 
    
    public static void broadcast(String message, String senderID, String path) 
    {
        for (ServerWebSocketManager listener : listeners) 
        {
            try 
            {
            	if(!listener.sessionID.equals(senderID) && listener.path.endsWith(path))
				{
            		listener.sendMessage(message);
				}
			} 
            catch (IOException e) 
            {
            	listeners.remove(listener);
			}
        }
    }

    private void sendMessage(String message) throws IOException 
    {
        this.session.getBasicRemote().sendText(message);
    }
  
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	public Map<String, List<String>> getRequestHeader() {
		return requestHeader;
	}

	public void setRequestHeader(Map<String, List<String>> requestHeader) {
		this.requestHeader = requestHeader;
	}

	public Map<String, List<String>> getResponseHeader() {
		return responseHeader;
	}

	public void setResponseHeader(Map<String, List<String>> responseHeader) {
		this.responseHeader = responseHeader;
	}

	public Map<String, List<String>> getParameter() {
		return parameter;
	}

	public void setParameter(Map<String, List<String>> parameter) {
		this.parameter = parameter;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
    
}

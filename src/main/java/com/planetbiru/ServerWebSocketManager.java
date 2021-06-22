package com.planetbiru;

import java.io.IOException;
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
import com.planetbiru.constant.JsonKey;
import com.planetbiru.gsm.SMSUtil;
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

	private Session session;
	private String clientIP = "";
	private Map<String, List<String>> requestHeader = new HashMap<>();
	private Map<String, List<String>> responseHeader = new HashMap<>();
	private Map<String, List<String>> parameter = new HashMap<>();
	private String sessionID = "";
    private String username = "";
    private String channel = "";	
	
	private static Set<ServerWebSocketManager> listeners = new CopyOnWriteArraySet<>();
    
    Random rand = new Random();  
    
    @PostConstruct
    public void init()
    {
    	Config.setUserSettingPath(userSettingPath);
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
        
        this.requestHeader = requestHdr;
        this.responseHeader = responseHdr;
        this.parameter = param;
        this.sessionID = Utility.sha1(""+System.currentTimeMillis()+rand.nextInt(1000000000));
        
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

	
	
    private void sendServerStatus() 
    {
		JSONArray data = new JSONArray();
		JSONObject info = new JSONObject();
		
		JSONObject modem = new JSONObject();
		modem.put(JsonKey.NAME, "modem_connected");
		modem.put(JsonKey.VALUE, SMSUtil.isConnected());
		data.put(modem);
		JSONObject wsEnable = new JSONObject();
		wsEnable.put(JsonKey.NAME, "ws_enable");
		wsEnable.put(JsonKey.VALUE, ConfigFeederWS.isFeederWsEnable());
		data.put(wsEnable);
		
		JSONObject wsConnected = new JSONObject();
		wsConnected.put(JsonKey.NAME, "ws_connected");
		wsConnected.put(JsonKey.VALUE, ConfigFeederWS.isConnected());
		data.put(wsConnected);
		
		JSONObject amqpEnable = new JSONObject();
		amqpEnable.put(JsonKey.NAME, "amqp_enable");
		amqpEnable.put(JsonKey.VALUE, ConfigFeederAMQP.isFeederAmqpEnable());
		data.put(amqpEnable);
		
		JSONObject amqpConnected = new JSONObject();
		amqpConnected.put(JsonKey.NAME, "amqp_connected");
		amqpConnected.put(JsonKey.VALUE, ConfigFeederAMQP.isConnected());
		data.put(amqpConnected);
		
		JSONObject httpEnable = new JSONObject();
		httpEnable.put(JsonKey.NAME, "http_enable");
		httpEnable.put(JsonKey.VALUE, ConfigAPI.isHttpEnable() || ConfigAPI.isHttpsEnable());
		data.put(httpEnable);
		
		info.put("command", "server-info");
		info.put("data", data);
		
		try {
			this.sendMessage(info.toString(4));
		} catch (JSONException | IOException e) {
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
    public void onMessage(String messageReceived) {	
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

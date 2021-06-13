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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.planetbiru.constant.JsonKey;
import com.planetbiru.user.WebSocketUserAccount;
import com.planetbiru.user.NoUserRegisteredException;
import com.planetbiru.util.Utility;
import com.planetbiru.wstools.MessageDecoder;
import com.planetbiru.wstools.MessageEncoder;
import com.planetbiru.wstools.ServletAwareConfigurator;


@Component
@ServerEndpoint(value = "/websocket", 
	configurator = ServletAwareConfigurator.class,
	decoders = MessageDecoder.class, 
	encoders = MessageEncoder.class)
public class ServerWebSocket {
	
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
	
	private static Set<ServerWebSocket> listeners = new CopyOnWriteArraySet<>();
    
    private static Logger logger = LogManager.getLogger(ServerWebSocket.class);   
    
    Random rand = new Random();
    
    
    @PostConstruct
    public void init()
    {
    	WebSocketUserAccount.init(userSettingPath);
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
        	WebSocketUserAccount.getUserAccount().load();
        	auth = WebSocketUserAccount.getUserAccount().checkUserAuth(requestHdr);
		} 
        catch (NoUserRegisteredException e) 
        {
			/**
			 * Do nothing
			 */
        	logger.error(e.getMessage());
		}     
        
        if(auth)
        {
            listeners.add(this);
            this.sendWelcomeMessage();
        }
	}
    
    private void sendWelcomeMessage() {
		String welcomeMessage = this.createWelcomeMessage();	
		try 
		{
			this.sendMessage(welcomeMessage);
		} 
		catch (IOException e) 
		{
			/**
			 * Do nothing
			 */
		}
	}

	private String createWelcomeMessage() 
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
		return msg.toString(4);
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
        for (ServerWebSocket listener : listeners) 
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

package com.planetbiru.receiver.amqp;

import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import com.planetbiru.constant.JsonKey;
import com.planetbiru.gsm.GSMException;
import com.planetbiru.gsm.SMSUtil;

public class RabbitMQReceiver implements MessageListener{
	private Logger logger = LogManager.getLogger(RabbitMQReceiver.class);
	private CountDownLatch latch = new CountDownLatch(1);
	public void onMessage(Message messageRaw) 
	{
		if(messageRaw != null && messageRaw.getBody() != null)
		{
			String requestBody = new String(messageRaw.getBody());
			try
			{
				JSONObject requestJSON = new JSONObject(requestBody);
				JSONArray data = requestJSON.optJSONArray(JsonKey.DATA);
				if(data != null && !data.isEmpty())
				{
					for(int i = 0; i<data.length(); i++)
					{
						JSONObject datum = data.getJSONObject(i);
					    String receiver = datum.optString(JsonKey.RECEIVER, "");
					    String message = datum.optString(JsonKey.MESSAGE, "");
					    try 
					    {
							SMSUtil.sendSMS(receiver, message);
						} 
					    catch (GSMException e) 
					    {
							logger.error(e.getMessage());
						}
					}
				}
			}
			catch(JSONException e)
			{
				logger.error(e.getMessage());
			}
			this.latch.countDown();
		}
	}

}

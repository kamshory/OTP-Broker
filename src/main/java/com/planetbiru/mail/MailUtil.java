package com.planetbiru.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import com.planetbiru.config.ConfigEmail;

public class MailUtil {
	private static int counter = -1;
	private static List<Integer> activeAccounts = new ArrayList<>();

	public void send(String receiver, String subject, String message) throws MessagingException {
		int index = MailUtil.getIndex();
		ConfigEmail.getAccounts().get(index).send(receiver, subject, message);
	}

	private static int getIndex() {
		MailUtil.counter++;
		if(MailUtil.counter >= MailUtil.activeAccounts.size())
		{
			MailUtil.counter = 0;
		}
		return MailUtil.activeAccounts.get(MailUtil.counter).intValue();
	}

	private static void reindex() {
		List<Integer> activeAccounts = new ArrayList<>();
		for(int i = 0; i<ConfigEmail.getAccounts().size(); i++)
		{
			if(ConfigEmail.getAccounts().get(i).isActive())
			{
				activeAccounts.add(i);
			}
		}
		MailUtil.activeAccounts = activeAccounts;
	}
	public static void updateIndex() {
		MailUtil.reindex();
		
	}
	
}

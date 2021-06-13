package com.planetbiru.user;

public class WebSocketUserAccount {
	private static WebUserAccount userAccount = new WebUserAccount();
	private WebSocketUserAccount()
	{
		
	}
	public static void init(String path)
	{
		WebSocketUserAccount.getUserAccount().init(path);
	}
	public static WebUserAccount getUserAccount() {
		return userAccount;
	}
	public static void setUserAccount(WebUserAccount userAccount) {
		WebSocketUserAccount.userAccount = userAccount;
	}
}

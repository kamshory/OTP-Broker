package com.planetbiru.user;

public class Account {
	private Account()
	{
		
	}
	private static UserAccount userAccount = new UserAccount();
	public static void init(String path)
	{
		Account.getUserAccount().init(path);
	}
	public static UserAccount getUserAccount() {
		return userAccount;
	}
	public static void setUserAccount(UserAccount userAccount) {
		Account.userAccount = userAccount;
	}
}

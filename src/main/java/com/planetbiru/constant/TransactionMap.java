package com.planetbiru.constant;

import java.util.HashMap;
import java.util.Map;

public class TransactionMap {
	private static Map<String, String> mapTransactionCode = new HashMap<>();
	private static boolean mapInitialized = false;
	
	private TransactionMap()
	{
		
	}
	public static void initMap()
	{
		mapTransactionCode.put("CASH_DEPOSIT",                  "01");
		mapTransactionCode.put("CASH_WITHDRAWALS",              "02");
		mapTransactionCode.put("FUND_TRANSFER",                 "03");
		mapTransactionCode.put("FUND_TRANSFER_IN",              "04");
		mapTransactionCode.put("FUND_TRANSFER_OUT",             "05");
		mapTransactionCode.put("CORRECTION_CREDIT",             "06");
		mapTransactionCode.put("CORRECTION_DEBIT",              "07");
		mapTransactionCode.put("PROFIT_SHARING",                "08");
		mapTransactionCode.put("MEMBER_PROFIT_SHARING",         "09");
		mapTransactionCode.put("TAX",                           "10");
		mapTransactionCode.put("ADMINISTRATIVE_COST",           "11");
		mapTransactionCode.put("DORMAN_ACCOUNT_ACTIVATION",     "12");
		mapTransactionCode.put("CASH_INSTALLMENT_PAYMENT",      "50");
		mapTransactionCode.put("AUTODEBIT_INSTALLMENT_PAYMENT", "51");
		mapTransactionCode.put("PARTIAL_INSTALLMENT_PAYMENT",   "52");
		mapTransactionCode.put("COLLATERAL_SALES",              "13");
	
		mapTransactionCode.put("PRINCIPAL_DEPOSIT",             "61");
		mapTransactionCode.put("MANDATORY_DEPOSIT",             "62");
		mapTransactionCode.put("OPTIONAL_DEPOSIT",              "63");
		mapTransactionCode.put("TRANSFER_FROM_MEMBER_SAVING",   "64");
	
		mapTransactionCode.put("PRINCIPAL_RETRACTION",          "66");
		mapTransactionCode.put("MANDATORY_RETRACTION",          "67");
		mapTransactionCode.put("OPTIONAL_RETRACTION",           "68");
		mapTransactionCode.put("ALL_RETRACTION",                "69");
	}
	public static Map<String, String> getMap()
	{
		if(!TransactionMap.mapInitialized)
		{
			initMap();
		}
		return mapTransactionCode;
	}
}

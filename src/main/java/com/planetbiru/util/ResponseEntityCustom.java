// ============================================================================
// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.planetbiru.util;

import org.springframework.http.HttpHeaders;

public class ResponseEntityCustom {
	private int statusCode = 0;
	private String body = "";
	private HttpHeaders responseHeaders = new HttpHeaders();
	
	public ResponseEntityCustom(String body, int statusCode)
	{
		this.body = body;
		this.statusCode = statusCode;
	}
	public ResponseEntityCustom(String body, int statusCode, HttpHeaders responseHeaders)
	{
		this.body = body;
		this.statusCode = statusCode;
		this.responseHeaders = responseHeaders;
	}
	public ResponseEntityCustom() {
		/**
		 * Do nothing
		 */
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public HttpHeaders getResponseHeaders() {
		return responseHeaders;
	}
	public void setResponseHeaders(HttpHeaders responseHeaders) {
		this.responseHeaders = responseHeaders;
	}
	public int getStatusCodeValue()
	{
		return this.statusCode;
	}
	
	
	
}

package com.planetbiru.receiver.ws;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.planetbiru.cookie.CookieServer;

public class WebSocketContent {

	private String fileName;
	private HttpHeaders responseHeaders;
	private byte[] responseBody;
	private HttpStatus statusCode;
	private CookieServer cookie;
	private String contentType;

	public WebSocketContent(String fileName, HttpHeaders responseHeaders, byte[] responseBody, HttpStatus statusCode, CookieServer cookie, String contentType) {
		this.fileName = fileName;
		this.responseHeaders = responseHeaders;
		this.responseBody = responseBody;
		this.statusCode = statusCode;
		this.cookie = cookie;
		this.contentType = contentType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public HttpHeaders getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(HttpHeaders responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

	public byte[] getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(byte[] responseBody) {
		this.responseBody = responseBody;
	}

	public HttpStatus getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(HttpStatus statusCode) {
		this.statusCode = statusCode;
	}

	public CookieServer getCookie() {
		return cookie;
	}

	public void setCookie(CookieServer cookie) {
		this.cookie = cookie;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	
}

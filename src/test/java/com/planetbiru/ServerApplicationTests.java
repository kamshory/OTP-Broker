package com.planetbiru;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.planetbiru.cookie.CookieItem;

@SpringBootTest
class ServerApplicationTests {

	@Test
	void contextLoads() {
		CookieItem cookieItem = new CookieItem();
		assertNull(cookieItem.getName());
	}

}

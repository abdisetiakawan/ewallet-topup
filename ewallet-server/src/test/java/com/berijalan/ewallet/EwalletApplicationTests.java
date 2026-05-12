package com.berijalan.ewallet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest(properties = "spring.data.redis.repositories.enabled=false")
class EwalletApplicationTests {

	@MockBean(name = "redisTemplate")
	private RedisTemplate<String, Object> redisTemplate;

	@Test
	void contextLoads() {
	}

}

package com.berijalan.ewallet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"spring.data.redis.repositories.enabled=false",
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.jpa.show-sql=false",
		"logging.level.org.hibernate.SQL=OFF"
})
class EwalletApplicationTests {

	@MockitoBean(name = "redisTemplate")
	private RedisTemplate<String, Object> redisTemplate;

	@Test
	void contextLoads() {
	}

}

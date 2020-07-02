package com.raghu.fta;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.test.context.ContextConfiguration;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@EnableEurekaClient
@EnableDiscoveryClient
@ContextConfiguration(classes = {FeatureToggleApplicationTests.class})
class FeatureToggleApplicationTests {

	@Test
	void contextLoads() {
	}
	@Test
	void findAnyVsFindFirstTest(){
		System.out.println(IntStream.range(0, 100).findAny());
		System.out.println(IntStream.range(0, 100).findAny());
		System.out.println(IntStream.range(0, 100).findAny());
		System.out.println(IntStream.range(0, 100).findAny());
		System.out.println(IntStream.range(0, 100).findFirst());
	}

}

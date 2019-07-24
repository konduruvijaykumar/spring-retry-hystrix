package org.pjay.retry.hystrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

@SpringBootApplication
@EnableCircuitBreaker
public class SpringRetryHystrixService1Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringRetryHystrixService1Application.class, args);
	}

}

/**
 * 
 */
package org.pjay.retry.hystrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * @author vijayk
 *
 */
@Service
public class SpringRetryHystrixService1SimpleService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	// Random random = new Random();

	@HystrixCommand(fallbackMethod = "getHystrixIntServiceFallback")
	public int getHystrixIntService() throws Exception {
		log.info(" ## getHystrixIntService() ##");
		if (Math.random() > 0.5) {
			/*
			 * if (!random.nextBoolean()) condition is not generating many failures to see
			 * circuit breaker permanently open.
			 */
			// Sleep for some time to observe difference between success and failure
			Thread.sleep(1000 * 2);
			throw new RuntimeException("Service failed moving to fallback method");
		}
		// One is success and Zero is failure
		return 1;
	}

	@SuppressWarnings("unused")
	private int getHystrixIntServiceFallback() {
		log.info(" ## getHystrixIntServiceFallback() ##");
		return 0;
	}

}

/**
 * 
 */
package org.pjay.retry.hystrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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

	// delay = 1000 is the default value.
	@Retryable(include = { Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 1500))
	public int getRetryIntService() throws Exception {
		log.info(" ## getRetryIntService() ##");
		if (Math.random() > 0.5) {
			Thread.sleep(1000 * 2);
			throw new RuntimeException("Service failed when executing getRetryIntService() method");
		}
		// One is success and Zero is failure
		return 1;
	}

	@Recover
	private int recoverFromException(Exception exception) {
		log.info(" ## recoverFromException(Exception exception) ##");
		return 0;
	}

	/*
	 * resetTimeout attribute: default value 20000 - If the circuit is open for
	 * longer than this timeout then it resets on the next call to give the
	 * downstream component
	 * 
	 * openTimeout attribute: default value is 5000 - When maxAttempts() failures
	 * are reached within this timeout, the circuit is opened automatically,
	 * preventing access to the downstream component.
	 */
	// Same recover method above will be reused by @CircuitBreaker also
	@CircuitBreaker(include = { Exception.class }, maxAttempts = 2)
	public int getRetryCircuitBrreakerIntService() throws Exception {
		log.info(" ## getRetryCircuitBrreakerIntService() ##");
		if (Math.random() > 0.5) {
			Thread.sleep(1000 * 3);
			throw new RuntimeException("Service failed when executing getRetryIntService() method");
		}
		// One is success and Zero is failure
		return 1;
	}

}

/**
 * 
 */
package org.pjay.retry.hystrix;

import static org.pjay.retry.hystrix.ApplicationConstants.URL_SERVICE2_TIMEOUT;
import static org.pjay.retry.hystrix.ApplicationConstants.URL_UNKNOWN_HOST_API;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * @author vijayk
 *
 */
@RestController
public class SpringRetryHystrixService1Controller {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	@Qualifier("customRestTemplate")
	private RestTemplate customRestTemplate;

	@Value("${hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds:10000}")
	private String hystrixCBTimeout;

	@GetMapping(value = { "/", "/home", "/hello" })
	public String hello() {
		return "Hi there from Spring Retry Hystrix Service 1";
	}

	// https://stackoverflow.com/questions/45943715/how-to-configure-delay-time-in-spring-retry-spring-boot
	// https://www.baeldung.com/spring-retry
	/*
	 * @Backoff attributes: delay - is the delay between retry attempts when an
	 * exception occurs (default value is 1000).
	 * 
	 * @Backoff attributes: maxDelay - is the maximum delay or wait between retries.
	 * 
	 * @Backoff attributes: multiplier - a multiplier is used to use to calculate
	 * the next backoff delay (default 0 = ignored) - See example below. If delay =
	 * 1000, maxAttempts = 3 and multiplier = 2. Then 1st attempt delay = 1000, 2nd
	 * = (1000 * 2) is 2000, 3rd = (2000 * 2) is 4000
	 */
	@GetMapping("retry-service-not-running")
	@Retryable(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 2000))
	public ResponseEntity<Result> retryWithNoServiceRunning() {
		log.info(" ## Start retryWithNoServiceRunning() ## ");
		log.info(" ## End retryWithNoServiceRunning() but resttemplate call processing ## ");
		return null;
	}

	@GetMapping("retry-no-service-api")
	@Retryable(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 2000))
	public ResponseEntity<Result> retryWithNoServiceAPI() {
		log.info(" ## Start retryWithNoServiceAPI() ## ");
		log.info(" ## End retryWithNoServiceAPI() but resttemplate call processing ## ");
		return null;
	}

	@GetMapping("retry-timeout-service/{timeOutMilliSec}")
	@Retryable(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 2000))
	public ResponseEntity<Result> retryWithTiemoutService(@PathVariable long timeOutMilliSec) {
		log.info(" ## Start retryWithTiemoutService(@PathVariable long timeOutMilliSec) ## ");
		log.info(" ## End retryWithTiemoutService(@PathVariable long timeOutMilliSec) but resttemplate call processing ## ");
		return null;
	}

	@GetMapping("retry-unknown-host")
	@Retryable(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 2000))
	public ResponseEntity<Result> retryWithUnknownHost() {
		log.info(" ## Start retryWithUnknownHost() ## ");
		log.info(" ## End retryWithUnknownHost() but resttemplate call processing ## ");
		return customRestTemplate.exchange(URL_UNKNOWN_HOST_API, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
	}

	@GetMapping("retry-test-resttemplate/{timeOutMilliSec}")
	@Retryable(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 2000))
	public ResponseEntity<Result> retryWithRestTemplate(@PathVariable long timeOutMilliSec) {
		log.info(" ## Start retryWithRestTemplate(@PathVariable long timeOutMilliSec) ## ");
		Map<String, String> params = new HashMap<String, String>();
		if (timeOutMilliSec < 0) {
			throw new RuntimeException("Time out value should be positive number");
		}
		params.put("timeOutMilliSec", (new Long(timeOutMilliSec)).toString());
		log.info(" ## End retryWithRestTemplate(@PathVariable long timeOutMilliSec) but resttemplate call processing ## ");
		return restTemplate.exchange(URL_SERVICE2_TIMEOUT, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				}, params);
	}

	@Recover
	private ResponseEntity<Result> recoverFromResourceAccessException(ResourceAccessException resourceAccessException) {
		log.info(" ## Start recoverFromResourceAccessException(ResourceAccessException resourceAccessException) ## ");
		Result result = new Result();
		resourceAccessException.printStackTrace();
		result.setMessage(resourceAccessException.getMessage());
		result.setStatus("I/O error");
		log.info(" ## End recoverFromResourceAccessException(ResourceAccessException resourceAccessException) ## ");
		return new ResponseEntity<>(result, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Recover
	private ResponseEntity<Result> recoverFromRestClientResponseException(
			RestClientResponseException restClientResponseException) {
		log.info(
				" ## Start recoverFromRestClientResponseException(RestClientResponseException restClientResponseException) ## ");
		Result result = new Result();
		restClientResponseException.printStackTrace();
		result.setMessage(restClientResponseException.getMessage());
		result.setStatus(restClientResponseException.getStatusText());
		log.info(
				" ## End recoverFromRestClientResponseException(RestClientResponseException restClientResponseException) ## ");
		return new ResponseEntity<>(result, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Recover
	private ResponseEntity<Result> recoverFromException(Exception exception) {
		log.info(" ## Start recoverFromException(Exception exception) ## ");
		Result result = new Result();
		exception.printStackTrace();
		result.setMessage(exception.getMessage());
		result.setStatus("Unknown");
		log.info(" ## End recoverFromException(Exception exception) ## ");
		return new ResponseEntity<>(result, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@GetMapping("test-resttemplate/{timeOutMilliSec}")
	public ResponseEntity<Result> testRestTemplate(@PathVariable long timeOutMilliSec) {
		log.info(" ## Start testRestTemplate(@PathVariable long timeOutMilliSec) ## ");
		Map<String, String> params = new HashMap<String, String>();
		if (timeOutMilliSec < 0) {
			throw new RuntimeException("Time out value should be positive number");
		}
		params.put("timeOutMilliSec", (new Long(timeOutMilliSec)).toString());
		log.info(" ## End testRestTemplate(@PathVariable long timeOutMilliSec) but resttemplate call processing ## ");
		return restTemplate.exchange(URL_SERVICE2_TIMEOUT, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				}, params);
	}

	@GetMapping("test-customresttemplate/{timeOutMilliSec}")
	public ResponseEntity<Result> testCustomRestTemplate(@PathVariable long timeOutMilliSec) {
		log.info(" ## Start testCustomRestTemplate(@PathVariable long timeOutMilliSec) ## ");
		Map<String, String> params = new HashMap<String, String>();
		if (timeOutMilliSec < 0) {
			throw new RuntimeException("Time out value should be positive number");
		}
		params.put("timeOutMilliSec", (new Long(timeOutMilliSec)).toString());
		log.info(" ## End testCustomRestTemplate(@PathVariable long timeOutMilliSec) but resttemplate call processing ## ");
		return customRestTemplate.exchange(URL_SERVICE2_TIMEOUT, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				}, params);
	}

	@GetMapping("test-hystrix-cb/{timeOutMilliSec}")
	// @formatter:off
	/*
	@HystrixCommand(fallbackMethod = "testHystrixCBFallback", commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10000") })
	*/
	
	/*
	@HystrixCommand(fallbackMethod = "testHystrixCBFallback", commandProperties = {
			@HystrixProperty(name = HYSTRIX_EXECUTION_TIMEOUT_IN_MILLISECONDS, value = HYSTRIX_TIMEOUT_VALUE) })
	*/
	/*
	@HystrixCommand(fallbackMethod = "testHystrixCBFallback", commandKey = "testHystrixCB")
	 */
	// @formatter:on
	// Exceptions are transferred back to fallback method, without waiting for
	// timeout, some exceptions can be ignored using attribute ignoreExceptions
	@HystrixCommand(fallbackMethod = "testHystrixCBFallback")
	public ResponseEntity<Result> testHystrixCircuitBreaker(@PathVariable long timeOutMilliSec) {
		log.info(" ## Start testHystrixCircuitBreaker(@PathVariable long timeOutMilliSec) ## ");
		Map<String, String> params = new HashMap<String, String>();
		if (timeOutMilliSec < 0) {
			throw new RuntimeException("Time out value should be positive number");
		}
		params.put("timeOutMilliSec", (new Long(timeOutMilliSec)).toString());
		log.info(" ## End testHystrixCircuitBreaker(@PathVariable long timeOutMilliSec) but resttemplate call processing ## ");
		return restTemplate.exchange(URL_SERVICE2_TIMEOUT, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				}, params);
	}

	@SuppressWarnings("unused")
	private ResponseEntity<Result> testHystrixCBFallback(long timeOutMilliSec) {
		log.info(" ## Start testHystrixCBFallback(long timeOutMilliSec) ## ");
		Result result = new Result();
		result.setMessage(
				"Response returned from testHystrixCBFallback method as we did not receive response from external service by configured time out "
						+ /* HYSTRIX_TIMEOUT_VALUE */ hystrixCBTimeout + " milli seconds, where as passed time out is "
						+ timeOutMilliSec + " milli seconds");
		log.info(" ## End testHystrixCBFallback(long timeOutMilliSec) ## ");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

}

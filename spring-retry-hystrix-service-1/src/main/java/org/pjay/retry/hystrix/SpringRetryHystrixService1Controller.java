/**
 * 
 */
package org.pjay.retry.hystrix;

import static org.pjay.retry.hystrix.ApplicationConstants.URL_SERVICE2_EXCEPTION;
import static org.pjay.retry.hystrix.ApplicationConstants.URL_SERVICE2_NO_API;
import static org.pjay.retry.hystrix.ApplicationConstants.URL_SERVICE2_RANDOM_EXCEPTION;
import static org.pjay.retry.hystrix.ApplicationConstants.URL_SERVICE2_SUCCESS;
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
import org.springframework.retry.annotation.CircuitBreaker;
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
	 * 
	 * value or include attribute: is not provided (excludes attribute: is also
	 * empty) then retry is done for all exceptions. If any specific exceptions are
	 * mentioned then that exceptions are only retried
	 */
	@GetMapping("retry-service-not-running")
	@Retryable(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 2000))
	public ResponseEntity<Result> retryWithNoServiceRunning() {
		log.info(" ## Start retryWithNoServiceRunning() ## ");
		Result result = null;
		ResponseEntity<Result> responseEntity = customRestTemplate.exchange(URL_SERVICE2_SUCCESS, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
		if (null != responseEntity && null != responseEntity.getBody()) {
			result = responseEntity.getBody();
			result.setMessage("Response returned from external service-2. Please stop service-2 and test this API");
		} else {
			result = new Result();
			result.setMessage("No idea on how did i reach this block. This issue needs to be fixed");
		}
		log.info(" ## End retryWithNoServiceRunning() ## ");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/*
	 * My understanding on @CircuitBreaker annotation
	 * 
	 * CircuitBreaker will not do a retry in case of exception, it will directly go
	 * to Recover block.
	 * 
	 * CircuitBreaker is itself annotated with @Retryable(stateful = true), which
	 * helps in maintaining the data about failures, number of attempts etc for all
	 * requests for a particular method.
	 * 
	 * maxAttempts attribute: It is the maximum number of attempts (including the
	 * first failure), defaults to 3
	 * 
	 * openTimeout attribute: When maxAttempts() failures are reached within this
	 * timeout, the circuit is opened automatically, preventing access to the
	 * downstream component. This where the whole trick lies in, for example if
	 * maxAttempts = 4, resetTimeout = 18000 (18 sec's) and openTimeout = 6000 (6
	 * sec's). If the number of failures are reaching the maxAttempt (4) within
	 * openTimeout (6 sec's), then the CircuitBreaker is open, will wait till
	 * resetTimeout (18 sec's) and close the circuit.
	 * 
	 * resetTimeout attribute: If the circuit is open for longer than this timeout
	 * then it resets on the next call to give the downstream component a chance to
	 * respond again.
	 */
	@GetMapping("cb-service-not-running")
	@CircuitBreaker(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2, resetTimeout = 25000, openTimeout = 5000)
	public ResponseEntity<Result> cbWithNoServiceRunning() {
		log.info(" ## Start cbWithNoServiceRunning() ## ");
		Result result = null;
		ResponseEntity<Result> responseEntity = restTemplate.exchange(URL_SERVICE2_SUCCESS, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
		if (null != responseEntity && null != responseEntity.getBody()) {
			result = responseEntity.getBody();
			result.setMessage("Response returned from external service-2. Please stop service-2 and test this API");
		} else {
			result = new Result();
			result.setMessage("No idea on how did i reach this block. This issue needs to be fixed");
		}
		log.info(" ## End cbWithNoServiceRunning() ## ");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("retry-no-service-api")
	@Retryable(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 2000))
	public ResponseEntity<Result> retryWithNoServiceAPI() {
		log.info(" ## Start retryWithNoServiceAPI() ## ");
		log.info(" ## End retryWithNoServiceAPI() but resttemplate call processing ## ");
		return customRestTemplate.exchange(URL_SERVICE2_NO_API, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
	}

	@GetMapping("cb-no-service-api")
	@CircuitBreaker(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2)
	public ResponseEntity<Result> cbWithNoServiceAPI() {
		log.info(" ## Start cbWithNoServiceAPI() ## ");
		log.info(" ## End cbWithNoServiceAPI() but resttemplate call processing ## ");
		return restTemplate.exchange(URL_SERVICE2_NO_API, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
	}

	@GetMapping("retry-timeout-service/{timeOutMilliSec}")
	@Retryable(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 2000))
	public ResponseEntity<Result> retryWithTiemoutService(@PathVariable long timeOutMilliSec) {
		log.info(" ## Start retryWithTiemoutService(@PathVariable long timeOutMilliSec) ## ");
		Map<String, String> params = new HashMap<String, String>();
		if (timeOutMilliSec < 0) {
			throw new RuntimeException("Time out value should be positive number");
		}
		params.put("timeOutMilliSec", (new Long(timeOutMilliSec)).toString());
		log.info(
				" ## End retryWithTiemoutService(@PathVariable long timeOutMilliSec) but resttemplate call processing ## ");
		return customRestTemplate.exchange(URL_SERVICE2_TIMEOUT, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				}, params);
	}

	@GetMapping("cb-timeout-service/{timeOutMilliSec}")
	@CircuitBreaker(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 4)
	public ResponseEntity<Result> cbWithTiemoutService(@PathVariable long timeOutMilliSec) {
		log.info(" ## Start cbWithTiemoutService(@PathVariable long timeOutMilliSec) ## ");
		Map<String, String> params = new HashMap<String, String>();
		if (timeOutMilliSec < 0) {
			throw new RuntimeException("Time out value should be positive number");
		}
		params.put("timeOutMilliSec", (new Long(timeOutMilliSec)).toString());
		log.info(
				" ## End cbWithTiemoutService(@PathVariable long timeOutMilliSec) but resttemplate call processing ## ");
		/*
		 * return customRestTemplate.exchange(URL_SERVICE2_TIMEOUT, HttpMethod.GET,
		 * null, new ParameterizedTypeReference<Result>() { }, params);
		 */
		// As @CircuitBreaker never breaks on time, but breaks on maxAttempts in a given
		// window openTimeout. So better use custom rest template for closing connection
		return restTemplate.exchange(URL_SERVICE2_TIMEOUT, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				}, params);
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

	@GetMapping("cb-unknown-host")
	@CircuitBreaker(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 4)
	public ResponseEntity<Result> cbWithUnknownHost() {
		log.info(" ## Start cbWithUnknownHost() ## ");
		log.info(" ## End cbWithUnknownHost() but resttemplate call processing ## ");
		return restTemplate.exchange(URL_UNKNOWN_HOST_API, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
	}

	@GetMapping("retry-random-exception")
	@Retryable(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 2000))
	public ResponseEntity<Result> retryWithRandomException() {
		log.info(" ## Start retryWithRandomException() ## ");
		log.info(" ## End retryWithRandomException() but resttemplate call processing ## ");
		return customRestTemplate.exchange(URL_SERVICE2_RANDOM_EXCEPTION, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
	}

	@GetMapping("cb-random-exception")
	@CircuitBreaker(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 3)
	public ResponseEntity<Result> cbWithRandomException() {
		log.info(" ## Start cbWithRandomException() ## ");
		log.info(" ## End cbWithRandomException() but resttemplate call processing ## ");
		return restTemplate.exchange(URL_SERVICE2_RANDOM_EXCEPTION, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
	}

	@GetMapping("retry-exception")
	@Retryable(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 2, backoff = @Backoff(delay = 2000))
	public ResponseEntity<Result> retryWithException() {
		log.info(" ## Start retryWithException() ## ");
		log.info(" ## End retryWithException() but resttemplate call processing ## ");
		return customRestTemplate.exchange(URL_SERVICE2_EXCEPTION, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
	}

	@GetMapping("cb-exception")
	@CircuitBreaker(value = { ResourceAccessException.class, RestClientResponseException.class,
			Exception.class }, maxAttempts = 4)
	public ResponseEntity<Result> cbWithException() {
		log.info(" ## Start cbWithException() ## ");
		log.info(" ## End cbWithException() but resttemplate call processing ## ");
		return restTemplate.exchange(URL_SERVICE2_EXCEPTION, HttpMethod.GET, null,
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
		log.info(
				" ## End retryWithRestTemplate(@PathVariable long timeOutMilliSec) but resttemplate call processing ## ");
		return restTemplate.exchange(URL_SERVICE2_TIMEOUT, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				}, params);
	}

	@Recover
	private ResponseEntity<Result> recoverFromResourceAccessException(ResourceAccessException resourceAccessException) {
		log.info(" ## Start recoverFromResourceAccessException(ResourceAccessException resourceAccessException) ## ");
		Result result = new Result();
		// resourceAccessException.printStackTrace();
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
		// restClientResponseException.printStackTrace();
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
		// exception.printStackTrace();
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
		log.info(
				" ## End testCustomRestTemplate(@PathVariable long timeOutMilliSec) but resttemplate call processing ## ");
		return customRestTemplate.exchange(URL_SERVICE2_TIMEOUT, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				}, params);
	}

	@GetMapping("test-hystrix-timeout/{timeOutMilliSec}")
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
	public ResponseEntity<Result> testHystrixCircuitBreakerTimeout(@PathVariable long timeOutMilliSec) {
		log.info(" ## Start testHystrixCircuitBreaker(@PathVariable long timeOutMilliSec) ## ");
		Map<String, String> params = new HashMap<String, String>();
		if (timeOutMilliSec < 0) {
			throw new RuntimeException("Time out value should be positive number");
		}
		params.put("timeOutMilliSec", (new Long(timeOutMilliSec)).toString());
		log.info(
				" ## End testHystrixCircuitBreaker(@PathVariable long timeOutMilliSec) but resttemplate call processing ## ");
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

	@GetMapping("hystrix-service-not-running")
	@HystrixCommand(fallbackMethod = "hystrixCBFallback")
	public ResponseEntity<Result> hystrixWithNoServiceRunning() {
		log.info(" ## Start hystrixWithNoServiceRunning() ## ");
		Result result = null;
		ResponseEntity<Result> responseEntity = restTemplate.exchange(URL_SERVICE2_SUCCESS, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
		if (null != responseEntity && null != responseEntity.getBody()) {
			result = responseEntity.getBody();
			result.setMessage("Response returned from external service-2. Please stop service-2 and test this API");
		} else {
			result = new Result();
			result.setMessage("No idea on how did i reach this block. This issue needs to be fixed");
		}
		log.info(" ## End hystrixWithNoServiceRunning() ## ");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("hystrix-no-service-api")
	@HystrixCommand(fallbackMethod = "hystrixCBFallback")
	public ResponseEntity<Result> hystrixWithNoServiceAPI() {
		log.info(" ## Start hystrixWithNoServiceAPI() ## ");
		log.info(" ## End hystrixWithNoServiceAPI() but resttemplate call processing ## ");
		return restTemplate.exchange(URL_SERVICE2_NO_API, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
	}

	@GetMapping("hystrix-unknown-host")
	@HystrixCommand(fallbackMethod = "hystrixCBFallback")
	public ResponseEntity<Result> hystrixWithUnknownHost() {
		log.info(" ## Start hystrixWithUnknownHost() ## ");
		log.info(" ## End hystrixWithUnknownHost() but resttemplate call processing ## ");
		return restTemplate.exchange(URL_UNKNOWN_HOST_API, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
	}

	@GetMapping("hystrix-random-exception")
	@HystrixCommand(fallbackMethod = "hystrixCBFallback")
	public ResponseEntity<Result> hystrixWithRandomException() {
		log.info(" ## Start hystrixWithRandomException() ## ");
		log.info(" ## End hystrixWithRandomException() but resttemplate call processing ## ");
		return restTemplate.exchange(URL_SERVICE2_RANDOM_EXCEPTION, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
	}

	@GetMapping("hystrix-exception")
	@HystrixCommand(fallbackMethod = "hystrixCBFallback")
	public ResponseEntity<Result> hystrixWithException() {
		log.info(" ## Start hystrixWithException() ## ");
		log.info(" ## End hystrixWithException() but resttemplate call processing ## ");
		return restTemplate.exchange(URL_SERVICE2_EXCEPTION, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				});
	}

	@SuppressWarnings("unused")
	private ResponseEntity<Result> hystrixCBFallback() {
		log.info(" ## Start hystrixCBFallback() ## ");
		Result result = new Result();
		result.setMessage(
				"Response returned from hystrixCBFallback method, this could be caused by exception/did not receive response from external service by configured time out "
						+ hystrixCBTimeout + " milli seconds");
		log.info(" ## End hystrixCBFallback() ## ");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

}

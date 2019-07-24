/**
 * 
 */
package org.pjay.retry.hystrix;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

/**
 * @author vijayk
 *
 */
@RestController
public class SpringRetryHystrixService1Controller {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	@Qualifier("customRestTemplate")
	private RestTemplate customRestTemplate;

	@GetMapping(value = { "/", "/home", "/hello" })
	public String hello() {
		return "Hi there from Spring Retry Hystrix Service 1";
	}

	@GetMapping("test-resttemplate/{timeOutMilliSec}")
	public ResponseEntity<Result> testRestTemplate(@PathVariable long timeOutMilliSec) {
		Map<String, String> params = new HashMap<String, String>();
		if (timeOutMilliSec < 0) {
			throw new RuntimeException("Time out value should be positive number");
		}
		params.put("timeOutMilliSec", (new Long(timeOutMilliSec)).toString());
		return restTemplate.exchange(ApplicationConstants.URL_SERVICE2_TIMEOUT, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				}, params);
	}

	@GetMapping("test-customresttemplate/{timeOutMilliSec}")
	public ResponseEntity<Result> testCustomRestTemplate(@PathVariable long timeOutMilliSec) {
		Map<String, String> params = new HashMap<String, String>();
		if (timeOutMilliSec < 0) {
			throw new RuntimeException("Time out value should be positive number");
		}
		params.put("timeOutMilliSec", (new Long(timeOutMilliSec)).toString());
		return customRestTemplate.exchange(ApplicationConstants.URL_SERVICE2_TIMEOUT, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				}, params);
	}

	@GetMapping("test-hystrix-cb/{timeOutMilliSec}")
	@HystrixCommand(fallbackMethod = "testHystrixCBFallback", commandProperties = {
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10000") })
	public ResponseEntity<Result> testHystrixCircuitBreaker(@PathVariable long timeOutMilliSec) {
		Map<String, String> params = new HashMap<String, String>();
		if (timeOutMilliSec < 0) {
			throw new RuntimeException("Time out value should be positive number");
		}
		params.put("timeOutMilliSec", (new Long(timeOutMilliSec)).toString());
		return customRestTemplate.exchange(ApplicationConstants.URL_SERVICE2_TIMEOUT, HttpMethod.GET, null,
				new ParameterizedTypeReference<Result>() {
				}, params);
	}

	@SuppressWarnings("unused")
	private ResponseEntity<Result> testHystrixCBFallback(long timeOutMilliSec) {
		Result result = new Result();
		result.setMessage(
				"Response returned from testHystrixCBFallback method as we did not receive response from external service by configured time out "
						+ 10000 + " milli seconds, where as passed time out is " + timeOutMilliSec + " milli seconds");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

}

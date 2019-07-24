/**
 * 
 */
package org.pjay.retry.hystrix;

import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author vijayk
 *
 */
@RestController
public class SpringRetryHystrixService2Controller {

	Random random = new Random();

	@GetMapping(value = { "/", "/home", "/hello" })
	public String hello() {
		return "Hi there from Spring Retry Hystrix Service 2";
	}

	@GetMapping("service2-timeout/{timeOutMilliSec}")
	public ResponseEntity<Result> service2Timeout(@PathVariable long timeOutMilliSec) {
		try {
			Thread.sleep(timeOutMilliSec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Result result = new Result();
		result.setMessage(
				"Response from service2-timeout API with time out sent as " + timeOutMilliSec + " milli seconds");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("service2-success")
	public ResponseEntity<Result> service2Success() {
		Result result = new Result();
		result.setMessage("Response from service2-success API received");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("service2-exception")
	public ResponseEntity<Result> service2Exception() {
		@SuppressWarnings("unused")
		Result result = new Result();
		// oops some runtime exception occurred during the method execution
		throw new RuntimeException("OOPS some runtime exception occurred during the service2-exception API execution");
		// result.setMessage("Response from service2 exception API is received");
		// return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@GetMapping("service2-random-exception")
	public ResponseEntity<Result> service2RandomException() {
		Result result = new Result();
		if (random.nextBoolean()) {
			// oops some runtime exception occurred during the method execution
			throw new RuntimeException(
					"OOPS some runtime exception occurred during the service2-random-exception API execution");
		}
		result.setMessage("Response from service2 random exception API received");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

}

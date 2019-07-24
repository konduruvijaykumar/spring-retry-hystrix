/**
 * 
 */
package org.pjay.retry.hystrix;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author vijayk
 *
 */
@RestController
public class SpringRetryHystrixService2Controller {

	@GetMapping(value = {"/", "/home", "/hello"})
	public String hello() {
		return "Hi there from Spring Retry Hystrix Service 2";
	}

}

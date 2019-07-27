/**
 * 
 */
package org.pjay.retry.hystrix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author vijayk
 *
 */
@RestController
public class SpringRetryHystrixService1SimpleController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	SpringRetryHystrixService1SimpleService springRetryHystrixService1SimpleService;

	@GetMapping("/simple-hystrix-getint")
	public int hystrixGetInt() throws Exception {
		log.info(" ## hystrixGetInt() ##");
		return springRetryHystrixService1SimpleService.getHystrixIntService();
	}

	@GetMapping("/simple-retry-getint")
	public int retryGetInt() throws Exception {
		return springRetryHystrixService1SimpleService.getRetryIntService();
	}
	
	@GetMapping("/simple-retry-cb-getint")
	public int retryCircuitBreakerGetInt() throws Exception {
		return springRetryHystrixService1SimpleService.getRetryCircuitBrreakerIntService();
	}

}

package org.pjay.retry.hystrix;

import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.retry.annotation.EnableRetry;

import rx.schedulers.Schedulers;

@SpringBootApplication
@EnableCircuitBreaker
@EnableRetry
public class SpringRetryHystrixService1Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringRetryHystrixService1Application.class, args);
	}

	// https://github.com/Netflix/Hystrix/issues/1766
	// This was done for below issue shown up due to Hystrix Circuit breaker code, during server shutdown
	/*
	 * 2019-07-24 18:17:24.855 WARN 20884 --- [n(14)-127.0.0.1]
	 * o.a.c.loader.WebappClassLoaderBase : The web application [ROOT] appears to
	 * have started a thread named [RxIoScheduler-1 (Evictor)] but has failed to
	 * stop it. This is very likely to create a memory leak. Stack trace of thread:
	 * sun.misc.Unsafe.park(Native Method)
	 * java.util.concurrent.locks.LockSupport.parkNanos(LockSupport.java:215)
	 */
	
	/*
	 * 2019-07-24 18:17:24.856 WARN 20884 --- [n(14)-127.0.0.1]
	 * o.a.c.loader.WebappClassLoaderBase : The web application [ROOT] appears to
	 * have started a thread named [RxComputationScheduler-1] but has failed to stop
	 * it. This is very likely to create a memory leak. Stack trace of thread:
	 * sun.misc.Unsafe.park(Native Method)
	 * java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	 */
	@PreDestroy
	public void shutdown() {
		Schedulers.shutdown();
	}

}

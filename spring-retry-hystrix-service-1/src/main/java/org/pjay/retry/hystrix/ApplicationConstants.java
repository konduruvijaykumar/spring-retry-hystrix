/**
 * 
 */
package org.pjay.retry.hystrix;

/**
 * @author vijayk
 *
 */
public class ApplicationConstants {
	
	public static final String API_HOST_URL = "http://localhost:8282/";
	public static final String URL_SERVICE2_TIMEOUT = API_HOST_URL + "service2-timeout/{timeOutMilliSec}";
	public static final String URL_SERVICE2_SUCCESS = API_HOST_URL + "service2-success";
	public static final String URL_SERVICE2_EXCEPTION = API_HOST_URL + "service2-exception";
	public static final String URL_SERVICE2_RANDOM_EXCEPTION = API_HOST_URL + "service2-random-exception";
	
}

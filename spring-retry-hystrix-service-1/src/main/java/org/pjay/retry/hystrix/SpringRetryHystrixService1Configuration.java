/**
 * 
 */
package org.pjay.retry.hystrix;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author vijayk
 *
 */
@Configuration
public class SpringRetryHystrixService1Configuration {
	
	@Value("${custom.rest.template.connection.connection-request-timeout:10000}")
	private int connectionRequestTimeout;
	
	@Value("${custom.rest.template.connection.connect-timeout:10000}")
	private int connectionTimeout;
	
	@Value("${custom.rest.template.connection.read-timeout:10000}")
	private int readTimeout;

	@Bean
	@Primary
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Bean("customRestTemplate")
	public RestTemplate getCustomRestTemplate() {
		// HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		// httpComponentsClientHttpRequestFactory.setConnectionRequestTimeout(connectionRequestTimeout);
		// httpComponentsClientHttpRequestFactory.setConnectTimeout(connectionTimeout);
		// httpComponentsClientHttpRequestFactory.setReadTimeout(readTimeout);
		// return new RestTemplate(httpComponentsClientHttpRequestFactory);
		return new RestTemplate(httpComponentsClientHttpRequestFactory());
	}

	// https://stackoverflow.com/questions/13837012/spring-resttemplate-timeout
	@Bean
	@ConfigurationProperties(prefix = "custom.rest.template.connection")
	public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {
		return new HttpComponentsClientHttpRequestFactory();
	}

}

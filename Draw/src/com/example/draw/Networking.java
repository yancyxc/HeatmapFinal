package com.example.draw;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;

/**Networking
 * <br/><br/>
 * Helper class for getting pre-configured RestTemplate.
 * 
 * @author Chris Allen on Mar 21, 2013
 */
public class Networking {

	/**Factory method for retrieving a pre-configured RestTempalte.
	 * 
	 * @return {@link RestTemplate} with support for SSL and JSON message converting
	 */
	public static RestTemplate defaultRest() {
		RestTemplate REST = new RestTemplate();

		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(new FormHttpMessageConverter());
		messageConverters.add(new StringHttpMessageConverter());
		messageConverters.add(new MappingJacksonHttpMessageConverter());
		REST.setMessageConverters(messageConverters);

		try { DisableSSLCertificateCheckUtil.disableChecks(); }
		catch (Exception e) { e.printStackTrace(); }
		
		return REST;
	}
}

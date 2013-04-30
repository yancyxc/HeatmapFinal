package com.example.draw;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.web.client.RestTemplate;

import android.util.Log;

// most of these codes were based on chris allen, but i made some modification to fit my need(Luan Pham)

public class serverAPI {

	private ExecutorService executor;	// execute a background thread
	private RestTemplate REST;			// use restTemplate to connect to the server
	private APIDelegate delegate;		// create a apidelegate to handle the response
	// testing server to post data
	private String urlstr;
	
	//constructor
	public serverAPI(String url) {
		// limit background task to 5
		executor = Executors.newFixedThreadPool(5);
		this.urlstr = url;
	}
	
	// this function set the deleage and set default for RestTemplate
	public void setDelegate(APIDelegate d) {
		delegate = d;
		REST = Networking.defaultRest(); // class provided by christ allen
	}

	// this is a get method to get data from a server
	public void get() {
		// execute a task
		executor.submit(new Runnable() {
			@Override
			public void run() {
				String response = null;
				try {
					response = REST.getForObject(urlstr, String.class);			// get request and save it to a string
					delegate.getReturned(serverAPI.this, response, null);		// call delegate to handle the response
				} catch(Exception e) {
					e.printStackTrace();
					delegate.getReturned(serverAPI.this, response, e);			// else call delegate will error 
				}
			}
		});
	}///end of get
	
	// this is a post method to send data
	public void post(final String str) {
		executor.submit(new Runnable() {
			@Override
			public void run() {
				String response = null;
				try {
//------------>					// right now, response is not properly handle
					//response = REST.postForObject("http://192.168.1.89/api/test/", str, String.class);
				//	response = REST.postForObject("http://posttestserver.com/data/", str, String.class);
					Log.i("cs499","sedning msg from serverApi");
					response = REST.postForObject(urlstr, str, String.class);
					// if response is true
					delegate.postReturned(serverAPI.this, true, null);
				} catch(Exception e) {
					e.printStackTrace();
					delegate.postReturned(serverAPI.this, false, e);	// call delegate will exeception
				}
			}
		});
	}//end of post
	
		
}// end of class

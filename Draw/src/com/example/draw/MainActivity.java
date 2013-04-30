package com.example.draw;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;


// this is an activity which create a drawview and record it
public class MainActivity extends Activity  {
   
	DrawView drawView;	// paint view
    record rec;			// class that record touch event of a view
    
    // this method is create when android app is first startup
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);		// save instant of this activity
        // Set full screen view
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                         WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);	// no tilte on app

        drawView = new DrawView(this);  	// create paint program
       String username = "joshua.yancy@gmail.com";
       String password = "password";
       String url = "http://192.168.1.11:8000/api/post/";
       // rec = new record(this,drawView);	// pass on a view and record it
        rec = new record.Builder(this, drawView).setAutoRecord(true)
        		 		                        .setTimeRecord(5)
        		 		                        .setAutoPost(true)
        		 		                        .setAutoSave(true)
        		 		                        .setAutoPlay(true)
        		 		                        .setUsername(username)
        		 		                        .setPassword(password)
        		 		                        .setUrl(url)
        		 		                        .Build();
        
        setContentView(rec);				// set view to rec
        rec.startRecording();
       
    }
    
   
}
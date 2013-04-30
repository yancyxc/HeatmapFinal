/*
* Author: Luan Pham and Joshua Yancy
* Date: May, 2013
* 
* Purpose: the purpose of this class is to record a Single View that is passed in. This class extend a LinearLayout and it will contain the passed in
*		   View as a child view. it will override OnTouchIntercept to record the touch events and send the event to the next event handler.
*
*		   This class provides the Automated function such as auto-recording, auto-Post, auto-getk, and auto-replay for the developers.
*          The developer has the free to handle any one of these function.
*
* Structure:
*	
* FUNCTIONS:
*===============
* record()- constructor initialize all the data
* ontouchIntercept() - recrod touch points
* startRecording()	- set variable to start recording, so ontouchIntercept will start recording on the next event
* stopRecording()	- stop recording ontouchIntercept
* AutoRecord()	- this function will set autoRecord variable to ture and call StartTimer to handle the time in a different thread
* AutoPost()	- Auto post recording to the server
* AutoPlay()	- Auto replay the record events right after finishing recording
* getRecordStatus()	- get the status or recording
* Save()	- save the EventList/Recoridng to a file timestampt
* Post()	- Post the recording to the server
* Get()		- get a file from the server
* SetJson()	- set EventList to json object
* LoadFile() - load data from a filename
* JsonToEventList()	- convert json object to eventList
* Play()	- play the EventList
* StartTimer()	- start the timer for recording. stop based on the timer set
* SetTime()	- set the amount of time recording in seconds
* getReturned()	- echo the return status of get file from a server
* postReturned()	- echo the status
* SendSmg()	- this function is called by startTimer and send the next event to the child
* OnLayout() - change the layout accordingly 
*
* Builder Class
*===================
* private record() constructor for builder to use
* AutoRecord()
* AutoPost()
* AutoPlay()
* 
*
*
*/

package com.example.draw;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

//this class has method to record a view, parse the data and send to a server. it also can get data from 
// a server and deserialized it and replay the user touch event. 
@SuppressLint("Recycle")
public class record extends LinearLayout implements APIDelegate{
	
	ArrayList<MotionEvent> EventList;	// stored all the recorded event
	//ArrayList<MotionEvent> EventListFromFile;
	View view;					    // we dont' need this, but just stored the view of the child(paint program)
	JSONArray JsonList;					// list of json objects			
	JSONArray jArray;
	private boolean start = false; 		// boolean used to start and stop recording
    private boolean firstTouch = true;	// boolean used to start recording on first touch
	
	private int i = 0;							// position of motionData
	private long dx = 0;						//1000 millisecond delay to replay
	private Handler handler; 					//new thread
	private Runnable runnable; 					// function add to handler

	//might not used <-- unapproprivate
	Handler handler2 = new Handler();			// new thread
	Runnable runnable2; 						// new runable
	boolean go = true;							// boolean to start recording
	
	//rec and builder options 
	private boolean AutoRecord;
	private int timeRecording;
	private boolean AutoSave;
	private boolean AutoPost;
	private boolean AutoPlay;
	
	private String username;
	private String password;
	private String url;
	
	
	//builder class, create a final instant of this class
	public static class Builder{
		
		private Context builderContext;
		private View builderView;
		private boolean AutoRecord;
		private int timeRecording;
		private boolean AutoSave;
		private boolean AutoPost;
		private boolean AutoPlay;
		private String username;
		private String password;
		private String url;
		
		public Builder(Context context, View view){
			this.builderContext = context;
			this.builderView = view;
			this.timeRecording = 0;
			this.AutoRecord = false;
			this.AutoPlay = false;
			this.username = "username";
			this.password = "password";
			this.url = "www.url.com";
		}
			//automated functions
			public Builder setAutoRecord(boolean setAutoOnOff){this.AutoRecord = setAutoOnOff; return this;}
			public Builder setTimeRecord(int time){this.timeRecording = time; return this; }
			public Builder setAutoSave(boolean save){this.AutoSave = save; return this;}
			public Builder setAutoPost(boolean post){this.AutoPost = post; return this; }
			public Builder setAutoPlay(boolean play){this.AutoPlay = play; return this; }
			public Builder setUsername(String username){this.username = username; return this;}
			public Builder setPassword(String password){this.password = password; return this; }
			public Builder setUrl(String url){this.url = url; return this; }
		
		public record Build(){
			return new record(this);
			
		}
		
		
	} // end of builder class
	
	//only get called from Builder class
	private record(Builder builder){
		super(builder.builderContext);
		this.addView(builder.builderView);
		
		//these two list can be allocated elsewhere, but put it here since they are important and most used
		EventList = new ArrayList<MotionEvent>();	// allocate space for list of motion event object
		
		this.AutoRecord = builder.AutoRecord;
		
		this.timeRecording = builder.timeRecording;
		this.AutoPost = builder.AutoPost;
		this.AutoSave = builder.AutoSave;
		this.AutoPlay = builder.AutoPlay;
		this.username = builder.username;
		this.password = builder.password;
		this.url = builder.url;
		
		this.view = builder.builderView;
		
	}
	
	// constructor, initialized variable. this function take a context and a view
	public record(Context context, View view) {
		super(context);							// pass the context to the superclass
		this.addView(view);						// add view to viewgroup
		//these two list can be allocated elsewhere, but put it here since they are important and most used
		EventList = new ArrayList<MotionEvent>();	// allocate space for list of motion event object
		this.AutoRecord = false;
		this.timeRecording = 0;
	}

	// this function is auto generated and it's important when implement a viewgroup
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		 super.onLayout(changed, l, t, r, b);
	}

	
	// this method is used to record all the touch point of the childview before pass the event
	// to child view
    @Override
	public boolean onInterceptTouchEvent (MotionEvent ev){
    	if(start){
    			// start timer only at firstTouch 
    			if(this.AutoRecord && firstTouch){
    					firstTouch = false;
    			    	Log.i("CS499", "startRecording");
    					startTimer();	// start timer Recording
    			}	
    			
    			MotionEvent tempEv = MotionEvent.obtain(ev);	//copy event
    			EventList.add(tempEv);
		}							// save it		
	
    	
		return false;									// pass the touch event to the child view(paint)
    }
	
    public boolean getRecStatus(){
    	return start;
    }
    
	public void startRecording(){		// clear data and set start to record
		EventList.clear();
		start = true;
		firstTouch = true;
	}
	public void stopRecording(){		// set start to false, stop recording
		start = false;

    	Log.i("CS499", "stopRecording");
		
    	//for testing drawView
		//DrawView dw = (DrawView)this.getChildAt(0);
		//dw.points.clear();
	}

	// convert a list of motionEvent object to a list of json object so we can save it to a file and send it to a server
	private boolean setJson(){
		// this function return false if there is an error in converting a list of motionevent to a list of JsonObject
		
		if(EventList.size() == 0)
			return false;
		
		JsonList = new JSONArray();	
		
		try {
		//iterate through the list
		for(MotionEvent e:EventList){
			
			// create a temp jsonObj and initialize all the value of the motionEvent objects 
			// an event can be retrieve if we have all the below value
			JSONObject jsonObj = new JSONObject();
			
			jsonObj.put("eventTime", e.getEventTime()); 	jsonObj.put("downTime", e.getDownTime());
			jsonObj.put("action", e.getAction());			jsonObj.put("deviceId", e.getDeviceId());
			jsonObj.put("edgeFlags", e.getEdgeFlags()); 	jsonObj.put("metaState",e.getMetaState());
			jsonObj.put("x", e.getX());
			jsonObj.put("y", e.getY());						jsonObj.put("size", e.getSize());
			jsonObj.put("xPrecision", e.getXPrecision());	jsonObj.put("yPrecision", e.getYPrecision());
			jsonObj.put("pressure", e.getPressure());
			
			// add the jsonObj to a Json List
			JsonList.put(jsonObj);	
		}
		
		} catch (JSONException e) {
			// return false if there's an error
			return false;
		}
		
		return true;
	}
	
	
	// this function will save the MotionEVent List to a file locally 
	public boolean save() {
	
//---->		//note this function should return boolean so check/update later
		if((EventList.size() == 0) || !setJson())
			return false;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String currentDateandTime = sdf.format(new Date());
		
		// file name including directory
		//String filename = "directory"+ File.separator + "box.json";
		String filename = "MotionData: " + File.separator + currentDateandTime + ".jsonArray";
		
		String output =null;
		// output is a json string which represents the data of all the user touch points 
		// created in setJson function
		output = JsonList.toString();
		// write to data using fileUtility class given to us by chris allen
		boolean statusSave = FileUtility.writeToFile(this.getContext(), output, filename); // <----check for return value for error handling
	
		getFileList();
		// return true
		return statusSave;
	}
	
	// this method will load the data in from a textfile and parse it back to a List of MotionEVent obj
	// return false if there's an error, otherwise true
	public boolean loadFile(String Filename){
		
		EventList.clear();	// clear array of motionEvent
		// filename
		String filename = "directory"+File.separator+ Filename;
		// open and read in file as a string and stored in fileContents
		String fileContents = FileUtility.readFile(this.getContext(), filename);
		
		// these are the values of MotionEvent object that we need before we can 
		// retrieved a MotionEvent object
		
		
		 // return true if there's no error
		 if(fileContents != null)
			 return jsonToEventList(fileContents);
		 
		 return false;
	}
	
	public boolean jsonToEventList(String jsonString){
		
		long downTime, eventTime;
		int action, metaState,deviceId,edgeFlags;
		float x, y, pressure, size, xPrecision,yPrecision;
		
		 try {
			
			//JArray is a JsonArray String(MotionEVent string)
			jArray = new JSONArray(jsonString);

			for(int i = 0; i < jArray.length();i++){
				
				// get the json object
				JSONObject j = jArray.getJSONObject(i);
				
				// get the values from json object
				downTime = j.getLong("downTime"); 
				eventTime = j.getLong("eventTime");
				action = j.getInt("action");
				x = (float) j.getLong("x");
				y = (float) j.getLong("y");
				pressure = (float) j.getLong("pressure");
				size = (float) j.getLong("size");
				metaState = (int) j.getInt("metaState");
				xPrecision = (float) j.getLong("xPrecision");
				yPrecision = (float) j.getLong("yPrecision");
				deviceId = j.getInt("deviceId");
				edgeFlags = j.getInt("edgeFlags");
				
				//recreate the event using the value above
				MotionEvent tempEv = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, size, metaState,
														xPrecision, yPrecision, deviceId, edgeFlags);
				
				// add the event to a list
				EventList.add(tempEv);
			}
		} catch (JSONException e) {
			//error
			return false;
		}
		
		return true;
	}
	
	// this function will replay all the touch events by passing the event to the childView
	public void play(){	
		    i = 0;
		    Log.i("CS499", "play()");
		    handler = new Handler();
			// a runnable that will call sendMsg and increment i
		    runnable = new Runnable() {
		        public void run() {
		            sendMsg(i);				// i is the postion of motionData (List of event) to replay
		            i++;					// next event
		        }
		    };

		    runnable.run();   
		
	}
	
	int time = 0;
	// this function will send event to the childview 
	private void sendMsg(int i){
		// loop through event list that was recorded
		if(i < EventList.size()){
			time += dx;
			handler.postDelayed(runnable, dx);	// update handler with delay time, which is dx
			if(i < (EventList.size() -1) )		// calculate the next delay time dx...for example the time between 1st and 2nd touch
				dx = EventList.get(i + 1).getEventTime() - EventList.get(i).getEventTime();
			this.getChildAt(0).dispatchTouchEvent(EventList.get(i));		// send event to the child view
			}
		else{
			Log.i("CS499","done replay, time replay is " + time);
			handler.removeCallbacks(runnable);	// remove runnable, which is a thread that called this function
		}
	}

	
	// this function start start the timer
	private void startTimer(){

		 handler2.removeCallbacks(null);
		 runnable2 = new Runnable() {
		        public void run() {
		        		stopRecording();
		        		
		        	// do automated play, post, and save	
		        		if(AutoPlay)
		        			play();

		        		if(EventList.size() > 0){
		        			if(AutoPost)
		        				post();
		        			if(AutoSave)
		        				save();
		        		}
		        		
		        		handler2.removeCallbacks(runnable2);
		        } 
		    };

		handler2.postDelayed(runnable2, this.timeRecording * 1000);
		 
	}
	
	//post function
	//note* we will make a json object that stores the json Array list(motionEvent) along with 
	//some header such as type of phone/device, screen,size...
	public void post () {		

		serverAPI s;			// a class that handle post and get
		s = new serverAPI(url);	
		s.setDelegate(this);	//set delegate to this 
		if(setJson()){
			//final String smg = JsonList.toString();
			
			 final String androidId = Secure.getString(this.getContext().getContentResolver(), Secure.ANDROID_ID);
			 JSONObject jsonObj1 = new JSONObject();
			// Display display = this.getContext().get
			 WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
			 Display display = wm.getDefaultDisplay();
			 int screen_width = display.getWidth();  // deprecated
			 int screen_height = display.getHeight();  // deprecated
			
			 try{
			   jsonObj1.put("password", password); 
			   jsonObj1.put("username", username);
			   jsonObj1.put("screen width", screen_width);      
			   jsonObj1.put("screen height", screen_height); 
			   jsonObj1.put("Serial", androidId);
			   jsonObj1.put("Version", android.os.Build.VERSION.RELEASE);
			  // jsonObj1.put("motion events", EventList);
			   jsonObj1.put("appname",  this.getContext().getPackageName());
			   jsonObj1.put("Motion Events", JsonList);
			
			   final String smg = jsonObj1.toString();  
			   s.post(smg);
			   Log.i("CS499", "POST DONE");
			   Log.i("CS499", smg);
			 }
			 catch(Exception e){
				 e.printStackTrace();
			 }
		}
			
									// post the data to a server
	}
	
	public void get(){
		serverAPI s;			// a class that handle post and get
		s = new serverAPI(url);	
		s.setDelegate(this);	//set delegate to this 
		s.get();
	}
	
	//return a list of filename from app directory
	public ArrayList<String> getFileList(){
		
		//File dirFiles = this.getContext().getDir("directory", 1);
		File dirFiles = this.getContext().getApplicationContext().getFilesDir();
		ArrayList<String> temp = new ArrayList<String>();
		for (String strFile : dirFiles.list())
		{
			System.out.println(strFile);
			temp.add(strFile);
		}
		
		return temp;
	}
	
	public void setAutoRec(boolean set){
		this.AutoRecord = set;
	}
	public void setTimeRec(int time){
		this.timeRecording = time;
	}
	public void setAutoPost(boolean post){
		this.AutoPost = post;
	}
	public void setAutoPlay(boolean play){
		this.AutoPlay = play; 
		}
	public void setAutoSave(boolean save){
		this.AutoSave = save;
	}
	
	// these method is called when get return
	@Override
	public void getReturned(serverAPI api, String result, Exception e) {
		
		if(e == null)
			jsonToEventList(result);
		else
			System.out.println("error in get");
	}

	@Override
	public void postReturned(serverAPI api, boolean success, Exception e) {
		// TODO Auto-generated method stub
		if(success)
			Log.i("CS499","send work");
		else
			Log.i("CS499","error sending");
	}


}///end of class

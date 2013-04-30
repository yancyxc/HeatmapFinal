/*
 * Author: Luan Pham
 * Date: may, 2013
 * 
 * purpose: the purpose of this program is to make a class extend view and implement touch listener. it will also draw the
 * 			touch points on the canvas. This paint program will be used to test the recording AndroidHeapMap for cs499.
 * 
 * 
 * 
 */


package com.example.draw;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;


/*  this class is a small paint program that i will used to test my heap-map recording.
 *  the only thing this class does is create a view and implement touch listener.
 *	this program will draw the point where the user touch.
 */

// view class implement touch lister 
public class DrawView extends View implements OnTouchListener {
   
	
    public List<Point> points = new ArrayList<Point>();		// list of user touch points
    public Paint paint = new Paint();						// paint class

    public DrawView(Context context) {						// constructor
        super(context);										// pass context to superclass
        setFocusable(true);									// set focus 
        setFocusableInTouchMode(true);						

        this.setOnTouchListener(this);						// add listener

        paint.setColor(Color.BLACK);						// set default paint color
        paint.setAntiAlias(true);	
    }

    // this function will clear all the user touch point
    public void reset(){
    	points.clear();					
    }
    
    // this method is to draw on the canvas
    @Override
    public void onDraw(Canvas canvas) {
       
    	// iterate and draw all the point in the list points
    	for (Point i : points) {
            canvas.drawCircle(i.x, i.y, 5, paint);	
        }

    }

    // this method is called when the user touch this view
    public boolean onTouch(View view, MotionEvent event) {
        Point point = new Point();		// create new point
        point.x = event.getX();			// add data
        point.y = event.getY();
        points.add(point);				// add to a list
        invalidate();					
   
        return true;					// return true means the event has be used, which means no other function will get this event. 
    }
}

// class point stored info of x and y position
class Point {
    float x, y;    
}


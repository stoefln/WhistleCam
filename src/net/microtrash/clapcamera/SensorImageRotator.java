/*******************************************************************************
 * ClapCamera
 *
 * Copyright 2013 by Stephan Petzl
 * http://www.stephanpetzl.com
 *
 * This application is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package net.microtrash.clapcamera;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;

@SuppressLint("NewApi")
public class SensorImageRotator {

	private static String TAG = "SensorImageRotator";
	private int degreesQuantized = 0;
	public int getDegreesQuantized() {
		return degreesQuantized;
	}

	public void setDegreesQuantized(int degreesQuantized) {
		this.degreesQuantized = degreesQuantized;
	}

	private int orientation = 0;
	private int defaultOrientation = 0;
	private int oldDegreesQuantized = 0;
	private Activity activity;
	
	private OrientationEventListener orientationEventListener;
	private ArrayList<OrientationChangedListener> listeners;
	
	/**
	 * indicates whether its a tablet (0) or a phone (1)
	 * @return
	 */
	public int getDefaultDisplayOrientation(){
		return defaultOrientation;
	}
	
	public int getOrientation(){
		return orientation;
	}
	
	
	@SuppressWarnings("deprecation")
	public SensorImageRotator(Activity activity){
		this.activity = activity;
		listeners = new ArrayList<OrientationChangedListener>();
		Display display;  
		
	    display = activity.getWindow().getWindowManager().getDefaultDisplay();
	    try{
	    	defaultOrientation = display.getRotation(); // take care: this is only working on API level 8 and above. 0 => Landskape (tablet), 1 => Portrait (phone)
	    } catch (Exception e){
	    	defaultOrientation = display.getOrientation(); // deprecated
	    }
		Log.v(TAG, "default rotation: "+defaultOrientation);
		
		orientationEventListener = new OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL){
		    @Override
		    public void onOrientationChanged(int orientationValue) {
		    	orientation = orientationValue;
		    	oldDegreesQuantized = degreesQuantized;
		    	degreesQuantized = 0; // default -> landscape
		    	
		    	if(orientationValue >= 315 || orientationValue < 45){ // portrait
		    		degreesQuantized = 90;
		    	}else if(orientationValue < 135 && orientationValue > 45){ // inversed landscape
		    		degreesQuantized = 180;
		    	}else if(orientationValue >= 135 && orientationValue < 230){ // inversed portrait
		    		degreesQuantized = 270;
		    	}
		    	
		    	if(defaultOrientation == 0){ // if tablet
		    		degreesQuantized -= 90;
		    		if(degreesQuantized < 0){
		    			degreesQuantized += 360;
		    		}
		    	}
		    	if(oldDegreesQuantized != degreesQuantized){
		    		orientationChanged(degreesQuantized);
		    	}
		    }
		};
		
		if (orientationEventListener.canDetectOrientation()){
			orientationEventListener.enable();
		}
	}
	
	protected void orientationChanged(int degrees){
		Log.v(TAG, "degrees "+degrees);
		for(OrientationChangedListener listener : this.listeners){
			listener.orientationChanged(degreesQuantized);
		}
	}
	
	public Bitmap cropAndRotateBitmap(Bitmap bitmap, Rect cropRect){
		Log.v(TAG, "cropAndRotateBitmap()");
		Log.v(TAG, "bitmap: "+bitmap);
		Log.v(TAG, "orientation: "+orientation);
		Log.v(TAG, "degrees: "+degreesQuantized);
		Log.v(TAG, "cropRect: "+cropRect);
		int cropRectWidth = cropRect.right - cropRect.left;
		int cropRectHeight = cropRect.bottom - cropRect.top;
		
		
		System.gc();
		
		
		Bitmap result;
		Matrix m = new Matrix();
		Canvas canvas = new Canvas();
		if(degreesQuantized == 0){
			result = Bitmap.createBitmap(cropRectWidth, cropRectHeight, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(result);
			m.postTranslate(-cropRect.left, -cropRect.top);
			
		}else if(degreesQuantized == 90){
			Log.v(TAG, "rotate 90, cropRect: "+cropRect);
			result = Bitmap.createBitmap(cropRectHeight, cropRectWidth, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(result);
			m.postTranslate(-cropRect.left, -cropRect.height()-cropRect.top);
			m.postRotate(90);
	
		}else if(degreesQuantized == 180){
			Log.v(TAG, "rotate 180");
			result = Bitmap.createBitmap(cropRectWidth, cropRectHeight, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(result);
			m.postTranslate(-cropRect.left-cropRect.width(), -cropRect.height()-cropRect.top);
			m.postRotate(180);
			
		}else{ // 270
			Log.v(TAG, "rotate 270");
			result = Bitmap.createBitmap(cropRectHeight, cropRectWidth, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(result);
			m.postTranslate(-cropRect.width()-cropRect.left, -cropRect.top);
			m.postRotate(270);
	
		}
		Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		whitePaint.setStyle(Paint.Style.FILL);
		whitePaint.setColor(Color.WHITE);
		
		
		canvas.drawBitmap(bitmap, m, whitePaint);
		//canvas.restore();
		return result;
	}
	
	public Bitmap rotateBitmap(Bitmap bitmap){
		
		Log.v(TAG, "rotateBitmap()");
		Log.v(TAG, "orientation: "+orientation);
		Log.v(TAG, "degrees: "+degreesQuantized);
		
		
		System.gc();
		
		
		Bitmap result;
		Canvas canvas = new Canvas();
		if(degreesQuantized == 0){
			result = Bitmap.createBitmap(bitmap.getWidth(),	bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			canvas = new Canvas(result);
			canvas.save();
		}else if(degreesQuantized == 90){
			result = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
			canvas = new Canvas(result);
			canvas.save();
			canvas.rotate(90);
			canvas.translate(0, -1*bitmap.getHeight());
		}else if(degreesQuantized == 180){
			result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			canvas = new Canvas(result);
			canvas.save();
			canvas.rotate(180);
			canvas.translate(-1*bitmap.getWidth(), -1*bitmap.getHeight());
		}else{ // 270
			result = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
			canvas = new Canvas(result);
			canvas.save();
			canvas.rotate(270);
			canvas.translate(-1*bitmap.getWidth(), 0);
		}
		
		
		canvas.drawBitmap(bitmap, new Matrix(), null);
		canvas.restore();
		return result;
	}
	
	public void disable(){
		orientationEventListener.disable();
	}

	public void addOrientationChangedListener(OrientationChangedListener listener) {
		this.listeners.add(listener);
		
	}
	

}

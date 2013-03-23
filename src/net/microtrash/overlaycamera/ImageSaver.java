/*******************************************************************************
 * OverlayCamera
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
package net.microtrash.overlaycamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class ImageSaver {

	private Activity context;
	private int imageQuality;
	private String defaultImageFormat = "JPEG";
	private SensorImageRotator sensorImageRotator;
	private String lastCompositionPath;
	private static final String TAG = "ImageSaver";
	
	public String getDefaultImageFormat() {
		return defaultImageFormat;
	}
	public void setDefaultImageFormat(String defaultImageFormat) {
		this.defaultImageFormat = defaultImageFormat;
	}
	public int getImageQuality() {
		return imageQuality;
	}
	public void setImageQuality(int imageQuality) {
		this.imageQuality = imageQuality;
	}
	public SensorImageRotator getSensorImageRotator() {
		return sensorImageRotator;
	}
	public void setSensorImageRotator(SensorImageRotator sensorImageRotator) {
		this.sensorImageRotator = sensorImageRotator;
	}
	
	public ImageSaver(Activity context) {
		this.context = context;
		this.sensorImageRotator = new SensorImageRotator(context);
		this.lastCompositionPath = "";
	}

	

	public String saveImage(Bitmap image){
		
		
		try {
			String dir = Environment.getExternalStorageDirectory().getPath() + "/OverlayCamera/";
			
			
			File noMedia = new File(dir+".nomedia"); // don't know why, but something created a .nomedia file in my dir. so make shure each time that it's not therere
			noMedia.delete();
			OutputStream stream = null;
			
			
			this.printFreeRam();

			
			Bitmap processedImage = null;
			
			Log.v(TAG, "saving image without transparency");
			processedImage = sensorImageRotator.rotateBitmap(image);
		
			this.printFreeRam();
			if(context.getIntent().getAction().equals("android.media.action.IMAGE_CAPTURE") && context.getIntent().getExtras() != null){
				Uri saveUri = (Uri) context.getIntent().getExtras().getParcelable(MediaStore.EXTRA_OUTPUT);
				if (saveUri != null) {
				    // Save the bitmap to the specified URI (use a try/catch block)
				    stream = context.getContentResolver().openOutputStream(saveUri);
					processedImage.compress(CompressFormat.JPEG, this.imageQuality, stream);
					Log.v(TAG, "returning via reference");
					context.setResult(Activity.RESULT_OK);
					stream.flush();
					stream.close();
				} else {
					Log.v(TAG, "returning via putExtra()");
				    // If the intent doesn't contain an URI, send the bitmap as a Parcelable
				    // (it is a good idea to reduce its size to ~50k pixels before)
					context.setResult(Activity.RESULT_OK, new Intent("inline-data").putExtra("data", processedImage));
				}
				context.finish();
				return saveUri.toString();
			}else{				
				if(this.getDefaultImageFormat().equals("PNG")){
					
					this.lastCompositionPath = String.format(dir+"overlaycam_%d.png", System.currentTimeMillis());
					stream = new FileOutputStream(this.lastCompositionPath);
					processedImage.compress(CompressFormat.PNG, 100, stream);
				}else{ // this.imageFormat == "JPEG"
					this.lastCompositionPath = String.format(dir+"overlaycam_%d.jpg", System.currentTimeMillis());
					stream = new FileOutputStream(this.lastCompositionPath);
					processedImage.compress(CompressFormat.JPEG, this.imageQuality, stream);
				}
				Log.v(TAG, "image saved to "+lastCompositionPath);
				stream.flush();
				stream.close();
				
				processedImage.recycle();
				processedImage = null;
				// TODO: move the filescanner into its own service / thread
				new SingleFileScanner(context, this.lastCompositionPath);
				return lastCompositionPath;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private void printFreeRam() {
		// TODO Auto-generated method stub
		
	}
	public String getLastCompositionPath() {
		return this.lastCompositionPath;
	}
}

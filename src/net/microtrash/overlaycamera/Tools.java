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

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Debug;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;

public class Tools {
	
	public static String exception2String(Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
	public static int dip2Pixels(int dip, Context context){
		Resources r = context.getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
		return px;
	}
	
	public static String getRealPathFromURI(Uri contentUri, Activity activity) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = activity.managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

	public static void restartActivity(Activity activity) {
		Intent intent = activity.getIntent();
		activity.finish();
		activity.startActivity(intent);
		
	}
	
	/**
	 * Checks if a bitmap with the specified size fits in memory
	 * @param bmpwidth Bitmap width
	 * @param bmpheight Bitmap height
	 * @param bmpdensity Bitmap bpp (use 2 as default)
	 * @return true if the bitmap fits in memory false otherwise
	 */
	public static boolean checkBitmapFitsInMemory(long bmpwidth,long bmpheight, int bmpdensity ){
	    long reqsize=bmpwidth*bmpheight*bmpdensity;
	    long allocNativeHeap = Debug.getNativeHeapAllocatedSize();

	    if ((reqsize + allocNativeHeap + Tools.getHeapPad()) >= Runtime.getRuntime().maxMemory())
	    {
	        return false;
	    }
	    return true;
	}

	public static long getHeapPad(){
		return (long) Math.max(4*1024*1024,Runtime.getRuntime().maxMemory()*0.1);
	}

	/**
	 * find how much we have to downsample the image so it fits into memory
	 * @param width
	 * @param height
	 * @return
	 */
	public static double getDownscalingFactor(int width, int height) {
		int downscalingFactor;
		for (downscalingFactor = 1; downscalingFactor < 16; downscalingFactor ++) {
			double w = (double) width / downscalingFactor;
			double h = (double) height / downscalingFactor;
			if(Tools.checkBitmapFitsInMemory((int) w, (int) h, 4*4)){ // 4 channels (RGBA) * 4 layers
				
				break;
			}else{
				
			}
		}
		// don't allow that high resolutions- it makes the app slow
		if(width > 3400 && downscalingFactor == 1){
			downscalingFactor = 2;
		}
		return downscalingFactor;
	}


}

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

import java.util.HashMap;

import com.crittercism.NewFeedbackSpringboardActivity;

import net.microtrash.clapcamera.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.crittercism.app.Crittercism;

public class DialogBuilder {
	
	
	public static AlertDialog getProFeatureDialog(final Activity activity, String feature){
		HashMap<String, String> parameters = new HashMap<String,String>();
		parameters.put("feature", feature);
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Sorry, "+feature+" is only available in ClapCamera PRO. The PRO version includes other nice features too.\nYou wanna take a look?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   String url = "http://play.google.com/store/apps/details?id=net.microtrash.clapcamera";
		        	   Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		        	   activity.startActivity(browserIntent);
		           }
		           
		       })
		       .setNegativeButton("Back", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       }).setCancelable(true);
		return builder.create();
	}
	
	public static AlertDialog getCrashDialog(final BaseCameraActivity activity){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Sorry! An error occured:(\nWe will do everything to get this sorted out. Please restart the app.")
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   activity.finish();
		           }
		       })
		       .setCancelable(false);
		return builder.create();
		
	}

	public static AlertDialog getCrashOnLastAppLoadDialog(final BaseCameraActivity activity){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setMessage("Sorry! ClapCamera did crash the last time :(\nI'm trying hard to improve the stability of the app, and feedback would help me a lot!")
		       .setCancelable(false)
		       .setPositiveButton("Give Feedback", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   Intent i = new Intent(activity, NewFeedbackSpringboardActivity.class);
		        	   activity.startActivity(i);
		        	   dialog.cancel();
		        	   activity.finish();
		           }
		           
		       })
		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   //restartDialog.show();
		        	   dialog.cancel();
		        	   Tools.restartActivity(activity);
		           }
		       }).setCancelable(true);
		return builder.create();
	}

	public static Dialog getQuitDialog(final Activity activity, final Preview preview) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.quit)
		.setMessage(R.string.really_quit)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (preview != null && preview.camera != null) {
					preview.camera.release();
					preview.camera = null;
				}
				activity.finish();	
			}
		})
		.setNegativeButton(R.string.no, null);
		return builder.create();
	}


}

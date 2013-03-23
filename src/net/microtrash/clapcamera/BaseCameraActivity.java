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

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;

import net.microtrash.clapcamera.views.MenuView;
import net.microtrash.clapcamera.views.MenuViewCallback;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.flurry.android.FlurryAgent;



public class BaseCameraActivity extends Activity implements MenuViewCallback,  
															PreviewCallback, 
															UncaughtExceptionHandler{
	public static final boolean dev = true;
	public static final String TAG = "ClapCameraera";
	private Preview preview;
	private ClapView clapView;
	//private Display display;
	private ImageSaver imageSaver;
	protected RelativeLayout containerLayout;
	private RelativeLayout clapLayout;
	private RelativeLayout previewLayout;
	private RelativeLayout menuLayout;
	
    static final int CUTOUT = 1;
    static final int EDITPATH = 2;
    static final int SCALE_LOAD_IMAGE = 3;
    

    
	// is set to true while the camera is retrieving the image, so the shoot button gets disabled
	boolean saving = false;
	// is set to true while the camera is focusing, so the shoot button gets disabled
	boolean focusing = false;
	
	private Statistics stats = new Statistics();
	// file path to the last saved composition
	private String lastCompositionPath;
	
	// indicates whether the app crashed the last time it was started
	private boolean crashed = false;
	private Thread.UncaughtExceptionHandler originalUncoughtExceptionHandler;
	protected MenuView menuView;
	protected boolean pro = false;
	
	private Uri loadImageUri = null;
	
	Runnable focusTimer = null;
	private double imageToPortRatio;
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		if(BaseCameraActivity.dev){
    		Crittercism.init(getApplicationContext(), "DEVELOPEMENT");
    		FlurryAgent.onStartSession(this, "DEVELOPEMENT");
    	}else{
	    	Crittercism.init(getApplicationContext(), "xxxx");
	    	FlurryAgent.onStartSession(this, "xxxx");
    	}
		originalUncoughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);  
		
		

		FrameLayout rootLayout = new FrameLayout(this);
		
		containerLayout = new RelativeLayout(this);
		
		previewLayout = new RelativeLayout(this);
		previewLayout.setBackgroundColor(0x00808080);
		
		clapLayout = new RelativeLayout(this);
		clapLayout.setBackgroundColor(0x00808080);
		
		RelativeLayout topLayout = new RelativeLayout(this);
		topLayout.setBackgroundColor(0x00808080);
		
		
		containerLayout.addView(previewLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		containerLayout.addView(clapLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		
		menuView = new MenuView(this,this);
		menuLayout = new RelativeLayout(this);
		menuLayout.addView(menuView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
		containerLayout.addView(menuLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		rootLayout.addView(containerLayout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
		setContentView(rootLayout);
		
		
		
		String firstStartKey = "firstStart v1.0";
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		if(preferences.getString("crashed", "") != ""){	// show only the first time, the app is started
			crashed = true;
			DialogBuilder.getCrashOnLastAppLoadDialog(this).show();
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("crashed", "");
			editor.commit();
		}
		if(preferences.getString(firstStartKey, "") == ""){	// show only the first time, the app is started
			//DialogBuilder.getInstructionsDialog(this).show();
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(firstStartKey, "no");
			editor.commit();
		}
		
		imageSaver = new ImageSaver(this);
	}

	@Override
	protected void onDestroy() {

		if(clapView != null){
			clapView.recycle();
		}
		imageSaver.getSensorImageRotator().disable();
		super.onDestroy();
	}

	
	public void previewReady(int offsetLeft, int offsetTop, int previewWidth, int previewHeight, int pictureWidth, int pictureHeight, int downscalingFactor, String allResolutions){

		this.offsetLeft = offsetLeft;
		this.offsetTop = offsetTop;
		int workingImageWidth = pictureWidth / downscalingFactor;
		int workingImageHeight = pictureHeight / downscalingFactor;
		
		if(clapView == null){

			Log.v(TAG, offsetLeft+" "+offsetTop+" "+ previewWidth+" "+previewHeight+" "+workingImageWidth+" "+workingImageHeight);
			imageToPortRatio = (double) pictureWidth / (double) downscalingFactor / (double) previewWidth;
			clapView = new ClapView(this);
			clapView.init(offsetLeft, offsetTop, previewWidth, previewHeight, workingImageWidth, workingImageHeight);
			clapView.setDisplayMatrix(getDisplayMatrix());
			clapView.setImageToPortRatio(imageToPortRatio);
			clapLayout.addView(clapView, new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
			
			
			
			HashMap<String, String> parameters = new HashMap<String,String>();
			parameters.put("offsetLeft", String.valueOf(offsetLeft));
			parameters.put("offsetTop", String.valueOf(offsetTop));
			parameters.put("bestPictureSize", pictureWidth+"x"+pictureHeight);
			parameters.put("bestPreviewSize", previewWidth+"x"+previewHeight);
			parameters.put("workingImageSize", workingImageWidth+"x"+workingImageHeight);
			parameters.put("downscalingFactor", String.valueOf(downscalingFactor));
			parameters.put("allResolutions", allResolutions);
			parameters.put("FEATURE_CAMERA_AUTOFOCUS", getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS) ? "yes" : "no");
			parameters.put("Build.MODEL", Build.MODEL);
			parameters.put("Build.MANUFACTURER", Build.MANUFACTURER);
			FlurryAgent.logEvent("started",parameters);
		}
		
		if(loadImageUri != null){
			loadImage(loadImageUri, workingImageWidth, workingImageHeight);
	        loadImageUri = null;
		}
	}
	private int lastOrientation = 0;
	
	private void loadImage(Uri imageUri, int workingImageWidth, int workingImageHeight){
		Bitmap image; 
        try {  
        	clapView.beforeShoot();
			System.gc();
			
			// we have to downsample big images, otherwise we will get an OutOfMemoryException
			// so lets look how big the image is, which we are going to load. after that do some calculation and downsample 
			// the image while loading 
			InputStream in = getContentResolver().openInputStream(imageUri);
            BufferedInputStream buf = new BufferedInputStream(in);
            byte[] bMapArray= new byte[buf.available()];
            buf.read(bMapArray);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(Tools.getRealPathFromURI(imageUri, this), options);

            int downSample = (int) ((float) (options.outHeight*options.outWidth) / (float) (workingImageWidth*workingImageHeight));
            
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			if(downSample <= 0){
				downSample = 1;
			}
			
			options.inSampleSize = downSample;
            image = BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length, options);
            Log.v(TAG, "downsampling image by: "+downSample);
//clapView.setLoadedImageBitmap(image);
            clapView.addLoadedImageBitmap(image);
        	menuView.doneButton.show();
        	menuView.switchLayersButton.show();
        	hideAndShowControlsCheck();
        	clapView.invalidate();
        	
        } catch (FileNotFoundException e) {  
            e.printStackTrace(); 
        } catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * hides and shows controls depending on the state of the composition (transparent areas VS no transparency)
	 */
	private void hideAndShowControlsCheck(){
		
	}
	public void onPreviewStart(){

		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)){
			focusTimer = new Runnable() {
			    public void run() {
					if(preview != null 
							&& preview.camera != null 
							&& !saving 
							&& (imageSaver.getSensorImageRotator().getOrientation() == 0 || imageSaver.getSensorImageRotator().getOrientation() != lastOrientation)){
						focusing = true;
						Log.d(TAG, "focusing..");
						try{
							preview.camera.autoFocus(new Camera.AutoFocusCallback() {	
	
								public void onAutoFocus(boolean success, Camera camera) {
									//Log.d(TAG, "onAutoFocus()");
									focusing = false;
									if(shootButtonWasPressed){	// if shooting was scheduled
										shoot();
										shootButtonWasPressed = false;
									}
								}
							});
						}catch(Exception e){
							FlurryAgent.onError("2", e.getMessage(), "focusError");
							Log.e(TAG, "focusError!");
						}
					}
					lastOrientation = imageSaver.getSensorImageRotator().getOrientation();
					preview.postDelayed(focusTimer,4000);
			    }
			};
			Log.v(TAG,"focusTimer run()");
			focusTimer.run();
		}else{
			focusTimer = null;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			DialogBuilder.getQuitDialog(this, preview).show();
			return true;
		}else if (keyCode == KeyEvent.KEYCODE_MENU) {
			menuView.toggleMenu();
	        return true; 
		} else {
			return super.onKeyDown(keyCode, event);
		}
		
	}


	@Override
	protected void onStart() {
		super.onStart();
		if(BaseCameraActivity.dev){
    		Crittercism.init(getApplicationContext(), "DEVELOPEMENT");
    		FlurryAgent.onStartSession(this, "DEVELOPEMENT");
    	}else{
	    	Crittercism.init(getApplicationContext(), "");
	    	FlurryAgent.onStartSession(this, "");
    	}
		this.loadPrefs();
		Log.d(TAG,"onStart()");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG,"onResume()");
		if(!crashed){
			previewLayout.removeAllViews();
			preview = new Preview(this, this);
			previewLayout.addView(preview, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
			preview.openCamera();
		}
	}

	public void initPreview(){
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		focusTimer = null;
		Log.d(TAG,"onPause()");
		
	}

	@Override
	public void onStop()
	{
	   super.onStop();
	   focusTimer = null;
	   HashMap<String, String> parameters = new HashMap<String,String>();
	   parameters.put("shotNum", String.valueOf(stats.shotsTotal));
	   FlurryAgent.logEvent("onStop", parameters);
	   FlurryAgent.onEndSession(this);
	   
	}
	
	
    private void loadPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        imageSaver.setDefaultImageFormat(prefs.getString("imageFormatPref", "JPEG"));
        imageSaver.setImageQuality(Integer.parseInt(prefs.getString("imageQualityPref", "70")));

        Log.d(TAG,"loadPrefs(): defaultImageFormat"+imageSaver.getDefaultImageFormat());
    }
    
	
	// used to schedule shoot button presses, e.g. when the camera is not ready for shooting (like while focusing)
	private boolean shootButtonWasPressed;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG,"onActivityResult");
	    super.onActivityResult(requestCode, resultCode, data); 
	    if (resultCode == RESULT_OK){
	    	loadImageUri = data.getData();
	    }
	}

	@Override
	public void shootButtonClicked() {
		if(saving == false && focusing == false && preview.camera != null){
			shoot();
		}else{
			shootButtonWasPressed = true;
		}
	}
	public void printFreeRam(){
		this.printFreeRam(TAG);
	}
	public void printFreeRam(String tag){
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		long availableMegs = mi.availMem / 1048576L;
		long gcused = Runtime.getRuntime().totalMemory();
		long gcfree = Runtime.getRuntime().freeMemory();
		long gcmax = Runtime.getRuntime().maxMemory();
		final long mb = 1024L*1024L;
		Log.v(tag, String.format("sys free ram: %d, gc free:%d max:%d used:%d", availableMegs, gcfree/mb, gcmax/mb, gcused/mb));
	}
	private void shoot(){
		try{
			Log.v(TAG, "shoot()");
			clapView.beforeShoot();
			HashMap<String, String> parameters = new HashMap<String,String>();
			//parameters.put("pathNum", String.valueOf(pathManager.getPaths().size()));
			Crittercism.leaveBreadcrumb("shoot()");
			FlurryAgent.logEvent("shoot",parameters);
			focusing = false;
			saving = true;
			Log.v(TAG, "preview.camera.takePicture()");
			preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		}catch (Exception e) {
			Log.d(TAG,"error occured: "+e.getMessage()+"\n"+e.getStackTrace());
			Toast.makeText(this, "Uuups, I am sorry! There went something wrong, while trying to take a photo. Please restart me...", Toast.LENGTH_LONG).show();
		}
	}
	
	// Called when shutter is opened
	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {}
	};

	// Handles data for raw picture
	PictureCallback rawCallback = new PictureCallback() {
		// never worked in android 
		public void onPictureTaken(byte[] data, Camera camera) {}
	};

	// Handles data for jpeg picture
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken( byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - jpeg");
			Crittercism.leaveBreadcrumb("onPictureTaken()");
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			options.inSampleSize = (int) Tools.getDownscalingFactor( preview.getBestPictureSize().width, preview.getBestPictureSize().height);
			
			printFreeRam();
			System.gc();
			printFreeRam();
			Bitmap photo = null;
			for(int i = 0; i<6; i++){ // if something goes wrong while getting the image - try lower resolution
				try{
					Log.v(TAG, "downscalingFactor: "+options.inSampleSize);
					photo = BitmapFactory.decodeByteArray(data, 0, data.length, options);
					break;
				} catch( OutOfMemoryError ex){
					Log.e(TAG, "outofMemory!");
					options.inSampleSize++;
				}
			}
			
			data = null;
			
			if(photo == null){
				Toast.makeText(BaseCameraActivity.this, "OutOfMemoryError, please restart the app", Toast.LENGTH_LONG).show();
				FlurryAgent.onError("3", "OutOfMemoryError, downsamplingFactor: "+options.inSampleSize+"", "decodeByteArrays");
			}else{
				float downsampling = clapView.getComposition().getWidth() / photo.getWidth();
				if(downsampling != 1){
					Toast.makeText(BaseCameraActivity.this, "Compatibillity mode acitvated: Downsampled image by factor "+downsampling+" cause app is running on low memory.", Toast.LENGTH_LONG).show();
				}
				//touchView.resetMatrix();
				clapView.addBitmap(photo);
				//clapView.setLoadedImageBitmap(photo);
				saving = false;
				hideAndShowControlsCheck();
			
				menuView.showFileOperationButtons();
				menuView.hideMenu();
				//menuView.doneButton.show();
				
				stats.shotsTotal++; stats.shotsOfComposition++;
			}
			clapView.invalidate();
			
			preview.camera.startPreview();
		}
	};
	
	/*
	@Override
	public void focusButtonClicked() {
		focusing = true;
		FlurryAgent.logEvent("focusButtonClicked");
		preview.camera.autoFocus(new Camera.AutoFocusCallback() {	
			public void onAutoFocus(boolean success, Camera camera) {
				focusing = false;
				if(shootButtonWasPressed){
					shoot();
					shootButtonWasPressed = false;
				}
			}
		});
	}*/

	@Override
	public void onMenuShow() {
		
	}
	@Override
	public void onMenuHide() {
		
	}

	@Override
	public void undoButtonClicked() {
		FlurryAgent.logEvent("undoButtonClicked");
		//pathManager.getPaths().get(100).bottom++;
		
		hideAndShowControlsCheck();

		this.clapView.invalidate();

	}


	@Override
	public void saveImageButtonClicked() {
		
		imageSaver.saveImage(clapView.getComposition());

		HashMap<String, String> parameters = new HashMap<String,String>();
		parameters.put("pathNum", String.valueOf(stats.shotsOfComposition));
		parameters.put("degreesQuantized", ""+imageSaver.getSensorImageRotator().getDegreesQuantized());
		parameters.put("defaultOrientation", ""+imageSaver.getSensorImageRotator().getDefaultDisplayOrientation());
		parameters.put("imageSize", String.valueOf(clapView.getComposition().getWidth()) + "x" + String.valueOf(clapView.getComposition().getHeight()));
		parameters.put("imageFormat", imageSaver.getDefaultImageFormat());
		FlurryAgent.logEvent("save",parameters);
		
	}
		
	@Override
	public void newImageButtonClicked() {
		HashMap<String, String> parameters = new HashMap<String,String>();
	    parameters.put("shotNum", String.valueOf(stats.shotsOfComposition));
	    FlurryAgent.logEvent("newImageButton", parameters);
	    
		
		this.clapView.clearView();

		
		stats.shotsOfComposition = 0;
		this.clapView.invalidate();
	}

	@Override
	public void shareImageButtonClicked() {
		
		FlurryAgent.logEvent("shareImage");
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");
		share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + this.lastCompositionPath));
		//share.putExtra(Intent.EXTRA_TEXT, "http://www.microtrash.net");
		share.putExtra(Intent.EXTRA_SUBJECT,"cutoutcam");
		
		startActivity(Intent.createChooser(share, "Share Image"));
	}
	


	@Override
	public void preferencesButtonClicked() {
		Intent preferenceActivity = new Intent(getBaseContext(), PreferenceActivity.class);
		startActivity(preferenceActivity);
	}

	@Override
	public void instructionsButtonClicked() {
		//DialogBuilder.getInstructionsDialog(this).show();
	}
	

	private float offsetTop;
	private float offsetLeft;

	private Matrix getDisplayMatrix(){
		Matrix m = new Matrix();
		m.postTranslate(this.offsetLeft, this.offsetTop);
		return m;
	}


	@Override
	public void loadImageButtonClicked() {
		FlurryAgent.logEvent("loadImageButtonClicked");
		Crittercism.leaveBreadcrumb("loadImageButtonClicked()");
		
		Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
	    startActivityForResult(intent, 0);

	}

	@Override
	public void switchMode(Mode mode) {
		clapView.switchMode(mode);
		menuView.shootButton.show();
	}

	@Override
	public void doneButtonClicked() {
		
		
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("crashed", "yes");
		editor.commit();
		if(originalUncoughtExceptionHandler != null){
			originalUncoughtExceptionHandler.uncaughtException(thread, ex);
		}
	}


	@Override
	public void switchLayersButtonClicked() {
		clapView.switchLayers();
	}


	@Override
	public void focusButtonClicked() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void previewBitmapAvailable(Bitmap previewBitmap) {
		clapView.setPreviewClap(previewBitmap);
	}

}

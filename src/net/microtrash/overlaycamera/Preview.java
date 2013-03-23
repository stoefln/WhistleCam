package net.microtrash.overlaycamera;

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

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.ImageFormat;

import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Debug;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.flurry.android.FlurryAgent;

/**
 * 
 * openCamera() onSizeChanged() onLayout() onLayout() surfaceCreated()
 * surfaceChanged() onLayout()
 */
class Preview extends ViewGroup implements SurfaceHolder.Callback { // <1>
	private static final String TAG = "Preview";

	SurfaceHolder mHolder; // <2>
	public Camera camera; // <3>
	SurfaceView mSurfaceView;

	int l2 = 0, t2 = 0, r2 = 0, b2 = 0;
	int padding = 10;
	PreviewCallback cb;
	private double downscalingFactor = 1;
	// the size of this view. gets set in onMeasure()
	int fullWidth, fullHeight;
	Size bestPictureSize = null;
	Size bestPreviewSize = null;

	private String allResolutions;

	private Context context;

	public Preview(Context context, PreviewCallback callback) {
		super(context);
		this.cb = callback;
		init(context);
	}

	private void init(Context context) {
		setKeepScreenOn(true);
		this.context = context;
		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);
		mHolder = mSurfaceView.getHolder(); // <4>
		mHolder.addCallback(this); // <5>
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // <6>
	}

	static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
	    final int frameSize = width * height;

	    for (int j = 0, yp = 0; j < height; j++) {
	        int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
	        for (int i = 0; i < width; i++, yp++) {
	            int y = (0xff & ((int) yuv420sp[yp])) - 16;
	            if (y < 0) y = 0;
	            if ((i & 1) == 0) {
	                v = (0xff & yuv420sp[uvp++]) - 128;
	                u = (0xff & yuv420sp[uvp++]) - 128;
	            }
	            int y1192 = 1192 * y;
	            int r = (y1192 + 1634 * v);
	            int g = (y1192 - 833 * v - 400 * u);
	            int b = (y1192 + 2066 * u);

	            if (r < 0) r = 0; else if (r > 262143) r = 262143;
	            if (g < 0) g = 0; else if (g > 262143) g = 262143;
	            if (b < 0) b = 0; else if (b > 262143) b = 262143;

	            rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
	        }
	    }
	}
	private Bitmap previewBitmap;
	
	/*not needed at the moment
	 Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
		private int imageFormat;
		

		public void onPreviewFrame(byte[] data, Camera camera) {
			Parameters parameters = camera.getParameters();
			imageFormat = parameters.getPreviewFormat();
			if (imageFormat == ImageFormat.NV21) {
				
				int[] argb8888 = new int[Preview.this.getPreviewWidth()*Preview.this.getPreviewHeight()];
				Preview.decodeYUV420SP(argb8888, data, Preview.this.getPreviewWidth(), Preview.this.getPreviewHeight());

				
				previewBitmap = Bitmap.createBitmap(argb8888, Preview.this.getPreviewWidth(), Preview.this.getPreviewHeight(), Config.ARGB_8888);
				cb.previewBitmapAvailable(previewBitmap);
			}

		}

	};*/

	public void openCamera() {
		Log.d(TAG, "openCamera()");
		if (this.camera == null) {
			try {
				this.camera = Camera.open();
				this.camera.setErrorCallback(new ErrorCallback() {
					@Override
					public void onError(int error, Camera camera) {
						Log.e(TAG, "error! code:" + error);
						Toast.makeText(context,
								"Camera error occured: " + error, 8000).show();
					}
				});
				
				requestLayout(); // -> onSizeChanged() -> onLayout()
			} catch (Exception e) {
				String errorMessage = "Manufacturer: " + Build.MANUFACTURER
						+ "; Model:" + Build.MODEL + "; Camera: " + this.camera
						+ "; Stacktrace:" + Tools.exception2String(e);
				Log.d(TAG, "error occured: " + errorMessage);
				FlurryAgent.onError("1", errorMessage, "Preview.openCamera()");
				Toast.makeText(
						context,
						"Uuups, I am sorry! Could not connect to the camera device. Please restart me or your phone.",
						8000).show();
				Crittercism.logHandledException(e);
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d(TAG, "onSizeChanged() " + w + " " + h);
		fullWidth = w;
		fullHeight = h;
		if (fullWidth < fullHeight) {
			int tmp = fullHeight;
			fullHeight = fullWidth;
			fullWidth = tmp;
			Log.d(TAG, "switched:" + fullWidth + "x" + fullHeight);
		} else {
			Log.d(TAG, "fullSize:" + fullWidth + "x" + fullHeight);
		}
		if (this.camera != null) {
			this.setCameraPreviewSize();
			this.setCameraPictureSize();
			if (getChildCount() > 0) {
				final View child = getChildAt(0);
				Log.d(TAG,
						"r:" + this.getPreviewRight() + " l:"
								+ this.getPreviewLeft() + " b:"
								+ this.getPreviewBottom() + " t:"
								+ this.getPreviewTop());
				child.layout(this.getPreviewLeft(), this.getPreviewTop(),
						this.getPreviewRight(), this.getPreviewBottom());
				cb.previewReady(getPreviewLeft(), getPreviewTop(),
						getPreviewRight() - getPreviewLeft(),
						getPreviewBottom() - getPreviewTop(),
						getBestPictureSize().width,
						getBestPictureSize().height, (int) downscalingFactor,
						allResolutions);
				
				
			}
		}

		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void calcScaledPreviewSize() {
		int previewWidth = getBestPreviewSize().width;
		int previewHeight = getBestPreviewSize().height;
		float scaledWidth;
		float scaledHeight;

		Log.d(TAG, "preview width: " + previewWidth + ", preview height: "
				+ previewHeight);
		Log.d(TAG, "display width: " + fullWidth + ", display height: "
				+ fullHeight);
		float previewRatio = (float) previewWidth / (float) previewHeight;
		float displayRatio = (float) fullWidth / (float) fullHeight;

		if (displayRatio >= previewRatio) { // the display is wider then the
											// preview image
			scaledHeight = fullHeight - 2 * padding;
			scaledWidth = scaledHeight * previewRatio;
			l2 = (int) (fullWidth - scaledWidth) / 2;
			t2 = padding;
			r2 = (int) (fullWidth + scaledWidth) / 2;
			b2 = (int) scaledHeight + padding;

		} else {
			scaledWidth = fullWidth - 2 * padding;
			scaledHeight = scaledWidth / previewRatio;
			l2 = padding;
			t2 = (int) (fullHeight - scaledHeight) / 2;
			r2 = (int) scaledWidth + padding;
			b2 = (int) (fullHeight + scaledHeight) / 2;
		}
	}

	public int getPreviewTop() {
		if (this.t2 == 0) {
			this.calcScaledPreviewSize();
		}
		return t2;
	}

	public int getPreviewBottom() {
		if (this.b2 == 0) {
			this.calcScaledPreviewSize();
		}
		return b2;
	}

	public int getPreviewLeft() {
		if (this.l2 == 0) {
			this.calcScaledPreviewSize();
		}
		return l2;
	}

	public int getPreviewRight() {
		if (this.r2 == 0) {
			this.calcScaledPreviewSize();
		}
		return r2;
	}

	public int getPreviewWidth() {
		return this.getPreviewRight() - this.getPreviewLeft();
	}

	public int getPreviewHeight() {
		return this.getPreviewBottom() - this.getPreviewTop();
	}

	private void setCameraPreviewSize() {
		Camera.Parameters parameters = camera.getParameters();
		if (parameters.getPreviewSize() != this.getBestPreviewSize()) {
			parameters.setPreviewSize(this.getBestPreviewSize().width,
					this.getBestPreviewSize().height);
			this.camera.setParameters(parameters);
		}
	}

	private void setCameraPictureSize() {
		Camera.Parameters parameters = this.camera.getParameters();
		if (parameters.getPictureSize() != this.getBestPictureSize()) {
			parameters.setPictureSize(getBestPictureSize().width,
					getBestPictureSize().height);
			this.camera.setParameters(parameters);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d(TAG, "onLayout()");
		/*
		 * if (changed && getChildCount() > 0 && this.camera != null) { final
		 * View child = getChildAt(0);
		 * Log.d(TAG,"r:"+this.getPreviewRight()+" l:"
		 * +this.getPreviewLeft()+" b:"
		 * +this.getPreviewBottom()+" t:"+this.getPreviewTop());
		 * child.layout(this.getPreviewLeft(), this.getPreviewTop(),
		 * this.getPreviewRight(), this.getPreviewBottom());
		 * cameraActivity.initOverlay
		 * (this.getPreviewLeft(),this.getPreviewTop(),
		 * this.getPreviewRight(),this.getPreviewBottom()); }
		 */
	}

	public Size getBestPictureSize() {
		if (this.bestPictureSize == null) {
			this.calculateOptimalPictureAndPreviewSizes();
		}
		return bestPictureSize;
	}

	public Size getBestPreviewSize() {
		if (this.bestPreviewSize == null) {
			this.calculateOptimalPictureAndPreviewSizes();
		}
		return bestPreviewSize;
	}

	Hashtable<Double, Size> bestPictureSizes = new Hashtable<Double, Size>();
	Hashtable<Double, Size> bestPreviewSizes = new Hashtable<Double, Size>();

	/**
	 * find the best resolution for both: picture and preview size by following
	 * rules ISSUES: 1. on each device you have several different camera picture
	 * sizes and camera preview sizes 2. the picture size and the camera size
	 * have to match in terms of their width to height relation (=aspect ratio)
	 * 3. we want to make sure the area of the display is used as efficient as
	 * possible, therefore we should try to match the aspect ratios of display
	 * and image as well 4. you never know how much RAM you will have available,
	 * once the user switches the app and returnes, memory might not be enough
	 * anymore
	 * 
	 * VERY NEW: 1. find all preview sizes which match a image size (same aspect
	 * ratio) -> resolution-settings 2. preselect the highest
	 * resolution-settings which fits twice into memory 3. generate UI for
	 * selecting other resolutions-settings-> let the user decide which
	 * resolution fits best for his/her device
	 * 
	 * NEW: 1. calculate targetRato = device display aspect ratio 2. find a
	 * matching preview image size and set it as bestPictureSize (matching: the
	 * picture and preview aspect ratios have to match) 3. if a another
	 * pictureSize-previewSize match is found, which has a better targetRatio
	 * match and the picture size > bestPictureSize - 20% -> set this one as
	 * bestPictureSize 4. try to find a downscaling factor which allows us to
	 * keep 2-3 layers (bitmap instances) of that resolution in memory
	 * 
	 * OLD: 1. try to find the best matching picture resolution for targetRatio
	 * 2. only allow resolutions above 800px width 3. look for an exactly
	 * matching preview resolution. if not found: 4. try to find the second best
	 * matching picture resolution for targetRatio and start all over again 5.
	 * if nothing matching was found: use first picture and first preview
	 * resolution
	 * 
	 * @param targetRatio
	 */
	private void calculateOptimalPictureAndPreviewSizes() {

		/*
		 * double fullWidth = (double) cameraActivity.display.getWidth(); double
		 * fullHeight = (double) cameraActivity.display.getHeight();
		 */
		double targetRatio = (fullWidth - 2 * (double) padding)
				/ (fullHeight - 2 * (double) padding);
		Log.v(TAG,
				"calculateOptimalPictureAndPreviewSizes() width targetRatio: "
						+ targetRatio + " fullWidth:" + fullWidth
						+ " fullHeight:" + fullHeight);

		if (bestPreviewSize == null) {
			allResolutions = "";
			List<Size> pictureSizes = this.camera.getParameters()
					.getSupportedPictureSizes();
			List<Size> previewSizes = this.camera.getParameters()
					.getSupportedPreviewSizes();
			Collections.sort(pictureSizes, new Comparator<Size>() {
				public int compare(Size s1, Size s2) {
					return s2.width - s1.width;
				}
			});

			Collections.sort(previewSizes, new Comparator<Size>() {
				public int compare(Size s1, Size s2) {
					return s2.width - s1.width;
				}
			});

			allResolutions += "picture sizes:\n";
			for (Size size : pictureSizes) {
				allResolutions += String.valueOf(size.width) + 'x'
						+ String.valueOf(size.height) + ", ratio: "
						+ ((double) size.width / (double) size.height) + ";\n";
			}

			allResolutions += "preview sizes:\n";
			for (Size size : previewSizes) {
				allResolutions += String.valueOf(size.width) + 'x'
						+ String.valueOf(size.height) + ", ratio: "
						+ ((double) size.width / (double) size.height) + ";\n";
			}
			Log.v(TAG, "allResolutions: \n" + allResolutions);
			double bestRatio = 0;
			boolean matchingFinished = false;
			Log.v(TAG, "start matching picture and preview size...");
			for (Size pictureSize : pictureSizes) {
				double pictureRatio = (double) pictureSize.width
						/ (double) pictureSize.height;
				Log.v(TAG, "size: " + pictureSize.width + "x"
						+ pictureSize.height + " " + pictureRatio);
				double previewRatio;
				for (Size previewSize : previewSizes) {
					previewRatio = (double) previewSize.width
							/ (double) previewSize.height;
					if (previewRatio == pictureRatio) {

						if (bestPreviewSize == null) {
							bestPreviewSize = previewSize;
							bestPictureSize = pictureSize;
							bestRatio = pictureRatio;
							Log.v(TAG,
									"found picture size:"
											+ bestPictureSize.width
											+ "x"
											+ bestPictureSize.height
											+ " ratio: "
											+ ((double) bestPictureSize.width / (double) bestPictureSize.height));
							Log.v(TAG, "...continue searching...");
							break;
						} else {
							Log.v(TAG, "  pixels: " + pictureSize.width
									* pictureSize.height);
							Log.v(TAG, "  thresh: "
									+ (double) bestPictureSize.width
									* (double) bestPictureSize.height * 0.75D);
							if (Math.abs(targetRatio - bestRatio) > Math
									.abs(targetRatio - pictureRatio)
									&& pictureSize.width * pictureSize.height > (double) bestPictureSize.width
											* (double) bestPictureSize.height
											* 0.75D) {
								bestPreviewSize = previewSize;
								bestPictureSize = pictureSize;
								bestRatio = pictureRatio;
								matchingFinished = true;
								Log.v(TAG,
										"found even better match:"
												+ bestPictureSize.width
												+ "x"
												+ bestPictureSize.height
												+ " ratio: "
												+ ((double) bestPictureSize.width / (double) bestPictureSize.height));
							}
						}
					}
				}
				if (matchingFinished) {
					break;
				}
			}

			if (bestPreviewSize == null) {
				bestPictureSize = pictureSizes.get(0);
				bestPreviewSize = previewSizes.get(0);
				Log.v(TAG, "no match found!");
			}

			downscalingFactor = Tools.getDownscalingFactor(
					bestPictureSize.width, bestPictureSize.height);

			Log.v(TAG,
					"choosen picture size:"
							+ bestPictureSize.width
							+ "x"
							+ bestPictureSize.height
							+ " ratio: "
							+ ((double) bestPictureSize.width / (double) bestPictureSize.height));
			Log.v(TAG,
					"choosen preview size:"
							+ bestPreviewSize.width
							+ "x"
							+ bestPreviewSize.height
							+ " ratio: "
							+ ((double) bestPreviewSize.width / (double) bestPreviewSize.height));
			Log.v(TAG, "choosen downScalingFactor: " + downscalingFactor);
			// instantiate metadata json object
			JSONObject metadata = new JSONObject();
			MemoryInfo mi = new MemoryInfo();
			ActivityManager activityManager = (ActivityManager) context
					.getSystemService(Activity.ACTIVITY_SERVICE);
			activityManager.getMemoryInfo(mi);
			final long mb = 1024L * 1024L;
			try {
				metadata.put("totalMemory", Runtime.getRuntime().totalMemory()
						/ mb);
				metadata.put("freeMemory", Runtime.getRuntime().freeMemory()
						/ mb);
				metadata.put("maxMemory", Runtime.getRuntime().maxMemory() / mb);
				metadata.put("availableMemory", mi.availMem / mb);
				metadata.put("nativeHeapAllocatedSize",
						Debug.getNativeHeapAllocatedSize() / mb);
				metadata.put("heapPad", Tools.getHeapPad() / mb);
				metadata.put("bestPictureSize", bestPictureSize.width + "x"
						+ bestPictureSize.height);
				metadata.put("bestPreviewSize", bestPreviewSize.width + "x"
						+ bestPreviewSize.height);
				metadata.put("workingPictureSize", (int) bestPictureSize.width
						/ downscalingFactor + "x"
						+ (int) bestPictureSize.height / downscalingFactor);
				metadata.put("downscalingFactor", downscalingFactor);
				metadata.put("requiredSize",
						(bestPictureSize.width / downscalingFactor)
								* (bestPictureSize.height / downscalingFactor)
								* 4 * 2 / mb); // 4 channels * 2 layers
			} catch (JSONException e) {
				e.printStackTrace();
			}

			Crittercism.setMetadata(metadata);

		}

	}

	// Called once the holder is ready
	public void surfaceCreated(SurfaceHolder holder) { // <7>
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		Log.d(TAG, "surfaceCreated()");
		try {
			if (this.camera != null) {
				this.camera.setPreviewDisplay(holder);
			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);

		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.d(TAG, "surfaceChanged()");
		if (camera != null) {

			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(getBestPreviewSize().width,
					getBestPreviewSize().height);
			camera.setParameters(parameters);
			camera.startPreview();
			cb.onPreviewStart();
			requestLayout();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) { // <14>
		Log.d(TAG, "surfaceDestroyed()");
		if (this.camera != null) {
			camera.stopPreview();
			camera.lock();
			camera.release();
			this.camera = null;
		}
	}

	public void releaseCamera() {
		Log.d(TAG, "releaseCamera()");
		if (camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
	}

}
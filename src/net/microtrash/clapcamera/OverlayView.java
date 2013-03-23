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

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.Log;
import android.widget.ImageView;


class ClapView extends ImageView { // <1>

	private Bitmap image = null;
	private int displayWidth, displayHeight, offsetTop=0, offsetLeft=0;
	
	private Paint whitePaint, whiteLinePaint;
	private Paint transparentPaint, blackPaint, blackLinePaint, semiTransparentPaint;
	private Paint dashedPaint;
	private static String TAG = "Clap";
	private boolean dirty = true;
	private Canvas cacheCanvas;
	private Bitmap cacheBitmap;
	private Mode mode = null;
	private Matrix displayMatrix;
	private Matrix transformMatrix;
	private double imageToPortRatio;
	// These matrices will be used to move and zoom image
    Matrix zoomMatrix;

    // Remember some things for zooming
   
    BaseCameraActivity context;

	private boolean viewLoadedImageOnTop = true;
	private Bitmap previewBitmap;
	private Paint previewTransferPaint;


    public int getOffsetTop(){
    	return offsetTop;
    }
    public int getOffsetLeft(){
    	return offsetLeft;
    }
    
    public void setDisplayMatrix(Matrix m){
		displayMatrix = m;
		this.transformMatrix = new Matrix(zoomMatrix);
		transformMatrix.postConcat(this.displayMatrix);
	}
    
    public void setZoomMatrix(Matrix zoomMatrix) {
		this.zoomMatrix = zoomMatrix;
		this.transformMatrix = new Matrix(zoomMatrix);
		transformMatrix.postConcat(this.displayMatrix);
	}
    
    
    
    public void setImageToPortRatio(double ratio) {
		this.imageToPortRatio = ratio;
	}
    
    public void init(int offsetLeft, int offsetTop, int displayWidth, int displayHeight, int pictureWidth, int pictureHeight){
    	this.offsetLeft = offsetLeft;
    	this.offsetTop = offsetTop;
    	this.displayWidth = displayWidth;
    	this.displayHeight = displayHeight;
    	this.image = Bitmap.createBitmap(pictureWidth, pictureHeight, Bitmap.Config.ARGB_8888);
    }
    
	public ClapView(BaseCameraActivity context) {
		super(context);
		super.setClickable(true);
		this.context = context;
		this.zoomMatrix = new Matrix();
		this.displayMatrix = new Matrix();
		
		whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		whitePaint.setStyle(Paint.Style.FILL);
		whitePaint.setColor(Color.WHITE);
		
		semiTransparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		semiTransparentPaint.setStyle(Paint.Style.FILL);
		semiTransparentPaint.setColor(Color.argb(112, 255, 255, 255));
		
		whiteLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		whiteLinePaint.setStyle(Paint.Style.STROKE);
		whiteLinePaint.setColor(Color.argb(112, 255, 255, 255));
		whiteLinePaint.setStrokeWidth(2);
		
		blackLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		blackLinePaint.setStyle(Paint.Style.STROKE);
		blackLinePaint.setColor(Color.BLACK);
		blackLinePaint.setStrokeWidth(2);
		
		transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		transparentPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		transparentPaint.setStyle(Paint.Style.FILL);
		transparentPaint.setColor(Color.TRANSPARENT);
		
		previewTransferPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		blackPaint.setStyle(Paint.Style.FILL);
		blackPaint.setColor(Color.BLACK);
		
		dashedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dashedPaint.setARGB(255, 255, 255,255);
		dashedPaint.setStyle(Style.STROKE);
		dashedPaint.setStrokeWidth(2);
		dashedPaint.setPathEffect(new DashPathEffect(new float[] {10,10}, 0));
		//matrix.setTranslate(1f, 1f);
		
		
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		setMeasuredDimension(width, height);
		if(cacheCanvas == null){
			cacheCanvas = new Canvas();
	    	cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    	cacheCanvas.setBitmap(cacheBitmap);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
    
	public void setDisplayWidth(int width){
		this.displayWidth = width;
	}
	public void setDisplayHeight(int height){
		this.displayHeight = height;
	}
	
	
	
	public void addLoadedImageBitmap(Bitmap bitmap) {
		
		
		Log.v(TAG,"addLoadedImageBitmap(). size: "+bitmap.getWidth()+"X"+bitmap.getHeight()+" "+(float)bitmap.getWidth()/(float)bitmap.getHeight());	
		//Log.v(TAG,"this.image. size: "+image.getWidth()+"X"+image.getHeight()+" "+(float)image.getWidth()/(float)image.getHeight());
		context.printFreeRam(TAG);
		Canvas c = new Canvas(this.image);
		BitmapShader shader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
		
		
		Paint transferPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		transferPaint.setShader(shader);
		
		c.drawBitmap(bitmap, new Matrix(), transferPaint);
		
		
		shader = null;
		
		
		bitmap.recycle();
		context.printFreeRam(TAG);
		
		bitmap = null;
		
	}
	
	public void addBitmap(Bitmap bitmap) {
		context.printFreeRam(TAG);
		Canvas c = new Canvas(this.image);
		BitmapShader shader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
		Matrix m = new Matrix();

		Matrix inverseMatrix = new Matrix();
    	zoomMatrix.invert(inverseMatrix);
    	float s = 1F/ (float) this.imageToPortRatio;
    	inverseMatrix.preScale(s, s);
		m.postConcat(inverseMatrix);
		
		shader.setLocalMatrix(m);
		Paint transferPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		transferPaint.setShader(shader);

		transferPaint.setXfermode(new PorterDuffXfermode(this.getMode()));
		c.drawRect(new Rect(0,0,this.image.getWidth(),this.image.getHeight()), transferPaint);
	

		bitmap.recycle();
		bitmap = null;
		context.printFreeRam(TAG);
	}
	
	
	private Mode getMode() {
		if(this.mode == null){
			this.mode = Mode.DST_OVER;
			//this.mode = Mode.MULTIPLY;
		}
		return this.mode;
	}
	/**
	 * we have to prepare the image in case the selection is inverted:
	 * cutting out the paths, by drawing transparent shapes
	 * the reason why we are doing this BEFORE shoot() is just to save an additional image layer (and thus RAM)
	 */
	public void beforeShoot() {
		
	}
	

	
	public void recycle(){
		if(this.image != null){
			this.image.recycle();
			this.image = null;
		}
	}
	public void clearView(){
		if(this.image != null){
			int width = this.image.getWidth();
			int height = this.image.getHeight();
			this.image.recycle();
			this.image = null;
			System.gc();
			this.image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			this.invalidate();
		}
	}

	private Bitmap getCashedBitmap(){
		if(this.dirty == true){
			if (this.image != null) {
	
				//cacheCanvas.drawBitmap(this.image, this.transformMatrix, this.whitePaint);
				/*if(this.previewBitmap != null){
					previewTransferPaint.setXfermode(new PorterDuffXfermode(this.getMode()));
					cacheCanvas.drawBitmap(this.previewBitmap, this.displayMatrix, previewTransferPaint);
				}*/
				
			}
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			Path p = new Path();
			p.moveTo(this.getOffsetLeft(), this.getOffsetTop());
			p.lineTo(this.getOffsetLeft(), this.getOffsetTop()+this.displayHeight);
			p.lineTo(this.getOffsetLeft()+this.displayWidth, this.getOffsetTop()+this.displayHeight);
			p.lineTo(this.getOffsetLeft()+this.displayWidth, this.getOffsetTop());
			p.lineTo(this.getOffsetLeft(), this.getOffsetTop());
			cacheCanvas.drawPath(p, whiteLinePaint);
			this.dirty = false;
		}
		return this.cacheBitmap;
	}
	@Override
	protected void onDraw(Canvas viewCanvas) {
		super.onDraw(viewCanvas);
		
		viewCanvas.drawBitmap(this.getCashedBitmap(), 0, 0, whitePaint);

	}



	@Override
	public void invalidate() {
		this.dirty = true;
		super.invalidate();
	}

	
	
	public Bitmap getComposition() {
		return this.image;
	}

	public void switchMode(Mode mode) {
		this.mode = mode;
		this.invalidate();
	}

	public float getPortWidth() {
		return this.displayWidth;
	}


	public void switchLayers() {
		this.viewLoadedImageOnTop = !this.viewLoadedImageOnTop;
		this.dirty = true;
		this.invalidate();
	}
	public void setPreviewImage(Bitmap previewBitmap) {
		this.previewBitmap = previewBitmap;
		this.invalidate();
	}

}

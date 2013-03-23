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
package net.microtrash.overlaycamera.views;

import net.microtrash.overlaycamera.Tools;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;

public class IconButton extends View {

	static final int StateDefault = 0;
	static final int StateFocused = 1;
	static final int StatePressed = 2;

	private Bitmap mBitmapDefault;

	protected int radius;
	protected int color = 0xff000000;
	private boolean visible = true;
	private int savedLeft, savedTop;
	// indicates whether the button is visible when the menu is rendered
	public boolean visibleInMenu = true;
	private Paint paint;
	private Context context;
	private static String TAG = "IconButton";
	
	private Bitmap bitmap;
	private Paint linePaint;
	
	public IconButton setBitmap(Bitmap bitmap){
		this.bitmap = bitmap;
		return this;
	}
	public int getRadius(){
		return radius;
	}
	
	public IconButton setRadiusDip(int radius){
		radius = Tools.dip2Pixels(radius, context);
		this.setRadius(radius);
		return this;
	}
	
	public IconButton setRadius(int radius){
		this.radius = radius;
		this.setMinimumWidth(this.getTotalWidth());
		this.setMinimumHeight(this.getTotalHeight());
		return this;
	}


	public int getTotalWidth(){
		return 2*radius+2*padding;
	}
	public int getTotalHeight(){
		return 2*radius+2*padding;
	}
	
	public IconButton setColor(int color){
		this.color = color;
		return this;
	}

	public IconButton(Context context) {
		super(context);
		init(context);
	}
	
	public IconButton(Context context, int id) {
	    super(context);
	    this.bitmap = BitmapFactory.decodeResource(context.getResources(), id);
		init(context);
		
	}

	
	

	private void init(Context context) {
		this.context = context;
		setClickable(true);
		setBackgroundColor(0x00000000);
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);

		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setColor(0xFFFFFFFF);
		//font1 = Typeface.createFromAsset(context.getAssets(), "fonts/Airstream/Airstream.ttf");
		//font1 = Typeface.createFromAsset(context.getAssets(), "fonts/Droid/droid.otf");
		//font1 = Typeface.createFromAsset(context.getAssets(), "fonts/Bebas/BEBAS___.ttf");
		//font1 = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto/Roboto-Thin.ttf");
		this.setRadiusDip(30);
		
		
		setOnClickListener(onClickListener);
		setOnTouchListener(onTouchListener);
	}
	
	public void setVisibility(int visibility) {
		if(visibility == GONE || visibility == INVISIBLE){
			this.visible = false;
		}else{
			this.visible = true;
		}
		super.setVisibility(visibility);
	}
	
	
	private Matrix matrix = new Matrix();
	private Canvas canvas;
	/**
	 * draws the normal button (without rollover)
	 * @param canvas
	 */
	
	int padding = 0;
	private Point getCenter(){
		return new Point(this.radius+padding, this.radius+padding);
	}
	protected Bitmap drawDefault(){
		//canvas.drawARGB(255, 255, 0, 0);
		if(this.mBitmapDefault == null){
			mBitmapDefault = Bitmap.createBitmap(this.getTotalWidth(), this.getTotalHeight(), Config.ARGB_8888);
			canvas = new Canvas(mBitmapDefault);
		}
		paint.setAntiAlias(true); // for a nicer paint
		paint.setColor(color);
		paint.setStrokeWidth(1);
		paint.setStyle(Style.FILL);
		
		/*Paint p = new Paint();
		p.setColor(0xFF00FF00);
		p.setStyle(Style.FILL);
		canvas.drawRect(new Rect(0,0, canvas.getWidth(), canvas.getHeight()), p);*/
		Path path = new Path();
		path.addCircle(getCenter().x, getCenter().y, radius, Path.Direction.CW);
		canvas.drawPath(path, paint);
		//linePaint.setStrokeWidth(4);
		//canvas.drawCircle(getCenter().x, getCenter().y, radius, linePaint);
		if(this.bitmap != null){
			
			float scaleFactor =  0.5f * (float) this.radius * 2 / (float) this.bitmap.getWidth(); 
			matrix.reset();
			matrix.postScale(scaleFactor, scaleFactor);
			float scaledWidth = matrix.mapRadius(this.bitmap.getWidth());
			float scaledHeight = matrix.mapRadius(this.bitmap.getHeight());
			matrix.postTranslate((this.getTotalWidth()-scaledWidth)/2, (this.getTotalHeight()-scaledHeight)/2);
			
			canvas.save(); 
			canvas.rotate(currentRotation,this.getCenter().x, this.getCenter().y);
			canvas.drawBitmap(this.bitmap, this.matrix, paint);
		    canvas.restore();
		}
		
		return this.mBitmapDefault;
	}
	
	
	
	protected Bitmap getDefaultBitmap(){
		if(mBitmapDefault == null){
			mBitmapDefault = this.drawDefault();
		}
		return mBitmapDefault;
	}
	
	

	
	@Override
	protected void onDraw(Canvas canvas) {
		//Log.v("Button","onDraw(): "+this.getWidth()+"x"+this.getHeight()+" "+canvas.getWidth()+"x"+canvas.getHeight());
	    super.onDraw(canvas);
	    canvas.drawBitmap(this.getDefaultBitmap(), 0, 0, paint);
		//super.onDraw(canvas);
		//canvas.drawBitmap(this.getDefaultBitmap(), new Rect(0, 0, this.radius*8, this.radius*8), new Rect(0,0, this.radius*2, this.radius*2), null);
	}

	
	public void recycle() {
		if(mBitmapDefault != null){
			mBitmapDefault.recycle();
			mBitmapDefault = null;
		}
	}
	
	public void hide(){
		this.hide(true);
	}
	public void hide(boolean withAnimation){
		if(this.visible == true){
			savedLeft = getLeft();
			savedTop = getTop();
			if(!withAnimation){
				this.visible = false;
				layout(0, 0, 0, 0);
				this.setVisibility(GONE);
			}else{
				ScaleAnimation anim = new ScaleAnimation(1, 0, 1, 0, this.getCenter().x, this.getCenter().y);
				anim.setDuration(300);
				anim.setFillAfter(true);
				anim.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {}
					@Override
					public void onAnimationRepeat(Animation animation) {}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						visible = false;
						layout(0, 0, 0, 0);
					}
				});
				this.startAnimation(anim);
			}
			
		}
		
	}
	private int currentRotation = 0;
	
	public void rotate(int degrees){
		
		if(this.visible){
			OvershootInterpolator inter = new OvershootInterpolator();
			RotateAnimation anim = new RotateAnimation(currentRotation-degrees, 0, this.getCenter().x, this.getCenter().y);
			anim.setDuration(500);
			anim.setFillAfter(false);
			anim.setInterpolator(inter);
			currentRotation = degrees;
			drawDefault();
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {}
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					
				}
			});
			this.startAnimation(anim);
		}else{
			currentRotation = degrees;
			drawDefault();
		}
		
	}
	
	
	public void show(){
		if(this.visible == false){
			this.setVisibility(VISIBLE);
			OvershootInterpolator inter = new OvershootInterpolator();
			ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1, this.getRadius(), this.getRadius());
			anim.setDuration(300);
			anim.setInterpolator(inter);
			anim.setFillAfter(true);
			anim.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {	}
				
				@Override
				public void onAnimationRepeat(Animation animation) { }
				
				@Override
				public void onAnimationEnd(Animation animation) {
					visible = true;
					
				}
			});
			this.startAnimation(anim);
			
			this.layout(this.savedLeft, this.savedTop, this.savedLeft+2*this.radius, this.savedTop+2*this.radius);
		}
		this.visible = true;
	}
	

	public IconButton setOnClickCallback(OnClickListener l) {
		super.setOnClickListener(l);
		return this;
	}
	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {

		}
	};
	
	private OnTouchListener onTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//Log.v("Button","onTouch()");
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				OvershootInterpolator inter = new OvershootInterpolator();
				ScaleAnimation anim = new ScaleAnimation(1, (float) 1.5, 1,(float) 1.5, IconButton.this.getRadius(), IconButton.this.getRadius());
				anim.setInterpolator(inter);
				anim.setDuration(200);
				anim.setFillAfter(true);
				IconButton.this.startAnimation(anim);
			}else if(event.getAction() == MotionEvent.ACTION_UP){
				ScaleAnimation anim = new ScaleAnimation((float) 1.5, 1, (float) 1.5, 1, IconButton.this.getRadius(), IconButton.this.getRadius());
				anim.setDuration(300);
				anim.setFillAfter(true);
				IconButton.this.startAnimation(anim);
			}
			return false;
		}
	};


	public IconButton setCaption(String string) {
		return this;
	}

	public IconButton setTextSize(int i) {
		return this;
		
	}
	
	

}

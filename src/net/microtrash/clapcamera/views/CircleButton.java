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
package net.microtrash.clapcamera.views;


import net.microtrash.clapcamera.Tools;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;

public class CircleButton extends Button {

	static final int StateDefault = 0;
	static final int StateFocused = 1;
	static final int StatePressed = 2;


	private Bitmap mBitmapDefault;

	private String mCaption;
	protected int radius;
	protected int color = 0xff000000;
	private Typeface font1;
	private boolean visible = true;
	private int savedLeft, savedTop;
	// indicates whether the button is visible when the menu is rendered
	public boolean visibleInMenu = true;
	private Paint paint;
	private Context context;
	private static String TAG = "CircleButton";
	
	public int getRadius(){
		return radius;
	}
	
	public CircleButton setRadiusDip(int radius){
		radius = Tools.dip2Pixels(radius, context);
		this.setRadius(radius);
		return this;
	}
	
	public CircleButton setRadius(int radius){
		this.radius = radius;
		this.setWidth(2*radius);
		this.setHeight(2*radius);
		return this;
	}

	public CircleButton setColor(int color){
		this.color = color;
		return this;
	}
	
	public CircleButton setCaption(String caption){
		mCaption = caption;
		return this;
	}
	public CircleButton(Context context) {
	    super(context);
		init(context);
	}

	public CircleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CircleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		this.context = context;
		setClickable(true);
		setBackgroundColor(0x00000000);
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);
		//font1 = Typeface.createFromAsset(context.getAssets(), "fonts/Airstream/Airstream.ttf");
		//font1 = Typeface.createFromAsset(context.getAssets(), "fonts/Droid/droid.otf");
		//font1 = Typeface.createFromAsset(context.getAssets(), "fonts/Bebas/BEBAS___.ttf");
		//font1 = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto/Roboto-Thin.ttf");
		font1 = Typeface.createFromAsset(context.getAssets(), "fonts/Capsuula/Capsuula.ttf");
		mCaption = "Caption";
		this.setRadiusDip(30);
		this.setTextSize(22);
		
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
	/**
	 * draws the normal button (without rollover)
	 * @param canvas
	 */
	protected void drawDefault(Canvas canvas){
		//canvas.drawARGB(255, 255, 0, 0);
		Paint paintText = new Paint();
		paintText.setAntiAlias(true);
		paintText.setTextSize(this.getTextSize());
		paintText.setColor(0xffffffff); // white
		paintText.setTypeface(this.font1);
		paintText.setTextAlign(Align.CENTER);
		
		Rect bounds = new Rect();
		
		paintText.getTextBounds("t",0,1,bounds);
		
		float left = (float) radius;
		Paint paint = new Paint();
		paint.setAntiAlias(true); // for a nicer paint

		paint.setColor(color);
		paint.setStrokeWidth(1);
		paint.setStyle(Style.FILL);
		
		Path path = new Path();
		path.addCircle(radius, radius, radius, Path.Direction.CW);
		canvas.drawPath(path, paint);
		
		canvas.save(); 
		canvas.rotate(-45,this.getRadius(),this.getRadius());
		
		String[] lines = mCaption.split("\n");
		float lineSpacing = bounds.height() / 5;
		float bottomLine;
		if(lines.length == 1){
			bottomLine = (float) radius + bounds.height()/2;
		}else{
			float totalHeight = lines.length * bounds.height(); // spacing between lines is 1/5 of the line height
			bottomLine = (float) radius - totalHeight/2 + bounds.height()/2;
		}
		float offset = 0;
		for(String line: lines){
			canvas.drawText(line, left, bottomLine+offset, paintText);
			offset += bounds.height() + lineSpacing; 
		}
	    canvas.restore();
	}
	
	
	
	protected Bitmap getDefaultBitmap(){
		if(mBitmapDefault == null){
			mBitmapDefault = Bitmap.createBitmap(2*radius, 2*radius, Config.ARGB_8888);
			Canvas canvas = new Canvas(mBitmapDefault);
			this.drawDefault(canvas);
			return mBitmapDefault;
		}
		return mBitmapDefault;
	}
	
	

	
	@Override
	protected void onDraw(Canvas canvas) {
		//Log.v("Button","onDraw(): "+this.getWidth()+"x"+this.getHeight());
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
				this.setVisibility(GONE);
			}else{
				ScaleAnimation anim = new ScaleAnimation(1, 0, 1, 0, this.getRadius(), this.getRadius());
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
	

	public CircleButton setOnClickCallback(OnClickListener l) {
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
				ScaleAnimation anim = new ScaleAnimation(1, (float) 1.5, 1,(float) 1.5, CircleButton.this.getRadius(), CircleButton.this.getRadius());
				anim.setInterpolator(inter);
				anim.setDuration(200);
				anim.setFillAfter(true);
				CircleButton.this.startAnimation(anim);
			}else if(event.getAction() == MotionEvent.ACTION_UP){
				ScaleAnimation anim = new ScaleAnimation((float) 1.5, 1, (float) 1.5, 1, CircleButton.this.getRadius(), CircleButton.this.getRadius());
				anim.setDuration(300);
				anim.setFillAfter(true);
				CircleButton.this.startAnimation(anim);
			}
			return false;
		}
	};
	
	

}

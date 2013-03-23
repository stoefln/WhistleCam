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

import java.util.ArrayList;

import net.microtrash.clapcamera.R;
import net.microtrash.clapcamera.Tools;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * old colors:
 * 636350
 * D9CDA9
 * 7F7D7C
 * A16B61
 * A6978A
 * 
 * new colors:
 * 222f36
 * a6a393
 * 424c46
 * 070f14
 * 000000
 */
public class MenuView extends ViewGroup{

	public IconButton shootButton;
	public IconButton menuButton, closeMenuButton, doneButton, 
						newImageButton, saveImageButton, shareImageButton, shareImageYesButton, shareImageNoButton, preferencesButton, instructionsButton, loadImageButton, 
						modeButton;
	public IconButton switchLayersButton;
	
	private boolean menuVisible = false;
	private ArrayList<IconButton> menuButtons;
	// indicates if every button is positioned correctly
	private boolean initialised = false;
	private static String TAG = "ClapCameraera";
	private Context context;
	private ArrayList<CircleButton> modeButtons;
	
	
	public void recycle(){
		for(IconButton button : menuButtons){			
			button.recycle();
		}
	}
	public void hideMenu(){
		hideMenu(true);
	}
	public void hideMenu(boolean withAnimation){
			
		boolean isSomeButtonVisible = false;
		for(IconButton button : menuButtons){				
			button.hide(withAnimation);
			if(button != closeMenuButton){
				isSomeButtonVisible = isSomeButtonVisible || button.visibleInMenu;
			}
		}
		
		cb.onMenuHide();
		Log.v(TAG, "hideMenu(), menuVisible:"+this.menuVisible);
		this.menuVisible = false;
		
	}
	
	public void showMenu(){
		for(IconButton button : menuButtons){
			if(button.visibleInMenu){
				button.show();
			}
		}
		cb.onMenuShow();
		Log.v(TAG, "showMenu(), menuVisible:"+this.menuVisible);
		this.menuVisible = true;
	}
	
	public void showModeButtons(){
		for(CircleButton button : modeButtons){
			button.show();
		}
	}
	public void hideModeButtons(){
		hideModeButtons(true);
	}
	public void hideModeButtons(boolean withAnimation){
		for(CircleButton button : modeButtons){
			button.hide(withAnimation);
		}
	}
	
	public void hideFileOperationButtons(){
		this.saveImageButton.visibleInMenu = false;
		this.newImageButton.visibleInMenu = false;
		this.shareImageButton.visibleInMenu = false;
	}
	
	public void showFileOperationButtons(){
		this.saveImageButton.visibleInMenu = true;
		this.newImageButton.visibleInMenu = true;	
		this.shareImageButton.visibleInMenu = true;
	}
	
	public void showShareMenu(){
		this.shareImageButton.show();
		this.shareImageYesButton.show();
		this.shareImageNoButton.show();
	}
	public void hideShareMenu(){
		hideShareMenu(true);
	}
	public void hideShareMenu(boolean withAnimation){
		this.shareImageButton.hide(withAnimation);
		this.shareImageYesButton.hide(withAnimation);
		this.shareImageNoButton.hide(withAnimation);
	}
	
	
	
	public void toggleMenu(){
		if(this.menuVisible){
			this.hideMenu();
		}else{
			this.showMenu();
		}
	}
	final MenuViewCallback cb;
	
	int color1, color2, color3, color4;
	public MenuView(Context context, MenuViewCallback callback) {
		super(context);
		this.context = context;
		this.setVisibility(INVISIBLE);
		menuButtons = new ArrayList<IconButton>();
		/*
		     * new colors:
			 * 222f36
			 * a6a393
			 * 424c46
			 * 070f14
			 * 000000*/
		color1 = 0xFF000000;
		color2 = 0xFF424c46;
		color3 = 0xFF222f36;
		color4 = 0xFFa6a393;
		
		PackageManager pm = context.getPackageManager();
        
        
		cb = callback;
		
		shootButton = new IconButton(context, R.drawable.camera);
		shootButton.setRadiusDip(40);
		shootButton.setColor(color3);
		shootButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { 
				cb.shootButtonClicked();

				hideMenu();
			}
		});
		
		doneButton = new IconButton(context, R.drawable.check);
		doneButton.setCaption("Done");
		doneButton.setTextSize(25);
		doneButton.setRadiusDip(41);
		doneButton.setColor(color2);
		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				cb.doneButtonClicked();
				hideMenu();
				doneButton.hide();
			}
		});
		
		
		
		
		closeMenuButton = new IconButton(context, R.drawable.cancel);
		closeMenuButton.setCaption("Back");
		closeMenuButton.setColor(color2);
		closeMenuButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) { // <5>
				hideMenu();
				hideModeButtons();
			}
		});
		menuButtons.add(closeMenuButton);
		
		
		
		modeButton = new IconButton(context);
		//modeButton.visibleInMenu = false;
		modeButton.setCaption("Mode");
			modeButton.setColor(color2);
			modeButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) { // <5>
					hideMenu();
					showModeButtons();
				}
			});
		menuButtons.add(modeButton);
        
		
	
		
		saveImageButton = new IconButton(context, R.drawable.save);
		saveImageButton.setCaption("Save");
		saveImageButton.setColor(color3);
		saveImageButton.visibleInMenu = false;
		saveImageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				hideMenu();
				cb.saveImageButtonClicked();
			}
		   
		});
		menuButtons.add(saveImageButton);
		
		newImageButton = new IconButton(context, R.drawable.trash);
		newImageButton.setCaption("New");
		newImageButton.setColor(color3);
		newImageButton.visibleInMenu = false;
		newImageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				cb.newImageButtonClicked();
				hideMenu();
				hideFileOperationButtons();
				shootButton.show();

			}
		   
		});
		menuButtons.add(newImageButton);
		
		shareImageButton = new IconButton(context);
		shareImageButton.setCaption("Saved image\nto sdcard...\nWanna share?");
		shareImageButton.setRadiusDip(96);
		shareImageButton.setTextSize(26);
		shareImageButton.setColor(color4);
		shareImageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				hideMenu();
				cb.shareImageButtonClicked();
			}
		   
		});
		
		shareImageYesButton = new IconButton(context);
		shareImageYesButton.setCaption("Yes!");
		shareImageYesButton.setColor(color3);
		shareImageYesButton.setRadiusDip(40);
		shareImageYesButton.setTextSize(28);
		shareImageYesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				hideMenu();
				cb.shareImageButtonClicked();
			}
		});
		
		shareImageNoButton = new IconButton(context);
		shareImageNoButton.setCaption("No");
		shareImageNoButton.setColor(color2);
		shareImageNoButton.setRadiusDip(25);
		shareImageNoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				hideMenu();
				hideShareMenu();
			}
		});
		
		
		preferencesButton = new IconButton(context, R.drawable.settings);
		preferencesButton.setCaption("Setup");
		preferencesButton.setColor(color4);
		preferencesButton.visibleInMenu = true;
		preferencesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				hideMenu();
				cb.preferencesButtonClicked();
			}
		   
		});
		
		menuButtons.add(preferencesButton);
		
		instructionsButton = new IconButton(context, R.drawable.help);
		instructionsButton.setCaption("Help");
		instructionsButton.setColor(color4);
		instructionsButton.visibleInMenu = true;
		instructionsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				hideMenu();
				cb.instructionsButtonClicked();
			}
		   
		});
		
		menuButtons.add(instructionsButton);
		
		loadImageButton = new IconButton(context, R.drawable.load);
		loadImageButton.setCaption("Load");
		loadImageButton.setColor(color3);
		loadImageButton.visibleInMenu = true;
		loadImageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				hideMenu();
				cb.loadImageButtonClicked();
			}
		   
		});
		
		menuButtons.add(loadImageButton);
		

		
		switchLayersButton = new IconButton(context, R.drawable.switch_layers);
		switchLayersButton.setCaption("Switch Layers");
		switchLayersButton.setColor(color3);
		switchLayersButton.visibleInMenu = false;
		switchLayersButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				cb.switchLayersButtonClicked();
			}
		   
		});
		
		int modeButtonRadius = 35;
		this.modeButtons = new ArrayList<CircleButton>();
		/**
		 * darken... darken those pixels which are darker in the new image as in the source image on dest
		 * lighten... lighten those pixels  which are lighter in the new image as in the source image on dest
		 * screen... use source pixels to lighten dest
		 * multiply... simple multipy new and old -> image gets darker
		 */
		this.modeButtons.add(new CircleButton(context)
								.setCaption("Multiply")
								.setRadiusDip(modeButtonRadius)
								.setOnClickCallback(new OnClickListener() {
									@Override
									public void onClick(View v) {
										cb.switchMode(Mode.MULTIPLY);
										hideModeButtons();
									}
								}));
		this.modeButtons.add(new CircleButton(context)
								.setCaption("DST")
								.setRadiusDip(modeButtonRadius)
								.setOnClickCallback(new OnClickListener() {
									@Override
									public void onClick(View v) {
										cb.switchMode(Mode.DST);
										hideModeButtons();
									}
								}));
		this.modeButtons.add(new CircleButton(context)
								.setCaption("ADD")
								.setRadiusDip(modeButtonRadius)
								.setOnClickCallback(new OnClickListener() {
									@Override
									public void onClick(View v) {
										//cb.switchMode(Mode.ADD);
										hideModeButtons();
									}
								}));
		
		this.modeButtons.add(new CircleButton(context)
								.setCaption("Screen")
								.setRadiusDip(modeButtonRadius)
								.setOnClickCallback(new OnClickListener() {
									@Override
									public void onClick(View v) {
										cb.switchMode(Mode.SCREEN);
										hideModeButtons();
									}
								}));
		this.modeButtons.add(new CircleButton(context)
								.setCaption("Normal")
								.setRadiusDip(modeButtonRadius)
								.setOnClickCallback(new OnClickListener() {
									@Override
									public void onClick(View v) {
										cb.switchMode(null);
										hideModeButtons();
									}
								}));
		this.modeButtons.add(new CircleButton(context)
								.setCaption("XOR")
								.setRadiusDip(modeButtonRadius)
								.setOnClickCallback(new OnClickListener() {
									@Override
									public void onClick(View v) {
										cb.switchMode(Mode.XOR);
										hideModeButtons();
									}
								}));
		
		this.addAllButtonsToView();		
	}
	
	/**
	 * setup subcontrols: the later we add the view, the more it is on the top (above other controls)
	 */
	private void addAllButtonsToView(){
		this.addView(this.switchLayersButton);

		this.addView(this.shootButton);
		this.addView(this.doneButton);
		
		
		this.addView(this.shareImageButton);
		this.addView(this.shareImageYesButton);
		this.addView(this.shareImageNoButton);
		
		for(IconButton button : menuButtons){
			this.addView(button);
		}
		for(CircleButton button : modeButtons){
			this.addView(button);
		}
	}
	
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

    } // <1>

	private int dip2pix(int dip){
		return Tools.dip2Pixels(dip, context);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if(!this.initialised){
			
			this.positionButton(shootButton,         100, 50, dip2pix(-27), dip2pix(6));
			this.positionButton(doneButton,          100, 50, dip2pix(-27), dip2pix(6));

			this.positionButton(switchLayersButton   ,100,50, dip2pix(-20), dip2pix(-54));
			//this.positionButton(menuButton,          100, 50, -33, 85);
			this.positionButton(closeMenuButton,     100, 50, dip2pix(-20), dip2pix(60));
			//this.positionButton(focusButton,         100, 50, dip2pix(-66), dip2pix(83));
			
			
			Point modeButtonPos = this.positionButton(modeButton,  100, 50, -33-closeMenuButton.getRadius()*3, 80);
			
			this.positionButton(shareImageButton, 50, 50, 0, 0);
			this.positionButton(shareImageYesButton, 50, 50, shareImageButton.getRadius(), 0);
			this.positionButton(shareImageNoButton, 50, 50, 0, shareImageButton.getRadius());
			this.positionButton(saveImageButton, 0, 30, 40, 0);
			this.positionButton(newImageButton, 0, 30, 40+saveImageButton.getRadius()*2, 0);
			this.positionButton(loadImageButton, 0, 30, 40+saveImageButton.getRadius()*4, 0);
			this.positionButton(preferencesButton, 0, 30, 40+saveImageButton.getRadius()*6, 0);
			this.positionButton(instructionsButton, 0, 30, 40+saveImageButton.getRadius()*8, 0);
			
			
			
			double p = 0;
			for(CircleButton cb : modeButtons){
				double x = Math.sin(p)*100D;
				double y = Math.cos(p)*100D;
				this.positionButton(cb, 0, 0, modeButtonPos.x+(int)x, modeButtonPos.y+(int) y);
				p += Math.PI*2 / modeButtons.size();
			}
			this.hideMenu(false);
			this.hideModeButtons(false);
			this.hideShareMenu(false);
			this.doneButton.hide(false);
			this.switchLayersButton.hide(false);
			this.initialised = true;
			
			setVisibility(VISIBLE);
			shootButton.show();
		}
	}
	
	private Point positionButton(CircleButton button,int xPercentage, int yPercentage, int xOffset, int yOffset){
		Point p = new Point();
		if(button != null){
			int r = button.getRadius();
			p.x = xPercentage*getMeasuredWidth()/100+xOffset;
			p.y = yPercentage*getMeasuredHeight()/100+yOffset;
			button.layout(p.x-r, p.y-r, p.x+r, p.y+r);
			return p;
		}
		return null;
		
	}
	
	private Point positionButton(IconButton button,int xPercentage, int yPercentage, int xOffset, int yOffset){
		Point p = new Point();
		if(button != null){
			int r = button.getRadius();
			p.x = xPercentage*getMeasuredWidth()/100+xOffset;
			p.y = yPercentage*getMeasuredHeight()/100+yOffset;
			button.layout(p.x-r, p.y-r, p.x+r, p.y+r);
			return p;
		}
		return null;
		
	}
	
}
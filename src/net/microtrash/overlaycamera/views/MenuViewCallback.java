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

import android.graphics.PorterDuff.Mode;

public interface MenuViewCallback {
	public void shootButtonClicked();
	public void focusButtonClicked();
	public void undoButtonClicked();
	public void saveImageButtonClicked();
	public void newImageButtonClicked();
	public void shareImageButtonClicked();
	public void preferencesButtonClicked();
	public void onMenuHide();
	public void onMenuShow();
	public void instructionsButtonClicked();
	public void loadImageButtonClicked();
	public void switchMode(Mode mode);
	public void doneButtonClicked();
	public void switchLayersButtonClicked();
}

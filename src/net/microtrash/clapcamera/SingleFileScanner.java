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


import java.io.File;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class SingleFileScanner implements MediaScannerConnectionClient {
	
	private MediaScannerConnection mMs;
	private File mFile;
	private SingleFileScannerCallback callback = null;
	
	public SingleFileScanner(Context context, String filePath, SingleFileScannerCallback callback) {
		this.callback = callback;
		
	    mFile = new File(filePath);
	    mMs = new MediaScannerConnection(context, this);
	    mMs.connect();
	}
	public SingleFileScanner(Context context, String filePath) {
	
	    mFile = new File(filePath);
	    mMs = new MediaScannerConnection(context, this);
	    mMs.connect();
	}

	@Override
	public void onMediaScannerConnected() {
	    mMs.scanFile(mFile.getAbsolutePath(), null);
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
	    mMs.disconnect();
	    if(callback != null){
	    	callback.onScanCompleted(path, uri);
	    }
	}
	
}
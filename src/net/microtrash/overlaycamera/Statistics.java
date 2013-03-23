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

public class Statistics {
	// how many shots did the user take during this session
	public int shotsTotal = 0;
	// how many shots did the user take for this collage
	public int shotsOfComposition = 0;
	// how many cutouts did the user do for this collage
	public int cutoutPaths = 0;
	// how many times was the app started
	public int startsTotal = 0;
	public boolean hasVoted = false;
}

/*
UIThreadDispatcher.java
Copyright (C) 2014  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package com.nanfeng.pet.yytcallphonedemoapplication.linphone;

import android.os.Handler;
import android.os.Looper;

public class UIThreadDispatcher {
	private static Handler mHandler = new Handler(Looper.getMainLooper());
	
	public static void dispatch(Runnable r) {
		mHandler.post(r);
	}
}

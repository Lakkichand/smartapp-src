package com.jiubang.ggheart.apps.desks.appfunc;

import android.util.Log;

import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;

public class AppFuncExceptionHandler {
	public static void handle(Exception e) {
		if (e != null) {
			// e.printStackTrace();
			Log.e("AppFuncExceptionHandler", "Exception msg=" + e.getMessage());
		}
		GoLauncher.sendHandler(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
				IDiyFrameIds.APPFUNC_FRAME, IDiyMsgIds.APPDRAWER_OPERATION_FAILED, 0, null, null);
	}
}

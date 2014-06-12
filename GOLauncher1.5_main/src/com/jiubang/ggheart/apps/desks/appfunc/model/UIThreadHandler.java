package com.jiubang.ggheart.apps.desks.appfunc.model;

import android.os.Handler;
import android.os.Message;

import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;

/**
 * 
 * @author wenjiaming
 * 
 */
public class UIThreadHandler {

	private static UIThreadHandler instance;;
	private Handler handler;

	private UIThreadHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case AppFuncConstants.EXITAPPFUNCFROMHOME :
						XBaseGrid grid = (XBaseGrid) msg.obj;
						grid.notify(AppFuncConstants.EXITAPPFUNCFROMHOME, msg.arg1);
						break;
					case AppFuncConstants.OPENDIALOG_ALLAPP : {
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.ALLAPPS_GRID,
								AppFuncConstants.OPENDIALOG_ALLAPP, msg.obj);
					}
						break;
					case AppFuncConstants.APP_TICK :
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.XVIEW,
								AppFuncConstants.APP_TICK, null);
						break;
					case AppFuncConstants.START_REFRESH_GRID_LIST : {
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.XVIEW,
								AppFuncConstants.START_REFRESH_GRID_LIST, null);
					}
						break;
					case AppFuncConstants.FINISH_REFRESH_GRID_LIST : {
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.XVIEW,
								AppFuncConstants.FINISH_REFRESH_GRID_LIST, null);
					}
						break;
					default :
						break;
				}
			};
		};
	}

	/**
	 * 第一次调用时，必须由UI线程创建
	 * 
	 * @return
	 */
	public static UIThreadHandler getInstance() {
		if (instance == null) {
			instance = new UIThreadHandler();
		}
		return instance;
	}

	public void sendEmptyMessage(int what) {
		handler.sendEmptyMessage(what);
	}

	public static void destroyInstance() {
		instance = null;
	}

	public Message getMessage() {
		return handler.obtainMessage();
	}

	public void sendMessage(Message message) {
		handler.sendMessage(message);
	}

}

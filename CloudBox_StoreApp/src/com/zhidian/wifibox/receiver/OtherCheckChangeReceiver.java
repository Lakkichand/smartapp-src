package com.zhidian.wifibox.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zhidian.wifibox.listener.IOnMusicCheckChangeListener;
import com.zhidian.wifibox.listener.IOnOtherCheckChangeListener;

/**
 * 
 * 当其他文件被选择的时候发送该广播
 * 
 * @author shihuajian
 *
 */

public class OtherCheckChangeReceiver extends BroadcastReceiver {
	
	public final static String PATH_NAME = "com.zhidian.wifibox.receiver.otherCheckChange";
	
	public final static String CHOOSE_COUNT_FLAG = "ChooseCount";
	/** 总数量 */
	public final static String TOTAL_COUNT = "TotalCount";
	/** 是否需要刷新数据 */
	public final static String IS_REFRESH = "isRefresh";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context instanceof IOnMusicCheckChangeListener) {
			((IOnOtherCheckChangeListener) context).OnOtherCheckChange(intent);
		}

	}

}

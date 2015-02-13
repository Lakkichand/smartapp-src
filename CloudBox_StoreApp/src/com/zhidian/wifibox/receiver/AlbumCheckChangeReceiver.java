package com.zhidian.wifibox.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zhidian.wifibox.listener.IOnAlbumCheckChangeListener;
import com.zhidian.wifibox.listener.IOnMusicCheckChangeListener;

/**
 * 
 * 当图册被选择的时候发送该广播
 * 
 * @author shihuajian
 *
 */

public class AlbumCheckChangeReceiver extends BroadcastReceiver {
	
	/** 相册文件夹 */
	public final static String PATH_NAME = "com.zhidian.wifibox.receiver.albumCheckChange";
	/** 相册文件夹详细 */
	public final static String PATH_NAME2 = "com.zhidian.wifibox.receiver.albumDetailCheckChange";
	
	/** 所选的数量 */
	public final static String CHOOSE_COUNT_FLAG = "ChooseCount";
	/** 总数量 */
	public final static String TOTAL_COUNT = "TotalCount";
	/** 是否需要刷新数据 */
	public final static String IS_REFRESH = "isRefresh";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context instanceof IOnAlbumCheckChangeListener) {
			((IOnAlbumCheckChangeListener) context).OnAlbumCheckChange(intent);
		}

	}

}

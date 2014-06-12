package com.jiubang.ggheart.data.statistics;

import com.jiubang.ggheart.launcher.ICustomAction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 与木瓜移动合作时，通过广播机制进行数据交互的广播接收器 木瓜移动主要是通过该广播接收器获取GO桌面的IMEI号进行统计
 * 以后如果还有别的应用想获取GO桌面的IMEI号，也可以通过该广播接收器获取
 * 
 * @author wangzhuobin
 * 
 */
public class StatisticsBroadcastReceiver extends BroadcastReceiver {

//	public static String ACTION_GET_VIRTUAL_ID = "com.papayamobile.ACTION_GET_VIRTUAL_ID";
//	public static String ACTION_RETURN_VIRTUAL_ID = "com.papayamobile.ACTION_RETURN_VIRTUAL_ID";
	public static String KEY_PACKAGE_NAME = "packageName";
	public static String KEY_ID = "id";
	public static String KEY_ID_TYPE = "idType";
	public static final int TYPE_DETAULT_VALUE = 1;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (null == context || null == intent) {
			return;
		}
		// 外部的应用把其包名传过来，方便我们日后的处理
		String activePackageName = intent.getStringExtra(KEY_PACKAGE_NAME);
		// 通过广播把包命和IMEI返回给外部应用
		Intent returnIntent = new Intent(ICustomAction.ACTION_RETURN_VIRTUAL_ID);
		// 包命
		returnIntent.putExtra(KEY_PACKAGE_NAME, context.getPackageName());
		// IMEI
		returnIntent.putExtra(KEY_ID, Statistics.getVirtualIMEI(context));
		returnIntent.putExtra(KEY_ID_TYPE, TYPE_DETAULT_VALUE);
		context.sendBroadcast(returnIntent);
	}
}

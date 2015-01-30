package com.zhidian.wifibox.receiver;

import android.content.Context;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

/**
 * 信鸽广播
 * 
 * @author xiedezhi
 * 
 */
public class XGPushReceiver extends XGPushBaseReceiver {

	@Override
	public void onDeleteTagResult(Context arg0, int arg1, String arg2) {
	}

	@Override
	public void onNotifactionClickedResult(Context arg0,
			XGPushClickedResult arg1) {
	}

	@Override
	public void onNotifactionShowedResult(Context arg0, XGPushShowedResult arg1) {
	}

	@Override
	public void onRegisterResult(Context arg0, int arg1,
			XGPushRegisterResult arg2) {
	}

	@Override
	public void onSetTagResult(Context arg0, int arg1, String arg2) {
	}

	@Override
	public void onTextMessage(Context arg0, XGPushTextMessage arg1) {
	}

	@Override
	public void onUnregisterResult(Context arg0, int arg1) {
	}

}

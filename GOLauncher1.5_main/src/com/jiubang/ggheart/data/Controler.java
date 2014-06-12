package com.jiubang.ggheart.data;

import java.util.List;

import android.content.Context;

/**
 * 控制器
 * 
 * @author guodanyang
 * 
 */
public class Controler extends BroadCaster implements BroadCaster.BroadCasterObserver {
	// 上下文
	protected Context mContext;

	public Controler(Context context) {
		mContext = context;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void onBCChange(int msgId, int param, Object object, List objects) {
		onHandleBCChange(msgId, param, object, objects);
	}

	/**
	 * 处理收到的消息
	 * 
	 * @param msgId
	 * @param param
	 * @param object
	 * @param objects
	 */
	@SuppressWarnings("rawtypes")
	protected void onHandleBCChange(int msgId, int param, Object object, List objects) {
		return;
	}
}

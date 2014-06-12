package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.go.util.device.Machine;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageBaseBean;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean.MessageHeadBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * 类描述:  消息中心的主题推送通知栏
 * 功能详细描述: 当主题推送在通知栏的时候，点击，进入这个Activity触发某些内容
 * 
 * @author  shenjinbao
 * @date  [2012-10-20]
 */
public class MsgNotifyActivity extends Activity {
	private MessageManager mManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		String mId = intent.getExtras().getString("msgId");

		mManager = MessageManager.getMessageManager(GOLauncherApp.getContext());
		
		boolean isRemoved = intent.getExtras().getBoolean("remove");
		MessageHeadBean msgHeadBean = null;
		if (mManager != null) {
			msgHeadBean = mManager.getMessageHeadBean(mId);
		}
		if (isRemoved && mManager != null) {
			if (msgHeadBean != null) {
				mManager.markAsRemoved(msgHeadBean);
				Bitmap bitmap = msgHeadBean.mBitmap;
				if (bitmap != null && !bitmap.isRecycled()) {
					msgHeadBean.mBitmap.recycle();
					msgHeadBean.mBitmap = null;
				}
			}
			finish();
		} else {
			if (mManager != null && mManager.getMessageList() != null && Machine.isNetworkOK(this)) {
				if (msgHeadBean != null) {
					Bitmap bitmap = msgHeadBean.mBitmap;
					if (bitmap != null && !bitmap.isRecycled()) {
						msgHeadBean.mBitmap.recycle();
						msgHeadBean.mBitmap = null;
					}
					mManager.handleMsgClick(msgHeadBean,
							MessageBaseBean.VIEWTYPE_STATUS_BAR);
				}
				finish();
			} else {
				finish();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}

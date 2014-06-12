package com.jiubang.ggheart.components.facebook;

import android.os.Handler;
import android.os.Message;
/**
 * 
 * @author xiangliang
 *
 */
public class OGRequestServerHandler extends Handler {

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case FacebookOpenGraphUtil.FACEBOOK_OG_SUCCEESS:
			String url = (String) msg.obj;
			GoFacebookUtil.sendOpenGraph(url);
			break;

		default:
			break;
		}
	}
}

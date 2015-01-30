package com.zhidian.wifibox.javascript;

import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.zhidian.wifibox.activity.HTMLGameActivity;
import com.zhidian.wifibox.view.MainViewGroup;

/**
 * play68的本地接口
 * 
 * @author xiedezhi
 * 
 */
public class Play68Interface {

	private HTMLGameActivity activity;

	public Play68Interface(HTMLGameActivity activity) {
		this.activity = activity;
	}

	@JavascriptInterface
	public void jump() {
		if (activity == null) {
			return;
		}
		// 跳转到游戏：分类页
		Intent intent = new Intent(MainViewGroup.IPCACTION);
		intent.putExtra("action", 1);
		activity.sendBroadcast(intent);
		activity.finish();
	}
}
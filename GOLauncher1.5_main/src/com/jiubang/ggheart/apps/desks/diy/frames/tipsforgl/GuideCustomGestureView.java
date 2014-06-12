package com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 类描述:自定义手势提示 
 * 功能详细描述:
 * 
 * @author zhengxiangcan
 * @date [2012-9-17]
 */
public class GuideCustomGestureView extends RelativeLayout implements
		OnClickListener {
	private Button mButton; // 按钮got it

	public GuideCustomGestureView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mButton = (Button) findViewById(R.id.got_it);
		mButton.setOnClickListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (v == mButton) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.GUIDE_GL_FRAME,
					null, null);
			PreferencesManager sharedPreferences = new PreferencesManager(
					GoLauncher.getContext(), IPreferencesIds.USERTUTORIALCONFIG,
					Context.MODE_PRIVATE);
			sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_CUSTOM_GESTURE, false);
			sharedPreferences.commit();
		}
	}
}

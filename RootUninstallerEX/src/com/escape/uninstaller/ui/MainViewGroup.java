package com.escape.uninstaller.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.escape.uninstaller.activity.MainActivity;
import com.smartapp.rootuninstaller.R;

public class MainViewGroup extends LinearLayout {

	private ActionBarFrame mActionBarFrame;

	public MainViewGroup(Context context) {
		super(context);
	}

	public MainViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		mActionBarFrame = (ActionBarFrame) findViewById(R.id.actionbarframe);
		findViewById(R.id.sliding).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity activity = getActivity();
				if (activity != null) {
					activity.toggle();
				}
			}
		});
		findViewById(R.id.search).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 搜索
			}
		});
		findViewById(R.id.share).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 分享
			}
		});
		findViewById(R.id.sort).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 排序
			}
		});
		findViewById(R.id.action_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO 取消搜索
					}
				});
		findViewById(R.id.cleartext).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 清除
			}
		});
	}

	private MainActivity getActivity() {
		if ((getContext() != null) && (getContext() instanceof MainActivity)) {
			return (MainActivity) getContext();
		}
		return null;
	}

	public void onDestroy() {
		mActionBarFrame.stop();
	}

}

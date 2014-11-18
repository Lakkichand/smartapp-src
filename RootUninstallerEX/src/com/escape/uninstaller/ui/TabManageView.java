package com.escape.uninstaller.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.escape.uninstaller.activity.MainActivity;
import com.smartapp.rootuninstaller.R;

public class TabManageView extends LinearLayout {

	private ActionBarFrame mActionBarFrame;

	public TabManageView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setOrientation(VERTICAL);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		mActionBarFrame = (ActionBarFrame) inflater.inflate(
				R.layout.actionbarframe, null);
		mActionBarFrame.findViewById(R.id.sliding).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						MainActivity activity = getActivity();
						if (activity != null) {
							activity.toggle();
						}
					}
				});
		mActionBarFrame.findViewById(R.id.search).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO 搜索
					}
				});
		mActionBarFrame.findViewById(R.id.share).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO 分享
					}
				});
		mActionBarFrame.findViewById(R.id.sort).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO 排序
					}
				});
		mActionBarFrame.findViewById(R.id.action_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO 取消搜索
					}
				});
		mActionBarFrame.findViewById(R.id.cleartext).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO 清除
					}
				});
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		addView(mActionBarFrame, lp);
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

package com.escape.uninstaller.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.escape.uninstaller.activity.MainActivity;
import com.escape.uninstaller.adapter.MainAdapter;
import com.escape.uninstaller.util.DrawUtil;
import com.smartapp.rootuninstaller.R;
import com.viewpagerindicator.TitlePageIndicator;

public class TabManageView extends LinearLayout {

	private ActionBarFrame mActionBarFrame;

	private TitlePageIndicator mIndicator;
	private ViewPager mViewPager;
	private MainAdapter mAdapter;

	public TabManageView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setOrientation(VERTICAL);
		// actionbar
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
		// PageIndicator
		mIndicator = new TitlePageIndicator(getContext());
		int padding = DrawUtil.dip2px(getContext(), 10);
		mIndicator.setPadding(padding, padding, padding, padding);
		lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		addView(mIndicator, lp);
		// viewpager
		mViewPager = new ViewPager(getContext());
		mViewPager.setId(0x1234);
		lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0);
		lp.weight = 1;
		addView(mViewPager, lp);
		mAdapter = new MainAdapter(getActivity().getSupportFragmentManager());
		mViewPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mViewPager);
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

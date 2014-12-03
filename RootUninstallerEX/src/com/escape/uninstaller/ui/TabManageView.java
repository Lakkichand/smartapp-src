package com.escape.uninstaller.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.escape.uninstaller.activity.MainActivity;
import com.escape.uninstaller.adapter.MainAdapter;
import com.escape.uninstaller.util.DrawUtil;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.smartapp.rootuninstaller.R;
import com.viewpagerindicator.TitlePageIndicator;

public class TabManageView extends LinearLayout {

	private ActionBarFrame mActionBarFrame;

	private TitlePageIndicator mIndicator;
	private ViewPager mViewPager;
	private MainAdapter mAdapter;

	private List<IFragment> mFragmentList = new ArrayList<IFragment>();

	private ViewPager.OnPageChangeListener pageListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int currentScreen) {
			switch (currentScreen) {
			case 0:
				getActivity().getSlidingMenu().setTouchModeAbove(
						SlidingMenu.TOUCHMODE_FULLSCREEN);
				break;
			default:
				getActivity().getSlidingMenu().setTouchModeAbove(
						SlidingMenu.TOUCHMODE_NONE);
				break;
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	public TabManageView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setOrientation(VERTICAL);
		setBackgroundColor(0xFFebebeb);
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
		mFragmentList.add(UserAppFragment.newInstance());
		mFragmentList.add(SystemAppFragment.newInstance());
		mFragmentList.add(TrashFragment.newInstance());
		mAdapter.setList(mFragmentList);
		mViewPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mViewPager);
		mIndicator.setSelectedColor(0xFF45c01a);
		mIndicator.setTextColor(0x8845c01a);
		mIndicator.setTextSize(DrawUtil.dip2px(getContext(), 15.5f));
		mIndicator.setFooterColor(0xFF45c01a);
		mIndicator.setFooterLineColor(0xFFc8c8c8);
		mIndicator.setFooterLineHeight(DrawUtil.dip2px(getContext(), 1));
		mIndicator.setOnPageChangeListener(pageListener);
	}

	/**
	 * 当系统有安装，卸载，更新应用等操作时回调该接口
	 * 
	 * @param packName
	 *            安装/卸载/更新的包名
	 */
	public void onAppAction(String packName) {
		for (IFragment fragment : mFragmentList) {
			fragment.onAppAction(packName);
		}
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

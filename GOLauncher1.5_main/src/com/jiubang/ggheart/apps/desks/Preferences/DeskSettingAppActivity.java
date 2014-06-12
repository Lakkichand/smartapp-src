package com.jiubang.ggheart.apps.desks.Preferences;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingScreenAppFunTabView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingScreenDeskTabView;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;

/**
 * 
 * <br>类描述:桌面设置-应用设置Activity
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-10]
 */
public class DeskSettingAppActivity extends DeskSettingBaseActivity
		implements
			ScreenScrollerListener,
			OnClickListener {
	private DeskSettingScreenDeskTabView mDeskView; //桌面View
	private DeskSettingScreenAppFunTabView mFunctionView; //功能表View
	private ScrollerViewGroup mScrollerViewGroup; // 滚动控件
	private RelativeLayout mLayoutFunction;
	private RelativeLayout mLayoutDesk;
	private LinearLayout mButtonLineFunction;
	private LinearLayout mButtonLineDesk;
	private TextView mTextFunction;
	private TextView mTextDesk;
	private int mSelectColor;
	private int mNoSelectColor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.desk_setting_app);
		initScrollerViewGroup();
		initTitle();
		load();
	}

	/**
	 * <br>功能简述:初始化2个标题
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void initTitle() {
		mLayoutFunction = (RelativeLayout) findViewById(R.id.layout_function);
		mLayoutFunction.setOnClickListener(this);
		mLayoutDesk = (RelativeLayout) findViewById(R.id.layout_desk);
		mLayoutDesk.setOnClickListener(this);

		mTextFunction = (TextView) findViewById(R.id.text_function);
		mTextDesk = (TextView) findViewById(R.id.text_desk);

		mButtonLineFunction = (LinearLayout) findViewById(R.id.button_line_function);
		mButtonLineDesk = (LinearLayout) findViewById(R.id.button_line_desk);
		
		mSelectColor = getResources().getColor(R.color.desk_setting_tab_title_select);
		mNoSelectColor = getResources().getColor(R.color.desk_setting_tab_title_no_select);
	}

	/**
	 * <br>功能简述:初始化滚动内容
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void initScrollerViewGroup() {
		mScrollerViewGroup = new ScrollerViewGroup(DeskSettingAppActivity.this, this);
		mDeskView = new DeskSettingScreenDeskTabView(this);
		mFunctionView = new DeskSettingScreenAppFunTabView(this);
		mScrollerViewGroup.addView(mDeskView);
		mScrollerViewGroup.addView(mFunctionView);
		mScrollerViewGroup.setScreenCount(2);
		LinearLayout viewGroupLayout = (LinearLayout) findViewById(R.id.view_group_layout);
		viewGroupLayout.addView(mScrollerViewGroup);
	}

	@Override
	public void load() {
		super.load();
		mDeskView.load();
		mFunctionView.load();
	}

	@Override
	public void save() {
		super.save();
		mDeskView.save();
		mFunctionView.save();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.layout_desk :
				mScrollerViewGroup.gotoViewByIndex(0);
				break;

			case R.id.layout_function :
				mScrollerViewGroup.gotoViewByIndex(1);
				break;

			default :
				break;
		}

	}

	/**
	 * <br>功能简述:显示桌面设置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void showDeskTab() {
		mButtonLineFunction.setVisibility(View.GONE);
		mButtonLineDesk.setVisibility(View.VISIBLE);
		mTextFunction.setTextColor(mNoSelectColor);
		mTextDesk.setTextColor(mSelectColor);

	}
	/**
	 * <br>功能简述:显示功能表设置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void showFunAppTab() {
		mButtonLineFunction.setVisibility(View.VISIBLE);
		mButtonLineDesk.setVisibility(View.GONE);
		mTextFunction.setTextColor(mSelectColor);
		mTextDesk.setTextColor(mNoSelectColor);
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return null;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {

	}

	@Override
	public void onFlingIntercepted() {

	}

	@Override
	public void onScrollStart() {

	}

	@Override
	public void onFlingStart() {

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		if (newScreen == 0) {
			showDeskTab();
		}

		else if (newScreen == 1) {
			showFunAppTab();
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {

	}

	@Override
	public void invalidate() {

	}

	@Override
	public void scrollBy(int x, int y) {

	}

	@Override
	public int getScrollX() {
		return 0;
	}

	@Override
	public int getScrollY() {
		return 0;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDeskView != null) {
			mDeskView.changeOrientation();
		}

		if (mFunctionView != null) {
			mFunctionView.changeOrientation();
		}
	}

}

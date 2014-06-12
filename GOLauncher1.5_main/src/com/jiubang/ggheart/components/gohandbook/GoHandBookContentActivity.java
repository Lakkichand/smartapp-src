package com.jiubang.ggheart.components.gohandbook;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DesktopIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IndicatorListner;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicatorItem;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * <br>类描述: GO手册每页详细内容容器
 * <br>功能详细描述:
 * 
 * @author licanhui
 * @date [2012-8-24]
 */
public class GoHandBookContentActivity extends Activity
		implements
			ScreenScrollerListener,
			IndicatorListner,
			OnClickListener {
	private TextView mTitleTextView; // 标题
	private LinearLayout mbackLayout; // 标题布局
	private LinearLayout mViewGroupLayout; // 放webView布局
	private String[] mKeyList;
	private int mOpenPage; //需要打开的页面
	private int mUrlSize; //需要打开网页的数量
	private boolean mIsFirstScreenChanged = true; // 是否第一次触发ScreenChanged方法。如果打开的是mOpenPage是0页。不会触发
	private LinearLayout mIndicatorLayout; //指示器布局
	private DesktopIndicator mIndicator; // 指示器
	private ScrollerViewGroup mScrollerViewGroup; // 滚动控件

	private LinearLayout mTryNowLayout; // 马上试用布局
	private Button mContinueBrowseBtn; // 继续浏览按钮
	private Button mTryNowBtn; // 马上试用按钮

	private final static float MIN_PERCENT = 0; //指示器最小百分比
	private final static float MAX_PERCENT = 100; //指示器最大百分比

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.go_handbook_content);
		// 标题
		mTitleTextView = (TextView) findViewById(R.id.title);
		StatisticsData.countMenuData(this, StatisticsData.MENU_ID_MANUAL);
		String titleString = getIntent().getStringExtra(GoHandBookConstants.TITLE);
		if (titleString != null) {
			mTitleTextView.setText(titleString);
		}
		// 返回布局
		mbackLayout = (LinearLayout) findViewById(R.id.backLayout);
		mbackLayout.setOnClickListener(this);
		mIndicatorLayout = (LinearLayout) findViewById(R.id.indicator_layout);
		mTryNowLayout = (LinearLayout) findViewById(R.id.try_now_layout);
		mContinueBrowseBtn = (Button) findViewById(R.id.continue_browse_btn);
		mContinueBrowseBtn.setOnClickListener(this);
		mTryNowBtn = (Button) findViewById(R.id.try_now_btn);
		mTryNowBtn.setOnClickListener(this);
		initScrollerViewGroupData();

	}

	@Override
	protected void onDestroy() {
		//要循环销毁每个Webview，有可能webview没有加载完会一直加载。导致Activity注销不了
		if (mScrollerViewGroup != null) {
			for (int i = 0; i < mScrollerViewGroup.getChildCount(); i++) {
				GoHandBookItemWebView bookItemWebView = (GoHandBookItemWebView) mScrollerViewGroup.getChildAt(i);
				bookItemWebView.onDestroy();
				bookItemWebView = null;
			}
		}
		super.onDestroy();
	}

	/**
	 * 设置每个webView的内容数据和指示器
	 */
	private void initScrollerViewGroupData() {
		mScrollerViewGroup = new ScrollerViewGroup(GoHandBookContentActivity.this, this);

		mKeyList = getIntent().getStringArrayExtra(GoHandBookConstants.ID_LIST);
		mOpenPage = getIntent().getIntExtra(GoHandBookConstants.OPEN_PAGE, 0);

		// 判断等于第一页
		if (mOpenPage == 0) {
			mIsFirstScreenChanged = false; // 第0页不会触发ScreenChanged
		} else {
			mIsFirstScreenChanged = true;
		}

		String[] urls = getIntent().getStringArrayExtra(GoHandBookConstants.URL_LIST);
		String serverAddresString = getIntent().getStringExtra(GoHandBookConstants.SERVER_ADDRESS);
		if (serverAddresString == null) {
			serverAddresString = "";
		}

		//添加每个webView
		mUrlSize = urls.length;
		for (int i = 0; i < mUrlSize; i++) {
			GoHandBookItemWebView bookItemWebView = new GoHandBookItemWebView(this, mKeyList[i], i,
					mOpenPage);
			bookItemWebView.loadUrl(serverAddresString + urls[i]);
			mScrollerViewGroup.addView(bookItemWebView);
		}
		mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount()); //设置总页面
		mScrollerViewGroup.gotoViewByIndexImmediately(mOpenPage); //打开的页面

		//在布局添加滑屏控件
		LinearLayout.LayoutParams params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);

		mViewGroupLayout = (LinearLayout) findViewById(R.id.viewGroup);
		mViewGroupLayout.addView(mScrollerViewGroup, params);

		//设置指示器
		mIndicator = (DesktopIndicator) findViewById(R.id.indicator);
		mIndicator.setDefaultDotsIndicatorImage(R.drawable.guide_indicator_cur,
				R.drawable.guide_indicator_other);
		mIndicator.setDotIndicatorLayoutMode(ScreenIndicator.LAYOUT_MODE_ADJUST_PICSIZE);
		mIndicator.setDotIndicatorDrawMode(ScreenIndicatorItem.DRAW_MODE_INDIVIDUAL);
		mIndicator.setIndicatorListner(this); //设置指示器监听器
		mIndicator.setTotal(mUrlSize); //设置总数
		mIndicator.setCurrent(0); //指示器当前页

	}

	@Override
	public void clickIndicatorItem(int index) {
		mScrollerViewGroup.gotoViewByIndex(index); //点击指示器某个位置跳到某页

	}

	@Override
	public void sliding(float percent) {
		if (MIN_PERCENT <= percent && percent <= MAX_PERCENT) {
			mScrollerViewGroup.getScreenScroller().setScrollPercent(percent); //滑动指示器
		}
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {

		mIndicator.setCurrent(newScreen); //设置指示器当前页
		if (!mIsFirstScreenChanged) {
			GoHandBookItemWebView bookItemWebView = (GoHandBookItemWebView) mScrollerViewGroup
					.getChildAt(newScreen);
			//ps:当滑动到最后一页，继续滑动，当没有弹回正常位置继续滑动。newScreen会超出设置的页面总数。导致报错
			if (newScreen < mKeyList.length) {
				bookItemWebView.updateHaveReadData(mKeyList[newScreen]); //调用JS方法设置是否已读
			}
		} else {
			mIsFirstScreenChanged = false;
		}

		if (newScreen == mUrlSize - 1) {
			//最后一页隐藏指示器
			mIndicatorLayout.setVisibility(View.GONE);
			mTryNowLayout.setVisibility(View.VISIBLE);
		} else {
			mIndicatorLayout.setVisibility(View.VISIBLE);
			mTryNowLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public ScreenScroller getScreenScroller() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onScrollStart() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onFlingStart() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		// TODO Auto-generated method stub
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub
	}

	@Override
	public void scrollBy(int x, int y) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getScrollX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getScrollY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		//返回
			case R.id.backLayout :
				GoHandBookContentActivity.this.finish();
				break;
			//马上试用
			case R.id.try_now_btn :		
				tryNow();
				StatisticsData.countMenuData(this, StatisticsData.MENU_ID_MANUAL_TRY_NOW);
				break;
			//继续浏览
			case R.id.continue_browse_btn :
				continueBrowse();
				StatisticsData.countMenuData(this, StatisticsData.MENU_ID_MANUAL_CON_BROWSE);
				break;

			default :
				break;
		}
	}

	/**
	 * <br>功能简述:	继续浏览
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void continueBrowse() {
		Intent intent = new Intent();
		intent.putExtra(GoHandBookConstants.RESULT_TYPE, GoHandBookConstants.CONTINUE_BROWSE_TYPE);
		this.setResult(RESULT_OK, intent);
		this.finish();
	}

	/**
	 * <br>功能简述:马上试用
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void tryNow() {
		Intent intent = new Intent();
		intent.putExtra(GoHandBookConstants.RESULT_TYPE, GoHandBookConstants.TRY_NOW_TYPE);
		this.setResult(RESULT_OK, intent);
		this.finish();
	}
	
	@Override
	public Resources getResources() {
		DeskResourcesConfiguration configuration = DeskResourcesConfiguration.createInstance(this.getApplicationContext());
		if (null != configuration) {
			Resources resources = configuration.getDeskResources();
			if (null != resources) {
				return resources;
			}
		}
		return super.getResources();
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

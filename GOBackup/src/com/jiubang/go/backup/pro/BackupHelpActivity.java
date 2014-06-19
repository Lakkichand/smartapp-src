package com.jiubang.go.backup.pro;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.ui.ScrollerView.ScreenScroller;
import com.jiubang.go.backup.pro.ui.ScrollerView.ScreenScrollerListener;
import com.jiubang.go.backup.pro.ui.ScrollerView.ScrollerViewGroup;

/**
 * @author jiangpeihe
 *帮助文档
 */
public class BackupHelpActivity extends Activity
		implements
			ScreenScrollerListener,
			android.view.View.OnClickListener {
	public static final String EXTRA_TAB_ID = "extra_tab_id";
	public static final int QUESTIONS_TAB_ID = 0;
	public static final int RULES_TAB_ID = 1;
	
	private ScrollerViewGroup mScrollerViewGroup;
	private ViewGroup mTabBar = null;
	private ViewGroup mRulesFrame = null;
	private ViewGroup mQuestionsFrame = null;
	private TextView mQuestionsTab = null;
	private TextView mRulesTab = null;
	private ImageView mQuestionsTabIndicator = null;
	private ImageView mRulesTabIndicator = null;
	private int mCurTabId = QUESTIONS_TAB_ID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_main_activity);
		initScrollView();
		initTabView();
		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setText(getResources().getString(R.string.help));
		View returnButton = findViewById(R.id.return_btn);
		if (returnButton != null) {
			returnButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
		TextView whatIsRootView = (TextView) findViewById(R.id.what_is_root);
		//底部加横线;
		whatIsRootView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		whatIsRootView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://en.wikipedia.org/wiki/Rooting_(Android_OS)"));
				try {
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		TextView howToGetRootView = (TextView) findViewById(R.id.how_to_get_root);
		//底部加横线;
		howToGetRootView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		howToGetRootView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://en.wikipedia.org/wiki/Rooting_(Android_OS)"));
				try {
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		if (getIntent() != null) {
			mCurTabId = getIntent().getIntExtra(EXTRA_TAB_ID, QUESTIONS_TAB_ID);
			mScrollerViewGroup.gotoViewByIndex(mCurTabId);
			focusViewTab(mCurTabId);
		}
	}
	/**
	 * 初始化滚动功能页
	 */
	private void initScrollView() {
		mScrollerViewGroup = (ScrollerViewGroup) findViewById(R.id.scrollerPageView);
		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
			mScrollerViewGroup.setScreenScrollerListener(this);
		}
	}

	/**
	 * 初始化tab页
	 */
	private void initTabView() {
		mTabBar = (ViewGroup) findViewById(R.id.mainview_tab);
		if (mTabBar != null) {
			mRulesTab = (TextView) mTabBar.findViewById(R.id.web_text);
			mRulesTabIndicator = (ImageView) mTabBar.findViewById(R.id.web_image);
			mRulesFrame = (ViewGroup) mTabBar.findViewById(R.id.web_layout);
			mRulesTab.setOnClickListener(this);
			mRulesTabIndicator.setOnClickListener(this);
			mRulesFrame.setOnClickListener(this);
			mQuestionsTab = (TextView) mTabBar.findViewById(R.id.local_text);
			mQuestionsTabIndicator = (ImageView) mTabBar.findViewById(R.id.local_image);
			mQuestionsFrame = (ViewGroup) mTabBar.findViewById(R.id.local_layout);
			mQuestionsTab.setOnClickListener(this);
			mQuestionsTabIndicator.setOnClickListener(this);
			mQuestionsFrame.setOnClickListener(this);
			focusViewTab(mCurTabId);
		}
	}

	/**
	 * 本地tab选中状态
	 */
	private void focusViewTab(int tabId) {
		switch (tabId) {
			case QUESTIONS_TAB_ID :
				mRulesTab.setTextColor(getResources().getColor(R.color.main_tab_text_unfocus));
				mRulesTabIndicator.setVisibility(View.GONE);
				mQuestionsTab.setTextColor(getResources().getColor(R.color.main_tab_text_focused));
				mQuestionsTabIndicator.setVisibility(View.VISIBLE);
				mQuestionsTabIndicator.setImageResource(R.drawable.theme_tab_light);
				break;
			case RULES_TAB_ID :
				mRulesTab.setTextColor(getResources().getColor(R.color.main_tab_text_focused));
				mRulesTabIndicator.setVisibility(View.VISIBLE);
				mRulesTabIndicator.setImageResource(R.drawable.theme_tab_light);
				mQuestionsTab.setTextColor(getResources().getColor(R.color.main_tab_text_unfocus));
				mQuestionsTabIndicator.setVisibility(View.GONE);
				break;
			default :
				break;
		}
	}
	@Override
	public void onClick(View v) {
		if (v == mRulesTab || v == mRulesFrame || v == mRulesTabIndicator) {
			if (mCurTabId == RULES_TAB_ID) {
				return;
			}
			mCurTabId = RULES_TAB_ID;
		} else if (v == mQuestionsTab || v == mQuestionsTabIndicator || v == mQuestionsFrame) {
			if (mCurTabId == QUESTIONS_TAB_ID) {
				return;
			}
			mCurTabId = QUESTIONS_TAB_ID;
		}
		mScrollerViewGroup.gotoViewByIndex(mCurTabId);
		focusViewTab(mCurTabId);
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
		// TODO Auto-generated method stub

	}
	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mCurTabId = newScreen;
		focusViewTab(mCurTabId);
	}
	@Override
	public void onScrollFinish(int currentScreen) {

	}
	@Override
	public void postInvalidate() {

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

}

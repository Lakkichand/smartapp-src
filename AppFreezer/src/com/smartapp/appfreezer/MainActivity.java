package com.smartapp.appfreezer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.smartapp.appfreezer.ui.GoBackupTabsBar;
import com.smartapp.appfreezer.ui.ScreenScroller;
import com.smartapp.appfreezer.ui.ScreenScrollerListener;
import com.smartapp.appfreezer.ui.ScrollerViewGroup;

public class MainActivity extends Activity implements ScreenScrollerListener {

	// TODO 监听程序安装事件
	// TODO 刷新机制如何？
	// TODO 刚进去不获取root权限，展示非冻结应用和冻结应用，进行冻结/解冻时才申请root权限
	// TODO 界面仿照GO备份和App Quarantine
	// TODO 右上角有个菜单按钮，有筛选、刷新、反馈、评分、分享、其他应用
	// TODO 加入crash report
	// TODO 广告条
	// TODO 换图标

	private ScrollerViewGroup mScrollerViewGroup;
	/**
	 * 顶部tab栏
	 */
	private GoBackupTabsBar mTabsBar = null;

	/**
	 * 已启用listview
	 */
	private ListView mEnableListView = null;
	private IAdapter mEnableAdapter = null;

	/**
	 * 已禁用listview
	 */
	private ListView mDisableListView = null;
	private IAdapter mDisableAdapter = null;

	private Button mFreezeBtn = null;
	/**
	 * 冻结按钮
	 */
	private ViewGroup mFreezeBtnFrame = null;

	private Button mUnFreezeBtn = null;
	/**
	 * 解冻按钮
	 */
	private ViewGroup mUnFreezeBtnFrame = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		initScrollView();
		initTabView();

		mEnableListView = (ListView) findViewById(R.id.enable_listview);
		mEnableAdapter = new IAdapter(MainActivity.this,
				new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						refreshBtn();
					}
				});
		mEnableListView.setAdapter(mEnableAdapter);
		mEnableListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 按下整项改变checkbox
				ListDataBean bean = (ListDataBean) view.getTag();
				bean.mIsSelect = !bean.mIsSelect;
				mEnableAdapter.notifyDataSetChanged();
			}
		});

		mDisableListView = (ListView) findViewById(R.id.disable_listview);
		mDisableAdapter = new IAdapter(MainActivity.this,
				new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						refreshBtn();
					}
				});
		mDisableListView.setAdapter(mDisableAdapter);
		mDisableListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 按下整项改变checkbox
				ListDataBean bean = (ListDataBean) view.getTag();
				bean.mIsSelect = !bean.mIsSelect;
				mDisableAdapter.notifyDataSetChanged();
			}
		});
		// 刷新已启用和已禁用列表
		refresh();

		mFreezeBtn = (Button) findViewById(R.id.freeze_btn);
		mFreezeBtn.setClickable(false);
		mUnFreezeBtn = (Button) findViewById(R.id.unfreeze_btn);
		mUnFreezeBtn.setClickable(false);

		mFreezeBtnFrame = (ViewGroup) findViewById(R.id.freeze_btn_frame);
		mUnFreezeBtnFrame = (ViewGroup) findViewById(R.id.unfreeze_btn_frame);

		mFreezeBtnFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 冻结应用
			}
		});

		mUnFreezeBtnFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 解冻应用
			}
		});
		// 更新冻结/解冻按钮状态
		refreshBtn();
	}

	/**
	 * 更新冻结/解冻按钮状态
	 */
	private void refreshBtn() {
		int freezeCount = mEnableAdapter.getSelectCount();
		int unfreezeCount = mDisableAdapter.getSelectCount();

		if (freezeCount <= 0) {
			mFreezeBtn.setTextColor(0xFF909090);
			mFreezeBtnFrame.setEnabled(false);
		} else {
			mFreezeBtn.setTextColor(0xffffffff);
			mFreezeBtnFrame.setEnabled(true);
		}
		mFreezeBtn
				.setText(getString(R.string.freeze) + "(" + freezeCount + ")");

		if (unfreezeCount <= 0) {
			mUnFreezeBtn.setTextColor(0xFF909090);
			mUnFreezeBtnFrame.setEnabled(false);
		} else {
			mUnFreezeBtn.setTextColor(0xffffffff);
			mUnFreezeBtnFrame.setEnabled(true);
		}
		mUnFreezeBtn.setText(getString(R.string.unfreeze) + "(" + unfreezeCount
				+ ")");
	}

	/**
	 * 刷新已启用和已禁用列表
	 */
	private void refresh() {
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		dialog.setMessage(getString(R.string.wait));
		dialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		dialog.show();

		Thread thread = new Thread() {
			@Override
			public void run() {
				final List<ListDataBean> enableList = new ArrayList<ListDataBean>();
				final List<ListDataBean> disableList = new ArrayList<ListDataBean>();
				AppFreezer.getEnableAndDisableAppByApi(MainActivity.this,
						enableList, disableList);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						dialog.dismiss();
						mEnableAdapter.update(enableList);
						mDisableAdapter.update(disableList);
					}
				});

			}
		};
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}

	/**
	 * 初始化滚动功能页
	 */
	private void initScrollView() {
		mScrollerViewGroup = (ScrollerViewGroup) findViewById(R.id.scrollerPageView);
		mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
		mScrollerViewGroup.setScreenScrollerListener(this);
		mScrollerViewGroup.gotoViewByIndex(0);

	}

	/**
	 * 初始化tab页
	 */
	private void initTabView() {
		FrameLayout tabFrame = (FrameLayout) findViewById(R.id.tabbar_frame);
		mTabsBar = new GoBackupTabsBar(MainActivity.this,
				new GoBackupTabsBar.TabObserver() {

					@Override
					public void handleChangeTab(int tabIndex) {
						// 控制scrollerViewGroup跳转
						mScrollerViewGroup.gotoViewByIndex(tabIndex);
					}
				});
		tabFrame.addView(mTabsBar);
		mTabsBar.cleanData();
		List<String> titles = new ArrayList<String>();
		titles.add(MainActivity.this.getResources().getString(R.string.enable));
		titles.add(MainActivity.this.getResources().getString(R.string.disable));
		mTabsBar.initTabsBar(titles);
		mTabsBar.setButtonSelected(0, false);

		mTabsBar.setBackgroundColor(0xFF202020);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 杀掉进程
		android.os.Process.killProcess(android.os.Process.myPid());
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
		mTabsBar.setButtonSelected(newScreen, true);
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

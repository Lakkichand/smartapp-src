package com.jiubang.ggheart.appgame.base.component;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 搜索container，根据UI2.0新增
 * 
 * @author xiedezhi
 * 
 */
public class SearchContainer extends FrameLayout implements IContainer {
	private LayoutInflater mInflater = null;
	/**
	 * 搜索View
	 */
	private AppsManagementSearchView mSearchView = null;
	/**
	 * searchView的布局属性
	 */
	private FrameLayout.LayoutParams mParams = null;

	private int mAccess = -1;
	/**
	 * 该container的分类id
	 */
	private int mTypeId = -1;
	/**
	 * 是否在激活状态
	 */
	private boolean mIsActive = false;

	public SearchContainer(Context context) {
		super(context);
	}

	public SearchContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SearchContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mInflater = LayoutInflater.from(getContext());
	}

	/**
	 * 展示搜索界面
	 */
	private void showSearchView() {
		this.removeView(mSearchView);
		// 初始化searchView
		mSearchView = (AppsManagementSearchView) mInflater.inflate(R.layout.apps_management_search,
				null);
		mParams = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		this.addView(mSearchView, mParams);
		mSearchView.setVisibility(View.VISIBLE);
		mSearchView.setClickable(true);
		// 返回按钮设为不可见
		View backBtn = mSearchView.findViewById(R.id.apps_management_search_back_btn);
		backBtn.setVisibility(View.GONE);
		// 输入框背景
		View edittext = mSearchView.findViewById(R.id.apps_management_search_edt);
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) edittext
				.getLayoutParams();
		params.leftMargin = RecommAppsUtils.dip2px(getContext(), 10.666667f);
		// 设置入口
		mSearchView.setAccess(mAccess);
		// 填充搜索关键字
		mSearchView.showIM(false);
		mSearchView.setBackClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AppsManagementActivity.sendHandler(getContext(),
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						IDiyMsgIds.REMOVE_SEARCH_VIEW, 0, null, null);
			}
		});
		// 展示热门搜索关键字
//		mSearchView.showHotSearchKeyword();
		// 使输入框获取焦点
		EditText et = (EditText) mSearchView.findViewById(R.id.apps_management_search_edt);
		if (!et.isFocused()) {
			OnFocusChangeListener listener = et.getOnFocusChangeListener();
			et.setOnFocusChangeListener(null);
			et.requestFocus();
			et.setOnFocusChangeListener(listener);
		}
	}

	@Override
	public void cleanup() {

	}

	@Override
	public void sdCardTurnOff() {

	}

	@Override
	public void sdCardTurnOn() {

	}

	@Override
	public void onActiveChange(boolean isActive) {
		if (isActive == mIsActive) {
			return;
		}
		mIsActive = isActive;
		if (isActive) {
			showSearchView();
		} else {
			if (mSearchView != null) {
				mSearchView.setVisibility(View.GONE);
				this.removeView(mSearchView);
				mSearchView = null;
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(AppGameMenu menu) {
		ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
		boolean isNeedDownloadManager = true;
		if (channelConfig != null) {
			isNeedDownloadManager = channelConfig.isNeedDownloadManager();
		}
		int resId[] = null;
		if (isNeedDownloadManager) {
			resId = new int[] { IMenuHandler.MENU_ITEM_FRESH,
					IMenuHandler.MENU_ITEM_DOWNLOAD_MANAGER, IMenuHandler.MENU_ITEM_SETTING,
					IMenuHandler.MENU_ITEM_FEEDBACK };
		} else {
			resId = new int[] { IMenuHandler.MENU_ITEM_FRESH, IMenuHandler.MENU_ITEM_SETTING,
					IMenuHandler.MENU_ITEM_FEEDBACK };
		}
		menu.setResourceId(resId);
		menu.show(this);
		return true;
	}

	@Override
	public boolean onOptionItemSelected(int id) {
		switch (id) {
		case IMenuHandler.MENU_ITEM_FRESH:
			// 整个tab栏刷新
			TabController.refreshCurrentTab();
			return true;
		}
		return false;
	}

	@Override
	public void onResume() {
		if (mSearchView != null) {
			mSearchView.doRefresh();
		}
	}

	@Override
	public void onStop() {

	}

	@Override
	public void onAppAction(String packName, int appAction) {
		if (mSearchView != null) {
			mSearchView.onAppAction(packName, appAction);
		}
	}

	@Override
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh) {
		if (bean == null) {
			return;
		}
		mTypeId = bean.typeId;
	}

	@Override
	public void initEntrance(int access) {
		mAccess = access;
	}

	@Override
	public int getTypeId() {
		return 0;
	}

	@Override
	public void onFinishAllUpdateContent() {

	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		if (mSearchView != null) {
			mSearchView.notifyDownloadState(downloadTask);
			// mSearchView.updateListView(downloadTask);
		}
	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (mSearchView != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			// 按返回键时，如果有显示结果，则把结果清除，否则按默认返回事件处理
			if (!mSearchView.isResultEmpty()) {
				mSearchView.cleanSearchData();
				// listview设为不可见
				View listview = mSearchView.findViewById(R.id.apps_management_search_result_list_frame);
				listview.setVisibility(View.GONE);
				mSearchView.showIM(false);
				return true;
			} else {
				return false;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onTrafficSavingModeChange() {
		if (mSearchView != null) {
			mSearchView.doRefresh();
		}
	}

	@Override
	public void setUpdateData(Object value, int state) {
		
	}

	@Override
	public void fillupMultiContainer(List<CategoriesDataBean> cBeans, List<IContainer> containers) {
		//do nothing	
	}

	@Override
	public void removeContainers() {
		//do nothing	
	}

	@Override
	public List<IContainer> getSubContainers() {
		return null;
	}

	@Override
	public void onMultiVisiableChange(boolean visiable) {
		//do nothing			
	}
	
	@Override
	public void prevLoading() {
		//do nothing
	}

	@Override
	public void prevLoadFinish() {
		//do nothing
	}

	@Override
	public void setBuilder(ContainerBuiler builder) {
		// do nothing
	}
}

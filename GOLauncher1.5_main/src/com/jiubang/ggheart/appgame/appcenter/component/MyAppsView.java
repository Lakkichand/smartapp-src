package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.bean.AppInfo;
import com.jiubang.ggheart.appgame.appcenter.component.MyAppsListAdapter.AppstListItemViews;
import com.jiubang.ggheart.appgame.appcenter.contorler.MyAppsDataManager.SortedAppInfo;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 
 * <br>
 * 类描述: 我的应用，列表view <br>
 * 功能详细描述:
 * 
 * @author zhoujun
 * @date [2012-9-11]
 */
public class MyAppsView extends PinnedHeaderListView {

	public final static int APP_IN_PHONE = 0;
	public final static int APP_IN_SDCARD = 1;
	private PopupWindow mPopupWindow;
	private MyAppsListAdapter mListAdapter = null;
	private int mAppListState = 0;
	// private Handler mHandler = null;
	private Handler mContainerHandler = null;

	// 界面调用类型
	public final static int VIEW_TYPE_APPS = 0; // 我的应用
	public final static int VIEW_TYPE_APPS_UNINSTALL = 1; // 批量卸载

	private int mViewType = VIEW_TYPE_APPS;

	private int mCurrSelectedPos = -1;

	public MyAppsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyAppsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyAppsView(Context context) {
		super(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

	}

	/**
	 * 设置界面调用类型 由于此界面供 我的应用，批量卸载 共同使用，以此值区分
	 * 
	 * @param type
	 * @author zhaojunjie
	 */
	public void setViewtype(int type) {
		mViewType = type;
	}

	/**
	 * 
	 * @param h
	 */
	public void setContainerHandler(Handler h) {
		mContainerHandler = h;
	}

	public void initView() {
		mListAdapter = new MyAppsListAdapter(getContext(), mAppListState,
				mViewType);
		// this.setSelector(R.drawable.recomm_app_list_item_selector);
		mListAdapter.setContainerHandler(mContainerHandler);
		setAdapter(mListAdapter);
		this.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {

				// // 没有安装GO任务管理或者安装的版本低，提示用户安装或更新
				// if
				// (!GOLauncherApp.getApplication().isHasInstalledTaskmanager())
				// {
				// if (!GOLauncherApp.getApplication().isHasShowMessage()) {
				// // 弹dialog来提示
				// onCreateDialog(view.getContext(), position);
				// GOLauncherApp.getApplication().setHasShowMessage(true);
				// return;
				// }
				// }
				// 如果是应用管理
				// 则显示卸载界面
				final AppInfo itemInfo = (AppInfo) mListAdapter
						.getItem(position);
				if (mViewType == VIEW_TYPE_APPS) {
				} else if (mViewType == VIEW_TYPE_APPS_UNINSTALL) { // 是批量卸载，则钩选
					selectApp(position, (AppstListItemViews) view.getTag());
				}
			}
		});

		// setOnItemLongClickListener(new OnItemLongClickListener() {
		//
		// @Override
		// public boolean onItemLongClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// AppsManagementActivity.sendHandler(this,
		// IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
		// IDiyMsgIds.SHOW_UNINSTALL_APP_VIEW, 0, null, null);
		// return true;
		// }
		// });
		// this.setDivider(null);
		// LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());
		// View mPinnedHeader = mLayoutInflater.inflate(
		// R.layout.appsmanagement_list_head, this, false);
		// this.setPinnedHeaderView(mPinnedHeader);

	}

	private void addHeaderView() {
		LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());
		View mPinnedHeader = mLayoutInflater.inflate(
				R.layout.recomm_appsmanagement_list_head, this, false);
		// 对显示的文字做margin的设置
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		TextView tv = (TextView) mPinnedHeader.findViewById(R.id.nametext);
		tv.setPadding(
				getContext().getResources()
				.getDimensionPixelSize(R.dimen.download_manager_text_padding) * 2, getContext()
				.getResources()
				.getDimensionPixelSize(R.dimen.download_manager_text_padding), 0, getContext()
				.getResources()
				.getDimensionPixelSize(R.dimen.download_manager_text_padding));
		tv.setLayoutParams(lp);
		this.setPinnedHeaderView(mPinnedHeader);
	}

	public void setData(SortedAppInfo sortedAppInfo) {
		if (mListAdapter != null) {
			mListAdapter.setData(sortedAppInfo);
		}

	}

	public void setAppListState(int state) {
		mAppListState = state;
	}

	// public void setHandler(Handler handler) {
	// mHandler = handler;
	// }

	// public void updateList() {
	// init();
	// }

	// public void updateList(List<String> appSizeList) {
	// if (mListAdapter != null) {
	// mListAdapter.refreshData(appSizeList);
	// }
	// }

	public void showHeaderView() {
		PreferencesManager preferences = new PreferencesManager(getContext(),
				IPreferencesIds.APPS_ORDER_TYPE, Context.MODE_PRIVATE);
		int orderType = preferences.getInt("orderType", 2);
		if (orderType == 2) {
			if (this.getmHeaderView() == null) {
				addHeaderView();
			}
		} else {
			if (this.getmHeaderView() != null) {
				this.setPinnedHeaderView(null);
			}
		}
	}

	public void refreshView() {
		if (mListAdapter != null) {
			mListAdapter.notifyDataSetChanged();
		}
	}

	public int getmCurrSelectedPos() {
		return mCurrSelectedPos;
	}

	public void resetSelectedPos() {
		mCurrSelectedPos = -1;
	}

	/**
	 * 
	 * @param position
	 */
	private void selectApp(int position, AppstListItemViews itemViews) {
		AppInfo itemInfo = (AppInfo) mListAdapter.getItem(position);
		mListAdapter.clickedApp(itemInfo, itemViews);
	}

	public void cleanup() {

		if (mListAdapter != null) {
			mListAdapter.cleanup();
			mListAdapter = null;
		}

		int count = this.getChildCount();
		if (count > 0) {
			AppstListItemViews appListItemView = null;
			for (int i = 0; i < count; i++) {
				appListItemView = (AppstListItemViews) this.getChildAt(i)
						.getTag();
				appListItemView.destory();
			}
		}
		// if (mSaveIsOpen != null) {
		// mSaveIsOpen.clear();
		// }
	}

	/**
	 * 改变所有应用选中状态
	 * 
	 * @param b
	 * @author zhaojunjie
	 */
	public void setAllAppsSelectState(boolean b) {
		mListAdapter.setAllAppsSelectState(b);
		mListAdapter.notifyDataSetChanged();
	}

	/**
	 * 取得被选中的应用包名
	 * 
	 * @return
	 */
	public ArrayList<String> getSelectApps() {
		return mListAdapter.getSelectApps();
	}

	/**
	 * 刷新列表
	 */
	public void refreshData() {
		// mListAdapter.refreshData();
	}

}

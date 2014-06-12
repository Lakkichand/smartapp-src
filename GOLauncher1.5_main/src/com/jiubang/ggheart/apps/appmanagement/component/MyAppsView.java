package com.jiubang.ggheart.apps.appmanagement.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.appmanagement.bean.AppInfo;
import com.jiubang.ggheart.apps.appmanagement.component.MyAppsListAdapter.AppstListItemViews;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 
 * <br>类描述:我的应用
 * <br>功能详细描述:
 */
public class MyAppsView extends PinnedHeaderListView {

	public final static int APP_IN_PHONE = 0;
	public final static int APP_IN_SDCARD = 1;

	// private ArrayList<AppItemInfo> mMyAppsData = null;
	private MyAppsListAdapter mListAdapter = null;
	private int mAppListState = 0;
	private Handler mHandler = null;
	private Handler mContainerHandler = null;

	// 界面调用类型
	public static int VIEW_TYPE_APPS = 0; // 我的应用
	public static int VIEW_TYPE_APPS_UNINSTALL = 1; // 批量卸载

	private int mViewType = VIEW_TYPE_APPS;

	// 样式

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

		mListAdapter = new MyAppsListAdapter(getContext(), mAppListState, mViewType);
		mListAdapter.setContainerHandler(mContainerHandler);
		this.setSelector(R.drawable.recomm_app_list_item_selector);
		setAdapter(mListAdapter);
		setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
				if (mViewType == VIEW_TYPE_APPS) {
					showAppInfo(position);
				} else if (mViewType == VIEW_TYPE_APPS_UNINSTALL) { // 是批量卸载，则钩选
					SelectApp(position, (AppstListItemViews) view.getTag());
				}
			}
		});

		setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// mContactLogic.onScrollStateChanged(view, scrollState);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {
				// AndroidDevice.hideInputMethod(ContactListActivity.this);
				if (view instanceof PinnedHeaderListView) {
					((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
				}
			}
		});
		// this.setDivider(null);
		// LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());
		// View mPinnedHeader = mLayoutInflater.inflate(
		// R.layout.appsmanagement_list_head, this, false);
		// this.setPinnedHeaderView(mPinnedHeader);

	}

	private void addHeaderView() {
		LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());
		View mPinnedHeader = mLayoutInflater.inflate(R.layout.recomm_appsmanagement_list_head,
				this, false);
		TextView tv = (TextView) mPinnedHeader.findViewById(R.id.nametext);
		tv.setPadding(
				0,
				getContext().getResources().getDimensionPixelSize(
						R.dimen.download_manager_text_padding), 0, getContext().getResources()
						.getDimensionPixelSize(R.dimen.download_manager_text_padding));
		this.setPinnedHeaderView(mPinnedHeader);
	}

	public void setAppListState(int state) {
		mAppListState = state;
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	// public void updateList() {
	// init();
	// }

	public void updateList(List<String> appSizeList) {
		if (mListAdapter != null) {
			mListAdapter.refreshData(appSizeList);
		}
	}

	public void showHeaderView() {
		PreferencesManager preferences = new PreferencesManager(getContext(),
				IPreferencesIds.APPS_ORDER_TYPE, Context.MODE_PRIVATE);
		int orderType = preferences.getInt("orderType", 0);
		if (orderType == 0) {
			if (this.getmHeaderView() == null) {
				addHeaderView();
			}
		} else {
			if (this.getmHeaderView() != null) {
				this.setPinnedHeaderView(null);
			}
		}
	}

	// public void refreshData(List<String> appSizeList) {
	// if (mListAdapter != null) {
	// mListAdapter.refreshData(appSizeList, false);
	// }
	// }

	public void refreshView() {
		if (mListAdapter != null) {
			mListAdapter.notifyDataSetChanged();
		}
	}

	// protected void onCreateDialog(final Context context, final int position)
	// {
	//
	// AlertDialog alertBuilder = new AlertDialog.Builder(context).create();
	// alertBuilder.setTitle(context.getResources().getString(
	// R.string.apps_management_dialog_title));
	// alertBuilder.setMessage(context.getResources().getString(
	// R.string.apps_management_dialog_message)
	// + "\n");
	// alertBuilder.setButton(
	// DialogInterface.BUTTON_POSITIVE,
	// context.getResources().getString(
	// R.string.apps_management_dialog_button_confirm),
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// // 去goStrore下载高级任务管理器
	// GoStoreOperatorUtil.gotoStoreDetailDirectly(context,
	// ApplicationManager.GO_TASKMANAGE_PACKAGENAME,
	// 20);
	// }
	// });
	//
	// alertBuilder.setButton(DialogInterface.BUTTON_NEGATIVE, getResources()
	// .getString(R.string.apps_management_dialog_button_cancel),
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// showAppInfo(position);
	// dialog.dismiss();
	// }
	// });
	// alertBuilder.show();
	// }

	private void showAppInfo(int position) {
		// AppItemInfo itemInfo = (AppItemInfo) mListAdapter.getItem(position);
		AppInfo itemInfo = (AppInfo) mListAdapter.getItem(position);

		if (mHandler != null) {
			Message msg = new Message();
			msg.obj = itemInfo.mPackageName;
			msg.what = AppsManagementActivity.SHOW_APP_DETAILS;
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 
	 * @param position
	 */
	private void SelectApp(int position, AppstListItemViews itemViews) {
		AppInfo itemInfo = (AppInfo) mListAdapter.getItem(position);
		mListAdapter.ClickedApp(itemInfo, itemViews);
	}

	public void cleanup() {
		int count = this.getChildCount();
		if (count > 0) {
			AppstListItemViews appListItemView = null;
			for (int i = 0; i < count; i++) {
				appListItemView = (AppstListItemViews) this.getChildAt(i).getTag();
				appListItemView.destory();

			}
		}
		if (mListAdapter != null) {
			mListAdapter.cleanup();
			mListAdapter = null;
		}
	}

	/**
	 * 改变所有应用选中状态
	 * 
	 * @param b
	 * @author zhaojunjie
	 */
	public void SetAllAppsSelectState(boolean b) {
		mListAdapter.SetAllAppsSelectState(b);
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

package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.contorler.MyAppsDataManager;
import com.jiubang.ggheart.appgame.appcenter.contorler.MyAppsDataManager.DataLoadCompletedListenter;
import com.jiubang.ggheart.appgame.appcenter.contorler.MyAppsDataManager.SortedAppInfo;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.ContainerBuiler;
import com.jiubang.ggheart.appgame.base.component.IContainer;
import com.jiubang.ggheart.appgame.base.component.IMenuHandler;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.DeskBuilder;
import com.jiubang.ggheart.components.DeskToast;

/**
 * 
 * <br>
 * 类描述: 我的应用container <br>
 * 功能详细描述: 我的应用 模块功能的container
 * 
 * @author zhoujun
 * @date [2012-9-11]
 */
public class MyAppsContainer extends LinearLayout implements IContainer,
		OnClickListener {

	/**
	 * container的分类id
	 */
	private int mTypeId = -1;

	private Context mContext = null;
	private LinearLayout mProgressLinearLayout; // 进度条
	private MyAppsView mPhoneListView = null;

	// private boolean mIsActive = false;
	private MyAppsDataManager mAppDataManager;
	private PreferencesManager mPreferences;

	private TextView mSortByName;
	private TextView mSortByTime;
	private TextView mSortBySize;

	private static final int INSTALL_LOCATION = 0;
	private static final int TIME_SORT = 1;
	private static final int NAME_SORT = 2;
	private static final int SIZE_SORT = 3;

	public MyAppsContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MyAppsContainer(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		mAppDataManager = MyAppsDataManager.getInstance(context);
	}

	private DataLoadCompletedListenter mDataListenter = new DataLoadCompletedListenter() {
		@Override
		public void loadCompleted(SortedAppInfo sortedAppInfo) {
			if (sortedAppInfo != null) {
				showAppListView(sortedAppInfo);
			}
		}
	};

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initView();
	}

	private void initView() {
		mPhoneListView = (MyAppsView) this.findViewById(R.id.phone_list);
		mPhoneListView.setAppListState(MyAppsView.APP_IN_PHONE);
		mPhoneListView.setViewtype(MyAppsView.VIEW_TYPE_APPS);
		mPhoneListView.initView();
		mPhoneListView.setSaveEnabled(false);
		mPhoneListView.setOnScrollListener(mScrollListener);
		mProgressLinearLayout = (LinearLayout) this
				.findViewById(R.id.app_list_progress);

		mSortByName = (TextView) findViewById(R.id.namesort);
		mSortByTime = (TextView) findViewById(R.id.timesort);
		mSortBySize = (TextView) findViewById(R.id.sizesort);

		mSortByName.setOnClickListener(this);
		mSortByTime.setOnClickListener(this);
		mSortBySize.setOnClickListener(this);

		mPreferences = new PreferencesManager(getContext(),
				IPreferencesIds.APPS_ORDER_TYPE, Context.MODE_PRIVATE);

		int orderType = mPreferences.getInt("orderType", NAME_SORT);

		resetTextColor(orderType);

	}

	/**
	 * 刷新列表数据
	 */
	private void updateList() {
		mProgressLinearLayout.setVisibility(View.VISIBLE);
		mPhoneListView.setVisibility(View.GONE);
		SortedAppInfo sortedAppInfo = mAppDataManager.getData(mDataListenter);
		if (sortedAppInfo != null) {
			showAppListView(sortedAppInfo);
		}
	}

	/**
	 * 获取数据后，显示我的应用列表
	 * 
	 * @param sortedAppInfo
	 */
	private void showAppListView(SortedAppInfo sortedAppInfo) {
		if (mProgressLinearLayout != null) {
			mProgressLinearLayout.setVisibility(View.GONE);
		}
		if (mPhoneListView != null) {
			mPhoneListView.setVisibility(View.VISIBLE);
			mPhoneListView.showHeaderView();
			mPhoneListView.setData(sortedAppInfo);
		}
	}

	/**
	 * 按注定类型排序
	 * 
	 * @param sortType
	 *            指定类型
	 */
	public void sortedAppByType(int sortType) {
		if (mAppDataManager != null) {
			mAppDataManager.sortedAppByType(sortType);
			if (mPhoneListView != null) {
				mPhoneListView.setSelection(0);
			}
		}
	}

	@Override
	public void cleanup() {

		if (mContext != null) {
			mContext = null;
		}

		if (mPhoneListView != null) {
			mPhoneListView.cleanup();
			mPhoneListView.setAdapter(null);
			mPhoneListView = null;
		}

		if (mAppDataManager != null) {
			mAppDataManager.removeDataLoadComletedListenter(mDataListenter);
			mDataListenter = null;
			// mAppDataManager.cleanData();
		}

	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// menu.addSubMenu(0, IMenuHandler.MENU_ID_REFRESH, 0,
	// R.string.apps_mgr_menu_item_refresh);
	// return true;
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case IMenuHandler.MENU_ID_REFRESH:
	// if (mPhoneListView != null) {
	// updateList();
	// mPhoneListView.setSelection(0);
	// }
	// return true;
	// }
	// return false;
	// }

	@Override
	public void sdCardTurnOff() {
		if (mAppDataManager != null) {
			mAppDataManager.sdCardTurnOff();
		}
	}

	@Override
	public void sdCardTurnOn() {
		if (mAppDataManager != null) {
			mAppDataManager.sdCardTurnOn();
		}
	}

	@Override
	public void onActiveChange(boolean isActive) {
		// TODO Auto-generated method stub
		if (isActive && mPhoneListView != null) {
			updateList();
			// mPhoneListView.refreshView();
		}
	}

	@Override
	public void onResume() {
		if (mPhoneListView != null) {
			int currSelectPos = mPhoneListView.getmCurrSelectedPos();
			if (currSelectPos != -1) {
				mAppDataManager.moveLocation(currSelectPos);
				mPhoneListView.resetSelectedPos();
			}
		}
	}

	@Override
	public void onAppAction(String packName, int appAction) {
		// TODO Auto-generated method stub
		if (mAppDataManager != null) {
			if (appAction == MainViewGroup.FLAG_INSTALL) {
				// 安装应用后，将新安装的应用添加在应用列表中
				mAppDataManager.installApp(packName);
			} else if (appAction == MainViewGroup.FLAG_UNINSTALL) {
				mAppDataManager.uninstallApp(packName);
			}
		}
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateContent(ClassificationDataBean bean,
			boolean isPrevLoadRefresh) {
		if (bean == null) {
			return;
		}
		mTypeId = bean.typeId;
	}

	@Override
	public void initEntrance(int access) {

	}

	@Override
	public int getTypeId() {
		return mTypeId;
	}

	@Override
	public void onFinishAllUpdateContent() {

	}

	@Override
	public void notifyDownloadState(DownloadTask downlaodTask) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {

	}

	@Override
	public boolean onPrepareOptionsMenu(AppGameMenu menu) {
		// TODO Auto-generated method stub
		int[] resId = new int[] { IMenuHandler.MENU_ITEM_FRESH, IMenuHandler.MENU_ITEM_SETTING,
				IMenuHandler.MENU_ITEM_FEEDBACK };
		menu.setResourceId(resId);
		menu.show(this);
		return true;
	}

	@Override
	public boolean onOptionItemSelected(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case IMenuHandler.MENU_ITEM_FRESH:
			if (mPhoneListView != null) {
				updateList();
				mPhoneListView.setSelection(0);
			}
			break;
		// case IMenuHandler.MENU_ITEM_SORT_BY:
		// showSelectSort();
		// break;
		// case IMenuHandler.MENU_ITEM_BATCH_UNINSTALL:
		// AppsManagementActivity.sendHandler(this,
		// IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
		// IDiyMsgIds.SHOW_UNINSTALL_APP_VIEW, 0, null, null);
		// break;
		default:
			return false;

		}
		return true;
	}

	public void showSelectSort() {
		AlertDialog.Builder builder = new DeskBuilder(getContext());
		final PreferencesManager mPreferences = new PreferencesManager(
				getContext(), IPreferencesIds.APPS_ORDER_TYPE,
				Context.MODE_PRIVATE);
		final int orderType = mPreferences.getInt("orderType", 2);

		builder.setTitle(getContext()
				.getString(R.string.apps_sort_dialog_title));
		builder.setNegativeButton(R.string.cancle,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
					}
				});

		CharSequence[] data = getContext().getResources().getTextArray(
				R.array.apps_sort_dialog_style);
		if (null != data && data.length > 0) {
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
					getContext(), R.layout.desk_select_dialog_singlechoice,
					R.id.radio_textview, data);
			builder.setAdapter(adapter, null);
		}

		builder.setSingleChoiceItems(R.array.select_sort_style, orderType,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						int currType = item;
						mPreferences.putInt("orderType", currType);
						mPreferences.commit();
						if (orderType != currType) {
							sortedAppByType(currType);
						}
					}
				});
		try {
			builder.show();
		} catch (Exception e) {
			try {
				DeskToast.makeText(getContext(), R.string.alerDialog_error,
						Toast.LENGTH_SHORT).show();
			} catch (OutOfMemoryError error) {
				OutOfMemoryHandler.handle();
			}
		}
	}

	@Override
	public void onTrafficSavingModeChange() {
		// do nothing
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.namesort:
			AsyncImageManager.getInstance().restore();
			mPreferences.putInt("orderType", NAME_SORT);
			mPreferences.commit();
			sortedAppByType(NAME_SORT);
			resetTextColor(NAME_SORT);
			break;
		case R.id.timesort:
			AsyncImageManager.getInstance().restore();
			mPreferences.putInt("orderType", TIME_SORT);
			mPreferences.commit();
			sortedAppByType(TIME_SORT);
			resetTextColor(TIME_SORT);

			break;
		case R.id.sizesort:
			AsyncImageManager.getInstance().restore();
			mPreferences.putInt("orderType", SIZE_SORT);
			mPreferences.commit();
			sortedAppByType(SIZE_SORT);
			resetTextColor(SIZE_SORT);
			break;
		default:
			break;
		}
	}

	private static final int COLOR_AGRONE = 255;
	private static final int COLOR_AGRTWO = 0x66;
	private static final int COLOR_AGRTHREE = 0x66;
	private static final int COLOR_AGRFOUR = 0x66;

	private void resetTextColor(int sort) {
		switch (sort) {
		case INSTALL_LOCATION:
		case NAME_SORT:
			mSortByName.setTextColor(Color.WHITE);
			mSortByTime.setTextColor(Color.argb(COLOR_AGRONE, COLOR_AGRTWO,
					COLOR_AGRTHREE, COLOR_AGRFOUR));
			mSortBySize.setTextColor(Color.argb(COLOR_AGRONE, COLOR_AGRTWO,
					COLOR_AGRTHREE, COLOR_AGRFOUR));

			mSortByName
					.setBackgroundResource(R.drawable.app_mgr_tab_left_light);
			mSortByTime.setBackgroundResource(R.drawable.app_mgr_tab);
			mSortBySize.setBackgroundResource(R.drawable.app_mgr_tab_right);

			break;
		case TIME_SORT:
			mSortByTime.setTextColor(Color.WHITE);
			mSortByName.setTextColor(Color.argb(COLOR_AGRONE, COLOR_AGRTWO,
					COLOR_AGRTHREE, COLOR_AGRFOUR));
			mSortBySize.setTextColor(Color.argb(COLOR_AGRONE, COLOR_AGRTWO,
					COLOR_AGRTHREE, COLOR_AGRFOUR));

			mSortByTime.setBackgroundResource(R.drawable.app_mgr_mid_light);
			mSortByName.setBackgroundResource(R.drawable.app_mgr_tab_left);
			mSortBySize.setBackgroundResource(R.drawable.app_mgr_tab_right);
			break;
		case SIZE_SORT:
			mSortBySize.setTextColor(Color.WHITE);
			mSortByTime.setTextColor(Color.argb(COLOR_AGRONE, COLOR_AGRTWO,
					COLOR_AGRTHREE, COLOR_AGRFOUR));
			mSortByName.setTextColor(Color.argb(COLOR_AGRONE, COLOR_AGRTWO,
					COLOR_AGRTHREE, COLOR_AGRFOUR));

			mSortBySize
					.setBackgroundResource(R.drawable.app_mgr_tab_right_light);
			mSortByTime.setBackgroundResource(R.drawable.app_mgr_tab);
			mSortByName.setBackgroundResource(R.drawable.app_mgr_tab_left);
			break;
		default:
			break;
		}
	}

	@Override
	public void setUpdateData(Object value, int state) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * listview滑动监听器
	 */
	private OnScrollListener mScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE : {
					//列表停止滚动时
					//找出列表可见的第一项和最后一项
					int start = view.getFirstVisiblePosition();
					int end = view.getLastVisiblePosition();
					//如果有添加HeaderView，要减去
					ListView lisView = null;
					if (view instanceof ListView) {
						lisView = (ListView) view;
					}
					if (lisView != null) {
						int headViewCount = lisView.getHeaderViewsCount();
						start -= headViewCount;
						end -= headViewCount;
					}
					if (end >= view.getCount()) {
						end = view.getCount() - 1;
					}
					//对图片控制器进行位置限制设置
					AsyncImageManager.getInstance().setLimitPosition(start, end);
					//然后解锁通知加载
					AsyncImageManager.getInstance().unlock();
				}
					break;
				case OnScrollListener.SCROLL_STATE_FLING : {
					//列表在滚动，图片控制器加锁
					AsyncImageManager.getInstance().lock();
				}
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL : {
					//列表在滚动，图片控制器加锁
					AsyncImageManager.getInstance().lock();
				}
					break;
				default :
					break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {
			if (view instanceof PinnedHeaderListView) {
				((PinnedHeaderListView) view)
						.configureHeaderView(firstVisibleItem);
			}
		}
	};
	
	@Override
	public void prevLoading() {
		//do nothing
	}

	@Override
	public void prevLoadFinish() {
		//do nothing
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
	public void setBuilder(ContainerBuiler builder) {
		// do nothing
	}

}

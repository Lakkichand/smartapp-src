package com.jiubang.ggheart.appgame.gostore.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.ContainerBuiler;
import com.jiubang.ggheart.appgame.base.component.IContainer;
import com.jiubang.ggheart.appgame.base.component.IMenuHandler;
import com.jiubang.ggheart.appgame.base.data.ClassificationExceptionRecord;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 九宫格分类Container
 * 
 * @author zhouxuewen
 * 
 */
public class GridSortContainer extends FrameLayout implements IContainer {
	/**
	 * 数据列表对应的分类id
	 */
	private int mTypeId = -1;
	/**
	 * 是否激活状态
	 */
	private boolean mIsActive = false;
	/**
	 * 是否在显示错误提示页面
	 */
	private boolean mShowingError = false;
	/**
	 * 标志位，表示当变成激活状态时需要重新刷界面
	 */
	private boolean mIsNeedToRefreshWhenActive = false;

	private LayoutInflater mInflater;
	/**
	 * 错误提示页
	 */
	private NetworkTipsTool mNetworkTipsTool;
	/**
	 * 数据展示视图
	 */
	private GridView mGridView;
	/**
	 * 数据适配器
	 */
	private GridSortAdapter mAdapter;
	/**
	 * 数据列表
	 */
	private List<CategoriesDataBean> mCategoriesList = new ArrayList<CategoriesDataBean>();
	/**
	 * 列表默认显示图标
	 */
	private Drawable mDefaultIcon = null;
	/**
	 * 数据列表为空时的重试点击监听器，刷新当前tab
	 */
	private OnClickListener mRetryClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			TabController.refreshCurrentTab();
		}
	};
	/**
	 * 反馈按钮监听器
	 */
	private OnClickListener mFeedbackListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ClassificationExceptionRecord.sendFeedbackMail(getContext());
		}
	};

	public GridSortContainer(Context context) {
		super(context);
	}

	public GridSortContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GridSortContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		mInflater = LayoutInflater.from(getContext());
		mDefaultIcon = getResources().getDrawable(R.drawable.appcenter_feature_default_banner);

		ViewGroup tipsView = (ViewGroup) findViewById(R.id.tips_view);
		mNetworkTipsTool = new NetworkTipsTool(tipsView);

		mGridView = (GridView) findViewById(R.id.categories_gridview);

		mAdapter = new GridSortAdapter(this.getContext());
		mAdapter.setDefaultIcon(mDefaultIcon);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mAdapter != null) {
					Object tag = view.getTag(R.id.appgame);
					CategoriesDataBean bean = null;
					if (tag != null && tag instanceof CategoriesDataBean) {
						bean = (CategoriesDataBean) tag;
					}
					if (bean == null) {
						return;
					}
					switch (bean.feature) {
						case CategoriesDataBean.FEATURE_FOR_DEFAULT:
							// 进入下一级tab栏
							TabController.skipToTheNextTab(bean.typeId, bean.name, -1, true, -1, -1, null);
							break;
						case CategoriesDataBean.FEATURE_FOR_YJZWJ :
							break;
						case CategoriesDataBean.FEATURE_FOR_APP_UPDATE:
							break;
						case CategoriesDataBean.FEATURE_FOR_GAME_AND_APP:
							break;
						case CategoriesDataBean.FEATURE_FOR_MANAGEMENT:
							break;
						case CategoriesDataBean.FEATURE_FOR_SEARCH:
							break;
						case CategoriesDataBean.FEATURE_FOR_THEME:
							// 如果点击Go应用&专题，则跳到go精品
//							byte entryId = GoStoreStatisticsUtil.ENTRY_TYPE_APPCENTER;
//							StatisticsData.countStatData(view.getContext(), StatisticsData.ENTRY_KEY_APPCENTER);
//							if (mEntrance == MainViewGroup.GAMEZONE_ENTRANCE) {
//								entryId = GoStoreStatisticsUtil.ENTRY_TYPE_GAMECENTER;
//							}
//							// 保存入口
//							GoStoreStatisticsUtil.setCurrentEntry(entryId, view.getContext());
//							// 跳到go精品
//							Intent intent = new Intent();
//							intent.setClass(view.getContext(), GoStore.class);
//							view.getContext().startActivity(intent);
							break;
						default :
							break;
					}
				}
			}
		});
	}

	@Override
	public void cleanup() {
		if (mCategoriesList != null) {
			mCategoriesList.clear();
		}
	}

	@Override
	public void sdCardTurnOff() {
		// do nothing
	}

	@Override
	public void sdCardTurnOn() {
		// do nothing
	}

	/**
	 * 刷新界面，展示分类数据
	 */
	private void doRefresh() {
		if (!FileUtil.isSDCardAvaiable()) {
			mGridView.setVisibility(View.GONE);
			mNetworkTipsTool.showNothing();
			mNetworkTipsTool.showRetryErrorTip(null, false);
			mShowingError = true;
			return;
		}
		if (mIsActive && mIsNeedToRefreshWhenActive) {
			mIsNeedToRefreshWhenActive = false;
			mNetworkTipsTool.showNothing();
			mAdapter.onActiveChange(true);
			// 刷新数据和界面
			mAdapter.refreshData(mCategoriesList);
			mAdapter.notifyDataSetChanged();
			mGridView.setVisibility(View.VISIBLE);
		}
		mShowingError = false;
	}

	@Override
	public void onActiveChange(boolean isActive) {
		if (isActive == mIsActive) {
			return;
		}
		mIsActive = isActive;
		mAdapter.onActiveChange(isActive);
		if (isActive) {
			if (!mShowingError) {
				doRefresh();
			}
		}
	}

	@Override
	public void onResume() {
		if (mIsActive) {
			if (!mShowingError) {
				doRefresh();
			}
		}
	}

	@Override
	public void onStop() {
		mIsNeedToRefreshWhenActive = true;
	}

	@Override
	public void onAppAction(String packName, int appAction) {
		// do nothing
	}

	@Override
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh) {
		if (bean == null || bean.dataType != ClassificationDataBean.GRID_SORT) {
			Log.e("CategoriesContainer", "updateContent  bean == null|| bean.dataType != ClassificationDataBean.GRID_SORT");
			return;
		}
		if (isPrevLoadRefresh && bean.categoriesList == null) {
			return;
		}
		if (bean.categoriesList == null) {
			// 显示重试页面，点击重试刷新整个tab栏
			mGridView.setVisibility(View.GONE);
			mNetworkTipsTool.showNothing();
			// 判断SD卡是否有网络错误日志，另外网络是否正常，是则显示反馈按钮
			if (FileUtil.isFileExist(LauncherEnv.Path.APP_MANAGER_CLASSIFICATION_EXCEPTION_RECORD_PATH)
					&& Machine.isNetworkOK(getContext())) {
				mNetworkTipsTool.showRetryErrorTip(mRetryClickListener, mFeedbackListener);
			} else {
				mNetworkTipsTool.showRetryErrorTip(mRetryClickListener, true);
			}
			mShowingError = true;
			return;
		}
		mIsNeedToRefreshWhenActive = true;
		// 保存分类id
		mTypeId = bean.typeId;
		// 把数据保存下来
		mCategoriesList.clear();
		// 不要直接用updatecontent传过来的列表，因为这个对象是暴露出去的，有可能在子线程被修改，所以要copy一份保存
		if (bean.categoriesList != null) {
			for (CategoriesDataBean category : bean.categoriesList) {
				mCategoriesList.add(category);
			}
		}
		if (!isPrevLoadRefresh) { // 如果不是预加载后刷新，显示进度条
			// 重置状态
			mIsActive = false;
			mShowingError = false;
			if (mIsNeedToRefreshWhenActive && mGridView.getChildCount() <= 0) {
				// 显示进度条
				mGridView.setVisibility(View.GONE);
				mNetworkTipsTool.showNothing();
				mNetworkTipsTool.showProgress();
			}
		} else if (mIsActive) {
			//如果是预加载拿到更新数据，直接刷新界面
			mShowingError = false;
			doRefresh();
		}
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

	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {

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
			case IMenuHandler.MENU_ITEM_FRESH :
				// 整个tab栏刷新
				TabController.refreshCurrentTab();
				return true;
		}
		return false;
	}

	@Override
	public void onTrafficSavingModeChange() {
		if (!mIsActive) {
			mIsNeedToRefreshWhenActive = true;
			return;
		}
		if (mAdapter != null) {
			mAdapter.onActiveChange(mIsActive);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void setUpdateData(Object value, int state) {
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
		List<IContainer> ret = new ArrayList<IContainer>();
		ret.add(this);
		return ret;
	}

	@Override
	public void onMultiVisiableChange(boolean visiable) {
		if (!visiable) {
			//TODO 回收图片
			mIsNeedToRefreshWhenActive = true;
		}
	}

	@Override
	public void prevLoadFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prevLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBuilder(ContainerBuiler builder) {
		// do nothing
	}

}

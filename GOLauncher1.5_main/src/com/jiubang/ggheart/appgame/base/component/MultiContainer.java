package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.MultiTabBar.TabObserver;
import com.jiubang.ggheart.appgame.base.data.TabDataManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;

/**
 * 
 * 应用中心和GO精品合并而增加的container，用于按钮形式展示分类数据，底下会有N个子container展示具体的应用信息
 * 
 * 该container比较特殊，不需要展示具体应用数据，只负责显示和管理它的子container
 * 
 * 在View结构上只添加当前显示的container，其他两个container会用List缓存着
 * 
 * MultiContainer为激活时，子container中只有当前屏处于激活状态，其他不显示的屏处于非激活状态，只有点击按钮tab栏切换页面时才会更改激活状态
 * 
 * MultiContainer为非激活时，所有container都处于非激活状态
 * 
 * @author  xiedezhi
 * @date  [2012-11-28]
 */
public class MultiContainer extends LinearLayout implements IContainer {
	/**
	 * 当前正在显示的页面下标
	 */
	private int mCurrentIndex = -1;
	/**
	 * 子container列表
	 */
	private List<IContainer> mContainers = new ArrayList<IContainer>();
	/**
	 * 子container布局参数
	 */
	private LinearLayout.LayoutParams mContainerLayoutParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
	/**
	 * 按钮形式tab栏布局参数
	 */
	private LinearLayout.LayoutParams mButtonBarLayoutParams = null;
	/**
	 * 分隔线布局参数
	 */
	private LinearLayout.LayoutParams mLineLayoutParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	/**
	 * 顶部按钮形式tab栏
	 */
	private MultiTabBar mMultiTabBar = null;
	/**
	 * 分隔线
	 */
	private ImageView mMultiLine = null;
	/**
	 * 广告位选中位置
	 */
	private int mADSelection = Integer.MAX_VALUE / 2;
	/**
	 * 广告位选中监听器
	 */
	private OnItemSelectedListener mItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			mADSelection = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};
	/**
	 * 点击按钮tab栏的监听器
	 */
	private TabObserver mTabObserver = new TabObserver() {

		@Override
		public void handleChangeTab(int tabIndex) {
			if (!mIsActive) {
				return;
			}
			mMultiTabBar.setButtonSelected(tabIndex);
			if (mContainers != null && mCurrentIndex != tabIndex && tabIndex >= 0
					&& tabIndex < mContainers.size()) {
				if (mCurrentIndex >= 0 && mCurrentIndex < mContainers.size()) {
					//分类页面切换，列表改变了，要重置图片管理器的列表控制信息
					AsyncImageManager.getInstance().restore();
					IContainer container = mContainers.get(mCurrentIndex);
					//通知页面转成非激活状态
					container.onActiveChange(false);
					MultiContainer.this.removeView((View) container);
					//移除子container后通知该container
					container.onMultiVisiableChange(false);
					mCurrentIndex = tabIndex;
					container = mContainers.get(tabIndex);
					MultiContainer.this.addView((View) container, mContainerLayoutParams);
					// 统计tab点击数
					AppsManagementActivity.sendHandler(getContext(),
							IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
							IDiyMsgIds.SAVE_TAB_CLICK, container.getTypeId(), null, null);
					//通知页面转成激活状态
					container.onActiveChange(true);
					//添加子container后通知该container
					container.onMultiVisiableChange(true);
					if (mDataBean != null && mDataBean.categoriesList != null) {
						for (int i = 0; i < mDataBean.categoriesList.size(); i++) {
							CategoriesDataBean cBean = mDataBean.categoriesList.get(i);
							if (cBean != null) {
								if (i == tabIndex) {
									cBean.isHome = 1;
								} else {
									cBean.isHome = 0;
								}
							}
						}
					}
				}
			}
			if (mADBanner != null && mADBanner.getVisibility() == View.VISIBLE
					&& mADBanner.getParent() != null) {
				mADBanner.showNext();
			}
		}
	};
	/**
	 * 该container的数据
	 */
	private ClassificationDataBean mDataBean = null;
	/**
	 * 数据列表对应的分类id
	 */
	private int mTypeId = -1;

	private boolean mIsActive = false;
	/**
	 * 广告推荐位
	 */
	private AppGameADBanner mADBanner;
	/**
	 * 广告推荐位adapter
	 */
	private AppGameADAdapter mAdapter;

	public MultiContainer(Context context) {
		super(context);
		init();
	}

	public MultiContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		this.setOrientation(LinearLayout.VERTICAL);
		mButtonBarLayoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, DrawUtils.dip2px(48));
		mButtonBarLayoutParams.leftMargin = mButtonBarLayoutParams.rightMargin = 0;
		mMultiTabBar = new MultiTabBar(getContext(), mTabObserver);
		mMultiTabBar.setPadding(DrawUtils.dip2px(20), 0, DrawUtils.dip2px(20), 0);
		mMultiLine = new ImageView(getContext());
		mMultiLine.setScaleType(ScaleType.FIT_XY);
		mMultiLine.setImageResource(R.drawable.themestore_list_item_line);
	}

	@Override
	public void cleanup() {
		if (mContainers != null && mContainers.size() > 0) {
			for (IContainer container : mContainers) {
				container.cleanup();
			}
			mContainers.clear();
		}
	}

	@Override
	public void sdCardTurnOff() {
		//只把消息发给当前显示的container，因为TabManageView会把消息发送给所有的IdleContainer
		if (mContainers != null && mCurrentIndex >= 0 && mCurrentIndex < mContainers.size()) {
			IContainer container = mContainers.get(mCurrentIndex);
			container.sdCardTurnOff();
		}
	}

	@Override
	public void sdCardTurnOn() {
		//只把消息发给当前显示的container，因为TabManageView会把消息发送给所有的IdleContainer
		if (mContainers != null && mCurrentIndex >= 0 && mCurrentIndex < mContainers.size()) {
			IContainer container = mContainers.get(mCurrentIndex);
			container.sdCardTurnOn();
		}
	}

	@Override
	public void onActiveChange(boolean isActive) {
		mIsActive = isActive;
		if (isActive) {
			//把消息分发给当前页面
			if (mContainers != null && mCurrentIndex >= 0 && mCurrentIndex < mContainers.size()) {
				IContainer container = mContainers.get(mCurrentIndex);
				container.onActiveChange(isActive);
			}
		} else {
			//把消息分发给子container
			if (mContainers != null && mContainers.size() > 0) {
				for (IContainer container : mContainers) {
					container.onActiveChange(isActive);
				}
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(AppGameMenu menu) {
		//把消息分发给当前页面
		if (mContainers != null && mCurrentIndex >= 0 && mCurrentIndex < mContainers.size()) {
			IContainer container = mContainers.get(mCurrentIndex);
			return container.onPrepareOptionsMenu(menu);
		}
		return false;
	}

	@Override
	public boolean onOptionItemSelected(int id) {
		//把消息分发给当前页面
		if (mContainers != null && mCurrentIndex >= 0 && mCurrentIndex < mContainers.size()) {
			IContainer container = mContainers.get(mCurrentIndex);
			return container.onOptionItemSelected(id);
		}
		return false;
	}

	@Override
	public void onResume() {
		//把消息分发给子container
		if (mContainers != null && mContainers.size() > 0) {
			for (IContainer container : mContainers) {
				container.onResume();
			}
		}
	}

	@Override
	public void onStop() {
		//把消息分发给子container
		if (mContainers != null && mContainers.size() > 0) {
			for (IContainer container : mContainers) {
				container.onStop();
			}
		}
	}

	@Override
	public void onAppAction(String packName, int appAction) {
		//只把消息发给当前显示的container，因为TabManageView会把消息发送给所有的IdleContainer
		if (mContainers != null && mCurrentIndex >= 0 && mCurrentIndex < mContainers.size()) {
			IContainer container = mContainers.get(mCurrentIndex);
			container.onAppAction(packName, appAction);
		}
	}

	@Override
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh) {
		if (bean != null) {
			mTypeId = bean.typeId;
			mDataBean = bean;
		}
		//不需要检查分类列表是否为空，由TabManageView负责检查并负责生成子container给MultiContainer填充
	}

	@Override
	public void initEntrance(int access) {
		//把消息分发给子container
		if (mContainers != null && mContainers.size() > 0) {
			for (IContainer container : mContainers) {
				container.initEntrance(access);
			}
		}
	}

	@Override
	public int getTypeId() {
		return mTypeId;
	}

	@Override
	public void onFinishAllUpdateContent() {
		//do nothing
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		//只把消息发给当前显示的container，因为TabManageView会把消息发送给所有的IdleContainer
		if (mContainers != null && mCurrentIndex >= 0 && mCurrentIndex < mContainers.size()) {
			IContainer container = mContainers.get(mCurrentIndex);
			container.notifyDownloadState(downloadTask);
		}
	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {
		//把消息分发给子container
		if (mContainers != null && mContainers.size() > 0) {
			for (IContainer container : mContainers) {
				container.setDownloadTaskList(taskList);
			}
		}
	}

	@Override
	public void onTrafficSavingModeChange() {
		//把消息分发给子container
		if (mContainers != null && mContainers.size() > 0) {
			for (IContainer container : mContainers) {
				container.onTrafficSavingModeChange();
			}
		}
	}

	@Override
	public void setUpdateData(Object value, int state) {
		//把消息分发给子container
		if (mContainers != null && mContainers.size() > 0) {
			for (IContainer container : mContainers) {
				container.setUpdateData(value, state);
			}
		}
	}

	@Override
	public void fillupMultiContainer(List<CategoriesDataBean> cBeans, List<IContainer> containers) {
		if (cBeans == null || cBeans.size() <= 0 || containers == null || containers.size() <= 0
				|| cBeans.size() != containers.size()) {
			return;
		}
		this.addView(mMultiTabBar, mButtonBarLayoutParams);
		this.addView(mMultiLine, mLineLayoutParams);
		
		// 初始化广告推荐位
		boolean hasADBanner = false;
		ClassificationDataBean adBean = null;
		if (mDataBean != null && mDataBean.categoriesList != null
				&& mDataBean.categoriesList.size() > 0) {
			for (CategoriesDataBean category : mDataBean.categoriesList) {
				if (category == null) {
					continue;
				}
				ClassificationDataBean tbean = TabDataManager.getInstance().getTabData(category.typeId);
				if (tbean != null && tbean.dataType == ClassificationDataBean.AD_BANNER
						&& tbean.featureList != null && tbean.featureList.size() > 0) {
					hasADBanner = true;
					adBean = tbean;
				}
			}
		}
		if (hasADBanner) {
			if (mAdapter == null) {
				mAdapter = new AppGameADAdapter(getContext());
			}
			if (mADBanner == null) {
				mADBanner = new AppGameADBanner(getContext());
				mADBanner.setOnItemSelectedListener(mItemSelectedListener);
			}
			mAdapter.update(adBean.featureList);
			mADBanner.setAdapter(mAdapter);
			mADBanner.setVisibility(View.VISIBLE);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			this.addView(mADBanner, lp);
			
			mADBanner.setSelection(mADSelection);
		}
		
		List<String> titles = new ArrayList<String>();
		for (CategoriesDataBean bean : cBeans) {
			titles.add(bean.name);
		}
		mMultiTabBar.initTabsBar(titles);
		for (IContainer container : containers) {
			mContainers.add(container);
		}
		int home = 0;
		for (int i = 0; i < cBeans.size(); i++) {
			CategoriesDataBean bean = cBeans.get(i);
			if (bean.isHome == 1) {
				home = i;
				break;
			}
		}
		mMultiTabBar.setButtonSelected(home);
		mCurrentIndex = home;
		this.addView((View) containers.get(home), mContainerLayoutParams);
		// 统计tab点击数
		AppsManagementActivity.sendHandler(getContext(),
				IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME, IDiyMsgIds.SAVE_TAB_CLICK, containers
						.get(home).getTypeId(), null, null);
	}

	@Override
	public void removeContainers() {
		this.removeAllViews();
		mMultiTabBar.removeAllViews();
		mContainers.clear();
		mCurrentIndex = -1;
	}

	@Override
	public List<IContainer> getSubContainers() {
		return mContainers;
	}

	@Override
	public void onMultiVisiableChange(boolean visiable) {
		//do nothing
	}

	@Override
	public void prevLoading() {
		//把消息分发给子container
		if (mContainers != null && mContainers.size() > 0) {
			for (IContainer container : mContainers) {
				container.prevLoading();
			}
		}
	}

	@Override
	public void prevLoadFinish() {
		//把消息分发给子container
		if (mContainers != null && mContainers.size() > 0) {
			for (IContainer container : mContainers) {
				container.prevLoadFinish();
			}
		}
	}
	
	@Override
	public void setBuilder(ContainerBuiler builder) {
		// do nothing
	}

}

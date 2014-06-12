package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.data.TabDataManager;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;

/**
 * 一个空的container，没用具体的显示类型
 * 
 * 进入应用中心时，只读取解释展示首屏数据，其他屏暂时用EmptyContainer代替，等滑到该屏时再读取该屏的本地数据，生成真正的container
 * 
 * @author xiedezhi
 * 
 */
public class EmptyContainer extends FrameLayout implements IContainer, IModeChangeListener {

	/**
	 * 分类id
	 */
	private int mTypeId;

	/**
	 * 逻辑控制器，用于读取数据 
	 */
	private EmptyController mController;

	/**
	 * 是否已经被激活
	 */
	private boolean mHasActive = false;

	/**
	 * 生成真正container的工厂
	 */
	private ContainerBuiler mBuilder;

	/**
	 * 真正的container
	 */
	private IContainer mRealContainer;

	/**
	 * 提示页
	 */
	private NetworkTipsTool mNetworkTipsTool;

	/**
	 * 入口
	 */
	private int mEntrance;

	/**
	 * 下载任务列表
	 */
	private List<DownloadTask> mDownloadTaskList;

	/**
	 * 更新数据
	 */
	private Object mUpdateData;

	/**
	 * 获取更新数据的状态
	 */
	private int mUpdateState;

	/**
	 * 后台是否正在加载新数据
	 */
	private boolean mIsPrevLoading = false;

	/**
	 * 是否在激活状态
	 */
	private boolean mIsActive = false;

	public EmptyContainer(Context context) {
		super(context);
		init();
	}

	public EmptyContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public EmptyContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		LinearLayout tipView = new LinearLayout(getContext());
		this.addView(tipView, 0, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT));
		mNetworkTipsTool = new NetworkTipsTool(tipView);
		mController = new EmptyController(getContext(), this);
	}

	@Override
	public void cleanup() {
		if (mRealContainer != null) {
			mRealContainer.cleanup();
		}
	}

	@Override
	public void sdCardTurnOff() {
		if (mRealContainer != null) {
			mRealContainer.sdCardTurnOff();
		}
	}

	@Override
	public void sdCardTurnOn() {
		if (mRealContainer != null) {
			mRealContainer.sdCardTurnOn();
		}
	}

	@Override
	public void onActiveChange(boolean isActive) {
		mIsActive = isActive;
		// 如果第一次激活，则读取数据并生成正真的container
		if (!mHasActive) {
			mController.sendAsyncRequest(EmptyController.ACTION_LOAD_DATA, mTypeId);
			mHasActive = true;
		} else {
			if (mRealContainer != null) {
				mRealContainer.onActiveChange(isActive);
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(AppGameMenu menu) {
		if (mRealContainer != null) {
			return mRealContainer.onPrepareOptionsMenu(menu);
		}
		return false;
	}

	@Override
	public boolean onOptionItemSelected(int id) {
		if (mRealContainer != null) {
			return mRealContainer.onOptionItemSelected(id);
		}
		return false;
	}

	@Override
	public void onResume() {
		if (mRealContainer != null) {
			mRealContainer.onResume();
		}
	}

	@Override
	public void onStop() {
		if (mRealContainer != null) {
			mRealContainer.onStop();
		}
	}

	@Override
	public void onAppAction(String packName, int appAction) {
		if (mRealContainer != null) {
			mRealContainer.onAppAction(packName, appAction);
		}
	}

	@Override
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh) {
		if (bean != null && bean.dataType == ClassificationDataBean.EMPTY_TYPE) {
			if (mNetworkTipsTool != null) {
				mNetworkTipsTool.showNothing();
				mNetworkTipsTool.showProgress();
			}
			mTypeId = bean.typeId;
		} else {
			Log.e("EmptyContainer",
					"bean == null || bean.dataType != ClassificationDataBean.EMPTY_TYPE");
		}
	}

	@Override
	public void initEntrance(int access) {
		mEntrance = access;
		if (mRealContainer != null) {
			mRealContainer.initEntrance(access);
		}
		if (mController != null) {
			mController.setEntrance(access);
		}
	}

	@Override
	public int getTypeId() {
		// 因为这个container不是真正的列表页，所以返回一个没意义的id
		return Integer.MIN_VALUE;
	}

	@Override
	public void onFinishAllUpdateContent() {
		if (mRealContainer != null) {
			mRealContainer.onFinishAllUpdateContent();
		}
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		if (mRealContainer != null) {
			mRealContainer.notifyDownloadState(downloadTask);
		}
	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {
		mDownloadTaskList = taskList;
		if (mRealContainer != null) {
			mRealContainer.setDownloadTaskList(taskList);
		}
	}

	@Override
	public void onTrafficSavingModeChange() {
		if (mRealContainer != null) {
			mRealContainer.onTrafficSavingModeChange();
		}
	}

	@Override
	public void setUpdateData(Object value, int state) {
		mUpdateData = value;
		mUpdateState = state;
		if (mRealContainer != null) {
			mRealContainer.setUpdateData(value, state);
		}
	}

	@Override
	public void fillupMultiContainer(List<CategoriesDataBean> cBeans, List<IContainer> containers) {
		// do nothing
	}

	@Override
	public void removeContainers() {
		if (mRealContainer != null) {
			mRealContainer.removeContainers();
		}
		this.removeAllViews();
	}

	@Override
	public List<IContainer> getSubContainers() {
		if (mRealContainer != null) {
			return mRealContainer.getSubContainers();
		}
		return null;
	}

	@Override
	public void onMultiVisiableChange(boolean visiable) {
		// do nothing
	}

	@Override
	public void prevLoading() {
		mIsPrevLoading = true;
		// 把消息传递到真正的container
		if (mRealContainer != null) {
			mRealContainer.prevLoading();
		}
	}

	@Override
	public void prevLoadFinish() {
		mIsPrevLoading = false;
		// 把消息传递到真正的container
		if (mRealContainer != null) {
			mRealContainer.prevLoadFinish();
		}
	}

	/**
	 * 初始化container信息
	 * @param container
	 */
	private void initCotainerInfo(IContainer container, ClassificationDataBean bean) {
		if (container == null) {
			return;
		}
		// 初始化container入口值
		container.initEntrance(mEntrance);
		// 把当前正的downloadtask设到每个container里
		container.setDownloadTaskList(mDownloadTaskList);
		// container填充数据，填充数据的时候只是把数据缓存起来，到转换成激活状态时才刷新界面
		container.updateContent(bean, false);
		//为每一个container设置可更新应用数据
		container.setUpdateData(mUpdateData, mUpdateState);
		if (mIsPrevLoading) {
			container.prevLoading();
		} else {
			container.prevLoadFinish();
		}
		// 设置ContainerBuilder，暂时只用在EmptyContainer
		container.setBuilder(mBuilder);
	}

	@Override
	public void onModleChanged(int action, int state, final Object value) {
		if (mBuilder == null || value == null || (!(value instanceof ClassificationDataBean))) {
			return;
		}
		post(new Runnable() {

			@Override
			public void run() {
				// 如果container已经被移除，则不需要处理返回的数据
				if (EmptyContainer.this.getParent() == null) {
					Log.e("EmptyContainer", "this.getParent() == null");
					return;
				}
				ClassificationDataBean bean = (ClassificationDataBean) value;
				mRealContainer = mBuilder.getContainer(bean);
				if (mRealContainer == null || (!(mRealContainer instanceof View))) {
					return;
				}
				//按钮tab栏展示的双层container
				if (bean.dataType == ClassificationDataBean.BUTTON_TAB
						|| bean.dataType == ClassificationDataBean.TAB_TYPE) {
					//清空container
					mRealContainer.removeContainers();
					//子container分类信息
					List<CategoriesDataBean> subCBeans = new ArrayList<CategoriesDataBean>();
					//子container列表
					List<IContainer> subIContainers = new ArrayList<IContainer>();
					if (bean.categoriesList != null) {
						for (CategoriesDataBean cbean : bean.categoriesList) {
							int typeId = cbean.typeId;
							ClassificationDataBean subBean = TabDataManager.getInstance()
									.getTabData(typeId);
							if (subBean != null) {
								IContainer subContainer = mBuilder.getContainer(subBean);
								if (subContainer != null) {
									//初始化信息
									initCotainerInfo(subContainer, subBean);
									subCBeans.add(cbean);
									subIContainers.add(subContainer);
								}
							} else {
								Log.e("TabManageView", "MultiContainer subBean == null");
							}
						}
					}
					initCotainerInfo(mRealContainer, bean);
					//填充container
					mRealContainer.fillupMultiContainer(subCBeans, subIContainers);
				} else {
					//普通应用列表container
					initCotainerInfo(mRealContainer, bean);
				}
				// 添加container
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
				EmptyContainer.this.addView((View) mRealContainer, lp);
				if (mNetworkTipsTool != null) {
					mNetworkTipsTool.showNothing();
				}
				// 设置激活状态
				mRealContainer.onActiveChange(mIsActive);
			}
		});
	}

	@Override
	public void setBuilder(ContainerBuiler builder) {
		mBuilder = builder;
	}

}

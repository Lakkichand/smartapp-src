package com.jiubang.ggheart.apps.desks.appfunc;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.AppUtils;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XAnimator;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XLinearLayout;
import com.jiubang.core.mars.XMotion;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.common.component.AppFuncTopSwitchContainer;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncFolderQuickAddBar;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncHomeComponent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncTabBasicContent;
import com.jiubang.ggheart.apps.appfunc.component.ProManageEditDock;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncTabTitle.OnTabTitleSelectionChanged;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager.AnimationInfo;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.data.statistics.StatisticsAppsInfoData;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:该类用于放置tab栏组件mTabTitles，以及用于显示不同tab项对应内容的容器组件mCurrentView
 * <br>功能详细描述:
 * 
 * @author  
 * @date  [2012-9-4]
 */
public class AppFuncTabComponent extends XPanel implements OnTabTitleSelectionChanged {

	private Activity mActivity;

	private XLinearLayout mLinearLayout;

	/**
	 * tab title view
	 */
	private AppFuncTabTitle mTabTitles;

	/**
	 * tab当前内容的容器
	 */
	private XPanel mTabContent;

	/**
	 * 管理tab title 和 tab contant
	 */
	private List<TabMap> mTabMaps = new ArrayList<TabMap>();

	/**
	 * 管理tab 的索引
	 */
	protected int mCurrentTab = -1;

	/**
	 * 当前的tab content view
	 */
	private XComponent mCurrentView;

	/**
	 * 竖屏时Tab栏的高度，以Hight Density为标准
	 */
	public static final int TAB_HEIGHT_V_DIMENS_ID = R.dimen.appfunc_tabheight_v;
	/**
	 * 横屏时Tab栏的宽度，以Hight Density为标准
	 */
	public static final int TAB_WIDTH_H_DIMENS_ID = R.dimen.appfunc_tabheight_h;
	/**
	 * tab title 的长度
	 */
	private int mTabWidth;
	/**
	 * tab title 的高度
	 */
	private int mTabHeight;

	private AppFuncUtils mUtils;

	private OnTabContentChangeListener mOnTabChangeListener;

	private OnSameTabClickListener mOnSameTabClickListener;

	private AppFuncThemeController mThemeCtrl;

	private int mStatuBarHeight = 0;
	/**
	 * 是否显示选项卡
	 */
	private boolean mShowTabRow = false;

	private FunAppSetting mFunAppSetting;

	private AppFuncTopSwitchContainer mTopBarContainer = null;
	/**
	 * 正在运行操作栏
	 */
	private ProManageEditDock mProManageEdit = null;

	/**
	 * 文件夹快速添加栏
	 */
	private AppFuncFolderQuickAddBar mFolderQuickBar = null;

	public AppFuncTabComponent(Activity activity, int tickCount, int x, int y, int width, int height) {
		super(tickCount, x, y, width, height);

		mLinearLayout = new XLinearLayout();
		setLayout(mLinearLayout);

		mActivity = activity;

		mTopBarContainer = new AppFuncTopSwitchContainer(mActivity, tickCount, 0, 0, 0, 0);

		mTabTitles = new AppFuncTabTitle(activity, tickCount, 0, 0, 0, 0);
		mTabTitles.setTabSelectionListener(this);
		mTopBarContainer.setNormalBar(mTabTitles);

		mTabContent = new XPanel(tickCount, 0, 0, 0, 0);

		mProManageEdit = new ProManageEditDock(activity, tickCount, 0, 0, 0, 0);

		mFolderQuickBar = new AppFuncFolderQuickAddBar(mActivity, tickCount, x, y, width, height);
		mTopBarContainer.setEditBar(mFolderQuickBar);

		addComponent(mTopBarContainer);
		addComponent(mTabContent);

		mUtils = AppFuncUtils.getInstance(mActivity);
		mThemeCtrl = AppFuncFrame.getThemeController();
		mFunAppSetting = GOLauncherApp.getSettingControler().getFunAppSetting();

		initResource();
	}

	/**
	 * <br>功能简述: 往tab栏中增加一项tab项
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param tabTitle tab项ui组件
	 * @param content tab项对应的内容组件
	 * @param tagId tabid
	 */
	public void addTab(XComponent tabTitle, XComponent content, int tagId) {

		if (tabTitle == null) {
			throw new IllegalArgumentException(
					"you must specify a way to create the tab indicator.");
		}

		if (content == null) {
			throw new IllegalArgumentException("you must specify a way to create the tab content");
		}

		if (isExistTabId(tagId)) {
			throw new IllegalArgumentException("you must specify a unique tab id ");
		}

		TabMap tabMap = new TabMap(tagId);
		tabMap.setIndicator(tabTitle);
		tabMap.setContent(content);

		mTabTitles.addTabTitle(tabTitle);
		mTabMaps.add(tabMap);

		if (mCurrentTab == -1) {
			setCurrentTab(0);
		}
	}

	/**
	 * 替换tabId对应的tab下的内容组件
	 * 
	 * @param tabId
	 * @param content
	 *            要替换的内容组件
	 */
	public void replaceContentByTabId(int tabId, XComponent content) {
		if (content == null) {
			return;
		}
		for (TabMap tabMap : mTabMaps) {
			if (tabMap != null && tabMap.mTagId == tabId) {
				tabMap.setContent(content);
				if (mUtils.isVertical()) {

					if (getFirstTab().mId != AppFuncConstants.ALLAPPS) {
						changeComponentLayout();
					}
					if (mCurrentTab == tabId) {
						mTabContent.removeComponent(mCurrentView);
						mCurrentView = content;
						mTabContent.addComponent(mCurrentView);
					}

				} else {

					if (getFirstTab().mId != AppFuncConstants.PROCESSMANAGEMENT) {
						changeComponentLayout();
					}
					if (0 == tabId) {
						mTabContent.removeComponent(mCurrentView);
						mCurrentView = content;
						mTabContent.addComponent(mCurrentView);
					}

				}

			}
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mTabContent != null) {
			mTabContent.paintCurrentFrame(canvas, mTabContent.mX, mTabContent.mY);
		}

		if (mTopBarContainer != null && mShowTabRow) {
			mTopBarContainer.paintCurrentFrame(canvas, mTopBarContainer.mX, mTopBarContainer.mY);
		}
	}

	/**
	 * <br>功能简述:设置当前显示tab项内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param index 要显示的tab项的索引，0表示第一个tab项，依次类推
	 */
	public void setCurrentTab(int index) {
		if (mTabMaps == null || index < 0 || index >= mTabMaps.size()) {
			return;
		}

		if (index == mCurrentTab) {
			return;
		}

		// 通知之前的content，需要躲避起来，新的content闪亮登场
		if (mCurrentTab != -1) {
			mTabContent.removeComponent(mCurrentView);
			getCurrentTabTitleView().setSelected(false);

		}

		mCurrentTab = index;
		final AppFuncTabComponent.TabMap spec = mTabMaps.get(index);

		if (AppFuncConstants.PROCESSMANAGEMENT == spec.getTagId()) {
			// 如果是管理程序tab，注册监听器
			if (!AppFuncFrame.getDataHandler().mHasInitTaskMgrHandler) {
				AppFuncFrame.getDataHandler().initTaskMgrHandler();
				AppFuncFrame.getDataHandler().mHasInitTaskMgrHandler = true;
			}
		} else if (AppFuncConstants.RECENTAPPS == spec.getTagId()) {
			if (!AppFuncFrame.getDataHandler().mHasinitRecentHandler) {
				AppFuncFrame.getDataHandler().initRecentHandler();
				AppFuncFrame.getDataHandler().mHasinitRecentHandler = true;
			}
		}
		// 将焦点移动到当前的 tab title 上
		mTabTitles.focusCurrentTab(mCurrentTab);

		// tab content
		mCurrentView = spec.mContentView;

		if (mCurrentView != null) {
			mTabContent.addComponent(mCurrentView);
			getCurrentTabTitleView().setSelected(true);
			mTabTitles.cleanFocus();
		}
		int tagId = getCurrentTabTagId();
		changeEditBarByTagId(tagId);
		// 如果有需要发送当前转换tab的事件
		invokeOnTabChangeListener(tagId);
	}

	private void changeEditBarByTagId(int tagId) {

		switch (tagId) {
			case AppFuncMainView.TABID_ALL :
				mTopBarContainer.setEditBar(mFolderQuickBar);
				break;
			case AppFuncMainView.TABID_RECENT :
				mTopBarContainer.setEditBar(null);
				break;
			case AppFuncMainView.TABID_RUNNING :
				mTopBarContainer.setEditBar(mProManageEdit);
				break;
			default :
				break;
		}
	}

	/**
	 * <br>功能简述:设置tab栏切换事件发生时的事件监听器
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param l
	 */
	public void setOnTabChangedListener(OnTabContentChangeListener l) {
		mOnTabChangeListener = l;
	}

	private void invokeOnTabChangeListener(int tagId) {
		if (mOnTabChangeListener != null) {
			mOnTabChangeListener.onTabChanged(tagId);
		}
	}
	/**
	 * <br>功能简述:设置当前tab项被点击事件发生时的事件监听器
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param listener
	 */
	public void setOnSameTabClickListener(OnSameTabClickListener listener) {
		mOnSameTabClickListener = listener;
	}

	private void invokeOnSameTabClickListener() {
		if (mOnSameTabClickListener != null) {
			mOnSameTabClickListener.onSameTabClick(getCurrentTabTagId());
		}
	}

	/**
	 * <br>功能简述:获取当前tab项的id
	 * <br>功能详细描述:
	 * <br>注意:是tab项的id，而不是他在tab栏中的索引
	 * @return
	 */
	public int getCurrentTabTagId() {
		if (mCurrentTab >= 0 && mCurrentTab < mTabMaps.size()) {
			return mTabMaps.get(mCurrentTab).getTagId();
		}
		return -1;
	}

	/**
	 * <br>功能简述:返回当前tab在tab栏中的索引
	 * <br>功能详细描述:
	 * <br>注意:是tab项在tab栏中的索引，而不是它的id
	 * @return
	 */
	public int getCurrentIndex() {
		return mCurrentTab;
	}

	/**
	 * <br>功能简述:获取当前tab项ui组件引用
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public XComponent getCurrentTabTitleView() {
		if (mCurrentTab >= 0 && mCurrentTab < mTabMaps.size()) {
			return mTabTitles.getChildAt(mCurrentTab);
		}
		return null;
	}

	/**
	 * <br>功能简述:获取当前tab项对应的内容组件引用
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public XComponent getCurrentContentView() {
		return mCurrentView;
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		setPosition(left, top, right, bottom);
		resizeComponent();
		if (mLinearLayout != null) {
			mLinearLayout.layout(this);
		}
		mStatuBarHeight = GoLauncher.getStatusbarHeight();
		GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
				IDiyMsgIds.RESPONSE_GLIDE_UP_DOWN_ACTION, 1, null, null);
	}

	public void resizeComponent() {

		int contentWidth;
		int contentHeigth;
		byte tabOrientation;

		boolean isVertical = AppFuncUtils.getInstance(mActivity).isVertical();
		FuncAppDataHandler handler = AppFuncFrame.getDataHandler();
		// 使用Waterfall特效时自动隐藏Tab栏
		mShowTabRow = handler.isShowTabRow();
		if (isVertical) {
			contentWidth = mTabWidth = mWidth;
			mTabHeight = mShowTabRow ? mUtils.getDimensionPixelSize(TAB_HEIGHT_V_DIMENS_ID) : 0;
			contentHeigth = mHeight - mTabHeight;
			tabOrientation = XLinearLayout.HORIZONTAL;

			mLinearLayout.setOrientation(XLinearLayout.VERTICAL);
		} else {
			mTabWidth = mShowTabRow ? mUtils.getDimensionPixelSize(TAB_WIDTH_H_DIMENS_ID) : 0;
			contentWidth = mWidth - mTabWidth;
			contentHeigth = mTabHeight = mHeight;
			tabOrientation = XLinearLayout.VERTICAL;
			mLinearLayout.setOrientation(XLinearLayout.HORIZONTAL);
		}
		mTabTitles.setMotionFilter(null);
		mTabTitles.setVisible(mShowTabRow);
		mTabTitles.setSize(mTabWidth, mTabHeight);
		mTabTitles.setOrientation(tabOrientation);

		mTopBarContainer.setVisible(mShowTabRow);
		mTopBarContainer.setSize(mTabWidth, mTabHeight);
		mTabContent.setSize(contentWidth, contentHeigth);

		int count = mTabMaps.size();
		for (int i = 0; i < count; i++) {
			TabMap tabMap = mTabMaps.get(i);
			if (isVertical) {
				tabMap.mIndicatorView.setSize(0, mTabTitles.getHeight());
			} else {
				tabMap.mIndicatorView.setSize(mTabTitles.getWidth(), 0);
			}

			tabMap.mContentView.setSize(contentWidth, contentHeigth);
		}
	}

	/**
	 * <br>功能简述:设置tab栏中对应index索引的tab项为焦点
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param index
	 * @param isFocus
	 */
	public void setTabFocus(int index, boolean isFocus) {
		mTabTitles.setFocusIndex(index, isFocus);
	}

	/**
	 * <br>功能简述:切换tab内容到tabIndex对应的tab项的内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param tabIndex
	 */
	public void switchTab(int tabIndex) {
		if (tabIndex == mCurrentTab) {
			return;
		}

		// just for test
		// if (tabIndex == 1)
		// {
		// HttpAdapter httpAdapter = AppCore.getInstance().getHttpAdapter();
		// AppCore.getInstance().getAppsListUpdateManager().startCheckUpdate(httpAdapter,
		// getUpdateConnectListener(), false);
		// }
		// just for test end

		setCurrentTab(tabIndex);
		TabMap tabMap = mTabMaps.get(tabIndex);
		tabMap.mContentView.layout(0, 0, mTabContent.getWidth(), mTabContent.getHeight());
	}

	/**
	 * 更新连接监听
	 * 
	 * @return
	 */
	private IConnectListener getUpdateConnectListener() {
		final IConnectListener receiver = new IConnectListener() {

			@Override
			public void onStart(THttpRequest arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinish(THttpRequest arg0, IResponse arg1) {
				// TODO Auto-generated method stub
				// 1、清理统计数据
				StatisticsAppsInfoData.resetStatisticsAllDataInfos(mContext);

				// 2、可以更新的数据bean
				if (arg1 != null) {
					ArrayList<BaseBean> listBeans = (ArrayList<BaseBean>) arg1.getResponse();
					if (listBeans != null && listBeans.size() > 0) {
						AppsBean appsBean = (AppsBean) listBeans.get(0);
						if (appsBean != null && appsBean.mListBeans != null
								&& appsBean.mListBeans.size() > 0) {
							GoLauncher.sendHandler(null, IDiyFrameIds.APPFUNC_FRAME,
									IDiyMsgIds.EVENT_APPS_LIST_UPDATE, 0, appsBean, null);
						}
					}

				}

			}

			@Override
			public void onException(THttpRequest arg0, int arg1) {
				// TODO Auto-generated method stub
				StatisticsData.saveHttpExceptionDate(mContext, arg0, arg1);
			}
		};

		return receiver;
	}

	/**
	 * <br>功能简述:获取索引为0的tab项的ui组件引用
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public XComponent getFirstTab() {
		return mTabTitles.getFirstComponent();
	}

	// 改变组件的位置以及索引
	public void changeComponentLayout() {
		int indexEnd = mTabMaps.size() - 1;
		if (mCurrentTab == 0) {
			mCurrentTab = mTabMaps.size() - 1;
		} else if (mCurrentTab == indexEnd) {
			mCurrentTab = 0;
		}

		TabMap tabMap1 = mTabMaps.get(0);
		TabMap tabMap2 = mTabMaps.get(indexEnd);
		mTabMaps.set(0, tabMap2);
		mTabMaps.set(indexEnd, tabMap1);

		mTabTitles.changeComponent();
	}

	@Override
	public void onTabSelectionChanged(XComponent touchComponent) {

		int count = mTabMaps.size();
		TabMap tabMap;
		for (int i = 0; i < count; i++) {
			tabMap = mTabMaps.get(i);
			if (tabMap.mIndicatorView == touchComponent) {
				if (i == mCurrentTab) {
					invokeOnSameTabClickListener(); // 发送点击相同tab的事件
					return;
				}
				switchTab(i);
			}
		}
	}

	/**
	 * tab title 切换的事件监听器的接口
	 */
	public interface OnTabContentChangeListener {
		void onTabChanged(int tabId);
	}

	/**
	 * tab title 点击相同tab的监听器接口
	 */
	public interface OnSameTabClickListener {
		void onSameTabClick(int tabId);
	}

	/**
	 * 检查加入到Tab Id是否已存在 return
	 */
	private boolean isExistTabId(int checkTabId) {
		int count = mTabMaps.size();
		TabMap tabMap;
		for (int i = 0; i < count; i++) {
			tabMap = mTabMaps.get(i);
			if (tabMap.getTagId() == checkTabId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取tab的个数 return
	 */
	public int getTabCount() {
		if (mTabTitles != null) {
			return mTabTitles.getTabCount();
		}
		return 0;
	}

	/**
	 * 管理tab title 与 content的容器
	 */
	public class TabMap {
		private int mTagId;

		XComponent mIndicatorView;
		XComponent mContentView;

		private TabMap(int tag) {
			mTagId = tag;
		}

		public TabMap setIndicator(XComponent component) {
			mIndicatorView = component;
			return this;
		}

		public TabMap setContent(XComponent component) {
			mContentView = component;
			return this;
		}

		public int getTagId() {
			return mTagId;
		}
	}

	@Override
	public boolean onTouch(MotionEvent event) {

		boolean isAction = false;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mTouchComponent = null;
			mIsActionDown = true;
			int x = (int) event.getX();
			int y = (int) event.getY();
			int size = mDrawComponent.size();
			for (int i = size - 1; i >= 0; i--) {
				XComponent component = mDrawComponent.get(i);
				if (component.XYInRange(x, y)) {
					isAction = component.onTouch(event);
					if (isAction) {
						mTouchComponent = component;
					}
					break;
				}
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			// 如果mTouchComponent不为空，则一定收到ACTION_DOWN事件， mIsActionDown = true;
			if (mTouchComponent != null) {
				isAction = mTouchComponent.onTouch(event);
			} else if (!mIsActionDown) {
				// 如果mIsActionDown =false;则没有收到ACTION_DOWN事件，重新执行以下Down事件的操作。
				// 但是事件的Action为Move
				int x = (int) event.getX();
				int y = (int) event.getY();
				int size = mDrawComponent.size();
				for (int i = size - 1; i >= 0; i--) {
					XComponent component = mDrawComponent.get(i);
					// 如果触摸点移到了状态栏上
					boolean handle = false;
					boolean isVertical = AppFuncUtils.getInstance(mActivity).isVertical();
					if (isVertical) {
						if (y <= mStatuBarHeight + mTabHeight) {
							int ty = y + mStatuBarHeight + mTabHeight;
							event = MotionEvent.obtain(event);
							event.setLocation(x, ty);
							handle = component.XYInRange(x, ty);
						}
					} else {
						if (y <= mStatuBarHeight) {
							int ty = y + mStatuBarHeight;
							event = MotionEvent.obtain(event);
							event.setLocation(x, ty);
							handle = component.XYInRange(x, ty);
						}
					}

					if (component.XYInRange(x, y) || handle) {
						isAction = component.onTouch(event);
						if (isAction) {
							mTouchComponent = component;
						}
						break;
					}
				}
				mIsActionDown = true;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mTouchComponent != null) {
				isAction = mTouchComponent.onTouch(event);
			} else if (!mIsActionDown) {
				// 如果mIsActionDown =false;则没有收到ACTION_DOWN事件，重新执行以下Down事件的操作。
				// 但是事件的Action为Up
				int x = (int) event.getX();
				int y = (int) event.getY();
				int size = mDrawComponent.size();
				for (int i = size - 1; i >= 0; i--) {
					XComponent component = mDrawComponent.get(i);

					// 如果触摸点移到了状态栏上
					boolean handle = false;
					boolean isVertical = AppFuncUtils.getInstance(mActivity).isVertical();
					if (isVertical) {
						if (y <= mStatuBarHeight + mTabHeight) {
							int ty = y + mStatuBarHeight + mTabHeight;
							event = MotionEvent.obtain(event);
							event.setLocation(x, ty);
							handle = component.XYInRange(x, ty);
						}
					} else {
						if (y <= mStatuBarHeight) {
							int ty = y + mStatuBarHeight;
							event = MotionEvent.obtain(event);
							event.setLocation(x, ty);
							handle = component.XYInRange(x, ty);
						}
					}

					if (component.XYInRange(x, y) || handle) {
						isAction = component.onTouch(event);
						if (isAction) {
							mTouchComponent = component;
						}
						break;
					}
				}
			}
			mIsActionDown = false;
			mTouchComponent = null;
		}
		return isAction;

	}

	/**
	 * <br>功能简述:获取tab栏的高度（竖屏时）或宽度（横屏时）
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public int getTabSize() {
		if (AppFuncUtils.getInstance(mActivity).isVertical()) {
			return mTabHeight;
		} else {
			return mTabWidth;
		}
	}

	public void onFolderHide() {
		XComponent content = getCurrentContentView();
		if (content != null && content instanceof AppFuncTabBasicContent) {
			mTouchComponent = ((AppFuncTabBasicContent) content).getXGrid();
		}
	}
	/**
	 * <br>功能简述:显示tab栏，有动画
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void showTabTitles() {
		mFunAppSetting.setShowTabRow(FunAppSetting.ON);
		final AppFuncTabBasicContent content = (AppFuncTabBasicContent) getCurrentContentView();
		final XBaseGrid grid = content.getXGrid();
		if (grid != null) {
			GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
					IDiyMsgIds.RESPONSE_GLIDE_UP_DOWN_ACTION, 0, null, null);
			final AppFuncHomeComponent homeComponent = content.getHomeComponent();
			int endX = 0;
			int endY = 0;
			mShowTabRow = true;
			mTabTitles.setVisible(mShowTabRow);
			mTopBarContainer.setVisible(mShowTabRow);
			if (GoLauncher.isPortait()) {
				mTopBarContainer.layout(0, -mUtils.getDimensionPixelSize(TAB_HEIGHT_V_DIMENS_ID),
						mWidth, 0);
				endX = mTopBarContainer.mX;
				endY = 0;
			} else {
				mTopBarContainer.layout(-mUtils.getDimensionPixelSize(TAB_WIDTH_H_DIMENS_ID), 0, 0,
						mHeight);
				endX = 0;
				endY = mTopBarContainer.mY;
			}

			final int contentX = content.mX;
			final int contentY = content.mY;
			final int contentWidth = content.getWidth();
			final int contentHeight = content.getHeight();
			final int homeX = homeComponent.mX;
			final int homeY = homeComponent.mY;
			XMotion moveMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mTopBarContainer.mX,
					mTopBarContainer.mY, endX, endY, 20, 1, 1);
			AnimationInfo info = new AnimationInfo(AnimationInfo.TYPE_CHANGE_POSITION,
					mTopBarContainer, moveMotion, new IAnimateListener() {

						@Override
						public void onStart(XAnimator animator) {
						}

						@Override
						public void onProgress(XAnimator animator, int progress) {
							if (GoLauncher.isPortait()) {
								int diff = Math.round(progress
										* mUtils.getDimensionPixelSize(TAB_HEIGHT_V_DIMENS_ID)
										/ 100f);
								content.mY = contentY + diff;
								content.setSize(contentWidth, contentHeight - diff);
								if (grid != null) {
									grid.fixIconsPosition();
								}
								homeComponent.mY = homeY - diff;
							} else {
								int diff = Math.round(progress
										* mUtils.getDimensionPixelSize(TAB_WIDTH_H_DIMENS_ID)
										/ 100f);
								content.mX = contentX + diff;
								content.setSize(contentWidth - diff, contentHeight);
								if (grid != null) {
									grid.fixIconsPosition();
								}
								homeComponent.mX = homeX - diff;
							}
						}

						@Override
						public void onFinish(XAnimator animator) {
							requestLayout();
						}
					});
			AnimationManager.getInstance(mActivity).attachAnimation(info, null);
		} else {
			requestLayout();
		}
	}
	/**
	 * <br>功能简述:隐藏tab栏，有动画
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void hideTabTitles() {
		mFunAppSetting.setShowTabRow(FunAppSetting.OFF);
		final AppFuncTabBasicContent content = (AppFuncTabBasicContent) getCurrentContentView();
		final XBaseGrid grid = content.getXGrid();
		if (grid != null) {
			GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
					IDiyMsgIds.RESPONSE_GLIDE_UP_DOWN_ACTION, 0, null, null);
			int endX = 0;
			int endY = 0;
			if (GoLauncher.isPortait()) {
				mTopBarContainer.mX = 0;
				mTopBarContainer.mY = 0;
				endX = mTopBarContainer.mX;
				endY = -mUtils.getDimensionPixelSize(TAB_HEIGHT_V_DIMENS_ID);

			} else {
				mTopBarContainer.mX = 0;
				mTopBarContainer.mY = 0;
				endX = -mUtils.getDimensionPixelSize(TAB_WIDTH_H_DIMENS_ID);
				endY = mTopBarContainer.mY;
			}

			final AppFuncHomeComponent homeComponent = content.getHomeComponent();
			final int contentX = content.mX;
			final int contentY = content.mY;
			final int contentWidth = content.getWidth();
			final int contentHeight = content.getHeight();
			final int homeX = homeComponent.mX;
			final int homeY = homeComponent.mY;

			XMotion moveMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mTopBarContainer.mX,
					mTopBarContainer.mY, endX, endY, 20, 1, 1);
			AnimationInfo info = new AnimationInfo(AnimationInfo.TYPE_CHANGE_POSITION,
					mTopBarContainer, moveMotion, new IAnimateListener() {

						@Override
						public void onStart(XAnimator animator) {
						}

						@Override
						public void onProgress(XAnimator animator, int progress) {
							if (GoLauncher.isPortait()) {
								int diff = Math.round(progress
										* mUtils.getDimensionPixelSize(TAB_HEIGHT_V_DIMENS_ID)
										/ 100f);
								content.mY = contentY - diff;
								content.setSize(contentWidth, contentHeight + diff);
								grid.fixIconsPosition();
								homeComponent.mY = homeY + diff;
							} else {
								int diff = Math.round(progress
										* mUtils.getDimensionPixelSize(TAB_WIDTH_H_DIMENS_ID)
										/ 100f);
								content.mX = contentX - diff;
								content.setSize(contentWidth + diff, contentHeight);
								grid.fixIconsPosition();
								homeComponent.mX = homeX + diff;
							}

						}

						@Override
						public void onFinish(XAnimator animator) {
							requestLayout();
						}
					});
			AnimationManager.getInstance(mActivity).attachAnimation(info, null);
		} else {
			requestLayout();
		}
	}
	/**
	 * <br>功能简述:切换tab栏到正常模式（既显示tab）
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param needAnimation
	 */
	public void showTopNormalbar(boolean needAnimation) {
		mTopBarContainer.showNormalBar(needAnimation);
	}
	/**
	 * <br>功能简述:切换tab栏到编辑模式
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param needAnimation
	 */
	public void showTopEditBar(boolean needAnimation) {
		mTopBarContainer.showEditBar(needAnimation);
	}
	/**
	 * <br>功能简述:获取tab栏的容器，方便获取tab栏和编辑栏
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public AppFuncTopSwitchContainer getTopBarContainer() {
		return mTopBarContainer;
	}

	private void initResource() {
		String curPackageName = ThemeManager.getInstance(mActivity).getCurThemePackage();
		String packageName = null;
		if (!curPackageName.equals(GOLauncherApp.getSettingControler().getFunAppSetting()
				.getTabHomeBgSetting())) {
			packageName = GOLauncherApp.getSettingControler().getFunAppSetting()
					.getTabHomeBgSetting();
		}
		if (!AppUtils.isAppExist(mContext, packageName)) {
			packageName = GOLauncherApp.getThemeManager().getCurThemePackage();
		}

		mProManageEdit.loadResource(packageName);
	}

	public AppFuncFolderQuickAddBar getFolderQuickAddBar() {
		return mFolderQuickBar;
	}
	
	public ProManageEditDock getProManageEditDock() {
		return mProManageEdit;
	}
}

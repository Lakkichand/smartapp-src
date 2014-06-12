package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jiubang.core.message.IMessageHandler;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingActivity;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.controller.TabController;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataGroup;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.view.TabActionBar.TabObserver;
import com.zhidian.wifibox.view.dialog.ConfirmDialog;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.view.scroller.ScreenScroller;
import com.zhidian3g.wifibox.view.scroller.ScreenScrollerListener;
import com.zhidian3g.wifibox.view.scroller.ScrollerViewGroup;

/**
 * 负责展示一个层级的应用列表，跳转逻辑等交给TabController
 * 
 * TabManageView只负责显示一个TabDataGroup的数据，不负责逻辑处理
 * 
 * @author xiedezhi
 * 
 */
public class TabManageView extends LinearLayout implements IMessageHandler,
		ScreenScrollerListener {
	/**
	 * 工厂类
	 */
	private ContainerBuilder mBuilder;
	/**
	 * 当前显示的层级数据
	 */
	private TabDataGroup mGroup;
	private LayoutInflater mInflater = LayoutInflater.from(getContext());
	/**
	 * 当前显示的container
	 */
	private List<IContainer> mCurrentContainers = new ArrayList<IContainer>();
	/**
	 * 左右滑动组件
	 */
	private ScrollerViewGroup mScrollerViewGroup;
	/**
	 * 导航栏
	 */
	private NavigationBar mNavigationBar;
	/**
	 * 顶部标题栏，只有第二及第二以后的层级才显示
	 */
	private RelativeLayout mTitleBar;
	/**
	 * 搜索栏，第一层级才会显示
	 */
	private RelativeLayout mSearchBar;
	/**
	 * 标示栏
	 */
	private TabActionBar mActionBar;
	/**
	 * 用TabDataGroup更新界面的Listener
	 */
	private TAIResponseListener mUpdateViewRListener = new TAIResponseListener() {

		@Override
		public void onSuccess(TAResponse response) {
			TabDataGroup group = (TabDataGroup) response.getData();
			// 显示TabDataGroup
			updateView(group);
		}

		@Override
		public void onStart() {
		}

		@Override
		public void onRuning(TAResponse response) {
		}

		@Override
		public void onFinish() {
		}

		@Override
		public void onFailure(TAResponse response) {
		}
	};
	/**
	 * 点击监听
	 */
	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.searchEdit:
				MainActivity.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
						IDiyMsgIds.SHOW_SEARCHVIEW, -1, null, null);
				break;
			case R.id.searchBtn:
				MainActivity.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
						IDiyMsgIds.SHOW_SEARCHVIEW, -1, null, null);
				break;
			case R.id.super_speed_Btn:
				// 先检查是否可以切换到极速模式
				boolean b = ModeManager.checkRapidly();
				if (!b) {
					// 提示
					ConfirmDialog cDialog = new ConfirmDialog(getContext(),
							"网络提示", "请到指定门店连接WIFI，才能切换到超速模式");
					cDialog.show();
				} else {
					// 点击模式切换时，先设置ModeManager，再通知MainDrawer和MainViewgroup
					MainActivity.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
							IDiyMsgIds.REMOVE_SWITCH_MODE_TIP, -1, null, null);
					ModeManager.getInstance().setRapidly(
							!ModeManager.getInstance().isRapidly());
					MainActivity.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
							IDiyMsgIds.SWITCH_MODE, -1, null, null);
				}
				break;
			default:
				break;
			}
		}
	};

	public TabManageView(Context context) {
		super(context);
		init();
	}

	public TabManageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		mBuilder = new ContainerBuilder(getContext(), mInflater);
		initView();
		// 注册消息组件
		MainActivity.registMsgHandler(this);
		this.setBackgroundColor(0xFFe0e0e0);
	}

	/**
	 * 初始化view树
	 */
	private void initView() {
		this.setOrientation(LinearLayout.VERTICAL);
		// 标题栏
		mTitleBar = (RelativeLayout) mInflater
				.inflate(R.layout.tab_title, null);
		mTitleBar.findViewById(R.id.back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 后退层级
						MainActivity.sendHandler(null,
								IDiyFrameIds.TABMANAGEVIEW,
								IDiyMsgIds.BACK_ON_ONE_LEVEL, -1, null, null);
					}
				});
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, DrawUtil.dip2px(
						getContext(), 42));
		addView(mTitleBar, lp);
		mTitleBar.setVisibility(View.GONE);
		// 搜索条
		mSearchBar = (RelativeLayout) mInflater.inflate(R.layout.main_header,
				null);
		mSearchBar.findViewById(R.id.mapSearchBtn).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						((SlidingActivity) getContext()).getSlidingMenu()
								.toggle();
					}
				});
		mSearchBar.findViewById(R.id.searchEdit).setOnClickListener(
				mOnClickListener);
		mSearchBar.findViewById(R.id.searchBtn).setOnClickListener(
				mOnClickListener);
		mSearchBar.findViewById(R.id.super_speed_Btn).setOnClickListener(
				mOnClickListener);
		addView(mSearchBar, lp);
		mSearchBar.setVisibility(View.GONE);
		// actionbar
		mActionBar = new TabActionBar(getContext(), new TabObserver() {

			@Override
			public void handleChangeTab(int tabIndex) {
				// 根据当前层级和下标设置TOUCHMODE
				if (TabDataManager.getInstance().getTabStackSize() > 1) {
					((SlidingActivity) (getContext())).getSlidingMenu()
							.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
				} else {
					if (tabIndex == 0) {
						((SlidingActivity) (getContext())).getSlidingMenu()
								.setTouchModeAbove(
										SlidingMenu.TOUCHMODE_FULLSCREEN);
					} else {
						((SlidingActivity) (getContext())).getSlidingMenu()
								.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
					}
				}
				mScrollerViewGroup.setScreenScrollerListener(null);
				mScrollerViewGroup.gotoViewByIndexImmediately(tabIndex);
				mScrollerViewGroup
						.setScreenScrollerListener(TabManageView.this);
				mGroup.index = tabIndex;
			}
		});
		lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, DrawUtil.dip2px(
						getContext(), 43));
		addView(mActionBar, lp);
		mActionBar.setVisibility(View.GONE);
		// 滑动组件
		mScrollerViewGroup = new ScrollerViewGroup(getContext(), this);
		lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, 0);
		lp.weight = 1.0f;
		addView(mScrollerViewGroup, lp);
		mScrollerViewGroup.getScreenScroller().setPadding(0);
		// 导航栏
		mNavigationBar = (NavigationBar) mInflater.inflate(
				R.layout.navigatetionbar, null);
		lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, DrawUtil.dip2px(
						getContext(), 50));
		lp.weight = 0;
		addView(mNavigationBar, lp);
	}

	/**
	 * 更新整个层级的界面
	 */
	private void updateView(TabDataGroup group) {
		if (group == null || group.mPageList == null
				|| group.mPageList.size() <= 0) {
			return;
		}
		mGroup = group;
		mCurrentContainers.clear();
		// 清除未完成的图标任务
		AsyncImageManager.getInstance().removeAllTask();
		mScrollerViewGroup.removeAllViews();
		for (PageDataBean bean : group.mPageList) {
			// 获取container
			IContainer container = mBuilder.getContainer(bean);
			mScrollerViewGroup.addView((View) container);
			// 初始化container
			container.updateContent(bean);
			mCurrentContainers.add(container);
		}
		// 更新页面数量
		mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
		// 根据当前层级和下标设置TOUCHMODE
		if (TabDataManager.getInstance().getTabStackSize() > 1) {
			((SlidingActivity) (getContext())).getSlidingMenu()
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		} else {
			if (group.index == 0) {
				((SlidingActivity) (getContext())).getSlidingMenu()
						.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			} else {
				((SlidingActivity) (getContext())).getSlidingMenu()
						.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
			}
		}
		mScrollerViewGroup.setScreenScrollerListener(null);
		mScrollerViewGroup.gotoViewByIndexImmediately(group.index);
		mScrollerViewGroup.setScreenScrollerListener(this);
		// 根据数据、当前层级数和网络模式显示UI
		if (!ModeManager.getInstance().isRapidly()) {
			if (TabDataManager.getInstance().getTabStackSize() > 1) {
				mTitleBar.setVisibility(View.VISIBLE);
				mSearchBar.setVisibility(View.GONE);
				mNavigationBar.setVisibility(View.GONE);
				TextView title = (TextView) mTitleBar.findViewById(R.id.title);
				title.setText(group.title);
			} else {
				mTitleBar.setVisibility(View.GONE);
				mSearchBar.setVisibility(View.VISIBLE);
				mNavigationBar.setVisibility(View.VISIBLE);
			}
			if (group.mPageList.size() > 1) {
				List<String> list = new ArrayList<String>();
				for (PageDataBean bean : group.mPageList) {
					list.add(bean.mTitle);
				}
				mActionBar.init(list);
				mActionBar.setSelection(group.index);
				mActionBar.setVisibility(View.VISIBLE);
			} else {
				mActionBar.setVisibility(View.GONE);
			}
		} else {
			mTitleBar.setVisibility(View.GONE);
			mSearchBar.setVisibility(View.GONE);
			mActionBar.setVisibility(View.GONE);
		}
		if (TabDataManager.getInstance().getTabStackSize() > 1) {
			MainActivity.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
					IDiyMsgIds.REMOVE_SWITCH_MODE_TIP, -1, null, null);
		} else {
			MainActivity.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
					IDiyMsgIds.SHOW_SWITCH_MODE_TIP, -1, null, null);
		}
	}

	/**
	 * 当系统有安装，卸载，更新应用等操作时回调该接口
	 * 
	 * @param packName
	 *            安装/卸载/更新的包名
	 */
	public void onAppAction(String packName) {
		for (IContainer container : mCurrentContainers) {
			container.onAppAction(packName);
		}
	}

	/**
	 * activity onresume
	 */
	public void onResume() {
		for (IContainer container : mCurrentContainers) {
			container.onResume();
		}
	}

	/**
	 * 网络模式发生改变
	 */
	public void onModeChange() {
		// 通知导航栏
		mNavigationBar.onModeChange();
	}

	/**
	 * 更新应用的下载进度
	 */
	public void notifyDownloadState(DownloadTask downloadTask) {
		for (IContainer container : mCurrentContainers) {
			container.notifyDownloadState(downloadTask);
		}
	}

	@Override
	public int getId() {
		return IDiyFrameIds.TABMANAGEVIEW;
	}

	@Override
	public boolean handleMessage(Object who, int type, final int msgId,
			final int param, final Object object, final List objects) {
		switch (msgId) {
		case IDiyMsgIds.SWITCH_NAVIGATION: {
			String url = (String) object;
			// 通知TabController读取导航栏数据
			TAApplication.getApplication().doCommand(
					getContext().getString(R.string.tabcontroller),
					new TARequest(TabController.SWITCH_NAVIGATION, url),
					mUpdateViewRListener, true, false);
			break;
		}
		case IDiyMsgIds.ENTER_NEXT_LEVEL: {
			String url = (String) object;
			Object obj = null;
			if (objects != null && objects.size() > 0) {
				obj = objects.get(0);
			}
			Object[] objs = { url, obj };
			// 通知TabController读取下一层数据
			TAApplication.getApplication().doCommand(
					getContext().getString(R.string.tabcontroller),
					new TARequest(TabController.JUMP_NEXT_LEVEL, objs),
					mUpdateViewRListener, true, false);
			break;
		}
		case IDiyMsgIds.BACK_ON_ONE_LEVEL: {
			if (TabDataManager.getInstance().getTabStackSize() > 1) {
				// 通知TabController读取上一层数据
				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.tabcontroller),
						new TARequest(TabController.FALLBACK_TAB, null),
						mUpdateViewRListener, true, false);
			}
			break;
		}
		case IDiyMsgIds.REFRESH_CONTAINER: {
			// 后台拿到新的数据后刷新对应的页面
			String idUrl = (String) object;
			for (IContainer container : mCurrentContainers) {
				if (container.getDataUrl().equals(idUrl)) {
					container.updateContent(TabDataManager.getInstance()
							.getPageData(idUrl));
				}
			}
			break;
		}
		default:
			break;
		}
		return false;
	}

	/**
	 * activity onDestory
	 */
	public void onDestory() {
		mNavigationBar.onDestory();
		MainActivity.unRegistMsgHandler(this);
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
		// 根据当前层级设置TOUCHMODE
		if (TabDataManager.getInstance().getTabStackSize() > 1) {
			((SlidingActivity) (getContext())).getSlidingMenu()
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		} else {
			switch (newScreen) {
			case 0:
				((SlidingActivity) (getContext())).getSlidingMenu()
						.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
				break;
			default:
				((SlidingActivity) (getContext())).getSlidingMenu()
						.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
				break;
			}
		}
		// 更新actionbar
		mActionBar.setSelection(newScreen);

	}

	@Override
	public void onScrollFinish(int currentScreen) {
		// 根据当前层级设置TOUCHMODE
		if (TabDataManager.getInstance().getTabStackSize() > 1) {
			((SlidingActivity) (getContext())).getSlidingMenu()
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		} else {
			switch (currentScreen) {
			case 0:
				((SlidingActivity) (getContext())).getSlidingMenu()
						.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
				break;
			default:
				((SlidingActivity) (getContext())).getSlidingMenu()
						.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
				break;
			}
		}
		// 更新TabDataGroup下标
		mGroup.index = currentScreen;
		// 更新actionbar
		mActionBar.setSelection(currentScreen);
	}

}

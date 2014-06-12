package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.gau.go.launcherex.R;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.appgame.base.component.IContainer;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.component.TabManageView;
import com.jiubang.ggheart.appgame.base.data.TabDataManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-27]
 */
public class AppsManageView extends TabManageView implements IMessageHandler {

	// 点击统计栏跳转到应用管理的标识
	public static final int APPS_START_TYPE = 1;

	private Handler mHandler = null; // 用于处理点击title返回gostore事件

	public AppsManageView(Context context, int entranceId) {
		super(context, entranceId);
		AppsManagementActivity.registMsgHandler(this);

		// 初始化界面
		initTitle();
	}

	/**
	 * 初始化标题的方法
	 */
	private void initTitle() {
		if (!(getEntrance() == MainViewGroup.ACCESS_FOR_GOSTORE_UPDATE || getEntrance() == MainViewGroup.ACCESS_FOR_GOSTORE)) {
			// 如果不是由GoStore进入，则不显示返回箭头
			mBackButton.setVisibility(GONE);
//			mIcon.setVisibility(View.VISIBLE);
		} else {
			// 如果从GoStore进入，显示返回键，隐藏图标
//			mIcon.setVisibility(View.GONE);
			mBackButton.setVisibility(View.VISIBLE);
		}
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TabDataManager.getInstance().getTabStackSize() > 1) { // 有两级或以上的tab
					// 如果正在刷新，则停止刷新，返回之前一页
					if (TabController.isTabRefreshing()) {
						Log.e("AppsManageView", "tab is refreshing");
						TabController.stopRefreshing();
						return;
					}
					TabController.fallBackTab();
				} else if (TabDataManager.getInstance().getTabStackSize() == 1) { // 有一级tab
					// 如果正在刷新，则停止刷新，返回之前一页
					if (TabController.isTabRefreshing()) {
						Log.e("AppsManageView", "tab is refreshing");
						TabController.stopRefreshing();
						return;
					}
					// 结束应用中心Activity
					mHandler.obtainMessage(AppsManagementActivity.FINISH_ACTIVITY).sendToTarget();
				} else { // 没有tab，直接退出
					// 结束应用中心Activity
					mHandler.obtainMessage(AppsManagementActivity.FINISH_ACTIVITY).sendToTarget();
				}
			}
		});

		mOperatorButton.setImageResource(R.drawable.ui2_search);
		mOperatorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 其他页面打开搜索界面
				AppsManagementActivity.sendHandler(this,
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME, IDiyMsgIds.SHOW_SEARCH_VIEW,
						0, null, null);
				AppManagementStatisticsUtil.getInstance();
				AppManagementStatisticsUtil.saveTabClickData(getContext(),
						AppManagementStatisticsUtil.TAB_ID_SEARCH, null);
			}
		});
	}

	@Override
	public void recycle() {
		AppsManagementActivity.unRegistMsgHandler(this);
		for (IContainer container : mContainers) {
			container.cleanup();
		}
		mContainers.clear();
		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.destory();
		}
		//图片管理器的清理
		AsyncImageManager.onDestory();
	}

	@Override
	protected void changeCurrentTab(int currScreen) {
		// 这里是属于回调方法，有可能回调回来的时候mTabDataGroup已经改变了，会导致数组越界，所以要加上保护
		try {
			// 统计
			int currTypeId = mTabDataGroup.data.get(currScreen).typeId;
			AppManagementStatisticsUtil.getInstance();
			// Log.e("XIEDEZHI", "typeid = " + currTypeId + "  name = "
			// + mTabDataGroup.titles.get(currScreen));
			AppManagementStatisticsUtil.saveTabClickData(getContext(), currTypeId, null);
			showOperatorButton(parseFunButton(mTabDataGroup.data.get(currScreen).funbutton));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void showOperatorButton(int[] funButtons) {		
		// 显示搜索按钮，暂时应用中心只支持搜索按钮
		mOperatorButton.setVisibility(View.GONE);
		if (funButtons == null || funButtons.length == 0) {
			return;
		}
		for (int fun : funButtons) {
			if (fun == FUNC_BUTTON_FOR_SEARCH) {
				mOperatorButton.setVisibility(View.VISIBLE);
				return;
			}
		}}

	@Override
	public boolean handleMessage(Object who, int type, final int msgId, final int param,
			final Object object, final List objects) {
		switch (msgId) {
			case IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT :

				int updateCount = param;
				// 要在管理tab栏上显示更新数字
				mGridTitleBar.setUpdateCount(updateCount);
				break;
		}
		return false;
	}

	@Override
	public int getId() {
		return IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME;
	}

	@Override
	public void onSDCardStateChange(boolean turnon) {
		super.onSDCardStateChange(turnon);
	}

	@Override
	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	@Override
	public void onAppAction(String packName, int appAction) {
		super.onAppAction(packName, appAction);
	}
}

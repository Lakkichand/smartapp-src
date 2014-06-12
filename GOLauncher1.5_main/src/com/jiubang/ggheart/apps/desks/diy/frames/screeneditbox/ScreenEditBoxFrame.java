package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Workspace;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.BaseTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.DesktopThemeTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.WidgetSubTab;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 屏幕编辑下部分的添加界面
 * 
 * @author jiangchao
 * 
 */
public class ScreenEditBoxFrame extends AbstractFrame implements OnRespondTouch, AnimationListener {

	public static final int ANIMATION_DURATION = 300;
	private ScreenEditLayout mLayoutView;
	private ScreenEditTabView mTabView;
	private ScreenEditLargeTabView mLargeTabView;

	private GoWidgetProviderInfo mInfo;
	public LayoutInflater mInflater;
	private String mPkgName;  // 从桌面外点击小部件或多屏多壁纸快捷方式跳转添加界面时的包名

	private DataEdngine mDataEdngine;
	public ScreenEditBoxFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);
		mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayoutView = (ScreenEditLayout) mInflater.inflate(R.layout.screen_edit_layout,
				mFrameManager.getRootView(), false);
		mTabView = (ScreenEditTabView) mLayoutView.findViewById(R.id.edit_box);
		mLargeTabView = (ScreenEditLargeTabView) mLayoutView.findViewById(R.id.edit_box_large);
		mDataEdngine = new DataEdngine(mActivity);
		mLayoutView.setTabView(mTabView);
		mLayoutView.setLargeTabView(mLargeTabView);
		mTabView.setEditLayout(mLayoutView);
		mLargeTabView.setEditLayout(mLayoutView);
		mTabView.setDataEdngine(mDataEdngine);
		mLargeTabView.setDataEdngine(mDataEdngine);
	}

	@Override
	public View getContentView() {
		return mLayoutView;
	}

	public ScreenEditTabView getTabView() {
		return mTabView;
	}
	public ScreenEditLargeTabView getLargeTabView() {
		return mLargeTabView;
	}
	@Override
	public void onVisiable(int visible) {
		super.onVisiable(visible);
		if (visible == View.VISIBLE) {
			mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME, View.INVISIBLE);
		}
	}

	@Override
	public void onRemove() {

		super.onRemove();
		// add by jiang
		GoLauncher
				.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK, 100,
						null, null);
		//显示屏幕层指示器
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SHOW_INDICATOR, 1, null,
				null);
		mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME, View.VISIBLE);
		mPkgName = null; //清空
		if (mInflater != null) {
			mInflater = null;
		}

		if (mTabView != null) {
			mTabView.selfDestruct();
			mTabView = null;
		}
		if (mLargeTabView != null) {
			mLargeTabView.selfDestruct();
			mLargeTabView = null;
		}
		if (mInfo != null) {
			mInfo = null;
		}
		if (mLayoutView != null) {
			mLayoutView = null;
		}
		//用于解决按返回键或是点击屏幕退出添加界面后,按Home键回主屏无过渡动画
		Workspace.sFlag = false;
	}

	@Override
	public boolean isRespondTouch() {
		return true;
	}

	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		boolean ret = false;
		switch (msgId) {

			case IDiyMsgIds.SCREEN_EDIT_ADD_APPS :
				mLayoutView.changesizeForApp(BaseTab.TAB_ADDAPPS);
				break;

			case IDiyMsgIds.SCREEN_EDIT_ADD_FORLDER :
				mLayoutView.changesizeForApp(BaseTab.TAB_ADDFOLDER);
				break;

			case IDiyMsgIds.SCREEN_EDIT_ADD_GOSHORTCUT :
				mLayoutView.changesizeForApp(BaseTab.TAB_ADDGOSHORTCUT);
				break;

			case IDiyMsgIds.SCREEN_EDIT_ADD_GOWIDGET :
				mInfo = (GoWidgetProviderInfo) object;
				mLayoutView.changesizeForWidget();
				break;

			case IDiyMsgIds.SCREEN_EDIT_ADD_GOWIDGET_INFO :
				mLargeTabView.changeGowidgetToInfoPage();
				break;

			case IDiyMsgIds.SCREEN_EDIT_ADD_GOWIDGET_PIC :
				mLargeTabView.changeGowidgetToPicPage();
				break;
			// 处理大主题含widget,未安装情况下点击下载跳go widget部分
			case IDiyMsgIds.SCREEN_EDIT_GOTO_GO_WIDGET :
				DesktopThemeTab desktopThemeTab = (DesktopThemeTab) mTabView.getDataEdngine()
						.getTab(BaseTab.TAB_THEME);
				if (desktopThemeTab != null) {
					desktopThemeTab.dismissDialog();
					mTabView.setCurrentTab(BaseTab.TAB_GOWIDGET);
					mTabView.onRefreshTopBack(BaseTab.TAB_GOWIDGET);
				}
				break;
			case IDiyMsgIds.SCREENEDIT_SHOW_ANIMATION_IN: {
				GoLauncher
						.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREENEDIT_WORKSPACE_INDICATOR_UP, 1,
								null, null);
				// 进入动画
				mLayoutView.setContainerMode(0);
				Animation animation = AnimationUtils.loadAnimation(mActivity,
						R.anim.input_method_enter);
				animation.setDuration(ANIMATION_DURATION);
				mLayoutView.startAnimation(animation);
				if (object != null) {
					HashMap map = (HashMap) object;
					String tab = (String) map.get("editTab");
					mPkgName = (String) map.get("pkg");
					if (tab == null) {
						mTabView.setCurrentTab(BaseTab.TAB_APP);
						break;
					}
					if (mPkgName != null
							&& mPkgName.equals(LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME)) {
						// 多屏多壁纸入口
						mTabView.setCurrentTab(BaseTab.TAB_WALLPAPER);
						break;
					}
					if (tab.equals(BaseTab.TAB_GOWIDGET)) {
						// 1,由menu 点击进入 gowidget 2,外部gowidget图标进入
						mTabView.refreshBackTab(BaseTab.TAB_GOWIDGET);
						mTabView.setCurrentTab(BaseTab.TAB_GOWIDGET);
					} else if (tab.equals(BaseTab.TAB_WALLPAPER)) {
						// 由menu 点击进入 壁纸
						mTabView.setCurrentTab(BaseTab.TAB_WALLPAPER);
					} else if (tab.equals(BaseTab.TAB_EFFECTS)) {
						// 由menu 点击进入 特效
						mTabView.setCurrentTab(BaseTab.TAB_EFFECTS);
					} else if (tab.equals(BaseTab.TAB_THEMELOCKER)) {
						// 由Go手册进入主题设置
						mTabView.setCurrentTab(BaseTab.TAB_THEMELOCKER);
					}
				} else {
					mTabView.setCurrentTab(BaseTab.TAB_APP);
				}
			}
				break;
	
			case IDiyMsgIds.SCREENEDIT_SHOW_ANIMATION_OUT: {
				mLayoutView.setContainerMode(0);
				// 退出动画
				Animation animationOut = AnimationUtils.loadAnimation(mActivity,
						R.anim.input_method_exit);
				animationOut.setDuration(ANIMATION_DURATION);
				animationOut.setInterpolator(mActivity,
						android.R.anim.decelerate_interpolator);
				animationOut.setFillAfter(true);
				animationOut.setAnimationListener(this);
				mLayoutView.startAnimation(animationOut);
			}
				break;
			// 添加widget到桌面
			case IDiyMsgIds.SCREEN_EDIT_ADD_GOWIDGET_TO_SCREEN : {
				WidgetSubTab widgetSubTab = (WidgetSubTab) mTabView.getDataEdngine().getTab(
						BaseTab.TAB_ADDGOWIDGET);
				if (widgetSubTab != null) {
					widgetSubTab.addGoWidget(param);
				}
			}
			break;
			case IDiyMsgIds.EVENT_INSTALL_APP :
			case IDiyMsgIds.EVENT_INSTALL_PACKAGE :
			case IDiyMsgIds.EVENT_UNINSTALL_APP :
			case IDiyMsgIds.EVENT_UNINSTALL_PACKAGE : {
				if (object != null) {
					// 通知子Tab数据更新
					if (mLargeTabView != null) {
						BaseTab curTab = mLargeTabView.getDataEdngine().getTab(
								mLargeTabView.getCurTabTag());
						BaseTab curTab2 = mTabView.getDataEdngine().getTab(mTabView.getCurTabTag());
						// widget预览页处理
						if (curTab != null && curTab2 != null
								&& curTab.getTag().equals(BaseTab.TAB_ADDGOWIDGET)
								&& curTab2.getTag().equals(BaseTab.TAB_GOWIDGET)) {
							mLayoutView.changesizeForNormal();
							mTabView.getDataEdngine().getTab(BaseTab.TAB_GOWIDGET)
									.handleAppChanged(msgId, (String) object);
						} else {
							if (curTab2 != null) {
								curTab2.handleAppChanged(msgId, (String) object);
							}
						}
					}
				}
			}

				break;

			case IDiyMsgIds.SCREEN_EDIT_UPDATE_WALLPAPER_ITEMS :
				// 刷新壁纸列表
				mTabView.updateTab(BaseTab.TAB_WALLPAPER);
				break;
				
			case IFrameworkMsgId.SYSTEM_ON_RESUME :
				// add by chenbingdong
				// 返回添加界面，界面缩放，会涉及到壁纸的滑动，影响多屏多壁纸，因此发送广播调整
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_SEND_BROADCASTTO_MULTIPLEWALLPAPER, -1, null,
						null);
				break;
			case IDiyMsgIds.SCREEN_EDIT_REFRESH_THEME:
				if (mTabView != null && mTabView.getCurTabTag() != null
						&& mTabView.getCurTabTag().equals(BaseTab.TAB_THEME)) {
					DesktopThemeTab curTab2 = (DesktopThemeTab) mTabView
							.getDataEdngine().getTab(mTabView.getCurTabTag());
					curTab2.changeLastSelectView();
				}
				break;
			case IDiyMsgIds.GET_SCREEN_EDIT_BOX_CONTAINER_MODE : {
				if (object != null && object instanceof int[]) {
					((int[]) object)[0] = mLayoutView.getContainerMode();
				}
			}
				break;
			default :
				break;
		}
		return ret;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (mLayoutView.mIsAnimation) {
				return true;
			}
			final String tabTag = mTabView.getCurTabTag();
			final int tabLevel = mTabView.getCurTabLevel(tabTag);

			if (mLayoutView.mIsLargeShowing) {
				String tab = mLargeTabView.getCurTabTag();
				// 离开文件夹新建页面
				if (tab != null && tab.equals(BaseTab.TAB_ADDFOLDER)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.LEAVE_NEW_FOLDER_STATE, -1, null, null);
				}
				mLayoutView.changesizeForNormal();
			} else if (tabLevel == BaseTab.TAB_LEVEL_1) {
				// 设置文件夹不响应
				mLayoutView.setIsExiting(true);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREENEDIT_CLOSE_SCREEN_EDIT_LAYOUT, 1, null, null);
				return true;
			} else if (tabLevel == BaseTab.TAB_LEVEL_2) {
				// mLayoutView.changesizeForNormal();
				// 如果是第二级，那先要返回第一级
				mTabView.backToFirstLevel(tabTag);

			} else if (tabLevel == BaseTab.TAB_LEVEL_3) {
				// 离开文件夹新建页面
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.LEAVE_NEW_FOLDER_STATE, -1, null, null);

				// 如果是第三级，那先要返回AppTab
				mLayoutView.changesizeForNormal();
				// mTabView.backToFirstLevel(tabTag);
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		GoLauncher.postMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
				IDiyFrameIds.SCREEN_EDIT_BOX_FRAME, null, null);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}
	public GoWidgetProviderInfo getInfo() {
		return mInfo;
	}

	public void setInfo(GoWidgetProviderInfo mInfo) {
		this.mInfo = mInfo;
	}
	
}

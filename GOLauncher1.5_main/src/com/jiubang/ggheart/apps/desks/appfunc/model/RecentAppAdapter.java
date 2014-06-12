package com.jiubang.ggheart.apps.desks.appfunc.model;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.component.RecentAppsIcon;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.data.info.AppItemInfo;

/**
 * 
 * <br>类描述: 最近打开数据适配器
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-12-27]
 */
public class RecentAppAdapter extends AppFuncAdapter {
	private volatile List<AppItemInfo> mApps;
	private AppFuncThemeController mThemeController;
	private int mCellCount;

	public RecentAppAdapter(Activity activity, boolean drawText) {
		super(activity, drawText);
		mApps = new ArrayList<AppItemInfo>();
		mThemeController = AppFuncFrame.getThemeController();
		AppDrawerControler.getInstance(mActivity).setRecentAppObserver(this);
	}

	@Override
	public int getCount() {
		if (mApps == null) {
			return 0;
		} else {
			if (mApps.size() > mCellCount) {
				return mCellCount;
			}
			return mApps.size();
		}
	}

	@Override
	public Object getItem(int position) {
		return mApps.get(position);
	}

	@Override
	public XComponent getComponent(int position, int x, int y, int width, int height,
			XComponent convertView, XPanel parent) {
		RecentAppsIcon appIcon = null;
		// TODO: 处理“最近打开”
		AppItemInfo info = mApps.get(position);
		if (convertView == null || !(convertView instanceof RecentAppsIcon)) {
			Drawable editDrawable = mThemeController
					.getDrawable(mThemeController.getThemeBean().mAppIconBean.mDeletApp);
			if ((editDrawable instanceof BitmapDrawable) == false) {
				appIcon = new RecentAppsIcon(mActivity, AppFuncConstants.TICK_COUNT, x, y, width,
						height, info, null, mDrawText);
			} else {
				appIcon = new RecentAppsIcon(mActivity, AppFuncConstants.TICK_COUNT, x, y, width,
						height, info, (BitmapDrawable) editDrawable, mDrawText);
			}
			appIcon.setEventListener(appIcon);
		} else {
			appIcon = (RecentAppsIcon) convertView;
			appIcon.setAppInfo(info);
			appIcon.setNameVisible(mDrawText);
		}

		return appIcon;
	}

	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		switch (msgId) {
			case SHOWNAME_CHANGED : {
				// 广播方式
				mDrawText = (AppFuncFrame.getDataHandler().getShowName() < FunAppSetting.APPNAMEVISIABLEYES)
						? false
						: true;
				// if (dataObserver != null){
				// dataObserver.onInvalidated();
				// }
				return false;
			}
			case CLEAR_RECENTAPP : {
				if (dataObserver != null) {
					dataObserver.onInvalidated();
				}

				return true;
			}
			case APP_REMOVED : {
				// 此消息还需要所有程序处理，因此返回false
				// 更新最近打开数据
				mApps = AppDrawerControler.getInstance(mActivity).getRecentAppItems();
				if (dataObserver != null) {
					dataObserver.onInvalidated();
				}

				return false;
			}
			case UPDATE_RECENTAPP : {
				if (AppFuncFrame.getDataHandler() != null) {
					mApps = AppDrawerControler.getInstance(mActivity).getRecentAppItems();
					if (dataObserver != null) {
						dataObserver.onInvalidated();
					}
				}
				return true;
			}
			case APP_ADDED : {
				mApps = AppDrawerControler.getInstance(mActivity).getRecentAppItems();
				if (dataObserver != null) {
					dataObserver.onInvalidated();
				}

				return false;
			}
		}
		return false;
	}

	@Override
	public void loadApp() {
		mApps = AppDrawerControler.getInstance(mActivity).getRecentAppItems();
	}

	@Override
	public boolean dataSourceLoaded() {
		if (mApps == null) {
			return false;
		} else {
			return true;
		}
	}

	public void setMaxCount(int count) {
		mCellCount = count;
	}

	@Override
	public void reloadApps() {
		// TODO Auto-generated method stub

	}
}

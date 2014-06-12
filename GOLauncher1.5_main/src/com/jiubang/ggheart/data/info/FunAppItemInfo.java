package com.jiubang.ggheart.data.info;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;

import com.jiubang.ggheart.plugin.notification.NotificationType;

/**
 * 
 * <br>类描述: 功能表应用图标类
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-22]
 */
public class FunAppItemInfo extends FunItemInfo {

	// 重设了数据bean TODO:做消息映射转换
	public static final int TITLECHANGED = END;
	public static final int INCONCHANGE = END + 1;
	public static final int RESETBEAN = END + 2;
	public static final int REFRESH_UPDATE_STATUS = END + 3;
	public static final int UNREADCHANGED = END + 4;
	public static final int UNREADTYPECHANGED = END + 5;
	/**
	 * 是否推荐应用发生改变
	 */
	public static final int IS_RECOMMEND_APP_CHANGE = END + 7;

	private AppItemInfo mAppItemInfo;
	private boolean mIsTemp; // 是否是暂存的 TODO:放到FunAppItemInfo

	private boolean mIsNew = false; // 是否是新安装的
	/**
	 * 是否可更新
	 */
	private boolean mIsUpdate = false;

	/**
	 * 在哪个文件夹
	 */
	private long mInWhitchFolder;

	/**
	 * 应用程序进入文件夹的时间。
	 */
	private long mTimeInFolder;
	/**
	 *  进程唯一标识
	 */
	private int mPid;
	/**
	 * 是否是白名单中的
	 */
	private boolean mIsIgnore;

	public FunAppItemInfo(AppItemInfo appItemInfo) {
		super();

		mType = TYPE_APP;
		mAppItemInfo = appItemInfo;
		if (null != appItemInfo) {
			mIsNew = mAppItemInfo.mIsNewRecommendApp;
			// 更新唯一标识
			mIntent = appItemInfo.mIntent;
			// 监听数据体bean
			mAppItemInfo.registerObserver(this);
		}
	}

	/**
	 * 设置数据bean
	 * 
	 * @param appItemInfo
	 */
	public void setAppItemInfo(AppItemInfo appItemInfo) {
		mAppItemInfo = appItemInfo;

		if (null != appItemInfo) {
			// 更新唯一标识
			mIntent = appItemInfo.mIntent;
			// 监听数据体bean
			mAppItemInfo.registerObserver(this);
			broadCast(RESETBEAN, 0, mAppItemInfo, null);
		}
	}

	/**
	 * 获取数据
	 * 
	 * @return
	 */
	public AppItemInfo getAppItemInfo() {
		return mAppItemInfo;
	}

	/**
	 * 获取应用程序是否为系统应用程序
	 * 
	 * @author huyong
	 * @return
	 */
	@Override
	public boolean isSysApp() {
		if (mAppItemInfo == null) {
			return false;
		} else {
			return mAppItemInfo.getIsSysApp();
		}
	}

	/**
	 * 获取名称
	 * 
	 * @return 名称
	 */
	@Override
	public String getTitle() {
		if (null == mAppItemInfo) {
			return null;
		}
		return mAppItemInfo.mTitle;
	}

	/**
	 * 获取时间
	 * 
	 * @param packageMgr
	 * @return
	 */
	@Override
	public long getTime(PackageManager packageMgr) {
		if (null == mAppItemInfo) {
			return 0;
		}
		return mAppItemInfo.getAppTime(packageMgr);
	}
	
	/**
	 * 获得进程唯一标识
	 * @return
	 */
	public int getPid() {
		return mPid;
	}
	
	/**
	 * 获取是否在白名单中的
	 * @return
	 */
	public boolean isIgnore() {
		return mIsIgnore;
	}
	
	/**
	 * 设置进程唯一标识
	 * @param pId
	 */
	public void setPid(int pId) {
		mPid = pId;
	}
	
	/**
	 * 设置是否在白名单中的
	 */
	public void setIsIgnore(boolean isIgnore) {
		mIsIgnore = isIgnore;
	}

	/**
	 * 是否为暂存
	 * 
	 * @return 是否为暂存
	 */
	public boolean isTemp() {
		return mIsTemp;
	}

	// public boolean isPriority() {
	// return false;
	// }

	/**
	 * 设置是否是新安装的
	 */
	public void setIsNew(boolean isNew) {
		mIsNew = isNew;
	}

	public void setIsUpdate(boolean isUpdate) {
		mIsUpdate = isUpdate;
	}

	/**
	 * 设置是暂存的
	 * 
	 * @param isTemp
	 *            是否为暂存
	 */
	public void setIsTemp(boolean isTemp) {
		mIsTemp = isTemp;
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case AppItemInfo.INCONCHANGE :
				broadCast(INCONCHANGE, param, object, objects);
				return;
			case AppItemInfo.TITLECHANGE :
				broadCast(TITLECHANGED, param, object, objects);
				return;
			case AppItemInfo.UNREADCHANGE :
				setUnreadCount(param);
				broadCast(UNREADCHANGED, param, object, objects);
				return;
			case AppItemInfo.UNREADTYPECHANGE :
				setNotificationType(NotificationType.NOTIFICATIONTYPE_MORE_APP);
				broadCast(UNREADTYPECHANGED, param, object, objects);
				return;
			case AppItemInfo.IS_RECOMMEND_APP_CHANGE :
				if (object instanceof Boolean) {
					setIsNew((Boolean) object);
					broadCast(IS_RECOMMEND_APP_CHANGE, param, object, objects);
				}
			default :
				break;
		}
		// 直接转发
		super.broadCast(msgId, param, object, objects);
	}

	public boolean isNew() {
		return mIsNew;
	}

	public boolean isUpdate() {
		return mIsUpdate;
	}

	public long getTimeInFolder() {

		return mTimeInFolder;
	}

	public void setTimeInFolder(long mTimeInFolder) {
		this.mTimeInFolder = mTimeInFolder;
	}

	@Override
	public int getClickedCount(Context context) {
		if (mAppItemInfo == null) {
			return 0;
		}
		return mAppItemInfo.getActiveCount(context);
	}

	public long getInWhitchFolder() {
		return mInWhitchFolder;
	}

	public void setInWhitchFolder(long inWhitchFolder) {
		mInWhitchFolder = inWhitchFolder;
	}

	@Override
	public boolean isPriority() {
		return false;
	}

}

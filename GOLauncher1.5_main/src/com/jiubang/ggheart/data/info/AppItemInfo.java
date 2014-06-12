package com.jiubang.ggheart.data.info;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.common.bussiness.AppClassifyBussiness;
import com.jiubang.ggheart.data.statistics.StatisticsAppsInfoData;
import com.jiubang.ggheart.plugin.notification.NotificationType;

/**
 * 基础图标、部件信息
 * 
 * @author HuYong
 * @version 1.0
 */
public class AppItemInfo extends BaseItemInfo {
	// 消息id
	public static final int INCONCHANGE = 0;
	public static final int TITLECHANGE = 1;
	public static final int UNREADCHANGE = 2;
	public static final int UNREADTYPECHANGE = 3;
	// TODO:现在的id需要用一个文件统一保存起来，否则很多id重复使用
	public static final int IS_RECOMMEND_APP_CHANGE = 20;

	// item类型
	public long mID; // 部件标识ID
	public String mTitle = null; // 部件名称
	public Intent mIntent = null; // Intent
	public int mItemType; // 部件类型
	public String mProcessName = null; // 程序名
//	public int mWidgetID = -1; // widgetID
	public String mIconPackage = null;
	public String mIconResource = null;
	public BitmapDrawable mIcon = null;

	public Uri mUri = null;
	public int mDisplayMode;
//	public int mIsInnerAction; // 是否内部跳转动作

	private int mIsSysApp; // 是否是系统应用程序

	private boolean mIsTemp; // 是否是暂存的 TODO:放到FunAppItemInfo

	private int mNotificationType = NotificationType.IS_NOT_NOTIFICSTION; // 通讯统计应用类型
	private int mUnreadCount = 0;
	private long mInstalledTime;
	private int mActiveCount = -1;
	/**
	 * 是否新安装推荐应用，是则显示new标志
	 */
	public boolean mIsNewRecommendApp = false;
	/**
	 * 应用分类(对应编号查看xml配置)
	 */
	public int mClassification = AppClassifyBussiness.NO_CLASSIFY_APP;

//	public long getItemId() {
//		return mID;
//	}

	public AppItemInfo() {
		mID = System.currentTimeMillis();
//		mIsInnerAction = 0;
	}

	/**
	 * 是否为暂存
	 * 
	 * @return 是否为暂存
	 */
	public boolean isTemp() {
		return mIsTemp;
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

	/**
	 * 返回是否为系统应用程序，若是，则返回true，否则，返回false。
	 * 
	 * @author huyong
	 * @return
	 */
	public boolean getIsSysApp() {
		boolean result = false;
		if (mIsSysApp == 1) {
			result = true;
		}
		return result;
	}

	public void setIsSysApp(int isSysApp) {
		// 系统应用分类到一个值
		if (isSysApp == 1) {
			mClassification = AppClassifyBussiness.SYSTEM_APP;
		}
		this.mIsSysApp = isSysApp;
	}

	/**
	 * 将信息组装进values以便写到数据库中
	 * 
	 * @param values
	 */
	public void onAddToDatabase(ContentValues values) {
//		values.put(PartsTable.ID, mID);
//		values.put(PartsTable.TITLE, mTitle);
//		if (mIntent != null) {
//			values.put(PartsTable.INTENT, mIntent.toUri(0));
//		}
//		values.put(PartsTable.ITEMTYPE, mItemType);
//		values.put(PartsTable.WIDGETID, mWidgetID);
//		values.put(PartsTable.ICONPACKAGE, mIconPackage);
//		values.put(PartsTable.ICONRESOURCE, mIconResource);
//		writeBitmap(values, mIcon);
//		if (mUri != null) {
//			values.put(PartsTable.URI, mUri.toString());
//		}
//		values.put(PartsTable.DISPLAYMODE, mDisplayMode);
//		values.put(PartsTable.INNERACTION, mIsInnerAction);
	}

	protected void writeBitmap(ContentValues values, BitmapDrawable bitmapDrawable) {
//		if (bitmapDrawable != null) {
//			Bitmap bitmap = bitmapDrawable.getBitmap();
//			if (bitmap == null) {
//				return;
//			}
//			int size = bitmap.getWidth() * bitmap.getHeight() * 4;
//			ByteArrayOutputStream out = new ByteArrayOutputStream(size);
//			try {
//				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
//				out.flush();
//				out.close();
//				values.put(PartsTable.ICON, out.toByteArray());
//			} catch (IOException e) {
//				Log.w("Error", "Could not write icon");
//			}
//		}
	}

	public BitmapDrawable getIcon() {
		return mIcon;
	}

	public void setIcon(final BitmapDrawable icon) {
		if (icon == null || icon == this.mIcon) {
			return;
		}
		this.mIcon = icon;

		// 通知图标改变了
		broadCast(INCONCHANGE, 0, icon, null);
	}

	/**
	 * 获取名称
	 * 
	 * @return 名称
	 */
	public final String getTitle() {
		return mTitle;
	}

	public void setTitle(final String itemTitle) {
		if (itemTitle == null || itemTitle.equals(this.mTitle)) {
			return;
		}
		this.mTitle = itemTitle;
		broadCast(TITLECHANGE, 0, itemTitle, null);
	}

	/**
	 * 获取安装时间
	 * 
	 * @author huyong
	 * @param packageMgr
	 * @return
	 */
	public long getAppTime(PackageManager packageMgr) {
		if (mInstalledTime <= 0) {
			String sourceDir = null;
			try {
				sourceDir = packageMgr.getActivityInfo(mIntent.getComponent(), 0).applicationInfo.sourceDir;
				File file = new File(sourceDir);
				mInstalledTime = file.lastModified();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mInstalledTime;
	}

	/**
	 * 获取应用大小
	 * 
	 * @author kingyang
	 * @param packageMgr
	 * @return
	 */
	public long getAppSize(PackageManager packageMgr) {
		String publicSourceDir = null;
		try {
			publicSourceDir = packageMgr.getActivityInfo(mIntent.getComponent(), 0).applicationInfo.publicSourceDir;
			File file = new File(publicSourceDir);
			return file.length();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public String getAppPackageName() {
		return mIntent.getComponent().getPackageName();
	}

	public int getNotificationType() {
		return mNotificationType;
	}

	public void setNotificationType(int NotificationType) {
		mNotificationType = NotificationType;
		broadCast(UNREADTYPECHANGE, NotificationType, null, null);
	}

	public int getUnreadCount() {
		return mUnreadCount;
	}

	public void setUnreadCount(int UnreadCount) {
		mUnreadCount = UnreadCount;
		broadCast(UNREADCHANGE, UnreadCount, null, null);
	}

	public int getActiveCount(Context context) {
		if (mActiveCount < 0) {
			mActiveCount = StatisticsAppsInfoData.getAppClickedCount(mIntent, context);
		}
		return mActiveCount;
	}

	public void addActiveCount(Context context, int addCount) {
		if (mActiveCount < 0) {
			mActiveCount = StatisticsAppsInfoData.getAppClickedCount(mIntent, context);
		}
		mActiveCount += addCount;
	}
	
	public void setIsNewRecommendApp(boolean isRecommendApp) {
		mIsNewRecommendApp = isRecommendApp;
		broadCast(IS_RECOMMEND_APP_CHANGE, -1, isRecommendApp, null);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AppItemInfo) {
			AppItemInfo itemInfo = (AppItemInfo) o;
			return ConvertUtils.intentToStringCompare(this.mIntent, itemInfo.mIntent);
		}
		return super.equals(o);
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}
	
}

package com.jiubang.ggheart.apps.appfunc.business;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.go.util.DbUtil;
import com.jiubang.ggheart.apps.appfunc.component.RecentAppsIcon;
import com.jiubang.ggheart.apps.appfunc.controler.AppConfigControler;
import com.jiubang.ggheart.apps.appfunc.data.RecentAppDataModel;
import com.jiubang.ggheart.bussiness.BaseBussiness;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.tables.RecentAppTable;

/**
 * 
 * <br>类描述: 最近打开业务逻辑处理器
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-12-27]
 */
public class RecentAppBussiness extends BaseBussiness implements BroadCasterObserver {

	private RecentAppDataModel mDataModel;
	private ArrayList<AppItemInfo> mRecAppItems;
	private ArrayList<AppItemInfo> mRecAppItemsCopy;
	private int mAppListMaxLength = 24;

	public RecentAppBussiness(Context context) {
		super(context);
		mDataModel = new RecentAppDataModel(context);
		AppConfigControler.getInstance(mContext).registerObserver(this);
	}

	/**
	 * <br>功能简述: 获取
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public synchronized ArrayList<AppItemInfo> getRecentAppItems() {
		if (mRecAppItems == null) {
			initRecentAppItems();
		}
		return mRecAppItems;
	}

	private void initRecentAppItems() {
		Cursor cur = mDataModel.getRecentAppItems();
		if (cur != null) {
			try {
				mRecAppItems = new ArrayList<AppItemInfo>(cur.getCount());
				if (cur.moveToLast()) {
					do {
						String intentStr = DbUtil.getString(cur, RecentAppTable.INTENT);
						Intent intent = ConvertUtils.stringToIntent(intentStr);
						AppItemInfo appItemInfo = mAppDataEngine.getAppItemExceptHide(intent);
						if (appItemInfo != null) {
							mRecAppItems.add(appItemInfo);
						}
					} while (cur.moveToPrevious());
				}
			} finally {
				cur.close();
			}
		}
	}

	/**
	 * <br>功能简述: 添加最近打开数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param intent
	 * @param index
	 */
	@SuppressWarnings("unchecked")
	public synchronized void addRecentAppItem(final Intent intent) {
		if (intent == null) {
			return;
		}
		if (mRecAppItems == null) {
			initRecentAppItems();
		}
		AppItemInfo appItemInfo = mAppDataEngine.getAppItemExceptHide(intent);
		if (appItemInfo == null) {
			return;
		}
		
		try {
			mDataModel.beginTransaction();
			int count = 0;
			// 3.15版本，在最近打开Tab点击应用不刷新显示，退出再返回的时候再刷新
			// modify by wuziyi 2012.8.24
			// 内存中的
			if (RecentAppsIcon.sIsStartFromRencetTab) {
				if (mRecAppItemsCopy == null) {
					mRecAppItemsCopy = (ArrayList<AppItemInfo>) mRecAppItems.clone();
				}
				// 先清除该id在最近打开已存在的
				removeRecentAppItem(intent);
				mRecAppItemsCopy.add(0, appItemInfo);
				count = mRecAppItemsCopy.size();
			} else {
				// 先清除该id在最近打开已存在的
				removeRecentAppItem(intent);
				mRecAppItems.add(0, appItemInfo);
				count = mRecAppItems.size();
			}
			// 添加到数据库(第二个参数保留，可用于管理顺序)
			mDataModel.addRecentAppItem(intent, 0);

			if (count > mAppListMaxLength) {
				// 删除最后一条记录
				// 内存中的
				AppItemInfo removeInfo = null;
				if (RecentAppsIcon.sIsStartFromRencetTab) {
					removeInfo = mRecAppItemsCopy.remove(count - 1);
				} else {
					removeInfo = mRecAppItems.remove(count - 1);
				}

				if (removeInfo != null) {
					// 数据库中的
					Intent removeIntent = removeInfo.mIntent;
					mDataModel.removeRecentAppItem(removeIntent);
				}
			}
			mDataModel.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				mDataModel.endTransaction();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * <br>功能简述: 删除最近打开数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param intent
	 */
	public synchronized void removeRecentAppItem(final Intent intent) {
		if (intent == null) {
			return;
		}
		if (!RecentAppsIcon.sIsStartFromRencetTab) {
			if (mRecAppItems != null) {
				for (AppItemInfo appItemInfo : mRecAppItems) {
					if (ConvertUtils.intentCompare(appItemInfo.mIntent, intent)) {
						mRecAppItems.remove(appItemInfo);
						break;
					}
				}
			}
		} else {
			if (mRecAppItemsCopy != null) {
				for (AppItemInfo appItemInfo : mRecAppItemsCopy) {
					if (ConvertUtils.intentCompare(appItemInfo.mIntent, intent)) {
						mRecAppItemsCopy.remove(appItemInfo);
						break;
					}
				}
			}
		}

		mDataModel.removeRecentAppItem(intent);
	}

	/**
	 * <br>功能简述: 删除所有最近打开数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public synchronized void removeAllRecentAppItems() {
		if (mRecAppItemsCopy != null) {
			mRecAppItemsCopy.clear();
			mRecAppItemsCopy = null;
		}
		// 复位标记
		RecentAppsIcon.sIsStartFromRencetTab = false;

		if (mRecAppItems != null) {
			mRecAppItems.clear();
		}
		mDataModel.removeAllRecentAppItems();
	}

	/**
	 * 处理卸载
	 * 
	 * @param appItemInfos
	 */
	public void handleUnInstall(final ArrayList<AppItemInfo> appItemInfos) {
		if (null == appItemInfos) {
			return;
		}

		// 批量删除
		AppItemInfo appItemInfo = null;
		int size = appItemInfos.size();
		for (int i = 0; i < size; ++i) {
			appItemInfo = appItemInfos.get(i);
			if (appItemInfo == null) {
				continue;
			}
			removeRecentAppItem(appItemInfo.mIntent);
		}
	}

	/**
	 * 设置最近打开程序节点信息列表最大长度
	 * 
	 * @param appListMaxLenth
	 *            最近打开程序节点信息列表最大长度
	 * 
	 */
	public void setAppListMaxLenth(int appListMaxLenth) {
		mAppListMaxLength = appListMaxLenth;
	}

	public synchronized void refreshItemsFromCopy() {
		if (mRecAppItemsCopy != null && RecentAppsIcon.sIsStartFromRencetTab) {
			if (mRecAppItems != null) {
				mRecAppItems.clear();
				mRecAppItems.addAll(mRecAppItemsCopy);
			} else {
				mRecAppItems = new ArrayList<AppItemInfo>(mRecAppItemsCopy);
			}
			mRecAppItemsCopy.clear();
			mRecAppItemsCopy = null;
			RecentAppsIcon.sIsStartFromRencetTab = false;
		}
	}

	@Override
	public synchronized void cleanup() {
		super.cleanup();
		if (mRecAppItems != null) {
			mRecAppItems.clear();
		}
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case AppConfigControler.ADDHIDEITEMS :
				mRecAppItems = null;
				mRecAppItemsCopy = null;
				initRecentAppItems();
				break;

			default :
				break;
		}
		
	}
}

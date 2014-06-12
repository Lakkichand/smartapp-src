package com.jiubang.ggheart.data;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.data.info.SysShortCutItemInfo;
import com.jiubang.ggheart.data.model.SysShortCutDataModel;
import com.jiubang.ggheart.launcher.ICustomAction;


public class SysShortCutControler extends Controler implements ICleanable {
	private String LOG_TAG = "sysshortcut_controler";

	private SysShortCutDataModel mDataModel;
	private ArrayList<SysShortCutItemInfo> mSysShortCuts;

	public SysShortCutControler(Context context) {
		super(context);

		mDataModel = new SysShortCutDataModel(context);
		try {
			mSysShortCuts = mDataModel.getSysShortCutRecords();
		} catch (Exception e) {
			mSysShortCuts = new ArrayList<SysShortCutItemInfo>();
			Log.i(LOG_TAG, "get system shortcut records exception");
		}
	}

	public synchronized boolean addSysShortCut(Intent intent, String name, BitmapDrawable icon) {
		boolean bRet = false;

		SysShortCutItemInfo info = getSysShortCutItemInfo(intent);
		if (null == info) {
			try {
				// 加记录
				mDataModel.addSysShortCutRecord(intent, name, icon);

				// 加缓存
				info = new SysShortCutItemInfo();
				info.mIntent = intent;
				info.mTitle = name;
				info.mIcon = icon;
				info.mRefCount = 1;
				addSysShortCutOriginCache(info);

				bRet = true;
			} catch (Exception e) {
				Log.i(LOG_TAG, "add system shortcut record exception");
			}
		} else {
			try {
				// 之前版本在dock显示上漏了屏幕预览的图片，所以补上
				if (intent.getAction().equals(ICustomAction.ACTION_SHOW_PREVIEW)) {
					info.mIcon = icon;
				}
				// 更新引用计数
				mDataModel.updateSysShortCutRefCount(intent, info.mRefCount + 1);

				// 更新缓存引用计数
				info.mRefCount += 1;

				bRet = true;
			} catch (Exception e) {
				Log.i(LOG_TAG, "update system shortcut record refrence++ exception");
			}
		}

		return bRet;
	}

	public synchronized boolean delSysShortCut(Intent intent) {
		boolean bRet = false;

		SysShortCutItemInfo info = getSysShortCutItemInfo(intent);
		if (null == info) {
			Log.i(LOG_TAG, "can not find delete system shortcut cache");
		} else {
			if (1 == info.mRefCount) {
				try {
					// 消记录
					mDataModel.delSysShortCutRecord(intent);

					// 消缓存
					removeSysShortCutOriginCache(info);

					bRet = true;
				} catch (Exception e) {
					Log.i(LOG_TAG, "delete system shortcut record exception");
				}

			} else {
				try {
					// 更新引用计数
					mDataModel.updateSysShortCutRefCount(intent, info.mRefCount - 1);

					// 更新缓存引用计数
					info.mRefCount -= 1;

					bRet = true;
				} catch (Exception e) {
					Log.i(LOG_TAG, "update system shortcut record refrence-- exception");
				}
			}
		}

		return bRet;
	}

	/**
	 * 加入系统快捷方式缓存
	 * 
	 * @param appInfo
	 */
	private void addSysShortCutOriginCache(SysShortCutItemInfo appInfo) {
		mSysShortCuts.add(appInfo);
	}

	/**
	 * 移除系统快捷方式的缓存
	 * 
	 * @param appInfo
	 */
	private void removeSysShortCutOriginCache(SysShortCutItemInfo appInfo) {
		mSysShortCuts.remove(appInfo);
	}

	/**
	 * 关联源 只读接口，不可修改数据
	 * 
	 * @return
	 */
	public synchronized SysShortCutItemInfo getSysShortCutItemInfo(Intent intent) {
		if (null == mSysShortCuts) {
			return null;
		}

		SysShortCutItemInfo retInfo = null;
		int sz = mSysShortCuts.size();
		for (int i = 0; i < sz; i++) {
			SysShortCutItemInfo info = mSysShortCuts.get(i);
			if (null == info) {
				continue;
			}
			if (info.isEqual(intent)) {
				retInfo = info;
				break;
			}
		}

		return retInfo;
	}

	@Override
	public void cleanup() {
		clearAllObserver();
		cleanSysShortCuts();
	}

	/**
	 * <br>功能简述:清除、释放列表mSysShortCuts
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private synchronized void cleanSysShortCuts() {
		mSysShortCuts.clear();
	}
}

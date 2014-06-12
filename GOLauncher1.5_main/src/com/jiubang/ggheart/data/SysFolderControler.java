package com.jiubang.ggheart.data;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;

import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.data.info.SysFolderItemInfo;
import com.jiubang.ggheart.data.model.SysFolderDataModel;

public class SysFolderControler extends Controler implements ICleanable {
	private String LOG_TAG = "sysshortcut_controler";

	private SysFolderDataModel mDataModel;
	private ArrayList<SysFolderItemInfo> mSysFolders;

	public SysFolderControler(Context context) {
		super(context);

		mDataModel = new SysFolderDataModel(context);
		try {
			mSysFolders = mDataModel.getSysFolderRecords();
		} catch (Exception e) {
			mSysFolders = new ArrayList<SysFolderItemInfo>();
			Log.i(LOG_TAG, "get system folder records exception");
		}
	}

	/**
	 * 只读接口，不可修改数据
	 * 
	 * @return
	 */
	public ArrayList<SysFolderItemInfo> getSysFolderInfos() {
		return mSysFolders;
	}

	public synchronized boolean addSysFolder(Intent intent, Uri uri, int displayMode, String name,
			BitmapDrawable icon) {
		boolean bRet = false;

		SysFolderItemInfo info = getSysFolderItemInfo(intent, uri);
		if (null == info) {
			try {
				// 加记录
				mDataModel.addSysFolderRecord(intent, uri, displayMode, name, icon);

				// 加缓存
				info = new SysFolderItemInfo();
				info.mIntent = intent;
				info.mUri = uri;
				info.mTitle = name;
				info.mIcon = icon;
				info.mRefCount = 1;
				addSysFolderOriginCache(info);

				bRet = true;
			} catch (Exception e) {
				Log.i(LOG_TAG, "add system folder record exception");
			}
		} else {
			try {
				// 更新引用计数
				mDataModel.updateSysFolderRefCount(intent, uri, info.mRefCount + 1);

				// 更新缓存引用计数
				info.mRefCount += 1;

				bRet = true;
			} catch (Exception e) {
				Log.i(LOG_TAG, "update system folder record refrence++ exception");
			}

		}

		return bRet;
	}

	public synchronized boolean delSysFolder(Intent intent, Uri uri) {
		boolean bRet = false;

		SysFolderItemInfo info = getSysFolderItemInfo(intent, uri);
		if (null == info) {
			Log.i(LOG_TAG, "can not find delete system folder cache");
		} else {
			if (1 == info.mRefCount) {
				try {
					// 消记录
					mDataModel.delSysFolderRecord(intent, uri);

					// 消缓存
					removeSysFolderOriginCache(info);

					bRet = true;
				} catch (Exception e) {
					Log.i(LOG_TAG, "delete system folder record exception");
				}
			} else {
				try {
					// 更新应用计数
					mDataModel.updateSysFolderRefCount(intent, uri, info.mRefCount - 1);

					// 更新缓存引用计数
					info.mRefCount -= 1;

					bRet = true;
				} catch (Exception e) {
					Log.i(LOG_TAG, "update system folder record refrence-- exception");
				}
			}
		}

		return bRet;
	}

	/**
	 * 加入系统文件夹缓存
	 * 
	 * @param appInfo
	 */
	private void addSysFolderOriginCache(SysFolderItemInfo appInfo) {
		mSysFolders.add(appInfo);
	}

	/**
	 * 移除系统文件夹得缓存
	 * 
	 * @param appInfo
	 */
	private void removeSysFolderOriginCache(SysFolderItemInfo appInfo) {
		mSysFolders.remove(appInfo);
	}

	/**
	 * 关联源 只读接口，不可修改数据
	 * 
	 * @return
	 */
	public SysFolderItemInfo getSysFolderItemInfo(Intent intent, Uri uri) {
		SysFolderItemInfo retInfo = null;

		if (null == mSysFolders) {
			return retInfo;
		}

		int sz = mSysFolders.size();
		for (int i = 0; i < sz; i++) {
			SysFolderItemInfo info = mSysFolders.get(i);
			if (null == info) {
				continue;
			}
			if (info.isEqual(intent, uri)) {
				retInfo = info;
				break;
			}
		}

		return retInfo;
	}

	@Override
	public void cleanup() {
		clearAllObserver();
		mSysFolders.clear();
	}
}

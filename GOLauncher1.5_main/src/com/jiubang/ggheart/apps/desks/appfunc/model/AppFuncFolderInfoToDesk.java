package com.jiubang.ggheart.apps.desks.appfunc.model;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class AppFuncFolderInfoToDesk implements Parcelable {
	public long folderId;
	public String name;
	public ArrayList<Intent> intentList;

	public AppFuncFolderInfoToDesk(FunFolderItemInfo info) {
		folderId = info.getFolderId();
		name = info.getTitle();
		intentList = new ArrayList<Intent>();
		ArrayList<FunAppItemInfo> appInfos = info.getFunAppItemInfos();
		if (appInfos != null) {
			for (FunAppItemInfo appInfo : appInfos) {
				intentList.add(appInfo.getIntent());
			}
		}
	}

	private AppFuncFolderInfoToDesk(Parcel src) {
		folderId = src.readLong();
		name = src.readString();
		intentList = src.readArrayList(Intent.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(folderId);
		dest.writeString(name);
		dest.writeList(intentList);
	}

	public static final Parcelable.Creator<AppFuncFolderInfoToDesk> CREATOR = new Parcelable.Creator<AppFuncFolderInfoToDesk>() {
		@Override
		public AppFuncFolderInfoToDesk createFromParcel(Parcel source) {
			return new AppFuncFolderInfoToDesk(source);
		}

		@Override
		public AppFuncFolderInfoToDesk[] newArray(int size) {
			return new AppFuncFolderInfoToDesk[size];
		}
	};

	public UserFolderInfo toUserFolderInfo() {
		UserFolderInfo folderInfo = new UserFolderInfo();
		folderInfo.mInScreenId = -1;
		folderInfo.mItemType = IItemType.ITEM_TYPE_USER_FOLDER;
		folderInfo.mRefId = folderId;
		folderInfo.mTitle = name;
		folderInfo.mSpanX = 1;
		folderInfo.mSpanY = 1;
		if (null != intentList) {
			AppDataEngine dataEngine = GOLauncherApp.getAppDataEngine();
			if (null == dataEngine) {
				return folderInfo;
			}
			int sz = intentList.size();
			for (int i = 0; i < sz; i++) {
				AppItemInfo info = dataEngine.getAppItem(intentList.get(i));
				if (null == info) {
					continue;
				}
				ShortCutInfo shortCutInfo = new ShortCutInfo();
				shortCutInfo.mIcon = info.mIcon;
				shortCutInfo.mIntent = info.mIntent;
				shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
				shortCutInfo.mSpanX = 1;
				shortCutInfo.mSpanY = 1;
				shortCutInfo.mTitle = info.mTitle;
				shortCutInfo.mTimeInFolder = System.currentTimeMillis();
				shortCutInfo.mCounter = info.getUnreadCount();
				shortCutInfo.setRelativeItemInfo(info);
				folderInfo.add(shortCutInfo);
			}
		}
		return folderInfo;
	}
}

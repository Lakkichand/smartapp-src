package com.jiubang.ggheart.plugin.shell.folder;

import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-2-21]
 */
public class GLAppFolderInfo {
	public static final int FOLDER_FROM_SCREEN = 0x1;
	public static final int FOLDER_FROM_APPDRAWER = 0x2;
	public static final int FOLDER_FROM_DOCK = 0x3;
	public final int folderFrom;
	public final long folderId;
	private UserFolderInfo mScreenFoIderInfo;
	private FunFolderItemInfo mAppDrawerFolderInfo;

	public GLAppFolderInfo(UserFolderInfo screenFolderInfo, int from) {
		folderId = screenFolderInfo.mInScreenId;
		mScreenFoIderInfo = screenFolderInfo;
		folderFrom = from;
	}

	public GLAppFolderInfo(FunFolderItemInfo appDrawerFolderInfo) {
		folderId = appDrawerFolderInfo.getFolderId();
		mAppDrawerFolderInfo = appDrawerFolderInfo;
		folderFrom = FOLDER_FROM_APPDRAWER;
	}
	public UserFolderInfo getScreenFoIderInfo() {
		return mScreenFoIderInfo;
	}

	public FunFolderItemInfo getAppDrawerFolderInfo() {
		return mAppDrawerFolderInfo;
	}

}

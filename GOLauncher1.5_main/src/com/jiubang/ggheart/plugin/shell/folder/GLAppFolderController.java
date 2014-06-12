package com.jiubang.ggheart.plugin.shell.folder;

import java.util.ArrayList;
import java.util.HashMap;

import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.data.Controler;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:文件夹逻辑操作控制
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2013-2-21]
 */
public class GLAppFolderController extends Controler implements ICleanable {
	private HashMap<Long, GLAppFolderInfo> mAllFoldersContent;
	private static volatile GLAppFolderController sInstance;
	private GLAppDrawerFolderBussiness mDrawerFolderBussiness;
	private GLAppFolderController() {
		super(GOLauncherApp.getContext());
		mAllFoldersContent = new HashMap<Long, GLAppFolderInfo>();
		mDrawerFolderBussiness = new GLAppDrawerFolderBussiness();
	}
	public static GLAppFolderController getInstance() {
		if (sInstance == null) {
			sInstance = new GLAppFolderController();
		}
		return sInstance;
	}

	public void addFolderInfo(GLAppFolderInfo folderInfo) {
		if (!mAllFoldersContent.containsKey(folderInfo.folderId)) {
			mAllFoldersContent.put(folderInfo.folderId, folderInfo);
			switch (folderInfo.folderFrom) {
				case GLAppFolderInfo.FOLDER_FROM_APPDRAWER :
					FunFolderItemInfo appdrawerFolderInfo = folderInfo.getAppDrawerFolderInfo();
					ArrayList<FunAppItemInfo> funAppItemInfos = mDrawerFolderBussiness
							.getFolderContentFromDB(appdrawerFolderInfo);
					appdrawerFolderInfo.setFolderContent(funAppItemInfos);
					break;
				case GLAppFolderInfo.FOLDER_FROM_DOCK :
					
					break;
				case GLAppFolderInfo.FOLDER_FROM_SCREEN :
					
					break;
				default :
					break;
			}
		}
	}

	public GLAppFolderInfo getFolderInfoById(long folderId) {
		return mAllFoldersContent.get(folderId);

	}
	
	/**
	 * <br>功能简述:新建功能表文件夹
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param folderInfo
	 */
	public void createAppDrawerFolder(ArrayList<FunAppItemInfo> itemInfos, String folderName) {
		if (itemInfos == null) {
			return;
		}
		int location = 0;
		createAppDrawerFolder(itemInfos, folderName, location);
	}

	/**
	 * <br>功能简述:新建功能表文件夹
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param folderInfo
	 */
	public void createAppDrawerFolder(ArrayList<FunAppItemInfo> itemInfos, String folderName, int location) {
		if (itemInfos == null) {
			return;
		}
		//在内存中准备文件夹的数据。
		FunFolderItemInfo folderItemInfo = new FunFolderItemInfo(folderName);
		addFolderInfo(new GLAppFolderInfo(folderItemInfo));
		int startIndex = folderItemInfo.getFolderSize();
		folderItemInfo.addFolderContentBatch(folderItemInfo.getFolderSize(), itemInfos);
		//写数据库
		mDrawerFolderBussiness.createFolderInAppDrawer(folderItemInfo, startIndex, location);
	}
	
	public void addInfoAppDrawerFolder(FunFolderItemInfo folderInfo, FunAppItemInfo appInfo) {
		folderInfo.addFolderContent(folderInfo.getFolderSize(), appInfo);
		mDrawerFolderBussiness.addFunAppToFolder(folderInfo, appInfo);
	}
	
	/**
	 * <br>功能简述:新建桌面文件夹
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param folderInfo
	 */
	public void createScreenFolder(GLAppFolderInfo folderInfo) {

	}
	/**
	 * <br>功能简述:新建Dock文件夹
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param folderInfo
	 */
	public void createDockFolder(GLAppFolderInfo folderInfo) {

	}
	@Override
	public void cleanup() {

	}
}

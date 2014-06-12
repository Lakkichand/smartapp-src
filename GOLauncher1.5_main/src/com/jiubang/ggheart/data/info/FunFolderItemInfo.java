package com.jiubang.ggheart.data.info;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;

import com.go.util.ConvertUtils;
import com.go.util.SortUtils;
import com.jiubang.ggheart.apps.appfunc.controler.AppConfigControler;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.model.FunDataModel;

/**
 * 文件夹信息基类
 * @author yangguanxiang
 *
 */
public class FunFolderItemInfo extends FunItemInfo {
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_NEW_FOLDER = 1;
	public static final int TYPE_GAME = 2;
	public static final int TYPE_SOCIAL = 3;
	public static final int TYPE_SYSTEM = 4;
	public static final int TYPE_TOOL = 5;

	public static final int ADDITEM = END;
	public static final int REMOVEITEM = END + 1;
	public static final int ADDITEM_INMOVE = END + 2;
	public static final int REMOVEITEM_INMOVE = END + 3;
	public static final int TITLECHANGED = END + 4;
	public static final int INCONCHANGE = END + 5;
	public static final int SORTFINISH = END + 6; // 排序完成
	public static final int SORTFAILED = END + 7; // 排序失败

	private long mFolderId; // 文件夹id：若为0，则为应用程序；否则为文件夹
	private String mFolderTitle; // 文件夹名称 TODO:封装接口
//	private String mIconPath; // 文件夹图标路径

	private boolean mfolderchange;

	private boolean mfolderfirstcreate;

	// 文件夹中的元素
	private ArrayList<FunAppItemInfo> mFunAppItemInfos = null;
	// 是否已初始化
	private volatile boolean mHasInit = false;

	private int mFolderType = TYPE_NORMAL;

	private FunDataModel mFunDataModel;

	
	/**
	 * 文件夹构造
	 * 
	 * @param folderTitle
	 * @param iconPath
	 */
	public FunFolderItemInfo(FunDataModel funDataModel, String folderTitle) {
		mType = TYPE_FOLDER;
		mFunDataModel = funDataModel;
		// 若是文件夹，给予唯一的intent
		String str = Long.toString(System.currentTimeMillis());
		mIntent = new Intent(str);
		// 产生唯一的文件夹id
		mFolderId = System.currentTimeMillis();
		mFolderTitle = folderTitle;
		if (null == mFolderTitle) {
			mFolderTitle = "Folder Name";
		}

		mFunAppItemInfos = new ArrayList<FunAppItemInfo>();
	}

	public FunFolderItemInfo(String folderTitle) {
		this(null, folderTitle);
	}

	public FunFolderItemInfo(FunDataModel funDataModel, String folderTitle, int folderType) {
		this(funDataModel, folderTitle);
		mFolderType = folderType;
	}

	public FunFolderItemInfo(String folderTitle, int folderType) {
		this(null, folderTitle, folderType);
	}

	/**
	 * 清除原来的排序
	 */
	private void refreshIndex() {
		FunItemInfo funItemInfo = null;
		for (int i = 0; i < mFunAppItemInfos.size(); ++i) {
			funItemInfo = mFunAppItemInfos.get(i);
			if (null == funItemInfo) {
				continue;
			}

			funItemInfo.setIndex(i);
		}
	}

	/**
	 * 按时间排序，并保存到数据库
	 * 
	 * @param order
	 */
	public synchronized boolean sortByTimeAndSave(Context context, String order) {
		if (!mHasInit) {
			return false;
		}

		// LogUnit.i("FunFolderItemInfo", "sortByTimeAndSave()");
		boolean ret = true;
		// 排序
		try {
			sortByTime(context, order);
		} catch (Exception e) {
			ret = false;
		}
		// 清除原来的排序
		refreshIndex();
		// 保存排序
		mFunDataModel.updateFunAppItemsIndexInFolder(mFolderId, mFunAppItemInfos);
		// 通知
		return ret;
	}

	/**
	 * 按字母排序，并保存到数据库
	 * 
	 * @param order
	 */
	public synchronized boolean sortByLetterAndSave(String order) {
		if (!mHasInit) {
			return false;
		}

		// LogUnit.i("FunFolderItemInfo", "sortByLetterAndSave()");
		boolean ret = true;
		// 排序
		try {
			sortByLetter(order);
		} catch (Exception e) {
			ret = false;
		}
		// 清除原来的排序
		refreshIndex();
		// 保存排序
		mFunDataModel.updateFunAppItemsIndexInFolder(mFolderId, mFunAppItemInfos);
		// 通知
		return ret;
	}

	private void sortByLetter(String order) {
		String sortMethod = "getTitle";
		SortUtils.sort(mFunAppItemInfos, sortMethod, null, null, order);
	}

	private void sortByTime(Context context, final String order) {
		String sortMethod = "getTimeInFolder";
		// PackageManager packageMgr = context.getPackageManager();
		// Class[] methodArgClasses = new Class[] { PackageManager.class };
		// Object[] methodArg = new Object[] { packageMgr };
		// mFunAppItemInfos.
		mHasInit = false;
		mFunAppItemInfos = getFunAppItemInfos();
		SortUtils.sortByLong(mFunAppItemInfos, sortMethod, null, null, order);
	}

	public boolean isMfolderchange() {
		return mfolderchange;
	}

	public void setMfolderchange(boolean mfolderchange) {
		this.mfolderchange = mfolderchange;
	}

	public boolean isMfolderfirstcreate() {
		return mfolderfirstcreate;
	}

	public void setMfolderfirstcreate(boolean mfolderfirstcreate) {
		this.mfolderfirstcreate = mfolderfirstcreate;
	}

	/**
	 * 获取文件夹中所有程序列表, 后台不维护该列表TODO:重命名为getFunAppItems
	 * 
	 * @param folderId
	 * @return
	 */
	public final ArrayList<FunAppItemInfo> getFunAppItemInfos() {
		if (mHasInit) {
			return mFunAppItemInfos;
		}
		mFunAppItemInfos = mFunDataModel.getAppsInFolder(mFolderId, false, this);
		mHasInit = true;
		return mFunAppItemInfos;
	}

	/**
	 * 获取文件夹中没有被隐藏的程序列表, 后台不维护该列表
	 * 
	 * @param folderId
	 * @return
	 */
	public final ArrayList<FunAppItemInfo> getFunAppItemInfosForShow() {
		ArrayList<FunAppItemInfo> appList = getFunAppItemInfos();
		ArrayList<FunAppItemInfo> appListExceptHide = new ArrayList<FunAppItemInfo>();
		for (FunAppItemInfo app : appList) {
			if ((app != null) && (!app.isHide())) {
				appListExceptHide.add(app);
			}
		}
		return appListExceptHide;
	}

	/**
	 * 取得文件夹中第index个元素TODO:放到文件夹 TODO:重命名为getFunAppItem
	 * 
	 * @param folderId
	 *            文件夹id
	 * @param index
	 *            下标
	 * @return
	 */
	public FunAppItemInfo getFunAppItemInFolder(final int index) {
		// 从内存中获取
		if (mHasInit) {
			int size = mFunAppItemInfos.size();
			int idx = (index > size - 1) ? size - 1 : index;
			return mFunAppItemInfos.get(idx);
		}
		// 从数据库中获取
		return mFunDataModel.getFunAppItemInFolder(mFolderId, index);
	}

	/**
	 * 取得文件夹中对应的元素 TODO:修改命名 与findInList重复了
	 * 
	 * @param intent
	 *            唯一标识Intent
	 * @return 对应元素
	 */
	public int getFunAppItem(final Intent intent) {
		// 从内存中获取
		if (mHasInit) {
			FunAppItemInfo funAppItemInfo = null;
			int size = mFunAppItemInfos.size();
			for (int i = 0; i < size; ++i) {
				funAppItemInfo = mFunAppItemInfos.get(i);
				if (null == funAppItemInfo) {
					continue;
				}

				if (ConvertUtils.intentCompare(intent, funAppItemInfo.getIntent())) {
					return i;
				}
			}
			return -1;
		}
		// 从数据库中获取
		return mFunDataModel.getAppItemIndexInFolder(mFolderId, intent);
	}

	/**
	 * 设置文件夹中元素是否隐藏
	 * 
	 * @param intent
	 * @param hide
	 */
	public boolean setHideFunAppItemInfo(final Intent intent, final boolean hide, Context context) {
		int idx = findInList(intent);
		if (idx < 0) {
			return false;
		}

		FunAppItemInfo funAppItemInfo = getFunAppItemInFolder(idx);
		if (null == funAppItemInfo) {
			return false;
		}
		
		funAppItemInfo.setHideInfo(AppConfigControler.getInstance(context).getHideInfo(funAppItemInfo.getIntent()));
		return true;
	}

	/**
	 * 获取FolderId
	 * 
	 * @return FolderId
	 */
	@Override
	public long getFolderId() {
		return mFolderId;
	}

	/**
	 * 设置文件夹id
	 * 
	 * @param folderId
	 *            id
	 */
	public void setFolderId(long folderId) {
		mFolderId = folderId;
	}

	/**
	 * 获取名称
	 * 
	 * @return 名称
	 */
	@Override
	public String getTitle() {
		return mFolderTitle;
	}

	// public boolean isPriority() {
	// return true;
	// }

	/**
	 * 设置文件夹名称
	 * 
	 * @param folderTitle
	 *            文件夹名称
	 * @throws DatabaseException
	 */
	public void setTitle(String folderName) throws DatabaseException {
		mFolderTitle = folderName;
		// 更新数据库
		mFunDataModel.updateFunAppItem(mFolderId, folderName);
		broadCast(TITLECHANGED, 0, folderName, null);
	}

	/**
	 * 获取时间
	 * 
	 * @param packageMgr
	 * @return
	 */
	@Override
	public long getTime(PackageManager packageMgr) {
		return mFolderId;
	}

	/**
	 * 移动文件夹中的元素(业务层逻辑)
	 * 
	 * @param folderId
	 *            文件id
	 * @param resIndex
	 *            源位置
	 * @param tarindex
	 *            目标位置
	 * @param funItemInfo
	 *            移动的元素
	 * @throws DatabaseException
	 */
	public void moveFunAppItem(final int resIndex, final int tarIndex) throws DatabaseException {
		// 缓存要删除的元素
		FunAppItemInfo funItemInfo = getFunAppItemInFolder(resIndex);
		// 删除元素
		removeFunAppItemInfo(funItemInfo, false);
		broadCast(REMOVEITEM_INMOVE, 0, funItemInfo, null);
		// 插入元素
		addFunAppItemInfo(tarIndex, funItemInfo, false, true);
		broadCast(ADDITEM_INMOVE, tarIndex, funItemInfo, null);
	}

	/**
	 * 移动文件夹中的元素(业务层逻辑)
	 * 
	 * @param folderId
	 *            文件id
	 * @param resIndex
	 *            源位置
	 * @param tarindex
	 *            目标位置
	 * @param funItemInfo
	 *            移动的元素
	 * @throws DatabaseException
	 */
	public boolean moveFunAppItem2(final int resIndex, final int tarIndex) {
		// 移动后就把排序规则清除掉。
		SharedPreferences preferences = GoLauncher.getContext()
				.getPreferences(Context.MODE_PRIVATE);
		Editor editor = preferences.edit().remove(String.valueOf(mFolderId));
		editor.commit();
		// 缓存要删除的元素
		FunAppItemInfo funItemInfo = getFunAppItemInFolder(resIndex);
		if (null == funItemInfo) {
			return false;
		}

		// 操作数据库
		try {
			mFunDataModel.beginTransaction();
			if (mFunDataModel.moveFolderItem(mFolderId, resIndex, tarIndex)) {
				// 数据库操作成功才操作内存
				// 删除元素
				removeFunAppItemInfo(funItemInfo.getAppItemInfo().mIntent, false, false);
				broadCast(REMOVEITEM_INMOVE, 0, funItemInfo, null);
				// 插入元素
				addFunAppItemInfo(tarIndex, funItemInfo, false, false);
				mFunDataModel.setTransactionSuccessful();
				broadCast(ADDITEM_INMOVE, tarIndex, funItemInfo, null);
				return true;
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			mFunDataModel.endTransaction();
		}
		return false;
	}

	/**
	 * 从文件夹中删除一个元素 TODO:优化此函数，针对引用
	 * 
	 * @param folderId
	 *            文件id
	 * @param funItemInfo
	 *            要删除的元素
	 * @throws DatabaseException
	 */
	public FunAppItemInfo removeFunAppItemInfo(FunAppItemInfo funItemInfo, boolean notify)
			throws DatabaseException {
		if (null == funItemInfo) {
			return null;
		}

		// 从内存及数据库中删除
		if (null == funItemInfo.getAppItemInfo()) {
			return null;
		}
		removeFunAppItemInfo(funItemInfo.getAppItemInfo().mIntent, notify, true);
		return funItemInfo;
	}

	/**
	 * 根据唯一标识Intent删除(并维护顺序)
	 * 
	 * @param intent
	 *            唯一标识Intent
	 * @return 被删元素
	 * @throws DatabaseException
	 */
	public synchronized FunAppItemInfo removeFunAppItemInfo(final Intent intent, boolean notify,
			boolean handleDB) throws DatabaseException {
		if (null == intent) {
			return null;
		}

		int idx = -1;
		FunAppItemInfo tempInfo = null;
		if (mHasInit) {
			idx = findInList(intent);
			if (idx >= 0) {
				// 从内存中删除
				tempInfo = mFunAppItemInfos.remove(idx);
				mUnreadCount -= tempInfo.getUnreadCount();
				// 更新mIndex
				FunItemInfo info = null;
				for (int i = idx; i < mFunAppItemInfos.size(); ++i) {
					info = mFunAppItemInfos.get(i);
					info.setIndex(info.getIndex() - 1);
				}
			}
		}

		if (handleDB) {
			// 从数据库中删除
			mFunDataModel.removeFunAppFromFolder(mFolderId, intent);
		}

		// 通知
		if (notify) {
			broadCast(REMOVEITEM, idx, tempInfo, null);
		}
		return tempInfo;
	}

	/**
	 * 找到对应元素的下标, 找内存中的
	 * 
	 * @param funItemInfo
	 *            元素
	 * @return 下标
	 */
	public int findInList(final FunAppItemInfo funItemInfo) {
		if (mHasInit) {
			FunAppItemInfo info = null;
			for (int i = 0; i < mFunAppItemInfos.size(); ++i) {
				info = mFunAppItemInfos.get(i);
				if (funItemInfo == info) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 找到对应Intent的下标, 先找内存中的, 再找数据库中的
	 * 
	 * @param intent
	 *            Intent
	 * @return 下标
	 */
	public int findInList(final Intent intent) {
		if (mHasInit) {
			// 找内存中的
			String str1 = null;
			String str2 = null;
			AppItemInfo appItemInfo = null;
			FunAppItemInfo info = null;
			for (int i = 0; i < mFunAppItemInfos.size(); ++i) {
				info = mFunAppItemInfos.get(i);
				appItemInfo = info.getAppItemInfo();
				if (null == appItemInfo) {
					continue;
				}

				str1 = ConvertUtils.intentToString(intent);
				str2 = ConvertUtils.intentToString(appItemInfo.mIntent);
				if (null == str1 || null == str2) {
					continue;
				}

				if (0 == str1.compareTo(str2)) {
					return i;
				}
			}
			return -1;
		}

		// 找数据库中的
		return mFunDataModel.getAppItemIndexInFolder(mFolderId, intent);
	}

	/**
	 * 获取文件夹中图标个数
	 * 
	 * @return 图标个数
	 */
	public final int getSize() {
		if (mHasInit) {
			return mFunAppItemInfos.size();
		}
		return mFunDataModel.getSizeOfFolder(mFolderId);
	}

	/**
	 * 在文件夹的index位置添加元素，若文件夹没有index位置，则添加到末尾 TODO:放到文件夹
	 * 调用前最好先判断文件夹内是否有重复
	 * 
	 * @param folderId
	 *            文件id
	 * @param index
	 *            添加的位置
	 * @param funItemInfo
	 *            要添加的元素
	 * @return 返回true表示加入成功，返回false表示失败（可能是传入数据有问题）
	 * 
	 * @throws DatabaseException
	 * 
	 * 
	 */
	public synchronized boolean addFunAppItemInfo(int index, FunAppItemInfo funItemInfo,
			boolean notify, boolean handleDB) throws DatabaseException {
		if (null == funItemInfo) {
			return false;
		}
		// 不支持添加文件夹到文件夹
		if (FunItemInfo.TYPE_FOLDER == funItemInfo.getType()) {
			return false;
		}

		if (null == funItemInfo.getAppItemInfo()) {
			return false;
		}

		// 文件夹中元素个数TODO:根据内存中的获取
		int size = getSize();
		// 越界
		if (index < 0 || index > size) {
			return false;
		}

		// TODO:保证不重复添加
		if (mHasInit || mfolderfirstcreate) {
			// 是否存在相同
//			if (isExistAppInFolder(funItemInfo)) {
//				return false;
//			}

			// 取小的
			int idx = index > mFunAppItemInfos.size() ? mFunAppItemInfos.size() : index;
			// 添加到内存
			mFunAppItemInfos.add(idx, funItemInfo);
			// 更新mItemInAppIndex
			funItemInfo.setIndex(idx);
			if (!funItemInfo.getAppItemInfo().mIsNewRecommendApp) { // 这里由于不知道原来为什么要去掉new标识，仍保留
				funItemInfo.setIsNew(false);
			}
			mUnreadCount += funItemInfo.getUnreadCount();
			FunAppItemInfo info = null;
			for (int i = idx + 1; i < mFunAppItemInfos.size(); ++i) {
				info = mFunAppItemInfos.get(i);
				info.setIndex(info.getIndex() + 1);
			}
		}

		if (handleDB) {
			// 添加到数据，并更新index
			AppItemInfo appItemInfo = funItemInfo.getAppItemInfo();
			String title = appItemInfo.mTitle;
			if (null == title) {
				// TODO:默认的title
				title = "AppName";
			}
			mFunDataModel.addFunAppToFolder(mFolderId, index, appItemInfo.mIntent, title);
		}

		if (notify) {
			// 通知
			broadCast(ADDITEM, index, funItemInfo, null);
		}
		if (mfolderfirstcreate) {
			mHasInit = true;
		}
		return true;
	}

	/**
	 * 是否存在相同的应用
	 * @param funItemInfo 将要加进这个文件夹的应用info
	 * @return
	 */
	public boolean isExistAppInFolder(FunAppItemInfo funItemInfo) {
		int upIdx = findInList(funItemInfo.getIntent());
		// 若已存在
		if (upIdx >= 0) {
			FunAppItemInfo funAppItemInfo = getFunAppItemInFolder(upIdx);
			if (null == funAppItemInfo) {
				return true;
			}
			if (null == funAppItemInfo.getAppItemInfo()) {
				return true;
			}
			if (funAppItemInfo.getAppItemInfo().isTemp()
					&& !funItemInfo.getAppItemInfo().isTemp()) {
				funAppItemInfo.setAppItemInfo(funItemInfo.getAppItemInfo());
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 批量添加到文件夹
	 * 
	 * @param index
	 * @param funItemInfo
	 * @throws DatabaseException
	 */
	public void addFunAppItemInfos(int startIndex, ArrayList<FunAppItemInfo> funItemInfos)
			throws DatabaseException {
		// 批量添加到内存
		addFunAppItemInfosInMem(startIndex, funItemInfos);
		// 批量添加到数据库
		mFunDataModel.addFunAppItemInfosInDB(mFolderId, startIndex, funItemInfos);
	}

	public void addFunAppItemInfosInMem(int startIndex, ArrayList<FunAppItemInfo> funItemInfos) {
		if (null == funItemInfos) {
			return;
		}

		int add = 0;
		FunAppItemInfo addItem = null;
		FunAppItemInfo funAppItemInfo = null;
		int size = funItemInfos.size();
		for (int i = 0; i < size; ++i) {
			funAppItemInfo = funItemInfos.get(i);
			if (null == funAppItemInfo) {
				continue;
			}

			// 添加到内存，不发通知
			addItem = addFunAppItemInfoInMem(startIndex + add, funAppItemInfo, false);
			if (null != addItem) {
				++add;
			}
		}
	}

	/**
	 * 添加到内存, 用于批量添加
	 * 
	 * @param index
	 * @param funItemInfo
	 * @param notify
	 */
	private FunAppItemInfo addFunAppItemInfoInMem(int index, FunAppItemInfo funItemInfo,
			boolean notify) {
		if (null == funItemInfo) {
			return null;
		}
		// 不支持添加文件夹到文件夹
		if (FunItemInfo.TYPE_FOLDER == funItemInfo.getType()) {
			return null;
		}

		if (null == funItemInfo.getAppItemInfo()) {
			return null;
		}

		// 文件夹中元素个数TODO:根据内存中的获取
		int size = getSize();
		// 越界
		if (index < 0 || index > size) {
			return null;
		}

		// TODO:保证不重复添加
		if (mHasInit) {
			// 取小的
			int idx = index > mFunAppItemInfos.size() ? mFunAppItemInfos.size() : index;
			// 添加到内存
			mFunAppItemInfos.add(idx, funItemInfo);
			// 更新mItemInAppIndex
			funItemInfo.setIndex(idx);
			FunAppItemInfo info = null;
			for (int i = idx + 1; i < mFunAppItemInfos.size(); ++i) {
				info = mFunAppItemInfos.get(i);
				info.setIndex(info.getIndex() + 1);
			}
		}

		if (notify) {
			// 通知
			broadCast(ADDITEM, index, funItemInfo, null);
		}

		return funItemInfo;
	}

	/**
	 * 清空文件夹中的数据
	 * 
	 * @throws DatabaseException
	 */
	public void clearFunAppItems() throws DatabaseException {
		mFunDataModel.clearFolderAppItems(mFolderId);
	}

	/**
	 * 处理SD卡数据
	 * 
	 * @param appItemInfos
	 * @param updateData
	 */
	public void handleSDAppItems(final ArrayList<AppItemInfo> appItemInfos, boolean updateData) {
		if (null == appItemInfos) {
			return;
		}

		if (mHasInit) {
			AppItemInfo appItemInfo = null;
			FunAppItemInfo funItemInfo = null;
			// 需要移除的应用程序列表
			ArrayList<AppItemInfo> removeList = null;
			int size = mFunAppItemInfos.size();
			for (int i = size - 1; i >= 0; --i) {
				funItemInfo = mFunAppItemInfos.get(i);
				if (null == funItemInfo) {
					continue;
				}

				appItemInfo = funItemInfo.getAppItemInfo();
				if (null == appItemInfo) {
					continue;
				}

				// 不是暂存数据
				if (!appItemInfo.isTemp()) {
					continue;
				}

				int idx = findInList(appItemInfos, appItemInfo.mIntent);
				if (idx >= 0) {
					// 若在sd卡程序数组中, 更新数据
					AppItemInfo info = mFunDataModel.getAppItem(appItemInfo.mIntent);
					funItemInfo.setAppItemInfo(info);
				} else {
					if (updateData) {
						// 不在sd卡程序数组中,从内存及数据库中移除
						try {
							removeFunAppItemInfo(funItemInfo, true);
						} catch (DatabaseException e) {
							e.printStackTrace();
						}
						if (funItemInfo != null) {
							if (removeList == null) {
								removeList = new ArrayList<AppItemInfo>();
							}
							removeList.add(funItemInfo.getAppItemInfo());
						}
					}
				}
			}
			// 通知桌面来更新桌面文件夹
			if (removeList != null && removeList.size() > 0) {
				GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
						IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, 0,
						mFolderId, removeList);
			}
		} else {
			// 只处理数据库的
		}
	}

	/**
	 * 是否存在于数据中
	 * 
	 * @param appItemInfos
	 *            数组
	 * @param intent
	 *            唯一标识Intent
	 * @return 是否存在
	 */
	private int findInList(final ArrayList<AppItemInfo> appItemInfos, Intent intent) {
		if (null == appItemInfos || null == intent) {
			return -1;
		}

		AppItemInfo appItemInfo = null;
		int size = appItemInfos.size();
		for (int i = 0; i < size; ++i) {
			appItemInfo = appItemInfos.get(i);
			if (null == appItemInfo) {
				continue;
			}

			if (ConvertUtils.intentCompare(intent, appItemInfo.mIntent)) {
				return i;
			}
		}
		return -1;
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
			default :
				break;
		}
		// 直接转发
		super.broadCast(msgId, param, object, objects);
	}

	/**
	 * 按使用频率排序，并保存到数据库
	 * 
	 * @param order
	 */
	public synchronized boolean sortByFrequencyAndSave(Context context, String order) {
		if (!mHasInit) {
			return false;
		}

		// LogUnit.i("FunFolderItemInfo", "sortByTimeAndSave()");
		boolean ret = true;
		// 排序
		try {
			sortByFrequency(context, order);
		} catch (Exception e) {
			ret = false;
		}
		// 清除原来的排序
		refreshIndex();
		// 保存排序
		mFunDataModel.updateFunAppItemsIndexInFolder(mFolderId, mFunAppItemInfos);
		// 通知
		return ret;
	}

	private void sortByFrequency(Context context, String order) {
		String sortMethod = "getClickedCount";
		Class[] methodArgClasses = new Class[] { Context.class };
		Object[] methodArg = new Object[] { context };
		SortUtils.sortByInt(mFunAppItemInfos, sortMethod, methodArgClasses, methodArg, order);
	}

	public void sortAfterAdd() {
		Context context = GoLauncher.getContext();
		SharedPreferences preferences = GoLauncher.getContext()
				.getPreferences(Context.MODE_PRIVATE);
		int type = preferences.getInt(String.valueOf(mFolderId), -1);
		try {
			switch (type) {
				case FunAppSetting.SORTTYPE_LETTER :
					sortByLetterAndSave("ASC");
					break;
				case FunAppSetting.SORTTYPE_TIMENEAR :
					sortByTimeAndSave(context, "DESC");
					break;
				case FunAppSetting.SORTTYPE_TIMEREMOTE :
					sortByTimeAndSave(context, "ASC");
					break;
				case FunAppSetting.SORTTYPE_FREQUENCY :
					sortByFrequencyAndSave(context, "DESC");
					break;
				default :
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getFolderType() {
		return mFolderType;
	}

	public void setFolderType(int type) {
		mFolderType = type;
	}

	@Override
	public int getClickedCount(Context context) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isHide() {
		return false;
	}

//	@Override
//	public int getUnreadCount() {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	@Override
	public boolean isSysApp() {
		return false;
	}

	@Override
	public boolean isPriority() {
		return true;
	}
	
/**----------------------------------3D插件文件夹方法----------------------------------*/
	/**
	 * <br>功能简述:外部直接设置这个文件夹的内容
	 * <br>功能详细描述:
	 * <br>注意:FunAppItemInfo必须是具有完整数据的，包括index
	 * @param mFunAppItemInfos
	 */
	/**
	 * 3D插件用的构造函数
	 */
	public FunFolderItemInfo(FunItem funItem) {
		mType = TYPE_FOLDER;
		// 若是文件夹，给予唯一的intent
		mIntent = funItem.mIntent;
		// 产生唯一的文件夹id
		mFolderId = funItem.mFolderId;
		mFolderTitle = funItem.mTitle;
		mIndex = funItem.mIndex;
		if (null == mFolderTitle) {
			mFolderTitle = "Folder Name";
		}

		mFunAppItemInfos = new ArrayList<FunAppItemInfo>();
	}

	public void setFolderContent(ArrayList<FunAppItemInfo> mFunAppItemInfos) {
		this.mFunAppItemInfos = mFunAppItemInfos;
	}

	public ArrayList<FunAppItemInfo> getFolderContent() {
		return mFunAppItemInfos;
	}

	/**
	 * 3D插件中使用此方法
	 * 获取文件夹中没有被隐藏的程序列表, 后台不维护该列表
	 * 
	 * @param folderId
	 * @return
	 */
	public final ArrayList<FunAppItemInfo> getFolderContentExceptHide() {
		ArrayList<FunAppItemInfo> appList = getFolderContent();
		ArrayList<FunAppItemInfo> appListExceptHide = new ArrayList<FunAppItemInfo>();
		for (FunAppItemInfo app : appList) {
			if ((app != null) && (!app.isHide())) {
				appListExceptHide.add(app);
			}
		}
		return appListExceptHide;
	}
	/**
	 * <br>功能简述:批量添加文件夹内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param startIndex
	 * @param funItemInfos
	 */
	public void addFolderContentBatch(int startIndex, ArrayList<FunAppItemInfo> funItemInfos) {
		if (null == funItemInfos) {
			return;
		}
		int add = 0;
		FunAppItemInfo addItem = null;
		FunAppItemInfo funAppItemInfo = null;
		int size = funItemInfos.size();
		for (int i = 0; i < size; ++i) {
			funAppItemInfo = funItemInfos.get(i);
			if (null == funAppItemInfo) {
				continue;
			}
			// 添加到内存，不发通知
			addItem = addFolderContent(startIndex + add, funAppItemInfo);
			if (null != addItem) {
				++add;
			}
		}
	}
	/**
	 * 文件夹内容添加到内存, 用于批量添加
	 * 
	 * @param index
	 * @param funItemInfo
	 * @param notify
	 */
	public FunAppItemInfo addFolderContent(int index, FunAppItemInfo funItemInfo
			) {
		if (null == funItemInfo || FunItemInfo.TYPE_FOLDER == funItemInfo.getType()
				|| null == funItemInfo.getAppItemInfo()) {
			return null;
		}
		// 文件夹中元素个数TODO:根据内存中的获取
		int size = getFolderSize();
		// 越界
		if (index < 0 || index > size) {
			return null;
		}
		if (mFunAppItemInfos != null) {
			// 取小的
			int idx = Math.min(index, mFunAppItemInfos.size());
//					index > mFunAppItemInfos.size() ? mFunAppItemInfos.size() : index;
			// 添加到内存
			mFunAppItemInfos.add(idx, funItemInfo);
			// 更新mItemInAppIndex
			funItemInfo.setIndex(idx);
			FunAppItemInfo info = null;
			for (int i = idx + 1; i < mFunAppItemInfos.size(); ++i) {
				info = mFunAppItemInfos.get(i);
				info.setIndex(info.getIndex() + 1);
			}
		}

//		if (notify) {
//			// 通知
//			broadCast(ADDITEM, index, funItemInfo, null);
//		}

		return funItemInfo;
	}
	/**
	 * 获取文件夹中图标个数
	 * 
	 * @return 图标个数
	 */
	public final int getFolderSize() {
		return mFunAppItemInfos.size();
	}
}

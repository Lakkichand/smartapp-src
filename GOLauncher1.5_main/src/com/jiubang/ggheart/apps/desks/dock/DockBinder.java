package com.jiubang.ggheart.apps.desks.dock;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
import android.os.Process;

import com.go.util.AsyncHandler;
import com.go.util.DeferredHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderIcon;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * dock文件夹加载器
 */
class DockBinder {
	private BindAsyncHandler mAsyncHandler;
	private BindDeferredHandler mDeferredHandler;

	/** 15秒内没有收到异步请求，则退出异步线程 */
	private final static int QUIT_DELAY = 15000;

	private static final int MSG_QUIT_THREAD = -1;

	private static final int MSG_ASYNC_UPDATE_FOLDER = 0x4;
	private static final int MSG_POST_UPDATE_FOLDER = 0x5;

	private static final int MSG_SYNCH_FOLDER_CONTENT = 0x6;
	private static final int MSG_POST_RELOAD_FOLDER_CONTENT = 0x7;

	private static final int MSG_REMOVE_FOLDER_CONTENT = 0x8;
	private static final int MSG_POST_DELETE_FOLDER = 0x9;

	/**
	 * 
	 * <br>类描述:同步文件夹数据类
	 * <br>功能详细描述:
	 * 
	 * @author  ruxueqin
	 * @date  [2012-10-16]
	 */
	private static class SyncFolderInfo {
		boolean mReload = false;
		UserFolderInfo mFolderInfo; // 编辑的文件夹
	}

	private final DockDataModel mDataModel;
	private Object mLock = new Object();
	private Context mContext;

	public DockBinder(Context context, DockDataModel model) {
		mContext = context;
		mDataModel = model;
		// init handler
		mDeferredHandler = new BindDeferredHandler();
	}

	public void cancel() {
		synchronized (mLock) {
			mDeferredHandler.cancel();
			if (mAsyncHandler != null) {
				mAsyncHandler.cancel();
			}
		}
	}

	/**
	 * 刷新文件夹图标
	 * 
	 * @param dockItemInfo
	 *            dock文件信息
	 * @param checkDel
	 *            刷新完后是否要检查删除文件夹
	 */
	public void updateFolderIconAsync(DockItemInfo dockItemInfo, int type, boolean checkDel) {
		synchronized (mLock) {
			if (mAsyncHandler == null) {
				mAsyncHandler = new BindAsyncHandler();
			}

			mAsyncHandler.removeMessages(MSG_QUIT_THREAD);
			Message message = new Message();
			message.what = MSG_ASYNC_UPDATE_FOLDER;
			message.obj = dockItemInfo;
			message.arg1 = checkDel ? 1 : 0;
			message.arg2 = type;
			mAsyncHandler.sendMessage(message);
		}
	}

	/**
	 * 删除文件夹内的items
	 * 
	 * @param folderInfo
	 * @param items
	 */
	public void removeFolderContent(UserFolderInfo folderInfo, ArrayList<ItemInfo> items,
			boolean checkDeleteFolder) {
		synchronized (mLock) {
			if (mAsyncHandler == null) {
				mAsyncHandler = new BindAsyncHandler();
			}

			mAsyncHandler.removeMessages(MSG_QUIT_THREAD);
			Message message = new Message();
			message.what = MSG_REMOVE_FOLDER_CONTENT;
			SyncFolderInfo syncFolderInfo = new SyncFolderInfo();
			syncFolderInfo.mReload = true;
			syncFolderInfo.mFolderInfo = folderInfo;
			message.obj = syncFolderInfo;
			message.arg1 = checkDeleteFolder ? 1 : 0;
			mAsyncHandler.sendMessage(message);
		}
	}

	private void requestQuit() {
		// 发出终止线程请求
		synchronized (mLock) {
			if (mAsyncHandler != null) {
				mAsyncHandler.sendEmptyMessageDelay(MSG_QUIT_THREAD, QUIT_DELAY);
			}
		}
	}

	/**
	 * 
	 * <br>类描述:异步加载器，用于异步加载、刷新dock文件夹
	 * <br>功能详细描述:
	 * 
	 * @author  ruxueqin
	 * @date  [2011]
	 */
	private class BindAsyncHandler extends AsyncHandler {
		public BindAsyncHandler() {
			super(ThreadName.DOCK_BINDER, Process.THREAD_PRIORITY_BACKGROUND);
		}

		@Override
		public void handleAsyncMessage(Message msg) {
			switch (msg.what) {
				case MSG_ASYNC_UPDATE_FOLDER : {
					if (msg.obj != null && msg.obj instanceof DockItemInfo) {
						final DockItemInfo dockItemInfo = (DockItemInfo) msg.obj;
						if (null == dockItemInfo.mItemInfo
								|| !(dockItemInfo.mItemInfo instanceof UserFolderInfo)) {
							return;
						}
						final UserFolderInfo folderInfo = (UserFolderInfo) dockItemInfo.mItemInfo;
						if (folderInfo != null) {
							// 需要重新加载文件夹内容
							if (!folderInfo.mContentsInit) {
								final ArrayList<ItemInfo> contents = mDataModel
										.getDockFolderItemsFromDB(folderInfo, -1, true);
								synchronized (folderInfo) {
									folderInfo.clear();
								}
								if (contents != null) {
									folderInfo.addAll(contents);
								}
								folderInfo.mContentsInit = true;
							}
							// 更新到UI
							Message uiMessage = new Message();
							uiMessage.what = MSG_POST_UPDATE_FOLDER;
							uiMessage.obj = dockItemInfo;
							uiMessage.arg1 = msg.arg1;
							if (msg.arg2 == DockUtil.TYPE_REFRASH_FOLDER_CONTENT_UNINSTALLAPP) {
								uiMessage.arg2 = dockItemInfo.mIndex;
							} else {
								uiMessage.arg2 = -1;
							}
							mDeferredHandler.sendMessage(uiMessage);
						}
					}

					// 发出终止线程请求
					requestQuit();
					break;
				}

				case MSG_SYNCH_FOLDER_CONTENT : {
					// final ScreenFrame screenFrame = mScreen.get();
					if (/* screenFrame != null && */msg.obj != null
							&& msg.obj instanceof SyncFolderInfo) {
						final SyncFolderInfo syncFolderInfo = (SyncFolderInfo) msg.obj;
						final UserFolderInfo folderInfo = syncFolderInfo.mFolderInfo;

						if (folderInfo != null) {
							// screenFrame.addUserFolderContent(
							// folderInfo.mInScreenId,
							// folderInfo,
							// syncFolderInfo.mItems, true);

							// 更新到UI
							if (syncFolderInfo.mReload) {
								Message uiMessage = new Message();
								uiMessage.what = MSG_POST_RELOAD_FOLDER_CONTENT;
								uiMessage.obj = folderInfo;
								mDeferredHandler.sendMessage(uiMessage);
							}
						}
					}

					// 发出终止线程请求
					requestQuit();
					break;
				}

				case MSG_REMOVE_FOLDER_CONTENT : {
					// final ScreenFrame screenFrame = mScreen.get();
					if (mDataModel != null && msg.obj != null && msg.obj instanceof SyncFolderInfo) {
						final SyncFolderInfo syncFolderInfo = (SyncFolderInfo) msg.obj;
						final UserFolderInfo folderInfo = syncFolderInfo.mFolderInfo;

						if (folderInfo != null) {
							mDataModel.removeDockFolder(folderInfo.mInScreenId);
							// screenFrame.removeUserFolderConent(
							// folderInfo.mInScreenId,
							// syncFolderInfo.mItems, true);

							// ArrayList<ItemInfo> items =
							// screenFrame.getFolderContent(folderInfo.mInScreenId);
							// boolean checkDeleteFolder = msg.arg1 == 1;
							// if (checkDeleteFolder && (null != items &&
							// items.size() <= 0))
							// {
							// // 更新到UI删除文件夹
							// Message uiMessage = new Message();
							// uiMessage.what = MSG_POST_DELETE_FOLDER;
							// uiMessage.obj = folderInfo;
							// mDeferredHandler.sendMessage(uiMessage);
							// }
							// else if (syncFolderInfo.mReload)
							// {
							// // 更新到UI刷新图标
							// Message uiMessage = new Message();
							// uiMessage.what = MSG_POST_RELOAD_FOLDER_CONTENT;
							// uiMessage.obj = folderInfo;
							// mDeferredHandler.sendMessage(uiMessage);
							// }
						}
					}

					// 发出终止线程请求
					requestQuit();
					break;
				}

				case MSG_QUIT_THREAD : {
					synchronized (mLock) {
						if (mAsyncHandler != null) {
							mAsyncHandler.cancel();
							mAsyncHandler = null;
						}
					}
					break;
				}

				default :
					break;
			}
		}
	}

	/**
	 * 
	 * <br>类描述:异步加载器UI线程处理器，用于配合BindAsyncHandler处理UI操作
	 * <br>功能详细描述:
	 * 
	 * @author  ruxueqin
	 * @date  [2011]
	 */
	private class BindDeferredHandler extends DeferredHandler {
		@Override
		public void handleIdleMessage(Message msg) {
			if (mDataModel != null) {
				switch (msg.what) {

					case MSG_POST_UPDATE_FOLDER : {
						if (msg.obj != null && msg.obj instanceof DockItemInfo
								&& ((DockItemInfo) msg.obj).mItemInfo instanceof UserFolderInfo) {
							final DockItemInfo item = (DockItemInfo) msg.obj;
							final UserFolderInfo folder = (UserFolderInfo) item.mItemInfo;
							final int count = folder.getChildCount();
							for (int i = 0; i < count && i < FolderIcon.INNER_ICON_SIZE; i++) {
								ShortCutInfo itemInfo = folder.getChildInfo(i);
								if (null != itemInfo) {
									itemInfo.registerObserver(item);
								}
							}

							mDataModel.prepareItemInfo(item.mItemInfo);
							final BitmapDrawable bDrawable = item.getFolderBackIcon();
							final Bitmap bitmap = item.prepareOpenFolderIcon(bDrawable);
							final BitmapDrawable icon = new BitmapDrawable(mContext.getResources(),
									bitmap);
							item.setIcon(icon);
							if (msg.arg1 == 1) {
								// 需要检查删除文件夹
								GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
										IDiyMsgIds.CHECK_FOLDER_NEED_DELETE, -1,
										folder.mInScreenId, null);
							}
						}
						break;
					}

					case MSG_POST_RELOAD_FOLDER_CONTENT : {
						if (msg.obj != null && msg.obj instanceof UserFolderInfo) {
							// screenFrame.reloadFolderContent((UserFolderInfo)msg.obj);
						}
						break;
					}

					case MSG_POST_DELETE_FOLDER : {
						if (msg.obj != null && msg.obj instanceof UserFolderInfo) {
							// screenFrame.deleteItem((UserFolderInfo)msg.obj);
						}
						break;
					}

					default :
						break;
				}
			}
		}
	}

}
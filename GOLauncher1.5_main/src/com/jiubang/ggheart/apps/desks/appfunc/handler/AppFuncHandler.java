package com.jiubang.ggheart.apps.desks.appfunc.handler;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.common.component.AppFuncTopSwitchContainer;
import com.jiubang.ggheart.apps.appfunc.component.AllAppTabBasicContent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncFolder;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncFolderQuickAddBar;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncHomeComponent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncTabBasicContent;
import com.jiubang.ggheart.apps.appfunc.component.ApplicationIcon;
import com.jiubang.ggheart.apps.appfunc.component.ProManageEditDock;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appfunc.controler.IndexFinder;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncMainView;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncTabComponent;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncAdapter;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;

/**
 * 用于处理组件之间的通信
 * 
 * @author wenjiaming
 * 
 */
public class AppFuncHandler {

	private static final int MSG_SHOW_FOLDER = 0;
	private static final int MSG_HIDE_FOLDER = 1;
	private static final int LOCATE_APP_IN_FOLDER = 2;
	private AppFuncMainView mAppFuncMainView;

	private Activity mActivity;

	private static AppFuncHandler sInstance;

	private Handler mHandler;

	public static AppFuncHandler getInstance() {
		if (sInstance == null) {
			sInstance = new AppFuncHandler();
		}
		return sInstance;
	}

	public void setAppFuncMainView(AppFuncMainView mAppFuncMainView, Activity activity) {
		this.mAppFuncMainView = mAppFuncMainView;
		this.mActivity = activity;
		initHandler();
	}

	private void initHandler() {
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_SHOW_FOLDER : {
						FunFolderItemInfo folderInfo = (FunFolderItemInfo) msg.obj;
						if (mAppFuncMainView != null) {

							// 1. 从后台获得对应的文件夹信息
							// 2. 如果XGrid在编辑状态，通知其停止编辑状态
							XBaseGrid curGrid = mAppFuncMainView.getCurrentContent().getXGrid();

							boolean inDragStatus = curGrid.isInDragStatus();
							if (inDragStatus) {
								curGrid.setDragStatus(false);
							}
//							curGrid.updateLayoutParams();
//							curGrid.requestLayout();
							// 3. 构造文件夹并显示
							mAppFuncMainView.initFolder(folderInfo, inDragStatus);
						}
					}
						break;
					
					case MSG_HIDE_FOLDER : {
						if (mAppFuncMainView.isFolderShow()) {
							mAppFuncMainView.hideFolder();
						}
					}
						break;
						
					case LOCATE_APP_IN_FOLDER : {
						Intent appIntent = (Intent) msg.obj;
						DeliverMsgManager.getInstance().onChange(
								AppFuncConstants.APPFOLDER_GRID,
								AppFuncConstants.APP_GRID_LOCATE_ITEM,
								appIntent);
					}
						break;
					default :
						break;
				}
			}
		};

	}

	/**
	 * 设置HomeIcon组件改变
	 * 
	 * @param b
	 * <BR>
	 *            true：改变为"Move to Desk"</BR> <BR>
	 *            false：改变为"Home"</BR>
	 */
	public void setHomeIconChangeDesk(boolean flag, boolean needAnimate) {
		XComponent component = mAppFuncMainView.getSeletedTabContentView();

		if (component instanceof AllAppTabBasicContent) {
			AllAppTabBasicContent content = (AllAppTabBasicContent) component;
			if (flag) {
				content.swtichHomeToDesk(needAnimate);
			} else {
				content.swtichDeskToHome(needAnimate);
				AppFuncHomeComponent homeComponent = content.getHomeComponent();
				homeComponent.changeHomeIconBackground(flag);
			}
		}
		//		if (component instanceof ProManageTabBasicContent) {
		//			ProManageTabBasicContent content = (ProManageTabBasicContent) component;
		//			if (b) {
		//				// TODO 这里等做在正在运行拖动图标时要去掉编辑状态时的组件，不用再切换了
		//				content.swtichHomeToDesk();
		//			} else {
		//				content.swtichDeskToHome();
		//				AppFuncHomeComponent homeComponent = content.getHomeComponent();
		//				homeComponent.changeHomeIconBackground(b);
		//
		//			}
		//		}
	}

	/**
	 * 设置Top栏组件
	 * @param flag (true: 显示eidtBar, false: 显示NormalBar)
	 *
	 */
	public void setTopChange(boolean flag, boolean needAnimate) {
		if (flag) {
			mAppFuncMainView.showTopEditBar(needAnimate);
		} else {
			mAppFuncMainView.showTopNormalBar(needAnimate);
		}
	}

	/**
	 * 检查被拖动的组件是否与HomeIcon重合
	 * 
	 * @param x
	 * @param y
	 * @return isOverlap 是否重叠
	 */
	public boolean checkDragComponentHomeIconOverlap(int x, int y) {
		boolean isOverlap = false;
		XComponent component = mAppFuncMainView.getSeletedTabContentView();
		if (component instanceof AllAppTabBasicContent) {
			AllAppTabBasicContent content = (AllAppTabBasicContent) component;
			AppFuncHomeComponent homeComponent = content.getHomeComponent();
			if (homeComponent != null && homeComponent.XYInRange(x, y)) {
				homeComponent.changeHomeIconBackground(true);
				isOverlap = true;
			} else {
				if (homeComponent != null) {
					homeComponent.changeHomeIconBackground(false);
				}

				isOverlap = false;
			}
		}
		return isOverlap;
	}

	public boolean checkDragComponentTopBarOverlap(int x, int y) {
		boolean isOverlap = false;
		AppFuncTopSwitchContainer container = getCurrentTabComponent().getTopBarContainer();
		if (container != null && container.XYInRange(x, y)) {
			isOverlap = true;
		}
		return isOverlap;
	}

	/**
	 * 当拖动组件到Home Icon区域时，改变Home Icon背景颜色。(拖动图标到桌面使用)
	 * 
	 * @param isOverlap
	 * <BR>
	 *            true：区域重叠</BR> <BR>
	 *            false：区域不重叠"</BR>
	 */
	public void changeHomeIconBackground(boolean on) {
		XComponent component = mAppFuncMainView.getSeletedTabContentView();
		if (component instanceof AppFuncTabBasicContent) {
			AppFuncTabBasicContent content = (AppFuncTabBasicContent) component;
			AppFuncHomeComponent homeComponent = content.getHomeComponent();
			homeComponent.changeHomeIconBackground(on);
		}
	}

	/**
	 * 通过菜单创建新的文件夹
	 * 
	 * @param folderName
	 *            创建的文件夹名字
	 * @param list
	 *            文件夹里面的图标
	 * @throws DatabaseException
	 */
	public void createFolderByMenu(String folderName, ArrayList<FunAppItemInfo> list)
			throws DatabaseException {
		FunControler funControler = AppFuncFrame.getFunControler();
		if (funControler != null && funControler.isHandling()) {
			try {
				DeskToast.makeText(mActivity, R.string.app_fun_strat_loading, Toast.LENGTH_SHORT)
						.show();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}
			return;
		}
		XComponent component = mAppFuncMainView.getSeletedTabContentView();
		if (component instanceof AllAppTabBasicContent) {
			// 前5次使用提示
			PreferencesManager manager = new PreferencesManager(mActivity);
			int count = manager.getInt(IPreferencesIds.APP_FUNC_NEW_FOLDER, 0);
			if (count < 5) {
				count++;
				manager.putInt(IPreferencesIds.APP_FUNC_NEW_FOLDER, count);
				manager.commit();
				// try {
				// DeskToast.makeText(mActivity, R.string.app_fun_new_folder,
				// Toast.LENGTH_SHORT).show();
				// } catch (OutOfMemoryError e) {
				// e.printStackTrace();
				// OutOfMemoryHandler.handle();
				// }
			}
			AppFuncTabBasicContent content = (AllAppTabBasicContent) component;
			XBaseGrid grid = content.getXGrid();
			FunFolderItemInfo folderInfo = null;
			int folderIndex = -1;
			try {
				funControler.getFunDataModel().beginTransaction();
				// 1.首先删除功能表根目录下将要加入到文件夹的程序
				funControler.removeFunAppItemInfos(list);
				// 2.获取要插入文件夹的位置的下标
				folderInfo = new FunFolderItemInfo(funControler.getFunDataModel(), folderName);
				folderIndex = IndexFinder.findIndex(mActivity, funControler.getFunAppItems(),
						false, folderInfo);
				// 3.将新的空文件夹放到对应位置
				funControler.addFunFolderItemInfo(folderIndex, folderInfo);
				// 4.把要加入的程序放到文件夹
				// folderInfo.setFolderId(tempFolder.getFolderId());
				folderInfo.addFunAppItemInfos(folderInfo.getSize(), list);
				funControler.getFunDataModel().setTransactionSuccessful();
			} finally {
				funControler.getFunDataModel().endTransaction();
			}
			if (folderInfo != null) {
				if (!folderInfo.isMfolderchange()) {
					folderInfo.setMfolderchange(true);
				}
				// 5.对文件夹里面的图标排序
				folderInfo.sortByLetterAndSave("ASC");
			}
			// 6.把屏幕移到新建文件夹的位置
			int index = folderIndex;
			AppFuncAdapter adapter = content.getAdapter();
			adapter.loadApp();
			count = adapter.getCount();
			int gridIndex = 0;
			for (int i = 0; i < count; i++) {
				long itemIndex = adapter.getItemId(i);
				if (index == itemIndex) {
					gridIndex = i;
					if (grid.isSupportScroll()) {
						boolean vScroll = grid.isVScroll();
						if (vScroll) {
							int colunmNums = grid.getColunmNums();
							grid.setFVisibleIndex(i - (i % colunmNums));
						} else {
							int colunmNums = grid.getColunmNums();
							int rowNums = grid.getRowNums();
							int total = colunmNums * rowNums;
							grid.setFVisibleIndex(i - (i % total));
						}
					}
					break;
				}
			}
			// 7.刷新XBaseGrid组件显示新的数据
			grid.updateLayoutParams();
			grid.requestLayout();
			XComponent xComponent = grid.getChildAt(gridIndex);
			if (xComponent != null && xComponent instanceof ApplicationIcon) {
				if (!mAppFuncMainView.isFolderShow()) {
					// 文件夹打开的情况下，新建文件夹，不需要做动画
					((ApplicationIcon) xComponent).stratNewIcon();
				}
			}
		}
	}

	/**
	 * 刷新Grid的布局
	 */
	public void refreshGrid() {
		XComponent component = mAppFuncMainView.getSeletedTabContentView();
		if (component instanceof AppFuncTabBasicContent) {
			AppFuncTabBasicContent content = (AppFuncTabBasicContent) component;
			content.refreshGrid();
		}
	}

	// public void startDeleteFolderMotion(FunFolderItemInfo funitem){
	// XComponent component = mAppFuncMainView.getSeletedTabContentView();
	// if (component instanceof AppFuncTabBasicContent) {
	// AppFuncTabBasicContent content = (AppFuncTabBasicContent) component;
	// content.startDeleteFolderMotion(funitem);
	// }
	//
	// }
	/**
	 * 刷新所有程序的Grid
	 */
	public void refreshAllAppGrid() {
		if (AppFuncFrame.sVisible) {
			mAppFuncMainView.refreshAllAppGrid();
		}
	}

	/**
	 * 显示要展现的文件夹
	 * 
	 */
	public void showFolder(FunFolderItemInfo folderInfo) {
		Message msg = mHandler.obtainMessage(MSG_SHOW_FOLDER);
		msg.obj = folderInfo;
		mHandler.sendMessage(msg);
	}

	/**
	 * 隐藏文件夹
	 * 
	 */
	public void removeFolder() {
		if (mAppFuncMainView != null) {
			mAppFuncMainView.removeFolder();
		}
	}

	public void hideFolder() {
		mHandler.sendEmptyMessage(MSG_HIDE_FOLDER);
	}
	/**
	 * 告诉tab得到焦点
	 */
	public void setTabHasFocus() {
		if (mAppFuncMainView != null) {
			mAppFuncMainView.getTabFocus();
		}
	}

	/**
	 * 检测XBaseGrid是否在文件夹里面
	 * 
	 * @param xBaseGrid
	 * @return
	 */
	public boolean isInFolder(XBaseGrid xBaseGrid) {
		if (xBaseGrid.getAttachPanel() instanceof AppFuncFolder) {
			return true;
		}
		return false;
	}

	/**
	 * 修改文件夹内的内容
	 * 
	 * @param mList
	 *            可供选择的组件列表
	 * @param mBooleanList
	 *            用户最新选择图标组件
	 * @param funFolderItemInfo
	 *            文件夹信息
	 * @throws DatabaseException
	 */
	public void modifyFolder(ArrayList<FunAppItemInfo> mList, ArrayList<Boolean> mBooleanList,
			FunFolderItemInfo funFolderItemInfo, boolean isNewFolder) throws DatabaseException {
		if (mList == null) {
			return;
		}
		FunControler funControler = AppFuncFrame.getFunControler();
		XComponent component = mAppFuncMainView.getSeletedTabContentView();
		if (component instanceof AllAppTabBasicContent) {
			AllAppTabBasicContent content = (AllAppTabBasicContent) component;
			XBaseGrid grid = content.getXGrid();
			int size = mList.size();
			ArrayList<AppItemInfo> removeList = null;
			ArrayList<AppItemInfo> addList = null;
			try {
				funControler.getFunDataModel().beginTransaction();
				for (int i = size - 1; i >= 0; i--) {
					FunAppItemInfo info = mList.get(i);
					Boolean b = mBooleanList.get(i);
					// 不打钩
					if (!b) {
						int index = funFolderItemInfo.findInList(info);
						if (index != -1) {
							// 如果文件夹中包含没有打钩的图标，从文件夹中删除，加到功能表根目录
							int findIndex = IndexFinder.findIndex(mActivity,
									funControler.getFunAppItems(), true, info);
							// int findIndex = funControler.findIndex(true,
							// info.getTitle(), "ASC");
							funControler.moveFunAppItemFromFolder(funFolderItemInfo, findIndex,
									info);
							if (removeList == null) {
								removeList = new ArrayList<AppItemInfo>();
							}
							removeList.add(info.getAppItemInfo());

						}
					} else { // 打钩
						int index = funFolderItemInfo.findInList(info);
						if (index == -1) {
							int findIndex = IndexFinder.findIndex(mActivity,
									funFolderItemInfo.getFunAppItemInfos(), false, info);
							// 如果文件夹中不包含没有打钩的图标，从功能表根目录中删除，加到文件夹
							funControler.moveFunAppItemToFolder(funFolderItemInfo, findIndex, info);
							if (addList == null) {
								addList = new ArrayList<AppItemInfo>();
							}
							addList.add(info.getAppItemInfo());
						}
					}
				}
				funControler.getFunDataModel().setTransactionSuccessful();
			} finally {
				funControler.getFunDataModel().endTransaction();
			}

			// 通知桌面同步更新桌面文件夹
			if (addList != null) {
				GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
						IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_FOLDER_ADDITEMS, 0,
						funFolderItemInfo.getFolderId(), addList);
				GoLauncher.sendHandler(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
						IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.SCREEN_FOLDER_ADDITEMS, 0,
						funFolderItemInfo.getFolderId(), addList);
			}
			if (removeList != null) {
				GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
						IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, 0,
						funFolderItemInfo.getFolderId(), removeList);
				GoLauncher.sendHandler(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
						IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, 0,
						funFolderItemInfo.getFolderId(), removeList);
			}
			if (isNewFolder) {
				int index = funFolderItemInfo.getIndex();
				AppFuncAdapter adapter = content.getAdapter();
				adapter.loadApp();
				int count = adapter.getCount();
				for (int i = 0; i < count; i++) {
					long itemIndex = adapter.getItemId(i);
					if (index == itemIndex) {
						if (grid.isSupportScroll()) {
							boolean vScroll = grid.isVScroll();
							if (vScroll) {
								int colunmNums = grid.getColunmNums();
								grid.setFVisibleIndex(i - (i % colunmNums));
							} else {
								int colunmNums = grid.getColunmNums();
								int rowNums = grid.getRowNums();
								int total = colunmNums * rowNums;
								grid.setFVisibleIndex(i - (i % total));
							}
						}
						break;
					}
				}
			}
			funFolderItemInfo.sortAfterAdd();
		}
	}

	/**
	 * 将图标加入文件夹
	 * 
	 * @param b
	 *            是否创建新的文件夹
	 * 
	 */
	public void mergerItemToFolder() {
		getCurrentGrid().mergeItemToFolder();
	}

	/**
	 * 获取当前Grid
	 * @return
	 */
	public XBaseGrid getCurrentGrid() {
		return mAppFuncMainView.getCurrentContent().getXGrid();
	}

	public void layoutRootFuncGrid(FunAppItemInfo info) {
		XComponent component = mAppFuncMainView.getSeletedTabContentView();
		if (component instanceof AllAppTabBasicContent) {
			AllAppTabBasicContent content = (AllAppTabBasicContent) component;
			XBaseGrid grid = content.getXGrid();
			int index = info.getIndex();
			AppFuncAdapter adapter = content.getAdapter();
			adapter.loadApp();
			int count = adapter.getCount();
			for (int i = 0; i < count; i++) {
				long itemIndex = adapter.getItemId(i);
				if (index == itemIndex) {
					if (grid.isSupportScroll()) {
						boolean vScroll = grid.isVScroll();
						if (vScroll) {
							int colunmNums = grid.getColunmNums();
							grid.setFVisibleIndex(i - (i % colunmNums));
						} else {
							int colunmNums = grid.getColunmNums();
							int rowNums = grid.getRowNums();
							int total = colunmNums * rowNums;
							grid.setFVisibleIndex(i - (i % total));
						}
					}
					break;
				}
			}
		}
	}

	/**
	 * 获取所知tab的HomeComponent
	 * 
	 * @return
	 */
	public AppFuncHomeComponent getCurrentHomeComponent() {
		XComponent component = mAppFuncMainView.getSeletedTabContentView();
		if (component instanceof AppFuncTabBasicContent) {
			AppFuncTabBasicContent content = (AppFuncTabBasicContent) component;
			return content.getHomeComponent();
		}
		return null;
	}

	/**
	 * 获取TabComponent
	 * @return
	 */
	public AppFuncTabComponent getCurrentTabComponent() {
		return mAppFuncMainView.getTabComponent();
	}

	/**
	 * 获取选中的TabContent
	 * @return
	 */
	public XComponent getSelectedTabContent() {
		return mAppFuncMainView.getSeletedTabContentView();
	}

	/**
	 * 获取文件夹快捷栏实例
	 * @return
	 */
	public AppFuncFolderQuickAddBar getFolderQuickAddBar() {
		return mAppFuncMainView.getFolderQuickAddBar();
	}

	/**
	 * 获取正在运行操作栏实例
	 * @return
	 */
	public ProManageEditDock getProManageEditDock() {
		return mAppFuncMainView.getProManageEditDock();
	}

	public void locateAppInFolder(Intent intent) {
		Message msg = mHandler.obtainMessage(LOCATE_APP_IN_FOLDER);
		msg.obj = intent;
		mHandler.sendMessage(msg);
	}
}

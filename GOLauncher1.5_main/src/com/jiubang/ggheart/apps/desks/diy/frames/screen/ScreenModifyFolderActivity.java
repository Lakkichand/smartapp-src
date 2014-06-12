package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.SortUtils;
import com.go.util.log.LogConstants;
import com.go.util.window.OrientationControl;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AppTab;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.components.MultiCheckViewGroup;
import com.jiubang.ggheart.components.OnMultiItemClickedListener;
import com.jiubang.ggheart.components.diygesture.gesturemanageview.DiyGestureConstants;
import com.jiubang.ggheart.components.renamewindow.RenameActivity;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ThreadName;
import com.jiubang.ggheart.plugin.shell.IShellManager;
import com.jiubang.ggheart.plugin.shell.ShellPluginFactory;

/**
 * 修改文件夹列表
 * 
 * @author jiangxuwen
 * 
 */
public class ScreenModifyFolderActivity extends Activity implements OnMultiItemClickedListener {
	// 列表的所有元素
	private ArrayList<Object> mAllList = new ArrayList<Object>();
	// 文件夹已有的元素
	// private ArrayList<ItemInfo> mFolderItems;
	// 勾选列表提交值
	private ArrayList<Boolean> mBooleanListPre;
	// 勾选列表初始值,改用数组存储是因为相比列表占较少空间，且个数已确定
	private boolean[] mBooleanListFin;
	// 文件夹ID
	private long mFolderID;
	// 文件夹已有元素和桌面上快捷方式的总和
	private int mCount;
	// 新建文件夹的当前屏幕
	private int mCurScreen;
	private ScreenControler mScreenControler;
	private UserFolderInfo mFolderInfo;
	private CharSequence mFolderName;
	// 是否为新建文件夹的标识
	private boolean mIsCreateFolder;
	// 是否需要删除mFolderInfo的标识
	private boolean mDeleteInfo = true;
	private boolean mIsDockFolder = false;
	private int mCreateType;
	private boolean mAddAppToDockUnfit; // 选择一个应用程序添加到非自适应dock条
	private boolean mGestureForApp; // 选择一个应用程序响应手势
	private boolean mDockAddApplicationGesture; // 选择一个应用程序响应dock手势
	private boolean mAddAppToDockFit; // 选择几个应用程序添加到自适应dock条
	private int mAddAppToDockFitMaxCount; // 最多可以添加几个
	private Bundle mBundle;
	private Handler mHandler;
	// private MyAdapter mAdapter;
	private Object mMutex;
	private int mCheckedNum = 0; // 目前勾选的数目
	// private ViewHolder viewHolder = new ViewHolder();
	private static final int INITFINISH = 1;
	private static final int COMMITFINISH = 2;
	public static final int MARKITEMCHANGED = 3; // 标记变化
	public static final int DOCKADDICON = 4; // dock条加应用程序
	private static final int GESTUREAPP = 5; // 响应手势的应用程序
	private static final int DOCKGESTUREAPP = 7; // 响应Dock手势的应用程序

	public static final int NOROOM = -1; // 标记变化
	public static final int RESETDATA = 6;
	// private static final int DELFINISH = 3;
	public static final int REMOVE_ITEMS = 1001;
	public static final int DELETE_FOLDER = 1002;
	public static final String CREATE_TYPE = "create_type";
	public static final String FOLDER_CREATE = "folder_create";
	public static final String FOLDER_ID = "folder_id";
	public static final String FOLDER_TITLE = "folder_title";
	public static final String FOLDER_CUR_SCREEN = "cur_screen";
	public static final String FOLDER_LOCATION = "location";
	public static final String FOLDER_DOCK = "dock_folder";
	public static final String ADD_APP_TO_DOCK_UNFIT = "add_app_to_dock_unfit";
	public static final String ADD_APP_TO_DOCK_FIT = "add_app_to_dock_fit";
	public static final String ADD_APP_TO_DOCK_FIT_MAXCOUNT = "add_app_to_dock_fit_maxcount";
	public static final String GESTURE_FOR_APP = "gesture_for_app";
	public static final String DOCK_ADD_APPLICATION_GESTURE = "dock_add_application_gesture";

	public static final String INTENT_STRING = "intent";

	private DeskButton mFinishButton, mCancle_btn;

	private MultiCheckViewGroup mMultiCheckViewGroup;

	// 效果设置
	private TextView mName;
	int mScreenCount[] = new int[1];
	public static final String INTENT_LIST_STRING = "intentlist";
	private boolean mNoRoom = false;
	private boolean mRenameDialogShowing = false; // 控制快速两次连击，弹出两个rename框
	private GoProgressBar mGoProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.screen_modify_grid);
		mCount = 0;
		mMutex = new Object();
		initHandler();
		mBundle = getIntent().getExtras();
		if (ShellPluginFactory.isUseShellPlugin(getApplicationContext())) {
			IShellManager manager = ShellPluginFactory.getShellManager();
			if (manager == null) {
				return;
			} else {
				mScreenControler = manager.getScreenControler();
				if (mScreenControler == null) {
					return;
				}
			}
		} else {
			ScreenFrame screenFrame = (ScreenFrame) GoLauncher.getFrame(IDiyFrameIds.SCREEN_FRAME);
			if (screenFrame == null) {
				return;
			}
			mScreenControler = screenFrame.mControler;
		}
		
		mGoProgressBar = (GoProgressBar) findViewById(R.id.modify_progress);
		mMultiCheckViewGroup = (MultiCheckViewGroup) findViewById(R.id.multi_check_viewgroup);
		mMultiCheckViewGroup.setMultiItemClickedListener(this);
		
		if (mBundle != null) {
			// 是否为新建文件夹
			mIsCreateFolder = mBundle.getBoolean(FOLDER_CREATE);
			mIsDockFolder = mBundle.getBoolean(FOLDER_DOCK);
			mCreateType = mBundle.getInt(CREATE_TYPE);
			mAddAppToDockUnfit = mBundle.getBoolean(ADD_APP_TO_DOCK_UNFIT);
			mAddAppToDockFit = mBundle.getBoolean(ADD_APP_TO_DOCK_FIT);
			mGestureForApp = mBundle.getBoolean(GESTURE_FOR_APP);
			mDockAddApplicationGesture = mBundle.getBoolean(DOCK_ADD_APPLICATION_GESTURE);

			if (mGestureForApp) {
				mFolderName = getText(R.string.gesture_for_app);
				mMultiCheckViewGroup.setIsSingleCheckType(true);
			} else if (mAddAppToDockUnfit) {
				// 选择一个应用程序添加到dock条
				mFolderName = getText(R.string.add_app_to_dock);
				mMultiCheckViewGroup.setIsSingleCheckType(true);
			} else if (mAddAppToDockFit) {
				mFolderName = getText(R.string.add_app_to_dock);
				mAddAppToDockFitMaxCount = mBundle.getInt(ADD_APP_TO_DOCK_FIT_MAXCOUNT);
			} else if (mDockAddApplicationGesture) {
				// add by zhengxiangcan 选择一个应用程序添加到dock栏图标手势
				mFolderName = getText(R.string.dock_add_application_gesture);
				mMultiCheckViewGroup.setIsSingleCheckType(true);
			} else if (mCreateType == AppTab.APP_ADD_TAB_ADD) {
				// 添加应用程序
				mFolderName = getText(R.string.tab_add_app);
			} else {
				if (mIsCreateFolder) {
					// findViewById(R.id.delete_btn).setVisibility(View.GONE);
					mCurScreen = mBundle.getInt(FOLDER_CUR_SCREEN);
					mFolderInfo = new UserFolderInfo();
					mFolderInfo.mTitle = getText(R.string.folder_name);
					mFolderName = mFolderInfo.mTitle;
					if (!mIsDockFolder) {
						int[] xy = mBundle.getIntArray(FOLDER_LOCATION);
						mFolderInfo.mCellX = xy[0];
						mFolderInfo.mCellY = xy[1];
						mScreenControler.addDesktopItem(mCurScreen, mFolderInfo);
						mFolderID = mFolderInfo.mInScreenId;
						xy = null;
					} else {
						mFolderInfo.mInScreenId = System.currentTimeMillis();
						mFolderID = mFolderInfo.mInScreenId;
					}

				} else {
					mFolderID = mBundle.getLong(FOLDER_ID);
					mFolderName = mBundle.getCharSequence(FOLDER_TITLE);
				}

			}
			initList();
		}

		mName = (TextView) findViewById(R.id.name);
		mName.setText(mFolderName);
		mName.setSingleLine(true);
		mName.setEllipsize(TruncateAt.MARQUEE);
		mName.setMarqueeRepeatLimit(-1);
		mName.setFocusableInTouchMode(true);

		mFinishButton = (DeskButton) findViewById(R.id.finish_btn);
		mFinishButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 按下完成按钮的话就不需要删除
				mDeleteInfo = false;
				String name = (String) mName.getText();
				handleElments(name);
			}
		});

		// add by zhengxiangcan 添加应用程序到dock或者为dock图标手势选择应用程序，确定按钮不可见
		if (mAddAppToDockUnfit || mDockAddApplicationGesture) {
			mFinishButton.setVisibility(View.GONE);
		}

		// 手势选择应用程序
		if (mGestureForApp || mAddAppToDockFit || mIsCreateFolder
				|| (mCreateType == AppTab.APP_ADD_TAB_ADD && mCheckedNum < 1)) {
			mFinishButton.setEnabled(false);
			mFinishButton.setTextColor(0XFFB9B9B9);
		}

		mCancle_btn = (DeskButton) findViewById(R.id.cancle_btn);
		mCancle_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});

		ImageView rename = (ImageView) findViewById(R.id.rename);
		rename.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mRenameDialogShowing) {
					return;
				}

				mRenameDialogShowing = true;
				Intent intent = new Intent(ScreenModifyFolderActivity.this, RenameActivity.class);
				CharSequence title = (null != mName.getText()) ? mName.getText().toString() : null;
				intent.putExtra(RenameActivity.NAME, title);
				intent.putExtra(RenameActivity.SHOW_RECOMMENDEDNAME, true);
				intent.putExtra(RenameActivity.FINISH_WHEN_CHANGE_ORIENTATION, true);
				startActivityForResult(intent, IRequestCodeIds.REQUEST_RENAME);
			}
		});
		if (mCreateType == AppTab.APP_ADD_TAB_ADD || mAddAppToDockUnfit || mAddAppToDockFit
				|| mGestureForApp || mDockAddApplicationGesture) {
			// jiangchao 修改不可见
			rename.setVisibility(View.GONE);
			// jiang
		}
		// 计算当前屏的空间数
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_GET_VANCANT_COUNT, -1, mScreenCount, null);

	}

	// 处理列表元素
	private void handleElments(final String name) {
		// 显示提示框
		showProgressDialog();
		mInitThread = new Thread(ThreadName.RENAME_SCREEN_ITEM) {
			@Override
			public void run() {
				synchronized (mMutex) {
					if (mGestureForApp) {
						handleAppForGesture();
					} else if (mAddAppToDockUnfit) {
						handleOneElmentsForApp();
					} else if (mAddAppToDockFit) {
						handleElmentsForDockFit();
					} else if (mDockAddApplicationGesture) {
						// add by zhengxiangcan dock栏图标手势添加应用程序
						handleAppForDockGesture();
					} else if (mCreateType == AppTab.APP_ADD_TAB_ADD) {
						handleElmentsForApp();
					} else {
						// 是否需要刷新文件夹的标识
						boolean needRefresh = false;
						int count = mAllList.size();
						// 要添加的元素
						ArrayList<AppItemInfo> addItemInfos = new ArrayList<AppItemInfo>();
						// 要删除的元素
						ArrayList<ItemInfo> delItemInfos = new ArrayList<ItemInfo>();
						for (int i = 0; i < count; i++) {
							if (mBooleanListPre.get(i) != mBooleanListFin[i]) {
								// 添加新增元素
								if (mBooleanListPre.get(i)) {
									if (mAllList.get(i) instanceof AppItemInfo) {
										addItemInfos.add((AppItemInfo) mAllList.get(i));
									}
								}
								// 添加要删除的元素
								else {
									delItemInfos.add((ItemInfo) mAllList.get(i));
								}
							}
						} // end for

						if (addItemInfos.size() > 0) {
							final ArrayList<ItemInfo> contents = getItemInfoList(addItemInfos);
							mScreenControler.addUserFolderContent(mFolderID, contents, false);
							if (!mIsCreateFolder) {
								// 刷新标识有效
								needRefresh = true;
							}
						}

						if (delItemInfos.size() > 0) {
							mScreenControler
									.removeUserFolderContent(mFolderID, delItemInfos, false);
							if (!mIsCreateFolder) {
								// 刷新标识有效
								needRefresh = true;
							}
						}

						// 处理文件名对话框
						if (name != null) {
							if (mFolderName != null && !mFolderName.equals(name)) {
								//修复(ADT-7213) 在文件夹添加应用的界面重命名文件夹时，令文件夹名字为空，确定回到桌面后文件夹名字没有改变
								//屏蔽判空条件 Edit by zzf
//								if (!name.trim().equals("")) {
									// 通知桌面重命名
									ArrayList<String> nameList = new ArrayList<String>();
									// 第一个为新的名字
									nameList.add(name);
									// 第二个为以前的名字
									nameList.add(mFolderName.toString());
									// 如果是新建文件夹
									if (mIsCreateFolder) {
										mFolderInfo.mTitle = name;
										mFolderInfo.setFeatureTitle(name);
										mScreenControler.updateDesktopItem(mCurScreen, mFolderInfo);
									} else {
										// 异步消息发送
										GoLauncher.sendHandler(this, IDiyFrameIds.SCREEN_FRAME,
												IDiyMsgIds.SCREEN_FOLDER_RENAME, 0, mFolderID,
												nameList);
										GoLauncher.sendHandler(this,
												IDiyFrameIds.DESK_USER_FOLDER_FRAME,
												IDiyMsgIds.SCREEN_FOLDER_RENAME, 0, mFolderID,
												nameList);
									}
//								}// end if
							}
						}

						// 如果刷新标识有效
						if (needRefresh) {
							if (mIsDockFolder) {
								GoLauncher.sendHandler(this, IDiyFrameIds.DOCK_FRAME,
										IDiyMsgIds.REFRASH_FOLDER_CONTENT, mCheckedNum, mFolderID,
										null);
							} else {
								GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
										IDiyMsgIds.SCREEN_FOLDER_ADDITEMS, 1, mFolderID, null);
							}
						}

						if (addItemInfos != null) {
							addItemInfos.clear();
							addItemInfos = null;
						}

						Message message = mHandler.obtainMessage();
						message.what = COMMITFINISH;
						mHandler.sendMessage(message);
					}
				}
			}
		};
		mInitThread.start();

	}

	/**
	 * 处理添加应用程序到dock图标手势
	 */
	private void handleAppForDockGesture() {
		if (mCheckedNum > 0) {
			int size = mAllList.size();
			AppItemInfo appItemInfo = null;
			for (int index = 0; index < size; index++) {
				if (mBooleanListPre.get(index)) {
					appItemInfo = (AppItemInfo) mAllList.get(index);
					break;
				}
			}
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			if (appItemInfo != null) {
				bundle.putParcelable(INTENT_STRING, appItemInfo.mIntent);
			}
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	/**
	 * 处理添加应用程序
	 */
	private void handleElmentsForApp() {
		if (mCheckedNum > 0) {
			int size = mAllList.size();
			// 要添加的元素
			ArrayList<Intent> mIntents = new ArrayList<Intent>();
			AppItemInfo appItemInfo = null;
			for (int index = 0; index < size; index++) {
				// if (mBooleanListFin[index]) {
				if (mBooleanListPre.get(index)) {
					appItemInfo = (AppItemInfo) mAllList.get(index);
					mIntents.add(appItemInfo.mIntent);
				}
			}

			Intent i = getIntent();
			Bundle b = new Bundle();
			b.putParcelableArrayList(INTENT_LIST_STRING, mIntents);

			i.putExtras(b);
			setResult(RESULT_OK, i);
			finish();
		}
	}

	/**
	 * <br>功能简述:处理添加几个应用程序到自适应dock条
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void handleElmentsForDockFit() {
		int count = mAllList.size();
		// 要添加的元素
		ArrayList<AppItemInfo> addItemInfos = new ArrayList<AppItemInfo>();
		for (int i = 0; i < count; i++) {
			if (mBooleanListPre.get(i)) {
				// 添加新增元素
				if (mAllList.get(i) instanceof AppItemInfo) {
					addItemInfos.add((AppItemInfo) mAllList.get(i));
				}
			}
		}
		GoLauncher.sendHandler(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.ADD_APPS_TO_DOCK_FIT, -1,
				null, addItemInfos);
		finish();
	}

	/**
	 * 处理添加一个应用程序到dock条
	 */
	private void handleOneElmentsForApp() {
		if (mCheckedNum > 0) {
			int size = mAllList.size();
			// 要添加的元素
			AppItemInfo appItemInfo = null;
			for (int index = 0; index < size; index++) {
				// if (mBooleanListFin[index]) {
				if (mBooleanListPre.get(index)) {
					appItemInfo = (AppItemInfo) mAllList.get(index);
					break;
				}
			}
			if (appItemInfo != null) {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
						IDiyMsgIds.DOCK_ADD_APPLICATION, -1, appItemInfo, null);
			}
			finish();
		}
	}

	/**
	 * 处理添加一个应用响应手势
	 */
	private void handleAppForGesture() {
		if (mCheckedNum > 0) {
			int size = mAllList.size();
			// 要添加的元素
			AppItemInfo appItemInfo = null;
			for (int index = 0; index < size; index++) {
				// if (mBooleanListFin[index]) {
				if (mBooleanListPre.get(index)) {
					appItemInfo = (AppItemInfo) mAllList.get(index);
					break;
				}
			}
			if (appItemInfo != null) {
				Intent intent = new Intent();
				intent.putExtra(DiyGestureConstants.APP_INTENT, appItemInfo.mIntent);
				intent.putExtra(DiyGestureConstants.APP_NAME, appItemInfo.mTitle);
				setResult(RESULT_OK, intent);

			}
			finish();
		}
	}

	private void showProgressDialog() {
		if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.INVISIBLE) {
			mGoProgressBar.setVisibility(View.VISIBLE);
		}
	}

	private void dismissProgressDialog() {
		if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.VISIBLE) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	private Thread mInitThread;

	private void start() {
		mInitThread = new Thread(ThreadName.INIT_MODIFY_FOLDER_APP_LIST) {
			@Override
			public void run() {
				synchronized (mMutex) {
					// 初始化3个列表
					// if (mFolderItems == null) {
					// mFolderItems = new ArrayList<ItemInfo>();
					// }
					if (mAllList == null) {
						mAllList = new ArrayList<Object>();
					}
					if (mBooleanListPre == null) {
						mBooleanListPre = new ArrayList<Boolean>();
					}
					// 先清空
					mAllList.clear();
					mBooleanListPre.clear();

					if (mCreateType != AppTab.APP_ADD_TAB_ADD && !mAddAppToDockUnfit
							&& !mAddAppToDockFit && !mGestureForApp) {
						// 获取文件夹的资源列表
						ArrayList<ItemInfo> items = mScreenControler.getFolderItems(mFolderID);
						// 添加已存在的元素
						if (items != null) {
							// ArrayList<ItemInfo> appsInFolder = mFolderItems;
							if (items.size() > 0) {
								try {
									// SortUtils.sortItemInfo(items,
									// ScreenModifyFolderActivity.this);
									SortUtils.sort(items, "getTitle", null, null, "ASC");
								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								}
								for (ItemInfo info : items) {
									// 列表元素个数递增
									mCount++;
									mAllList.add(info);
									mBooleanListPre.add(true);
								}
							}
						}

					}
					final AppDataEngine engine = GOLauncherApp.getAppDataEngine();

					ArrayList<AppItemInfo> list2 = engine.getCompletedAppItemInfosExceptHide();
					if (list2.size() > 0) {
						try {
							SortUtils.sort(list2, "getTitle", null, null, null);
						} catch (IllegalArgumentException e) {
							// 可能因为用户手机Java运行时环境的问题出错
							e.printStackTrace();
						}
						for (AppItemInfo info : list2) {

							if (info.mIntent != null && info.mIntent.getComponent() != null) {
								if (!isExist(info.mIntent.getComponent())) {
									mAllList.add(info);
									mBooleanListPre.add(false);
								}
							} // end if info

						} // end for
					}
					// 克隆一份勾选列表
					int booleanSize = mBooleanListPre.size();
					mBooleanListFin = new boolean[booleanSize];
					// mGridView.setBoolTable(mBooleanListFin);
					boolean value = false;
					for (int i = 0; i < booleanSize; i++) {
						value = mBooleanListPre.get(i);
						mBooleanListFin[i] = value;
						if (value) {
							mCheckedNum++;
						}
					}
					Message message = mHandler.obtainMessage();
					message.what = INITFINISH;
					mHandler.sendMessage(message);
				}
			}

		};
		mInitThread.start();
		mInitThread = null;
	}

	@SuppressWarnings("unchecked")
	public void initList() {
		// 显示提示框
		showProgressDialog();
		start();
		mInitThread = null;
	}

	// 判断是否已添加进列表
	private boolean isExist(ComponentName cName) {
		for (int i = 0; i < mCount; i++) {
			final Intent intent = ((ShortCutInfo) mAllList.get(i)).mIntent;
			if (intent == null) {
				continue;
			}
			final ComponentName componentName = intent.getComponent();
			if (componentName == null) {
				continue;
			}
			if (cName.equals(componentName)) {
				return true;
			}
		}
		return false;
	}

	private ArrayList<ItemInfo> getItemInfoList(ArrayList<AppItemInfo> infos) {
		if (null == infos) {
			return null;
		}

		ArrayList<ItemInfo> rets = new ArrayList<ItemInfo>();

		int sz = infos.size();
		for (int i = 0; i < sz; i++) {
			AppItemInfo info = infos.get(i);
			if (null == info) {
				continue;
			}
			ShortCutInfo ret = new ShortCutInfo();
			ret.mId = -1;
			ret.mIcon = info.mIcon;
			ret.mIntent = info.mIntent;
			ret.mItemType = IItemType.ITEM_TYPE_APPLICATION;
			ret.mSpanX = 1;
			ret.mSpanY = 1;
			ret.mTitle = info.mTitle;
			rets.add(ret);
		}

		return rets;
	}

	@Override
	protected void onDestroy() {
		synchronized (mMutex) {
			dismissProgressDialog();
			super.onDestroy();
			cleanHandlerMsg();
			// 如果是新建文件夹并且没有按下完成按钮
			if (mDeleteInfo && mIsCreateFolder) {
				// 删除新建文件夹的数据
				mScreenControler.removeDesktopItem(mFolderInfo);
			}

			if (mAllList != null) {
				mAllList.clear();
			}

			if (mBooleanListPre != null) {
				mBooleanListPre.clear();
				mBooleanListPre = null;
			}
			mBooleanListFin = null;
			// 释放资源反注册mFinishButton，icon里包含的TextFont
			try {
				if (mFinishButton != null) {
					mFinishButton.selfDestruct();
					mFinishButton = null;
				}
				if (mCancle_btn != null) {
					mCancle_btn.selfDestruct();
					mCancle_btn = null;
				}
				if (mMultiCheckViewGroup != null) {
					mMultiCheckViewGroup.recyle();
					mMultiCheckViewGroup = null;
				}
				// mScreenControler = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case NOROOM : {
						int toastId = mAddAppToDockFit
								? R.string.dock_is_full
								: R.string.tab_add_no_more_room;
						ScreenUtils.showToast(toastId, ScreenModifyFolderActivity.this);
					}
						break;
					case INITFINISH : {
						// 取消加载框
						mMultiCheckViewGroup.setContentList(mAllList, mBooleanListPre);
						dismissProgressDialog();
						break;
					}
					case COMMITFINISH : {
						// 取消加载框
						dismissProgressDialog();
						if (mIsCreateFolder) {
							if (mIsDockFolder) {
								GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
										IDiyMsgIds.CREATE_DESK_USERFOLDER, 0, mFolderInfo, null);
							} else {

								GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
										IDiyMsgIds.CREATE_DESK_USERFOLDER, 0, mFolderInfo, null);
								// 刷新
								GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
										IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, REMOVE_ITEMS,
										mFolderID, null);
							}
						}
						setResult(RESULT_OK);
						finish();
						break;
					}
					case MARKITEMCHANGED : {
						if (msg.arg1 == -1) {
							--mCheckedNum;
						} else {
							++mCheckedNum;
						}
						if ((mIsCreateFolder && mCheckedNum <= 1)
								|| ((mCreateType == AppTab.APP_ADD_TAB_ADD || mAddAppToDockFit) && mCheckedNum < 1)) {
							mFinishButton.setEnabled(false);
							mFinishButton.setTextColor(0XFFB9B9B9);
						} else {
							mFinishButton.setEnabled(true);
							mFinishButton.setTextColor(0XFF343434);
						}
						if (mCreateType == AppTab.APP_ADD_TAB_ADD) {
							mNoRoom = mCheckedNum >= mScreenCount[0];
						} else if (mIsCreateFolder) {
							mNoRoom = mScreenCount[0] < 1;
						} else if (mAddAppToDockFit) {
							mNoRoom = mCheckedNum >= mAddAppToDockFitMaxCount;
						}

						break;
					}
					case DOCKADDICON : {
						// 选中的应用程序添加到dock条上
						String name = (String) mName.getText();
						handleElments(name);
						break;
					}

						// 手势选择应用
					case GESTUREAPP : {
						if (mGestureForApp && mCheckedNum <= 0) {
							mFinishButton.setEnabled(false);
							mFinishButton.setTextColor(0XFFB9B9B9);
						} else {
							mFinishButton.setEnabled(true);
							mFinishButton.setTextColor(0XFF343434);
						}
						break;
					}

						// 为dock手势添加应用
					case DOCKGESTUREAPP : {
						String name = (String) mName.getText();
						handleElments(name);
						break;
					}
				}
			}
		};
	}

	private void cleanHandlerMsg() {
		if (mHandler != null) {
			mHandler.removeMessages(INITFINISH);
			mHandler.removeMessages(COMMITFINISH);
			// mHandler.removeMessages(DELFINISH);

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 检查屏幕翻转设置，并应用
		OrientationControl.setOrientation(this);
	}

	public ArrayList<Object> getAllList() {
		return mAllList;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case IRequestCodeIds.REQUEST_RENAME :
				if (resultCode == RESULT_OK) {
					String name = data.getStringExtra(RenameActivity.NAME);
					mName.setText(name);
				}
				mRenameDialogShowing = false;
				break;

			default :
				break;
		}
	}

	@Override
	public void onMultiItemClicked(int position, boolean isSelected) {
		Message msg = new Message();
		msg.what = ScreenModifyFolderActivity.MARKITEMCHANGED;

		if (isSelected && mNoRoom) {
			mBooleanListPre.set(position, false);
			msg.what = NOROOM;
			mHandler.sendMessage(msg);
			return;
		}
		// mBooleanListFin[position] = isSelected;
		if (isSelected) {
			msg.arg1 = 1;
		} else {
			msg.arg1 = -1;
		}
		if (mAddAppToDockUnfit && mCheckedNum == 0) { // 如果是往dock上添加图标，则在标记完第一个后就关掉编辑框
			++mCheckedNum;
			msg.what = DOCKADDICON;
			mHandler.sendMessage(msg);
			return;
		}

		if (mDockAddApplicationGesture && mCheckedNum == 0) { // 为dock图标手势选择应用，在标记完第一个后关闭编辑框
			++mCheckedNum;
			msg.what = DOCKGESTUREAPP;
			mHandler.sendMessage(msg);
			return;
		}

		// 手势选择应用
		if (mGestureForApp) {
			if (isSelected) {
				mCheckedNum = 1;
			} else {
				mCheckedNum = 0;
			}
			msg.what = GESTUREAPP;
		}
		mHandler.sendMessage(msg);
	}
	
	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}

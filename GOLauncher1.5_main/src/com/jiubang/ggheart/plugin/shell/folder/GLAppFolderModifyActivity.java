package com.jiubang.ggheart.plugin.shell.folder;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.SortUtils;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.common.controler.CommonControler;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.components.MultiCheckViewGroup;
import com.jiubang.ggheart.components.OnMultiItemClickedListener;
import com.jiubang.ggheart.components.renamewindow.RenameActivity;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 新增或修改文件夹的activity
 * 
 * @author dingzijian
 * 
 */
public class GLAppFolderModifyActivity extends Activity
		implements
			OnMultiItemClickedListener,
			OnClickListener {
	/**
	 * 显示程序的组件列表
	 */
	private ArrayList<Object> mList;
	/**
	 * 选钩列表
	 */
	private ArrayList<Boolean> mBooleanList;
	/**
	 * 当前修改的文件夹ID，新建文件夹的时候这个值为-1
	 */
	private long mFolderID = -1;

	private GLAppFolderController mFolderController;

	private AppDrawerControler mDrawerControler;

	private FunFolderItemInfo mFunFolderItemInfo;

	private String mFolderName;

	//	private boolean mIsCreateFolder;

	private int mFolderAction = -1;

	private Handler mHandler;

	private Object mMutex;
	private int mCheckedNum = 0; // 目前勾选的数目

	private RelativeLayout mContentLayout;
	private MultiCheckViewGroup mMultiCheckViewGroup;

	private DeskButton mFinishButton;
	private DeskButton mCancleButton;
	private TextView mNameText;

	private GLAppFolderInfo mAppFolderInfo;

	public static final String FOLDER_ID = "folder_id";
	
	public static final String FOLDER_NAME = "folder_name";
	
	public static final String APPDRAWER_FOLDER_TYPE = "appdrawer_folder_type";

	public static final int CREATE_FOLDER = 0x01;

	public static final int MODITY_FOLDER = 0x02;

	private static final int LOAD_APPS_FINISH = 0x03;

	private static final int HANDLE_APPS_FINISH = 0x04;

	public static final int MARK_ITEM_CHANGED = 0x05; // 标记变化

	public static final int MSG_NO_ITEMS = 0x06; // 标记变化

	private static final int MSG_FOLDER_EMPTY = 0x07;
	/**
	 * ok按钮是否点击过的标志，避免重复点击重复响应
	 */
	//	private boolean mHasSubmited = false;

	private GoProgressBar mGoProgressBar;

	/**
	 * 传进来编辑的文件夹是空的(以防万一，按取消就清掉)
	 */
	//	private boolean mIsEmptyFolder;
	private int mAppDrawerFolderType = FunFolderItemInfo.TYPE_NORMAL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mList = new ArrayList<Object>();
		mMutex = new Object();
		mFolderController = GLAppFolderController.getInstance();
		mDrawerControler = AppDrawerControler.getInstance(GOLauncherApp.getContext());

		getFolderAction();

		initView();

		initHandler();

		initData();

	}
	/**
	 * <br>功能简述:获取是修改文件夹还是创建新的文件夹
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void getFolderAction() {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mFolderID = bundle.getLong(FOLDER_ID, -1);
			mFolderAction = mFolderID != -1 && mFolderID > 0 ? MODITY_FOLDER : CREATE_FOLDER;
			mFolderName = bundle.getString(FOLDER_NAME);
			mAppDrawerFolderType = bundle.getInt(APPDRAWER_FOLDER_TYPE, FunFolderItemInfo.TYPE_NORMAL);
		} else {
			mFolderAction = CREATE_FOLDER;
			mFolderName = getString(R.string.folder_name);
		}
	}

	private void initData() {
		switch (mFolderAction) {
			case CREATE_FOLDER : {
				loadAppListForCreate();
				break;
			}
			case MODITY_FOLDER : {
				//TODO 修改文件夹
				break;
			}
			default :
				break;
		}
	}

	private void initView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTitle(R.string.app_fun_edit_folder_title);
		setContentView(R.layout.app_func_modify_folder_list);
		mGoProgressBar = (GoProgressBar) findViewById(R.id.appfunc_modify_progress);
		mContentLayout = (RelativeLayout) findViewById(R.id.contentview);
		mMultiCheckViewGroup = (MultiCheckViewGroup) findViewById(R.id.multi_check_viewgroup);
		mMultiCheckViewGroup.setMultiItemClickedListener(this);
		mNameText = (TextView) findViewById(R.id.appfunc_folder_name);
		mNameText.setSingleLine(true);
		mNameText.setEllipsize(TruncateAt.MARQUEE);
		mNameText.setMarqueeRepeatLimit(-1);
		mNameText.setFocusableInTouchMode(true);
		mFinishButton = (DeskButton) findViewById(R.id.finish_btn);
		mFinishButton.setOnClickListener(this);
		mCancleButton = (DeskButton) findViewById(R.id.cancle_btn);
		mCancleButton.setOnClickListener(this);
		ImageView rename = (ImageView) findViewById(R.id.rename);
		rename.setOnClickListener(this);
//		String folderName = "";
		switch (mFolderAction) {
			case CREATE_FOLDER : {
//				folderName = getString(R.string.folder_name);
				mFinishButton.setEnabled(false);
				mFinishButton.setTextColor(0XFFB9B9B9);
				break;
			}
			case MODITY_FOLDER : {
				//TODO 修改文件夹
				break;
			}
			default :
				break;
		}
		if (mFolderName == null || mFolderName.trim().equals("")) {
			mFolderName = getString(R.string.folder_name);
		}
		mNameText.setText(mFolderName);

	}
	private void loadAppListForCreate() {
		// 显示提示框
		showProgressDialog();

		new Thread(ThreadName.INIT_NEW_FOLDER_APP_LIST) {
			@Override
			public void run() {
				synchronized (mMutex) {
					mList.clear();
					if (mBooleanList == null) {
						mBooleanList = new ArrayList<Boolean>();
					}
					mBooleanList.clear();
					// 通过Intent组装一个Info列表
					LinkedList<FunAppItemInfo> arrayList = (LinkedList<FunAppItemInfo>) mDrawerControler
							.getFunItemInfosExceptFolder();
					// 对组装好的Info列表进行排序
					if (arrayList.size() > 0) {
						SortUtils.sort(arrayList, "getTitle", null, null, null);
					} else {
						mHandler.sendEmptyMessage(MSG_NO_ITEMS);
					}
					// // 把排序好的准备加入文件夹的程序放入选择列表
					for (FunAppItemInfo info : arrayList) {
						mList.add(info);
					}
					if (mBooleanList == null) {
						mBooleanList = new ArrayList<Boolean>();
					}
					mBooleanList.clear();
					if (mList != null) {
						int size = mList.size();
						for (int i = 0; i < size; i++) {
							mBooleanList.add(false);
						}
					}
					Message message = mHandler.obtainMessage(LOAD_APPS_FINISH);
					mHandler.sendMessage(message);
				}
			}
		}.start();
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

	@SuppressWarnings("unchecked")
	public void initAppListForModify() {
		// 显示提示框
		showProgressDialog();

		new Thread(ThreadName.INIT_MODIFY_FOLDER_APP_LIST) {
			@Override
			public void run() {
				synchronized (mMutex) {
					mList.clear();
					if (mBooleanList == null) {
						mBooleanList = new ArrayList<Boolean>();
					}
					mBooleanList.clear();
					if (mFunFolderItemInfo != null) {
						ArrayList<FunAppItemInfo> appsInFolder = (ArrayList<FunAppItemInfo>) mFunFolderItemInfo
								.getFolderContentExceptHide();
						if (appsInFolder.size() > 0) {
							SortUtils.sort(appsInFolder, "getTitle", null, null, null);
							for (FunAppItemInfo info : appsInFolder) {
								mList.add(info);
								mBooleanList.add(true);
								mCheckedNum++;
							}
						}
						//						else {
						//							mIsEmptyFolder = true;
						//						}
					}
					LinkedList<FunAppItemInfo> list = (LinkedList<FunAppItemInfo>) mDrawerControler
							.getFunItemInfosExceptFolder();
					if (list.size() > 0) {
						try {
							SortUtils.sort(list, "getTitle", null, null, null);
						} catch (IllegalArgumentException e) {
							// 可能因为用户手机Java运行时环境的问题出错
							e.printStackTrace();
						}
						for (FunAppItemInfo info : list) {
							mList.add(info);
							mBooleanList.add(false);
						}
					}

					Message message = mHandler.obtainMessage();
					message.what = LOAD_APPS_FINISH;
					mHandler.sendMessage(message);
				}
			}
		}.start();
	}

	@Override
	protected void onDestroy() {
		synchronized (mMutex) {
			dismissProgressDialog();
			super.onDestroy();
			try {
				// 释放资源反注册mFinishButton，icon里包含的TextFont
				if (mFinishButton != null) {
					mFinishButton.selfDestruct();
					mFinishButton = null;
				}
				if (mCancleButton != null) {
					mCancleButton.selfDestruct();
					mCancleButton = null;
				}
				if (mNameText != null && mNameText instanceof DeskTextView) {
					((DeskTextView) mNameText).selfDestruct();
					mNameText = null;
				}
				if (mMultiCheckViewGroup != null) {
					mMultiCheckViewGroup.recyle();
					mMultiCheckViewGroup = null;
				}
				mContentLayout = null;
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
					case LOAD_APPS_FINISH : {
						// 取消加载框
						dismissProgressDialog();
						if (mMultiCheckViewGroup != null) {
							mMultiCheckViewGroup.setContentList(mList, mBooleanList);
						}
						break;
					}
					case HANDLE_APPS_FINISH : {
						//						int count = 0;
						//						if (null != mBooleanList) {
						//							if (mBooleanList.isEmpty()) {
						//								count = 0;
						//							} else {
						//								for (Boolean bool : mBooleanList) {
						//									if (bool == Boolean.TRUE) {
						//										count++;
						//									}
						//								}
						//							}
						//						}

						//						if (!mIsCreateFolder) {
						//							if (count == 0) {
						//								ArrayList<AppItemInfo> removeList = null;
						//								try {
						//									removeList = AppFuncFrame.getFunControler().removeFolder(
						//											mFunFolderItemInfo);
						//									GoLauncher.sendMessage(
						//											GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
						//											IDiyFrameIds.SCREEN_FRAME,
						//											IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, -1,
						//											mFunFolderItemInfo.getFolderId(), removeList);
						//									GoLauncher.sendHandler(
						//											GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
						//											IDiyFrameIds.DOCK_FRAME,
						//											IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, -1,
						//											mFunFolderItemInfo.getFolderId(), removeList);
						//
						//									// 主动刷屏
						//									AppFuncHandler.getInstance().refreshGrid();
						//								} catch (DatabaseException e) {
						//									AppFuncExceptionHandler.handle(e);
						//								}
						//							}
						//						} else if (mNewFolderType != FunFolderItemInfo.TYPE_NORMAL) {
						//							// 创建的文件夹是从顶部操作栏点击生产的
						//							DeliverMsgManager.getInstance().onChange(
						//									AppFuncConstants.APP_FUNC_FOLDER_QUICK_ADD_BAR,
						//									AppFuncConstants.SET_SPECIAL_FOLDER_DISMISS, mNewFolderType);
						//						}
						dismissProgressDialog();
						setResult(Activity.RESULT_OK);
						finish();
						break;
					}
					case MARK_ITEM_CHANGED : {
						if (msg.arg1 == -1) {
							--mCheckedNum;
						} else {
							++mCheckedNum;
						}
						if (mCheckedNum < 1) {
							mFinishButton.setEnabled(false);
							mFinishButton.setTextColor(0XFFB9B9B9);
						} else {
							mFinishButton.setEnabled(true);
							mFinishButton.setTextColor(0XFF343434);
						}
						break;
					}
					case MSG_NO_ITEMS : {
						Toast.makeText(GLAppFolderModifyActivity.this,
								R.string.creat_folder_no_icon_tip, Toast.LENGTH_SHORT).show();
						finish();
					}
						break;

				}
			}
		};
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mContentLayout != null) {
			android.view.ViewGroup.LayoutParams layoutParams = mContentLayout.getLayoutParams();
			layoutParams.height = (int) getResources()
					.getDimension(R.dimen.folder_edit_view_height);
			layoutParams.width = (int) getResources().getDimension(R.dimen.folder_edit_view_width);
			mContentLayout.setLayoutParams(layoutParams);
		}
		if (mMultiCheckViewGroup != null) {
			mMultiCheckViewGroup.onConfigurationChanged();
		}
	}

	// 处理列表元素
	private void handleElmentsForCreate(final String name) {

		//		if (!mHasSubmited) {
		//			mHasSubmited = true;
		showProgressDialog();
		new Thread("create folder") {

			@Override
			public void run() {

				Looper.prepare();
				synchronized (mMutex) {
					ArrayList<FunAppItemInfo> list = new ArrayList<FunAppItemInfo>();
					ArrayList<AppItemInfo> appItemInfos = new ArrayList<AppItemInfo>();
					int size = mBooleanList.size();
					for (int i = 0; i < size; i++) {
						if (mBooleanList.get(i).booleanValue()) {
							FunAppItemInfo itemInfo = (FunAppItemInfo) mList.get(i);
							list.add(itemInfo);
							appItemInfos.add(itemInfo.getAppItemInfo());
						}
					}
					boolean needName = mNameText.getText().equals(getString(R.string.folder_name))
							? true
							: false;
					String folderName = "";
					if (needName) {
						folderName = CommonControler.getInstance(GLAppFolderModifyActivity.this)
								.generateFolderName(appItemInfos);
					} else {
						folderName = getString(R.string.folder_name);
					}
					mFolderController.createAppDrawerFolder(list, folderName);
					Message message = mHandler.obtainMessage();
					message.what = HANDLE_APPS_FINISH;
					mHandler.sendMessage(message);

				}
				Looper.myLooper().quit();
			}
		}.start();
	}

	private void handleElmentsForModify(String name) {
		if (mFunFolderItemInfo != null) {
			if (mFolderName != null && !mFolderName.equals(name)) {
				if (!name.trim().equals("")) {
					notifyFolderNameChanged(name);
				}
			}

			showProgressDialog();
			// //屏蔽掉loadding框的返回事件
			// if (mProgressDialog!=null) {
			// mProgressDialog.setOnKeyListener(new
			// DialogInterface.OnKeyListener() {
			//
			// @Override
			// public boolean onKey(DialogInterface dialoginterface, int i,
			// KeyEvent keyevent) {
			// return true;
			// }
			// });
			// }

			new Thread("move icon to folder") {

				@Override
				public void run() {

					Looper.prepare();
					synchronized (mMutex) {
						ArrayList<FunAppItemInfo> list = new ArrayList<FunAppItemInfo>();
						ArrayList<AppItemInfo> appItemInfos = new ArrayList<AppItemInfo>();
						for (int i = 0; i < mBooleanList.size(); i++) {
							try {
								FunAppItemInfo itemInfo = (FunAppItemInfo) mList.get(i);
								list.add(itemInfo);
								if (mBooleanList.get(i).booleanValue()) {
									appItemInfos.add(itemInfo.getAppItemInfo());
								}
							} catch (Exception e) {
								// TODO: handle exception
							}
						}
						if (mNameText.getText().equals(getString(R.string.folder_name))) {
							String folderName = CommonControler.getInstance(
									GLAppFolderModifyActivity.this)
									.generateFolderName(appItemInfos);
							notifyFolderNameChanged(folderName);
						}

						//TODO 修改文件夹内容
						//								AppFuncHandler.getInstance().modifyFolder(list, mBooleanList,
						//										mFunFolderItemInfo, false);
						Message message = mHandler.obtainMessage();
						message.what = HANDLE_APPS_FINISH;
						if (!mBooleanList.contains(true)) {
							message.arg1 = MSG_FOLDER_EMPTY;
						}
						mHandler.sendMessage(message);
					}
					Looper.myLooper().quit();
				}
			}.start();

		}
		//	}

	}

	private void notifyFolderNameChanged(final String name) {
		if (mFunFolderItemInfo == null) {
			return;
		}
		try {
			mFunFolderItemInfo.setTitle(name);
		} catch (DatabaseException e) {
			//			Message message = mHandler.obtainMessage();
			//			message.what = MOVE_FAILED;
			//			message.obj = e;
			//			mHandler.sendMessage(message);
			return;
		}
		// 通知桌面重命名
		ArrayList<String> nameList = new ArrayList<String>();
		// 第一个为新的名字
		nameList.add(name);
		// 第二个为以前的名字
		nameList.add(mFolderName);
		GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
				IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_FOLDER_RENAME, 0,
				mFunFolderItemInfo.getFolderId(), nameList);
		GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
				IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.SCREEN_FOLDER_RENAME, 0,
				mFunFolderItemInfo.getFolderId(), nameList);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case IRequestCodeIds.REQUEST_RENAME : {
				if (resultCode == Activity.RESULT_OK) {
					String name = data.getStringExtra(RenameActivity.NAME);
					int handlerid = data.getIntExtra(RenameActivity.HANDLERID, -1);
					long itemid = data.getLongExtra(RenameActivity.ITEMID, -1);
					if (name != null) {
						if (!name.trim().equals("")) {
							mNameText.setText(name);
						}
					}
					ArrayList<String> list = new ArrayList<String>();
					list.add(name);
					GoLauncher.sendMessage(this, handlerid, IDiyMsgIds.RENAME, -1, itemid, list);
					list.clear();
					list = null;
				}
			}
				break;
			default :
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onMultiItemClicked(int position, boolean isSelected) {

		Message msg = mHandler.obtainMessage();
		msg.what = MARK_ITEM_CHANGED;
		if (isSelected) {
			msg.arg1 = 1;
		} else {
			msg.arg1 = -1;
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
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.finish_btn : {
				String name = (String) mNameText.getText();
				handleElmentsForCreate(name);
			}
				break;
			case R.id.cancle_btn : {
				setResult(Activity.RESULT_OK);
				finish();
			}
				break;
			case R.id.rename : {
				Intent intent = new Intent(GLAppFolderModifyActivity.this, RenameActivity.class);
				CharSequence name = mNameText.getText();
				intent.putExtra(RenameActivity.NAME, name.toString());
				intent.putExtra(RenameActivity.HANDLERID, IDiyFrameIds.APPFUNC_FRAME);
				intent.putExtra(RenameActivity.SHOW_RECOMMENDEDNAME, true);
				intent.putExtra(RenameActivity.FINISH_WHEN_CHANGE_ORIENTATION, true);
				if (mFolderAction == MODITY_FOLDER) {
					intent.putExtra(RenameActivity.ITEMID, mFolderID);
				}
				startActivityForResult(intent, IRequestCodeIds.REQUEST_RENAME);
			}
				break;
			default :
				break;
		}

	}
}

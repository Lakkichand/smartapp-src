package com.jiubang.ggheart.apps.desks.appfunc;

import java.util.ArrayList;

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
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
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
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 修改文件夹列表
 * 
 * @author JiaMing Wen
 * 
 */
public class AppFuncModifyFolderActivity extends Activity implements
/* OnClickListener, */OnMultiItemClickedListener {
	/**
	 * 显示程序的组件列表
	 */
	private ArrayList<Object> mList;
	/**
	 * 选钩列表
	 */
	private ArrayList<Boolean> mBooleanList;

	/**
	 * 文件夹ID
	 */
	private long mFolderID;
	private FunControler mFunControler;
	private FunFolderItemInfo mFunFolderItemInfo;
	private String mFolderName;
	private boolean mIsCreateFolder;
	private Bundle mBundle;
	private Handler mHandler;
	// private MyAdapter mAdapter;
	private Object mMutex;
	private int mCheckedNum = 0; // 目前勾选的数目
	// 是否需要删除mFolderInfo的标识
	private boolean mDeleteInfo = true;
	// private ListView mListView;
	private RelativeLayout mContentLayout;
	private MultiCheckViewGroup mMultiCheckViewGroup;

	private DeskButton mFinishButton;
	private DeskButton mCancle_btn;
	private TextView mName;
	private static final int INIT_FINISH = 0;
	// private static final int DEL_FINISH = 1;
	private static final int MOVE_FINISH = 2; 
	private static final int MOVE_FAILED = 3; 
	public static final int MARK_ITEM_CHANGED = 4; // 标记变化
	// public static final int RESET_DATA = 6;//
	public static final int MSG_NO_ITEMS = 7; // 标记变化
	private static final int MSG_FOLDER_EMPTY = 10;
	/**
	 * ok按钮是否点击过的标志，避免重复点击重复响应
	 */
	private boolean mHasSubmited = false;

	private GoProgressBar mGoProgressBar;
	
	/**
	 * 是否临时文件夹，按取消的时候会删除该文件夹
	 */
	private boolean mIsTempFolder;
	
	/**
	 * 传进来编辑的文件夹是空的(以防万一，按取消就清掉)
	 */
	private boolean mIsEmptyFolder;
	private int mNewFolderType = FunFolderItemInfo.TYPE_NORMAL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mHasSubmited = false;
		setTitle(R.string.app_fun_edit_folder_title);
		XViewFrame viewFrame = XViewFrame.getInstance();
		if (viewFrame != null) {
			viewFrame.getAppFuncMainView().mOpenFuncSetting = true;
		}
		mList = new ArrayList<Object>();
		mMutex = new Object();
		mFunControler = AppFuncFrame.getFunControler();
		initHandler();
		mBundle = getIntent().getExtras();
		String newFolderName = null;
		if (mBundle != null) {
			mIsCreateFolder = mBundle.getBoolean(AppFuncConstants.CREATEFOLDER, false);
			mIsTempFolder = mBundle.getBoolean(AppFuncConstants.IS_TEMP_FOLDER, false);
			if (mIsCreateFolder) {
				// ArrayList<Intent> intentList = mBundle
				// .getParcelableArrayList(AppFuncConstants.FOLDER_INTENT);
				newFolderName = mBundle.getString(AppFuncConstants.NEW_FOLDER_NAME);
				mNewFolderType = mBundle.getInt(AppFuncConstants.NEW_FOLDER_TYPE, FunFolderItemInfo.TYPE_NORMAL);
				if (newFolderName == null) {
					newFolderName = getResources().getString(R.string.folder_name);
				}
				initNewFolderList();

			} else {
				mFolderID = mBundle.getLong(AppFuncConstants.FOLDER_ID, 0);
				FunItemInfo funAppItemInfo = mFunControler.getFunAppItemInfo(mFolderID);
				mFunFolderItemInfo = null;
				if (funAppItemInfo != null && funAppItemInfo instanceof FunFolderItemInfo) {
					mFunFolderItemInfo = (FunFolderItemInfo) funAppItemInfo;
				}
				initList();
			}
		}
		setContentView(R.layout.app_func_modify_folder_list);
		mGoProgressBar = (GoProgressBar) findViewById(R.id.appfunc_modify_progress);
		mContentLayout = (RelativeLayout) findViewById(R.id.contentview);
		mMultiCheckViewGroup = (MultiCheckViewGroup) findViewById(R.id.multi_check_viewgroup);
		mMultiCheckViewGroup.setMultiItemClickedListener(this);
		if (mFunFolderItemInfo != null) {
			mFolderName = mFunFolderItemInfo.getTitle();
			mName = (TextView) findViewById(R.id.appfunc_folder_name);
			mName.setText(mFolderName);
		} else {
			mFolderName = newFolderName;
			mName = (TextView) findViewById(R.id.appfunc_folder_name);
			mName.setText(mFolderName);
		}
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
				// Editable text = ((DeskEditText)
				// findViewById(R.id.userfolder_name))
				// .getText();
				String name = (String) mName.getText();
				handleElments(name);
			}
		});

		if (mIsCreateFolder || mIsEmptyFolder) {
			mFinishButton.setEnabled(false);
			mFinishButton.setTextColor(0XFFB9B9B9);
		}

		mCancle_btn = (DeskButton) findViewById(R.id.cancle_btn);
		mCancle_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mHasSubmited) {
					if (mIsTempFolder || mIsEmptyFolder) {
						try {
							mFunControler.removeFolder(mFunFolderItemInfo);
						} catch (DatabaseException e) {
							AppFuncExceptionHandler.handle(e);
						}
					}
				}
				setResult(Activity.RESULT_OK);
				finish();
			}
		});

		ImageView rename = (ImageView) findViewById(R.id.rename);
		rename.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// // TODO Auto-generated method stub
				// final AppFuncModifyFolderActivity activity =
				// AppFuncModifyFolderActivity.this;
				// mEditDialog = new
				// EditDialog(AppFuncModifyFolderActivity.this,
				// getString(R.string.folder_naming));
				// mEditDialog.setText(mName.getText().toString());
				// mEditDialog.setPositiveButton(getString(R.string.ok),
				// new DialogInterface.OnClickListener() {
				//
				// @Override
				// public void onClick(DialogInterface dialog,
				// int which) {
				// String title = null;
				// if (mEditDialog!=null) {
				// title = mEditDialog.getText();
				// }
				// if ((title != null)
				// && (title.trim().compareTo("") == 0)) {
				// title = activity
				// .getResources()
				// .getString(R.string.folder_name);
				// }
				// mName.setText(title);
				// }
				// });
				// mEditDialog.setNegativeButton(activity.getResources()
				// .getString(R.string.cancle),
				// new DialogInterface.OnClickListener() {
				//
				// @Override
				// public void onClick(DialogInterface dialog,
				// int which) {
				// }
				// });
				// mEditDialog.setOnDismissListener(new
				// DialogInterface.OnDismissListener()
				// {
				// @Override
				// public void onDismiss(DialogInterface dialog)
				// {
				// if(null != mEditDialog){
				// mEditDialog.selfDestruct();
				// }
				// mEditDialog = null;
				// }
				// });
				// mEditDialog.showWithInputMethod();
				// mEditDialog.show();
				Intent intent = new Intent(AppFuncModifyFolderActivity.this, RenameActivity.class);
				CharSequence name = mName.getText();
				intent.putExtra(RenameActivity.NAME, name.toString());
				intent.putExtra(RenameActivity.HANDLERID, IDiyFrameIds.APPFUNC_FRAME);
				intent.putExtra(RenameActivity.SHOW_RECOMMENDEDNAME, true);
				intent.putExtra(RenameActivity.FINISH_WHEN_CHANGE_ORIENTATION, true);
				if (mFunFolderItemInfo != null) {
					intent.putExtra(RenameActivity.ITEMID, mFunFolderItemInfo.getFolderId());
				}
				startActivityForResult(intent, IRequestCodeIds.REQUEST_RENAME);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void initNewFolderList() {
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
					ArrayList<FunAppItemInfo> arrayList = (ArrayList<FunAppItemInfo>) mFunControler
							.getFunAppItemsExceptFolder().clone();
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

					Message message = mHandler.obtainMessage();
					message.what = INIT_FINISH;
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
	public void initList() {
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
								.getFunAppItemInfosForShow().clone();
						if (appsInFolder.size() > 0) {
							SortUtils.sort(appsInFolder, "getTitle", null, null, null);
							for (FunAppItemInfo info : appsInFolder) {
								mList.add(info);
								mBooleanList.add(true);
								mCheckedNum++;
							}
						} else {
							mIsEmptyFolder = true;
						}
					}
					ArrayList<FunAppItemInfo> list = (ArrayList<FunAppItemInfo>) mFunControler
							.getFunAppItemsExceptFolder().clone();
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
					message.what = INIT_FINISH;
					mHandler.sendMessage(message);
				}
			}
		}.start();
	}

	@Override
	protected void onDestroy() {
		GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
				IDiyMsgIds.APPDRAWER_EXIT_FOLDER_EDIT_MODE, -1, null, null);
		synchronized (mMutex) {
			dismissProgressDialog();
			super.onDestroy();
			try {
				// 释放资源反注册mFinishButton，icon里包含的TextFont
				if (mFinishButton != null) {
					mFinishButton.selfDestruct();
					mFinishButton = null;
				}
				if (mCancle_btn != null) {
					mCancle_btn.selfDestruct();
					mCancle_btn = null;
				}
				if (mName != null && mName instanceof DeskTextView) {
					((DeskTextView) mName).selfDestruct();
					mName = null;
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
					case INIT_FINISH : {
						// 取消加载框
						dismissProgressDialog();
						if (mMultiCheckViewGroup != null) {
							mMultiCheckViewGroup.setContentList(mList, mBooleanList);
						}
						break;
					}
					// case DEL_FINISH: {
					// @SuppressWarnings("unchecked")
					// ArrayList<AppItemInfo> removeList =
					// (ArrayList<AppItemInfo>)msg.obj;
					// // 通知桌面文件夹同步
					// GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
					// IDiyFrameIds.SCREEN_FRAME,
					// IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS,
					// -1,
					// funFolderItemInfo.getFolderId(),
					// removeList);
					// DockConstant.rxqLog("DEL_FINISH");
					// GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
					// IDiyFrameIds.DOCK_FRAME,
					// IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS,
					// -1,
					// funFolderItemInfo.getFolderId(),
					// removeList);
					// // 主动刷屏
					// AppFuncHandler.getInstance().refreshGrid();
					// // 取消加载框
					// dismissProgressDialog();
					// setResult(Activity.RESULT_OK);
					// finish();
					// break;
					// }
					case MOVE_FINISH : {
						int count = 0;
						if (null != mBooleanList) {
							if (mBooleanList.isEmpty()) {
								count = 0;
							} else {
								for (Boolean bool : mBooleanList) {
									if (bool == Boolean.TRUE) {
										count++;
									}
								}
							}
						}

						if (!mIsCreateFolder) {
							if (count == 0) {
								ArrayList<AppItemInfo> removeList = null;
								try {
									removeList = AppFuncFrame.getFunControler().removeFolder(
											mFunFolderItemInfo);
									GoLauncher.sendMessage(
											GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
											IDiyFrameIds.SCREEN_FRAME,
											IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, -1,
											mFunFolderItemInfo.getFolderId(), removeList);
									GoLauncher.sendHandler(
											GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
											IDiyFrameIds.DOCK_FRAME,
											IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, -1,
											mFunFolderItemInfo.getFolderId(), removeList);

									// 主动刷屏
									AppFuncHandler.getInstance().refreshGrid();
								} catch (DatabaseException e) {
									AppFuncExceptionHandler.handle(e);
								}
							} 
						} else if (mNewFolderType != FunFolderItemInfo.TYPE_NORMAL) {
							// 创建的文件夹是从顶部操作栏点击生产的
							DeliverMsgManager.getInstance().onChange(
									AppFuncConstants.APP_FUNC_FOLDER_QUICK_ADD_BAR,
									AppFuncConstants.SET_SPECIAL_FOLDER_DISMISS,
									mNewFolderType);
						} 
						dismissProgressDialog();
						setResult(Activity.RESULT_OK);
						if (msg.arg1 == MSG_FOLDER_EMPTY) {
							XViewFrame.getInstance().getAppFuncMainView().removeFolder();
						}
						finish();
						break;
					}
					case MOVE_FAILED : {
						Exception e = null;
						if (msg.obj instanceof Exception) {
							e = (Exception) msg.obj;
						}
						AppFuncExceptionHandler.handle(e);
						dismissProgressDialog();
						setResult(Activity.RESULT_OK);
						finish();
						break;
					}
					// case RESET_DATA:
					// {
					// mGridView.initLayoutData(mList.size());
					// setAdapter();
					// mIndicator.setTotal(mGridView.getScreenCount());
					// mIndicator.setCurrent(0);
					// break;
					// }
					case MARK_ITEM_CHANGED : {
						if (msg.arg1 == -1) {
							--mCheckedNum;
						} else {
							++mCheckedNum;
						}
						if ((mIsCreateFolder || mIsEmptyFolder) && mCheckedNum < 1) {
							mFinishButton.setEnabled(false);
							mFinishButton.setTextColor(0XFFB9B9B9);
						} else {
							mFinishButton.setEnabled(true);
							mFinishButton.setTextColor(0XFF343434);
						}
						break;
					}
					case MSG_NO_ITEMS : {
						Toast.makeText(AppFuncModifyFolderActivity.this,
								R.string.creat_folder_no_icon_tip, 650).show();
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
	private void handleElments(final String name) {

		if (!mHasSubmited) {
			mHasSubmited = true;
			// Editable text = ((DeskEditText)
			// findViewById(R.id.folder_name)).getText();
			// String name = text.toString();

			if (mIsCreateFolder) {
				showProgressDialog();
				// 屏蔽掉loadding框的返回事件
				// if (mProgressDialog!=null) {
				// mGoProgressBar.setOnKeyListener(new
				// DialogInterface.OnKeyListener() {
				//
				// @Override
				// public boolean onKey(DialogInterface dialoginterface, int i,
				// KeyEvent keyevent) {
				// return true;
				// }
				// });
				// }

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

							try {
								String folderName = CommonControler.getInstance(AppFuncModifyFolderActivity.this).generateFolderName(appItemInfos);
								if (folderName != null && !folderName.equals("")
										&& mName.getText().equals(getString(R.string.folder_name))) {
									AppFuncHandler.getInstance().createFolderByMenu(folderName,
											list);
								} else {
									AppFuncHandler.getInstance().createFolderByMenu(name, list);
								}
								Message message = mHandler.obtainMessage();
								message.what = MOVE_FINISH;
								mHandler.sendMessage(message);
							} catch (DatabaseException e) {
								Message message = mHandler.obtainMessage();
								message.what = MOVE_FAILED;
								message.obj = e;
								mHandler.sendMessage(message);
							}

						}
						Looper.myLooper().quit();
					}
				}.start();
			}

			else if (mFunFolderItemInfo != null) {
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
							if (mName.getText().equals(getString(R.string.folder_name))) {
								String folderName = CommonControler.getInstance(
										AppFuncModifyFolderActivity.this).generateFolderName(
										appItemInfos);
								notifyFolderNameChanged(folderName);
							}
							try {
								if (mIsCreateFolder) {
									AppFuncHandler.getInstance().modifyFolder(list, mBooleanList,
											mFunFolderItemInfo, true);
								} else {
									AppFuncHandler.getInstance().modifyFolder(list, mBooleanList,
											mFunFolderItemInfo, false);
								}
								Message message = mHandler.obtainMessage();
								message.what = MOVE_FINISH;
								if (!mBooleanList.contains(true)) {
									message.arg1 = MSG_FOLDER_EMPTY;
								}
								mHandler.sendMessage(message);
							} catch (DatabaseException e) {
								Message message = mHandler.obtainMessage();
								message.what = MOVE_FAILED;
								message.obj = e;
								mHandler.sendMessage(message);
							}
						}
						Looper.myLooper().quit();
					}
				}.start();

			}
		}

	}

	private void notifyFolderNameChanged(final String name) {
		if (mFunFolderItemInfo == null) {
			return;
		}
		try {
			mFunFolderItemInfo.setTitle(name);
		} catch (DatabaseException e) {
			Message message = mHandler.obtainMessage();
			message.what = MOVE_FAILED;
			message.obj = e;
			mHandler.sendMessage(message);
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
							mName.setText(name);
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
}

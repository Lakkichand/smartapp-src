package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.go.util.log.LogConstants;
import com.go.util.window.OrientationControl;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AppTab;
import com.jiubang.ggheart.apps.desks.diy.themescan.EditDialog;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.components.MutilCheckGridView;
import com.jiubang.ggheart.components.MutilCheckViewAdapter;
import com.jiubang.ggheart.components.diygesture.gesturemanageview.DiyGestureConstants;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.MenuItemBean;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 桌面添加-添加go快捷方式
 * 
 * @author jiangchao
 * 
 */
public class ScreenEditAddGoLauncher extends Activity implements OnItemClickListener {

	public final static String GESTURE_FOR_SHORTCUT = "gesture_for_shortcut";
	public final static int GESTURESHORTCUT = 0;

	private ArrayList<Boolean> mBooleanListPre;
	// 勾选列表提交值,改用数组存储是因为相比列表占较少空间，且个数已确定
	private boolean[] mBooleanListFin;
	// 文件夹已有元素和桌面上快捷方式的总和
	private int mCount;
	// 新建文件夹的当前屏幕
	private CharSequence mFolderName;
	// 是否为新建文件夹的标识
	private boolean mIsCreateFolder;
	private int mCreateType;
	private Bundle mBundle;
	private Handler mHandler;
	// private MyAdapter mAdapter;
	private Object mMutex;
	private int mCheckedNum = 0; // 目前勾选的数目
	// private ViewHolder viewHolder = new ViewHolder();
	private static final int INITFINISH = 1;
	private static final int COMMITFINISH = 2;
	public static final int MARKITEMCHANGED = 3; // 标记变化
	public static final int NOROOM = -1; // 标记变化
	public static final int RESETDATA = 6; //
	// private static final int DELFINISH = 3;
	public static final int REMOVE_ITEMS = 1001;
	public static final String CREATE_TYPE = "create_type";

	private DeskButton mFinishButton, mCancle_btn;

	private MutilCheckGridView mGridView;
	// 效果设置
	private TextView mName;
	int mScreenCount[] = new int[1];
	private LayoutInflater mInflater;
	public static final String INTENT_LIST_STRING = "intentlist";
	private boolean mNoRoom = false;
	private boolean mRenameDialogShowing = false; // 控制快速两次连击，弹出两个rename框
	private boolean mGestureForShortcut = false;
	private GoProgressBar mGoProgressBar;

	// 将要添加的go快捷方式
	private ArrayList<ShortCutInfo> mAddItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.screen_modify_grid);
		mCount = 0;
		mMutex = new Object();
		mInflater = LayoutInflater.from(this);
		initHandler();
		mBundle = getIntent().getExtras();
		if (mBundle != null) {
			mGestureForShortcut = mBundle.getBoolean(GESTURE_FOR_SHORTCUT);
		}

		ScreenFrame screenFrame = (ScreenFrame) GoLauncher.getFrame(IDiyFrameIds.SCREEN_FRAME);
		if (screenFrame == null) {
			return;
		}
		mCreateType = AppTab.APP_ADD_TAB_GO_SHORTCUT;

		mFolderName = getString(R.string.dialog_name_go_shortcut);
		if (mGestureForShortcut) {
			mFolderName = getString(R.string.gesture_for_shortcut);
		}
		mAddItems = new ArrayList<ShortCutInfo>();
		mGoProgressBar = (GoProgressBar) findViewById(R.id.modify_progress);
		// 初始化数据
		initList();

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
				String name = (String) mName.getText();
				handleElments(name);
			}
		});

		// 手势隐藏完成按钮
		// if (mGestureForShortcut) {
		// mFinishButton.setVisibility(View.GONE);
		// }

		if (mIsCreateFolder || (mCreateType == AppTab.APP_ADD_TAB_GO_SHORTCUT && mCheckedNum < 1)) {
			mFinishButton.setEnabled(false);
			mFinishButton.setTextColor(0XFFB9B9B9);
		}

		mGridView = (MutilCheckGridView) findViewById(R.id.gridview);
		mGridView.setHanler(mHandler);

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
			private EditDialog mEditDialog;

			@Override
			public void onClick(View v) {
				if (mRenameDialogShowing) {
					return;
				}

				mRenameDialogShowing = true;
				// TODO Auto-generated method stub
				final ScreenEditAddGoLauncher activity = ScreenEditAddGoLauncher.this;
				mEditDialog = new EditDialog(ScreenEditAddGoLauncher.this,
						getString(R.string.folder_naming));
				mEditDialog.setText(mName.getText().toString());
				mEditDialog.setPositiveButton(getString(R.string.ok),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								String title = mEditDialog.getText();
								if ((title != null) && (title.trim().compareTo("") == 0)) {
									title = activity.getResources().getString(R.string.folder_name);
								}
							}
						});
				mEditDialog.setPositiveButton(getString(R.string.ok),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								String title = mEditDialog.getText();
								if ((title != null) && (title.trim().compareTo("") == 0)) {
									title = activity.getResources().getString(R.string.folder_name);
								}
								mName.setText(title);
							}
						});
				mEditDialog.setNegativeButton(activity.getResources().getString(R.string.cancle),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});
				mEditDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						mEditDialog.selfDestruct();
						mEditDialog = null;
						mRenameDialogShowing = false;
					}
				});
				mEditDialog.showWithInputMethod();
				mEditDialog.show();
			}
		});
		// 设置不可见
		rename.setVisibility(View.GONE);
		// 计算当前屏的空间数
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_GET_VANCANT_COUNT, -1, mScreenCount, null);

	}

	// 处理列表元素
	private void handleElments(final String name) {

		if (mGestureForShortcut) {
			handleShortcutForGesture();
			return;
		}

		if (mCheckedNum > 0) {
			int size = mList.size();
			// 要添加的元素
			ArrayList<Intent> mIntents = new ArrayList<Intent>();
			ShortCutInfo appItemInfo = null;
			for (int index = 0; index < size; index++) {
				if (mBooleanListFin[index]) {
					appItemInfo = mList.get(index);
					mIntents.add(appItemInfo.mIntent);
					mAddItems.add(appItemInfo);
				}
			}
		}
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_ADD_APPLICATIONS,
				-1, null, mAddItems);
		setResult(RESULT_OK);
		finish();

	}

	/**
	 * 处理添加应用程序
	 */
	private void handleElmentsForApp() {
		if (mCheckedNum > 0) {
			int size = mList.size();
			// 要添加的元素
			ArrayList<Intent> mIntents = new ArrayList<Intent>();
			ShortCutInfo appItemInfo = null;
			for (int index = 0; index < size; index++) {
				if (mBooleanListFin[index]) {
					appItemInfo = mList.get(index);
					mIntents.add(appItemInfo.mIntent);
					mAddItems.add(appItemInfo);
				}
			}

		}
	}

	private void handleShortcutForGesture() {
		if (mCheckedNum > 0) {
			int size = mList.size();
			// 要添加的元素
			ShortCutInfo shortCutInfo = null;
			for (int index = 0; index < size; index++) {
				if (mBooleanListFin[index]) {
					shortCutInfo = mList.get(index);
					break;
				}
			}
			if (shortCutInfo != null) {
				Intent intent = new Intent();
				intent.putExtra(DiyGestureConstants.APP_INTENT, shortCutInfo.mIntent);
				intent.putExtra(DiyGestureConstants.APP_NAME, shortCutInfo.mTitle);
				setResult(RESULT_OK, intent);
			}
			finish();
		}
	}

	private void showProgressDialog(int index) {
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
					/*
					 * // 初始化3个列表 // if (mFolderItems == null) { // mFolderItems
					 * = new ArrayList<ItemInfo>(); // } if (mAllList == null) {
					 * mAllList = new ArrayList<Object>(); } if (mBooleanListPre
					 * == null) { mBooleanListPre = new ArrayList<Boolean>(); }
					 * // 先清空 mAllList.clear(); mBooleanListPre.clear(); if
					 * (mCreateType != AppTab.APP_ADD_TAB_ADD) { // 获取文件夹的资源列表
					 * ArrayList<ItemInfo> items = mScreenControler
					 * .getFolderItems(mFolderID); // 添加已存在的元素 if (items !=
					 * null) { // ArrayList<ItemInfo> appsInFolder =
					 * mFolderItems; if (items.size() > 0) { try { //
					 * SortUtils.sortItemInfo(items, //
					 * ScreenModifyFolderActivity.this); SortUtils.sort(items,
					 * "getTitle", null, null, "ASC"); } catch
					 * (IllegalArgumentException e) { e.printStackTrace(); } for
					 * (ItemInfo info : items) { // 列表元素个数递增 mCount++;
					 * mAllList.add(info); mBooleanListPre.add(true); } } }
					 * 
					 * } final AppDataEngine engine = GOLauncherApp
					 * .getAppDataEngine();
					 * 
					 * ArrayList<AppItemInfo> list2 = engine
					 * .getCompletedAppItemInfosExceptHide();
					 */

					if (mGestureForShortcut) {
						handleShortcutForGesture();
					}

					// 初始化（jiang）
					if (mBooleanListPre == null) {
						mBooleanListPre = new ArrayList<Boolean>();
					}
					// 先清空
					mBooleanListPre.clear();
					initShortcutItem();
					for (ShortCutInfo info : mList) {
						// 列表元素个数递增
						mBooleanListPre.add(false);
					}
					mBooleanListFin = new boolean[mList.size()];
					/*
					 * if (list2.size() > 0) { try { SortUtils.sort(list2,
					 * "getTitle", null, null, null); } catch
					 * (IllegalArgumentException e) { // 可能因为用户手机Java运行时环境的问题出错
					 * e.printStackTrace(); } for (AppItemInfo info : list2) {
					 * 
					 * if (info.mIntent != null && info.mIntent.getComponent()
					 * != null) { if (!isExist(info.mIntent.getComponent())) {
					 * mAllList.add(info); mBooleanListPre.add(false); } }// end
					 * if info
					 * 
					 * }// end for } // 克隆一份勾选列表 int booleanSize =
					 * mBooleanListPre.size(); mBooleanListFin = new
					 * boolean[booleanSize]; //
					 * mGridView.setBoolTable(mBooleanListFin); boolean value =
					 * false; for (int i = 0; i < booleanSize; i++) { value =
					 * mBooleanListPre.get(i); mBooleanListFin[i] = value; if
					 * (value) { mCheckedNum++; } }
					 */
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
		showProgressDialog(INITFINISH);
		start();
		mInitThread = null;
	}

	// 判断是否已添加进列表
	private boolean isExist(ComponentName cName) {
		for (int i = 0; i < mCount; i++) {
			final Intent intent = (mList.get(i)).mIntent;
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

			if (mList != null) {
				mList.clear();
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
				if (mGridView != null) {
					mGridView.recyle();
				}
				mGridView = null;
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
						ScreenUtils.showToast(R.string.tab_add_no_more_room,
								ScreenEditAddGoLauncher.this);
					}
						break;
					case INITFINISH : {
						// 取消加载框
						/*
						 * dismissProgressDialog();
						 * mGridView.initLayoutData(mAllList.size());
						 * setAdapter(); mIndicator.setCurrent(0);
						 * mIndicator.setTotal(mGridView.getScreenCount());
						 */

						dismissProgressDialog();
						mGridView.initLayoutData(mList.size());
						setAdapter();

						break;
					}
					case COMMITFINISH : {
						// 取消加载框
						dismissProgressDialog();

						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_ADD_APPLICATIONS, -1, null, mAddItems);
						setResult(RESULT_OK);
						finish();
						break;
					}
					case MutilCheckGridView.UPDATEINDICATOR : {
					}
						break;
					case MARKITEMCHANGED : {
						if (msg.arg1 == -1) {
							--mCheckedNum;
						} else {
							++mCheckedNum;
						}
						if ((mIsCreateFolder && mCheckedNum <= 1)
								|| (mCreateType == AppTab.APP_ADD_TAB_GO_SHORTCUT && mCheckedNum < 1)) {
							mFinishButton.setEnabled(false);
							mFinishButton.setTextColor(0XFFB9B9B9);
						} else {
							mFinishButton.setEnabled(true);
							mFinishButton.setTextColor(0XFF343434);
						}
						if (mCreateType == AppTab.APP_ADD_TAB_GO_SHORTCUT) {
							mNoRoom = mCheckedNum >= mScreenCount[0];
						} else {
							if (mIsCreateFolder) {
								mNoRoom = mScreenCount[0] < 1;
							}
						}
						break;
					}
					case RESETDATA : {
						mGridView.initLayoutData(mList.size());
						setAdapter();
					}
						break;

					// 手势隐藏确认按钮
					// case GESTURESHORTCUT:
					// {
					// String name = (String) mName.getText();
					// handleElments(name);
					// break;
					// }

					// 手势选择应用
					case GESTURESHORTCUT : {
						if (mGestureForShortcut && mCheckedNum <= 0) {
							mFinishButton.setEnabled(false);
							mFinishButton.setTextColor(0XFFB9B9B9);
						} else {
							mFinishButton.setEnabled(true);
							mFinishButton.setTextColor(0XFF343434);
						}
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

	public List<ShortCutInfo> getAllList() {
		return mList;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		finish();
	}
	/**
	 * 
	 * @author jiangchao
	 * 
	 */
	private class MyAdapter extends MutilCheckViewAdapter {

		public MyAdapter(ArrayList<Object> list, int screenIndex) {
			super(list, screenIndex);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			Object info = null;
			try {
				info = getItem(position);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}

			if (info == null) {
				return null;
			}
			if (convertView == null) {
				try {
					convertView = mInflater.inflate(R.layout.folder_grid_item, parent, false);
				} catch (InflateException e) {
					e.printStackTrace();
				}
			}

			if (convertView == null) {
				return null;
			}
			TextView textView = (TextView) convertView.findViewById(R.id.name);
			ImageView img = (ImageView) convertView.findViewById(R.id.choice);
			if (getCheckStatus(mScreen, position)) {
				img.setVisibility(View.VISIBLE);
			} else {
				img.setVisibility(View.INVISIBLE);
			}
			if (info instanceof ShortCutInfo) {
				textView.setCompoundDrawablesWithIntrinsicBounds(null, ((ShortCutInfo) info).mIcon,
						null, null);
				textView.setText(((ShortCutInfo) info).mTitle);
				if (GOLauncherApp.getSettingControler().getDesktopSettingInfo().isShowTitle()) {
					textView.setText(((ShortCutInfo) info).mTitle);
				}
			} else if (info instanceof AppItemInfo) {
				textView.setCompoundDrawablesWithIntrinsicBounds(null, ((AppItemInfo) info).mIcon,
						null, null);;
				if (GOLauncherApp.getSettingControler().getDesktopSettingInfo().isShowTitle()) {
					textView.setText(((AppItemInfo) info).mTitle);
				}
			}
			textView.setTextSize(GoLauncher.getAppFontSize());

			return convertView;
		}
	}

	private boolean getCheckStatus(int screen, int position) {
		if (mBooleanListFin == null) {
			return false;
		} else {
			return mBooleanListFin[screen * mGridView.getCountPerPage() + position];
		}
	}

	private void setAdapter() {
		if (mList == null) {
			return;
		}
		final int count = mList.size();
		mGridView.removeAllViews();
		int screenCount = mGridView.getScreenCount();
		int itemsCountPerScreen = mGridView.getCountPerPage();
		int culumns = mGridView.getCellCol();
		for (int i = 0; i < screenCount; i++) {
			GridView page = new GridView(this);
			ArrayList<Object> tempList = new ArrayList<Object>();
			for (int j = 0; j < itemsCountPerScreen && itemsCountPerScreen * i + j < count; j++) {
				Object obj = mList.get(itemsCountPerScreen * i + j);
				tempList.add(obj);
			}
			page.setAdapter(new MyAdapter(tempList, i));
			page.setNumColumns(culumns);
			page.setHorizontalSpacing(0);
			page.setVerticalSpacing(0);
			page.requestLayout();
			page.setSelector(android.R.color.transparent);
			page.setOnItemClickListener(this);
			mGridView.addView(page);
		}// end for
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		MyAdapter adapter = (MyAdapter) parent.getAdapter();
		int itemsCountPerScreen = mGridView.getCountPerPage();
		int screenIndex = adapter.mScreen;
		int p = position + screenIndex * itemsCountPerScreen;
		if (p > mBooleanListFin.length) {
			return;
		}

		Message msg = new Message();
		msg.what = ScreenEditAddGoLauncher.MARKITEMCHANGED;
		if (!mBooleanListFin[p] && mNoRoom) {
			msg.what = NOROOM;
			mHandler.sendMessage(msg);
			return;
		}
		if (mBooleanListFin[p]) {
			mBooleanListFin[p] = false;
			msg.arg1 = -1;
		} else {
			mBooleanListFin[p] = true;
			msg.arg1 = 1;
		}

		// 手势隐藏确认按钮
		// if (mGestureForShortcut) {
		// ++mCheckedNum;
		// msg.what = GESTURESHORTCUT;
		// mHandler.sendMessage(msg);
		// return;
		// }

		// 手势选择应用
		if (mGestureForShortcut) {
			// 把所有的选择置空
			int size = mBooleanListFin.length;
			for (int i = 0; i < size; i++) {
				if (i != p) {
					mBooleanListFin[i] = false;
				}
			}

			if (mBooleanListFin[p]) {
				mCheckedNum = 1;
			} else {
				mCheckedNum = 0;
			}
			msg.what = GESTURESHORTCUT;
			// 循环把每个适配器的数据刷新
			int gridViewSize = mGridView.getChildCount();
			for (int i = 0; i < gridViewSize; i++) {
				MyAdapter adapterTemp = (MyAdapter) ((GridView) mGridView.getChildAt(i))
						.getAdapter();
				if (adapterTemp != null) {
					adapterTemp.notifyDataSetChanged();
				}
			}
		} else {
			// 只刷新当前适配器的数据
			adapter.notifyDataSetChanged();
		}
		mHandler.sendMessage(msg);
	}

	// add 初始化GO桌面快捷方式
	List<ShortCutInfo> mList;
	private String[] mIntentActions;
	private int[] mDrawableIds;
	private String[] mTitles;

	private static final int MAIN_SCREEN = 1;
	private static final int MAIN_SCREEN_OR_PREVIEW = 2;
	private static final int FUNCMENU = 3;
	private static final int NOTIFICATION = 4;
	private static final int STATUS_BAR = 5;
	private static final int THEME_SETTING = 6;
	private static final int PREFERENCES = 7;
	private static final int GO_STORE = 8;
	private static final int PREVIEW = 9;
	private static final int GO_LOCK = 10;
	private static final int LOCK_SCREEN = 11;
	private static final int DOCK_BAR = 12;
	private static final int MAIN_MENU = 13;
	private static final int DIY_GESTURE = 14;
	private static final int PHOTO = 15;
	private static final int MUSIC = 16;
	private static final int VIDEO = 17;

	private void initShortcutItem() {
		if (null == mList) {
			mList = new ArrayList<ShortCutInfo>();
		}
		initChoiceItem();
		final int count = mIntentActions.length;
		final String goComponentName = "com.gau.launcher.action";
		ShortCutInfo itemInfo = null;
		Intent intent = null;
		ComponentName cmpName = null;
		for (int i = 0; i < count; i++) {
			itemInfo = new ShortCutInfo();
			intent = new Intent(mIntentActions[i]);
			cmpName = new ComponentName(goComponentName, mIntentActions[i]);
			intent.setComponent(cmpName);
			itemInfo.mIntent = intent;
			itemInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
			itemInfo.mTitle = mTitles[i];
			itemInfo.mIcon = getItemImage(null, mDrawableIds[i], this, null);

			mList.add(itemInfo);

			itemInfo = null;
			intent = null;
			cmpName = null;
		}
		mIntentActions = null;
		mTitles = null;
		mDrawableIds = null;

	}

	private Drawable getItemImage(DeskThemeBean.MenuBean menuBean, int id, Context context,
			ImageExplorer imageExplorer) {
		Drawable ret = null;
		// 从主题获取(以后可能要用到)
		if (null != menuBean && null != menuBean.mItems) {

			int len = menuBean.mItems.size();
			for (int i = 0; i < len; i++) {
				MenuItemBean itemBean = menuBean.mItems.get(i);
				if (null == itemBean) {
					continue;
				}
				if (itemBean.mId == id) {
					if (null != itemBean.mImage) {
						ret = getDrawable(imageExplorer, itemBean.mImage.mResName);
					}
					break;
				}
			}
		}
		// 从主程序获取
		if (null == ret) {
			switch (id) {
				case MAIN_SCREEN :
					//					ret = getDrawable(context, R.drawable.go_shortcut_mainscreen);
					ret = getIcons(R.drawable.go_shortcut_mainscreen);
					break;

				case MAIN_SCREEN_OR_PREVIEW :
					//					ret = getDrawable(context, R.drawable.go_shortcut_main_or_preview);
					ret = getIcons(R.drawable.go_shortcut_main_or_preview);
					break;

				case FUNCMENU :
					ret = getIcons(R.drawable.go_shortcut_appdrawer);
					break;

				case NOTIFICATION :
					ret = getIcons(R.drawable.go_shortcut_notification);
					break;

				case STATUS_BAR :
					ret = getIcons(R.drawable.go_shortcut_statusbar);
					break;

				case THEME_SETTING :
					ret = getIcons(R.drawable.go_shortcut_themes);
					break;

				case PREFERENCES :
					ret = getIcons(R.drawable.go_shortcut_preferences);
					break;

				case GO_STORE :
					ret = getIcons(R.drawable.go_shortcut_store);
					break;

				case PREVIEW :
					ret = getIcons(R.drawable.go_shortcut_preview);
					break;

				// case GO_LOCK:
				// ret = getDrawable(context, R.drawable.go_shortcut_locker);
				// break;
				case LOCK_SCREEN :
					ret = getIcons(R.drawable.go_shortcut_lockscreen);
					break;
				case DOCK_BAR :
					ret = getIcons(R.drawable.go_shortcut_hide_dock);
					break;
				case MAIN_MENU : {
					ret = getIcons(R.drawable.go_shortcut_menu);
					break;
				}
				case DIY_GESTURE : {
					ret = getIcons(R.drawable.go_shortcut_diygesture);
					break;
				}
				case PHOTO : {
					ret = getIcons(R.drawable.go_shortcut_photo);
					break;
				}
				case MUSIC : {
					ret = getIcons(R.drawable.go_shortcut_music);
					break;
				}
				case VIDEO : {
					ret = getIcons(R.drawable.go_shortcut_video);
					break;
				}
				default :
					break;
			}
		}
		return ret;
	}

	// 初始化列表的选项
	private void initChoiceItem() {
		mIntentActions = new String[] { ICustomAction.ACTION_SHOW_MAIN_SCREEN,
				ICustomAction.ACTION_SHOW_MAIN_OR_PREVIEW,
				ICustomAction.ACTION_SHOW_FUNCMENU_FOR_LAUNCHER_ACITON,
				ICustomAction.ACTION_SHOW_EXPEND_BAR,
				ICustomAction.ACTION_SHOW_HIDE_STATUSBAR,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME,
				ICustomAction.ACTION_SHOW_PREFERENCES,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE,
				ICustomAction.ACTION_SHOW_PREVIEW,
				// ICustomAction.ACTION_SHOW_LOCKER_SETTING
				ICustomAction.ACTION_ENABLE_SCREEN_GUARD, ICustomAction.ACTION_SHOW_DOCK,
				ICustomAction.ACTION_SHOW_MENU, ICustomAction.ACTION_SHOW_DIYGESTURE,
				ICustomAction.ACTION_SHOW_PHOTO, ICustomAction.ACTION_SHOW_MUSIC,
				ICustomAction.ACTION_SHOW_VIDEO };

		mTitles = new String[] {
				getString(R.string.customname_mainscreen),
				getString(R.string.customname_mainscreen_or_preview),
				getString(R.string.customname_Appdrawer),
				getString(R.string.customname_notification),
				getString(R.string.customname_status_bar),
				getString(R.string.customname_themeSetting),
				getString(R.string.customname_preferences),
				getString(R.string.customname_gostore),
				getString(R.string.customname_preview),
				// getString(R.string.customname_golocker)
				getString(R.string.goshortcut_lockscreen),
				getString(R.string.goshortcut_showdockbar),
				getString(R.string.customname_mainmenu), getString(R.string.customname_diygesture),
				getString(R.string.customname_photo), getString(R.string.customname_music),
				getString(R.string.customname_video)};

		mDrawableIds = new int[] { MAIN_SCREEN, MAIN_SCREEN_OR_PREVIEW, FUNCMENU, NOTIFICATION,
				STATUS_BAR, THEME_SETTING, PREFERENCES, GO_STORE, PREVIEW// ,
																			// GO_LOCK
				, LOCK_SCREEN, DOCK_BAR, MAIN_MENU, DIY_GESTURE, PHOTO, MUSIC, VIDEO };
	}

	/*private Drawable getDrawable(Context context, int resId) {
		Drawable ret = null;
		if (null == context) {
			return ret;
		}
		try {
			if (Machine.isTablet(context)) {
				ret = ImageExplorer.getInstance(context).getDrawableForDensity(getResources(),
						resId);
			}
			if (null == ret) {
				ret = context.getResources().getDrawable(resId);
			}
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}*/

	private Drawable getDrawable(ImageExplorer imageExplorer, String resName) {
		Drawable ret = null;
		if (null == imageExplorer || null == resName) {
			return ret;
		}
		try {
			ret = imageExplorer.getDrawable(resName);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * <br>
	 * 功能简述:通过drawableId拿推荐图标图片 <br>
	 * 功能详细描述:可以过滤某些图标进行download tag标签合成图片（tag图片共享一张，减少图片资源） <br>
	 * @param drawableId
	 * @return　经过合成规则处理后的图片
	 */
	private Drawable getIcons(int drawableId) {
		Drawable tag = this.getResources().getDrawable(drawableId);
		Drawable drawable = this.getResources().getDrawable(R.drawable.screenedit_icon_bg);
		try {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas cv = new Canvas(bmp);
			ImageUtil.drawImage(cv, drawable, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			ImageUtil.drawImage(cv, tag, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			BitmapDrawable bmd = new BitmapDrawable(bmp);
			bmd.setTargetDensity(this.getResources().getDisplayMetrics());
			drawable = bmd;
		} catch (Throwable e) {
			// 出错则不进行download Tag合成图
		}

		return drawable;
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

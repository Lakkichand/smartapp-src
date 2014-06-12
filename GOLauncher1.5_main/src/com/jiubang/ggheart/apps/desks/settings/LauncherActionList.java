/**
 * 
 */
package com.jiubang.ggheart.apps.desks.settings;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.SortUtils;
import com.go.util.device.Machine;
import com.go.util.window.OrientationControl;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.components.DeskActivity;
import com.jiubang.ggheart.components.DeskProgressDialog;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.MenuItemBean;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * GoLauncher 快捷方式多选列表
 * 
 * @author jaingxuwen
 * 
 */
//CHECKSTYLE:OFF
public class LauncherActionList extends DeskActivity
		implements
			OnItemClickListener,
			OnClickListener {
	private List<ShortCutInfo> mList;

	public static final String INTENT_LIST_STRING = "intentlist";

	private MyAdapter mAdapter = null;
	private ProgressDialog mProgressDialog = null;

	private Object mMutex;

	private ArrayList<ShortCutInfo> mAddItems;

	private ArrayList<Boolean> mIsCheckList;

	/**
	 * 选了的程序数目
	 */
	private int mSelectedCount = 0;

	/**
	 * 当前屏剩余空位
	 */
	public static final String CURRENT_SCREEN_LEFT_COUNT_STRING = "curscreenleftcount";
	private int mCurrentScreenCount;

	/**
	 * UI项
	 */
	private DeskTextView mDeskTextView;
	private ListView mListView;
	private Button mButtonYes;
	private Button mButtonNo;

	private String[] mIntentActions;
	private int[] mDrawableIds;
	private String[] mTitles;
	// private Drawable mMenuPic = null;

	private final int MAIN_SCREEN = 1;
	private final int MAIN_SCREEN_OR_PREVIEW = 2;
	private final int FUNCMENU = 3;
	private final int NOTIFICATION = 4;
	private final int STATUS_BAR = 5;
	private final int THEME_SETTING = 6;
	private final int PREFERENCES = 7;
	private final int GO_STORE = 8;
	private final int PREVIEW = 9;
	private final int GO_LOCK = 10;
	private final int LOCK_SCREEN = 11;
	private final int DOCK_BAR = 12;
	private final int MAIN_MENU = 13;
	private final int PHOTO = 14;
	private final int MUSIC = 15;
	private final int VIDEO = 16;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 启动GO桌面
		// startGOLauncher("com.gau.go.launcherex");
		setTitle(R.string.launcher_action_list);
		int count[] = new int[1];
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_GET_VANCANT_COUNT, -1, count, null);
		// mCurrentScreenCount =
		// getIntent().getIntExtra(CURRENT_SCREEN_LEFT_COUNT_STRING, 0);
		mCurrentScreenCount = count[0];
		mAdapter = new MyAdapter(this);
		mMutex = new Object();
		mAddItems = new ArrayList<ShortCutInfo>();
		mIsCheckList = new ArrayList<Boolean>();

		setContentView(R.layout.launcher_action);
		initUiItems();
		initList();
	}

	private void updateCount() {
		mDeskTextView.setText(getString(R.string.homescreen_available_for_app) + mSelectedCount
				+ "/" + mCurrentScreenCount);
	}

	private void initUiItems() {
		mDeskTextView = (DeskTextView) findViewById(R.id.la_leftcount);
		mListView = (ListView) findViewById(R.id.la_list);
		mListView.setOnItemClickListener(this);

		mButtonYes = (Button) findViewById(R.id.la_yes);
		mButtonNo = (Button) findViewById(R.id.la_no);
		mButtonYes.setOnClickListener(this);
		mButtonNo.setOnClickListener(this);

		updateCount();
	}

	// 初始化列表的选项
	private void initChoiceItem() {
		mIntentActions = new String[] { ICustomAction.ACTION_SHOW_MAIN_SCREEN,
				ICustomAction.ACTION_SHOW_MAIN_OR_PREVIEW,
				ICustomAction.ACTION_SHOW_FUNCMENU_FOR_LAUNCHER_ACITON,
				ICustomAction.ACTION_SHOW_EXPEND_BAR, ICustomAction.ACTION_SHOW_HIDE_STATUSBAR,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME,
				ICustomAction.ACTION_SHOW_PREFERENCES,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE, ICustomAction.ACTION_SHOW_PREVIEW,
				// ICustomAction.ACTION_SHOW_LOCKER_SETTING
				ICustomAction.ACTION_ENABLE_SCREEN_GUARD, ICustomAction.ACTION_SHOW_DOCK,
				ICustomAction.ACTION_SHOW_MENU, ICustomAction.ACTION_SHOW_PHOTO, 
				ICustomAction.ACTION_SHOW_MUSIC,ICustomAction.ACTION_SHOW_VIDEO};

		mTitles = new String[] { getString(R.string.customname_mainscreen),
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
				getString(R.string.customname_mainmenu), 
				getString(R.string.customname_photo,
				getString(R.string.customname_music),
				getString(R.string.customname_video))};

		mDrawableIds = new int[] { MAIN_SCREEN, MAIN_SCREEN_OR_PREVIEW, FUNCMENU, NOTIFICATION,
				STATUS_BAR, THEME_SETTING, PREFERENCES, GO_STORE, PREVIEW// ,
																			// GO_LOCK
				, LOCK_SCREEN, DOCK_BAR, MAIN_MENU ,PHOTO,MUSIC,VIDEO};
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
					ret = getDrawable(context, R.drawable.go_shortcut_mainscreen);
					break;

				case MAIN_SCREEN_OR_PREVIEW :
					ret = getDrawable(context, R.drawable.go_shortcut_main_or_preview);
					break;

				case FUNCMENU :
					ret = getDrawable(context, R.drawable.go_shortcut_appdrawer);
					break;

				case NOTIFICATION :
					ret = getDrawable(context, R.drawable.go_shortcut_notification);
					break;

				case STATUS_BAR :
					ret = getDrawable(context, R.drawable.go_shortcut_statusbar);
					break;

				case THEME_SETTING :
					ret = getDrawable(context, R.drawable.go_shortcut_themes);
					break;

				case PREFERENCES :
					ret = getDrawable(context, R.drawable.go_shortcut_preferences);
					break;

				case GO_STORE :
					ret = getDrawable(context, R.drawable.go_shortcut_store);
					break;

				case PREVIEW :
					ret = getDrawable(context, R.drawable.go_shortcut_preview);
					break;

				// case GO_LOCK:
				// ret = getDrawable(context, R.drawable.go_shortcut_locker);
				// break;
				case LOCK_SCREEN :
					ret = getDrawable(context, R.drawable.go_shortcut_lockscreen);
					break;
				case DOCK_BAR :
					ret = getDrawable(context, R.drawable.go_shortcut_hide_dock);
					break;
				case MAIN_MENU : {
					ret = getDrawable(context, R.drawable.go_shortcut_menu);
					break;
				}
				case PHOTO : {
					ret = getDrawable(context, R.drawable.go_shortcut_photo);
					break;
				}
				case MUSIC : {
					ret = getDrawable(context, R.drawable.go_shortcut_music);
					break;
				}
				case VIDEO : {
					ret = getDrawable(context, R.drawable.go_shortcut_video);
					break;
				}
				default :
					break;
			}
		}
		return ret;
	}

	private Drawable getDrawable(Context context, int resId) {
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
	}

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

	// 初始化GO桌面快捷方式
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

	private void initList() {
		// 显示提示框
		showProgressDialog();
		// 异步初始化
		// final AppDataEngine engine =
		// AppCore.getInstance().getAppDataEngine();
		new Thread(ThreadName.SCREEN_INIT_APPLIST) {
			@Override
			public void run() {
				// 初始化数据
				synchronized (mMutex) {
					initShortcutItem();
					// mList = engine.getAllCompletedAppItemInfos();

					if (null != mAdapter) {
						mAdapter.notifyDataSetChanged();
					}

					sortByLetter();

					int size = mList.size();

					// NOTO:出现NullPointerException,不确定原因是否是在此线程上锁前就已执行了onDestroy
					if (null != mIsCheckList) {
						for (int i = 0; i < size; i++) {
							mIsCheckList.add(Boolean.FALSE);
						}

						// 对外通知
						Message msg = new Message();
						msg.what = APPLIST_INIT_OK;
						mHandler.sendMessage(msg);
					}
				}
			};
		}.start();
	}

	private final static int APPLIST_INIT_OK = 1000;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case APPLIST_INIT_OK :
					// 取消加载框
					dismissProgressDialog();
					mListView.setAdapter(mAdapter);

					break;

				default :
					break;
			}
		};
	};

	private void showProgressDialog() {
		if (null == mProgressDialog) {
			mProgressDialog = DeskProgressDialog.show(this, null,
					getString(R.string.sort_processing), true);
		}
	}

	private void dismissProgressDialog() {
		if (mProgressDialog != null) {
			try {
				mProgressDialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mProgressDialog = null;
		}
	}

	/**
	 * 按字母排序
	 */
	private void sortByLetter() {
		if (mList != null) {
			try {
				SortUtils.sort(mList, "getTitle", null, null, "ASC");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public final class ViewHolder implements BroadCasterObserver {
		public TextView title;
		public int mPosition;

		@Override
		public void onBCChange(int msgId, int param, Object object, List objects) {
			switch (msgId) {
				case AppItemInfo.INCONCHANGE : {
					try {
						BitmapDrawable drawable = null;
						if (null != mList) {
							drawable = (BitmapDrawable) mList.get(mPosition).mIcon;
							if (drawable != null) {
								title.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null,
										null);
							}
						}
					} catch (IndexOutOfBoundsException e) {
						e.printStackTrace();
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
						OutOfMemoryHandler.handle();
					}
				}
					break;

				default :
					break;
			}
		}
	}

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			synchronized (mMutex) {
				if (mList != null) {
					return mList.size();
				} else {
					return 0;
				}
			}
		}

		@Override
		public Object getItem(int arg0) {

			return null;
		}

		@Override
		public long getItemId(int arg0) {

			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				try {
					holder = new ViewHolder();
					convertView = mInflater.inflate(R.layout.multi_choice_item, null);
					holder.title = (TextView) convertView.findViewById(R.id.label);
					DeskSettingConstants.setTextViewTypeFace(holder.title);
					convertView.setTag(holder);
				} catch (OutOfMemoryError e) {
					OutOfMemoryHandler.handle();
				} catch (Exception e) {
				}
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			try {
				if (holder != null) {
					holder.mPosition = position;
					final ShortCutInfo shortCutInfo = mList.get(position);
					Drawable iconDrawable = shortCutInfo.mIcon;
					holder.title
					.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null);
					holder.title.setText(shortCutInfo.mTitle);
				}

				if (convertView != null) {
					final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
					int rowNum = position;
					if (mIsCheckList.get(rowNum)) {
						checkBox.setChecked(true);
					} else {
						checkBox.setChecked(false);
					}
				}
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return convertView;
		}
	}

	@Override
	protected void onDestroy() {
		synchronized (mMutex) {
			if (null != mList) {
				mList.clear();
				mList = null;
				mAdapter.notifyDataSetChanged();
			}

			if (null != mAddItems) {
				mAddItems.clear();
				mAddItems = null;
			}

			if (null != mIsCheckList) {
				mIsCheckList.clear();
				mIsCheckList = null;
			}

			mAdapter = null;
			dismissProgressDialog();
			cleanHandlerMsg();

			super.onDestroy();
		}
	}

	private void cleanHandlerMsg() {
		if (mHandler != null) {
			mHandler.removeMessages(APPLIST_INIT_OK);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 检查屏幕翻转设置，并应用
		OrientationControl.setOrientation(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mList != null) {
			ShortCutInfo itemInfo = mList.get(position);
			CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
			if (!checkBox.isChecked()) {
				if (mSelectedCount < mCurrentScreenCount) {
					mAddItems.add(itemInfo);
					checkBox.setChecked(true);
					mIsCheckList.remove(position);
					mIsCheckList.add(position, Boolean.TRUE);
					mSelectedCount++;
					updateCount();
				} else {
					DeskToast.makeText(LauncherActionList.this,
							getString(R.string.homescreen_full_warning), Toast.LENGTH_SHORT).show();
				}
			} else {
				mAddItems.remove(itemInfo);
				checkBox.setChecked(false);
				mIsCheckList.remove(position);
				mIsCheckList.add(position, Boolean.FALSE);
				mSelectedCount--;
				updateCount();
			}
		}

	}

	@Override
	public void onClick(View v) {
		if (v == mButtonYes) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_ADD_APPLICATIONS, -1, null, mAddItems);
			setResult(RESULT_OK);
			finish();
		} else if (v == mButtonNo) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}
}

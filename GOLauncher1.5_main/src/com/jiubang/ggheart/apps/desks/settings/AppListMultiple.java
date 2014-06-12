/**
 * 
 */
package com.jiubang.ggheart.apps.desks.settings;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
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
import com.go.util.window.OrientationControl;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.components.DeskActivity;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.DeskProgressDialog;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 应用程序多选列表
 * 
 * @author ruxueqin
 * 
 */
//CHECKSTYLE:OFF
public class AppListMultiple extends DeskActivity implements OnItemClickListener, OnClickListener {
	private List<AppItemInfo> mList;
	public static final String INTENT_LIST_STRING = "intentlist";

	private MyAdapter mAdapter = null;
	private ProgressDialog mProgressDialog = null;
	private Object mMutex;
	private ArrayList<Intent> mIntents;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.applist);

		mCurrentScreenCount = getIntent().getIntExtra(CURRENT_SCREEN_LEFT_COUNT_STRING, 0);
		setContentView(R.layout.applist_multiple);
		initUiItems();

		mAdapter = new MyAdapter(this);
		mMutex = new Object();
		mIntents = new ArrayList<Intent>();
		mIsCheckList = new ArrayList<Boolean>();

		initList();
	}

	private void updateCount() {
		mDeskTextView.setText(getString(R.string.homescreen_available_for_app) + mSelectedCount
				+ "/" + mCurrentScreenCount);
	}

	private void initUiItems() {
		mDeskTextView = (DeskTextView) findViewById(R.id.leftcount);
		mListView = (ListView) findViewById(R.id.list);
		mListView.setOnItemClickListener(this);

		mButtonYes = (Button) findViewById(R.id.yes);
		mButtonYes.setOnClickListener(this);
		mButtonNo = (Button) findViewById(R.id.no);
		mButtonNo.setOnClickListener(this);

		updateCount();
	}

	private void initList() {
		// 显示提示框
		showProgressDialog();
		// 异步初始化
		final AppDataEngine engine = GOLauncherApp.getAppDataEngine();
		new Thread(ThreadName.SCREEN_INIT_APPLIST) {
			@Override
			public void run() {
				// 初始化数据
				synchronized (mMutex) {
					mList = engine.getCompletedAppItemInfosExceptHide();

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
		@SuppressWarnings("rawtypes")
		public void onBCChange(int msgId, int param, Object object, List objects) {
			switch (msgId) {
				case AppItemInfo.INCONCHANGE : {
					try {
						BitmapDrawable drawable = null;
						if (null != mList) {
							drawable = mList.get(mPosition).mIcon;
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
					AppItemInfo appItemInfo = mList.get(position);
					final Drawable iconDrawable = appItemInfo.mIcon;
					holder.title
					.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null);
					holder.title.setText(appItemInfo.mTitle);
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

			if (null != mIntents) {
				mIntents.clear();
				mIntents = null;
			}

			if (null != mIsCheckList) {
				mIsCheckList.clear();
				mIsCheckList = null;
			}
			if (null != mDeskTextView) {
				mDeskTextView.selfDestruct();
				mDeskTextView = null ;
			}
			if (null != mButtonNo) {
				((DeskButton)mButtonNo).selfDestruct();
				mButtonNo = null;
			}
			if (null != mButtonYes) {
				((DeskButton)mButtonYes).selfDestruct();
				mButtonYes = null;
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
			AppItemInfo itemInfo = mList.get(position);
			final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
			if (checkBox != null && !checkBox.isChecked()) {
				if (mSelectedCount < mCurrentScreenCount) {
					mIntents.add(itemInfo.mIntent);
					checkBox.setChecked(true);
					mIsCheckList.remove(position);
					mIsCheckList.add(position, Boolean.TRUE);
					mSelectedCount++;
					updateCount();
				} else {
					DeskToast.makeText(AppListMultiple.this,
							getString(R.string.homescreen_full_warning), Toast.LENGTH_SHORT).show();
				}
			} else {
				mIntents.remove(itemInfo.mIntent);
				if (checkBox != null) {
					checkBox.setChecked(false);
				}
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
			Intent i = getIntent();
			Bundle b = new Bundle();
			b.putParcelableArrayList(INTENT_LIST_STRING, mIntents);

			i.putExtras(b);
			setResult(RESULT_OK, i);
			finish();
		} else if (v == mButtonNo) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

}

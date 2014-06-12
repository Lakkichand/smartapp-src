/**
 * 
 */
package com.jiubang.ggheart.apps.desks.appfunc;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.launcher.taskmanager.TaskMgrControler;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DesktopIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IIndicatorUpdateListner;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicatorItem;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.components.MutilCheckGridView;
import com.jiubang.ggheart.components.MutilCheckViewAdapter;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.AppItemInfo;

/**
 * 列表
 * 
 * @author penglong
 * 
 */
public class LockList extends Activity
		implements
			IIndicatorUpdateListner,
			OnClickListener,
			OnItemClickListener {
	private ArrayList<AppItemInfo> mList;

	public static final String URI_STRING = "uri";
	public static final String INTENT_STRING = "intent";
	public static final String ID_STRING = "id";
	public static final String ROWNUM_STRING = "row";
	public static final String TITLE_STRING = "title";
	private MutilCheckGridView mGridView;
	// private MyAdapter mAdapter = null;
	private DesktopIndicator mIndicator;
	private static final int INITFINISH = 1;
	private boolean[] mCheckTable;
	private boolean[] mModifyCheckTable;
	private Button mOk;
	private Button mCancle;
	private RelativeLayout mContentLayout;
	private Thread mInitThread;
	private LayoutInflater mInflater;
	private Object mMutex = null;
	private GoProgressBar mGoProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.lock_list);
		mMutex = new Object();
		mInflater = LayoutInflater.from(this);
		mContentLayout = (RelativeLayout) findViewById(R.id.contentview);
		mGridView = (MutilCheckGridView) findViewById(R.id.gridview);
		mIndicator = (DesktopIndicator) findViewById(R.id.folder_indicator);
		mIndicator.setDefaultDotsIndicatorImage(R.drawable.setting_dotindicator_lightbar,
				R.drawable.setting_dotindicator_normalbar);
		mIndicator.setDotIndicatorLayoutMode(ScreenIndicator.LAYOUT_MODE_ADJUST_PICSIZE);
		mIndicator.setDotIndicatorDrawMode(ScreenIndicatorItem.DRAW_MODE_INDIVIDUAL);
		mIndicator.setIndicatorListner(this);
		initList();
		mGridView.setHanler(mHandler);
		mGridView.setmIndicatorUpdateListner(this);
		mOk = (Button) findViewById(R.id.finish_btn);
		mCancle = (Button) findViewById(R.id.cancle_btn);
		mOk.setOnClickListener(this);
		mCancle.setOnClickListener(this);
		DeskSettingConstants.setTextViewTypeFace((TextView) findViewById(R.id.title));
		mGoProgressBar = (GoProgressBar) findViewById(R.id.appfunc_lock_progress);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
				case MutilCheckGridView.UPDATEINDICATOR : {
					if (mIndicator != null) {
						mIndicator.updateIndicator(msg.arg1, (Bundle) msg.obj);
					}

				}
					break;
				case INITFINISH : {
					dismissProgressDialog();
					if (mGridView != null) {
						mGridView.initLayoutData(mList.size());
						setAdapter();
						if (mIndicator != null) {
							mIndicator.setCurrent(0);
							mIndicator.setTotal(mGridView.getScreenCount());
						}
					}

				}
					break;

			}
		}

	};

	@Override
	protected void onDestroy() {
		synchronized (mMutex) {
			super.onDestroy();
			dismissProgressDialog();
			if (mList != null) {
				mList.clear();
				mList = null;
			}
			mIndicator.setIndicatorListner(null);
			mIndicator = null;
			mGridView.recyle();
			mGridView = null;
			mModifyCheckTable = null;
			mCheckTable = null;
			mContentLayout = null;
		}
		if (mCancle != null && mCancle instanceof DeskButton) {
			((DeskButton) mCancle).selfDestruct();
			mCancle = null;
		}
		if (mOk != null && mOk instanceof DeskButton) {
			((DeskButton) mOk).selfDestruct();
			mOk = null;
		}
	}

	@Override
	public void clickIndicatorItem(int index) {
		// TODO Auto-generated method stub
		mGridView.snapToScreen(index, false, -1);
	}

	@Override
	public void sliding(float percent) {
		// TODO Auto-generated method stub
		if (0 <= percent && percent <= 100) {
			mGridView.getScreenScroller().setScrollPercent(percent);
		}
	}

	@Override
	public void updateIndicator(int num, int current) {
		// TODO Auto-generated method stub
		if (num >= 0 && current >= 0 && current < num && mIndicator != null) {
			mIndicator.setTotal(num);
			mIndicator.setCurrent(current);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mOk) {
			if (mCheckTable != null && mList != null) {
				AppDrawerControler appDrawerControler = AppDrawerControler.getInstance(GoLauncher.getContext());
				for (int i = 0; i < mCheckTable.length && i < mList.size(); i++) {
					if (mModifyCheckTable[i] != mCheckTable[i]) {
						AppItemInfo info = mList.get(i);
						if (mModifyCheckTable[i]) {
//							AppCore.getInstance().getTaskMgrControler()
//									.addIgnoreAppItem(info.mIntent);
							appDrawerControler.addIgnoreAppItem(info.mIntent);
						} else {
//							AppCore.getInstance().getTaskMgrControler()
//									.delIgnoreAppItem(info.mIntent);
							appDrawerControler.delIgnoreAppItem(info.mIntent);
						}
					}
				}
			}
			finish();
		} else if (v == mCancle) {
			finish();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		android.view.ViewGroup.LayoutParams layoutParams = mContentLayout.getLayoutParams();
		layoutParams.height = (int) getResources().getDimension(R.dimen.folder_edit_view_height);
		layoutParams.width = (int) getResources().getDimension(R.dimen.folder_edit_view_width);
		mContentLayout.setLayoutParams(layoutParams);
		if (mGridView != null) {
			mGridView.changeOrientation();
			mGridView.removeAllViews();
			if (mList == null) {
				initList();
			} else {
				mGridView.initLayoutData(mList.size());
				setAdapter();
				mIndicator.setTotal(mGridView.getScreenCount());
				mIndicator.setCurrent(0);
			}
		}
	}

	private void showProgressDialog() {
		if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.INVISIBLE) {
			mGoProgressBar.setVisibility(View.VISIBLE);
		}
	}

	private void initList() {
		showProgressDialog();
		mInitThread = new Thread() {

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				synchronized (mMutex) {
					super.run();
					AppCore appCore = AppCore.getInstance();
					if (appCore != null) {
						TaskMgrControler taskMgrControler = appCore.getTaskMgrControler();
						if (taskMgrControler != null) {
							ArrayList<AppItemInfo> temp = taskMgrControler.getAllAppItemInfos();
							if (temp != null) {
								mList = (ArrayList<AppItemInfo>) temp.clone();
							}

							if (mList != null) {
								mCheckTable = new boolean[mList.size()];
								mModifyCheckTable = new boolean[mList.size()];
								for (int i = 0; i < mList.size(); i++) {
									AppItemInfo info = mList.get(i);
									boolean isIgnoreTask = taskMgrControler
											.isIgnoreTask(info.mIntent);
									mCheckTable[i] = mModifyCheckTable[i] = isIgnoreTask;
								}
							}
							Message message = mHandler.obtainMessage();
							message.what = INITFINISH;
							mHandler.sendMessage(message);
						}
					}
				}
			}

		};
		mInitThread.start();
		mInitThread = null;
	}

	private void dismissProgressDialog() {
		if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.VISIBLE) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
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
			page.setAdapter(new MyAdapter(this, tempList, i));
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
		int screenIndex = adapter.mScreen;
		int itemsCountPerScreen = mGridView.getCountPerPage();
		int p = position + screenIndex * itemsCountPerScreen;
		if (p > mModifyCheckTable.length) {
			return;
		}
		if (mModifyCheckTable[p]) {
			mModifyCheckTable[p] = false;
		} else {
			mModifyCheckTable[p] = true;
		}
		adapter.notifyDataSetChanged();
	}

	/**
	 * 
	 * 自定义适配器
	 *
	 */
	private class MyAdapter extends MutilCheckViewAdapter {

		public MyAdapter(Context context, ArrayList<Object> list, int screenIndex) {
			super(list, screenIndex);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			AppItemInfo info = null;
			try {
				info = (AppItemInfo) getItem(position);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}

			if (info == null) {
				return null;
			}
			if (convertView == null) {
				try {
					convertView = mInflater.inflate(R.layout.grid_multi_choice_item, parent, false);
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
			textView.setCompoundDrawablesWithIntrinsicBounds(null, info.mIcon, null, null);;
			textView.setText(info.mTitle);
			convertView.setTag(info);
			return convertView;
		}
	}

	private boolean getCheckStatus(int screen, int position) {
		if (mModifyCheckTable == null) {
			return false;
		} else {
			return mModifyCheckTable[screen * mGridView.getCountPerPage() + position];
		}
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

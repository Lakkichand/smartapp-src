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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.SortUtils;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.components.DeskListActivity;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 应用程序列表
 * 
 * @author ruxueqin
 * 
 */
//CHECKSTYLE:OFF
public class AppList extends DeskListActivity implements OnItemClickListener {
	private List<AppItemInfo> mList;

	public static final String URI_STRING = "uri";
	public static final String INTENT_STRING = "intent";
	public static final String ID_STRING = "id";
	public static final String ROWNUM_STRING = "row";
	public static final String TITLE_STRING = "title";

	private applistAdapter mAdapter = null;
	private ProgressDialog mProgressDialog = null;

	private Object mMutex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.applist);

		mAdapter = new applistAdapter(this);
		mMutex = new Object();
		initParams();
		initList();
	}

	private void initParams() {
		final ListView listView = getListView();
		listView.setFastScrollEnabled(true);
		listView.setOnItemClickListener(this);
	}

	public void initList() {
		// 显示提示框
		showProgressDialog();

		// 异步初始化
		final AppDataEngine engine = GOLauncherApp.getAppDataEngine();
		new Thread(ThreadName.DOCK_INIT_APPLIST) {
			@Override
			public void run() {
				// 初始化数据
				synchronized (mMutex) {
					mList = engine.getCompletedAppItemInfosExceptHide();
					if (null != mAdapter) {
						mAdapter.notifyDataSetChanged();
					}
					sortByLetter();
				}

				// 对外通知
				Message msg = new Message();
				msg.what = APPLIST_INIT_OK;
				mHandler.sendMessage(msg);
			};
		}.start();
	}

	private final static int APPLIST_INIT_OK = 1000;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case APPLIST_INIT_OK :
					setListAdapter(mAdapter);

					// 取消加载框
					dismissProgressDialog();
					break;

				default :
					break;
			}
		};
	};

	private void showProgressDialog() {
		if (null == mProgressDialog) {
			mProgressDialog = ProgressDialog.show(this, null, getString(R.string.sort_processing),
					true);
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
							drawable = mList.get(mPosition).mIcon;
							title.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null,
									null);
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

	private class applistAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<TextView> mTitle = new ArrayList<TextView>();

		public applistAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			synchronized (mMutex) {
				if (mList != null) {
					return mList.size();
				}
				return 0;
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
					convertView = mInflater.inflate(R.layout.app_list_item, null);
					holder.title = (TextView) convertView;
					mTitle.add(holder.title);
					convertView.setTag(holder);
				} catch (Exception e) {
					e.printStackTrace();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			try {
				if (holder != null) {
					holder.mPosition = position;
					Drawable iconDrawable = mList.get(position).mIcon;
					holder.title
					.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, null, null);
					holder.title.setText(mList.get(position).mTitle);
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
			mAdapter = null;

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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mList != null) {
			Intent i = getIntent();
			Bundle b = new Bundle();
			AppItemInfo itemInfo = mList.get(position);

			if (itemInfo != null) {
				b.putString(TITLE_STRING, itemInfo.mTitle);
				b.putInt(ROWNUM_STRING, position);
				b.putParcelable(INTENT_STRING, itemInfo.mIntent);
				b.putString(URI_STRING, itemInfo.mIntent.toURI());
				b.putLong(ID_STRING, itemInfo.mID);
			}

			i.putExtras(b);
			setResult(RESULT_OK, i);
		}

		finish();
	}
}

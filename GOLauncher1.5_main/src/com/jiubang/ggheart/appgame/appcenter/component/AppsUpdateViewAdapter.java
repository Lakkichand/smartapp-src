package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.contorler.ApplicationManager;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * 应用更新数据适配器
 */

public class AppsUpdateViewAdapter extends BaseAdapter {

	private ArrayList<AppBean> mAppBeanList = null;
	private LayoutInflater mLayoutInflater = null;
	private Context mContext = null;
	/**
	 * 默认图标
	 */
	private Bitmap mDefaultBitmap = null;

	public AppsUpdateViewAdapter(Context context,
			ArrayList<AppBean> appBeanArrayList) {
		mContext = context;
		mAppBeanList = appBeanArrayList;
		mLayoutInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mAppBeanList == null ? 0 : mAppBeanList.size();
	}

	@Override
	public Object getItem(int position) {
		Object object = null;
		if (mAppBeanList != null && position < mAppBeanList.size()) {
			object = mAppBeanList.get(position);
		}
		return object;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AppsUpdateInfoListItem appInfoListItem = null;
		if (mAppBeanList != null && position < mAppBeanList.size()) {
			final AppBean appBean = mAppBeanList.get(position);
			if (convertView != null
					&& convertView instanceof AppsUpdateInfoListItem) {
				appInfoListItem = (AppsUpdateInfoListItem) convertView;
				appInfoListItem.resetDefaultStatus();
			}
			if (appInfoListItem == null) {
				appInfoListItem = (AppsUpdateInfoListItem) mLayoutInflater
						.inflate(
								R.layout.recomm_appsmanagement_upate_list_item,
								null);
			}
			final LinearLayout mShowDetailAndUpdate = (LinearLayout) appInfoListItem
					.findViewById(R.id.intro);
			if (appBean.mIsOpen) {
				mShowDetailAndUpdate.setVisibility(View.VISIBLE);
			} else {
				mShowDetailAndUpdate.setVisibility(View.GONE);
			}
			appInfoListItem.bindAppBean(mContext, position, appBean,
					mDefaultBitmap);
			appInfoListItem.getmNoUpdate().setOnClickListener(mOnClickListen);
			appInfoListItem.getmContentLayout().setOnTouchListener(new OnTouchListener() {
				
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					int state = event.getAction();
					switch (state) {
						case MotionEvent.ACTION_DOWN :
							v.setBackgroundResource(R.drawable.tab_press);
							break;
						case MotionEvent.ACTION_MOVE :
							break;
						case MotionEvent.ACTION_UP :
							v.setBackgroundDrawable(null);
							int position = (Integer) v.getTag();
							if (mAppBeanList != null
									&& position < mAppBeanList.size()) {
								if (appBean.mIsOpen) {
									appBean.mIsOpen = false;
									mShowDetailAndUpdate
											.setVisibility(View.GONE);
								} else {
									appBean.mIsOpen = true;
									mShowDetailAndUpdate
											.setVisibility(View.VISIBLE);
									AppsManagementActivity
											.sendMessage(
													this,
													IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
													IDiyMsgIds.TOP_OF_LISTVIEW,
													position, null, null);
								}
							}
							break;
						case MotionEvent.ACTION_CANCEL :
							v.setBackgroundDrawable(null);
						default :
							break;
					}
					return true;
				}
			});
		}
		return appInfoListItem;
	}

	private OnClickListener mOnClickListen = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			if (mAppBeanList != null && position < mAppBeanList.size()) {
				AppBean appBean = mAppBeanList.get(position);
				appBean.mIsOpen = false;
				addNoUpdateApp(appBean.mPkgName, position);

			}
		}
	};

	/**
	 * 添加忽略更新的应用
	 * 
	 * @param packageName
	 */
	private void addNoUpdateApp(String packageName, int position) {
		StatisticsData
				.countStatData(mContext, StatisticsData.KEY_IGNORE_UPDATA);

		AppsManagementActivity.sendMessage(this,
				IDiyFrameIds.APPS_MANAGEMENT_UPDATE_APP_FRAME,
				IDiyMsgIds.CHANGE_APPLIST_INFO, position, packageName, null);
	}

	public void setDataSet(ArrayList<AppBean> appBeanArrayList) {
		mAppBeanList = appBeanArrayList;
		if (mAppBeanList != null && !mAppBeanList.isEmpty()) {
			ApplicationManager manager = AppsManagementActivity
					.getApplicationManager();

			for (AppBean appBean : mAppBeanList) {
				if (!manager.checkIfVersionSyn(appBean)) {
					manager.checkDownloadStatus(mContext, appBean);
				}
			}
		}
		notifyDataSetChanged();
	}

	public void notifyDataSetChanged(AppsUpdateView parent, AppBean appBean) {
		if (mAppBeanList != null && !mAppBeanList.isEmpty()) {
			int position = mAppBeanList.indexOf(appBean);
			if (position > -1) {
				int firstVisiblePos = parent.getFirstVisiblePosition();
				AppsUpdateInfoListItem item = (AppsUpdateInfoListItem) parent
						.getChildAt(position - firstVisiblePos);
				if (item != null) {
					item.setStatus(appBean.getStatus());
					//					this.notifyDataSetChanged();
				}
			}
		}
	}

	public ArrayList<AppBean> getAppBeanList() {
		return mAppBeanList;
	}

	/**
	 * 设置列表展现的默认图标
	 */
	public void setDefaultIcon(Drawable drawable) {
		if (drawable != null && drawable instanceof BitmapDrawable) {
			mDefaultBitmap = ((BitmapDrawable) drawable).getBitmap();
		}
	}

}
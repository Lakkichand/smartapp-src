package com.jiubang.ggheart.apps.appmanagement.component;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.appmanagement.controler.ApplicationManager;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class AppsUpdateViewAdapter extends BaseAdapter {

	private ArrayList<AppBean> mAppBeanList = null;
	private LayoutInflater mLayoutInflater = null;
	private Context mContext = null;
	private ArrayList<AppItemInfo> mAppItemInfos;

	public AppsUpdateViewAdapter(Context context, ArrayList<AppBean> appBeanArrayList) {
		mContext = context;
		mAppBeanList = appBeanArrayList;
		mLayoutInflater = LayoutInflater.from(mContext);
		mAppItemInfos = GOLauncherApp.getAppDataEngine().getAllAppItemInfos();
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
			AppBean appBean = mAppBeanList.get(position);
			if (convertView != null && convertView instanceof AppsUpdateInfoListItem) {
				appInfoListItem = (AppsUpdateInfoListItem) convertView;
				appInfoListItem.resetDefaultStatus();
			}
			if (appInfoListItem == null) {
				appInfoListItem = (AppsUpdateInfoListItem) mLayoutInflater.inflate(
						R.layout.appsmanagement_upate_list_item, null);
			}
			appInfoListItem.bindAppBean(mContext, position, appBean, mAppItemInfos);
		}
		return appInfoListItem;
	}

	public void setDataSet(ArrayList<AppBean> appBeanArrayList) {
		mAppBeanList = appBeanArrayList;
		if (mAppBeanList != null && !mAppBeanList.isEmpty()) {
			ApplicationManager manager = AppsManagementActivity.getApplicationManager();

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
				AppsUpdateInfoListItem item = (AppsUpdateInfoListItem) parent.getChildAt(position
						- firstVisiblePos);
				if (item != null) {
					item.setStatus(appBean.getStatus());
				}
			}
		}
	}

	public ArrayList<AppBean> getAppBeanList() {
		return mAppBeanList;
	}
}
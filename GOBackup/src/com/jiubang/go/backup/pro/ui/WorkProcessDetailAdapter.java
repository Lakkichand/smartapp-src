package com.jiubang.go.backup.pro.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.BaseEntry.EntryType;
import com.jiubang.go.backup.pro.data.CallLogBackupEntry;
import com.jiubang.go.backup.pro.data.ContactsBackupEntry;
import com.jiubang.go.backup.pro.data.GoLauncherSettingBackupEntry;
import com.jiubang.go.backup.pro.data.SmsBackupEntry;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine;
import com.jiubang.go.backup.pro.model.AsyncWorkEngine.WorkDetailBean;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author maiyongshen
 */
public class WorkProcessDetailAdapter extends BaseAdapter {
	private List<WorkDetailBean> mWorksItemDetail = new ArrayList<AsyncWorkEngine.WorkDetailBean>();
	private Map<Integer, Drawable> mIcons = new HashMap<Integer, Drawable>();
	private Context mContext;
	private LayoutInflater mInflater;
	private OnViewUpdateListener mOnViewUpdateListener;

	public WorkProcessDetailAdapter(Context context) {
		mContext = context.getApplicationContext();
		mInflater = LayoutInflater.from(context);
	}

	public void init(List<WorkDetailBean> detailBeans) {
		mWorksItemDetail = detailBeans;
		notifyDataSetChanged();
	}

	public void update(WorkDetailBean detailBean) {
		if (detailBean == null) {
			return;
		}
		if (Util.isCollectionEmpty(mWorksItemDetail)) {
			return;
		}
		int count = mWorksItemDetail.size();
		for (int i = 0; i < count; i++) {
			WorkDetailBean bean = mWorksItemDetail.get(i);
			if (bean.workId == detailBean.workId) {
				updateWorkDetailBean(bean, detailBean);
				notifyDataSetChanged();
				if (mOnViewUpdateListener != null) {
					mOnViewUpdateListener.onChildViewUpdated(i);
				}
				return;
			}
		}
	}

	private void updateWorkDetailBean(WorkDetailBean srcBean, WorkDetailBean destBean) {
		srcBean.workId = destBean.workId;
		srcBean.workObjectType = destBean.workObjectType;
		srcBean.workProgress = destBean.workProgress;
		// srcBean.workObject = destBean.workObject;
		// srcBean.workState = destBean.workState;
		srcBean.title = destBean.title;
	}

	@Override
	public int getCount() {
		return mWorksItemDetail.size();
	}

	@Override
	public Object getItem(int position) {
		return mWorksItemDetail.get(position);
	}

	@Override
	public long getItemId(int position) {
		return ((WorkDetailBean) getItem(position)).workId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.layout_work_progress_detail_item, parent,
					false);
		}
		WorkDetailBean item = (WorkDetailBean) getItem(position);
		if (item == null) {
			return null;
		}

		View icon = convertView.findViewById(R.id.icon);
		icon.setBackgroundDrawable(getIcon(item));

		TextView title = (TextView) convertView.findViewById(R.id.title);
		// title.setText(item.workObject + item.workState);
		title.setText(item.title);

		TextView progress = (TextView) convertView.findViewById(R.id.progress_detail);
		progress.setText(item.workProgress);
		return convertView;
	}

	private Drawable getIcon(WorkDetailBean workDetailBean) {
		Drawable icon = mIcons.get(workDetailBean.workId);
		if (icon != null) {
			return icon;
		}
		// workObjectType 为空 表示数据库文件
		if (workDetailBean.workObjectType == null) {
			icon = mContext.getResources().getDrawable(R.drawable.icon_database);
		} else {
			icon = getIconByEntryType(workDetailBean.workObjectType);
		}
		if (icon == null) {
			icon = getDefaultIcon();
		}
		mIcons.put(workDetailBean.workId, icon);
		return icon;
	}

	public Context getContext() {
		return mContext;
	}

	public Drawable getIconByEntryType(EntryType type) {
		switch (type) {
			case TYPE_SYSTEM_APP :
			case TYPE_USER_APP :
				return getContext().getResources().getDrawable(R.drawable.icon_app);
			case TYPE_SYSTEM_WALLPAPER :
				return getContext().getResources().getDrawable(R.drawable.icon_wallpaper);
			case TYPE_SYSTEM_RINGTONE :
				return getContext().getResources().getDrawable(R.drawable.icon_ringtone);
			case TYPE_USER_CONTACTS :
				return Util.loadIconFromPackageName(getContext(),
						ContactsBackupEntry.CONTACTS_PACKAGE_NAME);
			case TYPE_USER_SMS :
				return Util.loadIconFromPackageName(getContext(), SmsBackupEntry.MMS_PACKAGE_NAME);
			case TYPE_USER_BOOKMARK :
				return getContext().getResources().getDrawable(R.drawable.icon_bookmark);
			case TYPE_USER_MMS :
				return getContext().getResources().getDrawable(R.drawable.icon_mms);
			case TYPE_USER_CALL_HISTORY :
				return Util.loadIconFromPackageName(getContext(),
						CallLogBackupEntry.CALLLOG_PACKAGE_NAME);
			case TYPE_USER_DICTIONARY :
				return getContext().getResources().getDrawable(R.drawable.icon_user_dictionary);
			case TYPE_USER_GOLAUNCHER_SETTING :
				return Util.loadIconFromPackageName(getContext(),
						GoLauncherSettingBackupEntry.GOLAUNCHER_PACKAGE_NAME);
			case TYPE_SYSTEM_LAUNCHER_DATA :
				return getContext().getResources().getDrawable(R.drawable.icon_launcher_data);
			case TYPE_SYSTEM_WIFI :
				return getContext().getResources().getDrawable(R.drawable.icon_wifi);
			case TYPE_USER_CALENDAR :
				return getContext().getResources().getDrawable(R.drawable.icon_calendar);
			case TYPE_USER_IMAGE :
				return getContext().getResources().getDrawable(R.drawable.icon_image);
			default :
				return getDefaultIcon();
		}
	}

	private Drawable getDefaultIcon() {
		return mContext.getPackageManager().getDefaultActivityIcon();
	}

	public void clear() {
		if (mWorksItemDetail != null) {
			mWorksItemDetail.clear();
		}
		if (mIcons != null) {
			mIcons.clear();
		}
	}

	public void setOnViewUpdateListener(OnViewUpdateListener listener) {
		mOnViewUpdateListener = listener;
	}

	/**
	 * onViewUpdateListener 接口
	 */
	public static interface OnViewUpdateListener {
		public void onChildViewUpdated(int position);
	}

}

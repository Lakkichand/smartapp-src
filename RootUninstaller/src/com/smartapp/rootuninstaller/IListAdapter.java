package com.smartapp.rootuninstaller;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartapp.rootuninstaller.ImageManager.ImageCallback;

/**
 * 应用列表适配器
 * 
 * @author xiedezhi
 * 
 */
public class IListAdapter extends BaseAdapter {
	/**
	 * adapter类型，用户应用列表
	 */
	public static final int ADAPTER_TYPE_USERAPP = 1;
	/**
	 * adapter类型，系统应用列表
	 */
	public static final int ADAPTER_TYPE_SYSTEMAPP = 2;
	/**
	 * adapter类型，禁用应用列表
	 */
	public static final int ADAPTER_TYPE_DISABLEAPP = 3;

	private final int mType;

	private List<ListDataBean> mDataSource = new ArrayList<ListDataBean>();

	private Context mContext;
	private LayoutInflater mInflater;
	/**
	 * 用于向MainActivity发送消息
	 */
	private Handler mHandler;

	public IListAdapter(Context context, int type, Handler handler) {
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mHandler = handler;
		if (type != ADAPTER_TYPE_USERAPP && type != ADAPTER_TYPE_SYSTEMAPP
				&& type != ADAPTER_TYPE_DISABLEAPP) {
			throw new IllegalArgumentException("Illegal Adapter Type = " + type);
		}
		mType = type;
	}

	/**
	 * 更新数据源，调用notifyDataSetChanged
	 */
	public void update(List<ListDataBean> src) {
		mDataSource.clear();
		if (src != null) {
			for (ListDataBean bean : src) {
				mDataSource.add(bean);
			}
		}
		notifyDataSetChanged();
		mHandler.sendEmptyMessage(MainActivity.REFRESH_BTN);
	}

	@Override
	public int getCount() {
		return mDataSource.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, null);
		}
		ListDataBean bean = mDataSource.get(position);
		convertView.setTag(bean);

		final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		icon.setTag(bean.mInfo.packageName);

		// 用图片管理器获取图片
		ImageManager im = ImageManager.getInstance(mContext);
		Drawable drawable = im.loadDrawable(bean.mInfo.applicationInfo,
				bean.mInfo.packageName, new ImageCallback() {

					@Override
					public void imageLoad(String pkName, Drawable drawable) {
						if (icon.getTag().equals(pkName)) {
							icon.setImageDrawable(drawable);
						}
					}
				});
		if (drawable == null) {
			icon.setImageResource(R.drawable.default_icon);
		} else {
			icon.setImageDrawable(drawable);
		}

		// 应用名字
		TextView name = (TextView) convertView.findViewById(R.id.entry_title);
		name.setText(bean.mAppName + " " + bean.mInfo.versionName);

		// SD卡图标
		ImageView sdcard = (ImageView) convertView.findViewById(R.id.sdcard);
		if (bean.mIsSDCardApp) {
			sdcard.setVisibility(View.VISIBLE);
		} else {
			sdcard.setVisibility(View.GONE);
		}

		// 应用大小
		TextView size = (TextView) convertView.findViewById(R.id.size);
		size.setText(bean.mFileSize);

		// 更新日期
		TextView date = (TextView) convertView.findViewById(R.id.date);
		date.setText(bean.mLastModified);

		// 后台运行程序或者用户/系统程序
		TextView backProcess = (TextView) convertView
				.findViewById(R.id.backgroundprocess);
		backProcess.setVisibility(View.VISIBLE);
		if (mType == ADAPTER_TYPE_USERAPP || mType == ADAPTER_TYPE_SYSTEMAPP) {
			backProcess.setTextColor(0xFFFF0000);
			// 展示后台进程和消耗内存
			if (!TextUtils.isEmpty(bean.mRunningMemory)) {
				backProcess.setText(mContext.getString(R.string.backprocess)
						+ " " + bean.mRunningMemory);
			} else {
				backProcess.setVisibility(View.GONE);
			}
		} else {
			// 展示系统应用/用户应用
			if (bean.mIsSystemApp) {
				backProcess.setTextColor(0xFFFF0000);
				backProcess.setText(R.string.systemapp);
			} else {
				backProcess.setTextColor(0xFF00FF00);
				backProcess.setText(R.string.userapp);
			}
		}

		// checkbox
		CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
		checkBox.setTag(bean);
		if (bean.mIsSelect) {
			checkBox.setChecked(true);
		} else {
			checkBox.setChecked(false);
		}
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				ListDataBean x = (ListDataBean) buttonView.getTag();
				if (x != null) {
					x.mIsSelect = isChecked;
					notifyDataSetChanged();
					mHandler.sendEmptyMessage(MainActivity.REFRESH_BTN);
				}
			}
		});
		return convertView;
	}

	/**
	 * 获取已选择的数量
	 */
	public int getSelectCount() {
		int ret = 0;
		for (ListDataBean bean : mDataSource) {
			if (bean.mIsSelect) {
				ret++;
			}
		}
		return ret;
	}

	/**
	 * 获取已选择的应用
	 */
	public List<ListDataBean> getSelectBeans() {
		List<ListDataBean> ret = new ArrayList<ListDataBean>();
		for (ListDataBean bean : mDataSource) {
			if (bean.mIsSelect) {
				ret.add(bean);
			}
		}
		return ret;
	}

}

package com.smartapp.appfreezer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartapp.appfreezer.ImageManager.ImageCallback;

public class IAdapter extends BaseAdapter {

	private List<ListDataBean> mDataSource = new ArrayList<ListDataBean>();

	private Context mContext;

	private LayoutInflater mInflater;

	private Handler mHandler;

	public IAdapter(Context context, Handler handler) {
		mContext = context;
		mInflater = LayoutInflater.from(context);

		mHandler = handler;
	}

	/**
	 * 更新数据源并调用notifyDataSetChanged
	 */
	public void update(List<ListDataBean> src) {
		mDataSource.clear();
		if (src != null) {
			for (ListDataBean bean : src) {
				mDataSource.add(bean);
			}
		}
		notifyDataSetChanged();
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

		TextView text = (TextView) convertView.findViewById(R.id.entry_title);
		text.setText(bean.mAppName);
		if (bean.mIsSystemApp) {
			text.setTextColor(0xFFFF0000);
		} else {
			text.setTextColor(0xFFFFFFFF);
		}

		TextView summary = (TextView) convertView
				.findViewById(R.id.entry_summary1);
		summary.setText(bean.mInfo.versionName);

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
				}
				mHandler.sendEmptyMessage(MainActivity.MSG_REFRESH_BTN);
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

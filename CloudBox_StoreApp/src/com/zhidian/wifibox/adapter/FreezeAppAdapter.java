package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.FreezeAppActivity;
import com.zhidian.wifibox.data.AppUninstallBean;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 回收站adapter
 * 
 * @author xiedezhi
 * 
 */
public class FreezeAppAdapter extends BaseAdapter {

	private FreezeAppActivity mActivity;

	private List<AppUninstallBean> mList = new ArrayList<AppUninstallBean>();

	private LayoutInflater mInflater = LayoutInflater.from(TAApplication
			.getApplication());

	public FreezeAppAdapter(FreezeAppActivity activity) {
		mActivity = activity;
	}

	/**
	 * 单选点击事件
	 */
	private OnClickListener mSelectItemListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AppUninstallBean bean = (AppUninstallBean) v.getTag();
			bean.isSelect = !bean.isSelect;
			mActivity.updateSelectText();
			notifyDataSetChanged();
		}
	};

	@Override
	public int getCount() {
		return mList.size();
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
			convertView = mInflater
					.inflate(R.layout.userappuninstallitem, null);
		}
		AppUninstallBean bean = mList.get(position);
		final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		icon.setTag(bean.packname);
		Bitmap bm = AsyncImageManager.getInstance().loadIcon(bean.packname,
				true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap != null && imgUrl.equals(icon.getTag())) {
							icon.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm == null) {
			icon.setImageBitmap(DrawUtil.sDefaultIcon);
		} else {
			icon.setImageBitmap(bm);
		}
		TextView title = (TextView) convertView.findViewById(R.id.title);
		title.setText(bean.appname);
		TextView info = (TextView) convertView.findViewById(R.id.info);
		// 显示上次打开时间
		info.setText("占用:"
				+ Formatter.formatShortFileSize(TAApplication.getApplication(),
						bean.size));
		ImageView select = (ImageView) convertView.findViewById(R.id.select);
		if (bean.isSelect) {
			select.setImageResource(R.drawable.cleanmaster_select);
		} else {
			select.setImageResource(R.drawable.cleanmaster_noselect);
		}
		select.setTag(bean);
		select.setOnClickListener(mSelectItemListener);
		View gap3 = convertView.findViewById(R.id.gap3);
		if (position == mList.size() - 1) {
			gap3.setVisibility(View.VISIBLE);
		} else {
			gap3.setVisibility(View.GONE);
		}
		View line = convertView.findViewById(R.id.line);
		if (position == mList.size() - 1) {
			line.setVisibility(View.GONE);
		} else {
			line.setVisibility(View.VISIBLE);
		}
		View gap4 = convertView.findViewById(R.id.gap4);
		if (position == mList.size() - 1) {
			gap4.setVisibility(View.VISIBLE);
		} else {
			gap4.setVisibility(View.GONE);
		}
		return convertView;
	}

	/**
	 * 更新数据
	 */
	public void update(List<AppUninstallBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

}

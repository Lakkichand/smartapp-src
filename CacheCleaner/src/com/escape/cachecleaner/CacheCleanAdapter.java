package com.escape.cachecleaner;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 缓存清理适配器
 * 
 * @author xiedezhi
 * 
 */
public class CacheCleanAdapter extends BaseAdapter {

	private List<CacheDataBean> mList = new ArrayList<CacheDataBean>();

	private LayoutInflater mInflater = LayoutInflater.from(TAApplication
			.getApplication());
	/**
	 * 清理点击事件
	 */
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 清理单个应用的缓存
			CacheDataBean bean = (CacheDataBean) v.getTag();
			mHandler.obtainMessage(-1, bean).sendToTarget();
		}
	};
	/**
	 * 用于向Activity发送消息
	 */
	private Handler mHandler;

	public CacheCleanAdapter(Handler handler) {
		mHandler = handler;
	}

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
			convertView = mInflater.inflate(R.layout.cache_item, null);
		}
		CacheDataBean bean = mList.get(position);
		final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		TextView name = (TextView) convertView.findViewById(R.id.name);
		TextView size = (TextView) convertView.findViewById(R.id.size);
		LinearLayout clean = (LinearLayout) convertView
				.findViewById(R.id.clean);
		clean.setTag(bean);
		clean.setOnClickListener(mClickListener);
		icon.setTag(bean.mInfo.packageName);
		Bitmap bm = AsyncImageManager.getInstance().loadIcon(
				bean.mInfo.packageName, true, new AsyncImageLoadedCallBack() {

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
		name.setText(bean.mName);
		size.setText(FileUtil
				.convertFileSize((long) (bean.mCache / 1024.0f + 0.5f)));
		return convertView;
	}

	/**
	 * 更新数据
	 */
	public void update(List<CacheDataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

	/**
	 * 获取数据列表
	 */
	public List<CacheDataBean> getData() {
		return mList;
	}

	/**
	 * 获取总的缓存大小
	 */
	public long getTotalCacheSize() {
		long ret = 0;
		for (CacheDataBean bean : mList) {
			ret = ret + bean.mCache;
		}
		return ret;
	}

}

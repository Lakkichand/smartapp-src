package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.TopicDataBean;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 专题数据适配器
 * 
 * @author xiedezhi
 * 
 */
public class TopicAdapter extends BaseAdapter {

	private List<TopicDataBean> mList = new ArrayList<TopicDataBean>();
	private Context mContext;
	/**
	 * item高度
	 */
	private int mItemHeight;

	public TopicAdapter(Context context) {
		this.mContext = context;
		float itemWidth = (new InfoUtil(mContext).getWidth())
				- (2 * mContext.getResources().getDimension(
						R.dimen.activity_layout_marginLeftRight) + 0.5f);
		mItemHeight = (int) (itemWidth * 1.0 / 462.0 * 134.0 + 0.5);
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
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.list_item_topic, null);
		}
		final ImageView image = (ImageView) convertView
				.findViewById(R.id.banner);
		image.setLayoutParams(new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, mItemHeight));
		TopicDataBean bean = mList.get(position);
		image.setTag(bean.bannerUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, bean.bannerUrl.hashCode() + "",
				bean.bannerUrl, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						if (image.getTag().equals(imgUrl)) {
							image.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm != null) {
			image.setImageBitmap(bm);
		} else {
			// 默认
			image.setImageBitmap(DrawUtil.sTopicDefaultBanner);
		}
		TextView name = (TextView) convertView.findViewById(R.id.app_name);
		name.setText(bean.title);
		TextView time = (TextView) convertView
				.findViewById(R.id.app_download_time);
		time.setText(bean.updateTime);
		TextView content = (TextView) convertView
				.findViewById(R.id.app_describe);
		content.setText(bean.description);
		convertView.setTag(R.string.app_name, bean);
		return convertView;
	}

	/**
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<TopicDataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

}

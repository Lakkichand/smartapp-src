package com.zhidian.wifibox.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AppDetailActivity;
import com.zhidian.wifibox.data.DetailDataBean.RelatedRecommendBean;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 相关推荐adapter
 * 
 * @author zhaoyl
 * 
 */
public class RelatedRecommendAdapter extends
		AbsListAdapter<RelatedRecommendBean> {

	public RelatedRecommendAdapter(Context context,
			List<RelatedRecommendBean> dataList, TAApplication application) {
		super(context, dataList);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.gridview_item_relatedrecommend, null);
			holder.ivAppImage = (ImageView) convertView
					.findViewById(R.id.gridview_image);
			holder.tvAppName = (TextView) convertView
					.findViewById(R.id.gridview_name);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		RelatedRecommendBean rb = mDataList.get(position);
		final long appId = rb.id;
		holder.tvAppName.setText(rb.name);
		final ImageView image = holder.ivAppImage;
		image.setTag(rb.iconUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, rb.iconUrl.hashCode() + "",
				rb.iconUrl, true, false, new AsyncImageLoadedCallBack() {

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
			// TODO 默认
		}

		holder.ivAppImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, AppDetailActivity.class);
				intent.putExtra("appId", appId);
				mContext.startActivity(intent);
			}
		});
		return convertView;
	}

	static class ViewHolder {
		ImageView ivAppImage; // 应用Logo
		TextView tvAppName; // 应用名称

	}

}

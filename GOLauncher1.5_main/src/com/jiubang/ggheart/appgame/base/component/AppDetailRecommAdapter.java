package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.launcher.LauncherEnv;
/**
 * 相关推荐列表
 */
public class AppDetailRecommAdapter extends BaseAdapter {

	private LayoutInflater mInflater = null;
	private Context mContext;
	private ArrayList<BoutiqueApp> mList = null;
	private AsyncImageManager mImgManager = null;
	private Drawable mDefaultIcon = null;
	private String mLocalPath = null;

	public AppDetailRecommAdapter(Context context,
			ArrayList<BoutiqueApp> list, AsyncImageManager imgManager) {
		mContext = context;
		mList = new ArrayList<BoutiqueApp>();
		mList = list;
		Log.i("zj", "AppDetailRecommAdapter:" + mList.size());
		mInflater = LayoutInflater.from(context);
		mImgManager = imgManager;
		mDefaultIcon = mContext.getResources().getDrawable(
				R.drawable.default_icon);
		mLocalPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
	}

	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		}
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.layout_app_management_detail_recommend_info, null);
			StateListDrawable bg = new StateListDrawable();
			Drawable pressed = mContext.getResources().getDrawable(R.drawable.tab_press);
			bg.addState(new int[] { android.R.attr.state_pressed,
					android.R.attr.state_enabled }, pressed);
			convertView.setBackgroundDrawable(bg);

			viewHolder.mImageView = (ImageView) convertView
					.findViewById(R.id.app_detail_recommend_icon);
			viewHolder.mTextView = (TextView) convertView
					.findViewById(R.id.app_detail_recommend_name);
			viewHolder.mRatingBar = (RatingBar) convertView
					.findViewById(R.id.app_detail_recommend_ratingbar);

			viewHolder.mTextView.setText(mList.get(position).info.name);
			if (!TextUtils.isEmpty(mList.get(position).info.icon)
					&& TextUtils.isEmpty(mList.get(position).picLocalFileName)) {
				mList.get(position).picLocalFileName = String.valueOf(mList
						.get(position).info.icon.hashCode());
			}

			// 设置ICON
			setIcon(viewHolder.mImageView, mList.get(position).info.icon,
					mLocalPath, mList.get(position).picLocalFileName, true);
			// 设置应用名
			viewHolder.mTextView.setText(mList.get(position).info.name);
			Log.i("zj", "mName:" + mList.get(position).info.name);

			// 设置星级
			viewHolder.mRatingBar.setRating(mList.get(position).info.grade);

			convertView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		return convertView;
	}
/**
 * 
 * @author zhujian
 * 推荐列表信息 
 */
	public class ViewHolder {
		ImageView mImageView;
		TextView mTextView;
		RatingBar mRatingBar;

	}

	private void setIcon(final ImageView imageView, String imgUrl,
			String imgPath, String imgName, boolean setDefaultIcon) {
		imageView.setTag(imgUrl);
		// TODO:XIEDEZHI 这里能不能不要每次load图片都生成一个回调对象？
		Bitmap bm = mImgManager.loadImage(imgPath, imgName, imgUrl, true,
				false, null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageView.getTag().equals(imgUrl)) {
							imageView.setImageBitmap(imageBitmap);
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		if (bm != null) {
			imageView.setImageBitmap(bm);
		} else {
			if (setDefaultIcon) {
				imageView.setImageDrawable(mDefaultIcon);
			} else {
				imageView.setImageDrawable(null);
			}
		}
	}
}

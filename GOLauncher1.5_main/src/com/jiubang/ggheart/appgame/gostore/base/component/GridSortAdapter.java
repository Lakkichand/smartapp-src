/**
 * 
 */
package com.jiubang.ggheart.appgame.gostore.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.utils.MD5;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 九宫格分类Adapter
 * 
 * @author zhouxuewen
 * 
 */
public class GridSortAdapter extends BaseAdapter {

	private List<CategoriesDataBean> mRecommAppCtgList = new ArrayList<CategoriesDataBean>();
	private Context mContext = null;
	private AsyncImageManager mImgManager = AsyncImageManager.getInstance();
	/**
	 * 列表默认图标
	 */
	private Drawable mDefaultIcon = null;
	/**
	 * 该分类推荐页是否在激活状态
	 */
	private boolean mIsActive = false;

	public GridSortAdapter(Context context) {
		mContext = context;
	}

	/**
	 * 刷新数据并调用notifyDataSetChanged
	 */
	public void refreshData(List<CategoriesDataBean> recommAppCtgList) {
		mRecommAppCtgList.clear();
		if (recommAppCtgList == null) {
			return;
		}
		for (CategoriesDataBean category : recommAppCtgList) {
			mRecommAppCtgList.add(category);
		}
		// notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mRecommAppCtgList == null ? 0 : mRecommAppCtgList.size();
	}

	@Override
	public Object getItem(int position) {
		if (mRecommAppCtgList == null || position < 0 || position >= mRecommAppCtgList.size()) {
			return null;
		}
		return mRecommAppCtgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Log.e("XIEDEZHI", "RecommendedAppsCtgAdapter getview position = " +
		// position);
		if (mRecommAppCtgList != null && position < mRecommAppCtgList.size()) {
			if (convertView == null) {
//				LayoutInflater inflater = (LayoutInflater) mContext
//						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				convertView = inflater.inflate(
//						R.layout.app_game_grid_sort_item, null);
				convertView = new GridSortItem(mContext);
			}
			CategoriesDataBean recommAppCtg = mRecommAppCtgList.get(position);
			convertView.setTag(R.id.appgame, recommAppCtg);
//			TextView title = (TextView) convertView.findViewById(R.id.recomm_app_nametext);
			TextView title = ((GridSortItem) convertView).getTextView();
			String name = recommAppCtg.name;
			if (name != null) {
				name = name.trim();
			}
			title.setText(name);
			String apps = recommAppCtg.desc;
			if (apps != null) {
				apps = apps.trim();
			}
//			ImageView image = (ImageView) convertView.findViewById(R.id.recomm_app_group_image);
			ImageView image = ((GridSortItem) convertView).getImageView();
			if (mIsActive) {
				if (recommAppCtg.pic != null && !recommAppCtg.pic.trim().equals("")) {
					String fileName = MD5.encode(recommAppCtg.pic);
					String localPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
					setIcon(image, recommAppCtg.pic, localPath, fileName);
				} else {
					image.setImageDrawable(mDefaultIcon);
				}
			} else {
				image.setImageDrawable(mDefaultIcon);
			}
		}
		return convertView;
	}

	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setIcon(final ImageView imageView, String imgUrl, String imgPath, String imgName) {
		imageView.setTag(imgUrl);
		Bitmap bm = mImgManager.loadImage(imgPath, imgName, imgUrl, true, false, null,
				new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageView.getTag().equals(imgUrl) && mIsActive) {
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
			imageView.setImageDrawable(mDefaultIcon);
		}
	}

	/**
	 * 更改激活状态，如果是true则getview时会加载图标，否则不加载图标
	 * 
	 * @param isActive
	 *            是否为激活状态
	 */
	public void onActiveChange(boolean isActive) {
		mIsActive = isActive;
	}

	/**
	 * 设置列表默认的图标
	 */
	public void setDefaultIcon(Drawable icon) {
		mDefaultIcon = icon;
	}

}

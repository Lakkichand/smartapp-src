/**
 * 
 */
package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 应用推荐
 * 
 * @author zhoujun
 * 
 */
public class RecommendedAppsCtgAdapter extends BaseAdapter {

	private List<CategoriesDataBean> mRecommAppCtgList = new ArrayList<CategoriesDataBean>();
	private Context mContext = null;
	private AsyncImageManager mImgManager = AsyncImageManager.getInstance();
	/**
	 * 列表默认图标
	 */
	private Bitmap mDefaultBitmap = null;
	/**
	 * 该分类推荐页是否在激活状态
	 */
	private boolean mIsActive = false;

	public RecommendedAppsCtgAdapter(Context context) {
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
				LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(
						R.layout.recomm_appsmanagement_recomm_list_group_item, null);
			}
			CategoriesDataBean recommAppCtg = mRecommAppCtgList.get(position);
			convertView.setTag(R.id.appgame, recommAppCtg);
			TextView title = (TextView) convertView.findViewById(R.id.recomm_app_nametext);
			String name = recommAppCtg.name;
			if (name != null) {
				name = name.trim();
			}
			title.setText(name);
			TextView desc = (TextView) convertView.findViewById(R.id.recomm_app_desc);
			String apps = recommAppCtg.desc;
			if (apps != null) {
				apps = apps.trim();
			}
			desc.setText(apps);
			ImageSwitcher switcher = (ImageSwitcher) convertView
					.findViewById(R.id.categories_imageswitcher);
			if (mIsActive) {
				if (!TextUtils.isEmpty(recommAppCtg.icon)) {
					String fileName = String.valueOf(recommAppCtg.icon.hashCode());
					String localPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
					setIcon(position, switcher, recommAppCtg.icon, localPath, fileName);
				} else {
					((ImageView) switcher.getCurrentView()).setImageBitmap(mDefaultBitmap);
				}
			} else {
				((ImageView) switcher.getCurrentView()).setImageBitmap(mDefaultBitmap);
			}
		}
		return convertView;
	}

	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setIcon(final int position, final ImageSwitcher switcher, String imgUrl, String imgPath, String imgName) {
		if (switcher.getTag() != null && switcher.getTag().equals(imgUrl)) {
			ImageView image = (ImageView) switcher.getCurrentView();
			Drawable drawable = image.getDrawable();
			if (drawable != null && drawable instanceof BitmapDrawable) {
				BitmapDrawable bDrawable = (BitmapDrawable) drawable;
				if (bDrawable.getBitmap() != null && bDrawable.getBitmap() != mDefaultBitmap) {
					return;
				}
			}
		}
		switcher.setTag(imgUrl);
		Bitmap bm = mImgManager.loadImageForList(position, imgPath, imgName, imgUrl, true, false, AppGameDrawUtils.getInstance().mMaskIconOperator,
				new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (switcher.getTag().equals(imgUrl)) {
							Drawable drawable = ((ImageView) switcher
									.getCurrentView()).getDrawable();
							if (drawable instanceof BitmapDrawable) {
								Bitmap bm = ((BitmapDrawable) drawable)
										.getBitmap();
								if (bm == mDefaultBitmap) {
									switcher.setImageDrawable(new BitmapDrawable(imageBitmap));
								}
							}
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		ImageView imageView = (ImageView) switcher.getCurrentView();
		if (bm != null) {
			imageView.setImageBitmap(bm);
		} else {
			imageView.setImageBitmap(mDefaultBitmap);
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
	public void setDefaultIcon(Drawable drawable) {
		if (drawable != null && drawable instanceof BitmapDrawable) {
			mDefaultBitmap = ((BitmapDrawable) drawable).getBitmap();
		}
	}

}

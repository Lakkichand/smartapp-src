package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import com.gau.go.launcherex.R;
import com.go.util.DeferredHandler;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.launcher.LauncherEnv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

/**
 * 应用中心，广告推荐位adapter
 * 
 * @author xiedezhi
 * 
 */
public class AppGameADAdapter extends BaseAdapter {
	
	public AppGameADAdapter(Context context) {
		mContext = context;
	}

	private Context mContext;

	/**
	 * 默认banner图
	 */
	private Drawable mDefaultBanner = null;

	/**
	 * 设置图片的handler
	 */
	private DeferredHandler mHandler = new DeferredHandler() {

		@Override
		public void handleIdleMessage(Message msg) {
			if (msg != null && msg.obj != null) {
				Object obj = msg.obj;
				if (obj instanceof List<?>) {
					List<?> list = (List<?>) obj;
					if (list.size() == 3) {
						Object obj1 = list.get(0);
						Object obj2 = list.get(1);
						Object obj3 = list.get(2);
						if (obj1 instanceof ImageSwitcher
								&& obj2 instanceof Bitmap
								&& obj3 instanceof String) {
							ImageSwitcher switcher = (ImageSwitcher) obj1;
							Bitmap imageBitmap = (Bitmap) obj2;
							String imgUrl = (String) obj3;
							if (switcher.getTag().equals(imgUrl)) {
								ImageView imageView = (ImageView) switcher
										.getNextView();
								imageView.setBackgroundDrawable(null);
								switcher.setImageDrawable(new BitmapDrawable(
										imageBitmap));
							} else {
								imageBitmap = null;
								imgUrl = null;
							}
						}
					}
				}
			}
		}
	};

	/**
	 * 数据源
	 */
	private List<BoutiqueApp> mDataSource = new ArrayList<BoutiqueApp>();

	@Override
	public int getCount() {
		if (mDataSource == null || mDataSource.size() <= 0) {
			return 0;
		}
		// 个数返回无限大，则可以循环滑动
		return Integer.MAX_VALUE;
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
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater
					.inflate(R.layout.appgame_adbanner_item, null);
		}
		if (mDataSource == null || mDataSource.size() <= 0) {
			return convertView;
		}
		// 真实的位置
		int truePosition = position % mDataSource.size();
		BoutiqueApp app = mDataSource.get(truePosition);
		convertView.setTag(R.id.appgame, app);
		ImageSwitcher switcher = (ImageSwitcher) convertView
				.findViewById(R.id.appgame_adbanner_switcher);
		setImage(switcher, app.pic, app.picLocalPath, app.picLocalFileName);
		return convertView;
	}

	/**
	 * 更新数据源并调用notifyDatasetChange
	 */
	public void update(List<BoutiqueApp> data) {
		if (mDataSource == null) {
			mDataSource = new ArrayList<BoutiqueApp>();
		} else {
			mDataSource.clear();
		}
		for (BoutiqueApp app : data) {
			// 初始化应用的图片路径
			String pic = app.pic;
			if (!(pic == null || pic.equals(""))) {
				String fileName = String.valueOf(pic.hashCode());
				app.picLocalPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
				app.picLocalFileName = fileName;
			}
			mDataSource.add(app);
		}
	}

	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setImage(final ImageSwitcher switcher, String imgUrl,
			String imgPath, String imgName) {
		if (switcher.getTag() != null && switcher.getTag().equals(imgUrl)) {
			return;
		}
		switcher.getCurrentView().clearAnimation();
		switcher.getNextView().clearAnimation();
		switcher.setTag(imgUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(imgPath, imgName, imgUrl, true, true,
				null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						Message message = new Message();
						List<Object> list = new ArrayList<Object>();
						list.add(switcher);
						list.add(imageBitmap);
						list.add(imgUrl);
						message.obj = list;
						mHandler.sendMessage(message);
					}
				});
		ImageView imageView = (ImageView) switcher.getCurrentView();
		if (bm != null) {
			imageView.setBackgroundDrawable(null);
			imageView.setImageBitmap(bm);
		} else {
			if (mDefaultBanner == null) {
				// 应用中心国内的某些渠道要修改名称，修改为 安卓应用市场（中文）/GO
				// Market（英文），所以要进行渠道控制，默认图片里面有名字，要跟着换
				// Add by wangzhuobin 2012.10.31
				int id = R.drawable.appcenter_feature_default_banner;
				mDefaultBanner = mContext.getResources().getDrawable(id);
			}
			imageView.setImageBitmap(null);
			imageView.setBackgroundDrawable(mDefaultBanner);
		}
	}

}

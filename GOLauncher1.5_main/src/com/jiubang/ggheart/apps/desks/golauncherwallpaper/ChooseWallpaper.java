/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiubang.ggheart.apps.desks.golauncherwallpaper;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.go.util.window.WindowControl;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 壁纸选择
 * 
 * @author
 * 
 */
public class ChooseWallpaper extends Activity implements
		AdapterView.OnItemSelectedListener, OnClickListener {

	public static final String CHOOSERTYPE = "ChooserType"; // 选择器类型
	public static final String TYPE_WALLPAPERCHOOSER = "WallpaperChooser"; // 墙纸选择器
	public static final String TYPE_BACKGROUNDCHOOSER = "BackgroundChooser"; // 功能表背景选择器
	public static final String TYPE_DOCK_BACKGROUNDCHOOSER = "dock_BackgroundChooser"; // DOCK背景选择器

	public static final String BACGROUND_IMG_RESID = "Bacground_img_resid"; // 功能表背景图片资源id
	public static final String BACGROUND_IMG_RESPKGNAME = "Bacground_img_resPkgName"; // 功能表背景图片资源包名
	public static final String BACGROUND_IMG_RESNAME = "Bacground_img_res_name"; // 功能表背景图片资源名字
	public static final String BACGROUND_IMG_NAME = "Bacground_img_name"; // 背景图片资源名

	public static final String DOCK_RESNAME = "dock_backgroundlist"; // dock背景列表标识串
	public static final String WALLPAER_RESNAME = "wallpaperlist"; // 墙纸背景列表标识串
	public static final String FUN_RESNAME = "backgroundlist"; // 功能表背景列表标识串

	public static final String DOCK_BG_IMAG_NAME = "dock"; // dock背景图名称
	public static final String WALLPAPER_BG_IMAG_NAME = "default_wallpaper"; // 壁纸背景图名称
	public static final String FUN_BG_IMAG_NAME = "funbg"; // 功能标背景图名称

	public static final String TRANSPARENT_BG = "transparent_bg"; // 功能标背景图名称

	private Gallery mGallery;
	private ImageView mImageView;

	private Bitmap mBitmap;

	/**
	 * 子选项bean
	 * 
	 * @author
	 * 
	 */
	private class ImageItem {
		String mImageResName;
		int mImageResId;
		Resources mResource;
		String mPackageName;
	}

	private ArrayList<ImageItem> mThumbs; // 缩略图
	private ArrayList<ImageItem> mImages; // 墙纸应用大图
	private WallpaperLoader mLoader;

	private ChooserStrategy mChooserStrategy; // 定制chooser的外观
	
	private boolean mIsMultiWallpaper = false; // 是否由多屏多壁纸进入

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		OutOfMemoryHandler.handle();
		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.wallpaper_chooser);

		mThumbs = new ArrayList<ImageItem>();
		mImages = new ArrayList<ImageItem>();

		// TODO:获取传递过来的数据
		Bundle bundle = getIntent().getExtras();
		String chooserTyep = null;
		if (bundle != null) {
			chooserTyep = bundle.getString(CHOOSERTYPE);
			mIsMultiWallpaper = bundle.getBoolean("ismultiwallpaper", false);
		}
		mChooserStrategy = createChooserStrategy(chooserTyep);
		// 定制外观
		mChooserStrategy.configShape();

		mGallery = (Gallery) findViewById(R.id.gallery);
		mGallery.setAdapter(new ImageAdapter(this));
		mGallery.setOnItemSelectedListener(this);
		mGallery.setCallbackDuringFling(false);

		findViewById(R.id.set).setOnClickListener(this);

		mImageView = (ImageView) findViewById(R.id.wallpaper);

	}

	/**
	 * 加载功能表背景图片
	 * 
	 * @author huyong
	 */
	private void loadDrawables(String drawablesResName) {
		if (drawablesResName == null) {
			return;
		}

		// 增加透明背景
		if (drawablesResName.equals(DOCK_RESNAME)) {
			addAnTranslateBg(DOCK_BG_IMAG_NAME);
		} else if (drawablesResName.equals(FUN_RESNAME)) {
			addAnTranslateBg(FUN_BG_IMAG_NAME);
		}

		// 将本程序对应的默认主题也添加进去
		addDrawables(getResources(), getApplication().getPackageName(),
				drawablesResName);

		// 查找主题包，并将主题包中的墙纸提取出来
		Intent intent = new Intent(ICustomAction.ACTION_MAIN_THEME_PACKAGE);
		intent.addCategory(ThemeManager.THEME_CATEGORY);
		PackageManager pm = getPackageManager();
		List<ResolveInfo> themes = pm.queryIntentActivities(intent, 0);
		int size = themes.size();
		String themePackage = null;
		Resources resources = null;
		for (int i = 0; i < size; i++) {
			themePackage = themes.get(i).activityInfo.packageName.toString();
			try {
				resources = pm.getResourcesForApplication(themePackage);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			addDrawables(resources, themePackage, drawablesResName);

		}

	}

	private void addDrawables(Resources resources, String packageName,
			String resName) {
		if (resources == null || packageName == null) {
			return;
		}

		// dock条背景：添加默认主题的背景到选择器中
		if (DOCK_RESNAME.equals(resName)
				&& packageName.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
			ImageItem thumbItem = new ImageItem();
			thumbItem.mImageResName = DOCK_BG_IMAG_NAME;
			thumbItem.mImageResId = R.drawable.dock_thumb; // R.drawable.dock_thumb;
			thumbItem.mResource = resources;
			thumbItem.mPackageName = packageName;
			mThumbs.add(thumbItem);

			ImageItem imageItem = new ImageItem();
			imageItem.mImageResName = DOCK_BG_IMAG_NAME;
			imageItem.mImageResId = R.drawable.dock;
			imageItem.mResource = resources;
			imageItem.mPackageName = packageName;
			mImages.add(imageItem);
		} else {

			try {
				int drawableList = resources.getIdentifier(resName, "array",
						packageName);
				if (drawableList <= 0) {
					return;
				}
				final String[] extras = resources.getStringArray(drawableList);

				for (String extra : extras) {

					int res = resources.getIdentifier(extra, "drawable",
							packageName);
					if (res != 0) {
						final int thumbRes = resources.getIdentifier(extra
								+ "_thumb", "drawable", packageName);
						if (thumbRes != 0) {
							ImageItem thumbItem = new ImageItem();
							thumbItem.mImageResName = extra;
							thumbItem.mImageResId = thumbRes;
							thumbItem.mResource = resources;
							thumbItem.mPackageName = packageName;
							mThumbs.add(thumbItem);

							ImageItem imageItem = new ImageItem();
							imageItem.mImageResName = extra;
							imageItem.mImageResId = res;
							imageItem.mResource = resources;
							imageItem.mPackageName = packageName;
							mImages.add(imageItem);
						}
					}
				}

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// mIsWallpaperSet = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mLoader != null
				&& mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
			mLoader.cancel(true);
			mLoader = null;
		}
		OutOfMemoryHandler.handle();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void onItemSelected(AdapterView parent, View v, int position, long id) {
		if (mLoader != null
				&& mLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
			mLoader.cancel();
		}
		mLoader = (WallpaperLoader) new WallpaperLoader(false)
				.execute(position);
	}

	/*
	 * When using touch if you tap an image it triggers both the onItemClick and
	 * the onTouchEvent causing the wallpaper to be set twice. Ensure we only
	 * set the wallpaper once.
	 */
	private void selectWallpaper(int position) {
		if (mImages == null || mImages.size() <= 0
				|| position >= mImages.size()) {
			return;
		}

		final ImageItem imageItem = mImages.get(position);
		if (imageItem == null) {
			return;
		}

		final Resources resources = imageItem.mResource;
		final int resId = imageItem.mImageResId;
		if (mIsMultiWallpaper) {
			// 如果是多屏多壁纸，则设置Uri
			try {
				Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
						+ resources.getResourcePackageName(resId) + "/"
						+ resources.getResourceTypeName(resId) + "/"
						+ resources.getResourceEntryName(resId));
				
				Intent intent = new Intent();
				intent.setData(uri);
				setResult(RESULT_OK, intent);
			} catch (Exception e) {
				Log.i("ChooseWalpaper", "selectWallpaper()" + position + " has exception " + e.getMessage());
			}
			

		} else {
			WindowControl.setWallpaper(this, resources, resId);
			// 结束activity
			setResult(RESULT_OK);
		}
		finish();
	}
	
	private void selectBackground(int position) {
		if (mImages == null || mImages.size() <= 0
				|| position >= mImages.size()) {
			return;
		}

		final ImageItem imageItem = mImages.get(position);
		if (imageItem == null) {
			return;
		}

		final int resId = imageItem.mImageResId;
		String resName = imageItem.mImageResName;

		// 结束activity
		// setResult(RESULT_OK);
		Bundle bundle = new Bundle();
		if (position == 0) {
			bundle.putInt(BACGROUND_IMG_RESID, resId);
			bundle.putString(BACGROUND_IMG_RESNAME, resName);
			bundle.putString(BACGROUND_IMG_RESPKGNAME, TRANSPARENT_BG);
		} else {
			bundle.putInt(BACGROUND_IMG_RESID, resId);
			bundle.putString(BACGROUND_IMG_RESNAME, resName);
			bundle.putString(BACGROUND_IMG_RESPKGNAME, imageItem.mPackageName);
		}
		setResult(IRequestCodeIds.REQUEST_OPERATION_SELECT_BACKGROUND,
				getIntent().putExtras(bundle));
		finish();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void onNothingSelected(AdapterView parent) {
	}

	/**
	 * 适配器
	 * 
	 * @author
	 * 
	 */
	private class ImageAdapter extends BaseAdapter {
		private LayoutInflater mLayoutInflater;

		ImageAdapter(ChooseWallpaper context) {
			mLayoutInflater = context.getLayoutInflater();
		}

		@Override
		public int getCount() {
			return mThumbs.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView image = (ImageView) convertView;

			if (mThumbs == null || position >= mThumbs.size()) {
				return null;
			}

			final ImageItem imageItem = mThumbs.get(position);
			if (imageItem == null) {
				return null;
			}

			try {
				if (convertView == null) {
					image = (ImageView) mLayoutInflater.inflate(
							R.layout.wallpaper_item, parent, false);
				}

				final Resources resources = imageItem.mResource;
				final int resId = imageItem.mImageResId;
				if (resources != null && resId >= 0) {
					final Drawable thumbDrawable = resources.getDrawable(resId);
					thumbDrawable.setDither(true);
					image.setImageDrawable(thumbDrawable);
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			} catch (Throwable e) {
				e.printStackTrace();
			}

			if (null == image) {
				image = new ImageView(getApplicationContext());
			}

			return image;
		}
	}

	@Override
	public void onClick(View v) {
		// selectWallpaper(mGallery.getSelectedItemPosition());
		mChooserStrategy.chooserOperator();
	}

	/**
	 * 异步图片加载任务
	 * 
	 * @author
	 * 
	 */
	class WallpaperLoader extends AsyncTask<Integer, Void, Bitmap> {
		BitmapFactory.Options mOptions;
		boolean mIsSetWallpaper; // 是否需要设置为壁纸

		WallpaperLoader(boolean isSetWallpaper) {
			mOptions = new BitmapFactory.Options();
			mOptions.inDither = false;
			mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

			mIsSetWallpaper = isSetWallpaper;
		}

		@Override
		protected Bitmap doInBackground(Integer... params) {
			if (isCancelled()) {
				return null;
			}
			try {
				// TODO:
				if (mImages == null || mImages.size() <= 0) {
					return null;
				}
				Resources resources = mImages.get(params[0]).mResource;
				int resId = mImages.get(params[0]).mImageResId;
				final Bitmap b = BitmapFactory.decodeResource(resources, resId,
						mOptions);

				return b;
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
				return null;
			} catch (Throwable e) {
				// TODO: handle exception
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap b) {
			if (b == null) {
				return;
			}

			if (!isCancelled() && !mOptions.mCancel) {
				// Help the GC
				if (mBitmap != null) {
					mBitmap.recycle();
					mBitmap = null;
				}

				final ImageView view = mImageView;
				view.setImageBitmap(b);

				mBitmap = b;

				final Drawable drawable = view.getDrawable();
				drawable.setFilterBitmap(true);
				drawable.setDither(true);

				view.postInvalidate();

				mLoader = null;

			} else {
				b.recycle();
			}
		}

		void cancel() {
			mOptions.requestCancelDecode();
			super.cancel(true);
		}
	}

	// class ChooserStrategyFactory {
	private ChooserStrategy createChooserStrategy(final String chooserTyep) {
		if (chooserTyep != null && chooserTyep.equals(TYPE_BACKGROUNDCHOOSER)) {

			return new BackgroundChooserStrategy();

		} else if (chooserTyep != null
				&& chooserTyep.equals(TYPE_DOCK_BACKGROUNDCHOOSER)) {
			return new DockBackgroundChooserStrategy();
		} else {

			return new WallPaperChooserStrategy();
		}
	}

	// }

	// 增加一张透明背景
	private void addAnTranslateBg(String extra) {

		ImageItem transparentThumbItem = new ImageItem();
		ImageItem transparentImageItem = new ImageItem();

		Resources res = getResources();

		if (extra.equals(FUN_BG_IMAG_NAME)) {
			// 增加一功能表张透明背景
			transparentThumbItem.mImageResId = R.drawable.desk_setting_fun_transparent_lite;
			transparentThumbItem.mResource = res;

			transparentImageItem.mImageResId = R.drawable.desk_setting_fun_transparent;
			transparentImageItem.mResource = res;
		} else if (extra.equals(DOCK_BG_IMAG_NAME)) {
			// 增加一张dock透明背景
			transparentThumbItem.mImageResId = R.drawable.desk_setting_dock_transparent_lite;
			transparentThumbItem.mResource = res;

			transparentImageItem.mImageResId = R.drawable.desk_setting_dock_transparent;
			transparentImageItem.mResource = res;
		} else if (extra.equals(WALLPAPER_BG_IMAG_NAME)) {
			// 背景图不增加

		}

		mThumbs.add(transparentThumbItem);
		mImages.add(transparentImageItem);

	}

	/**
	 * 
	 * 选择器
	 * 
	 */
	abstract class ChooserStrategy {
		String mDrawableResName = null;

		// 配置外观
		abstract void configShape();

		// 设置chooser的响应
		abstract void chooserOperator();
	}

	/**
	 * 
	 * @author
	 * 
	 */
	class DefaultChooserStrategy extends ChooserStrategy {
		DefaultChooserStrategy() {
			mDrawableResName = "default";
		}

		@Override
		void configShape() {
			// TODO Auto-generated method stub
			// TODO:设置默认提示语
			Button setButton = (Button) findViewById(R.id.set);
			if (setButton != null) {
				setButton.setText(R.string.set_onlyforgolauncher);
			}
		}

		@Override
		void chooserOperator() {
			// TODO Auto-generated method stub
			// 设置默认返回操作
			// 结束activity
			// setResult(RESULT_OK);
			finish();
		}
	}

	/**
	 * 墙纸选择器
	 * 
	 * @author huyong
	 * 
	 */
	class WallPaperChooserStrategy extends ChooserStrategy {
		WallPaperChooserStrategy() {
			mDrawableResName = WALLPAER_RESNAME;
		}

		@Override
		void configShape() {
			// TODO Auto-generated method stub
			Button setButton = (Button) findViewById(R.id.set);
			if (setButton != null) {
				setButton.setText(R.string.set_wallpaper);
			}
			// 加载图片
			loadDrawables(mDrawableResName);
		}

		@Override
		void chooserOperator() {
			// TODO Auto-generated method stub
			selectWallpaper(mGallery.getSelectedItemPosition());
		}
	}

	/**
	 * 功能表背景选择器
	 * 
	 * @author huyong
	 * 
	 */
	class BackgroundChooserStrategy extends ChooserStrategy {
		BackgroundChooserStrategy() {
			mDrawableResName = FUN_RESNAME;
		}

		@Override
		void configShape() {
			// TODO Auto-generated method stub
			Button setButton = (Button) findViewById(R.id.set);
			if (setButton != null) {
				setButton.setText(R.string.set_func_background);
			}
			// 加载图片
			loadDrawables(mDrawableResName);
		}

		@Override
		void chooserOperator() {
			// TODO Auto-generated method stub
			// 功能表背景选择器
			selectBackground(mGallery.getSelectedItemPosition());
		}
	}

	/**
	 * DOCK背景选择器
	 * 
	 * @author huyong
	 * 
	 */
	class DockBackgroundChooserStrategy extends ChooserStrategy {
		DockBackgroundChooserStrategy() {
			mDrawableResName = DOCK_RESNAME;
		}

		@Override
		void configShape() {
			// TODO Auto-generated method stub
			Button setButton = (Button) findViewById(R.id.set);
			if (setButton != null) {
				setButton.setText(R.string.set_dock_background);
			}
			// 加载图片
			loadDrawables(mDrawableResName);
		}

		@Override
		void chooserOperator() {
			// TODO Auto-generated method stub
			// 功能表背景选择器
			selectDockBg(mGallery.getSelectedItemPosition());
		}
	}

	private void selectDockBg(int position) {
		if (mImages == null || mImages.size() <= 0
				|| position >= mImages.size()) {
			return;
		}

		final ImageItem imageItem = mImages.get(position);
		if (imageItem == null) {
			return;
		}

		final String resnameString = imageItem.mImageResName;

		// 结束activity
		Bundle bundle = new Bundle();
		// TRANSPARENT_BG
		if (position == 0) {
			bundle.putString(BACGROUND_IMG_NAME, resnameString);
			bundle.putString(BACGROUND_IMG_RESPKGNAME, TRANSPARENT_BG);
		} else {
			bundle.putString(BACGROUND_IMG_NAME, resnameString);
			bundle.putString(BACGROUND_IMG_RESPKGNAME, imageItem.mPackageName);
		}
		setResult(IRequestCodeIds.REQUEST_OPERATION_SELECT_DOCK_BACKGROUND,
				getIntent().putExtras(bundle));
		finish();
	}

	@Override
	public Resources getResources() {
		DeskResourcesConfiguration configuration = DeskResourcesConfiguration
				.createInstance(this.getApplicationContext());
		if (null != configuration) {
			Resources resources = configuration.getDeskResources();
			if (null != resources) {
				return resources;
			}
		}
		return super.getResources();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}

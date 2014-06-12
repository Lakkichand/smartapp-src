/*
 * 文 件 名:  NewExtraAdapter.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-6
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.base.net.InstallCallbackManager;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.base.utils.AppGameInstallingValidator;
import com.jiubang.ggheart.appgame.base.utils.WrapOnClickListener;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-8-6]
 */
public class NewExtraAdapter extends BaseAdapter {
	/**
	 * 火焰动画帧集合
	 */
	private List<Bitmap> mBurningFrame = null;
	/**
	 * 数据源
	 */
	private ArrayList<BoutiqueApp> mList = null;

	private boolean mIsActive = false;

	private LayoutInflater mInflater = null;
	/**
	 * 默认图标
	 */
	private Bitmap mDefaultBitmap = null;
	/**
	 * 图片管理器
	 */
	private AsyncImageManager mImgManager = null;
	/**
	 * 图标保存路径
	 */
	private String mPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
	/**
	 * 下载任务队列
	 */
	private List<DownloadTask> mDownloadTaskList = null;

	private Context mContext = null;
	/**
	 * 图标有火焰时要图标设置padding
	 */
	private int mBurningPadding = DrawUtils.dip2px(1);

	public NewExtraAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mImgManager = AsyncImageManager.getInstance();
		initBurningFrame();
	}

	/**
	 * 设置列表展现的默认图标
	 */
	public void setDefaultIcon(Drawable drawable) {
		if (drawable != null && drawable instanceof BitmapDrawable) {
			mDefaultBitmap = ((BitmapDrawable) drawable).getBitmap();
		}
	}

	/**
	 * 初始化火焰动画帧
	 */
	private void initBurningFrame() {
		mBurningFrame = new ArrayList<Bitmap>();
		Resources res = mContext.getResources();
		Bitmap bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_1)).getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_2)).getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_3)).getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_4)).getBitmap();
		mBurningFrame.add(bm);
	}

	/** {@inheritDoc} */

	@Override
	public int getCount() {
		if (mList != null) {
			return mList.size();
		} else {
			return 0;
		}
	}

	/** {@inheritDoc} */

	@Override
	public Object getItem(int position) {
		if (mList != null && position >= 0 && position < mList.size()) {
			return mList.get(position);
		} else {
			return null;
		}
	}

	/** {@inheritDoc} */

	@Override
	public long getItemId(int position) {
		return position;
	}

	/** {@inheritDoc} */

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Log.e("XIEDEZHI", "NewExtraAdapter getview position = " + position);
		// TODO:XIEDEZHI getview时不要生成新的对象
		Viewholder viewholder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.appgame_new_extra_item, null);
			viewholder = new Viewholder();
			viewholder.mFeatureIconStub = (ViewStub) convertView
					.findViewById(R.id.new_extra_feature_stub);
			viewholder.mIcon = (BurningIcon) convertView.findViewById(R.id.new_extra_icon);
			viewholder.mIconAnother = (BurningIcon) convertView
					.findViewById(R.id.new_extra_icon_another);
			viewholder.mImageSwitcher = (ImageSwitcher) convertView
					.findViewById(R.id.new_extra_imageswitcher);
			viewholder.mAppName = (TextView) convertView.findViewById(R.id.new_extra_app_name);
			viewholder.mAppSize = (TextView) convertView.findViewById(R.id.new_extra_app_size);
			viewholder.mDownloadLayout = (RelativeLayout) convertView
					.findViewById(R.id.new_extra_download_relativelayout);
			viewholder.mButton = (Button) convertView.findViewById(R.id.new_extra_button);
			viewholder.mIntroductionView = (TextView) convertView
					.findViewById(R.id.new_extra_introduction);
			viewholder.mButtonText = (TextView) convertView
					.findViewById(R.id.new_extra_button_text);
			viewholder.mRatingBar = (RatingBar) convertView.findViewById(R.id.new_extra_rating);
			viewholder.mDownloadCount = (TextView) convertView.findViewById(R.id.new_extra_download_count);
			convertView.setTag(viewholder);
		} else {
			viewholder = (Viewholder) convertView.getTag();
		}
		if (position >= mList.size()) {
			return null;
		}
		final BoutiqueApp app = mList.get(position);
		if (app != null) {
			convertView.setTag(R.id.appgame, app);
			viewholder.mAppName.setText(app.info.name);
			viewholder.mAppSize.setText(app.info.size);
			viewholder.mButton.setVisibility(View.VISIBLE);
			viewholder.mButtonText.setVisibility(View.VISIBLE);
			if (viewholder.mFeatureIcon != null) {
				viewholder.mFeatureIcon.setVisibility(View.GONE);
			}
			viewholder.mIcon.setBurning(false);
			viewholder.mIcon.setPadding(0, 0, 0, 0);
			viewholder.mIconAnother.setBurning(false);
			viewholder.mIconAnother.setPadding(0, 0, 0, 0);
			String typeinfo = app.info.typeinfo;
			//根据不同状态调整UI
			if (typeinfo != null && !typeinfo.equals("")) {
				viewholder.mIntroductionView.setVisibility(View.VISIBLE);
				viewholder.mIntroductionView.setText(typeinfo); // 简介
				LinearLayout.LayoutParams lp = (LayoutParams) viewholder.mIntroductionView
						.getLayoutParams();
				lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
				viewholder.mAppName.setMaxWidth(DrawUtils.dip2px(170));
			} else {
				viewholder.mIntroductionView.setVisibility(View.INVISIBLE);
				LinearLayout.LayoutParams lp = (LayoutParams) viewholder.mIntroductionView
						.getLayoutParams();
				lp.height = DrawUtils.dip2px(8);
				viewholder.mAppName.setMaxWidth(DrawUtils.dip2px(121.6666667f));
			}
			// 版本号显示
			viewholder.mDownloadCount.setText(app.info.dlcs);
//			if (app.info.version != null && !app.info.version.equals("")) {
//				viewholder.mVersion.setText(mContext.getResources().getString(
//						R.string.appgame_version)
//						+ app.info.version);
//			} else {
//				viewholder.mVersion.setText("");
//			}
			float grade = app.info.grade / 2.0f;
			// 星级显示
			viewholder.mRatingBar.setRating(grade);
			boolean isInstall = AppGameInstallingValidator.getInstance().isAppExist(mContext, app.info.packname);
			if (mIsActive) {
				// 判断用户是否已经点击过该应用
				int effect = 0; // 是否显示特效
				if (app.info.effect == 1 && !isInstall
						&& !DownloadUtil.checkViewedEffectApp(mContext, app.info.packname)) {
					effect = 1;
				}
				setIcon(position, viewholder.mImageSwitcher, app.info.icon, mPath, app.picLocalFileName, mDefaultBitmap,
						effect, true);
				if (viewholder.mFeatureIcon != null) {
					viewholder.mFeatureIcon.setImageBitmap(null);
				}
				// 设置特性图标，“必备”，“首发”，“最新”等等
				if (!TextUtils.isEmpty(app.info.ficon)) {
					if (viewholder.mFeatureIcon == null) {
						viewholder.mFeatureIconStub.inflate();
						viewholder.mFeatureIcon = (ImageView) convertView
								.findViewById(R.id.feature_icon);
					}
					if (viewholder.mFeatureIcon != null) {
						viewholder.mFeatureIcon.setVisibility(View.VISIBLE);
						setFeatureIcon(position, viewholder.mFeatureIcon, app.info.ficon, mPath,
								app.localFeatureFileName);
					}
				}
			} else {
				((ImageView) viewholder.mImageSwitcher.getCurrentView())
						.setImageBitmap(mDefaultBitmap);
				if (viewholder.mFeatureIcon != null) {
					viewholder.mFeatureIcon.setImageBitmap(null);
				}
			}
			// 判断按钮的状态，比如“安装",“已安装","下载"
			initAppState(viewholder, app, position, isInstall);
		}
		return convertView;
	}

	private void initAppState(Viewholder viewholder, final BoutiqueApp app, int position,
			boolean isInstall) {
		final int pos = position;
		final String fileName = GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH + app.info.packname
				+ "_" + app.info.version + ".apk";
		viewholder.mButton.setVisibility(View.VISIBLE);
		viewholder.mButtonText.setVisibility(View.VISIBLE);
		// 判断是否正在下载
		if (isDownloading(app.downloadState.state)) {
			viewholder.mButton.setClickable(false);
			viewholder.mButton.setVisibility(View.GONE);
			viewholder.mButtonText.setVisibility(View.VISIBLE);
			viewholder.mDownloadLayout.setClickable(false);
			switch (app.downloadState.state) {
				case DownloadTask.STATE_DOWNLOADING :
				case DownloadTask.STATE_START :
					viewholder.mButtonText.setText(app.downloadState.alreadyDownloadPercent + "%");
					break;
				case DownloadTask.STATE_STOP :
					viewholder.mButtonText.setText(R.string.download_manager_pause);
					break;
				case DownloadTask.STATE_WAIT :
					viewholder.mButtonText.setText(R.string.download_manager_wait);
					break;
			}
		} else if (isInstall) {
			// 已安装
			viewholder.mButton.setBackgroundResource(R.drawable.apps_uninstall_selected_part);
			viewholder.mButtonText.setText(R.string.appgame_installed);
			viewholder.mButton.setClickable(false);
			viewholder.mDownloadLayout.setClickable(false);
		} else if (isApkExist(fileName)) {
			// 存在安装包，显示安装
			// 安装
			viewholder.mButton.setBackgroundResource(R.drawable.appgame_install_selector);
			viewholder.mButtonText.setText(R.string.appgame_install);
			// 安装操作
			viewholder.mButton.setClickable(true);
			//使用R.id.new_extra_button作为ＢoutiqueApp　Tag的标识
			viewholder.mButton.setTag(R.id.new_extra_button, app);
			viewholder.mButton.setOnClickListener(mInstallListener);
			viewholder.mDownloadLayout.setClickable(true);
			viewholder.mDownloadLayout.setTag(R.id.new_extra_button, app);
			viewholder.mDownloadLayout.setOnClickListener(mInstallListener);
		} else { // 可下载
			if (app.info.isfree == 0) { // 免费
				// 下载
				viewholder.mButton.setBackgroundResource(R.drawable.appgame_download_selector);
				viewholder.mButtonText.setText(R.string.appgame_download);
				// 下载操作
				viewholder.mButton.setClickable(true);
				//使用R.id.new_extra_button作为ＢoutiqueApp　Tag的标识
				viewholder.mButton.setTag(R.id.new_extra_button, app);
				//使用R.id.new_extra_download_relativelayout作为 position　Tag的标识　
				viewholder.mButton.setTag(R.id.new_extra_download_relativelayout, pos);
				viewholder.mButton.setOnClickListener(mDownloadClickListener);
				viewholder.mDownloadLayout.setClickable(true);
				viewholder.mDownloadLayout.setTag(R.id.new_extra_button, app);
				viewholder.mDownloadLayout.setTag(R.id.new_extra_download_relativelayout, pos);
				viewholder.mDownloadLayout.setOnClickListener(mDownloadClickListener);
			} else { // 收费
				// 显示价格
				viewholder.mButton.setBackgroundResource(R.drawable.appgame_download_selector);
				viewholder.mButtonText.setText(app.info.price);
				viewholder.mButton.setClickable(true);
				//使用R.id.new_extra_button作为ＢoutiqueApp　Tag的标识
				viewholder.mButton.setTag(R.id.new_extra_button, app);
				//使用R.id.new_extra_download_relativelayout作为 position　Tag的标识　
				viewholder.mButton.setTag(R.id.new_extra_download_relativelayout, pos);
				viewholder.mButton.setOnClickListener(mGotoMarketListener);
				viewholder.mDownloadLayout.setClickable(true);
				viewholder.mDownloadLayout.setTag(R.id.new_extra_button, app);
				viewholder.mDownloadLayout.setTag(R.id.new_extra_download_relativelayout, pos);
				viewholder.mDownloadLayout.setOnClickListener(mGotoMarketListener);
			}
		}
	}

	/**
	 * 安装的按钮点击事件处理
	 */
	private OnClickListener mInstallListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!(v.getTag(R.id.new_extra_button) instanceof BoutiqueApp)) {
				return;
			}
			BoutiqueApp app = (BoutiqueApp) v.getTag(R.id.new_extra_button);
			String fileName = GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH + app.info.packname + "_"
					+ app.info.version + ".apk";
			if (app.info.effect == 1) {
				DownloadUtil.saveViewedEffectApp(mContext, app.info.packname);
			}
			sendBrocastToIntall(fileName, app);
		}
	};

	/**
	 * 免费下载的按钮点击事件处理
	 */
	private WrapOnClickListener mDownloadClickListener = new WrapOnClickListener() {

		@Override
		public void withoutSDCard(View v) {
			if (!(v.getTag(R.id.new_extra_button) instanceof BoutiqueApp)
					&& !(v.getTag(R.id.new_extra_download_relativelayout) instanceof Integer)) {
				return;
			}
			BoutiqueApp app = (BoutiqueApp) v.getTag(R.id.new_extra_button);
			int pos = (Integer) v.getTag(R.id.new_extra_download_relativelayout);
			if (app.info.effect == 1) {
				DownloadUtil.saveViewedEffectApp(mContext, app.info.packname);
			}
			// FTP类型直接下载
			if (app.info.downloadtype == 1) {
				// 直接下载到手机内存
				downloadApk(app, pos + 1, false);
			} else {
				goToMarket(app, pos);
			}
		}

		@Override
		public void withSDCard(View v) {
			if (!(v.getTag(R.id.new_extra_button) instanceof BoutiqueApp)
					&& !(v.getTag(R.id.new_extra_download_relativelayout) instanceof Integer)) {
				return;
			}
			BoutiqueApp app = (BoutiqueApp) v.getTag(R.id.new_extra_button);
			int pos = (Integer) v.getTag(R.id.new_extra_download_relativelayout);
			if (app.info.effect == 1) {
				DownloadUtil.saveViewedEffectApp(mContext, app.info.packname);
			}
			// FTP类型直接下载
			if (app.info.downloadtype == 1) {
				// 直接下载到SD卡
				downloadApk(app, pos + 1, true);
			} else {
				goToMarket(app, pos);
			}
		}
	};

	/**
	 * 跳转到电子市场的按钮点击事件处理
	 */
	private OnClickListener mGotoMarketListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!(v.getTag(R.id.new_extra_button) instanceof BoutiqueApp)
					&& !(v.getTag(R.id.new_extra_download_relativelayout) instanceof Integer)) {
				return;
			}
			BoutiqueApp app = (BoutiqueApp) v.getTag(R.id.new_extra_button);
			int pos = (Integer) v.getTag(R.id.new_extra_download_relativelayout);
			if (app.info.effect == 1) {
				DownloadUtil.saveViewedEffectApp(mContext, app.info.packname);
			}
			goToMarket(app, pos);
		}
	};

	/**
	 * 功能简述:判断是否处于下载服务中 功能详细描述: 注意:
	 * 
	 * @param state
	 * @return
	 */
	private boolean isDownloading(int state) {
		switch (state) {
			case DownloadTask.STATE_DOWNLOADING :
			case DownloadTask.STATE_START :
			case DownloadTask.STATE_STOP :
			case DownloadTask.STATE_WAIT :
				return true;
			default :
				return false;
		}
	}

	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setIcon(final int position, final ImageSwitcher switcher, String imgUrl,
			String imgPath, String imgName, final Bitmap defaultBitmap, final int effect, boolean isMask) {
		final int padding = mBurningPadding;
		if (switcher.getTag() != null && switcher.getTag().equals(imgUrl)) {
			ImageView image = (ImageView) switcher.getCurrentView();
			Drawable drawable = image.getDrawable();
			if (drawable != null && drawable instanceof BitmapDrawable) {
				BitmapDrawable bDrawable = (BitmapDrawable) drawable;
				if (bDrawable.getBitmap() != null && bDrawable.getBitmap() != mDefaultBitmap) {
					if (effect == 1 && image instanceof BurningIcon) {
						((BurningIcon) image).setBurningFrame(mBurningFrame);
						((BurningIcon) image).setBurning(true);
						image.setPadding(padding, padding, padding, padding);
					}
					return;
				}
			}
		}
		switcher.setTag(imgUrl);
		switcher.getCurrentView().clearAnimation();
		switcher.getNextView().clearAnimation();
		Bitmap bm = mImgManager.loadImageForList(position, imgPath, imgName, imgUrl, true, false,
				isMask ? AppGameDrawUtils.getInstance().mMaskIconOperator : null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (switcher.getTag().equals(imgUrl)) {
							Drawable drawable = ((ImageView) switcher
									.getCurrentView()).getDrawable();
							if (drawable instanceof BitmapDrawable) {
								Bitmap bm = ((BitmapDrawable) drawable)
										.getBitmap();
								if (bm == defaultBitmap) {
									switcher.setImageDrawable(new BitmapDrawable(imageBitmap));
								}
								ImageView imageView = (ImageView) switcher.getCurrentView();
								if (effect == 1 && imageView instanceof BurningIcon) {
									((BurningIcon) imageView).setBurningFrame(mBurningFrame);
									((BurningIcon) imageView).setBurning(true);
									imageView.setPadding(padding, padding, padding, padding);
								}
							}
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		ImageView imageView = (ImageView) switcher.getCurrentView();
		if (bm != null && mIsActive) {
			imageView.setImageBitmap(bm);
			if (effect == 1 && imageView instanceof BurningIcon) {
				((BurningIcon) imageView).setBurningFrame(mBurningFrame);
				((BurningIcon) imageView).setBurning(true);
				imageView.setPadding(padding, padding, padding, padding);
			}
		} else {
			imageView.setImageBitmap(defaultBitmap);
		}
	}

	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setFeatureIcon(final int position, final ImageView imageView, String imgUrl,
			String imgPath, String imgName) {
		// TODO:XIEDEZHI 修改接口，不要每次setIcon都要生成一个回调
		imageView.setTag(imgUrl);
		Bitmap bm = mImgManager.loadImageForList(position, imgPath, imgName, imgUrl, true, false,
				null, new AsyncImageLoadedCallBack() {
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
		if (bm != null && mIsActive) {
			imageView.setImageBitmap(bm);
		} else {
			imageView.setImageBitmap(null);
		}
	}

	/**
	 * 功能简述:下载App，并统计 功能详细描述: 注意:
	 * 
	 * @param context
	 * @param app
	 * @param position
	 */
	private void downloadApk(BoutiqueApp app, int position, boolean storeInSd) {
		if (mContext == null || app == null) {
			return;
		}

		// update by zhoujun 如果是木瓜移动的数据，需要回调url
		DownloadUtil.sendCBackUrl(BoutiqueApp.BoutiqueAppInfo.CBACK_URL_FOR_DOWNLOAD,
				app.info.cback, app.info.cbacktype, app.info.cbackurl);

		long id = Long.parseLong(app.info.appid);
		String pkgName = app.info.packname;
		String apkName = pkgName + "_" + app.info.version + ".apk";
		String filePath = Environment.getExternalStorageDirectory() + "/GoStore/download/"
				+ apkName;
		// 判断treatment的值
		if (app.info.treatment > 0) {
			InstallCallbackManager.saveTreatment(app.info.packname, app.info.treatment);
		}
		// 判断是否需要安装成功之后回调
		if (app.info.icbackurl != null && !app.info.icbackurl.equals("")) {
			InstallCallbackManager.saveCallbackUrl(app.info.packname, app.info.icbackurl);
		}
		// 统计代码,位置从1开始计算，所以加1
		AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(mContext, app.info.packname,
				Integer.parseInt(app.info.appid), String.valueOf(app.typeid), 1);
		GoStoreOperatorUtil.downloadFileDirectly(mContext, app.info.name, app.info.downloadurl, id,
				pkgName, new Class[] { AppDownloadListener.class }, apkName,
				DownloadTask.ICON_TYPE_URL, app.info.icon, AppsDetail.START_TYPE_APPRECOMMENDED,
				storeInSd);
	}

	/**
	 * 功能简述:发送广播进行安装，统计安装信息 功能详细描述: 注意:
	 * 
	 * @param context
	 * @param app
	 * @param fileName
	 */
	private void sendBrocastToIntall(String fileName, BoutiqueApp app) {
		if (mContext == null || TextUtils.isEmpty(fileName)) {
			return;
		}
		// 统计安装
		AppRecommendedStatisticsUtil.getInstance().saveReadyToInstall(mContext, app.info.packname,
				app.info.appid, 0, String.valueOf(app.typeid));
		AppsManagementActivity.sendHandler(mContext, IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
				IDiyMsgIds.APPS_MANAGEMENT_INSTALL_APP, 0, fileName, null);
	}

	/**
	 * 功能简述:跳转电子市场，并根据入口值进行位置信息统计 功能详细描述: 注意:
	 * 
	 * @param app
	 * @param pos
	 */
	private void goToMarket(BoutiqueApp app, int pos) {
		if (app != null) {
			// 判断treatment的值
			if (app.info.treatment > 0) {
				InstallCallbackManager.saveTreatment(app.info.packname, app.info.treatment);
			}
			// 判断是否需要安装成功之后回调
			if (app.info.icbackurl != null && !app.info.icbackurl.equals("")) {
				InstallCallbackManager.saveCallbackUrl(app.info.packname, app.info.icbackurl);
			}
			AppsDetail.jumpToDetail(mContext, app, AppsDetail.START_TYPE_APPRECOMMENDED, pos + 1,
					false);
		}
	}

	/**
	 * 功能简述:更新adapter的数据 功能详细描述: 注意:
	 * 
	 * @param list
	 * @param loadImage
	 *            是否加载图片
	 */
	public void updateList(ArrayList<BoutiqueApp> data) {
		if (mList == null) {
			mList = new ArrayList<BoutiqueApp>();
		} else {
			mList.clear();
		}
		if (data != null) {
			Map<String, DownloadTask> map = new HashMap<String, DownloadTask>();
			// 根据mDownloadTaskList初始化应用的下载状态
			if (mDownloadTaskList != null && mDownloadTaskList.size() > 0) {
				for (DownloadTask task : mDownloadTaskList) {
					if (task != null) {
						map.put(String.valueOf(task.getId()), task);
					}
				}
			}
			for (BoutiqueApp app : data) {
				// 初始化应用的图片路径
				String icon = app.info.icon;
				if (!(icon == null || icon.equals(""))) {
					String fileName = String.valueOf(icon.hashCode());
					app.picLocalPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
					app.picLocalFileName = fileName;
				}
				if (!TextUtils.isEmpty(app.info.ficon)) {
					app.localFeatureFileName = String.valueOf(app.info.ficon.hashCode());
				}
				if (map.containsKey(app.info.appid)) {
					DownloadTask task = map.get(app.info.appid);
					app.downloadState.state = task.getState();
					app.downloadState.alreadyDownloadPercent = task.getAlreadyDownloadPercent();
				}
				mList.add(app);
			}
		}
		// notifyDataSetChanged();
	}

	public void onActiveChange(boolean isActive) {
		mIsActive = isActive;
	}

	/**
	 * 功能简述:判断安装包是否存在 功能详细描述: 注意:
	 * 
	 * @param fileName
	 * @return
	 */
	private boolean isApkExist(String fileName) {
		if (!TextUtils.isEmpty(fileName)) {
			File file = new File(fileName);
			return file.exists();
		}
		return false;
	}

	/**
	 * 功能简述: 功能详细描述: 注意:
	 * 
	 * @param taskList
	 */
	public void setDownloadTaskList(List<DownloadTask> taskList) {
		mDownloadTaskList = taskList;
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 * @author liuxinyang
	 * @date [2012-8-14]
	 */
	public class Viewholder {
		public ViewStub mFeatureIconStub;
		public ImageView mFeatureIcon;
		public BurningIcon mIcon;
		public BurningIcon mIconAnother;
		public ImageSwitcher mImageSwitcher;
		public TextView mAppName;
		public TextView mAppSize;
		public RelativeLayout mDownloadLayout;
		public Button mButton;
		public TextView mButtonText;
		public TextView mIntroductionView = null;
		public RatingBar mRatingBar = null;
		public TextView mDownloadCount;
	}
}

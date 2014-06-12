package com.jiubang.ggheart.appgame.base.component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
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
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-9-12]
 */
public class SearchResultAdapter extends BaseAdapter {

	private Context mContext = null;

	private AsyncImageManager mImgManager = null;

	private LayoutInflater mLayoutInflater = null;

	private List<BoutiqueApp> mSearchList = new ArrayList<BoutiqueApp>();

	private String mPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;

	private String mSearchText = "";

	private String mSearchId = "";

	private Bitmap mDefaultBitmap;

	public SearchResultAdapter(Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		mImgManager = AsyncImageManager.getInstance();
		mDefaultBitmap = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.default_icon)).getBitmap();
	}

	/**
	 * 更新数据列表并调用notifyDatasetChange
	 */
	public void update(List<BoutiqueApp> list) {
		mSearchList.clear();
		if (list != null) {
			for (BoutiqueApp app : list) {
				mSearchList.add(app);
			}
		}
		notifyDataSetChanged();
	}

	public void setSearchText(String text) {
		mSearchText = text;
	}

	public void setSearchId(String id) {
		mSearchId = id;
	}

	@Override
	public int getCount() {
		if (mSearchList != null) {
			return mSearchList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		try {
			return mSearchList.get(position);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Viewholder viewholder = null;
		if (convertView == null) {
			convertView = mLayoutInflater
					.inflate(R.layout.apps_management_search_result_item, null);
			viewholder = new Viewholder();
			viewholder.mIcon = (ImageView) convertView.findViewById(R.id.search_icon);
			viewholder.mIconAnother = (ImageView) convertView
					.findViewById(R.id.search_icon_another);
			viewholder.mImageSwitcher = (ImageSwitcher) convertView
					.findViewById(R.id.search_switcher);
			viewholder.mAppName = (TextView) convertView.findViewById(R.id.search_app_name);
			viewholder.mAppSize = (TextView) convertView.findViewById(R.id.search_app_size);
			viewholder.mButton = (Button) convertView.findViewById(R.id.search_button);
			viewholder.mButtonText = (TextView) convertView.findViewById(R.id.search_button_text);
			viewholder.mRatingBar = (RatingBar) convertView.findViewById(R.id.search_rating);
			viewholder.mDownloadCount = (TextView) convertView.findViewById(R.id.search_download_count);
			viewholder.mTypeInfo = (TextView) convertView.findViewById(R.id.search_typeinfo);
			viewholder.mDownloadLayout = (RelativeLayout) convertView
					.findViewById(R.id.search_download_relativelayout);
			convertView.setTag(viewholder);
		} else {
			viewholder = (Viewholder) convertView.getTag();
		}
		if (position >= mSearchList.size()) {
			return null;
		}
		final BoutiqueApp app = mSearchList.get(position);
		if (app != null && app.info != null) {
			viewholder.mAppName.setText(app.info.name);
			viewholder.mAppSize.setText(app.info.size);
			viewholder.mButton.setVisibility(View.VISIBLE);
			viewholder.mButtonText.setVisibility(View.VISIBLE);
			viewholder.mTypeInfo.setText(app.info.typeinfo);
			viewholder.mDownloadCount.setText(app.info.dlcs);
			// 版本号显示
			//			if (appinfo.mVersion == null || appinfo.mVersion.equals("")) {
			//				viewholder.mVersion.setText(" ");
			//			} else {
			//				viewholder.mVersion.setText(mContext.getResources().getString(
			//						R.string.appgame_version)
			//						+ appinfo.mVersion);
			//			}
			float grade = app.info.grade / 2.0f;
			// 星级显示
			viewholder.mRatingBar.setRating(grade);
			initButtonState(viewholder, app, position);
			// 设置图标
			if (!TextUtils.isEmpty(app.info.icon)) {
				setIcon(position, viewholder.mImageSwitcher, app.info.icon, mPath,
						String.valueOf(app.info.icon.hashCode()));
			}
			convertView.setId(Integer.valueOf(app.info.appid));
		}
		return convertView;
	}

	private void initButtonState(Viewholder viewholder, final BoutiqueApp app, int position) {
		final int pos = position;
		final String fileName = GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH + app.info.packname
				+ "_" + app.info.version + ".apk";
		viewholder.mButton.setVisibility(View.VISIBLE);
		viewholder.mButtonText.setVisibility(View.VISIBLE);
		if (isDownloading(app.downloadState.state)) {
			viewholder.mButton.setVisibility(View.GONE);
			viewholder.mDownloadLayout.setOnClickListener(null);
			switch (app.downloadState.state) {
				case DownloadTask.STATE_DOWNLOADING :
				case DownloadTask.STATE_START :
					viewholder.mButtonText.setText(app.downloadState.alreadyDownloadPercent
							+ "%");
					break;
				case DownloadTask.STATE_STOP :
					viewholder.mButtonText.setText(R.string.download_manager_pause);
					break;
				case DownloadTask.STATE_WAIT :
					viewholder.mButtonText.setText(R.string.download_manager_wait);
					break;
			}
		} else if (AppGameInstallingValidator.getInstance().isAppExist(mContext, app.info.packname)) {
			// 已安装
			viewholder.mButton.setBackgroundResource(R.drawable.apps_uninstall_selected_part);
			viewholder.mButtonText.setText(R.string.appgame_installed);
			viewholder.mButton.setClickable(false);
			viewholder.mDownloadLayout.setOnClickListener(null);
		} else if (isApkExist(fileName)) {
			// 存在安装包，显示安装
			// 安装
			viewholder.mButton.setBackgroundResource(R.drawable.appgame_install_selector);
			viewholder.mButtonText.setText(R.string.appgame_install);
			// 安装操作
			viewholder.mButton.setClickable(true);
			// R.id.search_button作为KEY，存储BoutiqueApp
			viewholder.mButton.setTag(R.id.search_button, app);
			viewholder.mDownloadLayout.setTag(R.id.search_button, app);
			// search_button_text作为KEY，存储fileName
			viewholder.mDownloadLayout.setTag(R.id.search_button_text, fileName);
			viewholder.mButton.setTag(R.id.search_button_text, fileName);
			// 设置点击方法
			viewholder.mButton.setOnClickListener(mInstallListener);
			viewholder.mDownloadLayout.setOnClickListener(mInstallListener);
		} else { // 可下载
			if (app.info.isfree == 0) { // 免费
				// 下载
				viewholder.mButton.setBackgroundResource(R.drawable.appgame_download_selector);
				viewholder.mButtonText.setText(R.string.appgame_download);
				// 下载操作
				viewholder.mButton.setClickable(true);
				// R.id.search_button作为KEY，存储BoutiqueApp
				viewholder.mButton.setTag(R.id.search_button, app);
				viewholder.mDownloadLayout.setTag(R.id.search_button, app);
				// R.id.search_button_text作为KEY，存储位置pos
				viewholder.mButton.setTag(R.id.search_button_text, pos);
				viewholder.mDownloadLayout.setTag(R.id.search_button_text, pos);
				// 设置点击方法
				viewholder.mButton.setOnClickListener(mFtpDownloadListener);
				viewholder.mDownloadLayout.setOnClickListener(mFtpDownloadListener);
			} else { // 收费
				// 显示价格
				viewholder.mButton.setBackgroundResource(R.drawable.appgame_download_selector);
				viewholder.mButtonText.setText(app.info.price);
				viewholder.mButton.setClickable(true);
				// R.id.search_button作为KEY，存储BoutiqueApp
				viewholder.mButton.setTag(R.id.search_button, app);
				viewholder.mDownloadLayout.setTag(R.id.search_button, app);
				// R.id.search_button_text作为KEY，存储位置pos
				viewholder.mButton.setTag(R.id.search_button_text, pos);
				viewholder.mDownloadLayout.setTag(R.id.search_button_text, pos);
				// 设置点击方法
				viewholder.mButton.setOnClickListener(mGoToMarketListener);
				viewholder.mDownloadLayout.setOnClickListener(mGoToMarketListener);
			}
		}
	}

	/**
	 * 点击之后发广播进行安装
	 */
	private OnClickListener mInstallListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object obj1 = v.getTag(R.id.search_button);
			Object obj2 = v.getTag(R.id.search_button_text);
			if (obj1 == null || !(obj1 instanceof BoutiqueApp) || obj2 == null
					|| !(obj2 instanceof String)) {
				return;
			}
			BoutiqueApp app = (BoutiqueApp) obj1;
			String fileName = (String) obj2;
			if (app != null) {
				sendBrocastToIntall(mContext, app, fileName);
			}
		}
	};
	
	/**
	 * FTP直接下载，区分有SD卡与无SD卡的情况
	 */
	private WrapOnClickListener mFtpDownloadListener = new WrapOnClickListener() {
		@Override
		public void withoutSDCard(View v) {
			Object obj1 = v.getTag(R.id.search_button);
			Object obj2 = v.getTag(R.id.search_button_text);
			if (obj1 == null || !(obj1 instanceof BoutiqueApp) || obj2 == null
					|| !(obj2 instanceof Integer)) {
				return;
			}
			BoutiqueApp app = (BoutiqueApp) obj1;
			int pos = (Integer) obj2;
			if (app.info.downloadtype == 1) {
				downloadApk(mContext, app, pos + 1, false);
			} else {
				// 判断treatment的值
				if (app.info.treatment > 0) {
					InstallCallbackManager.saveTreatment(app.info.packname, app.info.treatment);
				}
				// 判断是否需要安装成功之后回调
				if (app.info.icbackurl != null && !app.info.icbackurl.equals("")) {
					InstallCallbackManager.saveCallbackUrl(app.info.packname, app.info.icbackurl);
				}
				StatisticsData.saveSearchKeywordStat(mContext, StatisticsData.SEARCH_ID_APPS,
						mSearchText, true);
				AppsDetail.jumpToDetail(mContext, app, AppsDetail.START_TYPE_APPRECOMMENDED,
						pos + 1, false);
			}
		}

		@Override
		public void withSDCard(View v) {
			Object obj1 = v.getTag(R.id.search_button);
			Object obj2 = v.getTag(R.id.search_button_text);
			if (obj1 == null || !(obj1 instanceof BoutiqueApp) || obj2 == null
					|| !(obj2 instanceof Integer)) {
				return;
			}
			BoutiqueApp app = (BoutiqueApp) obj1;
			int pos = (Integer) obj2;
			// FTP类型直接下载
			if (app.info.downloadtype == 1) {
				downloadApk(mContext, app, pos + 1, true);
			} else {
				// 判断treatment的值
				if (app.info.treatment > 0) {
					InstallCallbackManager.saveTreatment(app.info.packname, app.info.treatment);
				}
				// 判断是否需要安装成功之后回调
				if (app.info.icbackurl != null && !app.info.icbackurl.equals("")) {
					InstallCallbackManager.saveCallbackUrl(app.info.packname, app.info.icbackurl);
				}
				StatisticsData.saveSearchKeywordStat(mContext, StatisticsData.SEARCH_ID_APPS,
						mSearchText, true);
				AppsDetail.jumpToDetail(mContext, app, AppsDetail.START_TYPE_APPRECOMMENDED,
						pos + 1, false);
			}
		}
	};
	
	/**
	 * 收费的项，进入电子市场
	 */
	private OnClickListener mGoToMarketListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			BoutiqueApp app = (BoutiqueApp) v.getTag(R.id.search_button);
			int pos = (Integer) v.getTag(R.id.search_button_text);
			// 判断treatment的值
			if (app.info.treatment > 0) {
				InstallCallbackManager.saveTreatment(app.info.packname,
						app.info.treatment);
			}
			// 判断是否需要安装成功之后回调
			if (app.info.icbackurl != null && !app.info.icbackurl.equals("")) {
				InstallCallbackManager.saveCallbackUrl(app.info.packname,
						app.info.icbackurl);
			}
			StatisticsData.saveSearchKeywordStat(mContext,
					StatisticsData.SEARCH_ID_APPS, mSearchText, true);
			AppsDetail.jumpToDetail(mContext, app,
					AppsDetail.START_TYPE_APPRECOMMENDED, pos + 1, false);
		}
	};
	
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
			String imgPath, String imgName) {
		if (switcher.getTag() != null && switcher.getTag().equals(imgUrl)) {
			return;
		}
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
		switcher.getCurrentView().clearAnimation();
		switcher.getNextView().clearAnimation();
		Bitmap bm = mImgManager.loadImageForList(position, imgPath, imgName, imgUrl, true, false,
				AppGameDrawUtils.getInstance().mMaskIconOperator, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (switcher.getTag().equals(imgUrl)) {
							Drawable drawable = ((ImageView) switcher.getCurrentView())
									.getDrawable();
							if (drawable instanceof BitmapDrawable) {
								Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
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
	 * 功能简述:发送广播进行安装，统计安装信息 功能详细描述: 注意:
	 * 
	 * @param context
	 * @param app
	 * @param fileName
	 */
	private void sendBrocastToIntall(Context context, BoutiqueApp app, String fileName) {
		if (context == null || TextUtils.isEmpty(fileName) || app == null) {
			return;
		}
		AppsManagementActivity.sendHandler(context, IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
				IDiyMsgIds.APPS_MANAGEMENT_INSTALL_APP, 0, fileName, null);
	}

	/**
	 * 功能简述: 功能详细描述: 注意:
	 * 
	 * @param context
	 * @param app
	 */
	private void downloadApk(Context context, BoutiqueApp app, int position, boolean storeInSd) {
		if (context == null || app == null) {
			return;
		}

		// update by zhoujun 如果是木瓜移动的数据，需要回调url
		DownloadUtil.sendCBackUrl(BoutiqueApp.BoutiqueAppInfo.CBACK_URL_FOR_DOWNLOAD,
				app.info.cback, app.info.cbacktype, app.info.cbackurl);

		long id = Long.parseLong(app.info.appid);
		String pkgName = app.info.packname;
		String apkName = pkgName + "_" + app.info.version + ".apk";
		// 判断treatment的值
		if (app.info.treatment > 0) {
			InstallCallbackManager.saveTreatment(app.info.packname, app.info.treatment);
		}
		// 判断是否需要安装成功之后回调
		if (app.info.icbackurl != null && !app.info.icbackurl.equals("")) {
			InstallCallbackManager.saveCallbackUrl(app.info.packname, app.info.icbackurl);
		}
		StatisticsData.saveSearchKeywordStat(mContext, StatisticsData.SEARCH_ID_APPS, mSearchText,
				true);
		AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(mContext, app.info.packname,
				Integer.parseInt(app.info.appid), mSearchId, 1);
		GoStoreOperatorUtil.downloadFileDirectly(context, app.info.name, app.info.downloadurl, id, pkgName,
				new Class[] { AppDownloadListener.class }, apkName, DownloadTask.ICON_TYPE_URL,
				app.info.icon, AppsDetail.START_TYPE_APPRECOMMENDED, storeInSd);
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 * @author liuxinyang
	 * @date [2012-9-12]
	 */
	private class Viewholder {
		public ImageView mIcon;
		public ImageView mIconAnother;
		public ImageSwitcher mImageSwitcher;
		public TextView mAppName;
		public TextView mAppSize;
		public TextView mTypeInfo;
		public Button mButton;
		public TextView mButtonText;
		public RelativeLayout mDownloadLayout;
		public RatingBar mRatingBar = null;
		public TextView mDownloadCount;
	}

}

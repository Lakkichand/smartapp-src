package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.base.net.InstallCallbackManager;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.base.utils.ButtonUtils;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 精品推荐adapter
 * 
 * @author xiedezhi
 * 
 */
public class FeatureAdapter extends BaseAdapter {
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	/**
	 * 火焰动画帧集合
	 */
	private List<Bitmap> mBurningFrame = null;
	/**
	 * 该adapter对应的精品推荐页是否在激活状态
	 */
	private boolean mIsActive;
	/**
	 * 数据源，与container的数据不是同一个对象
	 */
	private List<BoutiqueApp> mDataSource = new ArrayList<BoutiqueApp>();
	/**
	 * 进入应用游戏中心时的DownloadManager里的下载任务列表，每次更新数据时都根据列表初始化应用的下载信息
	 */
	private List<DownloadTask> mDownloadTaskList = null;
	/**
	 * 下载按钮点击监听器
	 */
	private OnClickListener mDownloadClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Object tag = v
					.getTag(R.id.feature_iphonestyle_twocell_download_btn);
			if (tag != null && (tag instanceof BoutiqueApp)) {
				BoutiqueApp app = (BoutiqueApp) tag;
				if (app.info.effect == 1) {
					DownloadUtil.saveViewedEffectApp(mContext,
							app.info.packname);
				}
				if (app.acttype == BoutiqueApp.FEATURE_ACTTYPE_FTP) {
					// 统计
					// 应用中心统计
					AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(mContext,
							app.info.packname, Integer.parseInt(app.info.appid),
							String.valueOf(app.typeid), 1);
					downloadApk(mContext, app);
				} else if (app.acttype == BoutiqueApp.FEATURE_ACTTYPE_MARKET
						|| app.acttype == BoutiqueApp.FEATURE_ACTTYPE_BROWSER) {
					int pertain = AppsDetail.START_TYPE_APPRECOMMENDED;
					AppsDetail.jumpToDetail(v.getContext(), app, pertain,
							app.index, false);
				}
			} else {
				Log.e("FeatureAdapter", "mDownloadClickListener tag = " + tag);
			}
		}
	};
	/**
	 * 安装按钮点击监听器
	 */
	private OnClickListener mInstallClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Object tag1 = v
					.getTag(R.id.feature_iphonestyle_twocell_download_btn);
			Object tag2 = v.getTag(R.id.feature_iphonestyle_twocell_progress);
			if ((tag1 != null) && (tag1 instanceof BoutiqueApp)
					&& (tag2 != null) && (tag2 instanceof String)) {
				BoutiqueApp app = (BoutiqueApp) tag1;
				if (app.info.effect == 1) {
					DownloadUtil.saveViewedEffectApp(mContext,
							app.info.packname);
				}
				String fileName = (String) tag2;
				FeatureController.sendMsgToIntall(mContext, app,
						fileName);
			} else {
				Log.e("FeatureAdapter", "mInstallClickListener tag1 = " + tag1
						+ "  tag2 = " + tag2);
			}
		}
	};
	/**
	 * 默认图标
	 */
	private Drawable mDefaultIcon = null;

	private AsyncImageManager mImgManager = null;

	public FeatureAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mImgManager = AsyncImageManager.getInstance();
		initBurningFrame();
	}
	
	/**
	 * 设置列表展现的默认图标
	 */
	public void setDefaultIcon(Drawable drawable) {
		mDefaultIcon = drawable;
	}

	/**
	 * 初始化火焰动画帧
	 */
	private void initBurningFrame() {
		mBurningFrame = new ArrayList<Bitmap>();
		Resources res = mContext.getResources();
		Bitmap bm = ((BitmapDrawable) res
				.getDrawable(R.drawable.appgame_fire_1)).getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_2))
				.getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_3))
				.getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_4))
				.getBitmap();
		mBurningFrame.add(bm);
	}

	@Override
	public int getCount() {
		if (mDataSource == null) {
			return 0;
		}
		return mDataSource.size();
	}

	@Override
	public Object getItem(int position) {
		try {
			return mDataSource.get(position);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
//		Log.e("XIEDEZHI", "FeatureAdapter getview position = " + position);
		// TODO:XIEDEZHI getview是不要生成对象
		if (position < 0 || position >= mDataSource.size()) {
			return convertView;
		}
		FeatureViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.apps_mgr_feature_iphonestyle_twocell, null);
			holder = new FeatureViewHolder();
			holder.mIcon = (BurningIcon) convertView
					.findViewById(R.id.feature_iphonestyle_twocell_icon);
			holder.mName = (TextView) convertView
					.findViewById(R.id.feature_iphonestyle_twocell_name);
			holder.mSize = (TextView) convertView
					.findViewById(R.id.feature_iphonestyle_twocell_size);
			holder.mIntroductionView = (TextView) convertView
					.findViewById(R.id.feature_iphonestyle_twocell_introduction);
			holder.mProgressView = (TextView) convertView
					.findViewById(R.id.feature_iphonestyle_twocell_progress);
			holder.mDownloadBtn = (Button) convertView
					.findViewById(R.id.feature_iphonestyle_twocell_download_btn);
			holder.mFeatureIcon = (ImageView) convertView
					.findViewById(R.id.feature_iphonestyle_twocell_feature_icon);
			convertView.setTag(holder);
		} else {
			holder = (FeatureViewHolder) convertView.getTag();
		}
		BoutiqueApp app = mDataSource.get(position);
		if (app == null) {
			return convertView;
		}
		convertView.setTag(R.id.appgame, app);
		holder.mIcon.setBurning(false);
		holder.mIcon.setPadding(0, 0, 0, 0);
		boolean isInstall = RecommAppsUtils.isInstalled(mContext,
				app.info.packname, null);
		if (mIsActive) {
			// 如果在激活状态，加载图标
			// 判断用户是否已经点击过该应用
			int effect = 0; // 是否显示特效
			if (app.info.effect == 1
					&& !isInstall
					&& !DownloadUtil.checkViewedEffectApp(mContext,
							app.info.packname)) {
				effect = 1;
			}
			setIcon(holder.mIcon, app.info.icon, app.picLocalPath, app.picLocalFileName,
					mDefaultIcon, effect, true);
			// 设置特性图标，“必备”，“首发”，“最新”等等
			if (!TextUtils.isEmpty(app.info.ficon)) {
				holder.mFeatureIcon.setVisibility(View.VISIBLE);
				setIcon(holder.mFeatureIcon, app.info.ficon, app.picLocalPath,
						app.localFeatureFileName, null, 0, false);
			} else {
				holder.mFeatureIcon.setImageDrawable(null);
			}
		} else {
			holder.mIcon.setImageDrawable(mDefaultIcon);
			holder.mFeatureIcon.setImageDrawable(null);
		}
		holder.mName.setText(app.info.name); // 名字
		holder.mSize.setText(app.info.size); // 大小
		String typeinfo = app.info.typeinfo;
		if (typeinfo != null && !typeinfo.equals("")) {
			holder.mIntroductionView.setVisibility(View.VISIBLE);
			holder.mIntroductionView.setText(typeinfo); // 简介
		} else {
			holder.mIntroductionView.setVisibility(View.GONE);
		}
		initDownloadState(holder, app, isInstall);
		convertView.setFocusable(false);
		return convertView;
	}

	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setIcon(final ImageView imageView, String imgUrl, String imgPath, String imgName,
			Drawable defaultIcon, final int effect, boolean isMask) {
		// TODO:XIEDEZHI 修改接口，不要每次setIcon都要生成一个回调
		imageView.setTag(imgUrl);
		final int padding = DrawUtils.dip2px(1);
		Bitmap bm = mImgManager.loadImage(imgPath, imgName, imgUrl, true,
				false,
				isMask ? AppGameDrawUtils.getInstance().mMaskIconOperator
						: null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageView.getTag().equals(imgUrl) && mIsActive) {
							imageView.setImageBitmap(imageBitmap);
							if (effect == 1 && imageView instanceof BurningIcon) {
								((BurningIcon) imageView)
										.setBurningFrame(mBurningFrame);
								((BurningIcon) imageView).setBurning(true);
								imageView.setPadding(padding, padding, padding,
										padding);
							}
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		if (bm != null && mIsActive) {
			imageView.setImageBitmap(bm);
			if (effect == 1 && imageView instanceof BurningIcon) {
				((BurningIcon) imageView).setBurningFrame(mBurningFrame);
				((BurningIcon) imageView).setBurning(true);
				imageView.setPadding(padding, padding, padding, padding);
			}
		} else {
			imageView.setImageDrawable(defaultIcon);
		}
	}

	/**
	 * 根据应用信息初始化view的下载状态
	 */
	private void initDownloadState(FeatureViewHolder holder, BoutiqueApp app,
			boolean isInstall) {
		String pkgName = app.info.packname;
		if (pkgName == null || pkgName.equals("")) {
			return;
		}
		ButtonUtils.setButtonTextSize(holder.mDownloadBtn);
		String fileName = GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH + pkgName
				+ "_" + app.info.version + ".apk";
		if (isInstall) {
			// 已安装
			holder.mDownloadBtn.setOnClickListener(null);
			holder.mDownloadBtn.setClickable(false);
			holder.mDownloadBtn.setVisibility(View.VISIBLE);
			holder.mDownloadBtn.setText(R.string.themestore_already_install);
			holder.mDownloadBtn.setBackgroundDrawable(null);
			holder.mProgressView.setVisibility(View.GONE);
		} else if (FileUtil.isFileExist(fileName)) {
			// 如果APK已经下载完成
			holder.mProgressView.setVisibility(View.GONE);
			holder.mDownloadBtn.setVisibility(View.VISIBLE);
			holder.mDownloadBtn.setText(R.string.gostore_detail_install);
			holder.mDownloadBtn.setTag(
					R.id.feature_iphonestyle_twocell_download_btn, app);
			holder.mDownloadBtn.setTag(
					R.id.feature_iphonestyle_twocell_progress, fileName);
			holder.mDownloadBtn.setClickable(true);
			holder.mDownloadBtn.setOnClickListener(mInstallClickListener);
			holder.mDownloadBtn.setBackgroundResource(R.drawable.appgame_install_btn_selector);
		} else if (hasDownloadState(app)) {
			// 已经开始了下载
			int state = app.downloadState.state;
			String progress = "";
			if (state == DownloadTask.STATE_WAIT
					|| state == DownloadTask.STATE_START
					|| state == DownloadTask.STATE_RESTART) {
				// 等待下载
				progress = mContext.getString(R.string.download_manager_wait);
			} else if (state == DownloadTask.STATE_DOWNLOADING) {
				progress = app.downloadState.alreadyDownloadPercent + "%";
			} else if (state == DownloadTask.STATE_STOP) {
				progress = mContext.getString(R.string.download_manager_pause);
			}
			holder.mProgressView.setVisibility(View.VISIBLE);
			holder.mProgressView.setText(progress);
			holder.mDownloadBtn.setClickable(true);
			holder.mDownloadBtn.setVisibility(View.GONE);
		} else {
			// 显示下载按钮，点击跳转详情或者电子市场
			holder.mProgressView.setVisibility(View.GONE);
			holder.mDownloadBtn.setVisibility(View.VISIBLE);
			if (app.info.isfree == 0) {
				// 免费
				holder.mDownloadBtn.setText(R.string.appgame_download);
			} else {
				// 收费
				holder.mDownloadBtn.setText(app.info.price);
			}
			holder.mDownloadBtn.setTag(
					R.id.feature_iphonestyle_twocell_download_btn, app);
			holder.mDownloadBtn.setClickable(true);
			holder.mDownloadBtn.setOnClickListener(mDownloadClickListener);
			holder.mDownloadBtn.setBackgroundResource(R.drawable.appgame_install_btn_selector);
		}
	}

	/**
	 * 判断应用是否正在下载
	 * 
	 * @return 正在下载返回true，否则返回false
	 */
	private boolean hasDownloadState(BoutiqueApp app) {
		int state = app.downloadState.state;
		switch (state) {
		case DownloadTask.STATE_WAIT:
		case DownloadTask.STATE_START:
		case DownloadTask.STATE_DOWNLOADING:
		case DownloadTask.STATE_STOP:
		case DownloadTask.STATE_RESTART:
			return true;
		case DownloadTask.STATE_NEW:
		case DownloadTask.STATE_FAIL:
		case DownloadTask.STATE_FINISH:
		case DownloadTask.STATE_DELETE:
			return false;
		default:
			return false;
		}
	}

	/**
	 * 更新adapter数据源，并调用notifyDataSetChanged
	 * 
	 * @param data
	 *            新数据
	 */
	public void update(List<BoutiqueApp> data) {
		mDataSource.clear();
		if (data != null) {
			// 根据mDownloadTaskList初始化应用的下载状态
			if (mDownloadTaskList != null) {
				for (DownloadTask task : mDownloadTaskList) {
					for (BoutiqueApp app : data) {
						if (app.info.appid.equals(task.getId() + "")) {
							app.downloadState.state = task.getState();
							app.downloadState.alreadyDownloadPercent = task
									.getAlreadyDownloadPercent();
							break;
						}
					}
				}
			}
			for (BoutiqueApp app : data) {
				// 初始化应用的图片路径
				String icon = app.info.icon;
				if (!TextUtils.isEmpty(icon)) {
					String fileName = String.valueOf(icon.hashCode());
					app.picLocalPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
					app.picLocalFileName = fileName;
				}
				if (!TextUtils.isEmpty(app.info.ficon)) {
					app.localFeatureFileName = String.valueOf(app.info.ficon.hashCode());
				}
				mDataSource.add(app);
			}
		}
		// notifyDataSetChanged();
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
	 * 通知adapter有应用状态更新
	 * 
	 * @param packName
	 *            安装/卸载/更新的包名
	 * 
	 * @param appAction
	 *            代表应用的操作码，详情看{@link MainViewGroup}
	 */
	public void onAppAction(String packName, int appAction) {
		if (mDataSource == null) {
			return;
		}
		// 如果列表页在激活状态，并且系统更改的应用在应用列表中，则调用notifyDataSetChanged重新getview
		if (mIsActive) {
			for (BoutiqueApp app : mDataSource) {
				if (packName.equals(app.info.packname)) {
					notifyDataSetChanged();
				}
			}
		}
	}

	/**
	 * 发广播下载apk
	 */
	private void downloadApk(Context context, BoutiqueApp app) {
		if (context == null || app == null || app.info == null) {
			return;
		}

		// update by zhoujun 如果是木瓜移动的数据，需要回调url
		DownloadUtil.sendCBackUrl(BoutiqueApp.BoutiqueAppInfo.CBACK_URL_FOR_DOWNLOAD,
				app.info.cback, app.info.cbacktype, app.info.cbackurl);

		long id = Long.parseLong(app.info.appid);
		String pkgName = app.info.packname;
		String apkName = pkgName + "_" + app.info.version + ".apk";
		int pertain = AppsDetail.START_TYPE_APPRECOMMENDED;
		// 判断treatment的值
		if (app.info.treatment > 0) {
			InstallCallbackManager.saveTreatment(app.info.packname, app.info.treatment);
		}
		// 判断是否需要安装成功之后回调
		if (app.info.icbackurl != null && !app.info.icbackurl.equals("")) {
			InstallCallbackManager.saveCallbackUrl(app.info.packname, app.info.icbackurl);
		}
		GoStoreOperatorUtil.downloadFileDirectly(context, app.info.name, app.info.downloadurl, id,
				pkgName, new Class[] { AppDownloadListener.class }, apkName,
				DownloadTask.ICON_TYPE_URL, app.info.icon, pertain);
	}

	/**
	 * 设置downloadTask列表
	 */
	public void setDownloadTaskList(List<DownloadTask> downloadList) {
		mDownloadTaskList = downloadList;
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 */
	public class FeatureViewHolder {
		public BurningIcon mIcon = null; // 图标
		public TextView mName = null; // 名字
		public TextView mSize = null; // 大小
		public TextView mIntroductionView = null; // 简介
		public TextView mProgressView = null; // 下载进度
		public Button mDownloadBtn = null; // 下载按钮
		public ImageView mFeatureIcon = null; // 特性图标
	}
}

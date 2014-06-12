/*
 * 文 件 名:  EditorFavoriteAdapter.java
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
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.base.net.InstallCallbackManager;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.base.utils.AppGameInstallingValidator;
import com.jiubang.ggheart.appgame.base.utils.ButtonUtils;
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
public class EditorFavoriteAdapter extends BaseAdapter {

	/**
	 * 编辑推荐的所有app列表
	 */
	private ArrayList<BoutiqueApp> mList = null;

	private boolean mIsActive = false;

	private LayoutInflater mInflater = null;

	private AsyncImageManager mImgManager = null;

	private String mPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;

	private Context mContext = null;

	/**
	 * 列表展示的默认banner图
	 */
	private Drawable mDefaultBanner = null;

	/**
	 * 正在下载的任务列表
	 */
	private List<DownloadTask> mDownloadTaskList = new ArrayList<DownloadTask>();

	public EditorFavoriteAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mImgManager = AsyncImageManager.getInstance();
	}

	/** {@inheritDoc} */

	@Override
	public int getCount() {
		if (mList != null) {
			return mList.size();
		}
		return 0;
	}

	/** {@inheritDoc} */

	@Override
	public Object getItem(int position) {
		if (mList != null && position >= 0 && position < mList.size()) {
			return mList.get(position);
		}
		return null;
	}

	/** {@inheritDoc} */

	@Override
	public long getItemId(int position) {
		return position;
	}

	/** {@inheritDoc} */

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//		Log.e("XIEDEZHI", "EditorFavorieAdapter getview position = " + position);
		Viewholder viewholder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.appgame_editor_favorite_item, null);
			viewholder = new Viewholder();
			viewholder.mImg = (ImageView) convertView.findViewById(R.id.editor_favorite_img);
			viewholder.mImgAnother = (ImageView) convertView
					.findViewById(R.id.editor_favorite_img_another);
			viewholder.mImageSwitcher = (ImageSwitcher) convertView
					.findViewById(R.id.editor_favorite_imageswitcher);
			viewholder.mName = (TextView) convertView.findViewById(R.id.editor_favorite_name);
			viewholder.mButton = (Button) convertView.findViewById(R.id.editor_favorite_button);
			viewholder.mProgress = (TextView) convertView
					.findViewById(R.id.editor_favorite_prgress);
			viewholder.mSize = (TextView) convertView.findViewById(R.id.editor_favorite_size);
			viewholder.mSummary = (TextView) convertView.findViewById(R.id.editor_favorite_summary);
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
			if (mIsActive) {
				setIcon(position, viewholder.mImageSwitcher, app.pic, app.picLocalFileName);
			} else {
				ImageView imageView = (ImageView) viewholder.mImageSwitcher.getCurrentView();
				setDefaultBanner(imageView);
			}
			// UI的特殊要求，根据语言调整button的文字大小
			ButtonUtils.setButtonTextSize(viewholder.mButton);
			viewholder.mButton.setVisibility(View.VISIBLE);
			viewholder.mProgress.setVisibility(View.GONE);
			final int pos = position;
			// 专题
			if (app.type == 1) {
				viewholder.mButton.setText(R.string.appgame_editor_favorite_enter);
				viewholder.mName.setText(app.typeInfo.name);
				viewholder.mSummary.setText(app.typeInfo.summary);
				// 专题的应用个数
				viewholder.mSize.setVisibility(View.INVISIBLE);
				// button的跳转
				viewholder.mButton.setBackgroundResource(R.drawable.appgame_download_btn_selector);
				viewholder.mButton.setTextColor(Color.WHITE);
				viewholder.mButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						TabController.skipToTheNextTab(app.typeInfo.typeid, app.typeInfo.name, -1,
								true, -1, -1, null);
					}
				});
			} else { // 应用
				viewholder.mName.setText(app.info.name);
				viewholder.mSummary.setText(app.info.summary);
				viewholder.mSize.setText(app.info.size);
				viewholder.mSize.setVisibility(View.VISIBLE);
				initAppState(viewholder, app, position);
			}
		}
		return convertView;
	}

	/**
	 * 功能简述:设置图标 功能详细描述: 注意:
	 * 
	 * @param imgView
	 * @param url
	 * @param fileName
	 * @param viewGroup
	 */
	protected void setIcon(final int position, final ImageSwitcher switcher, String url,
			String fileName) {
		if (mImgManager == null) {
			mImgManager = AsyncImageManager.getInstance();
		}
		if (switcher.getTag() != null && switcher.getTag().equals(url)) {
			ImageView image = (ImageView) switcher.getCurrentView();
			Drawable drawable = image.getBackground();
			if (drawable == null) {
				return;
			}
		}
		switcher.setTag(url);
		switcher.getCurrentView().clearAnimation();
		switcher.getNextView().clearAnimation();
		Bitmap bm = mImgManager.loadImageForList(position, mPath, fileName, url, true, false, null,
				new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (switcher.getTag().equals(imgUrl)) {
							Drawable drawable = ((ImageView) switcher.getCurrentView())
									.getBackground();
							if (drawable != null) {
								ImageView imageView = (ImageView) switcher.getNextView();
								imageView.setBackgroundDrawable(null);
								switcher.setImageDrawable(new BitmapDrawable(imageBitmap));
							}
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		ImageView imageView = (ImageView) switcher.getCurrentView();
		if (bm != null) {
			imageView.setBackgroundDrawable(null);
			imageView.setImageBitmap(bm);
		} else {
			setDefaultBanner(imageView);
		}
	}

	/**
	 * 设置默认的banner图
	 */
	private void setDefaultBanner(ImageView item) {
		if (item == null) {
			return;
		}
		if (mDefaultBanner == null) {

			int id = R.drawable.appcenter_default_banner;
			mDefaultBanner = mContext.getResources().getDrawable(id);
		}
		item.setImageBitmap(null);
		item.setBackgroundDrawable(mDefaultBanner);
	}

	private void initAppState(Viewholder viewholder, final BoutiqueApp app, int position) {
		final int pos = position;
		final String fileName = GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH + app.info.packname
				+ "_" + app.info.version + ".apk";
		// 判断是否正在下载
		if (isDownloading(app.downloadState.state)) {
			viewholder.mButton.setVisibility(View.GONE);
			viewholder.mProgress.setVisibility(View.VISIBLE);
			switch (app.downloadState.state) {
				case DownloadTask.STATE_DOWNLOADING :
				case DownloadTask.STATE_START :
					viewholder.mProgress.setText(app.downloadState.alreadyDownloadPercent + "%");
					break;
				case DownloadTask.STATE_STOP :
					viewholder.mProgress.setText(R.string.download_manager_pause);
					break;
				case DownloadTask.STATE_WAIT :
					viewholder.mProgress.setText(R.string.download_manager_wait);
					break;
			}
		} else if (AppGameInstallingValidator.getInstance().isAppExist(mContext, app.info.packname)) {
			// 已安装
			viewholder.mButton.setBackgroundDrawable(null);
			viewholder.mButton.setText(R.string.appgame_installed);
			viewholder.mButton.setTextColor(0xFF393939);
			viewholder.mButton.setClickable(false);
		} else if (isApkExist(fileName)) {
			// 存在安装包，显示安装
			// 安装
			viewholder.mButton.setBackgroundResource(R.drawable.appgame_download_btn_selector);
			viewholder.mButton.setTextColor(Color.WHITE);
			viewholder.mButton.setText(R.string.appgame_install);
			// 安装操作
			viewholder.mButton.setClickable(true);
			viewholder.mButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					sendBrocastToIntall(fileName);
				}
			});
		} else { // 可下载
			if (app.info.isfree == 0) { // 免费
				// 下载
				viewholder.mButton.setBackgroundResource(R.drawable.appgame_download_btn_selector);
				viewholder.mButton.setTextColor(Color.WHITE);
				viewholder.mButton.setText(R.string.appgame_download);
				// 下载操作
				viewholder.mButton.setClickable(true);
				viewholder.mButton.setOnClickListener(new WrapOnClickListener() {

					@Override
					public void withoutSDCard(View v) {
						// FTP类型直接下载
						if (app.info.downloadtype == 1) {
							downloadApk(app, pos + 1, false);
						} else {
							goToMarket(app, pos);
						}
					}

					@Override
					public void withSDCard(View v) {
						// FTP类型直接下载
						if (app.info.downloadtype == 1) {
							downloadApk(app, pos + 1, true);
						} else {
							goToMarket(app, pos);
						}
					}
				});
			} else { // 收费
				// 显示价格
				viewholder.mButton.setBackgroundResource(R.drawable.appgame_download_btn_selector);
				viewholder.mButton.setTextColor(Color.WHITE);
				viewholder.mButton.setText(app.info.price);
				viewholder.mButton.setClickable(true);
				viewholder.mButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						goToMarket(app, pos);
					}
				});
			}
		}
	}

	/**
	 * 功能简述:判断是否处于下载状态 功能详细描述: 正在下载、开始下载、下载暂停、下载等待都返回true 注意:
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
	 * 功能简述:更新adapter的数据源 功能详细描述: 注意:
	 * 
	 * @param list
	 * @param loadImage
	 *            是否加载图片
	 */
	public void updateList(ArrayList<BoutiqueApp> list) {
		if (mList == null) {
			mList = new ArrayList<BoutiqueApp>();
		} else {
			mList.clear();
		}
		if (list == null) {
			return;
		}
		Map<String, DownloadTask> map = new HashMap<String, DownloadTask>();
		// 根据mDownloadTaskList初始化应用的下载状态
		if (mDownloadTaskList != null && mDownloadTaskList.size() > 0) {
			for (DownloadTask task : mDownloadTaskList) {
				if (task != null) {
					map.put(String.valueOf(task.getId()), task);
				}
			}
		}
		for (BoutiqueApp app : list) {
			if (app == null) {
				continue;
			}
			if (!TextUtils.isEmpty(app.pic)) {
				app.picLocalFileName = String.valueOf(app.pic.hashCode());
			}
			if (app.info != null && app.info.appid != null) {
				if (map.containsKey(app.info.appid)) {
					DownloadTask task = map.get(app.info.appid);
					app.downloadState.state = task.getState();
					app.downloadState.alreadyDownloadPercent = task.getAlreadyDownloadPercent();
				}
			}
			mList.add(app);
		}
		//		notifyDataSetChanged();
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
		// 判断treatment的值
		if (app.info.treatment > 0) {
			InstallCallbackManager.saveTreatment(app.info.packname, app.info.treatment);
		}
		// 判断是否需要安装成功之后回调
		if (app.info.icbackurl != null && !app.info.icbackurl.equals("")) {
			InstallCallbackManager.saveCallbackUrl(app.info.packname, app.info.icbackurl);
		}
		// 游戏中心
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
	private void sendBrocastToIntall(String fileName) {
		if (mContext == null || TextUtils.isEmpty(fileName)) {
			return;
		}
		AppsManagementActivity.sendHandler(mContext, IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
				IDiyMsgIds.APPS_MANAGEMENT_INSTALL_APP, 0, fileName, null);
	}

	/**
	 * 功能简述:跳转到电子市场，并进行统计 功能详细描述: 注意:
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
	 * 功能简述:设置是不是处于激活状态 功能详细描述: 注意:
	 * 
	 * @param isActive
	 */
	public void onActiveChange(boolean isActive) {
		mIsActive = isActive;
	}

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
	 * @date [2012-9-13]
	 */
	public class Viewholder {
		public RelativeLayout mLayout;
		public ImageView mImg;
		public ImageView mImgAnother;
		public ImageSwitcher mImageSwitcher;
		public TextView mName;
		public Button mButton;
		public TextView mProgress;
		public TextView mSize;
		public TextView mSummary;
	}
}

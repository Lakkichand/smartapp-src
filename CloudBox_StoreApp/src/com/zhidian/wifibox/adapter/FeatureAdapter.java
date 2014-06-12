package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import za.co.immedia.pinnedheaderlistview.SectionedBaseAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AppDetailActivity;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.controller.TabController;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.HtmlRegexpUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.view.ProgressBitmapDrawable;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 推荐列表adapter
 * 
 * @author zhaoyl
 * 
 */
public class FeatureAdapter extends SectionedBaseAdapter {

	private Context mContext;
	private List<AppDataBean> mList = new ArrayList<AppDataBean>();
	private LayoutInflater mInflater;
	private Bitmap mProgressBitmap;
	/**
	 * 浮在顶部的view
	 */
	private View mView;
	/**
	 * 跳转详情点击监听
	 */
	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AppDataBean bean = (AppDataBean) v.getTag();
			//long id = (Long) v.getTag(); TODO
			Intent intent = new Intent(mContext, AppDetailActivity.class);
			intent.putExtra("bean", bean);
			intent.putExtra("appId", bean.id);
			mContext.startActivity(intent);
		}
	};
	/**
	 * 打开应用的点击监听
	 */
	private OnClickListener mOpenAppClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String packName = (String) v.getTag();
			try {
				PackageManager packageManager = mContext.getPackageManager();
				Intent intent = packageManager
						.getLaunchIntentForPackage(packName);
				mContext.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	/**
	 * 打开APK的点击监听
	 */
	private OnClickListener mOpenApkClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				String fileName = (String) v.getTag();
				File file = new File(fileName);
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file),
						"application/vnd.android.package-archive");
				mContext.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	/**
	 * 暂停点击事件
	 */
	private OnClickListener mPauseClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AppDataBean bean = (AppDataBean) v.getTag();
			bean.downloadStatus = DownloadTask.PAUSING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_PAUSE);
			intent.putExtra("url", bean.downloadUrl);
			mContext.sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};
	/**
	 * 继续点击事件
	 */
	private OnClickListener mContinueClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AppDataBean bean = (AppDataBean) v.getTag();
			bean.downloadStatus = DownloadTask.DOWNLOADING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command",
					IDownloadInterface.REQUEST_COMMAND_CONTINUE);
			intent.putExtra("url", bean.downloadUrl);
			mContext.sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};
	/**
	 * 下载点击监听器
	 */
	private OnClickListener mDownloadClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AppDataBean bean = (AppDataBean) v.getTag();
			bean.downloadStatus = DownloadTask.DOWNLOADING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_ADD);
			intent.putExtra("url", bean.downloadUrl);
			intent.putExtra("iconUrl", bean.iconUrl);
			intent.putExtra("name", bean.name);
			intent.putExtra("size", bean.size);
			intent.putExtra("packName", bean.packName);
			intent.putExtra("appId", bean.id);
			intent.putExtra("version", bean.version);
			mContext.sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};

	public FeatureAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mProgressBitmap = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.icon_loading1)).getBitmap();
	}

	/**
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<AppDataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

	@Override
	public Object getItem(int section, int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int section, int position) {
		return position;
	}

	@Override
	public int getSectionCount() {
		return 1;
	}

	@Override
	public int getCountForSection(int section) {
		return mList.size();
	}

	@Override
	public View getItemView(int section, int position, View convertView,
			ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_recommend_app, null);
			holder.ivAvatar = (ImageView) convertView
					.findViewById(R.id.item_recommend_image);
			holder.btnDownLoad = (Button) convertView
					.findViewById(R.id.item_recommend_download);
			holder.tvAppDescribe = (TextView) convertView
					.findViewById(R.id.item_recommend_describe);
			holder.tvAppName = (TextView) convertView
					.findViewById(R.id.item_recommend_appname);
			holder.tvAppSize = (TextView) convertView
					.findViewById(R.id.item_recommend_size);
			holder.tvDownloadTime = (TextView) convertView
					.findViewById(R.id.item_recommend_time);
			holder.ratingBar = (RatingBar) convertView
					.findViewById(R.id.item_recommend_rating);
			holder.btnLayout = (LinearLayout) convertView
					.findViewById(R.id.recommend_layout_btn);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// 把bean的内容显示到view上
		AppDataBean bean = mList.get(position);
		long id = bean.id;
		holder.tvAppSize.setText(FileUtil.convertFileSize(bean.size));
		holder.tvAppName.setText(bean.name);
		holder.tvDownloadTime.setText(FileUtil
				.convertDownloadTimes(bean.downloads));
		holder.tvAppDescribe.setText(Html.fromHtml(HtmlRegexpUtil.filterimgHtml(bean.explain)));
		holder.ratingBar.setRating(bean.score / 2.0f);
		final ImageView image = holder.ivAvatar;
		image.setTag(bean.iconUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, bean.iconUrl.hashCode() + "",
				bean.iconUrl, true, true, new AsyncImageLoadedCallBack() {

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
			// 默认
			image.setImageBitmap(DrawUtil.sDefaultIcon);
		}
		holder.btnLayout.setTag(bean);
		holder.btnLayout.setOnClickListener(mItemClickListener);
		// 根据下载状态设置下载按钮的显示
		initDownloadState(holder.btnDownLoad, bean);
		return convertView;
	}

	/**
	 * 根据下载状态初始化下载按钮和点击事件
	 */
	private void initDownloadState(Button btnDownLoad, AppDataBean bean) {
		String packName = bean.packName;
		String apkFileName = DownloadUtil.getCApkFileFromUrl(bean.downloadUrl);
		boolean isInstall = InstallingValidator.getInstance().isAppExist(
				mContext, packName);
		if (isInstall) {
			// 已安装
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_open), null,
					null);
			btnDownLoad.setTag(packName);
			btnDownLoad.setOnClickListener(mOpenAppClickListener);
			btnDownLoad.setText(R.string.btn_open);
		} else if (bean.downloadStatus == DownloadTask.INSTALLING) {
			// 正在安装
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_install), null,
					null);
			btnDownLoad.setTag(null);
			btnDownLoad.setOnClickListener(null);
			btnDownLoad.setText(R.string.installing);
		} else if (FileUtil.isFileExist(apkFileName)) {
			// 已下载
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_install), null,
					null);
			btnDownLoad.setTag(apkFileName);
			btnDownLoad.setOnClickListener(mOpenApkClickListener);
			btnDownLoad.setText(R.string.btn_install);
		} else if (bean.downloadStatus == DownloadTask.DOWNLOADING) {
			// 正在下载
			// 根据具体的下载进度设置按钮图标
			Drawable drawable = new ProgressBitmapDrawable(
					mContext.getResources(), mProgressBitmap,
					bean.alreadyDownloadPercent, 0xFF888888, 0xFF0a78ee);
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, drawable,
					null, null);
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mPauseClickListener);
			btnDownLoad.setText(bean.alreadyDownloadPercent + "%");
		} else if (bean.downloadStatus == DownloadTask.PAUSING) {
			// 已经暂停
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_continue),
					null, null);
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mContinueClickListener);
			btnDownLoad.setText(R.string.btn_goon);
		} else {
			// 下载未开始或下载失败
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_download),
					null, null);
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mDownloadClickListener);
			btnDownLoad.setText(R.string.btn_download);
		}
	}

	@Override
	public View getSectionHeaderView(int section, View convertView,
			ViewGroup parent) {
		final NavigateViewHolder holder;
		if (convertView == null) {
			holder = new NavigateViewHolder();
			convertView = mInflater.inflate(R.layout.view_feature_navigate,
					null);
			holder.btnGame = (LinearLayout) convertView
					.findViewById(R.id.feature_navigate_game);
			holder.btnHotApp = (LinearLayout) convertView
					.findViewById(R.id.feature_navigate_hotapp);
			holder.btnSubject = (LinearLayout) convertView
					.findViewById(R.id.feature_navigate_subject);
			holder.btnRank = (LinearLayout) convertView
					.findViewById(R.id.feature_navigate_rank);
			convertView.setTag(holder);

		} else {
			holder = (NavigateViewHolder) convertView.getTag();
		}
		holder.btnHotApp.setOnClickListener(mClickListener);
		holder.btnSubject.setOnClickListener(mClickListener);
		holder.btnRank.setOnClickListener(mClickListener);
		holder.btnGame.setOnClickListener(mClickListener);
		mView = convertView;
		return convertView;
	}

	public void setTouch() {
		if (mView == null) {
			return;
		}
		NavigateViewHolder holder = (NavigateViewHolder) mView.getTag();
		holder.btnHotApp.setOnTouchListener(mTouchListener);
		holder.btnSubject.setOnTouchListener(mTouchListener);
		holder.btnRank.setOnTouchListener(mTouchListener);
		holder.btnGame.setOnTouchListener(mTouchListener);
		holder.btnHotApp.setOnClickListener(null);
		holder.btnSubject.setOnClickListener(null);
		holder.btnRank.setOnClickListener(null);
		holder.btnGame.setOnClickListener(null);
	}

	public void setClick() {
		if (mView == null) {
			return;
		}
		NavigateViewHolder holder = (NavigateViewHolder) mView.getTag();
		holder.btnHotApp.setOnTouchListener(null);
		holder.btnSubject.setOnTouchListener(null);
		holder.btnRank.setOnTouchListener(null);
		holder.btnGame.setOnTouchListener(null);
		holder.btnHotApp.setOnClickListener(mClickListener);
		holder.btnSubject.setOnClickListener(mClickListener);
		holder.btnRank.setOnClickListener(mClickListener);
		holder.btnGame.setOnClickListener(mClickListener);

	}

	/**
	 * 获取导航布局
	 */
	public View getLayout() {
		return mView;
	}

	static class ViewHolder {
		ImageView ivAvatar; // 应用头像
		TextView tvAppName; // 应用名称
		TextView tvDownloadTime; // 下载次数
		TextView tvAppSize; // 应用大小
		Button btnDownLoad; // 下载按钮
		TextView tvAppDescribe; // 应用描述
		RatingBar ratingBar; // 星级评分
		LinearLayout btnLayout; // 跳转按钮
	}

	static class NavigateViewHolder {
		LinearLayout btnHotApp; // 热门应用
		LinearLayout btnGame; // 精品游戏
		LinearLayout btnSubject; // 专题
		LinearLayout btnRank; // 排行
	}

	/**
	 * 浮动view 点击事件
	 */
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.feature_navigate_game:
				MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
						TabController.FEATUREHOTGAME, null);
				break;
			case R.id.feature_navigate_hotapp:
				// 通知TabManageView跳转下一层级
				MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
						TabController.FEATUREHOTAPP, null);
				break;
			case R.id.feature_navigate_subject:
				// 通知TabManageView跳转下一层级
				MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
						TabController.FEATURETOPIC, null);
				break;
			case R.id.feature_navigate_rank:
				// 通知TabManageView跳转下一层级
				MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
						TabController.FEATURECHARTS, null);
				break;

			default:
				break;
			}
		}
	};

	/**
	 * 浮动view touch事件
	 */
	private OnTouchListener mTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (v.getId()) {
			case R.id.feature_navigate_game:
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					// 通知TabManageView跳转下一层级
					MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
							IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
							TabController.FEATUREHOTGAME, null);
					break;
				}
				break;
			case R.id.feature_navigate_hotapp:
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					// 通知TabManageView跳转下一层级
					MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
							IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
							TabController.FEATUREHOTAPP, null);
					break;
				}
				break;
			case R.id.feature_navigate_subject:
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					// 通知TabManageView跳转下一层级
					MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
							IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
							TabController.FEATURETOPIC, null);
					break;
				}
				break;
			case R.id.feature_navigate_rank:
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					// 通知TabManageView跳转下一层级
					MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
							IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
							TabController.FEATURECHARTS, null);
					break;
				}
				break;

			default:
				break;
			}
			return false;
		}
	};

}

package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.XAppDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
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
 * 极速模式最新推荐页面数据适配器
 */
public class XNewAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	private List<XAppDataBean> mList = new ArrayList<XAppDataBean>();
	private Bitmap mProgressBitmap;
	/**
	 * 打开应用的点击监听
	 */
	private OnClickListener mOpenAppClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String packName = (String) v.getTag();
			try {
				PackageManager packageManager = TAApplication.getApplication()
						.getPackageManager();
				Intent intent = packageManager
						.getLaunchIntentForPackage(packName);
				TAApplication.getApplication().startActivity(intent);
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
				TAApplication.getApplication().startActivity(intent);
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
			XAppDataBean bean = (XAppDataBean) v.getTag();
			bean.downloadStatus = DownloadTask.PAUSING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_PAUSE);
			intent.putExtra("url", bean.downPath);
			TAApplication.getApplication().sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};
	/**
	 * 继续点击事件
	 */
	private OnClickListener mContinueClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			XAppDataBean bean = (XAppDataBean) v.getTag();
			bean.downloadStatus = DownloadTask.DOWNLOADING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command",
					IDownloadInterface.REQUEST_COMMAND_CONTINUE);
			intent.putExtra("url", bean.downPath);
			TAApplication.getApplication().sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};
	/**
	 * 下载点击监听器
	 */
	private OnClickListener mDownloadClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			XAppDataBean bean = (XAppDataBean) v.getTag();
			bean.downloadStatus = DownloadTask.DOWNLOADING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_ADD);
			intent.putExtra("url", bean.downPath);
			intent.putExtra("iconUrl", bean.iconPath);
			intent.putExtra("name", bean.name);
			intent.putExtra("size", bean.size);
			intent.putExtra("packName", bean.packageName);
			intent.putExtra("appId", bean.id + 0l);
			intent.putExtra("version", bean.version);
			TAApplication.getApplication().sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};

	public XNewAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		mProgressBitmap = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.icon_loading1)).getBitmap();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.speed_item_new, null);
			holder = new ViewHolder();
			holder.ivPhoto = (ImageView) convertView
					.findViewById(R.id.speed_app_logo);
			holder.ivSelect = (ImageView) convertView
					.findViewById(R.id.speed_app_select);
			holder.ivDownload = (ImageView) convertView
					.findViewById(R.id.download_iv);
			holder.tvApkName = (TextView) convertView
					.findViewById(R.id.speed_app_name);
			holder.speed = (TextView) convertView.findViewById(R.id.speed);
			holder.tvSize = (TextView) convertView
					.findViewById(R.id.size);
			holder.tvdownloadTime = (TextView) convertView
					.findViewById(R.id.item_new_time);
			holder.ratingBar = (RatingBar) convertView
					.findViewById(R.id.item_recommend_rating);
			holder.tvDownload = (TextView) convertView
					.findViewById(R.id.start_update_tv);
			holder.tvExplain = (TextView) convertView
					.findViewById(R.id.item_recommend_describe);
			holder.downloadArea = convertView.findViewById(R.id.download_area);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		XAppDataBean bean = mList.get(position);
		convertView.setTag(R.string.app, bean);
		final ImageView image = holder.ivPhoto;
		image.setTag(bean);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, bean.iconPath.hashCode() + "",
				bean.iconPath, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						XAppDataBean bean = (XAppDataBean) image.getTag();
						if (bean.iconPath.equals(imgUrl)) {
							if (bean.isSelect) {
								image.setImageBitmap(DrawUtil
										.createProgressBitmap(
												TAApplication.getApplication(),
												imageBitmap, 0));
							} else {
								image.setImageBitmap(DrawUtil
										.createProgressBitmap(
												TAApplication.getApplication(),
												imageBitmap, 100));
							}
						}
					}
				});
		if (bm == null) {
			bm = DrawUtil.sDefaultIcon;
		}
		holder.tvApkName.setText(bean.name);
		holder.tvExplain.setText(Html.fromHtml(HtmlRegexpUtil.filterimgHtml(bean.explain)));
		if (bean.downloadStatus == DownloadTask.DOWNLOADING) {
			holder.speed.setVisibility(View.VISIBLE);
			holder.tvSize.setVisibility(View.GONE);
			holder.speed.setText(bean.speed + "KB/S");
		} else {
			holder.speed.setVisibility(View.GONE);
			holder.tvSize.setVisibility(View.VISIBLE);
		}
		holder.ratingBar.setRating(bean.score / 2.0f);
		holder.tvSize.setText(FileUtil.convertFileSize(bean.size));
		holder.tvdownloadTime.setText(FileUtil
				.convertDownloadTimes(bean.downloads));
		initDownloadState(holder.ivDownload, holder.tvDownload, bean,
				holder.downloadArea);
		if (bean.isSelect) {
			holder.ivSelect.setVisibility(View.VISIBLE);
			image.setImageBitmap(DrawUtil.createProgressBitmap(
					TAApplication.getApplication(), bm, 0));
		} else {
			holder.ivSelect.setVisibility(View.GONE);
			image.setImageBitmap(DrawUtil.createProgressBitmap(
					TAApplication.getApplication(), bm, 100));
		}
		return convertView;
	}

	/**
	 * 根据下载状态初始化下载按钮和点击事件
	 */
	private void initDownloadState(ImageView image, TextView text,
			XAppDataBean bean, View downloadArea) {
		String packName = bean.packageName;
		String apkFileName = DownloadUtil.getXApkFileFromUrl(bean.downPath);
		boolean isInstall = InstallingValidator.getInstance().isAppExist(
				TAApplication.getApplication(), packName);
		if (isInstall) {
			// 已安装
			image.setImageResource(R.drawable.icon_open);
			downloadArea.setTag(packName);
			downloadArea.setOnClickListener(mOpenAppClickListener);
			text.setText(R.string.btn_open);
			if (bean.isSelect) {
				bean.isSelect = false;
			}
		} else if (bean.downloadStatus == DownloadTask.INSTALLING) {
			// 正在安装
			image.setImageResource(R.drawable.icon_install);
			downloadArea.setTag(null);
			downloadArea.setOnClickListener(null);
			text.setText(R.string.installing);
		} else if (FileUtil.isFileExist(apkFileName)) {
			// 已下载
			image.setImageResource(R.drawable.icon_install);
			downloadArea.setTag(apkFileName);
			downloadArea.setOnClickListener(mOpenApkClickListener);
			text.setText(R.string.btn_install);
			if (bean.isSelect) {
				bean.isSelect = false;
			}
		} else if (bean.downloadStatus == DownloadTask.DOWNLOADING) {
			// 正在下载
			// 根据具体的下载进度设置按钮图标
			Drawable drawable = new ProgressBitmapDrawable(TAApplication
					.getApplication().getResources(), mProgressBitmap,
					bean.alreadyDownloadPercent, 0xFF888888, 0xFF0a78ee);
			image.setImageDrawable(drawable);
			downloadArea.setTag(bean);
			downloadArea.setOnClickListener(mPauseClickListener);
			text.setText(bean.alreadyDownloadPercent + "%");
			if (bean.isSelect) {
				bean.isSelect = false;
			}
		} else if (bean.downloadStatus == DownloadTask.PAUSING) {
			// 已经暂停
			image.setImageResource(R.drawable.icon_continue);
			downloadArea.setTag(bean);
			downloadArea.setOnClickListener(mContinueClickListener);
			text.setText(R.string.btn_goon);
		} else {
			// 下载未开始或下载失败
			image.setImageResource(R.drawable.icon_download);
			downloadArea.setTag(bean);
			downloadArea.setOnClickListener(mDownloadClickListener);
			text.setText(R.string.btn_download);
		}
	}

	/**
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<XAppDataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

	static class ViewHolder {
		ImageView ivPhoto;
		ImageView ivSelect;
		ImageView ivDownload;
		TextView tvApkName;
		TextView speed;
		TextView tvSize;
		TextView tvdownloadTime; //下载次数
		TextView tvDownload;
		TextView tvExplain; //简介
		View downloadArea;
		RatingBar ratingBar; // 星级评分
	}

}

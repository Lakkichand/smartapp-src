package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.UpdateAppBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 更新App
 * 
 * @author zhaoyl
 * 
 */
public class UpdateAppAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<UpdateAppBean> updateList = new ArrayList<UpdateAppBean>();
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
			UpdateAppBean bean = (UpdateAppBean) v.getTag();
			bean.downloadStatus = DownloadTask.PAUSING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_PAUSE);
			intent.putExtra("url", bean.downloadUrl);
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
			UpdateAppBean bean = (UpdateAppBean) v.getTag();
			bean.downloadStatus = DownloadTask.WAITING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command",
					IDownloadInterface.REQUEST_COMMAND_CONTINUE);
			intent.putExtra("url", bean.downloadUrl);
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
			UpdateAppBean bean = (UpdateAppBean) v.getTag();
			bean.downloadStatus = DownloadTask.WAITING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_ADD);
			intent.putExtra("url", bean.downloadUrl);
			intent.putExtra("iconUrl", bean.iconUrl);
			intent.putExtra("name", bean.name);
			intent.putExtra("size", bean.size);
			intent.putExtra("packName", bean.packageName);
			intent.putExtra("appId", bean.id + 0l);
			intent.putExtra("version", bean.version);
			intent.putExtra("page", "应用更新");
			TAApplication.getApplication().sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};

	public UpdateAppAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.updatelist_item, null);
			holder.icon = (ImageView) convertView
					.findViewById(R.id.item_recommend_image);
			holder.name = (TextView) convertView
					.findViewById(R.id.item_recommend_appname);
			holder.progressbar = (ProgressBar) convertView
					.findViewById(R.id.progress);
			holder.speed = (TextView) convertView.findViewById(R.id.speed);
			holder.currentSize = (TextView) convertView
					.findViewById(R.id.current_size);
			holder.totalSize = (TextView) convertView
					.findViewById(R.id.item_recommend_size);
			holder.opeara = (Button) convertView.findViewById(R.id.operator);
			holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);
			holder.gap = convertView.findViewById(R.id.gap);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.arrow.setVisibility(View.INVISIBLE);
		holder.arrow.getLayoutParams().width = DrawUtil.dip2px(
				TAApplication.getApplication(), 20);
		if (position == getCount() - 1) {
			holder.gap.setVisibility(View.INVISIBLE);
		} else {
			holder.gap.setVisibility(View.VISIBLE);
		}
		UpdateAppBean ua = updateList.get(position);
		final ImageView image = holder.icon;
		image.setTag(ua.iconUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, ua.iconUrl.hashCode() + "",
				ua.iconUrl, true, true, new AsyncImageLoadedCallBack() {

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
		holder.name.setText(ua.name);
		initDownloadState(ua, holder.progressbar, holder.speed,
				holder.currentSize, holder.totalSize, holder.opeara);
		return convertView;
	}

	/**
	 * 根据下载状态初始化下载按钮和点击事件
	 */
	private void initDownloadState(UpdateAppBean bean, ProgressBar progress,
			TextView speed, TextView currentSize, TextView totalSize,
			Button opeara) {
		String apkFileName = DownloadUtil.getCApkFileFromUrl(bean.downloadUrl);
		if (bean.downloadStatus == DownloadTask.INSTALLING) {
			// 正在安装
			progress.setVisibility(View.GONE);
			speed.setVisibility(View.VISIBLE);
			currentSize.setVisibility(View.GONE);
			totalSize.setVisibility(View.GONE);
			opeara.setText("安装中");
			opeara.setBackgroundResource(R.drawable.downloadmanager_installing_btn_bg);
			opeara.setTextColor(0xFF669900);// 设置按钮文字颜色
			opeara.setOnClickListener(null);
			speed.setText("等待安装");
		} else if (FileUtil.isFileExist(apkFileName)) {
			// 已下载
			progress.setVisibility(View.GONE);
			speed.setVisibility(View.VISIBLE);
			currentSize.setVisibility(View.GONE);
			totalSize.setVisibility(View.GONE);
			opeara.setText("安装");
			opeara.setBackgroundResource(R.drawable.downloadmanager_install_btn_bg);
			Resources resource = TAApplication.getApplication().getResources();
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.downloadmanager_install_selector);
			if (csl != null) {
				opeara.setTextColor(csl);// 设置按钮文字颜色
			}
			opeara.setTag(apkFileName);
			opeara.setOnClickListener(mOpenApkClickListener);
			speed.setText("等待安装");
		} else if (bean.downloadStatus == DownloadTask.DOWNLOADING) {
			// 正在下载
			progress.setVisibility(View.VISIBLE);
			speed.setVisibility(View.VISIBLE);
			currentSize.setVisibility(View.VISIBLE);
			totalSize.setVisibility(View.VISIBLE);
			opeara.setText("暂停");
			opeara.setBackgroundResource(R.drawable.downloadmanager_pause_btn_bg);
			Resources resource = TAApplication.getApplication().getResources();
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.downloadmanager_pause_selector);
			if (csl != null) {
				opeara.setTextColor(csl);// 设置按钮文字颜色
			}
			opeara.setTag(bean);
			opeara.setOnClickListener(mPauseClickListener);
			progress.setProgress(bean.alreadyDownloadPercent);
			currentSize.setText(Formatter.formatShortFileSize(
					TAApplication.getApplication(), (long) (bean.size * 1024.0
							* bean.alreadyDownloadPercent / 100.0 + 0.5))
					+ "/");
			totalSize.setText(Formatter.formatShortFileSize(
					TAApplication.getApplication(), bean.size * 1024L));
			Map<String, DownloadTask> map = DownloadTaskRecorder.getInstance()
					.getDownloadTaskList();
			DownloadTask task = map.get(bean.downloadUrl);
			if (task != null) {
				speed.setText(task.speed + "KB/S");
			} else {
				speed.setText("0KB/S");
			}
		} else if (bean.downloadStatus == DownloadTask.WAITING) {
			// 等待下载
			progress.setVisibility(View.VISIBLE);
			speed.setVisibility(View.VISIBLE);
			currentSize.setVisibility(View.VISIBLE);
			totalSize.setVisibility(View.VISIBLE);
			opeara.setText("等待中");
			opeara.setBackgroundResource(R.drawable.downloadmanager_waiting_btn_bg);
			opeara.setTextColor(0xFF33b5e5);// 设置按钮文字颜色
			opeara.setOnClickListener(null);
			progress.setProgress(bean.alreadyDownloadPercent);
			speed.setText("等待中");
			currentSize.setText(Formatter.formatShortFileSize(
					TAApplication.getApplication(), (long) (bean.size * 1024.0
							* bean.alreadyDownloadPercent / 100.0 + 0.5))
					+ "/");
			totalSize.setText(Formatter.formatShortFileSize(
					TAApplication.getApplication(), bean.size * 1024L));
		} else if (bean.downloadStatus == DownloadTask.PAUSING) {
			// 已经暂停
			progress.setVisibility(View.VISIBLE);
			speed.setVisibility(View.VISIBLE);
			currentSize.setVisibility(View.VISIBLE);
			totalSize.setVisibility(View.VISIBLE);
			opeara.setText("继续");
			opeara.setBackgroundResource(R.drawable.downloadmanager_continue_btn_bg);
			Resources resource = TAApplication.getApplication().getResources();
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.downloadmanager_continue_selector);
			if (csl != null) {
				opeara.setTextColor(csl);// 设置按钮文字颜色
			}
			opeara.setTag(bean);
			opeara.setOnClickListener(mContinueClickListener);
			progress.setProgress(bean.alreadyDownloadPercent);
			speed.setText("已暂停");
			currentSize.setText(Formatter.formatShortFileSize(
					TAApplication.getApplication(), (long) (bean.size * 1024.0
							* bean.alreadyDownloadPercent / 100.0 + 0.5))
					+ "/");
			totalSize.setText(Formatter.formatShortFileSize(
					TAApplication.getApplication(), bean.size * 1024L));
		} else {
			// 下载未开始或下载失败
			progress.setVisibility(View.GONE);
			speed.setVisibility(View.VISIBLE);
			currentSize.setVisibility(View.GONE);
			totalSize.setVisibility(View.GONE);
			opeara.setText("升级");
			opeara.setBackgroundResource(R.drawable.downloadmanager_update_btn_bg);
			Resources resource = TAApplication.getApplication().getResources();
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.downloadmanager_update_selector);
			if (csl != null) {
				opeara.setTextColor(csl);// 设置按钮文字颜色
			}
			opeara.setTag(bean);
			opeara.setOnClickListener(mDownloadClickListener);
			progress.setProgress(bean.alreadyDownloadPercent);
			speed.setText("升至 "
					+ "V"
					+ bean.version
					+ "   "
					+ "需:"
					+ Formatter.formatShortFileSize(
							TAApplication.getApplication(), bean.size * 1024L)
					+ "流量");
		}
	}

	/**
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<UpdateAppBean> list) {
		updateList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		updateList.addAll(list);
		notifyDataSetChanged();
	}

	static class ViewHolder {
		ImageView icon; // 应用Logo
		TextView name; // 应用名称
		ProgressBar progressbar;
		TextView speed;
		TextView currentSize;
		TextView totalSize;
		Button opeara;
		ImageView arrow;
		View gap;
	}

	@Override
	public int getCount() {
		return updateList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

}

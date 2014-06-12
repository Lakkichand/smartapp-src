package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.UpdateAppBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.view.ProgressBitmapDrawable;
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
	private Bitmap mProgressBitmap;
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
			bean.downloadStatus = DownloadTask.DOWNLOADING;
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
			bean.downloadStatus = DownloadTask.DOWNLOADING;
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
			TAApplication.getApplication().sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};

	public UpdateAppAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mProgressBitmap = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.icon_loading1)).getBitmap();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_update, null);
			holder.ivAppImage = (ImageView) convertView
					.findViewById(R.id.app_logo);
			holder.ivUpdateImage = (ImageView) convertView
					.findViewById(R.id.download_iv);
			holder.tvAppSize = (TextView) convertView
					.findViewById(R.id.new_size_tv);
			holder.tvVersion = (TextView) convertView
					.findViewById(R.id.new_version_tv);
			holder.tvUpdateText = (TextView) convertView
					.findViewById(R.id.start_update_tv);
			holder.tvAppName = (TextView) convertView
					.findViewById(R.id.app_name_tv);
			holder.downloadArea = convertView.findViewById(R.id.aa);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		UpdateAppBean ua = updateList.get(position);
		final ImageView image = holder.ivAppImage;
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
		holder.tvAppName.setText(ua.name);
		holder.tvAppSize.setText("大小：" + FileUtil.convertFileSize(ua.size));
		holder.tvVersion.setText("版本：" + ua.version);

		initDownloadState(holder.ivUpdateImage, holder.tvUpdateText, ua,
				holder.downloadArea);
		return convertView;
	}

	/**
	 * 根据下载状态初始化下载按钮和点击事件
	 */
	private void initDownloadState(ImageView image, TextView text,
			UpdateAppBean bean, View downloadArea) {
		String apkFileName = DownloadUtil.getCApkFileFromUrl(bean.downloadUrl);
		if (bean.downloadStatus == DownloadTask.INSTALLING) {
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
		} else if (bean.downloadStatus == DownloadTask.PAUSING) {
			// 已经暂停
			image.setImageResource(R.drawable.icon_continue);
			downloadArea.setTag(bean);
			downloadArea.setOnClickListener(mContinueClickListener);
			text.setText(R.string.btn_goon);
		} else {
			// 下载未开始或下载失败
			image.setImageResource(R.drawable.update_icon);
			downloadArea.setTag(bean);
			downloadArea.setOnClickListener(mDownloadClickListener);
			text.setText(R.string.btn_update);
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
		ImageView ivAppImage; // 应用Logo
		TextView tvAppName; // 应用名称
		TextView tvVersion; // 版本
		TextView tvAppSize; // 应用大小
		ImageView ivUpdateImage; // 升级按钮image
		TextView tvUpdateText; // 升级Text
		View downloadArea;
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

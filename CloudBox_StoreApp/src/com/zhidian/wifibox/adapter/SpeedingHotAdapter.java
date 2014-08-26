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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.view.ProgressBitmapDrawable;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 极速下载：热门推荐适配器
 * 
 * @author xiedezhi
 * 
 */
public class SpeedingHotAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	private List<XAppDataBean> mList = new ArrayList<XAppDataBean>();

	private Bitmap mProgressBitmap;
	private Bitmap mProgressBitmapGoon;

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
			bean.downloadStatus = DownloadTask.WAITING;
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
			bean.downloadStatus = DownloadTask.WAITING;
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
			intent.putExtra("src", bean.src);
			TAApplication.getApplication().sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};
	/**
	 * 跳转详情点击监听
	 */
	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO
			XAppDataBean bean = (XAppDataBean) v.getTag();
		}
	};

	public SpeedingHotAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		mProgressBitmap = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.icon_loading1)).getBitmap();
		mProgressBitmapGoon = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.icon_continue)).getBitmap();
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
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_recommend_app, null);
			holder.ivAvatar = (ImageView) convertView
					.findViewById(R.id.item_recommend_image);
			holder.btnDownLoad = (LinearLayout) convertView
					.findViewById(R.id.download_btn);
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
			holder.tvStatusText = (TextView) convertView
					.findViewById(R.id.start_update_tv);
			holder.ivStatusImg = (ImageView) convertView
					.findViewById(R.id.download_iv);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// 把bean的内容显示到view上
		XAppDataBean bean = mList.get(position);
		holder.tvAppSize.setText(FileUtil.convertFileSize(bean.size));
		holder.tvAppName.setText(bean.name);
		holder.tvDownloadTime.setText(FileUtil
				.convertDownloadTimes(bean.downloads));
		holder.tvAppDescribe.setText(Html.fromHtml(bean.explain));
		holder.ratingBar.setRating(bean.score / 2.0f);

		final ImageView image = holder.ivAvatar;
		image.setTag(bean.iconPath);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, bean.iconPath.hashCode() + "",
				bean.iconPath, true, true, new AsyncImageLoadedCallBack() {

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
		initDownloadState(holder.btnDownLoad, holder.tvStatusText,
				holder.ivStatusImg, bean);
		return convertView;
	}

	/**
	 * 根据下载状态初始化下载按钮和点击事件
	 */
	private void initDownloadState(LinearLayout btnDownLoad, TextView tvStatus,
			ImageView ivIcon, XAppDataBean bean) {
		String packName = bean.packageName;
		String apkFileName = DownloadUtil.getCApkFileFromUrl(bean.downPath);
		boolean isInstall = InstallingValidator.getInstance().isAppExist(
				TAApplication.getApplication(), packName);
		if (isInstall) {
			// 已安装
			ivIcon.setImageResource(R.drawable.icon_open);
			tvStatus.setText(R.string.btn_open);
			tvStatus.setTextColor(TAApplication.getApplication()
					.getResources().getColor(R.color.download_status_blue));
			btnDownLoad.setTag(packName);
			btnDownLoad.setOnClickListener(mOpenAppClickListener);
		} else if (bean.downloadStatus == DownloadTask.INSTALLING) {
			// 正在安装
			ivIcon.setImageResource(R.drawable.icon_install);
			tvStatus.setText(R.string.installing);
			tvStatus.setTextColor(TAApplication.getApplication()
					.getResources().getColor(R.color.download_status_qing));
			
			btnDownLoad.setTag(null);
			btnDownLoad.setOnClickListener(null);
		} else if (FileUtil.isFileExist(apkFileName)) {
			// 已下载
			ivIcon.setImageResource(R.drawable.icon_install);
			tvStatus.setText(R.string.btn_install);
			tvStatus.setTextColor(TAApplication.getApplication()
					.getResources().getColor(R.color.download_status_qing));
			btnDownLoad.setTag(apkFileName);
			btnDownLoad.setOnClickListener(mOpenApkClickListener);
		} else if (bean.downloadStatus == DownloadTask.DOWNLOADING) {
			// 正在下载
			// 根据具体的下载进度设置按钮图标
			Drawable drawable = new ProgressBitmapDrawable(TAApplication
					.getApplication().getResources(), mProgressBitmap,
					bean.alreadyDownloadPercent, 0xFF99cc00, 0xFF669900);
			ivIcon.setImageDrawable(drawable);
			tvStatus.setText(bean.alreadyDownloadPercent + "%");
			tvStatus.setTextColor(TAApplication.getApplication()
					.getResources().getColor(R.color.download_status_ing));
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mPauseClickListener);
		} else if (bean.downloadStatus == DownloadTask.WAITING) {
			// 等待下载
			ivIcon.setImageResource(R.drawable.wait);
			tvStatus.setText(R.string.btn_waiting);
			tvStatus.setTextColor(TAApplication.getApplication()
					.getResources().getColor(R.color.download_status_wait));
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(null);
		} else if (bean.downloadStatus == DownloadTask.PAUSING) {
			// 已经暂停
			Drawable drawable = new ProgressBitmapDrawable(TAApplication
					.getApplication().getResources(), mProgressBitmapGoon,
					bean.alreadyDownloadPercent, 0xFFFFBB33, 0xFFFF8800);
			ivIcon.setImageDrawable(drawable);
			tvStatus.setText(R.string.btn_goon);
			tvStatus.setTextColor(TAApplication.getApplication()
					.getResources().getColor(R.color.download_status_goon));
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mContinueClickListener);
		} else {
			// 下载未开始或下载失败
			ivIcon.setImageResource(R.drawable.icon_download);
			tvStatus.setText(R.string.btn_download);
			tvStatus.setTextColor(TAApplication.getApplication()
					.getResources().getColor(R.color.download_status_wait));
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mDownloadClickListener);
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
		ImageView ivAvatar; // 应用头像
		TextView tvAppName; // 应用名称
		TextView tvDownloadTime; // 下载次数
		TextView tvAppSize; // 应用大小
		TextView tvAppDescribe; // 应用描述
		RatingBar ratingBar; // 星级评分
		LinearLayout btnLayout; // 跳转按钮
		LinearLayout btnDownLoad; // 下载按钮
		TextView tvStatusText; // 下载状态
		ImageView ivStatusImg; // 下载状态图标
	}

}

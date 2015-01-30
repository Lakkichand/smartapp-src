package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 自动搜索关键字Adapter
 * 
 * @author zhaoyl
 * 
 */
public class KeywordAdapter extends BaseAdapter {

	private Bitmap mProgressBitmap;
	private Bitmap mProgressBitmapGoon;
	private List<AppDataBean> mList = new ArrayList<AppDataBean>();
	private LayoutInflater mInflater;
	private Context mContext;
	private mDeleteOnclickListener deleteOnclickListener;

	public interface mDeleteOnclickListener {
		void onDelete(AppDataBean bean);
	}

	public KeywordAdapter(Context context, mDeleteOnclickListener onDeleListener) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		deleteOnclickListener = onDeleListener;
		mProgressBitmap = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.icon_loading1)).getBitmap();
		mProgressBitmapGoon = ((BitmapDrawable) context.getResources()
				.getDrawable(R.drawable.icon_continue)).getBitmap();
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
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_keyword, null);
			holder.ivAvatar = (ImageView) convertView
					.findViewById(R.id.keyword_avatar);
			holder.tvAppName = (TextView) convertView
					.findViewById(R.id.keyword_name);
			holder.mDownload3 = (LinearLayout) convertView
					.findViewById(R.id.download3);
			holder.mButton3 = (Button) convertView.findViewById(R.id.button3);
			holder.mBar3 = (ProgressBar) convertView
					.findViewById(R.id.progress3);
			holder.mProgressText3 = (TextView) convertView
					.findViewById(R.id.progress_text3);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		AppDataBean bean = mList.get(position);
		convertView.setTag(R.string.all_clear, bean);

		if (position == 0) {
			holder.ivAvatar.setVisibility(View.VISIBLE);
			holder.mDownload3.setVisibility(View.VISIBLE);

			final ImageView image = holder.ivAvatar;
			image.setTag(bean.iconUrl);
			Bitmap bm = AsyncImageManager.getInstance().loadImage(
					PathConstant.ICON_ROOT_PATH, bean.iconUrl.hashCode() + "",
					bean.iconUrl, true, true, new AsyncImageLoadedCallBack() {

						@Override
						public void imageLoaded(Bitmap imageBitmap,
								String imgUrl) {
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

			initDownloadState(holder.mDownload3, holder.mButton3, holder.mBar3,
					holder.mProgressText3, bean);
		} else {

			holder.ivAvatar.setVisibility(View.GONE);
			holder.mDownload3.setVisibility(View.GONE);

		}

		holder.tvAppName.setText(bean.name);

		return convertView;
	}

	/**
	 * 根据下载状态初始化下载按钮和点击事件
	 */
	private void initDownloadState(LinearLayout download, Button btnDownLoad,
			ProgressBar progress, TextView progressText, AppDataBean bean) {
		String packName = bean.packName;
		String apkFileName = DownloadUtil.getCApkFileFromUrl(bean.downloadUrl);
		boolean isInstall = InstallingValidator.getInstance().isAppExist(
				TAApplication.getApplication(), packName);
		if (isInstall) {
			// 已安装
			download.setOnClickListener(null);
			btnDownLoad.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			progressText.setVisibility(View.GONE);
			btnDownLoad.setText("打开");
			btnDownLoad.setTextColor(0xFF21a0fd);
			btnDownLoad.setTag(packName);
			btnDownLoad.setOnClickListener(mOpenAppClickListener);
		} else if (bean.downloadStatus == DownloadTask.INSTALLING) {
			// 正在安装
			download.setOnClickListener(null);
			btnDownLoad.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			progressText.setVisibility(View.GONE);
			btnDownLoad.setText("安装中");
			btnDownLoad.setTextColor(0xFF549900);
			btnDownLoad.setOnClickListener(null);
		} else if (FileUtil.isFileExist(apkFileName)) {
			// 已下载未安装
			download.setOnClickListener(null);
			btnDownLoad.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			progressText.setVisibility(View.GONE);
			btnDownLoad.setText("安装");
			btnDownLoad.setTextColor(0xFF589b00);
			btnDownLoad.setTag(apkFileName);
			btnDownLoad.setOnClickListener(mOpenApkClickListener);
		} else if (bean.downloadStatus == DownloadTask.DOWNLOADING) {
			// 正在下载
			btnDownLoad.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
			progressText.setVisibility(View.VISIBLE);
			progress.setProgress(bean.alreadyDownloadPercent);
			progressText.setText(bean.alreadyDownloadPercent + "%");
			// 点击暂停
			download.setTag(bean);
			download.setOnClickListener(mPauseClickListener);
		} else if (bean.downloadStatus == DownloadTask.WAITING) {
			// 等待下载
			download.setOnClickListener(null);
			btnDownLoad.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
			progressText.setVisibility(View.VISIBLE);
			progress.setProgress(bean.alreadyDownloadPercent);
			progressText.setText("等待中");
		} else if (bean.downloadStatus == DownloadTask.PAUSING) {
			// 已经暂停，点击继续
			download.setOnClickListener(null);
			btnDownLoad.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			progressText.setVisibility(View.GONE);
			btnDownLoad.setText("继续");
			btnDownLoad.setTextColor(0xFFff8600);
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mContinueClickListener);
		} else {
			// 下载未开始或下载失败
			download.setOnClickListener(null);
			btnDownLoad.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			progressText.setVisibility(View.GONE);
			btnDownLoad.setText("下载");
			btnDownLoad.setTextColor(0xFF343434);
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mDownloadClickListener);
		}
	}

	/**
	 * 删除点击事件
	 */
	private OnClickListener mDeleteClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AppDataBean bean = (AppDataBean) v.getTag();
			deleteOnclickListener.onDelete(bean);
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
			bean.downloadStatus = DownloadTask.WAITING;
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
			bean.downloadStatus = DownloadTask.WAITING;
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
			intent.putExtra("page", "搜索关键字提示");
			mContext.sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};

	static class ViewHolder {
		ImageView ivAvatar;// 头像
		TextView tvAppName; // 名称
		LinearLayout mDownload3;// 下载
		Button mButton3;
		ProgressBar mBar3;
		TextView mProgressText3;
	}

}

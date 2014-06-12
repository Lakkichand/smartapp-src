package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.XAllDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 极速模式全部应用页面数据适配器
 */
public class XAllAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<XAllDataBean> mList = new ArrayList<XAllDataBean>();
	/**
	 * 手机屏幕宽度
	 */
	private int width;
	private Context mContext;
	/**
	 * 点击事件
	 */
	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			XAllDataBean bean = (XAllDataBean) v.getTag();
			String apkFileName = DownloadUtil.getXApkFileFromUrl(bean.downPath);
			// 已下载
			boolean isDownloaded = FileUtil.isFileExist(apkFileName);
			// 已安装
			boolean isInstall = InstallingValidator.getInstance().isAppExist(
					mContext, bean.packName);
			if (isInstall) {
				// 已安装，打开应用
				try {
					PackageManager packageManager = mContext
							.getPackageManager();
					Intent intent = packageManager
							.getLaunchIntentForPackage(bean.packName);
					mContext.startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (isDownloaded) {
				// 已下载，点击安装
				try {
					File file = new File(apkFileName);
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(file),
							"application/vnd.android.package-archive");
					mContext.startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (bean.downloadStatus == DownloadTask.DOWNLOADING) {
				// 点击暂停
				bean.downloadStatus = DownloadTask.PAUSING;
				Intent intent = new Intent(
						IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
				intent.putExtra("command",
						IDownloadInterface.REQUEST_COMMAND_PAUSE);
				intent.putExtra("url", bean.downPath);
				mContext.sendBroadcast(intent);
				notifyDataSetChanged();
			} else if (bean.downloadStatus == DownloadTask.PAUSING) {
				// 点击继续
				bean.downloadStatus = DownloadTask.DOWNLOADING;
				Intent intent = new Intent(
						IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
				intent.putExtra("command",
						IDownloadInterface.REQUEST_COMMAND_CONTINUE);
				intent.putExtra("url", bean.downPath);
				mContext.sendBroadcast(intent);
				notifyDataSetChanged();
			} else if (bean.downloadStatus == DownloadTask.NOT_START
					&& !isDownloaded && !isInstall) {
				// 点击下载
				bean.downloadStatus = DownloadTask.DOWNLOADING;
				Intent intent = new Intent(
						IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
				intent.putExtra("command",
						IDownloadInterface.REQUEST_COMMAND_ADD);
				intent.putExtra("url", bean.downPath);
				intent.putExtra("iconUrl", bean.iconPath);
				intent.putExtra("name", bean.name);
				intent.putExtra("size", bean.size);
				intent.putExtra("packName", bean.packName);
				intent.putExtra("appId", bean.id + 0l);
				intent.putExtra("version", bean.version);
				mContext.sendBroadcast(intent);
				notifyDataSetChanged();
			}
		}
	};

	public XAllAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
		mContext = context;
		InfoUtil infoUtil = new InfoUtil(context);
		width = infoUtil.getWidth();
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
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.speed_gridview_item_all,
					null);
			int pwidth = width * 3 / 16;
			int pheight = 195 * pwidth / 150;
			convertView.setLayoutParams(new GridView.LayoutParams(pwidth,
					pheight));
			ImageView continueView = (ImageView) convertView
					.findViewById(R.id.continue_img);
			int csize = (int) (pwidth / 3.0 + 0.5);
			continueView.getLayoutParams().width = csize;
			continueView.getLayoutParams().height = csize;
		}
		XAllDataBean bean = mList.get(position);
		TextView progress = (TextView) convertView.findViewById(R.id.progress);
		final ImageView image = (ImageView) convertView
				.findViewById(R.id.speed_all_photo);
		image.setTag(bean);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, bean.iconPath.hashCode() + "",
				bean.iconPath, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						XAllDataBean bean = (XAllDataBean) image.getTag();
						if (bean.iconPath.equals(imgUrl)) {
							if (bean.downloadStatus == DownloadTask.DOWNLOADING
									|| bean.downloadStatus == DownloadTask.PAUSING) {
								image.setImageBitmap(DrawUtil
										.createProgressBitmap(mContext,
												imageBitmap,
												bean.alreadyDownloadPercent - 5));
							} else {
								image.setImageBitmap(DrawUtil
										.createProgressBitmap(mContext,
												imageBitmap, 100));
							}
						}
					}
				});
		if (bm == null) {
			bm = DrawUtil.sDefaultIcon;
		}
		if (bean.downloadStatus == DownloadTask.DOWNLOADING
				|| bean.downloadStatus == DownloadTask.PAUSING) {
			progress.setVisibility(View.VISIBLE);
			progress.setText(bean.alreadyDownloadPercent + "%");
			image.setImageBitmap(DrawUtil.createProgressBitmap(mContext, bm,
					bean.alreadyDownloadPercent - 5));
		} else {
			image.setImageBitmap(DrawUtil.createProgressBitmap(mContext, bm,
					100));
			progress.setVisibility(View.GONE);
		}
		ImageView continueImg = (ImageView) convertView
				.findViewById(R.id.continue_img);
		if (bean.downloadStatus == DownloadTask.PAUSING) {
			continueImg.setVisibility(View.VISIBLE);
		} else {
			continueImg.setVisibility(View.GONE);
		}
		String apkFileName = DownloadUtil.getXApkFileFromUrl(bean.downPath);
		// 已下载
		boolean isDownloaded = FileUtil.isFileExist(apkFileName);
		// 已安装
		boolean isInstall = InstallingValidator.getInstance().isAppExist(
				mContext, bean.packName);
		ImageView mask = (ImageView) convertView
				.findViewById(R.id.speed_all_check);
		// 未下载未安装
		if (bean.downloadStatus == DownloadTask.NOT_START && !isDownloaded
				&& !isInstall) {
			mask.setVisibility(View.VISIBLE);
		} else {
			mask.setVisibility(View.GONE);
		}
		ImageView installImg = (ImageView) convertView
				.findViewById(R.id.speed_all_no_install);
		// 已下载未安装
		if (isDownloaded && !isInstall) {
			installImg.setVisibility(View.VISIBLE);
		} else {
			installImg.setVisibility(View.GONE);
		}
		TextView name = (TextView) convertView
				.findViewById(R.id.speed_all_app_name);
		name.setText(bean.name);
		convertView.setTag(bean);
		convertView.setOnClickListener(mItemClickListener);
		return convertView;
	}

	/**
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<XAllDataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

}

package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AppDetailActivity;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.view.dialog.DownloadDeleteDialog;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 下载管理页数据适配器
 * 
 * @author xiedezhi
 * 
 */
public class DownloadManagerAdapter extends BaseExpandableListAdapter {
	/**
	 * 正在下载的任务
	 */
	private List<DownloadTask> mDownloadingList = new ArrayList<DownloadTask>();
	/**
	 * 已经下载完成的任务
	 */
	private List<DownloadTask> mDownloadedList = new ArrayList<DownloadTask>();

	/**
	 * 点击进入应用详情
	 */
	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			DownloadTask task = (DownloadTask) v.getTag();
			long id = task.appId;
			Intent intent = new Intent(TAApplication.getApplication(),
					AppDetailActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("appId", id);
			TAApplication.getApplication().startActivity(intent);
		}
	};
	/**
	 * 删除点击事件
	 */
	private OnClickListener mDeleteClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			DownloadTask task = (DownloadTask) v.getTag();
			DownloadDeleteDialog dialog = new DownloadDeleteDialog(
					TAApplication.getApplication(), task);
			dialog.show();
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
			DownloadTask task = (DownloadTask) v.getTag();
			task.state = DownloadTask.PAUSING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_PAUSE);
			intent.putExtra("url", task.url);
			TAApplication.getApplication().sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};
	/**
	 * 继续点击事件.
	 */
	private OnClickListener mContinueClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			DownloadTask task = (DownloadTask) v.getTag();
			task.state = DownloadTask.WAITING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command",
					IDownloadInterface.REQUEST_COMMAND_CONTINUE);
			intent.putExtra("url", task.url);
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
			DownloadTask task = (DownloadTask) v.getTag();
			task.state = DownloadTask.WAITING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_ADD);
			intent.putExtra("url", task.url);
			intent.putExtra("iconUrl", task.iconUrl);
			intent.putExtra("name", task.name);
			intent.putExtra("size", task.size);
			intent.putExtra("packName", task.packName);
			intent.putExtra("appId", task.appId);
			intent.putExtra("version", task.version);
			intent.putExtra("src", task.src);
			intent.putExtra("page", "下载管理");
			TAApplication.getApplication().sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};

	private LayoutInflater mInflater = LayoutInflater.from(TAApplication
			.getApplication());

	@Override
	public int getGroupCount() {
		return 2;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (groupPosition == 0) {
			return mDownloadingList.size();
		} else {
			return mDownloadedList.size();
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.downloadlist_title, null);
		}
		TextView title = (TextView) convertView.findViewById(R.id.title);
		TextView opera = (TextView) convertView.findViewById(R.id.all_start);
		View content = convertView.findViewById(R.id.content);
		if (groupPosition == 0) {
			boolean downloading = true;
			for (DownloadTask task : mDownloadingList) {
				if (task.state == DownloadTask.PAUSING) {
					downloading = false;
					break;
				}
			}
			if (!downloading) {
				opera.setText("全部开始");
				opera.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						for (DownloadTask task : mDownloadingList) {
							if (task.state == DownloadTask.PAUSING) {
								task.state = DownloadTask.WAITING;
								Intent intent = new Intent(
										IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
								intent.putExtra(
										"command",
										IDownloadInterface.REQUEST_COMMAND_CONTINUE);
								intent.putExtra("url", task.url);
								TAApplication.getApplication().sendBroadcast(
										intent);
							}
						}
						notifyDataSetChanged();
					}
				});
			} else {
				opera.setText("全部暂停");
				opera.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						for (DownloadTask task : mDownloadingList) {
							if (task.state == DownloadTask.DOWNLOADING
									|| task.state == DownloadTask.WAITING) {
								task.state = DownloadTask.PAUSING;
								Intent intent = new Intent(
										IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
								intent.putExtra(
										"command",
										IDownloadInterface.REQUEST_COMMAND_PAUSE);
								intent.putExtra("url", task.url);
								TAApplication.getApplication().sendBroadcast(
										intent);
							}
						}
						notifyDataSetChanged();
					}
				});
			}
			title.setText(R.string.download_title_task);
			if (mDownloadingList.size() <= 0) {
				content.setVisibility(View.GONE);
			} else {
				content.setVisibility(View.VISIBLE);
			}
		} else {
			opera.setText("全部安装");
			opera.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mDownloadedList != null) {
						for (DownloadTask task : mDownloadedList) {
							String apkFileName = DownloadUtil
									.getCApkFileFromUrl(task.url);
							boolean isInstall = InstallingValidator
									.getInstance().isAppExist(
											TAApplication.getApplication(),
											task.packName);
							if (isInstall
									&& task.state != DownloadTask.DOWNLOADING
									&& task.state != DownloadTask.PAUSING
									&& task.state != DownloadTask.WAITING) {
							} else if (task.state == DownloadTask.INSTALLING) {
							} else if (FileUtil.isFileExist(apkFileName)) {
								try {
									File file = new File(apkFileName);
									Intent intent = new Intent();
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.setAction(android.content.Intent.ACTION_VIEW);
									intent.setDataAndType(Uri.fromFile(file),
											"application/vnd.android.package-archive");
									TAApplication.getApplication()
											.startActivity(intent);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			});
			title.setText(R.string.download_title_history);
			if (mDownloadedList.size() <= 0) {
				content.setVisibility(View.GONE);
			} else {
				content.setVisibility(View.VISIBLE);
			}
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.downloadlist_item, null);
		}
		if (childPosition == 0) {
			convertView.setPadding(0, 0, 0, 0);
		} else {
			convertView.setPadding(0,
					DrawUtil.dip2px(TAApplication.getApplication(), 19), 0, 0);
		}
		View gap_bottom = convertView.findViewById(R.id.gap_bottom);
		if (mDownloadingList != null && mDownloadingList.size() > 0
				&& mDownloadedList != null && mDownloadedList.size() > 0
				&& groupPosition == 0 && isLastChild) {
			gap_bottom.setVisibility(View.VISIBLE);
		} else {
			gap_bottom.setVisibility(View.GONE);
		}
		DownloadTask task = null;
		if (groupPosition == 0) {
			task = mDownloadingList.get(childPosition);
		} else {
			task = mDownloadedList.get(childPosition);
		}
		convertView.findViewById(R.id.recommend_layout_btn).setTag(task);
		convertView.findViewById(R.id.recommend_layout_btn).setOnClickListener(
				mItemClickListener);
		final ImageView image = (ImageView) convertView
				.findViewById(R.id.item_recommend_image);
		image.setTag(task.iconUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, task.iconUrl.hashCode() + "",
				task.iconUrl, true, true, new AsyncImageLoadedCallBack() {

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
		TextView name = (TextView) convertView
				.findViewById(R.id.item_recommend_appname);
		TextView speed = (TextView) convertView.findViewById(R.id.speed);
		TextView size = (TextView) convertView
				.findViewById(R.id.item_recommend_size);
		TextView currentSize = (TextView) convertView
				.findViewById(R.id.current_size);
		ProgressBar progress = (ProgressBar) convertView
				.findViewById(R.id.progress);
		if (groupPosition == 0) {
			progress.setVisibility(View.VISIBLE);
			progress.setProgress(task.alreadyDownloadPercent);
			size.setVisibility(View.VISIBLE);
			size.setText(Formatter.formatShortFileSize(
					TAApplication.getApplication(), task.size * 1024L));
			currentSize.setVisibility(View.VISIBLE);
			currentSize.setText(Formatter.formatShortFileSize(
					TAApplication.getApplication(), (long) (task.size * 1024.0
							* task.alreadyDownloadPercent / 100.0 + 0.5))
					+ "/");
		} else {
			progress.setVisibility(View.GONE);
			size.setVisibility(View.GONE);
			currentSize.setVisibility(View.GONE);
		}
		if (groupPosition == 0 && task.state == DownloadTask.DOWNLOADING) {
			speed.setVisibility(View.VISIBLE);
			speed.setText(task.speed + "KB/S");
		} else if (groupPosition == 0 && task.state == DownloadTask.PAUSING) {
			speed.setVisibility(View.VISIBLE);
			speed.setText("已暂停");
		} else if (groupPosition == 0 && task.state == DownloadTask.WAITING) {
			speed.setVisibility(View.VISIBLE);
			speed.setText("等待中");
		} else {
			speed.setVisibility(View.GONE);
		}
		name.setText(task.name);
		ImageView delete = (ImageView) convertView.findViewById(R.id.arrow);
		delete.setTag(task);
		delete.setOnClickListener(mDeleteClickListener);
		Button downloadBtn = (Button) convertView.findViewById(R.id.operator);
		initDownloadState(speed, downloadBtn, task);
		if (isLastChild) {
			convertView.findViewById(R.id.gap).setVisibility(View.INVISIBLE);
		} else {
			convertView.findViewById(R.id.gap).setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	/**
	 * 根据下载状态初始化下载按钮和点击事件
	 */
	private void initDownloadState(TextView info, Button downloadBtn,
			DownloadTask task) {
		String packName = task.packName;
		String apkFileName = DownloadUtil.getCApkFileFromUrl(task.url);
		boolean isInstall = InstallingValidator.getInstance().isAppExist(
				TAApplication.getApplication(), packName);
		if (isInstall && task.state != DownloadTask.DOWNLOADING
				&& task.state != DownloadTask.PAUSING
				&& task.state != DownloadTask.WAITING) {
			// 已安装
			downloadBtn.setText("打开");
			downloadBtn
					.setBackgroundResource(R.drawable.downloadmanager_open_btn_bg);
			Resources resource = TAApplication.getApplication().getResources();
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.downloadmanager_open_selector);
			if (csl != null) {
				downloadBtn.setTextColor(csl);// 设置按钮文字颜色
			}
			downloadBtn.setTag(packName);
			downloadBtn.setOnClickListener(mOpenAppClickListener);
			info.setVisibility(View.VISIBLE);
			info.setText("已安装");
		} else if (task.state == DownloadTask.INSTALLING) {
			// 正在安装
			downloadBtn.setText("安装中");
			downloadBtn
					.setBackgroundResource(R.drawable.downloadmanager_installing_btn_bg);
			downloadBtn.setTextColor(0xFF669900);// 设置按钮文字颜色
			downloadBtn.setOnClickListener(null);
			info.setVisibility(View.VISIBLE);
			info.setText("等待安装");
		} else if (FileUtil.isFileExist(apkFileName)) {
			// 已下载
			downloadBtn.setText("安装");
			downloadBtn
					.setBackgroundResource(R.drawable.downloadmanager_install_btn_bg);
			Resources resource = TAApplication.getApplication().getResources();
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.downloadmanager_install_selector);
			if (csl != null) {
				downloadBtn.setTextColor(csl);// 设置按钮文字颜色
			}
			downloadBtn.setTag(apkFileName);
			downloadBtn.setOnClickListener(mOpenApkClickListener);
			info.setVisibility(View.VISIBLE);
			info.setText("等待安装");
		} else if (task.state == DownloadTask.DOWNLOADING) {
			// 正在下载
			downloadBtn.setText("暂停");
			downloadBtn
					.setBackgroundResource(R.drawable.downloadmanager_pause_btn_bg);
			Resources resource = TAApplication.getApplication().getResources();
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.downloadmanager_pause_selector);
			if (csl != null) {
				downloadBtn.setTextColor(csl);// 设置按钮文字颜色
			}
			downloadBtn.setTag(task);
			downloadBtn.setOnClickListener(mPauseClickListener);
		} else if (task.state == DownloadTask.WAITING) {
			// 等待下载
			downloadBtn.setText("等待中");
			downloadBtn
					.setBackgroundResource(R.drawable.downloadmanager_waiting_btn_bg);
			downloadBtn.setTextColor(0xFF33b5e5);// 设置按钮文字颜色
			downloadBtn.setOnClickListener(null);
		} else if (task.state == DownloadTask.PAUSING) {
			// 已经暂停
			downloadBtn.setText("继续");
			downloadBtn
					.setBackgroundResource(R.drawable.downloadmanager_continue_btn_bg);
			Resources resource = TAApplication.getApplication().getResources();
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.downloadmanager_continue_selector);
			if (csl != null) {
				downloadBtn.setTextColor(csl);// 设置按钮文字颜色
			}
			downloadBtn.setTag(task);
			downloadBtn.setOnClickListener(mContinueClickListener);
		} else {
			// 下载未开始或下载失败
			downloadBtn.setText("下载");
			downloadBtn
					.setBackgroundResource(R.drawable.downloadmanager_download_btn_bg);
			Resources resource = TAApplication.getApplication().getResources();
			ColorStateList csl = (ColorStateList) resource
					.getColorStateList(R.color.downloadmanager_download_selector);
			if (csl != null) {
				downloadBtn.setTextColor(csl);// 设置按钮文字颜色
			}
			downloadBtn.setTag(task);
			downloadBtn.setOnClickListener(mDownloadClickListener);
		}
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	/**
	 * 更新列表数据并刷新列表
	 */
	public void update(List<DownloadTask> downloading, List<DownloadTask> finish) {
		if (downloading == null) {
			downloading = new ArrayList<DownloadTask>();
		}
		if (finish == null) {
			finish = new ArrayList<DownloadTask>();
		}
		mDownloadingList.clear();
		mDownloadedList.clear();
		mDownloadingList.addAll(downloading);
		mDownloadedList.addAll(finish);
		try {
			Collections.sort(mDownloadingList, new Comparator<DownloadTask>() {

				@Override
				public int compare(DownloadTask lhs, DownloadTask rhs) {
					return lhs.unique.compareTo(rhs.unique);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Collections.sort(mDownloadedList, new Comparator<DownloadTask>() {

				@Override
				public int compare(DownloadTask lhs, DownloadTask rhs) {
					return lhs.unique.compareTo(rhs.unique);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		notifyDataSetChanged();
	}
}

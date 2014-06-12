package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import com.zhidian.wifibox.view.ProgressBitmapDrawable;
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
	 * 打开操作界面的任务
	 */
	private Set<String> mOpen = new HashSet<String>();

	private Bitmap mProgressBitmap = ((BitmapDrawable) TAApplication
			.getApplication().getResources()
			.getDrawable(R.drawable.icon_loading1)).getBitmap();
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
	 * 箭头的点击事件，收起或展开操作界面
	 */
	private OnClickListener mArrowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			DownloadTask task = (DownloadTask) v.getTag();
			if (mOpen.contains(task.url)) {
				mOpen.remove(task.url);
			} else {
				mOpen.add(task.url);
			}
			notifyDataSetChanged();
		}
	};
	/**
	 * 重新下载点击事件
	 */
	private OnClickListener mRedownloadClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final DownloadTask task = (DownloadTask) v.getTag();
			mOpen.remove(task.url);
			notifyDataSetChanged();
			{
				Intent intent = new Intent(
						IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
				intent.putExtra("command",
						IDownloadInterface.REQUEST_COMMAND_REDOWNLOAD);
				intent.putExtra("url", task.url);
				intent.putExtra("iconUrl", task.iconUrl);
				intent.putExtra("name", task.name);
				intent.putExtra("size", task.size);
				intent.putExtra("packName", task.packName);
				intent.putExtra("appId", task.appId);
				intent.putExtra("version", task.version);
				TAApplication.getApplication().sendBroadcast(intent);
			}
		}
	};
	/**
	 * 删除点击事件
	 */
	private OnClickListener mDeleteClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			DownloadTask task = (DownloadTask) v.getTag();
			mOpen.remove(task.url);
			notifyDataSetChanged();
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command",
					IDownloadInterface.REQUEST_COMMAND_DELETE);
			intent.putExtra("url", task.url);
			TAApplication.getApplication().sendBroadcast(intent);
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
	 * 继续点击事件
	 */
	private OnClickListener mContinueClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			DownloadTask task = (DownloadTask) v.getTag();
			task.state = DownloadTask.DOWNLOADING;
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
			task.state = DownloadTask.DOWNLOADING;
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
		ImageView image = (ImageView) convertView.findViewById(R.id.image);
		View content = convertView.findViewById(R.id.content);
		if (groupPosition == 0) {
			title.setText(R.string.download_title_task);
			if (mDownloadingList.size() <= 0) {
				content.setVisibility(View.GONE);
			} else {
				content.setVisibility(View.VISIBLE);
			}
		} else {
			title.setText(R.string.download_title_history);
			if (mDownloadedList.size() <= 0) {
				content.setVisibility(View.GONE);
			} else {
				content.setVisibility(View.VISIBLE);
			}
		}
		if (isExpanded) {
			image.setImageResource(R.drawable.blue_down_icon_up);
		} else {
			image.setImageResource(R.drawable.blue_down_icon);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.downloadlist_item, null);
		}
		DownloadTask task = null;
		boolean last = false;
		if (groupPosition == 0) {
			task = mDownloadingList.get(childPosition);
			if (childPosition == mDownloadingList.size() - 1) {
				last = true;
			}
		} else {
			task = mDownloadedList.get(childPosition);
			if (childPosition == mDownloadedList.size() - 1) {
				last = true;
			}
		}
		convertView.findViewById(R.id.recommend_layout_btn).setTag(task);
		convertView.findViewById(R.id.recommend_layout_btn).setOnClickListener(
				mItemClickListener);
		if (last) {
			convertView.findViewById(R.id.bottom).setVisibility(View.VISIBLE);
		} else {
			convertView.findViewById(R.id.bottom).setVisibility(View.GONE);
		}
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
		if (groupPosition == 0) {
			speed.setVisibility(View.VISIBLE);
			speed.setText(task.speed + "KB/S");
		} else {
			speed.setVisibility(View.GONE);
		}
		name.setText(task.name);
		TextView size = (TextView) convertView
				.findViewById(R.id.item_recommend_size);
		size.setText(FileUtil.convertFileSize(task.size));
		ImageView arrow = (ImageView) convertView.findViewById(R.id.arrow);
		arrow.setTag(task);
		arrow.setOnClickListener(mArrowClickListener);
		View operator = convertView.findViewById(R.id.operator);
		View redownload = operator.findViewById(R.id.redownload);
		redownload.setTag(task);
		View delete = operator.findViewById(R.id.delete);
		delete.setTag(task);
		if (mOpen.contains(task.url)) {
			arrow.setImageResource(R.drawable.grey_down_icon_up);
			operator.setVisibility(View.VISIBLE);
		} else {
			arrow.setImageResource(R.drawable.grey_down_icon);
			operator.setVisibility(View.GONE);
		}
		redownload.setOnClickListener(mRedownloadClickListener);
		delete.setOnClickListener(mDeleteClickListener);
		Button downloadBtn = (Button) convertView
				.findViewById(R.id.item_recommend_download);
		initDownloadState(downloadBtn, task);
		return convertView;
	}

	/**
	 * 根据下载状态初始化下载按钮和点击事件
	 */
	private void initDownloadState(Button btnDownLoad, DownloadTask task) {
		String packName = task.packName;
		String apkFileName = DownloadUtil.getCApkFileFromUrl(task.url);
		boolean isInstall = InstallingValidator.getInstance().isAppExist(
				TAApplication.getApplication(), packName);
		if (isInstall && task.state != DownloadTask.DOWNLOADING
				&& task.state != DownloadTask.PAUSING) {
			// 已安装
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(
					null,
					TAApplication.getApplication().getResources()
							.getDrawable(R.drawable.icon_open), null, null);
			btnDownLoad.setTag(packName);
			btnDownLoad.setOnClickListener(mOpenAppClickListener);
			btnDownLoad.setText(R.string.btn_open);
		} else if (task.state == DownloadTask.INSTALLING) {
			// 正在安装
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(
					null,
					TAApplication.getApplication().getResources()
							.getDrawable(R.drawable.icon_install), null, null);
			btnDownLoad.setTag(null);
			btnDownLoad.setOnClickListener(null);
			btnDownLoad.setText(R.string.installing);
		} else if (FileUtil.isFileExist(apkFileName)) {
			// 已下载
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(
					null,
					TAApplication.getApplication().getResources()
							.getDrawable(R.drawable.icon_install), null, null);
			btnDownLoad.setTag(apkFileName);
			btnDownLoad.setOnClickListener(mOpenApkClickListener);
			btnDownLoad.setText(R.string.btn_install);
		} else if (task.state == DownloadTask.DOWNLOADING) {
			// 正在下载
			// 根据具体的下载进度设置按钮图标
			Drawable drawable = new ProgressBitmapDrawable(TAApplication
					.getApplication().getResources(), mProgressBitmap,
					task.alreadyDownloadPercent, 0xFF888888, 0xFF0a78ee);
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, drawable,
					null, null);
			btnDownLoad.setTag(task);
			btnDownLoad.setOnClickListener(mPauseClickListener);
			btnDownLoad.setText(task.alreadyDownloadPercent + "%");
		} else if (task.state == DownloadTask.PAUSING) {
			// 已经暂停
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(
					null,
					TAApplication.getApplication().getResources()
							.getDrawable(R.drawable.icon_continue), null, null);
			btnDownLoad.setTag(task);
			btnDownLoad.setOnClickListener(mContinueClickListener);
			btnDownLoad.setText(R.string.btn_goon);
		} else {
			// 下载未开始或下载失败
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(
					null,
					TAApplication.getApplication().getResources()
							.getDrawable(R.drawable.icon_download), null, null);
			btnDownLoad.setTag(task);
			btnDownLoad.setOnClickListener(mDownloadClickListener);
			btnDownLoad.setText(R.string.btn_download);
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
		notifyDataSetChanged();
	}

}

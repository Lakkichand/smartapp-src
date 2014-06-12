package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.XAppDataBean;
import com.zhidian.wifibox.data.XMustDataBean;
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
 * 极速模式装机必备页面数据适配器
 */
public class XMustAdapter extends BaseExpandableListAdapter {

	private LayoutInflater inflater = LayoutInflater.from(TAApplication
			.getApplication());

	private List<XMustDataBean> mList = new ArrayList<XMustDataBean>();
	private Bitmap mProgressBitmap = ((BitmapDrawable) TAApplication
			.getApplication().getResources()
			.getDrawable(R.drawable.icon_loading1)).getBitmap();

	private Handler mHandler;

	public XMustAdapter(Handler handler) {
		mHandler = handler;
	}

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
	/**
	 * 列表项点击事件
	 */
	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			XAppDataBean bean = (XAppDataBean) v.getTag();
			if (bean.isSelect) {
				bean.isSelect = false;
			} else {
				String packName = bean.packageName;
				String apkFileName = DownloadUtil
						.getXApkFileFromUrl(bean.downPath);
				boolean isInstall = InstallingValidator.getInstance()
						.isAppExist(TAApplication.getApplication(), packName);
				if (isInstall) {
					bean.isSelect = false;
					// 打开应用
					try {
						PackageManager packageManager = TAApplication
								.getApplication().getPackageManager();
						Intent intent = packageManager
								.getLaunchIntentForPackage(packName);
						TAApplication.getApplication().startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (FileUtil.isFileExist(apkFileName)) {
					bean.isSelect = false;
					// 安装应用
					try {
						File file = new File(apkFileName);
						Intent intent = new Intent();
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setAction(android.content.Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.fromFile(file),
								"application/vnd.android.package-archive");
						TAApplication.getApplication().startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (bean.downloadStatus == DownloadTask.DOWNLOADING) {
					bean.isSelect = false;
					// 暂停下载
					bean.downloadStatus = DownloadTask.PAUSING;
					Intent intent = new Intent(
							IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
					intent.putExtra("command",
							IDownloadInterface.REQUEST_COMMAND_PAUSE);
					intent.putExtra("url", bean.downPath);
					TAApplication.getApplication().sendBroadcast(intent);
				} else {
					bean.isSelect = true;
				}
			}
			notifyDataSetChanged();
			calculateAppSize();
		}
	};
	/**
	 * 全选箭头点击事件
	 */
	private OnClickListener mToggleClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int index = (Integer) v.getTag();
			XMustDataBean mbean = mList.get(index);
			List<XAppDataBean> list = mbean.mAppList;
			boolean allSelect = true;
			for (XAppDataBean bean : list) {
				String packName = bean.packageName;
				String apkFileName = DownloadUtil
						.getXApkFileFromUrl(bean.downPath);
				boolean isInstall = InstallingValidator.getInstance()
						.isAppExist(TAApplication.getApplication(), packName);
				if (isInstall || FileUtil.isFileExist(apkFileName)
						|| bean.downloadStatus == DownloadTask.DOWNLOADING) {
				} else {
					if (!bean.isSelect) {
						allSelect = false;
						break;
					}
				}
			}
			if (allSelect) {
				for (XAppDataBean bean : list) {
					bean.isSelect = false;
				}
			} else {
				for (XAppDataBean bean : list) {
					String packName = bean.packageName;
					String apkFileName = DownloadUtil
							.getXApkFileFromUrl(bean.downPath);
					boolean isInstall = InstallingValidator.getInstance()
							.isAppExist(TAApplication.getApplication(),
									packName);
					if (isInstall || FileUtil.isFileExist(apkFileName)
							|| bean.downloadStatus == DownloadTask.DOWNLOADING) {
						bean.isSelect = false;
					} else {
						bean.isSelect = true;
					}
				}
			}
			notifyDataSetChanged();
			calculateAppSize();
		}
	};

	/**
	 * 标题弹开时字体的颜色
	 */
	private int mGroupTitleOpenColor = TAApplication.getApplication()
			.getResources().getColor(R.color.white);
	/**
	 * 标题关闭时字体的颜色
	 */
	private int mGroupTitleCloseColor = TAApplication.getApplication()
			.getResources().getColor(R.color.black);
	/**
	 * 标题关闭时背景的颜色
	 */
	private int mGroupBackGroundCloseColor = TAApplication.getApplication()
			.getResources().getColor(R.color.gray_qian);

	@Override
	public int getGroupCount() {
		return mList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mList.get(groupPosition).mAppList.size();
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
		GroupViewHolder holder;
		if (convertView == null) {
			holder = new GroupViewHolder();
			convertView = inflater
					.inflate(R.layout.speed_list_group_view, null);
			holder.toggleBtn = (ImageView) convertView
					.findViewById(R.id.speed_toggle);
			holder.tvGroupName = (TextView) convertView
					.findViewById(R.id.speed_group_name);
			holder.ivSign = (ImageView) convertView
					.findViewById(R.id.speed_group_indicator);
			holder.rLayout = (FrameLayout) convertView
					.findViewById(R.id.group_item_layout);
			holder.btn = (RelativeLayout) convertView
					.findViewById(R.id.list_group_but);
			holder.gap = convertView.findViewById(R.id.gap);
			holder.arrow = (ImageView) convertView
					.findViewById(R.id.arrow_down);
			
			convertView.setTag(holder);
		} else {
			holder = (GroupViewHolder) convertView.getTag();
		}
		holder.tvGroupName.setText(mList.get(groupPosition).name);
		if (isExpanded) {// 已展开
			mList.get(groupPosition).status = 1;
			holder.ivSign.setImageResource(R.drawable.speed_open_up);
			holder.btn.setBackgroundColor(0xFF8fc31f);
			holder.tvGroupName.setTextColor(mGroupTitleOpenColor);
			holder.gap.setVisibility(View.INVISIBLE);
			holder.arrow.setVisibility(View.VISIBLE);
			//convertView.findViewById(R.id.line).setVisibility(View.VISIBLE);
		} else {
			mList.get(groupPosition).status = 0;
			holder.ivSign.setImageResource(R.drawable.speed_shous);
			holder.btn.setBackgroundColor(mGroupBackGroundCloseColor);
			holder.tvGroupName.setTextColor(mGroupTitleCloseColor);
			holder.gap.setVisibility(View.VISIBLE);
			holder.arrow.setVisibility(View.GONE);
			//convertView.findViewById(R.id.line).setVisibility(View.GONE);
		}
		List<XAppDataBean> list = mList.get(groupPosition).mAppList;
		boolean select = false;
		for (XAppDataBean bean : list) {
			if (bean.isSelect) {
				select = true;
				break;
			}
		}
		boolean allSelect = true;
		for (XAppDataBean bean : list) {
			String packName = bean.packageName;
			String apkFileName = DownloadUtil.getXApkFileFromUrl(bean.downPath);
			boolean isInstall = InstallingValidator.getInstance().isAppExist(
					TAApplication.getApplication(), packName);
			if (isInstall || FileUtil.isFileExist(apkFileName)
					|| bean.downloadStatus == DownloadTask.DOWNLOADING) {
			} else {
				if (!bean.isSelect) {
					allSelect = false;
					break;
				}
			}
		}
		holder.toggleBtn.setTag(groupPosition);
		holder.toggleBtn.setOnClickListener(mToggleClickListener);
		if (select && allSelect) {
			holder.toggleBtn.setImageResource(R.drawable.speed_sele);
			holder.toggleBtn.setBackgroundColor(TAApplication.getApplication()
					.getResources().getColor(R.color.speed_cyan_s));
		} else if (select) {
			holder.toggleBtn.setImageResource(R.drawable.speed_sele_half);
			holder.toggleBtn.setBackgroundColor(TAApplication.getApplication()
					.getResources().getColor(R.color.gray_s));
		} else {
			holder.toggleBtn.setImageResource(R.drawable.speed_sele_no);
			holder.toggleBtn.setBackgroundColor(TAApplication.getApplication()
					.getResources().getColor(R.color.gray_s));
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ChildViewHolder holder;
		if (convertView == null) {
			holder = new ChildViewHolder();
			convertView = inflater.inflate(R.layout.speed_item_new, null);
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
			holder.must_on = (View) convertView.findViewById(R.id.must_on);
			holder.downloadArea = convertView.findViewById(R.id.download_area);

			convertView.setTag(holder);
		} else {
			holder = (ChildViewHolder) convertView.getTag();
		}
		// 左边
		XAppDataBean leftBean = mList.get(groupPosition).mAppList
				.get(childPosition);
		final ImageView leftImage = holder.ivPhoto;
		leftImage.setTag(leftBean);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, leftBean.iconPath.hashCode() + "",
				leftBean.iconPath, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						XAppDataBean bean = (XAppDataBean) leftImage.getTag();
						if (bean.iconPath.equals(imgUrl)) {
							if (bean.isSelect) {
								leftImage.setImageBitmap(DrawUtil
										.createProgressBitmap(
												TAApplication.getApplication(),
												imageBitmap, 0));
							} else {
								leftImage.setImageBitmap(DrawUtil
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
		holder.tvApkName.setText(leftBean.name);
		holder.tvExplain.setText(Html.fromHtml(HtmlRegexpUtil.filterimgHtml(leftBean.explain)));
		if (leftBean.downloadStatus == DownloadTask.DOWNLOADING) {
			holder.speed.setVisibility(View.VISIBLE);
			holder.tvSize.setVisibility(View.GONE);
			holder.speed.setText(leftBean.speed + "KB/S");
		} else {
			holder.speed.setVisibility(View.GONE);
			holder.tvSize.setVisibility(View.VISIBLE);
		}
		holder.ratingBar.setRating(leftBean.score / 2.0f);
		holder.tvSize.setText(FileUtil.convertFileSize(leftBean.size));
		holder.tvdownloadTime.setText(FileUtil
				.convertDownloadTimes(leftBean.downloads));
		initDownloadState(holder.ivDownload, holder.tvDownload, leftBean,
				holder.downloadArea);
		holder.must_on.setTag(leftBean);
		holder.must_on.setOnClickListener(mItemClickListener);javascript:;
		if (leftBean.isSelect) { 
			holder.ivSelect.setVisibility(View.VISIBLE);
			leftImage.setImageBitmap(DrawUtil.createProgressBitmap(
					TAApplication.getApplication(), bm, 0));
		} else {
			holder.ivSelect.setVisibility(View.GONE);
			leftImage.setImageBitmap(DrawUtil.createProgressBitmap(
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

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	/**
	 * 根据已选的应用计算下载大小和下载时间
	 */
	private void calculateAppSize() {
		mHandler.sendEmptyMessage(-1);
	}

	/**
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<XMustDataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

	static class GroupViewHolder {
		ImageView toggleBtn;
		TextView tvGroupName; // 组名
		ImageView ivSign;
		FrameLayout rLayout;
		RelativeLayout btn; // 点击事件
		View gap;
		ImageView arrow;		
	}

	static class ChildViewHolder {
		ImageView ivPhoto;
		ImageView ivSelect;
		ImageView ivDownload;
		TextView tvApkName;
		TextView speed;
		TextView tvSize;
		TextView tvDownload;
		View must_on;
		View downloadArea;
		TextView tvdownloadTime; //下载次数
		RatingBar ratingBar; // 星级评分
		TextView tvExplain; //简介
	}

}

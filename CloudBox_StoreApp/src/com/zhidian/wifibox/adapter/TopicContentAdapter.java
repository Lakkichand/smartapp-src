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

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AppDetailActivity;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.data.PageDataBean;
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
 * 专题详情页数据适配器
 * 
 * @author xiedezhi
 * 
 */
public class TopicContentAdapter extends SectionedBaseAdapter {

	private List<AppDataBean> mList = new ArrayList<AppDataBean>();
	/**
	 * 图片管理器
	 */
	private Context mContext;
	private LayoutInflater mInflater;
	private Bitmap mProgressBitmap;
	private Bitmap mProgressBitmapGoon;
	private allDownloadClickListener downloadClickListener;
	private PageDataBean bean = new PageDataBean();
	/**
	 * 统计标题
	 */
	public String mStatisticsTitle = "";
	/**
	 * 浮在顶部的view
	 */
	private View mView;

	public interface allDownloadClickListener {
		void onClick();
	}

	public void setPageDataBena(PageDataBean bean) {
		if (bean != null) {
			this.bean = bean;
			notifyDataSetChanged();
		}
	}

	public void setDownloadClick(allDownloadClickListener downloadClickListener) {
		if (downloadClickListener != null) {
			this.downloadClickListener = downloadClickListener;
		}
	}

	/**
	 * 跳转详情点击监听
	 */
	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// long id = (Long) v.getTag();
			AppDataBean bean = (AppDataBean) v.getTag();
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
			intent.putExtra("page", mStatisticsTitle);
			mContext.sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};

	public TopicContentAdapter(Context context) {
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		mProgressBitmap = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.icon_loading1)).getBitmap();
		mProgressBitmapGoon = ((BitmapDrawable) context.getResources()
				.getDrawable(R.drawable.icon_continue)).getBitmap();
	}

	/**
	 * 根据下载状态初始化下载按钮和点击事件
	 */
	private void initDownloadState(LinearLayout btnDownLoad, TextView tvStatus,
			ImageView ivIcon, AppDataBean bean) {
		String packName = bean.packName;
		String apkFileName = DownloadUtil.getCApkFileFromUrl(bean.downloadUrl);
		boolean isInstall = InstallingValidator.getInstance().isAppExist(
				mContext, packName);
		if (isInstall) {
			// 已安装
			ivIcon.setImageResource(R.drawable.icon_open);
			tvStatus.setText(R.string.btn_open);
			tvStatus.setTextColor(TAApplication.getApplication().getResources()
					.getColor(R.color.download_status_blue));
			btnDownLoad.setTag(packName);
			btnDownLoad.setOnClickListener(mOpenAppClickListener);
		} else if (bean.downloadStatus == DownloadTask.INSTALLING) {
			// 正在安装
			ivIcon.setImageResource(R.drawable.icon_install);
			tvStatus.setText(R.string.installing);
			tvStatus.setTextColor(TAApplication.getApplication().getResources()
					.getColor(R.color.download_status_qing));
			btnDownLoad.setTag(null);
			btnDownLoad.setOnClickListener(null);
		} else if (FileUtil.isFileExist(apkFileName)) {
			// 已下载
			ivIcon.setImageResource(R.drawable.icon_install);
			tvStatus.setText(R.string.btn_install);
			tvStatus.setTextColor(TAApplication.getApplication().getResources()
					.getColor(R.color.download_status_qing));

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
			tvStatus.setTextColor(TAApplication.getApplication().getResources()
					.getColor(R.color.download_status_ing));
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mPauseClickListener);
		} else if (bean.downloadStatus == DownloadTask.WAITING) {
			// 等待下载
			ivIcon.setImageResource(R.drawable.wait);
			tvStatus.setText(R.string.btn_waiting);
			tvStatus.setTextColor(TAApplication.getApplication().getResources()
					.getColor(R.color.download_status_wait));
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(null);
		} else if (bean.downloadStatus == DownloadTask.PAUSING) {
			// 已经暂停
			Drawable drawable = new ProgressBitmapDrawable(TAApplication
					.getApplication().getResources(), mProgressBitmapGoon,
					bean.alreadyDownloadPercent, 0xFFFFBB33, 0xFFFF8800);
			ivIcon.setImageDrawable(drawable);
			tvStatus.setText(R.string.btn_goon);
			tvStatus.setTextColor(TAApplication.getApplication().getResources()
					.getColor(R.color.download_status_goon));
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mContinueClickListener);
		} else {
			// 下载未开始或下载失败
			ivIcon.setImageResource(R.drawable.icon_download);
			tvStatus.setText(R.string.btn_download);
			tvStatus.setTextColor(TAApplication.getApplication().getResources()
					.getColor(R.color.download_status_wait));

			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mDownloadClickListener);
		}
	}

	/**
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<AppDataBean> list, String title) {
		mStatisticsTitle = title;
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
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_recommend_app, null);
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
		AppDataBean bean = mList.get(position);
		holder.tvAppSize.setText(FileUtil.convertFileSize(bean.size));
		holder.tvAppName.setText(bean.name);
		holder.tvDownloadTime.setText(FileUtil
				.convertDownloadTimes(bean.downloads));
		holder.tvAppDescribe.setText(Html.fromHtml(bean.explain));
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
		initDownloadState(holder.btnDownLoad, holder.tvStatusText,
				holder.ivStatusImg, bean);
		return convertView;
	}

	@Override
	public View getSectionHeaderView(int section, View convertView,
			ViewGroup parent) {
		SectionViewHolder holder;
		if (convertView == null) {
			holder = new SectionViewHolder();
			convertView = mInflater.inflate(R.layout.view_topiccontent_tip,
					null);
			holder.tvDecription = (TextView) convertView
					.findViewById(R.id.decription_tv);
			holder.tvHotName = (TextView) convertView
					.findViewById(R.id.hot_apps_tv);
			holder.tvTotal = (TextView) convertView
					.findViewById(R.id.app_no_tv_total);
			holder.tvSize = (TextView) convertView
					.findViewById(R.id.app_no_tv_size);
			holder.btnDownload = (Button) convertView
					.findViewById(R.id.download_iv);

			convertView.setTag(holder);

		} else {
			holder = (SectionViewHolder) convertView.getTag();
		}

		holder.tvHotName.setText(bean.titleMessage);
		holder.tvTotal.setText(bean.amount + "款应用");
		holder.tvSize.setText("大小：" + bean.AppSize);
		holder.tvDecription.setText(bean.detailMessage);
		holder.btnDownload.setOnClickListener(mClickListener);

		mView = convertView;
		return convertView;
	}

	/**
	 * 获取导航布局
	 */
	public View getLayout() {
		return mView;
	}

	public void setTouch() {
		if (mView == null) {
			return;
		}

		SectionViewHolder holder = (SectionViewHolder) mView.getTag();
		holder.btnDownload.setOnTouchListener(mTouchListener);
		holder.btnDownload.setOnClickListener(null);
	}

	public void setClick() {
		if (mView == null) {
			return;
		}

		SectionViewHolder holder = (SectionViewHolder) mView.getTag();
		holder.btnDownload.setOnTouchListener(null);
		holder.btnDownload.setOnClickListener(mClickListener);
	}

	/**
	 * 浮动view 点击事件
	 */
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.download_iv:
				if (downloadClickListener != null) {
					downloadClickListener.onClick();
				}
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
			case R.id.download_iv:
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					if (downloadClickListener != null) {
						downloadClickListener.onClick();
					}
				}

				break;

			default:
				break;
			}
			return false;
		}

	};

	static class SectionViewHolder {
		TextView tvHotName;// 专题名称
		Button btnDownload; // 下载全部
		TextView tvDecription; // 描述
		TextView tvTotal; // 应用个数
		TextView tvSize; // 应用大小
	}

}

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
import android.text.TextUtils;
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
import com.zhidian.wifibox.data.XAppDataBean;
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
 * 极速下载：装机必备适配器
 * 
 * @author xiedezhi
 * 
 */
public class SpeedingMandatoryAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	private List<SpeedingMandatoryDataBean> mList = new ArrayList<SpeedingMandatoryAdapter.SpeedingMandatoryDataBean>();

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

	private OnClickListener mAppClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// do nothing
		}
	};

	public SpeedingMandatoryAdapter(Context context) {
		inflater = LayoutInflater.from(context);
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
			convertView = inflater.inflate(R.layout.home_feature_item, null);
			holder = new ViewHolder();
			holder.mTitleFrame = (LinearLayout) convertView
					.findViewById(R.id.title);
			holder.mTitle = (TextView) convertView
					.findViewById(R.id.title_text);
			holder.mSubTitle = (TextView) convertView
					.findViewById(R.id.subTitle);
			holder.mTitleGap = convertView.findViewById(R.id.title_gap);
			holder.mApp1 = (LinearLayout) convertView.findViewById(R.id.app1);
			holder.mIcon1 = (ImageView) convertView.findViewById(R.id.icon1);
			holder.mName1 = (TextView) convertView.findViewById(R.id.name1);
			holder.mDownload1 = (LinearLayout) convertView
					.findViewById(R.id.download1);
			holder.mButton1 = (Button) convertView.findViewById(R.id.button1);
			holder.mBar1 = (ProgressBar) convertView
					.findViewById(R.id.progress1);
			holder.mProgressText1 = (TextView) convertView
					.findViewById(R.id.progress_text1);
			holder.mGap1 = convertView.findViewById(R.id.gap1);
			holder.mApp2 = (LinearLayout) convertView.findViewById(R.id.app2);
			holder.mIcon2 = (ImageView) convertView.findViewById(R.id.icon2);
			holder.mName2 = (TextView) convertView.findViewById(R.id.name2);
			holder.mDownload2 = (LinearLayout) convertView
					.findViewById(R.id.download2);
			holder.mButton2 = (Button) convertView.findViewById(R.id.button2);
			holder.mBar2 = (ProgressBar) convertView
					.findViewById(R.id.progress2);
			holder.mProgressText2 = (TextView) convertView
					.findViewById(R.id.progress_text2);
			holder.mGap2 = convertView.findViewById(R.id.gap2);
			holder.mApp3 = (LinearLayout) convertView.findViewById(R.id.app3);
			holder.mIcon3 = (ImageView) convertView.findViewById(R.id.icon3);
			holder.mName3 = (TextView) convertView.findViewById(R.id.name3);
			holder.mDownload3 = (LinearLayout) convertView
					.findViewById(R.id.download3);
			holder.mButton3 = (Button) convertView.findViewById(R.id.button3);
			holder.mBar3 = (ProgressBar) convertView
					.findViewById(R.id.progress3);
			holder.mProgressText3 = (TextView) convertView
					.findViewById(R.id.progress_text3);
			holder.mGap3 = convertView.findViewById(R.id.gap3);
			holder.mApp4 = (LinearLayout) convertView.findViewById(R.id.app4);
			holder.mIcon4 = (ImageView) convertView.findViewById(R.id.icon4);
			holder.mName4 = (TextView) convertView.findViewById(R.id.name4);
			holder.mDownload4 = (LinearLayout) convertView
					.findViewById(R.id.download4);
			holder.mButton4 = (Button) convertView.findViewById(R.id.button4);
			holder.mBar4 = (ProgressBar) convertView
					.findViewById(R.id.progress4);
			holder.mProgressText4 = (TextView) convertView
					.findViewById(R.id.progress_text4);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		SpeedingMandatoryDataBean bean = mList.get(position);
		if (TextUtils.isEmpty(bean.mTitle)) {
			holder.mTitleFrame.setVisibility(View.GONE);
			holder.mTitleGap.setVisibility(View.GONE);
		} else {
			holder.mTitleFrame.setVisibility(View.VISIBLE);
			holder.mTitleGap.setVisibility(View.VISIBLE);
			holder.mTitle.setText(bean.mTitle);
			holder.mSubTitle.setVisibility(View.INVISIBLE);
		}
		initApp(bean.mBean1, holder.mApp1, holder.mDownload1, holder.mIcon1,
				holder.mName1, holder.mButton1, holder.mBar1,
				holder.mProgressText1, holder.mGap1);
		initApp(bean.mBean2, holder.mApp2, holder.mDownload2, holder.mIcon2,
				holder.mName2, holder.mButton2, holder.mBar2,
				holder.mProgressText2, holder.mGap2);
		initApp(bean.mBean3, holder.mApp3, holder.mDownload3, holder.mIcon3,
				holder.mName3, holder.mButton3, holder.mBar3,
				holder.mProgressText3, holder.mGap3);
		initApp(bean.mBean4, holder.mApp4, holder.mDownload4, holder.mIcon4,
				holder.mName4, holder.mButton4, holder.mBar4,
				holder.mProgressText4, null);
		return convertView;
	}

	/**
	 * 初始化应用信息
	 */
	private void initApp(XAppDataBean bean, LinearLayout app,
			LinearLayout download, final ImageView icon, TextView name,
			Button button, ProgressBar progress, TextView progressText, View gap) {
		if (bean == null) {
			app.setVisibility(View.INVISIBLE);
			if (gap != null) {
				gap.setVisibility(View.INVISIBLE);
			}
			return;
		}
		if (gap != null) {
			gap.setVisibility(View.VISIBLE);
		}
		app.setVisibility(View.VISIBLE);
		app.setOnClickListener(mAppClickListener);
		icon.setTag(bean.iconPath);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, bean.iconPath.hashCode() + "",
				bean.iconPath, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						if (icon.getTag().equals(imgUrl)) {
							icon.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm == null) {
			bm = DrawUtil.sDefaultIcon;
		}
		icon.setImageBitmap(bm);
		name.setText(bean.name);
		initDownloadState(app, download, button, progress, progressText, bean);
	}

	/**
	 * 根据下载状态初始化下载按钮和点击事件
	 */
	private void initDownloadState(LinearLayout app, LinearLayout download,
			Button btnDownLoad, ProgressBar progress, TextView progressText,
			XAppDataBean bean) {
		String packName = bean.packageName;
		String apkFileName = DownloadUtil.getCApkFileFromUrl(bean.downPath);
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
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<SpeedingMandatoryDataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

	public static class SpeedingMandatoryDataBean {
		public String mTitle;

		public XAppDataBean mBean1;

		public XAppDataBean mBean2;

		public XAppDataBean mBean3;

		public XAppDataBean mBean4;
	}

	static class ViewHolder {
		LinearLayout mTitleFrame;
		TextView mTitle;
		TextView mSubTitle;
		View mTitleGap;
		LinearLayout mApp1;
		ImageView mIcon1;
		TextView mName1;
		LinearLayout mDownload1;
		Button mButton1;
		ProgressBar mBar1;
		TextView mProgressText1;
		View mGap1;
		LinearLayout mApp2;
		ImageView mIcon2;
		TextView mName2;
		LinearLayout mDownload2;
		Button mButton2;
		ProgressBar mBar2;
		TextView mProgressText2;
		View mGap2;
		LinearLayout mApp3;
		ImageView mIcon3;
		TextView mName3;
		LinearLayout mDownload3;
		Button mButton3;
		ProgressBar mBar3;
		TextView mProgressText3;
		View mGap3;
		LinearLayout mApp4;
		ImageView mIcon4;
		TextView mName4;
		LinearLayout mDownload4;
		Button mButton4;
		ProgressBar mBar4;
		TextView mProgressText4;
	}

}

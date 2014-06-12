package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.APKInfo;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.listener.ApkScaningCallBackListener;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * apk安装包adapter
 * 
 * @author zhaoyl
 * 
 */
public class ApkInfoAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<APKInfo> mList = new ArrayList<APKInfo>();
	private ApkScaningCallBackListener listener;
	/**
	 * 打开操作界面的任务
	 */
	private Set<String> mOpen = new HashSet<String>();

	public ApkInfoAdapter(Context context, ApkScaningCallBackListener listener) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		this.listener = listener;
	}

	/**
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<APKInfo> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

	public void update(APKInfo info) {
		mList.add(info);
		notifyDataSetChanged();
	}

	public List<APKInfo> getData() {
		return mList;

	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_apkmanage, null);
			holder = new ViewHolder();
			holder.ivSelect = (ImageView) convertView
					.findViewById(R.id.apkmanage_select);
			holder.ivAvater = (ImageView) convertView
					.findViewById(R.id.apkmanage_image);
			holder.tvapkname = (TextView) convertView
					.findViewById(R.id.apkmanage_appname);
			holder.tvIsInstall = (TextView) convertView
					.findViewById(R.id.apkmanage_isinstall);
			holder.tvSize = (TextView) convertView
					.findViewById(R.id.apkmanage_size);
			holder.arrow = (ImageView) convertView
					.findViewById(R.id.apkmanage_arrow);
			holder.install = (RelativeLayout) convertView
					.findViewById(R.id.apkmanage_install);
			holder.clear = (RelativeLayout) convertView
					.findViewById(R.id.apkmanage_clear);
			holder.operator = (View) convertView.findViewById(R.id.operator);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		APKInfo info = mList.get(position);
		convertView.setTag(R.string.clear, info);
		String packName = info.getPackname();
		if (mOpen.contains(packName)) {
			holder.arrow.setImageResource(R.drawable.grey_down_icon_up);
			holder.operator.setVisibility(View.VISIBLE);
		} else {
			holder.arrow.setImageResource(R.drawable.grey_down_icon);
			holder.operator.setVisibility(View.GONE);
		}

		holder.arrow.setTag(packName);
		holder.arrow.setOnClickListener(mArrowClickListener);
		holder.clear.setTag(info);
		holder.clear.setOnClickListener(mDeleteClickListener);

		if (info.isSelect) {
			holder.ivSelect.setImageResource(R.drawable.check_box_checked);
		} else {
			holder.ivSelect.setImageResource(R.drawable.check_box_default);
		}

		if (info.isDamage()) {// 表示此apk安装包已损坏
			holder.tvapkname.setText(info.getPackname());
			holder.tvIsInstall.setVisibility(View.GONE);
			holder.tvSize.setText("已损坏");
			holder.ivAvater.setImageBitmap(DrawUtil.sDefaultIcon);
			holder.install.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Toast.makeText(mContext, "此安装包已损坏，无法安装！",
							Toast.LENGTH_SHORT).show();
				}
			});
		} else {
			holder.tvIsInstall.setVisibility(View.VISIBLE);
			holder.tvapkname.setText(info.getAppname());
			holder.tvSize.setText(formatSize(info.getSize()));
			holder.ivAvater.setImageDrawable(info.getIcon());
			switch (info.getIsInstall()) {
			case 0:// 已安装
				holder.tvIsInstall.setText("（已安装）");
				break;
			case 1: // 未安装
				holder.tvIsInstall.setText("（未安装）");
				break;
			case 2:// 已安装
				holder.tvIsInstall.setText("（已安装）");
				break;
			default:
				break;
			}

			holder.install.setTag(info.getPath());
			holder.install.setOnClickListener(mInstallClickListener);

			// final ImageView image = holder.ivAvater;
			//
			// /**************** 加载头像begin ****************/
			// image.setTag(packName);
			// Bitmap bm = AsyncImageManager.getInstance().loadIcon(packName,
			// true,
			// true, new AsyncImageLoadedCallBack() {
			//
			// @Override
			// public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
			// if (imageBitmap == null) {
			// return;
			// }
			// if (image.getTag().equals(imgUrl)) {
			// image.setImageBitmap(imageBitmap);
			// }
			// }
			// });
			// if (bm != null) {
			// image.setImageBitmap(bm);
			// } else {
			// // 默认
			// image.setImageBitmap(DrawUtil.sDefaultIcon);
			// }
			// /**************** 加载头像end ****************/
			//
		}

		return convertView;
	}

	/**
	 * 箭头的点击事件，收起或展开操作界面
	 */
	private OnClickListener mArrowClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String packName = (String) v.getTag();
			if (mOpen.contains(packName)) {
				mOpen.remove(packName);
			} else {
				mOpen.add(packName);
			}
			notifyDataSetChanged();
		}
	};

	/**
	 * 安装点击事件
	 */
	private OnClickListener mInstallClickListener = new OnClickListener() {

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
	 * 删除点击事件
	 */
	private OnClickListener mDeleteClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				APKInfo info = (APKInfo) v.getTag();
				File f = new File(info.getPath());
				if (f.exists()) {
					f.delete();
					if (listener != null) {
						listener.callback(info);
					}					
					mList.remove(info);
					notifyDataSetChanged();
					if (listener != null && mList.size() <= 0) {
						listener.nowback();
					}
					Toast.makeText(mContext, "清理成功", Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	// 格式化 转化为.MB格式
	private String formatSize(long size) {
		return Formatter.formatFileSize(mContext, size);
	}

	static class ViewHolder {
		ImageView ivSelect; // 选择
		ImageView ivAvater; // 头像
		TextView tvapkname; // 名称
		TextView tvIsInstall; // 是否已安装
		TextView tvSize; // 大小
		ImageView arrow; // 显示
		RelativeLayout install; // 安装
		RelativeLayout clear; // 清理
		View operator;

	}

}

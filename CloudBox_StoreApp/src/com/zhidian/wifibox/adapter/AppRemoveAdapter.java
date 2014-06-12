package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.AppInfo;
import com.zhidian.wifibox.listener.AppsizeListener;
import com.zhidian.wifibox.listener.AsyncAppDisableCallBack;
import com.zhidian.wifibox.root.RootShell;
import com.zhidian.wifibox.util.AppInfoProvider;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.SystemAppUtil;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * app卸载adapter
 * 
 * @author zhaoyl
 * 
 */
public class AppRemoveAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private Context mContext;
	private List<AppInfo> mList = new ArrayList<AppInfo>();
	private AppInfoProvider provider;
	private Handler handler;
	private AsyncAppDisableCallBack loadedCallBack;

	public AppRemoveAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		provider = new AppInfoProvider(context);
		handler = new Handler();
	}
	
	public AppRemoveAdapter(Context context, AsyncAppDisableCallBack loadedCallBack) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		provider = new AppInfoProvider(context);
		handler = new Handler();
		this.loadedCallBack = loadedCallBack;
	}

	/**
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<AppInfo> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_app_remove, null);
			holder = new ViewHolder();
			holder.ivAppImage = (ImageView) convertView
					.findViewById(R.id.app_logo);
			holder.tvAppSize = (TextView) convertView
					.findViewById(R.id.new_size_tv);
			holder.tvAppName = (TextView) convertView
					.findViewById(R.id.app_name_tv);
			holder.isSelectImage = (ImageView) convertView
					.findViewById(R.id.app_remove_select);
			holder.downloadArea = convertView.findViewById(R.id.aa);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final AppInfo info = mList.get(position);
		convertView.setTag(R.string.remove, info);
		String appName = info.getAppname();
		final String packname = info.getPackname();
		holder.tvAppName.setText(appName);
		// holder.ivAppImage.setImageDrawable(info.getIcon());
		final ImageView image = holder.ivAppImage;

		/**************** 加载头像begin ****************/
		image.setTag(packname);
		Bitmap bm = AsyncImageManager.getInstance().loadIcon(packname, true,
				true, new AsyncImageLoadedCallBack() {

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
		/**************** 加载头像end ****************/

		if (info.isSelect) {
			holder.isSelectImage.setImageResource(R.drawable.check_box_checked);
		} else {
			holder.isSelectImage.setImageResource(R.drawable.check_box_default);
		}

		final TextView textView = holder.tvAppSize;
		textView.setTag(packname);
		
		try {
			provider.queryPacakgeSize(packname, new AppsizeListener() {

				@Override
				public void BackCall(final long totalSize, final String pagName) {

					handler.post(new Runnable() {

						@Override
						public void run() {
							if (textView.getTag().equals(pagName)) {
								textView.setText(formatSize(totalSize));
							}
							
						}
					});

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		holder.downloadArea.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				if (info.isSystemApp()) {// 冻结
					if (RootShell.isRootValid()) {
						//RemoveApp(packname);
						String command = SystemAppUtil.getDeVersion();
						SystemAppUtil.FreezeApp(packname,command);
						if (loadedCallBack != null) {
							loadedCallBack.callback(info);
						}
						Toast.makeText(mContext, "卸载成功", Toast.LENGTH_SHORT)
								.show();
					} else {
						Toast.makeText(mContext, "您的手机没有Root权限，无法卸载系统应用！",
								Toast.LENGTH_SHORT).show();
					}

				} else {// 卸载
					String uristr = "package:" + packname;
					Uri uri = Uri.parse(uristr);
					Intent deleteIntent = new Intent();
					deleteIntent.setAction(Intent.ACTION_DELETE);
					deleteIntent.setData(uri);
					mContext.startActivity(deleteIntent);
				}

			}
		});

//		final boolean isSelect = true;
//		holder.isSelectImage.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				if (isSelect) {
//					holder.isSelectImage
//							.setImageResource(R.drawable.check_box_checked);
//					// isSelect = false;
//				} else {
//					holder.isSelectImage
//							.setImageResource(R.drawable.check_box_default);
//					// isSelect = true;
//				}
//
//			}
//		});
		return convertView;
	}
	
	
	/***********************
	 * 卸载应用
	 **********************/
	private void RemoveApp(String packName) {
		String uristr = "package:" + packName;
		Uri uri = Uri.parse(uristr);
//		Intent deleteIntent = new Intent();
//		deleteIntent.setAction(Intent.ACTION_DELETE);
//		deleteIntent.setData(uri);
//		mContext.startActivity(deleteIntent);
		Uri uninstallUri = Uri. fromParts ( "package" , packName , null );
		mContext.startActivity(new Intent("android.intent.action.DELETE", uninstallUri));
	}

	// 格式化 转化为.MB格式
	private String formatSize(long size) {
		return Formatter.formatFileSize(mContext, size);
	}

	static class ViewHolder {
		ImageView ivAppImage; // 应用Logo
		TextView tvAppName; // 应用名称
		TextView tvAppSize; // 应用大小
		ImageView isSelectImage; // 选择
		View downloadArea;
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

}

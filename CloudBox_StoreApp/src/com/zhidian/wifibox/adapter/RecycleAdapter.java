package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;
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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 回收站Adpter
 * @author zhaoyl
 *
 */
public class RecycleAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<AppInfo> mList = new ArrayList<AppInfo>();
	private AppInfoProvider provider;
	private Handler handler;
	private AsyncAppDisableCallBack loadedCallBack;
	
	public RecycleAdapter(Context context, AsyncAppDisableCallBack loadedCallBack){
		mContext = context;
		mInflater = LayoutInflater.from(context);
		provider = new AppInfoProvider(context);
		handler = new Handler();
		this.loadedCallBack =loadedCallBack;
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
	public View getView(int position, View convertView, ViewGroup arg2) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_system_recycle, null);
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
		convertView.setTag(R.string.restore, info);
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
								String strSize = Formatter.formatFileSize(mContext, totalSize);
								textView.setText(strSize);
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
				
					if (RootShell.isRootValid()) {
						SystemAppUtil.FreezeApp(packname,SystemAppUtil.ENABLE_APP);
						if (loadedCallBack != null) {
							loadedCallBack.callback(info);
						}
						mList.remove(info);
						notifyDataSetChanged();		
						if (loadedCallBack != null && mList.size() <= 0) {
							loadedCallBack.nowback();
						}
						Toast.makeText(mContext, "还原成功", Toast.LENGTH_SHORT)
								.show();
					} else {
						Toast.makeText(mContext, "您的手机没有Root权限，无法还原系统应用！",
								Toast.LENGTH_SHORT).show();
					}

				}
			
		});
		return convertView;
	}
	
	static class ViewHolder{
		ImageView ivAppImage; // 应用Logo
		TextView tvAppName; // 应用名称
		TextView tvAppSize; // 应用大小
		ImageView isSelectImage; // 选择
		View downloadArea;
	}

	public List<AppInfo> getData() {
		return mList;
	}

}

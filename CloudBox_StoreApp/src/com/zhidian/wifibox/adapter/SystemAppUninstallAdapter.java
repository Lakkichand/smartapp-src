package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Handler;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.AppUninstallBean;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.AppFreezer;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog.CancleCallBackListener;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog.GoonCallBackListener;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 系统应用卸载适配器
 * 
 * @author xiedezhi
 * 
 */
public class SystemAppUninstallAdapter extends BaseAdapter {

	private List<AppUninstallBean> mList = new ArrayList<AppUninstallBean>();

	private LayoutInflater mInflater = LayoutInflater.from(TAApplication
			.getApplication());

	private Handler mHandler;

	private boolean mRoot = AppUtils.isRoot2();

	public SystemAppUninstallAdapter(Handler handler) {
		mHandler = handler;
	}

	/**
	 * 停用点击
	 */
	private OnClickListener mStopClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 跳转到应用详情
			String packageName = (String) v.getTag();
			AppUtils.showInstalledAppDetails(TAApplication.getApplication(),
					packageName);
		}
	};
	/**
	 * 卸载点击
	 */
	private OnClickListener mUninstallClickListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			// 先弹框确认
			final DeleteHintDialog dialog = new DeleteHintDialog(
					TAApplication.getApplication());
			dialog.getWindow().setType(
					WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			dialog.setCancelable(true);
			((TextView) dialog.findViewById(R.id.hint_count))
					.setText("卸载系统应用可能使手机不稳定");
			((TextView) dialog.findViewById(R.id.hint_delete))
					.setText("卸载的系统应用可在回收站还原");
			dialog.setCancleCallBackListener(new CancleCallBackListener() {

				@Override
				public void onClick() {
					dialog.dismiss();
				}
			});
			dialog.setGoonCallBackListener(new GoonCallBackListener() {

				@Override
				public void onClick() {
					dialog.dismiss();
					Toast.makeText(v.getContext(), "正在获取root权限，请稍后...",
							Toast.LENGTH_SHORT).show();
					// 冻结应用
					final String packageName = (String) v.getTag();
					v.postDelayed(new Runnable() {

						@Override
						public void run() {
							boolean result = AppFreezer.disablePackage(
									TAApplication.getApplication(), packageName);
							if (result) {
								Toast.makeText(TAApplication.getApplication(),
										"卸载成功", Toast.LENGTH_SHORT).show();
								mHandler.obtainMessage(-1, packageName)
										.sendToTarget();
								// 更新冻结个数
								TAApplication
										.sendHandler(null,
												IDiyFrameIds.APPUNINSTALL,
												IDiyMsgIds.UPDATE_FREEZE, 0,
												null, null);
							} else {
								Toast.makeText(TAApplication.getApplication(),
										"卸载失败，请检查是否有root权限", Toast.LENGTH_SHORT)
										.show();
							}
						}
					}, 100);
				}
			});
			dialog.show();
		}
	};

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
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.systemappuninstallitem,
					null);
		}
		View gap1 = convertView.findViewById(R.id.gap1);
		final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		TextView title = (TextView) convertView.findViewById(R.id.title);
		TextView info = (TextView) convertView.findViewById(R.id.info);
		Button btn = (Button) convertView.findViewById(R.id.operator);
		View line = convertView.findViewById(R.id.line);
		View gap2 = convertView.findViewById(R.id.gap2);
		AppUninstallBean bean = mList.get(position);
		if (position == 0) {
			gap1.setVisibility(View.VISIBLE);
		} else {
			gap1.setVisibility(View.GONE);
		}
		icon.setTag(bean.packname);
		Bitmap bm = AsyncImageManager.getInstance().loadIcon(bean.packname,
				true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap != null && imgUrl.equals(icon.getTag())) {
							icon.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm == null) {
			icon.setImageBitmap(DrawUtil.sDefaultIcon);
		} else {
			icon.setImageBitmap(bm);
		}
		title.setText(bean.appname);
		info.setText("占用:"
				+ Formatter.formatShortFileSize(TAApplication.getApplication(),
						bean.size));
		if (position == mList.size() - 1) {
			line.setVisibility(View.INVISIBLE);
			gap2.setVisibility(View.VISIBLE);
		} else {
			line.setVisibility(View.VISIBLE);
			gap2.setVisibility(View.GONE);
		}
		btn.setTag(bean.packname);
		if (mRoot) {
			btn.setText("卸载");
			btn.setTextColor(0xFFd45856);
			btn.setBackgroundResource(R.drawable.systemapp_uninstall_bg);
			btn.setOnClickListener(mUninstallClickListener);
		} else {
			btn.setText("停用");
			btn.setTextColor(0xFFb5b5b5);
			btn.setBackgroundResource(R.drawable.systemapp_stop_bg);
			btn.setOnClickListener(mStopClickListener);
		}
		View gap3 = convertView.findViewById(R.id.gap3);
		if (position == getCount() - 1) {
			gap3.setVisibility(View.VISIBLE);
		} else {
			gap3.setVisibility(View.GONE);
		}
		return convertView;
	}

	/**
	 * 更新数据
	 */
	public void update(List<AppUninstallBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

}

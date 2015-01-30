package com.zhidian.wifibox.handler;

import cn.trinea.android.common.util.PackageUtils;
import com.zhidian.wifibox.R;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * 安装结果Handler
 * @author zhaoyl
 *
 */
public class InstallHandler extends Handler {

	private Button btnDownload;
	private Context mContext;
	private String packageName;

	public InstallHandler(Context context, Button btnDownLoad,
			String packageName) {
		mContext = context;
		this.btnDownload = btnDownLoad;
		this.packageName = packageName;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case PackageUtils.INSTALL_SUCCEEDED:// 安装成功
			btnDownload.setText(R.string.btn_open);
			btnDownload.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_file_open), null,
					null);
			btnDownload.setTag(packageName);
			btnDownload.setOnClickListener(mOpenAppClickListener);
			Toast.makeText(mContext, "yeah！安装成功~~", Toast.LENGTH_SHORT).show();
			break;

		case PackageUtils.INSTALL_FAILED_ALREADY_EXISTS:
			btnDownload.setText("失败");
			btnDownload.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_install), null,
					null);
			Toast.makeText(mContext, "此应用已经安装", Toast.LENGTH_SHORT).show();

			break;
		case PackageUtils.INSTALL_FAILED_INVALID_APK:
			btnDownload.setText("失败");
			btnDownload.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_install), null,
					null);
			Toast.makeText(mContext, "此应用包无效，请重新下载", Toast.LENGTH_SHORT).show();
			break;
		case PackageUtils.INSTALL_FAILED_INSUFFICIENT_STORAGE:
			btnDownload.setText("失败");
			btnDownload.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_install), null,
					null);
			Toast.makeText(mContext, "没有足够的存储空间", Toast.LENGTH_SHORT).show();
			break;

		default:
			btnDownload.setText("失败");
			btnDownload.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_install), null,
					null);
			Toast.makeText(mContext, "未知原因", Toast.LENGTH_SHORT).show();
			break;
		}

	}

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

}

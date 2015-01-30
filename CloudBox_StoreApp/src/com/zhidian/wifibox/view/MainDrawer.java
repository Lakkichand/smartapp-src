package com.zhidian.wifibox.view;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AboutUsActivity;
import com.zhidian.wifibox.activity.CleanMasterActivity;
import com.zhidian.wifibox.activity.FeedbackActivity;
import com.zhidian.wifibox.activity.ImprintActivity;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.controller.MainController;
import com.zhidian.wifibox.data.AutoUpdateBean;
import com.zhidian.wifibox.data.MemoryBean;
import com.zhidian.wifibox.file.fragment.FileManagerActivity;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.Setting;
import com.zhidian.wifibox.view.dialog.ConfirmDialog;
import com.zhidian.wifibox.view.dialog.DataDialog;
import com.zhidian.wifibox.view.dialog.LoadingDialog;

/**
 * 侧边栏菜单内容
 * 
 * @author xiedezhi
 * 
 */
public class MainDrawer extends FrameLayout implements OnClickListener,
		OnCheckedChangeListener {

	private TextView memory, sdMemory;// memory：已使用内存
	private RelativeLayout aboutUs, feedback, imprint, phoneClear, apkManage;
	private ToggleButton installToggle, deleteToggle;
	private RoundProgressBar memoryRoundProgressBar, sdRoundProgressBar;
	private Context mContext;
	private TextView memoryTotal;// 总内存
	private TextView sdMemoryTotal;// SD卡总内存
	private TextView tvNowVersion; // 当前版本号
	private Setting setting;

	public MainDrawer(Context context) {
		super(context);
		this.mContext = context;
	}

	public MainDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		setting = new Setting(mContext);

		memory = (TextView) findViewById(R.id.memory_free_tv);
		memoryTotal = (TextView) findViewById(R.id.memory_total_tv);
		sdMemory = (TextView) findViewById(R.id.sd_memory_free_tv);
		sdMemoryTotal = (TextView) findViewById(R.id.sd_memory_total_tv);
		tvNowVersion = (TextView) findViewById(R.id.now_version);

		memoryRoundProgressBar = (RoundProgressBar) findViewById(R.id.memory_roundProgressBar);
		sdRoundProgressBar = (RoundProgressBar) findViewById(R.id.sdcard_roundProgressBar);

		installToggle = (ToggleButton) findViewById(R.id.install_after_download_tb);
		deleteToggle = (ToggleButton) findViewById(R.id.delete_after_install_tb);

		boolean value = setting
				.getBoolean(Setting.INSTALL_AFTER_DOWNLOAD, true);
		setting.putBoolean(Setting.INSTALL_AFTER_DOWNLOAD, value);
		installToggle.setChecked(value);
		value = setting.getBoolean(Setting.DELETE_AFTER_INSTALL, true);
		setting.putBoolean(Setting.DELETE_AFTER_INSTALL, value);
		deleteToggle.setChecked(value);

		phoneClear = (RelativeLayout) findViewById(R.id.phone_clear);
		apkManage = (RelativeLayout) findViewById(R.id.apk_manage);
		aboutUs = (RelativeLayout) findViewById(R.id.about_us_tv);
		feedback = (RelativeLayout) findViewById(R.id.feedback_tv);
		imprint = (RelativeLayout) findViewById(R.id.imprint_tv);

		phoneClear.setOnClickListener(this);
		apkManage.setOnClickListener(this);
		aboutUs.setOnClickListener(this);
		feedback.setOnClickListener(this);
		imprint.setOnClickListener(this);
		installToggle.setOnCheckedChangeListener(this);
		deleteToggle.setOnCheckedChangeListener(this);

		getMemory();
		getUpdateTime();
		tvNowVersion.setText(InfoUtil.getVersionName(TAApplication
				.getApplication()));

		findViewById(R.id.check_update).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						final LoadingDialog loading = new LoadingDialog(
								getContext(), "正在获取新版本信息，请稍后...");
						loading.show();
						postDelayed(new Runnable() {
							public void run() {
								checkForUpdates(loading);
							}
						}, 500);
					}
				});
	}

	/**
	 * 检查更新
	 */
	private void checkForUpdates(final Dialog loading) {
		// 先判断是否自动更新
		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.maincontroller),
				new TARequest(MainController.CHECK_FOR_UPDATE, null),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						loading.dismiss();
						AutoUpdateBean bean = (AutoUpdateBean) response
								.getData();
						if (bean != null && bean.statusCode == 0
								&& !bean.isLatest) {
							Log.e("", "updateurl = " + bean.updateUrl);
							File file = new File(DownloadUtil
									.getCApkFileFromUrl(bean.updateUrl));
							file.delete();
							file = new File(DownloadUtil
									.getCTempApkFileFromUrl(bean.updateUrl));
							file.delete();
							if (bean.isMust) {
								// 弹出更新对话框，点击取消则退出应用
								DataDialog dataDialog = new DataDialog(
										(MainActivity) getContext(),
										bean.version, FileUtil
												.convertFileSize(bean.size),
										bean.description, bean.updateUrl, true);
								try {
									dataDialog.show();
								} catch (Throwable e) {
									e.printStackTrace();
								}
							} else {
								if (InfoUtil.hasWifiConnection(getContext())) {
									// 弹出更新对话框，点击取消则关闭对话框
									DataDialog dataDialog = new DataDialog(
											(MainActivity) getContext(),
											bean.version,
											FileUtil.convertFileSize(bean.size),
											bean.description, bean.updateUrl,
											false);
									try {
										dataDialog.show();
									} catch (Throwable e) {
										e.printStackTrace();
									}
								} else {
									// do nothing
								}
							}
						} else {
							ConfirmDialog dialog = new ConfirmDialog(
									getContext());
							dialog.show();
						}
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}

					@Override
					public void onFailure(TAResponse response) {
						loading.dismiss();
						Toast.makeText(getContext(), "检查更新失败，请检查网络设置",
								Toast.LENGTH_SHORT).show();
					}
				}, true, false);
	}

	/*******************
	 * 获取版本更新时间
	 *******************/
	private void getUpdateTime() {
		TAApplication.getApplication().doCommand(
				mContext.getString(R.string.maincontroller),
				new TARequest(MainController.CHECK_FOR_UPDATE, null),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						AutoUpdateBean bean = (AutoUpdateBean) response
								.getData();
						if (bean != null && bean.statusCode == 0) {
							// 保存版本更新时间
							setting.putString(Setting.UPDATE_TIME,
									bean.updateTime);
						}
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}

					@Override
					public void onFailure(TAResponse response) {
					}
				}, true, false);

	}

	// 格式化 转化为.MB格式
	private String formatSize(long size) {
		return Formatter.formatFileSize(this.getContext(), size);
	}

	/**
	 * 获取内存大小
	 */
	public void getMemory() {
		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.maincontroller),

				new TARequest(MainController.CHECK_MEMORY, null),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						MemoryBean bean = (MemoryBean) response.getData();
						if (bean == null) {
							return;
						}
						long hasuserMemory = bean.getMemoryAvail()
								- bean.getMemorySize();// 已被使用内存
						memory.setText(formatSize(hasuserMemory));
						memoryTotal.setText("/("
								+ formatSize(bean.getMemoryAvail()) + ")");
						int memoryLeft = ((int) (bean.getMemorySize() / 1024 / 1024) * 100)
								/ (int) (bean.getMemoryAvail() / 1024 / 1024);
						memoryRoundProgressBar.setProgress(100 - memoryLeft);

						if (bean.getTotalSdMemory() != 0) {
							long hasuserSdMemory = bean.getTotalSdMemory()
									- bean.getAvailSdMemory();
							sdMemory.setText(formatSize(hasuserSdMemory));
							sdMemoryTotal.setText("/("
									+ formatSize(bean.getTotalSdMemory()) + ")");
							int sdMemoryLeft = ((int) (bean.getAvailSdMemory() / 1024 / 1024) * 100)
									/ (int) (bean.getTotalSdMemory() / 1024 / 1024);
							sdRoundProgressBar.setProgress(100 - sdMemoryLeft);
						}

					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}

					@Override
					public void onFailure(TAResponse response) {
					}
				}, true, false);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.phone_clear:
			// 手机清理
			Intent intent = new Intent();
			intent.setClass(getContext(), CleanMasterActivity.class);
			getContext().startActivity(intent);
			break;
		case R.id.apk_manage:
			// 资源管理
			Intent apkintent = new Intent();
			apkintent.setClass(getContext(), FileManagerActivity.class);
			getContext().startActivity(apkintent);
			break;
		case R.id.about_us_tv:
			Intent aboutIntent = new Intent(this.getContext(),
					AboutUsActivity.class);
			this.getContext().startActivity(aboutIntent);
			break;
		case R.id.feedback_tv:
			Intent feedbackIntent = new Intent(this.getContext(),
					FeedbackActivity.class);
			this.getContext().startActivity(feedbackIntent);
			break;
		case R.id.imprint_tv:
			Intent imprintIntent = new Intent(this.getContext(),
					ImprintActivity.class);
			this.getContext().startActivity(imprintIntent);
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton cb, boolean value) {
		switch (cb.getId()) {
		case R.id.install_after_download_tb:
			setting.putBoolean(Setting.INSTALL_AFTER_DOWNLOAD, value);

			break;
		case R.id.delete_after_install_tb:
			setting.putBoolean(Setting.DELETE_AFTER_INSTALL, value);
			break;
		default:
			break;
		}
	}

}

package com.zhidian.wifibox.view;

import android.content.Context;
import android.content.Intent;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AboutUsActivity;
import com.zhidian.wifibox.activity.FeedbackActivity;
import com.zhidian.wifibox.activity.ImprintActivity;
import com.zhidian.wifibox.controller.MainController;
import com.zhidian.wifibox.data.AutoUpdateBean;
import com.zhidian.wifibox.data.MemoryBean;
import com.zhidian.wifibox.util.Setting;

/**
 * 侧边栏菜单内容
 * 
 * @author xiedezhi
 * 
 */

public class MainDrawer extends FrameLayout implements OnClickListener,
		OnCheckedChangeListener {

	private TextView memory, sdMemory, aboutUs, feedback, imprint;// memory：已使用内存。
	private ToggleButton installToggle, deleteToggle;
	private ProgressBar memoryProgressBar, sdMemoryProgressBar;
	private Context mContext;
	private TextView memoryTotal;// 总内存
	private TextView sdMemoryTotal;// SD卡总内存
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

		memoryProgressBar = (ProgressBar) findViewById(R.id.memory_pb);
		sdMemoryProgressBar = (ProgressBar) findViewById(R.id.sd_memory_pb);

		installToggle = (ToggleButton) findViewById(R.id.install_after_download_tb);
		deleteToggle = (ToggleButton) findViewById(R.id.delete_after_install_tb);

		boolean value = setting
				.getBoolean(Setting.INSTALL_AFTER_DOWNLOAD, true);
		setting.putBoolean(Setting.INSTALL_AFTER_DOWNLOAD, value);
		installToggle.setChecked(value);
		value = setting.getBoolean(Setting.DELETE_AFTER_INSTALL, true);
		setting.putBoolean(Setting.DELETE_AFTER_INSTALL, value);
		deleteToggle.setChecked(value);

		aboutUs = (TextView) findViewById(R.id.about_us_tv);
		feedback = (TextView) findViewById(R.id.feedback_tv);
		imprint = (TextView) findViewById(R.id.imprint_tv);

		aboutUs.setOnClickListener(this);
		feedback.setOnClickListener(this);
		imprint.setOnClickListener(this);

		installToggle.setOnCheckedChangeListener(this);
		deleteToggle.setOnCheckedChangeListener(this);

		getMemory();
		getUpdateTime();

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
	private void getMemory() {
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
						memoryTotal.setText(formatSize(bean.getMemoryAvail()));
						int memoryLeft = ((int) (bean.getMemorySize() / 1024 / 1024) * memoryProgressBar
								.getMax())
								/ (int) (bean.getMemoryAvail() / 1024 / 1024);
						memoryProgressBar.setProgress(memoryProgressBar
								.getMax() - memoryLeft);

						if (bean.getTotalSdMemory() != 0) {
							long hasuserSdMemory = bean.getTotalSdMemory()
									- bean.getAvailSdMemory();
							sdMemory.setText(formatSize(hasuserSdMemory));
							sdMemoryTotal.setText(formatSize(bean
									.getTotalSdMemory()));
							int sdMemoryLeft = ((int) (bean.getAvailSdMemory() / 1024 / 1024) * sdMemoryProgressBar
									.getMax())
									/ (int) (bean.getTotalSdMemory() / 1024 / 1024);
							sdMemoryProgressBar.setProgress(sdMemoryProgressBar
									.getMax() - sdMemoryLeft);
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

	/**
	 * 网络模式发生改变
	 */
	public void onModeChange() {
		// TODO
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
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

package com.zhidian.wifibox.view;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.MainController;
import com.zhidian.wifibox.data.MemoryBean;

import android.content.Context;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 手机内存、SD卡空间View
 * 
 * @author zhaoyl
 * 
 */
public class MemoryView extends LinearLayout {

	private TextView memory, sdMemory;// memory：已使用内存。
	private ProgressBar memoryProgressBar, sdMemoryProgressBar;
	private TextView memoryTotal;// 总内存
	private TextView sdMemoryTotal;// SD卡总内存

	public MemoryView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		memory = (TextView) findViewById(R.id.memory_free_tv);
		memoryTotal = (TextView) findViewById(R.id.memory_total_tv);
		sdMemory = (TextView) findViewById(R.id.sd_memory_free_tv);
		sdMemoryTotal = (TextView) findViewById(R.id.sd_memory_total_tv);

		memoryProgressBar = (ProgressBar) findViewById(R.id.memory_pb);
		sdMemoryProgressBar = (ProgressBar) findViewById(R.id.sd_memory_pb);

		getMemory();

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
						memory.setText("" + formatSize(hasuserMemory));
						memoryTotal.setText("" + formatSize(bean.getMemoryAvail()));
						int memoryLeft = ((int) (bean.getMemorySize() / 1024 / 1024) * memoryProgressBar
								.getMax())
								/ (int) (bean.getMemoryAvail() / 1024 / 1024);
						memoryProgressBar.setProgress(memoryProgressBar
								.getMax() - memoryLeft);

						if (bean.getTotalSdMemory() != 0) {
							long hasuserSdMemory = bean.getTotalSdMemory()
									- bean.getAvailSdMemory();
							sdMemory.setText("" + formatSize(hasuserSdMemory));
							sdMemoryTotal.setText("" + formatSize(bean
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

}

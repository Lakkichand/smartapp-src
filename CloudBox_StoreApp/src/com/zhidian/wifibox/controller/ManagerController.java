package com.zhidian.wifibox.controller;

import java.util.Map;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.InfoUtil;

/**
 * 管理控制器
 * 
 * @author xiedezhi
 * 
 */
public class ManagerController extends TACommand {
	/**
	 * 计算咪表当前值
	 */
	public static final String CALCULATE = "MANAGERCONTROLLER_CALCULATE";

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(CALCULATE)) {
			int minScore = (Integer) request.getData();
			Map<String, Long> map = AppUtils.getRunningApp(TAApplication
					.getApplication());
			long system = 0;
			long user = 0;
			for (String pkg : map.keySet()) {
				if (AppUtils.isSystemApp(TAApplication.getApplication(), pkg)) {
					system += map.get(pkg);
				} else {
					user += map.get(pkg);
				}
			}
			long total = InfoUtil.getTotalRAM() * 1024L;
			int score = (int) (100.5 - ((system * 0.2 + user) / total * 100));
			if (score < minScore) {
				score = minScore;
			}
			sendSuccessMessage(score);
		}
	}

}

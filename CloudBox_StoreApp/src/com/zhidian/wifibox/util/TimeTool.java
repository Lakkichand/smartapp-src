package com.zhidian.wifibox.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.SystemClock;
import android.util.Log;
import cn.trinea.android.common.util.StringUtils;

import com.ta.TAApplication;

public class TimeTool {
	
	/**
	 * 对时间戳格式进行格式化，保证时间戳长度为13位
	 * 
	 * @param timestamp
	 *            时间戳
	 * @return 返回为13位的时间戳
	 */
	public static String formatTimestamp(String timestamp)
	{
		if (timestamp == null || "".equals(timestamp))
		{
			return "";
		}
		String tempTimeStamp = timestamp + "00000000000000";
		StringBuffer stringBuffer = new StringBuffer(tempTimeStamp);
		return tempTimeStamp = stringBuffer.substring(0, 13);
	}
	
	/**
	 * 把时间戳转换为字符串
	 * 
	 * 返回格式为：yyyy-MM-dd
	 * 
	 * @param timestamp
	 * @return 
	 */
	public static String timestampToString(String timestamp) {
		return timestampToString(timestamp, null);
		
	}
	
	/**
	 * 把时间戳转换为字符换
	 * @param timestamp
	 * @param format
	 * @return
	 */
	public static String timestampToString(String timestamp, String format) {
		if (StringUtils.isEmpty(format)) {
			format = "yyyy-MM-dd";
		}
		Calendar c = Calendar.getInstance();
		timestamp = formatTimestamp(timestamp);
		long _timestamp = Long.parseLong(timestamp);
		c.setTimeInMillis(_timestamp);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(c.getTime());
		} catch (Exception e) {
			e.printStackTrace();
			return timestamp;
		}

	}

	/**
	 * @param s
	 *            毫秒
	 */
	public static long[] TimeForString(long s) {
		s = s / 1000L;
		long[] i = new long[5];
		long N = s / 3600L;// 小时
		i[0] = N;

		s = s % 3600L;
		long K = s / 60L;// 分钟
		i[1] = K;

		s = s % 60L;
		long M = s;// 秒
		i[2] = M;

		return i;
	}

	/**
	 * 校验北京时间
	 */
	public static final void verifyTime(boolean thread) {
		if (!InfoUtil.hasNetWorkConnection(TAApplication.getApplication())) {
			return;
		}
		Runnable run = new Runnable() {

			@Override
			public void run() {
				SntpClient client = new SntpClient();
				boolean b = client.requestTime("ntp.api.bz", 5000);
				if (b) {
					long now = client.getNtpTime()
							+ SystemClock.elapsedRealtime()
							- client.getNtpTimeReference();
					long timestamp = now - System.currentTimeMillis();
					Setting setting = new Setting(
							TAApplication.getApplication());
					setting.putLong(Setting.TIMESTAMP, timestamp);
				}
			}
		};
		if (thread) {
			new Thread(run).start();
		} else {
			run.run();
		}
	}

	/**
	 * 获取标准北京时间
	 */
	public static final long getStandardTime() {
		Setting setting = new Setting(TAApplication.getApplication());
		long timestamp = setting.getLong(Setting.TIMESTAMP);
		long ret = System.currentTimeMillis() + timestamp;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(ret);// 获取当前时间
		Log.e("", "" + formatter.format(curDate));
		return ret;
	}

}

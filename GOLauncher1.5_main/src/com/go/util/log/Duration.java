package com.go.util.log;

import java.util.HashMap;

/**
 * 用于检测程序运行所需时间
 * @author yangguanxiang
 *
 */
public class Duration {
	public static HashMap<String, Duration> sMap = new HashMap<String, Duration>();
	public long start;
	public long end;

	private Duration() {
	};

	public static void reset(String tag) {
		if (sMap.containsKey(tag)) {
			Duration duration = sMap.get(tag);
			duration.start = 0;
			duration.end = 0;
		}
	}

	public static void clear(String tag) {
		if (sMap.containsKey(tag)) {
			sMap.remove(tag);
		}
	}

	public static void setStart(String tag) {
		Duration duration = getDurationInstance(tag);
		duration.start = System.currentTimeMillis();
	}

	public static void setEnd(String tag) {
		Duration duration = getDurationInstance(tag);
		duration.end = System.currentTimeMillis();
	}

	public static long getDuration(String tag) {
		Duration duration = getDurationInstance(tag);
		return duration.end - duration.start;
	}

	private static Duration getDurationInstance(String tag) {
		Duration duration;
		if (sMap.containsKey(tag)) {
			duration = sMap.get(tag);
		} else {
			duration = new Duration();
			sMap.put(tag, duration);
		}
		return duration;
	}
}

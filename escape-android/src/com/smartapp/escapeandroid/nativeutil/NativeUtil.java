package com.smartapp.escapeandroid.nativeutil;

import java.util.Random;

public class NativeUtil {
	static {
		System.loadLibrary("proxy-info");
	}

	private static final Random sRandom = new Random();

	public static native String getProxyInfo();

	public static String getEngineIp() {
		String str = getProxyInfo();
		String[] array = str.split("##");
		array = array[0].split("@");
		return array[0];
	}

	public static String getRandomAppId() {
		String str = getProxyInfo();
		String[] array = str.split("##");
		String ids = array[1];
		array = ids.split("@");
		int index = Math.abs((int) (sRandom.nextInt() % (array.length)));
		return array[index];
	}

}

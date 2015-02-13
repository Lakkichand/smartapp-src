package com.zhidian.wifibox.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

/**
 * 线程池管理
 * @author zhaoyl
 *
 */
public class ThreadPoolExecutorUtil {
	
	//private static ThreadPoolExecutorUtil instance;
	private static ExecutorService exec;
	
	public synchronized static ExecutorService getInstance(){
		if (exec == null) {
			exec = Executors.newFixedThreadPool(1);
			Log.i("ThreadPoolExecutorUtil", "新建线程池");
		}
		return exec;
		
	}

}

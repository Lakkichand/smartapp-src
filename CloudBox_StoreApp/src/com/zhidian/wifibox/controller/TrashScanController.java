package com.zhidian.wifibox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.Looper;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.zhidian.wifibox.data.TransScanDataBean;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.PathConstant;

/**
 * 垃圾清理控制器
 * 
 * @author xiedezhi
 * 
 */
public class TrashScanController extends TACommand {

	/**
	 * 扫描残留垃圾
	 */
	public static final String SCAN_TRASH = "TRASHSCANCONTROLLER_SCAN_TRASH";
	/**
	 * 当前正在扫描的文件路径
	 */
	private String mCurrentPath = "";

	private Handler mHandler = new Handler(Looper.getMainLooper());

	private Map<String, Integer> mMap = new HashMap<String, Integer>();
	/**
	 * 更新正在扫描的路径
	 */
	private Runnable mScanningRunnable = new Runnable() {

		@Override
		public void run() {
			mHandler.removeCallbacks(mScanningRunnable);
			sendRuntingMessage(mCurrentPath);
			mHandler.postDelayed(mScanningRunnable, 20);
		}
	};

	@Override
	protected void executeCommand() {
		if (!FileUtil.isSDCardAvaiable()) {
			// 没有残留文件
			sendSuccessMessage(null);
			return;
		}
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(SCAN_TRASH)) {
			mHandler.postDelayed(mScanningRunnable, 20);
			// 先扫描缩略图缓存文件
			TransScanDataBean thum = new TransScanDataBean();
			thum.isSelect = false;
			thum.suggestion = "建议每月清理";
			thum.title = "缩略图缓存文件";
			String directory = PathConstant.SDCARD + "/DCIM/.thumbnails";
			File file = new File(directory);
			if (file.exists() && file.isDirectory()) {
				File[] files = file.listFiles();
				if (files != null && files.length > 0) {
					long size = 0;
					List<String> paths = new ArrayList<String>();
					for (File sfile : files) {
						mCurrentPath = sfile.getAbsolutePath();
						if (sfile.isFile()) {
							size = size + sfile.length();
							paths.add(sfile.getAbsolutePath());
						}
					}
					if (paths.size() > 0) {
						thum.size = size;
						thum.paths = paths;
						List<TransScanDataBean> retList = new ArrayList<TransScanDataBean>();
						retList.add(thum);
						sendRuntingMessage(retList);
					}
				}
			}
			// 空文件夹
			TransScanDataBean empty = new TransScanDataBean();
			empty.isSelect = false;
			empty.suggestion = "";
			empty.title = "空文件夹";
			// 临时文件
			TransScanDataBean tmp = new TransScanDataBean();
			tmp.isSelect = false;
			tmp.suggestion = "";
			tmp.title = "临时文件";
			// 日志文件
			TransScanDataBean log = new TransScanDataBean();
			log.isSelect = false;
			log.suggestion = "";
			log.title = "日志文件";
			File root = new File(PathConstant.SDCARD);
			File[] rootFiles = root.listFiles();
			for (int index = 0; index < rootFiles.length; index++) {
				File xfile = rootFiles[index];
				mMap.put(xfile.getAbsolutePath(), (int) (index * 1.0
						/ rootFiles.length * 100 + 0.5));
			}
			// 扫描空文件夹、临时文件、日志文件
			globalScan(PathConstant.SDCARD, thum, empty, tmp, log);
			mHandler.removeCallbacks(mScanningRunnable);
			List<TransScanDataBean> retList = new ArrayList<TransScanDataBean>();
			if (thum.paths != null && thum.paths.size() > 0) {
				retList.add(thum);
			}
			if (empty.paths != null && empty.paths.size() > 0) {
				retList.add(empty);
			}
			if (tmp.paths != null && tmp.paths.size() > 0) {
				retList.add(tmp);
			}
			if (log.paths != null && log.paths.size() > 0) {
				retList.add(log);
			}
			for (TransScanDataBean bean : retList) {
				bean.isSelect = true;
			}
			sendSuccessMessage(retList);
		}
	}

	/**
	 * 全局扫描空文件夹、临时文件、日志文件
	 */
	private void globalScan(String rootpath, TransScanDataBean thum,
			TransScanDataBean empty, TransScanDataBean tmp,
			TransScanDataBean log) {
		if (mMap.containsKey(rootpath)) {
			sendRuntingMessage(mMap.get(rootpath));
		}
		mCurrentPath = rootpath;
		File file = new File(rootpath);
		if (file.exists()) {
			if (file.isFile()) {
				if (isTmpFile(file)) {
					tmp.paths.add(file.getAbsolutePath());
					tmp.size = tmp.size + file.length();
					sendRMessage(thum, empty, tmp, log);
				} else if (isLogFile(file)) {
					log.paths.add(file.getAbsolutePath());
					log.size = log.size + file.length();
					sendRMessage(thum, empty, tmp, log);
				}
			} else if (file.isDirectory()) {
				if (isEmptyDirectory(file)) {
					empty.paths.add(file.getAbsolutePath());
					empty.size = empty.size + file.length();
					sendRMessage(thum, empty, tmp, log);
				} else {
					File[] files = file.listFiles();
					for (File sfile : files) {
						mCurrentPath = sfile.getAbsolutePath();
						if (isTmpFile(sfile)) {
							tmp.paths.add(sfile.getAbsolutePath());
							tmp.size = tmp.size + sfile.length();
							sendRMessage(thum, empty, tmp, log);
						} else if (isLogFile(sfile)) {
							log.paths.add(sfile.getAbsolutePath());
							log.size = log.size + sfile.length();
							sendRMessage(thum, empty, tmp, log);
						} else if (sfile.isDirectory()) {
							globalScan(sfile.getAbsolutePath(), thum, empty,
									tmp, log);
						}
					}
				}
			}
		}
	}

	/**
	 * 把已经扫描到的结果返回
	 */
	private void sendRMessage(TransScanDataBean thum, TransScanDataBean empty,
			TransScanDataBean tmp, TransScanDataBean log) {
		List<TransScanDataBean> retList = new ArrayList<TransScanDataBean>();
		if (thum.paths != null && thum.paths.size() > 0) {
			retList.add(thum);
		}
		if (empty.paths != null && empty.paths.size() > 0) {
			retList.add(empty);
		}
		if (tmp.paths != null && tmp.paths.size() > 0) {
			retList.add(tmp);
		}
		if (log.paths != null && log.paths.size() > 0) {
			retList.add(log);
		}
		sendRuntingMessage(retList);
	}

	/**
	 * 是否空文件夹
	 */
	private boolean isEmptyDirectory(File file) {
		if (file.isDirectory()
				&& (file.listFiles() == null || file.listFiles().length <= 0)) {
			return true;
		}
		return false;
	}

	/**
	 * 是否临时文件
	 */
	private boolean isTmpFile(File file) {
		if (file.isFile()
				&& file.getAbsolutePath().toLowerCase().endsWith(".tmp")) {
			return true;
		}
		return false;
	}

	/**
	 * 是否日志文件
	 */
	private boolean isLogFile(File file) {
		if (file.isFile()
				&& file.getAbsolutePath().toLowerCase().endsWith(".log")) {
			return true;
		}
		return false;
	}

}

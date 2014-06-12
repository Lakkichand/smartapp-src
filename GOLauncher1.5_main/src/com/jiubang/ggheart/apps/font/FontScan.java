package com.jiubang.ggheart.apps.font;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Log;

import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.launcher.MyThread;

public class FontScan implements ISelfObject {
	private static final String TAG = "FontScan";

	// private static final String FONT_FOLDER = "fonts/";
	private static final String FONT_FOLDER = "fonts";
	private static final String FONT_FILE_EX = ".ttf";

	private MyThread mScanThread;
	private IProgressListener mListener;

	private static final int STATUS_NONE = 0;
	private static final int STATUS_SCAN_START = 1;
	private static final int STATUS_SCAN_PROGRESS = 2;
	private static final int STATUS_SCAN_FINISHED = 3;
	private static final int STATUS_SCAN_CANCEL = 4;

	public FontScan() {
		selfConstruct();
	}

	@Override
	public void selfConstruct() {

	}

	@Override
	public void selfDestruct() {
		cancelScan();
		mListener = null;
	}

	public void register(IProgressListener listener) {
		mListener = listener;
	}

	public void unregister(IProgressListener listener) {
		mListener = null;
	}

	public void startPackageScan(Context context) {
		if (null == context) {
			Log.i(TAG, "start file scan param is null");
			return;
		}
		final PackageManager pm = context.getPackageManager();
		if (null == pm) {
			Log.i(TAG, "start file scan get package manager is null");
			return;
		}
		final List<PackageInfo> packageInfos = pm
				.getInstalledPackages(PackageManager.GET_PERMISSIONS);
		if (null == packageInfos) {
			Log.i(TAG, "start file scan get package info is null");
			return;
		}
		final int sz = packageInfos.size();

		cancelScan();
		mScanThread = new MyThread() {
			@Override
			protected void doBackground() {
				updateUI(Integer.valueOf(STATUS_SCAN_START));

				for (int i = 0; i < sz; i++) {
					if (!getRunFlag()) {
						break;
					}

					PackageInfo info = packageInfos.get(i);
					if (null == info) {
						continue;
					}
					if (null == info.packageName) {
						continue;
					}
					Resources res = null;
					String[] ttfList = null;
					try {
						res = pm.getResourcesForApplication(info.packageName);
						ttfList = res.getAssets().list(FONT_FOLDER);
					} catch (Exception e) {
						if (null == res) {
							Log.i(TAG, "start file scan get package resource exception");
						} else {
							Log.i(TAG, "start file scan get package file list exception");
						}
						continue;
					}

					updateUI(info.packageName);

					if (ttfList != null) {
						for (int j = 0; j < ttfList.length; j++) {
							if (ttfList[j].indexOf(FONT_FILE_EX) > 0) {
								FontBean bean = new FontBean();
								bean.mFontFileType = FontBean.FONTFILETYPE_PACKAGE;
								bean.mPackageName = info.packageName;
								bean.mApplicationName = info.applicationInfo.loadLabel(pm)
										.toString();
								bean.mFileName = FONT_FOLDER + "/" + ttfList[j];

								updateUI(bean);
							}
						}
					}
				}

				if (getRunFlag()) {
					updateUI(Integer.valueOf(STATUS_SCAN_FINISHED));
				} else {
					updateUI(Integer.valueOf(STATUS_SCAN_CANCEL));
				}
			};

			@Override
			protected void doUpdateUI(Object obj) {
				if (null != mListener) {
					int status = STATUS_NONE;
					if (obj instanceof Integer) {
						status = ((Integer) obj).intValue();
					} else {
						status = STATUS_SCAN_PROGRESS;
					}
					switch (status) {
						case STATUS_SCAN_START :
							mListener.onStart(this);
							break;

						case STATUS_SCAN_PROGRESS :
							mListener.onProgress(this, obj);
							break;

						case STATUS_SCAN_FINISHED :
							mListener.onFinish(this);
							break;

						case STATUS_SCAN_CANCEL :
							mListener.onCancel(this);
							break;

						default :
							break;
					}
				}
			};
		};
		mScanThread.start();
	}

	public void startFileScan(final String[] fileArray) {
		if (null == fileArray) {
			Log.i(TAG, "start file scan param is null");
			return;
		}
		final int len = fileArray.length;
		if (len <= 0) {
			Log.i(TAG, "start file scan param have no data");
			return;
		}

		cancelScan();
		mScanThread = new MyThread() {
			@Override
			protected void doBackground() {
				updateUI(Integer.valueOf(STATUS_SCAN_START));

				for (int i = 0; i < len; i++) {
					yield();
					if (!getRunFlag()) {
						break;
					}

					String fileName = fileArray[i];
					if (null == fileName) {
						continue;
					}
					File file = new File(fileName);
					if (!file.exists()) {
						continue;
					}
					if (!file.isDirectory()) {
						continue;
					}

					updateUI(fileName);

					String[] ttfList = file.list();
					if (ttfList != null) {
						for (int j = 0; j < ttfList.length; j++) {
							if (ttfList[j].indexOf(FONT_FILE_EX) > 0) {
								FontBean bean = new FontBean();
								bean.mFontFileType = FontBean.FONTFILETYPE_FILE;
								bean.mPackageName = FontBean.SDCARD;
								bean.mApplicationName = FontBean.SDCARD;
								bean.mFileName = fileName + "/" + ttfList[j];

								updateUI(bean);
							}
						}
					}
				}

				if (getRunFlag()) {
					updateUI(Integer.valueOf(STATUS_SCAN_FINISHED));
				} else {
					updateUI(Integer.valueOf(STATUS_SCAN_CANCEL));
				}
			};

			@Override
			protected void doUpdateUI(Object obj) {
				if (null != mListener) {
					int status = STATUS_NONE;
					if (obj instanceof Integer) {
						status = ((Integer) obj).intValue();
					} else {
						status = STATUS_SCAN_PROGRESS;
					}
					switch (status) {
						case STATUS_SCAN_START :
							mListener.onStart(this);
							break;

						case STATUS_SCAN_PROGRESS :
							mListener.onProgress(this, obj);
							break;

						case STATUS_SCAN_FINISHED :
							mListener.onFinish(this);
							break;

						case STATUS_SCAN_CANCEL :
							mListener.onCancel(this);
							break;

						default :
							break;
					}
				}
			};
		};
		mScanThread.start();
	}

	public void cancelScan() {
		if (null != mScanThread) {
			mScanThread.setRunFlag(false);
			mScanThread = null;
		}
	}
}

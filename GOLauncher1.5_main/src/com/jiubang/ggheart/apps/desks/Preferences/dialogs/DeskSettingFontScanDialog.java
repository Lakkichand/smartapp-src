package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.apps.desks.settings.IFontScanPreferenceListener;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.apps.font.FontScan;
import com.jiubang.ggheart.apps.font.FontTypeface;
import com.jiubang.ggheart.apps.font.IProgressListener;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * 类描述:字体扫描对话框
 * 
 * @author kuanghaojun
 * @date [2012-9-13]
 */
public class DeskSettingFontScanDialog extends Dialog implements ISelfObject {

	private Context mContext;
	private View mContentView;
	private LinearLayout mDialogLayout;
	private Button mOKButton;
	private Button mCancelButton;
	private TextView mScanFolder;
	private TextView mScanResult;
	private FontScan mFontScan;
	private ArrayList<FontBean> mFontScanBeans;
	private IFontScanPreferenceListener mListener;

	// 状态
	private static final int STATUS_SCAN_NONE = 0;
	private static final int STATUS_SCAN_PACKAGE = 1;
	private static final int STATUS_SCAN_SDCARD = 2;
	private int mStatus = STATUS_SCAN_NONE;

	private HashSet<String> mPackageSet;

	public DeskSettingFontScanDialog(Context context) {
		super(context, R.style.Dialog);
		mContext = context;
		selfConstruct();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContentView = inflater.inflate(R.layout.desk_setting_font_scan_dialog, null);
		
		mDialogLayout = (LinearLayout) mContentView.findViewById(R.id.dialog_layout);
		DialogBase.setDialogWidth(mDialogLayout, mContext);
		
		mScanFolder = (TextView) mContentView.findViewById(R.id.scan_folder);
		mScanResult = (TextView) mContentView.findViewById(R.id.scan_result);

		mOKButton = (Button) mContentView.findViewById(R.id.dialog_ok);
		mCancelButton = (Button) mContentView.findViewById(R.id.dialog_cancel);
		mOKButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		mCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		registerIProgressListener();
		setContentView(mContentView);
	}

	@Override
	public void show() {
		super.show();
		mFontScan.startPackageScan(mContext);
		updateScanResult(0);
	}

	@Override
	public void dismiss() {
		/*由于字体扫描对话框在后台开启了线程扫描。当扫描时点击home键。推出了activity。扫描结束后会调用对话框的
		 dismiss（）方法。这时由于对应的对话框已经不存在。会导致报错：View not attached to window manager*/
		try {
			super.dismiss();
			if (null != mFontScan) {
				mFontScan.selfDestruct();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateScanFolder(String folder) {
		if (null != mScanFolder) {
			mScanFolder.setText(folder);
		}
	}

	private void updateScanResult(int count) {
		String summary = mContext.getString(R.string.font_scan_summary_head) + " " + count + " "
				+ mContext.getString(R.string.font_scan_summary_tail);
		if (null != mScanResult) {
			mScanResult.setText(summary);
		}
	}

	@Override
	public void selfConstruct() {
		
	}

	@Override
	public void selfDestruct() {
		dismiss();
		mContext = null;
		mContentView = null;
		mCancelButton = null;
		mOKButton = null;
		mScanFolder = null;
		mScanResult = null;
		if (null != mFontScan) {
			mFontScan.selfDestruct();
			mFontScan = null;
		}
		if (null != mFontScanBeans) {
			mFontScanBeans.clear();
			mFontScanBeans = null;
		}
		if (mPackageSet != null) {
			mPackageSet.clear();
			mPackageSet = null;
		}
		mListener = null;
	}

	private void initSystemFont() {
		File sysFontsFile = new File("/system/fonts/");
		if (sysFontsFile.exists() && sysFontsFile.isDirectory()) {
			File[] files = sysFontsFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile()) {
						FontBean bean = new FontBean();
						bean.mFontFileType = FontBean.FONTFILETYPE_FILE;
						bean.mPackageName = FontBean.SYSTEM;
						bean.mApplicationName = FontBean.SYSTEM;
						bean.mFileName = files[i].getAbsolutePath();
						mFontScanBeans.add(bean);
						if (mPackageSet == null) {
							mPackageSet = new HashSet<String>();
						}
						mPackageSet.add(bean.mPackageName);
					}
				}
			} else {
				resetDefaultFontBeans();
			}
		} else {
			resetDefaultFontBeans();
		}
	}

	private void resetDefaultFontBeans() {
		FontBean bean = new FontBean();
		bean.mFileName = FontTypeface.DEFAULT;
		mFontScanBeans.add(bean);
		bean = new FontBean();
		bean.mFileName = FontTypeface.DEFAULT_BOLD;
		mFontScanBeans.add(bean);
		bean = new FontBean();
		bean.mFileName = FontTypeface.SANS_SERIF;
		mFontScanBeans.add(bean);
		bean = new FontBean();
		bean.mFileName = FontTypeface.SERIF;
		mFontScanBeans.add(bean);
		bean = new FontBean();
		bean.mFileName = FontTypeface.MONOSPACE;
		mFontScanBeans.add(bean);
	}

	public void setOnFontScanPreferenceListener(IFontScanPreferenceListener listener) {
		mListener = listener;
	}

	public void registerIProgressListener() {
		mPackageSet = new HashSet<String>();
		mFontScan = new FontScan();
		IProgressListener listener = new IProgressListener() {
			@Override
			public void onStart(Object listenEntity) {
				if (STATUS_SCAN_NONE == mStatus) {
					mFontScanBeans = new ArrayList<FontBean>();
					// 系统包加入第一个
					initSystemFont();
					mStatus = STATUS_SCAN_PACKAGE;
				}
			}

			@Override
			public void onProgress(Object listenEntity, Object progressPrama) {
				if (null == progressPrama) {
					return;
				}
				if (null == mFontScanBeans) {
					return;
				}

				if (progressPrama instanceof String) {
					updateScanFolder((String) progressPrama);
				}

				if (progressPrama instanceof FontBean) {
					FontBean bean = (FontBean) progressPrama;
					mFontScanBeans.add(bean);
					if (mPackageSet == null) {
						mPackageSet = new HashSet<String>();
					}
					mPackageSet.add(bean.mPackageName);
					updateScanResult(mPackageSet.size() - 1);
				}
			}

			@Override
			public void onFinish(Object listenEntity) {
				if (STATUS_SCAN_PACKAGE == mStatus && null != mFontScan) {
					String[] scanArray = new String[1];
					scanArray[0] = LauncherEnv.Path.SDCARD + LauncherEnv.Path.FONT_PATH;
					mFontScan.startFileScan(scanArray);
					mStatus = STATUS_SCAN_SDCARD;
				} else {
					if (null != mListener) {
						mListener.onFontScanChanged(mFontScanBeans);
					}
					dismiss();
					mStatus = STATUS_SCAN_NONE;
					if (null != mPackageSet) {
						mPackageSet.clear();
					}
				}
			}

			@Override
			public void onCancel(Object listenEntity) {
				mStatus = STATUS_SCAN_NONE;
				if (null != mPackageSet) {
					mPackageSet.clear();
				}
			}
		};
		mFontScan.register(listener);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		//对话框关闭时遍历所有控件，把DeskView和DeskButton反注册
		DeskSettingConstants.selfDestruct(getWindow().getDecorView());
	}
	
}

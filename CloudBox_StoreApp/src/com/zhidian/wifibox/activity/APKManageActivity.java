package com.zhidian.wifibox.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.ta.TAApplication;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.ApkInfoAdapter;
import com.zhidian.wifibox.data.APKInfo;
import com.zhidian.wifibox.listener.ApkScaningCallBackListener;
import com.zhidian.wifibox.util.ApkInfoProvider;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * APK安装包管理
 * 
 * @author zhaoyl
 * 
 */
public class APKManageActivity extends Activity implements OnClickListener {

	private static final String TAG = APKManageActivity.class.getSimpleName();
	private static final int GET_DATA_APK = 100;
	private static final int GET_DATA_APK_SCANING = 200;
	private static final int GET_DATA_APK_FINISH = 300;
	private ListView listView;
	private TextView tvApkTotal, tvSizeTotal;
	private TextView tvScaning; //正在扫描
	private ToggleButton toggleButton;// 全选
	private Button btnInstall; // 一键安装
	private Button btnClear; // 一键清理
	private ApkInfoProvider provider;
	private ApkInfoAdapter adapter;
	private LinearLayout proLayout; // 加载loading
	private Context mContext;
	private long totalSize; // 可清理安装包总大小
	private long total;// 总apk安装包数
	private long selectTotal = 0; // 选择数
	private LinearLayout nocontentLayout; // 没有内容
	private LinearLayout havecontentLayout; // 有内容
	private LinearLayout showTotalLayout; //显示共有多少个

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apk_manage);
		provider = new ApkInfoProvider(this);
		mContext = this;
		// list = new ArrayList<APKInfo>();
		initUI();
		showProgress();
		initList();
		//initAPKData();
		initData();
		initRegisterReceiver();// 注册应用安装广播事件
	}

	/***************************
	 * 初始化UI
	 ***************************/
	private void initUI() {
		TextView tvTitle = (TextView) findViewById(R.id.header_title_text);
		tvTitle.setText(R.string.apk_manage);
		ImageView btnBack = (ImageView) findViewById(R.id.header_title_back);
		btnBack.setOnClickListener(this);

		listView = (ListView) findViewById(R.id.apk_manage_listview);
		tvApkTotal = (TextView) findViewById(R.id.apk_total);
		tvSizeTotal = (TextView) findViewById(R.id.apk_size);
		tvScaning = (TextView) findViewById(R.id.apk_manage_scaning);
		toggleButton = (ToggleButton) findViewById(R.id.apk_manage_togbtn_all);
		btnInstall = (Button) findViewById(R.id.apk_manage_btn_install);
		
		btnInstall.setOnClickListener(this);
		btnClear = (Button) findViewById(R.id.apk_manage_btn_clear);
		btnClear.setOnClickListener(this);

		proLayout = (LinearLayout) findViewById(R.id.show_scanning);
		nocontentLayout = (LinearLayout) findViewById(R.id.no_content);
		havecontentLayout = (LinearLayout) findViewById(R.id.have_content);
		showTotalLayout = (LinearLayout) findViewById(R.id.total);
		// contentLayout = (LinearLayout) findViewById(R.id.person_app_home);
	}

	private void initRegisterReceiver() {
		// 注册应用安装广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addDataScheme("package");
		registerReceiver(mAppInstallListener, intentFilter);

		intentFilter = new IntentFilter();
		intentFilter.addAction("DELETE_APK");
		registerReceiver(mDeleteApkListener, intentFilter);
	}

	/**
	 * 应用安装监听器
	 */
	private final BroadcastReceiver mAppInstallListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				List<APKInfo> list = adapter.getData();
				for (APKInfo info : list) {
					if (info.getPackname().equals(packageName)) {
						info.setIsInstall(0);
						adapter.notifyDataSetChanged();
					}
				}

			}
		}
	};

	/**
	 * 自动删除APK监听器
	 */
	private final BroadcastReceiver mDeleteApkListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if ("DELETE_APK".equals(action)) {
				String packageName = intent.getStringExtra("packName");
				List<APKInfo> list = adapter.getData();
				List<APKInfo> removeList = new ArrayList<APKInfo>();

				for (APKInfo info : list) {
					if (info.getPackname().equals(packageName)) {

						totalSize = totalSize - info.getSize();
						total = total - 1;
						if (total > 0) {

							if (info.isSelect) {// 如果此安装包已被选中
								selectTotal = selectTotal - 1;
								if (selectTotal > 0) {// 如果此时还有被选中数
									setButtonText();
								} else {
									initButton();
								}
							}

							// TODO
							tvApkTotal.setText(total + "");
							tvSizeTotal.setText(formatSize(totalSize));
//							tvTotal.setText("共" + total + "个安装包，清理可节省存储空间 "
//									+ formatSize(totalSize));
						} else {
							//tvTotal.setText("您的手机没有apk安装包");
							initButton();
						}

						removeList.add(info);

					}
				}

				list.removeAll(removeList);
				adapter.notifyDataSetChanged();

			}
		}
	};

	private void showProgress() {
		showTotalLayout.setVisibility(View.GONE);
		proLayout.setVisibility(View.VISIBLE);
	}

	private void showContent() {
		showTotalLayout.setVisibility(View.GONE);
		proLayout.setVisibility(View.GONE);
		havecontentLayout.setVisibility(View.GONE);
		nocontentLayout.setVisibility(View.VISIBLE);
	}

	private void initList() {
		adapter = new ApkInfoAdapter(mContext,
				new ApkScaningCallBackListener() {

					@Override
					public void callback(APKInfo info) {
						totalSize = totalSize - info.getSize();
						total = total - 1;
						if (total > 0) {

							if (info.isSelect) {// 如果此安装包已被选中
								selectTotal = selectTotal - 1;
								if (selectTotal > 0) {// 如果此时还有被选中数
									setButtonText();
								} else {
									initButton();
								}
							}

							// TODO
							tvApkTotal.setText(total + "");
							tvSizeTotal.setText(formatSize(totalSize));
//							tvTotal.setText("共" + total + "个安装包，清理可节省存储空间 "
//									+ formatSize(totalSize));
						} else {
							//tvTotal.setText("您的手机没有apk安装包");
							initButton();
						}

					}

					@Override
					public void nowback() {
						// TODO Auto-generated method stub
						showContent();
					}

					@Override
					public void nowScaning(String fileName) {
						// TODO Auto-generated method stub
						
					}
				});

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				APKInfo info = (APKInfo) view.getTag(R.string.clear);
				if (info.isSelect) {
					info.isSelect = false;
					selectTotal = selectTotal - 1;
					if (selectTotal > 0) {
						setButtonText();
					} else {
						btnInstall.setText("一键安装");
						btnClear.setText("一键清理");
					}
				} else {
					info.isSelect = true;
					selectTotal = selectTotal + 1;
					if (selectTotal > 0) {
						setButtonText();
					} else {
						btnInstall.setText("一键安装");
						btnClear.setText("一键清理");
					}
				}

				adapter.notifyDataSetChanged();
			}
		});

		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// toggleButton.setChecked(isChecked);
				if (total > 0) {

					if (isChecked) {
						selectTotal = total;
						btnInstall.setText("一键安装（" + selectTotal + "）");
						btnClear.setText("一键清理（" + selectTotal + "）");
						List<APKInfo> list = adapter.getData();
						for (APKInfo info : list) {
							if (!info.isSelect) {
								info.isSelect = true;
							}
						}
					} else {
						initButton();
						List<APKInfo> list = adapter.getData();
						for (APKInfo info : list) {
							if (info.isSelect) {
								info.isSelect = false;
							}
						}
					}

					adapter.notifyDataSetChanged();

				}
			}
		});
	}

	/***************************
	 * 获取数据
	 ***************************/

	private void initData() {
		toggleButton.setVisibility(View.INVISIBLE);
		new Thread() {
			@Override
			public void run() {
				super.run();
				provider.FindAllAPKFile(
						Environment.getExternalStorageDirectory(),
						new ApkScaningCallBackListener() {

							@Override
							public void callback(APKInfo info) {
								Message msg = new Message();
								msg.obj = info;
								msg.what = GET_DATA_APK;
								mHandler.sendMessage(msg);
							}

							@Override
							public void nowback() {
								// TODO Auto-generated method stub

							}

							@Override
							public void nowScaning(String fileName) {
								// TODO Auto-generated method stub
								Message msg = new Message();
								msg.obj = fileName;
								msg.what = GET_DATA_APK_SCANING;
								mHandler.sendMessage(msg);
							}
						});

				totalSize = provider.getTotalSize();
				total = provider.getTotal();
				mHandler.sendEmptyMessage(GET_DATA_APK_FINISH);
				Log.d("结束", "gggggggggggggggggggggggggg");
				// list = provider.getAPKInfos();
				// totlaSize = provider.getTotalSize();

			}
		}.start();

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_DATA_APK:
				APKInfo info = (APKInfo) msg.obj;
				// list.add(info);
				adapter.update(info);
				// tvTotal.setText("共" + list.size() + "个安装包，清理可节省存储空间 " +
				// totlaSize);
				// showContent();
				break;
			case GET_DATA_APK_FINISH:
				// TODO
				if (total > 0) {
					tvApkTotal.setText(total + "");
					tvSizeTotal.setText(formatSize(totalSize));
					
//					tvTotal.setText("共" + total + "个安装包，清理可节省存储空间 "
//							+ formatSize(totalSize));
					toggleButton.setVisibility(View.VISIBLE);
					proLayout.setVisibility(View.GONE);
					showTotalLayout.setVisibility(View.VISIBLE);
					havecontentLayout.setVisibility(View.VISIBLE);
					nocontentLayout.setVisibility(View.GONE);

				} else {
					showContent();
				}

				break;
				
			case GET_DATA_APK_SCANING:
				String path = (String) msg.obj;
				tvScaning.setText("正在扫描：" + path);
				break;

			default:
				break;
			}
		}

	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_title_back:
			this.finish();
			break;
		case R.id.apk_manage_btn_install:
			// 一键安装
			gotoInstall();
			break;
		case R.id.apk_manage_btn_clear:
			// 一键清理
			gotoClear();
			break;
		default:
			break;
		}

	}

	/***************************
	 * 一键清理
	 ***************************/
	private void gotoClear() {
		long selectSize = 0;
		List<APKInfo> removelist = new ArrayList<APKInfo>();
		if (total > 0) {
			boolean select = false;
			List<APKInfo> list = adapter.getData();
			for (APKInfo info : list) {
				if (info.isSelect) {
					select = true;
					break;
				}
			}

			if (select) {
				for (APKInfo info : list) {
					if (info.isSelect) {
						Log.d(TAG, "要清理的安装包:" + info.getAppname());
						selectSize = selectSize + info.getSize();
						clearApk(info.getPath());
						removelist.add(info);

					}
				}

				if (removelist.size() > 0) {
					adapter.getData().removeAll(removelist);
					adapter.notifyDataSetChanged();
					totalSize = totalSize - selectSize;
					total = total - removelist.size();
					if (total > 0) {
						tvApkTotal.setText(total + "");
						tvSizeTotal.setText(formatSize(totalSize));
//						tvTotal.setText("共" + total + "个安装包，清理可节省存储空间 "
//								+ formatSize(totalSize));
					} else {
						// TODO
						showContent();
					}

					initButton();
					Toast.makeText(mContext, "清理成功", Toast.LENGTH_SHORT).show();
				}

			} else {// 没选中有应用
				Toast.makeText(mContext, R.string.select_clear,
						Toast.LENGTH_SHORT).show();

			}

		}

	}

	/***************************
	 * 一键安装
	 ***************************/
	private void gotoInstall() {

		if (total > 0) {
			boolean select = false;
			List<APKInfo> list = adapter.getData();
			for (APKInfo info : list) {
				if (info.isSelect) {
					select = true;
					break;
				}
			}

			if (select) {
				for (APKInfo info : list) {
					if (info.isSelect) {
						Log.d(TAG, "要安装的应用:" + info.getAppname());
						InstallApk(info.getPath());
					}
				}

			} else {// 没选中有应用
				Toast.makeText(mContext, R.string.select_install,
						Toast.LENGTH_SHORT).show();

			}

		}
	}

	/***************************
	 * 安装APK
	 ***************************/
	private void InstallApk(String fileName) {
		try {
			File file = new File(fileName);
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file),
					"application/vnd.android.package-archive");
			mContext.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 格式化 转化为.MB格式
	private String formatSize(long size) {
		return Formatter.formatFileSize(this, size);
	}

	/***************************
	 * 清理APK
	 ***************************/
	private void clearApk(String fileName) {
		try {
			File f = new File(fileName);
			if (f.exists()) {
				f.delete();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initButton() {
		selectTotal = 0;
		btnInstall.setText("一键安装");
		btnClear.setText("一键清理");
	}

	private void setButtonText() {
		btnInstall.setText("一键安装（" + selectTotal + "）");
		btnClear.setText("一键清理（" + selectTotal + "）");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("安装包管理");
			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("安装包管理");
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mAppInstallListener);
		unregisterReceiver(mDeleteApkListener);
	}

}

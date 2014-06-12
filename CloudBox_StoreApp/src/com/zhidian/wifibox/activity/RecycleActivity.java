package com.zhidian.wifibox.activity;

import java.util.ArrayList;
import java.util.List;

import com.ta.TAApplication;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.RecycleAdapter;
import com.zhidian.wifibox.data.AppInfo;
import com.zhidian.wifibox.listener.AsyncAppDisableCallBack;
import com.zhidian.wifibox.root.RootShell;
import com.zhidian.wifibox.util.AppInfoProvider;
import com.zhidian.wifibox.util.AppUninstaller;
import com.zhidian.wifibox.util.SystemAppUtil;
import com.zhidian.wifibox.view.dialog.LoadingDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 回收站
 * 
 * @author zhaoyl
 * 
 */
public class RecycleActivity extends Activity implements OnClickListener {

	private static final String TAG = RecycleActivity.class.getSimpleName();
	private static final int GET_SUSSCEE_DATA = 100;
	private static final int DATA_NOW = 200;
	private static final int DATA_WANG = 300;
	private static final int DATA_LONG = 400;
	private static final int DATA_DELETE_SUCCESS = 500;
	private static final int DATA_DELETE_FAIL = 600;
	private static final int DATA_DELETE_NOT = 700;
	private static final int DATA_DELETE_NO_ROOT = 800;
	private Button btnForvevrDet; // 永久删除
	private Button btnRestore; // 批量还原
	private ListView listView;
	private RecycleAdapter adapter;
	private ToggleButton toggleButton;
	private AppInfoProvider provider;
	private List<AppInfo> list;
	private LinearLayout proLayout; // 加载loading
	private LinearLayout contentLayout; // 内容
	private LinearLayout nocontentLayout; // 没有内容
	private LinearLayout havecontentLayout; // 有内容
	private int selectTotal = 0; // 选择数
	private int total;// 被冻结的应用的总数
	private Context mContext;
	private LoadingDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recylce);
		provider = new AppInfoProvider(this);
		mContext = this;
		loadingDialog = new LoadingDialog(this);
		initUI();
		initList();
		showProgress();
		initData();
	}

	private void initList() {
		list = new ArrayList<AppInfo>();
		adapter = new RecycleAdapter(mContext, new AsyncAppDisableCallBack() {

			@Override
			public void callback(AppInfo info) {
				if (info.isSelect) {// 如果此应用已被选中
					selectTotal = selectTotal - 1;
					if (selectTotal > 0) {// 如果此时还有被选中数
						setButtonText();
					} else {
						initButton();
					}
				}

			}

			@Override
			public void nowback() {

				havecontentLayout.setVisibility(View.GONE);
				nocontentLayout.setVisibility(View.VISIBLE);

			}
		});

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				AppInfo info = (AppInfo) view.getTag(R.string.restore);
				if (info.isSelect) {
					info.isSelect = false;
					selectTotal = selectTotal - 1;
					if (selectTotal > 0) {
						setButtonText();
					} else {
						initButton();
					}
				} else {
					info.isSelect = true;
					selectTotal = selectTotal + 1;
					if (selectTotal > 0) {
						setButtonText();
					} else {
						initButton();
					}
				}

				adapter.notifyDataSetChanged();
			}
		});

		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (total > 0) {

					if (isChecked) {
						selectTotal = total;
						setButtonText();
						// List<APKInfo> list = adapter.getData();
						for (AppInfo info : list) {
							if (!info.isSelect) {
								info.isSelect = true;
							}
						}
					} else {
						initButton();
						// List<AppInfo> alist = list;
						for (AppInfo info : list) {
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

	/***********************
	 * 初始化UI
	 ***********************/
	private void initUI() {
		TextView tvTitle = (TextView) findViewById(R.id.header_title_text);
		tvTitle.setText(R.string.recycle);
		ImageButton btnBack = (ImageButton) findViewById(R.id.header_title_back);
		btnBack.setOnClickListener(this);

		btnForvevrDet = (Button) findViewById(R.id.recyle_btn_forever_delete);
		btnForvevrDet.setOnClickListener(this);
		btnRestore = (Button) findViewById(R.id.recyle_btn_restore);
		btnRestore.setOnClickListener(this);

		listView = (ListView) findViewById(R.id.recyle_listview);
		toggleButton = (ToggleButton) findViewById(R.id.recyle_togbtn_all);

		proLayout = (LinearLayout) findViewById(R.id.person_app_probar);
		contentLayout = (LinearLayout) findViewById(R.id.person_app_home);
		nocontentLayout = (LinearLayout) findViewById(R.id.no_content);
		havecontentLayout = (LinearLayout) findViewById(R.id.have_content);

	}

	private void showProgress() {
		contentLayout.setVisibility(View.GONE);
		proLayout.setVisibility(View.VISIBLE);
	}

	private void showContent() {
		contentLayout.setVisibility(View.VISIBLE);
		proLayout.setVisibility(View.GONE);
	}

	private void initButton() {
		selectTotal = 0;
		btnForvevrDet.setText("永久删除");
		btnRestore.setText("批量还原");
	}

	private void setButtonText() {
		btnForvevrDet.setText("永久删除（" + selectTotal + "）");
		btnRestore.setText("批量还原（" + selectTotal + "）");
	}

	/***********************
	 * 获取数据
	 ***********************/
	private void initData() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				list = provider.getDisableInfo();
				total = list.size();
				mHandler.sendEmptyMessage(GET_SUSSCEE_DATA);
			}
		}.start();
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_SUSSCEE_DATA:
				if (total > 0) {
					adapter.update(list);
					showContent();
					havecontentLayout.setVisibility(View.VISIBLE);
					nocontentLayout.setVisibility(View.GONE);
				} else {
					showContent();
					havecontentLayout.setVisibility(View.GONE);
					nocontentLayout.setVisibility(View.VISIBLE);
				}

				break;
			case DATA_NOW:
				Toast.makeText(mContext, "您手机没有ROOT权限，无法还原系统应用",
						Toast.LENGTH_SHORT).show();
				break;

			case DATA_WANG:

				adapter.notifyDataSetChanged();
				initButton();
				if (adapter.getData().size() > 0) {
					havecontentLayout.setVisibility(View.VISIBLE);
					nocontentLayout.setVisibility(View.GONE);
				} else {
					havecontentLayout.setVisibility(View.GONE);
					nocontentLayout.setVisibility(View.VISIBLE);
				}
				Toast.makeText(mContext, "还原成功", Toast.LENGTH_SHORT).show();
				break;

			case DATA_LONG:
				Toast.makeText(mContext, R.string.select_restore,
						Toast.LENGTH_SHORT).show();
				break;

			case DATA_DELETE_SUCCESS:
				initButton();

				if (adapter.getData().size() > 0) {
					havecontentLayout.setVisibility(View.VISIBLE);
					nocontentLayout.setVisibility(View.GONE);
				} else {
					havecontentLayout.setVisibility(View.GONE);
					nocontentLayout.setVisibility(View.VISIBLE);
				}
				adapter.notifyDataSetChanged();
				Toast.makeText(mContext, "永久卸载成功", Toast.LENGTH_SHORT).show();
				break;
			case DATA_DELETE_NO_ROOT:
				Toast.makeText(mContext, "您手机没有ROOT权限，无法永久卸载系统应用",
						Toast.LENGTH_SHORT).show();
				break;

			case DATA_DELETE_NOT:
				Toast.makeText(mContext, R.string.select_delete,
						Toast.LENGTH_SHORT).show();
				break;

			case DATA_DELETE_FAIL:
				Toast.makeText(mContext, "永久卸载失败", Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}

			closeDialog();
		}

	};

	@Override
	protected void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("回收站");
			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("回收站");
			MobclickAgent.onPause(this);
		}
	}

	/***************************
	 * 对话框
	 ***************************/

	private void showDialogMessage(CharSequence message) {
		loadingDialog.setMessage(message);
		if (!loadingDialog.isShowing()) {
			loadingDialog.show();
		}
	}

	private void closeDialog() {
		try {
			if (loadingDialog.isShowing()) {
				loadingDialog.dismiss();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_title_back:
			this.finish();
			break;
		case R.id.recyle_btn_forever_delete:
			showDialogMessage("正在删除");
			new Thread() {

				@Override
				public void run() {
					super.run();
					gotoPiDeleteForever();
				}

			}.start();

			break;
		case R.id.recyle_btn_restore:
			showDialogMessage("正在还原");
			new Thread() {

				@Override
				public void run() {
					super.run();
					gotoRestore();
				}

			}.start();

			break;

		default:
			break;
		}

	}

	/***********************
	 * 批量还原
	 ***********************/
	private void gotoRestore() {
		if (total > 0) {
			boolean select = false;
			List<AppInfo> list = adapter.getData();
			for (AppInfo info : list) {
				if (info.isSelect) {
					select = true;
					break;
				}
			}

			if (select) {
				List<AppInfo> removelist = new ArrayList<AppInfo>();
				if (RootShell.isRootValid()) {
					for (AppInfo info : list) {
						if (info.isSelect) {

							SystemAppUtil.FreezeApp(info.getPackname(),
									SystemAppUtil.ENABLE_APP);
							removelist.add(info);
						}
					}

					if (removelist.size() > 0) {
						adapter.getData().removeAll(removelist);
						mHandler.sendEmptyMessage(DATA_WANG);

					}

				} else {
					mHandler.sendEmptyMessage(DATA_NOW);
					return;
				}

			} else {// 没选中有应用
				mHandler.sendEmptyMessage(DATA_LONG);

			}
		}

	}

	/***********************
	 * 永久删除
	 ***********************/
	private void gotoPiDeleteForever() {

		if (total > 0) {
			boolean select = false;
			List<AppInfo> list = adapter.getData();
			for (AppInfo info : list) {
				if (info.isSelect) {
					select = true;
					break;
				}
			}

			if (select) {
				List<AppInfo> removelist = new ArrayList<AppInfo>();
				if (RootShell.isRootValid()) {
					for (AppInfo info : list) {
						if (info.isSelect) {
							// TODO
							if (RootShell.isRootValid()) {
								// 先解冻，再卸载
								SystemAppUtil.FreezeApp(info.getPackname(),
										SystemAppUtil.ENABLE_APP);

								boolean isTrue = AppUninstaller
										.silentUninstallSystemApp(
												info.getSourceDir(),
												info.getDataDir(),
												info.getPackname());
								if (isTrue) {
									removelist.add(info);
								}
							}

						}
					}
				} else {
					mHandler.sendEmptyMessage(DATA_DELETE_NO_ROOT);
					return;
				}

				if (removelist.size() > 0) {
					adapter.getData().removeAll(removelist);
					mHandler.sendEmptyMessage(DATA_DELETE_SUCCESS);

				} else {
					mHandler.sendEmptyMessage(DATA_DELETE_FAIL);
				}

			} else {// 没选中有应用
				mHandler.sendEmptyMessage(DATA_DELETE_NOT);

			}
		}
	}

}

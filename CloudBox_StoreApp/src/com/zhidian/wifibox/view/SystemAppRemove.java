package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.RecycleActivity;
import com.zhidian.wifibox.adapter.AppRemoveAdapter;
import com.zhidian.wifibox.data.AppInfo;
import com.zhidian.wifibox.listener.AppremoveCallBackListener;
import com.zhidian.wifibox.listener.AsyncAppDisableCallBack;
import com.zhidian.wifibox.root.RootShell;
import com.zhidian.wifibox.util.SystemAppUtil;
import com.zhidian.wifibox.view.dialog.LoadingDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 系统预装(软件卸载)
 * 
 * @author zhaoyl
 */
public class SystemAppRemove implements OnClickListener {

	private Context mContext;
	private ListView listView;
	private ToggleButton toggleButton;
	private Button btnPiRemove;
	private List<AppInfo> infosList;
	private AppRemoveAdapter adapter;
	private int total = 0;
	private static final int MSG_NOT_ROOT = 100;
	private static final int MSG_NOT_SELECT = 101;
	private static final int MSG_DISABLE_SUCCESS = 102;
	private AppremoveCallBackListener listener;
	private LoadingDialog loadingDialog;

	public SystemAppRemove(Context context, View view, List<AppInfo> infosList,
			AppremoveCallBackListener listener) {
		mContext = context;
		this.infosList = infosList;
		this.listener = listener;
		loadingDialog = new LoadingDialog(context);
		initUI(view);
		initListView();
	}

	private void initUI(View view) {
		listView = (ListView) view.findViewById(R.id.person_app_refreshview);
		toggleButton = (ToggleButton) view
				.findViewById(R.id.person_app_togbtn_all);
		btnPiRemove = (Button) view.findViewById(R.id.person_app_btn);
		btnPiRemove.setOnClickListener(this);
	}

	/*********************
	 * ListView头部
	 *********************/
	private void initListView() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = (View) inflater.inflate(R.layout.view_system_hint, null);
		listView.addHeaderView(view);
		Button button = (Button) view.findViewById(R.id.gain_root_btn);
		button.setOnClickListener(this);

		adapter = new AppRemoveAdapter(mContext, new AsyncAppDisableCallBack() {

			@Override
			public void callback(AppInfo info) {
				setStatus(info);
				// infosList.remove(info);
				// listener.callback(infosList.size());
				// adapter.update(infosList);
				// total = total - 1;
				// if (total > 0) {
				// btnPiRemove.setText("批量卸载（" + total + "）");
				// } else {
				// btnPiRemove.setText("批量卸载");
				// }
			}

			@Override
			public void nowback() {
				// TODO Auto-generated method stub
				
			}
		});
		listView.setAdapter(adapter);
		adapter.update(infosList);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				AppInfo info = (AppInfo) view.getTag(R.string.remove);
				if (info.isSelect) {
					info.isSelect = false;
					total = total - 1;
					if (total > 0) {
						btnPiRemove.setText("批量卸载（" + total + "）");
					} else {
						btnPiRemove.setText("批量卸载");
					}
				} else {
					info.isSelect = true;
					total = total + 1;
					if (total > 0) {
						btnPiRemove.setText("批量卸载（" + total + "）");
					} else {
						btnPiRemove.setText("批量卸载");
					}
				}

				adapter.notifyDataSetChanged();
			}
		});

		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				// toggleButton.setChecked(isChecked);
				if (infosList != null && infosList.size() > 0) {

					if (isChecked) {
						total = infosList.size();
						btnPiRemove.setText("批量卸载（" + total + "）");
						List<AppInfo> list = infosList;
						for (AppInfo info : list) {
							if (!info.isSelect) {
								info.isSelect = true;
							}
						}
					} else {
						total = 0;
						btnPiRemove.setText("批量卸载");
						List<AppInfo> list = infosList;
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

	public void setUpdate(List<AppInfo> infosList2) {
		infosList = infosList2;
		adapter.update(infosList);
	}

	/***************************
	 * 卸载完时调用
	 * 
	 * @param packName
	 *            包名
	 ****************************/
	public void setUpdate(String packName, AppremoveCallBackListener listener) {
		if (infosList != null && infosList.size() > 0) {
			for (AppInfo info : infosList) {
				if (packName.equals(info.getPackname())) {
					infosList.remove(info);
					listener.callback(infosList.size());
					total = total - 1;
					if (total > 0) {
						btnPiRemove.setText("批量卸载（" + total + "）");
					} else {
						btnPiRemove.setText("批量卸载");
					}
					break;
				}
			}
			adapter.update(infosList);
		}

	}

	/***********************
	 * 卸载应用
	 **********************/
	private void RemoveApp(String packName) {
		String uristr = "package:" + packName;
		Uri uri = Uri.parse(uristr);
		Intent deleteIntent = new Intent();
		deleteIntent.setAction(Intent.ACTION_DELETE);
		deleteIntent.setData(uri);
		mContext.startActivity(deleteIntent);
	}

	/***********************
	 * 批量冻结
	 **********************/
	private void gotoPiRemove() {
		if (infosList != null && infosList.size() > 0) {
			List<AppInfo> removelist = new ArrayList<AppInfo>();
			boolean select = false;
			List<AppInfo> list = infosList;
			for (AppInfo info : list) {
				if (info.isSelect) {
					select = true;
					break;
				}
			}

			if (select) {
				if (RootShell.isRootValid()) {
					for (AppInfo info : list) {
						if (info.isSelect) {
							Log.d("PersonApp", "要卸载的应用:" + info.getAppname());
							// RemoveApp(info.getPackname());
							String command = SystemAppUtil.getDeVersion();
							SystemAppUtil.FreezeApp(info.getPackname(),
									command);
							removelist.add(info);
						}

					}

					if (removelist.size() > 0) {
						infosList.removeAll(removelist);
						total = total - removelist.size();
						mHandler.sendEmptyMessage(MSG_DISABLE_SUCCESS);
					}
				} else {
					mHandler.sendEmptyMessage(MSG_NOT_ROOT);
					return;
				}

			} else {// 没选中有应用
				mHandler.sendEmptyMessage(MSG_NOT_SELECT);

			}

		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_NOT_ROOT:
				Toast.makeText(mContext, "您手机没有root权限，无法卸载系统应用",
						Toast.LENGTH_SHORT).show();
				break;
			case MSG_NOT_SELECT:
				Toast.makeText(mContext, R.string.select_remove,
						Toast.LENGTH_SHORT).show();
				break;
			case MSG_DISABLE_SUCCESS:
				adapter.update(infosList);
				listener.callback(infosList.size());
				if (total > 0) {
					btnPiRemove.setText("批量卸载（" + total + "）");
				} else {
					btnPiRemove.setText("批量卸载");
				}

				Toast.makeText(mContext, "卸载成功", Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
			
			closeDialog();
		}

	};

	private void setStatus(AppInfo info) {
		infosList.remove(info);
		listener.callback(infosList.size());
		adapter.update(infosList);
		total = total - 1;
		if (total > 0) {
			btnPiRemove.setText("批量卸载（" + total + "）");
		} else {
			btnPiRemove.setText("批量卸载");
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.person_app_btn:
			showDialogMessage("正在卸载");
			new Thread() {

				@Override
				public void run() {
					gotoPiRemove();
				}

			}.start();

			break;
		case R.id.gain_root_btn:
			gotoRecycleActivity();
			break;
		default:
			break;
		}

	}

	/**
	 * 跳转到回收站界面
	 */
	private void gotoRecycleActivity() {
		Intent intent = new Intent();
		intent.setClass(mContext, RecycleActivity.class);
		mContext.startActivity(intent);

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

}

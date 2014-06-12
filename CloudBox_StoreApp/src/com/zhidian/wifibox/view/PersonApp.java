package com.zhidian.wifibox.view;

import java.util.List;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.AppRemoveAdapter;
import com.zhidian.wifibox.data.AppInfo;
import com.zhidian.wifibox.listener.AppremoveCallBackListener;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * 个人应用(软件卸载)
 * 
 * @author zhaoyl
 * 
 */
public class PersonApp implements OnClickListener {

	private Context mContext;
	private ListView listView;
	private ToggleButton toggleButton;
	private Button btnPiRemove;
	private List<AppInfo> infosList;
	private AppRemoveAdapter adapter;
	private int total = 0;

	public PersonApp(Context context, View view, List<AppInfo> infosList) {
		mContext = context;
		this.infosList = infosList;
		initUI(view);
		initListView();
		// initData();
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
		MemoryView memoryView = (MemoryView) inflater.inflate(
				R.layout.view_memory_size, null);
		listView.addHeaderView(memoryView);

		adapter = new AppRemoveAdapter(mContext);
		listView.setAdapter(adapter);
		setUpdate(infosList);
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
					}else {
						btnPiRemove.setText("批量卸载");
					}
				} else {
					info.isSelect = true;
					total = total + 1;
					if (total > 0) {
						btnPiRemove.setText("批量卸载（" + total + "）");
					}else {
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
		adapter.update(infosList2);
	}

	/***************************
	 * 卸载完时调用
	 * 
	 * @param packName
	 *            包名
	 ****************************/
	public void setUpdate(String packName,AppremoveCallBackListener listener) {
		if (infosList != null && infosList.size() > 0) {
			for (AppInfo info : infosList) {
				if (packName.equals(info.getPackname())) {
					infosList.remove(info);
					listener.callback(infosList.size());
					total = total - 1;
					if (total > 0) {
						btnPiRemove.setText("批量卸载（" + total + "）");
					}else {
						btnPiRemove.setText("批量卸载");
					}
					break;
				}
			}
			adapter.update(infosList);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.person_app_btn:
			gotoPiRemove();
			break;

		default:
			break;
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
	 * 批量卸载
	 **********************/
	private void gotoPiRemove() {
		// TODO Auto-generated method stub
		if (infosList != null && infosList.size() > 0) {
			boolean select = false;
			List<AppInfo> list = infosList;
			for (AppInfo info : list) {
				if (info.isSelect) {
					select = true;
					break;
				}
			}

			if (select) {
				for (AppInfo info : list) {
					if (info.isSelect) {
						Log.d("PersonApp", "要卸载的应用:" + info.getAppname());
						RemoveApp(info.getPackname());
					}
				}

			} else {// 没选中有应用
				Toast.makeText(mContext, R.string.select_remove,
						Toast.LENGTH_SHORT).show();

			}

		}
	}

}

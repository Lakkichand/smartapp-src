package com.zhidian.checkapk;

import java.util.List;

import com.zhidian.bean.InstallBean;
import com.zhidian.checkapk.adapter.RecordAdapter;
import com.zhidian.wifibox.dao.InstallApkDao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;

/**
 * 
 * 查看记录
 * 
 * @author zhaoyl
 * 
 */
public class RecordActivity extends Activity {

	private ListView listView;
	private RecordAdapter adapter;
	private List<InstallBean> list;
	private Context mContext;
	private static final int DATA_SUCCESS = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		mContext = this;
		initUI();
		getData();
	}

	private void initUI() {
		// TODO Auto-generated method stub
		listView = (ListView) findViewById(R.id.listview);
	}

	/**
	 * 获取数据
	 */
	private void getData() {
		new Thread() {
			public void run() {
				InstallApkDao dao = new InstallApkDao(mContext);
				list = dao.getSpkData();
				mHandler.sendEmptyMessage(DATA_SUCCESS);
			};
		}.start();

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case DATA_SUCCESS:
				adapter = new RecordAdapter(mContext, list);
				listView.setAdapter(adapter);
				break;

			default:
				break;
			}
		};
	};
}

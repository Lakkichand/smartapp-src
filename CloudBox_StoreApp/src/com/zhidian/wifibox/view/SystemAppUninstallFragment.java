package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AppUninstallActivity;
import com.zhidian.wifibox.adapter.SystemAppUninstallAdapter;
import com.zhidian.wifibox.data.AppUninstallBean;
import com.zhidian.wifibox.util.AppFreezer;

/**
 * 系统应用卸载
 * 
 * @author xiedezhi
 * 
 */
public class SystemAppUninstallFragment extends Fragment {

	private ListView mListView;
	private SystemAppUninstallAdapter mAdapter;

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(android.os.Message msg) {
			if (msg.obj instanceof String) {
				String packageName = (String) msg.obj;
				List<AppUninstallBean> list = new ArrayList<AppUninstallBean>();
				for (AppUninstallBean bean : ((AppUninstallActivity) getActivity()).mSystemappInfo) {
					if (packageName.equals(bean.packname)) {
						continue;
					}
					list.add(bean);
				}
				((AppUninstallActivity) getActivity()).mSystemappInfo.clear();
				((AppUninstallActivity) getActivity()).mSystemappInfo
						.addAll(list);
				mAdapter.update(((AppUninstallActivity) getActivity()).mSystemappInfo);
			}
		};
	};

	@Override
	public void onResume() {
		super.onResume();
		// 检查应用是否被冻结
		List<AppUninstallBean> list = new ArrayList<AppUninstallBean>();
		for (AppUninstallBean bean : ((AppUninstallActivity) getActivity()).mSystemappInfo) {
			if (AppFreezer.isAppFreeze(getActivity(), bean.packname)) {
				continue;
			}
			list.add(bean);
		}
		((AppUninstallActivity) getActivity()).mSystemappInfo.clear();
		((AppUninstallActivity) getActivity()).mSystemappInfo.addAll(list);
		mAdapter.update(((AppUninstallActivity) getActivity()).mSystemappInfo);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View frame = inflater.inflate(R.layout.systemappuninstall, null);
		mListView = (ListView) frame.findViewById(R.id.sys_listview);
		mListView.addHeaderView(inflater.inflate(
				R.layout.systemappuninstallheader, null));
		mAdapter = new SystemAppUninstallAdapter(mHandler);
		mListView.setAdapter(mAdapter);
		mAdapter.update(((AppUninstallActivity) getActivity()).mSystemappInfo);
		return frame;
	}

}

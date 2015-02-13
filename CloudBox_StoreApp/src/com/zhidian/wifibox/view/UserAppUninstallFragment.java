package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AppUninstallActivity;
import com.zhidian.wifibox.adapter.UserAppUninstallAdapter;
import com.zhidian.wifibox.controller.TabController;
import com.zhidian.wifibox.data.AppUninstallBean;
import com.zhidian.wifibox.data.AppUninstallGroup;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;

/**
 * 用户应用卸载
 * 
 * @author xiedezhi
 * 
 */
public class UserAppUninstallFragment extends Fragment {

	private ExpandableListView mListView;
	private UserAppUninstallAdapter mAdapter;
	private View mNoContent;

	/**
	 * 应用卸载监听器
	 */
	private BroadcastReceiver mAppUninstallListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				for (AppUninstallGroup group : ((AppUninstallActivity) getActivity()).mUserappGroup) {
					List<AppUninstallBean> list = new ArrayList<AppUninstallBean>();
					for (AppUninstallBean bean : group.mList) {
						if (bean.packname.equals(packageName)) {
							continue;
						}
						list.add(bean);
					}
					group.mList = list;
				}
				List<AppUninstallGroup> gList = new ArrayList<AppUninstallGroup>();
				for (AppUninstallGroup group : ((AppUninstallActivity) getActivity()).mUserappGroup) {
					if (group.mList != null && group.mList.size() > 0) {
						gList.add(group);
					}
				}
				((AppUninstallActivity) getActivity()).mUserappGroup.clear();
				((AppUninstallActivity) getActivity()).mUserappGroup
						.addAll(gList);
				mAdapter.update(((AppUninstallActivity) getActivity()).mUserappGroup);
				boolean app = false;
				if (((AppUninstallActivity) getActivity()).mUserappGroup != null) {
					for (AppUninstallGroup group : ((AppUninstallActivity) getActivity()).mUserappGroup) {
						if (group.mList != null && group.mList.size() > 0) {
							app = true;
							break;
						}
					}
				}
				if (app) {
					mNoContent.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);
				} else {
					mNoContent.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);
				}
				TAApplication.sendHandler(null, IDiyFrameIds.APPUNINSTALL,
						IDiyMsgIds.UPDATE_UNINSTALL_BTN, 0, null, null);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addDataScheme("package");
		getActivity().registerReceiver(mAppUninstallListener, intentFilter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View frame = inflater.inflate(R.layout.userappuninstall, null);
		mNoContent = frame.findViewById(R.id.no_content);
		mNoContent.setVisibility(View.GONE);
		Button nBtn = (Button) mNoContent.findViewById(R.id.jump);
		TextView text = (TextView) mNoContent.findViewById(R.id.text);
		nBtn.setText("返回首页");
		nBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().finish();
				TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
						IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
						TabController.NAVIGATIONFEATURE, null);
				v.postDelayed(new Runnable() {

					@Override
					public void run() {
						TAApplication.sendHandler(null, IDiyFrameIds.ACTIONBAR,
								IDiyMsgIds.JUMP_TITLE, 0, null, null);
					}
				}, 50);
			}
		});
		text.setText("还没有安装应用");
		mListView = (ExpandableListView) frame.findViewById(R.id.listview);
		mListView.setGroupIndicator(null);
		mAdapter = new UserAppUninstallAdapter();
		mListView.setAdapter(mAdapter);
		mAdapter.update(((AppUninstallActivity) getActivity()).mUserappGroup);
		for (int i = 0; i < mAdapter.getGroupCount(); i++) {
			mListView.expandGroup(i);
		}
		mListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				return true;
			}
		});
		boolean app = false;
		if (((AppUninstallActivity) getActivity()).mUserappGroup != null) {
			for (AppUninstallGroup group : ((AppUninstallActivity) getActivity()).mUserappGroup) {
				if (group.mList != null && group.mList.size() > 0) {
					app = true;
					break;
				}
			}
		}
		if (app) {
			mNoContent.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		} else {
			mNoContent.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		}
		return frame;
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(mAppUninstallListener);
		super.onDestroy();
	}

}

/*
 * 文 件 名:  AppGameSettingActivity.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-7-24
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.setting;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-7-24]
 */
public class AppGameSettingActivity extends PreferenceActivity {

	private final static int SHOW_NETWORK_FLOW_DIALOG = 1;
	/**
	 * 省流量模式
	 */
	private PreferenceScreen mNetworkPf = null;

	private NetworkFlowAdapter mAdapter = null;

	private View mDialogView = null;

	private Dialog mTrafficDialog = null;

	private AppGameSettingData mSettingData = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.appgame_setting);
		mSettingData = AppGameSettingData.getInstance(this);
		initializeView();
		initializeHeadView();
	}

	/**
	 * 功能简述:初始化title栏 功能详细描述: 注意:
	 */
	private void initializeHeadView() {
		PreferenceScreen ps = getPreferenceScreen();
		AppGameSettingHeadView headView = new AppGameSettingHeadView(this);
		headView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		// 将view的排序置为第一位
		headView.setOrder(0);
		ps.addPreference(headView);
	}

	/**
	 * 功能简述:初始化设置项的layout 功能详细描述: 注意:
	 */
	private void initializeView() {
		mNetworkPf = (PreferenceScreen) findPreference(getString(R.string.key_appgame_network_flow));
		mNetworkPf
				.setSummary(getResources().getStringArray(R.array.network_flow_select_item)[mSettingData
						.getTrafficSavingMode()]);
		mNetworkPf.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				showDialog(SHOW_NETWORK_FLOW_DIALOG);
				return true;
			}
		});
		// 设置布局文件
		mNetworkPf.setLayoutResource(R.layout.appgame_setting_item);
		// 设置背景颜色以及分隔线
		ListView listView = this.getListView();
		listView.setDivider(this.getResources().getDrawable(R.drawable.allfunc_allapp_menu_line));
		listView.setBackgroundColor(0xfff2f2f2);
		listView.setSelector(new ColorDrawable(0x00000000));
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
			case SHOW_NETWORK_FLOW_DIALOG :
				return showNetworkFlowDialog();
		}
		return super.onCreateDialog(id);
	}

	private View createDialogView() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.appgame_network_dialog, null);
		ListView listview = (ListView) view.findViewById(R.id.appgame_network_dialog_listView);
		final String[] array = getResources().getStringArray(R.array.network_flow_select_item);
		mAdapter = new NetworkFlowAdapter(this, array);
		listview.setAdapter(mAdapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				mTrafficDialog.dismiss();
				String str = array[position];
				updatePreference(mNetworkPf, str);
				// 统计代码
				switch (position) {
				case 0:
					StatisticsData.countStatData(AppGameSettingActivity.this, StatisticsData.KEY_NOLOAD_ICON);
					break;
				case 1:
					StatisticsData.countStatData(AppGameSettingActivity.this, StatisticsData.KEY_ONLY_ICON);
					break;
				case 2:
					StatisticsData.countStatData(AppGameSettingActivity.this, StatisticsData.KEY_LOAD_ALL);
					break;

				default:
					break;
				}
				// 保存值到数据库
				mSettingData.updateValue(AppGameSettingTable.TRAFFIC_SAVING_MODE, position);
			}
		});
		Button cancelBtn = (Button) view.findViewById(R.id.appgame_network_dialog_cancel);
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTrafficDialog.dismiss();
			}
		});
		return view;
	}

	private Dialog showNetworkFlowDialog() {
		mDialogView = null;
		mDialogView = createDialogView();
		mTrafficDialog = null;
		mTrafficDialog = new Dialog(this, R.style.AppGameSettingDialog);
		mTrafficDialog.setContentView(mDialogView);
		return mTrafficDialog;
	}

	private void updatePreference(PreferenceScreen pf, String str) {
		if (pf != null) {
			pf.setSummary(str);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//运行在主进程，设置数据清理
		AppGameSettingData.destory();
	}
}

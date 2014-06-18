package com.smartapp.rootuninstaller;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.smartapp.rootuninstaller.ui.IPagerAdapter;
import com.smartapp.rootuninstaller.ui.TitlePagerActionBar;
import com.smartapp.rootuninstaller.util.MainDataController;
import com.smartapp.rootuninstaller.util.Util;

/**
 * 主界面，管理3个列表
 * 
 * @author xiedezhi
 * 
 */
public class MainActivity extends Activity {

	private TitlePagerActionBar mPagerActionBar;

	private ViewPager mViewPager;
	private IPagerAdapter mPagerAdapter;
	/**
	 * 用户应用列表
	 */
	private ListView mUserAppList;
	private ImageView mUserAppSort;
	private Button mUserAppUninstall;
	private Button mUserAppDisable;
	/**
	 * 用户应用数据适配器
	 */
	private IListAdapter mUserAppAdapter;
	/**
	 * 系统应用列表
	 */
	private ListView mSystemAppList;
	private ImageView mSystemAppSort;
	private Button mSystemAppUninstall;
	private Button mSystemAppDisable;
	/**
	 * 系统应用数据适配器
	 */
	private IListAdapter mSystemAppAdapter;
	/**
	 * 回收站列表
	 */
	private ListView mRecycleBinList;
	private ImageView mRecycleBinSort;
	private Button mRecycleBinRestore;
	private Button mRecycleBinClear;
	/**
	 * 回收站数据适配器
	 */
	private IListAdapter mRecycleBinAdapter;

	/**
	 * 刷新列表
	 */
	public static final int REFRESH_LIST = 1001;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH_LIST:
				if (mUserAppAdapter != null) {
					mUserAppAdapter.notifyDataSetChanged();
				}
				if (mSystemAppAdapter != null) {
					mSystemAppAdapter.notifyDataSetChanged();
				}
				if (mRecycleBinAdapter != null) {
					mRecycleBinAdapter.notifyDataSetChanged();
				}
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		mPagerActionBar = (TitlePagerActionBar) findViewById(R.id.pager_action_bar);
		mViewPager = (ViewPager) findViewById(R.id.view_pager);

		LayoutInflater inflater = LayoutInflater.from(this);

		View mainView1 = inflater.inflate(R.layout.mainview1, null);
		mUserAppList = (ListView) mainView1.findViewById(R.id.userapp_listview);
		mUserAppSort = (ImageView) mainView1.findViewById(R.id.userapp_sort);
		mUserAppUninstall = (Button) mainView1
				.findViewById(R.id.userapp_uninstaller);
		mUserAppDisable = (Button) mainView1.findViewById(R.id.userapp_disable);

		View mainView2 = inflater.inflate(R.layout.mainview2, null);
		mSystemAppList = (ListView) mainView2
				.findViewById(R.id.systemapp_listview);
		mSystemAppSort = (ImageView) mainView2
				.findViewById(R.id.systemapp_sort);
		mSystemAppUninstall = (Button) mainView2
				.findViewById(R.id.systemapp_uninstaller);
		mSystemAppDisable = (Button) mainView2
				.findViewById(R.id.systemapp_disable);

		View mainView3 = inflater.inflate(R.layout.mainview3, null);
		mRecycleBinList = (ListView) mainView3
				.findViewById(R.id.recyclebin_listview);
		mRecycleBinSort = (ImageView) mainView3
				.findViewById(R.id.recyclebin_sort);
		mRecycleBinRestore = (Button) mainView3
				.findViewById(R.id.recyclebin_restore);
		mRecycleBinClear = (Button) mainView3
				.findViewById(R.id.recyclebin_clear);

		final List<View> mList = new ArrayList<View>();
		mList.add(mainView1);
		mList.add(mainView2);
		mList.add(mainView3);

		mPagerAdapter = new IPagerAdapter(MainActivity.this, mList);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setPageMargin(Util.dip2px(MainActivity.this, 10));
		mViewPager.setPageMarginDrawable(new ColorDrawable(0xFF4d4d4d));

		mPagerActionBar.attachToViewPager(mViewPager);
		mViewPager.setCurrentItem(0);

		final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage(getString(R.string.readingapps));
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.show();

		new Thread() {
			public void run() {

				// 获取应用列表
				final List<ListDataBean> userAppList = new ArrayList<ListDataBean>();
				final List<ListDataBean> systemAppList = new ArrayList<ListDataBean>();
				final List<ListDataBean> disableAppList = new ArrayList<ListDataBean>();

				MainDataController.getMainAppList(MainActivity.this,
						userAppList, systemAppList, disableAppList, mHandler);

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						dialog.dismiss();

						// 展示列表
						mUserAppAdapter = new IListAdapter(MainActivity.this,
								IListAdapter.ADAPTER_TYPE_USERAPP, mHandler);
						mUserAppList.setAdapter(mUserAppAdapter);
						mUserAppAdapter.update(userAppList);

						mSystemAppAdapter = new IListAdapter(MainActivity.this,
								IListAdapter.ADAPTER_TYPE_SYSTEMAPP, mHandler);
						mSystemAppList.setAdapter(mSystemAppAdapter);
						mSystemAppAdapter.update(systemAppList);

						mRecycleBinAdapter = new IListAdapter(
								MainActivity.this,
								IListAdapter.ADAPTER_TYPE_DISABLEAPP, mHandler);
						mRecycleBinList.setAdapter(mRecycleBinAdapter);
						mRecycleBinAdapter.update(disableAppList);
					}
				});
			};
		}.start();

	}
}

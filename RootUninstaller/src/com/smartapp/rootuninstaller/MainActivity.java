package com.smartapp.rootuninstaller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.smartapp.rootuninstaller.comparator.DateAscendingComparator;
import com.smartapp.rootuninstaller.comparator.DateDescendingComparator;
import com.smartapp.rootuninstaller.comparator.MemoryAscendingComparator;
import com.smartapp.rootuninstaller.comparator.MemoryDescendingComparator;
import com.smartapp.rootuninstaller.comparator.NameAscendingComparator;
import com.smartapp.rootuninstaller.comparator.NameDescendingComparator;
import com.smartapp.rootuninstaller.comparator.SizeAscendingComparator;
import com.smartapp.rootuninstaller.comparator.SizeDescendingComparator;
import com.smartapp.rootuninstaller.ui.IPagerAdapter;
import com.smartapp.rootuninstaller.ui.TitlePagerActionBar;
import com.smartapp.rootuninstaller.util.AppUninstaller;
import com.smartapp.rootuninstaller.util.MainDataController;
import com.smartapp.rootuninstaller.util.RootShell;
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
	 * 菜单按钮
	 */
	private ImageButton mSetting;
	/**
	 * 系统可用空间
	 */
	private TextView mRomSpace;
	/**
	 * 弹出菜单
	 */
	private PopupWindow mMenu;

	/**
	 * 名字升序
	 */
	private static int NAME_ASCENDING = 1001;
	/**
	 * 名字降序
	 */
	private static int NAME_DESCENDING = 1002;
	/**
	 * 大小升序
	 */
	private static int SIZE_ASCENDING = 1003;
	/**
	 * 大小降序
	 */
	private static int SIZE_DESCENDING = 1004;
	/**
	 * 日期升序
	 */
	private static int DATE_ASCENDING = 1005;
	/**
	 * 日期降序
	 */
	private static int DATE_DESCENDING = 1006;
	/**
	 * 运行内存升序
	 */
	private static int MEMORY_ASCENDING = 1007;
	/**
	 * 运行内存降序
	 */
	private static int MEMORY_DESCENDING = 1008;

	/**
	 * 用户程序保存当前排序的key
	 */
	private static String USER_LIST_SORT_KEY = "USER_LIST_SORT_KEY";
	/**
	 * 系统程序保存当前排序的key
	 */
	private static String SYSTEM_LIST_SORT_KEY = "SYSTEM_LIST_SORT_KEY";
	/**
	 * 回收站保存当前排序的key
	 */
	private static String RECYCLEBIN_LIST_SORT_KEY = "RECYCLEBIN_LIST_SORT_KEY";

	/**
	 * 用户应用列表
	 */
	private List<ListDataBean> mUserListDataBean;
	/**
	 * 系统应用列表
	 */
	private List<ListDataBean> mSystemListDataBean;
	/**
	 * 回收站应用列表
	 */
	private List<ListDataBean> mRecycleListDataBean;

	/**
	 * 是否已经检查rootrom
	 */
	private volatile boolean mHasCheckRootRom = false;
	/**
	 * 是否rootrom
	 */
	private boolean mIsRootRom = false;

	/**
	 * 刷新列表
	 */
	public static final int REFRESH_LIST = 1001;
	/**
	 * 重新加载数据
	 */
	public static final int RELOAD_LIST = 1002;
	/**
	 * 更新按钮状态
	 */
	public static final int REFRESH_BTN = 1003;

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
				refreshBtn();
				break;
			case RELOAD_LIST:
				refreshList();
				break;
			case REFRESH_BTN:
				refreshBtn();
				break;
			default:
				break;
			}
		};
	};

	/**
	 * 当resume时是否重新加载列表
	 */
	private boolean mNeedToReLoadListWhenResume = false;

	/**
	 * 广告条
	 */
	private AdView adView;

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
		mUserAppList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListDataBean bean = (ListDataBean) view.getTag();
				if (bean != null) {
					bean.mIsSelect = !bean.mIsSelect;
				}
				if (mUserAppAdapter != null) {
					mUserAppAdapter.notifyDataSetChanged();
					refreshBtn();
				}
			}
		});

		mUserAppUninstall = (Button) mainView1
				.findViewById(R.id.userapp_uninstaller);
		mUserAppUninstall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 用户程序卸载
				uninstallUserApp(mUserAppAdapter.getSelectBeans());
			}
		});
		mUserAppDisable = (Button) mainView1.findViewById(R.id.userapp_disable);
		mUserAppDisable.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				freezeApp(mUserAppAdapter.getSelectBeans());
			}
		});

		View mainView2 = inflater.inflate(R.layout.mainview2, null);
		mSystemAppList = (ListView) mainView2
				.findViewById(R.id.systemapp_listview);
		mSystemAppList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListDataBean bean = (ListDataBean) view.getTag();
				if (bean != null) {
					bean.mIsSelect = !bean.mIsSelect;
				}
				if (mSystemAppAdapter != null) {
					mSystemAppAdapter.notifyDataSetChanged();
					refreshBtn();
				}
			}
		});
		mSystemAppSort = (ImageView) mainView2
				.findViewById(R.id.systemapp_sort);
		mSystemAppUninstall = (Button) mainView2
				.findViewById(R.id.systemapp_uninstaller);
		mSystemAppUninstall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				uninstallSystemApp(mSystemAppAdapter.getSelectBeans());
			}
		});
		mSystemAppDisable = (Button) mainView2
				.findViewById(R.id.systemapp_disable);
		mSystemAppDisable.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				freezeApp(mSystemAppAdapter.getSelectBeans());
			}
		});

		View mainView3 = inflater.inflate(R.layout.mainview3, null);
		mRecycleBinList = (ListView) mainView3
				.findViewById(R.id.recyclebin_listview);
		mRecycleBinList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListDataBean bean = (ListDataBean) view.getTag();
				if (bean != null) {
					bean.mIsSelect = !bean.mIsSelect;
				}
				if (mRecycleBinAdapter != null) {
					mRecycleBinAdapter.notifyDataSetChanged();
					refreshBtn();
				}
			}
		});
		mRecycleBinSort = (ImageView) mainView3
				.findViewById(R.id.recyclebin_sort);
		mRecycleBinRestore = (Button) mainView3
				.findViewById(R.id.recyclebin_restore);
		mRecycleBinRestore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 恢复应用
				unfreezeApp(mRecycleBinAdapter.getSelectBeans());
			}
		});
		mRecycleBinClear = (Button) mainView3
				.findViewById(R.id.recyclebin_clear);
		mRecycleBinClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				uninstallRecycleApp(mRecycleBinAdapter.getSelectBeans());
			}
		});

		List<View> mList = new ArrayList<View>();
		mList.add(mainView1);
		mList.add(mainView2);
		mList.add(mainView3);

		mPagerAdapter = new IPagerAdapter(MainActivity.this, mList);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setPageMargin(Util.dip2px(MainActivity.this, 10));
		mViewPager.setPageMarginDrawable(new ColorDrawable(0xFF4d4d4d));

		mPagerActionBar.attachToViewPager(mViewPager);
		mViewPager.setCurrentItem(0);

		mUserAppAdapter = new IListAdapter(MainActivity.this,
				IListAdapter.ADAPTER_TYPE_USERAPP, mHandler);
		mUserAppList.setAdapter(mUserAppAdapter);

		mSystemAppAdapter = new IListAdapter(MainActivity.this,
				IListAdapter.ADAPTER_TYPE_SYSTEMAPP, mHandler);
		mSystemAppList.setAdapter(mSystemAppAdapter);

		mRecycleBinAdapter = new IListAdapter(MainActivity.this,
				IListAdapter.ADAPTER_TYPE_DISABLEAPP, mHandler);
		mRecycleBinList.setAdapter(mRecycleBinAdapter);

		// 弹出菜单
		mSetting = (ImageButton) findViewById(R.id.setting);
		mSetting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showOrHideMenu();
			}
		});

		mRomSpace = (TextView) findViewById(R.id.rom);

		mUserAppSort = (ImageView) mainView1.findViewById(R.id.userapp_sort);
		mUserAppSort.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 用户程序列表排序
				showSortDialog(USER_LIST_SORT_KEY, mUserListDataBean,
						mUserAppAdapter);
			}
		});

		mSystemAppSort = (ImageView) mainView2
				.findViewById(R.id.systemapp_sort);
		mSystemAppSort.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 系统程序列表排序
				showSortDialog(SYSTEM_LIST_SORT_KEY, mSystemListDataBean,
						mSystemAppAdapter);
			}
		});

		mRecycleBinSort = (ImageView) mainView3
				.findViewById(R.id.recyclebin_sort);
		mRecycleBinSort.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 回收站排序
				showSortDialog(RECYCLEBIN_LIST_SORT_KEY, mRecycleListDataBean,
						mRecycleBinAdapter);
			}
		});

		// 添加广告条
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-6335053266754945/4337732515");
		adView.setAdSize(AdSize.BANNER);
		LinearLayout layout = (LinearLayout) findViewById(R.id.root);
		// 在其中添加 adView
		layout.addView(adView);
		// 启动一般性请求。
		AdRequest adRequest = new AdRequest.Builder().build();
		// 在adView中加载广告请求。
		adView.loadAd(adRequest);

		refreshList();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mNeedToReLoadListWhenResume) {
			refreshList();
			mNeedToReLoadListWhenResume = false;
		}
		adView.resume();
	}

	/**
	 * 更新底部按钮状态
	 */
	private void refreshBtn() {
		// 用户应用
		int selectUserAppCount = mUserAppAdapter.getSelectCount();
		if (selectUserAppCount <= 0) {
			mUserAppUninstall.setText(R.string.uninstall);
			mUserAppDisable.setText(R.string.disable);
		} else {
			mUserAppUninstall.setText(getString(R.string.uninstall) + "("
					+ selectUserAppCount + ")");
			mUserAppDisable.setText(getString(R.string.disable) + "("
					+ selectUserAppCount + ")");
		}
		// 系统应用
		int selectSystemAppCount = mSystemAppAdapter.getSelectCount();
		if (selectSystemAppCount <= 0) {
			mSystemAppUninstall.setText(R.string.uninstall);
			mSystemAppDisable.setText(R.string.disable);
		} else {
			mSystemAppUninstall.setText(getString(R.string.uninstall) + "("
					+ selectSystemAppCount + ")");
			mSystemAppDisable.setText(getString(R.string.disable) + "("
					+ selectSystemAppCount + ")");
		}
		// 回收站
		int selectRecycleAppCount = mRecycleBinAdapter.getSelectCount();
		if (selectRecycleAppCount <= 0) {
			mRecycleBinRestore.setText(R.string.restore);
			mRecycleBinClear.setText(R.string.remove);
		} else {
			mRecycleBinRestore.setText(getString(R.string.restore) + "("
					+ selectRecycleAppCount + ")");
			mRecycleBinClear.setText(getString(R.string.remove) + "("
					+ selectRecycleAppCount + ")");
		}
	}

	/**
	 * 删除回收站的应用
	 */
	private void uninstallRecycleApp(final List<ListDataBean> list) {
		// 检查是否有选中的项目
		if (list == null || list.size() <= 0) {
			Toast.makeText(MainActivity.this, R.string.selectitem,
					Toast.LENGTH_SHORT).show();
			return;
		}
		int userAppCount = 0;
		int systemAppCount = 0;
		for (ListDataBean bean : list) {
			if (bean.mIsSystemApp) {
				systemAppCount++;
			} else {
				userAppCount++;
			}
		}
		if (systemAppCount <= 0 && userAppCount > 0) {
			// 只有用户应用
			uninstallUserApp(list);
		} else if (systemAppCount > 0 && userAppCount <= 0) {
			// 只有系统应用
			uninstallSystemApp(list);
		} else if (systemAppCount > 0 && userAppCount > 0) {
			final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
			dialog.setMessage(getString(R.string.requestroot));
			dialog.setCancelable(false);// 设置进度条是否可以按退回键取消
			if (RootShell.hasInit()) {
			} else if (mHasCheckRootRom && !mIsRootRom) {
			} else {
				dialog.show();
			}
			// 先请求root权限再卸载
			Thread thread = new Thread() {
				@Override
				public void run() {
					// 先获取root权限
					boolean root = true;
					if (!mHasCheckRootRom) {
						mIsRootRom = Util.isRootRom(MainActivity.this);
						mHasCheckRootRom = true;
					}
					if (mIsRootRom) {
						root = RootShell.isRootValid();
					} else {
						root = false;
					}
					final boolean root_ = root;
					runOnUiThread(new Runnable() {
						public void run() {
							dialog.dismiss();
							if (!root_) {
								// 非root，提示用户“获取root权限失败，只能卸载用户应用，是否继续卸载？”
								AlertDialog.Builder builder = new AlertDialog.Builder(
										MainActivity.this)
										.setTitle(R.string.tips)
										.setMessage(
												R.string.contineuninstalluserapp)
										.setPositiveButton(
												R.string.goonuninstall,
												new DialogInterface.OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														dialog.dismiss();
														// 把用户程序选出来进行卸载
														List<ListDataBean> mUserApps = new ArrayList<ListDataBean>();
														for (ListDataBean bean : list) {
															if (!bean.mIsSystemApp) {
																mUserApps
																		.add(bean);
															}
														}
														// 普通卸载
														for (ListDataBean bean : mUserApps) {
															AppUninstaller
																	.commonUninstall(
																			MainActivity.this,
																			bean.mInfo.packageName);
														}
														// 卸载完后要重新刷新列表
														mNeedToReLoadListWhenResume = true;
													}
												})
										.setNegativeButton(
												R.string.cancel,
												new DialogInterface.OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														dialog.dismiss();
													}
												});
								builder.show();
							} else {
								// root，提示用户卸载系统程序有风险
								AlertDialog.Builder builder = new AlertDialog.Builder(
										MainActivity.this)
										.setTitle(R.string.warning)
										.setMessage(
												R.string.systemappuninstallwarning)
										.setPositiveButton(
												R.string.goonuninstall,
												new DialogInterface.OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														dialog.dismiss();
														// 静默卸载
														SilentUninstallTaskThread taskThread = new SilentUninstallTaskThread(
																MainActivity.this,
																mHandler, list);
														taskThread.start();
													}
												})
										.setNegativeButton(
												R.string.cancel,
												new DialogInterface.OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														dialog.dismiss();
													}
												});
								builder.show();
							}
						}
					});
				}
			};
			thread.start();
		}
	}

	/**
	 * 卸载用户程序
	 */
	private void uninstallUserApp(final List<ListDataBean> list) {
		// 检查是否有选中的项目
		if (list == null || list.size() <= 0) {
			Toast.makeText(MainActivity.this, R.string.selectitem,
					Toast.LENGTH_SHORT).show();
			return;
		}
		final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		dialog.setMessage(getString(R.string.requestroot));
		dialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		if (RootShell.hasInit()) {
		} else if (mHasCheckRootRom && !mIsRootRom) {
		} else {
			dialog.show();
		}
		// 先请求root权限再卸载
		Thread thread = new Thread() {
			@Override
			public void run() {
				// 先获取root权限
				boolean root = true;
				if (!mHasCheckRootRom) {
					mIsRootRom = Util.isRootRom(MainActivity.this);
					mHasCheckRootRom = true;
				}
				if (mIsRootRom) {
					root = RootShell.isRootValid();
				} else {
					root = false;
				}
				final boolean root_ = root;
				runOnUiThread(new Runnable() {
					public void run() {
						dialog.dismiss();
						if (!root_) {
							// 普通卸载
							for (ListDataBean bean : list) {
								AppUninstaller.commonUninstall(
										MainActivity.this,
										bean.mInfo.packageName);
							}
							// 卸载完后要重新刷新列表
							mNeedToReLoadListWhenResume = true;
						} else {
							// 弹框确认卸载
							AlertDialog.Builder builder = new AlertDialog.Builder(
									MainActivity.this)
									.setTitle(R.string.warning)
									.setMessage(R.string.silentuninstalltip)
									.setPositiveButton(
											R.string.goonuninstall,
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													dialog.dismiss();
													// 静默卸载
													SilentUninstallTaskThread taskThread = new SilentUninstallTaskThread(
															MainActivity.this,
															mHandler, list);
													taskThread.start();
												}
											})
									.setNegativeButton(
											R.string.cancel,
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													dialog.dismiss();
												}
											});
							builder.show();
						}
					}
				});
			}
		};
		thread.start();
	}

	/**
	 * 卸载系统程序
	 */
	private void uninstallSystemApp(final List<ListDataBean> list) {
		// 检查是否有选中的项目
		if (list == null || list.size() <= 0) {
			Toast.makeText(MainActivity.this, R.string.selectitem,
					Toast.LENGTH_SHORT).show();
			return;
		}
		final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		dialog.setMessage(getString(R.string.requestroot));
		dialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		if (RootShell.hasInit()) {
		} else if (mHasCheckRootRom && !mIsRootRom) {
		} else {
			dialog.show();
		}
		// 先请求root权限再卸载
		Thread thread = new Thread() {
			@Override
			public void run() {
				// 先获取root权限
				boolean root = true;
				if (!mHasCheckRootRom) {
					mIsRootRom = Util.isRootRom(MainActivity.this);
					mHasCheckRootRom = true;
				}
				if (mIsRootRom) {
					root = RootShell.isRootValid();
				} else {
					root = false;
				}
				final boolean root_ = root;
				runOnUiThread(new Runnable() {
					public void run() {
						dialog.dismiss();
						if (!root_) {
							noRootError(R.string.systemappnorootmsg);
						} else {
							// 提示卸载系统应用有风险，建议使用禁用功能
							AlertDialog.Builder builder = new AlertDialog.Builder(
									MainActivity.this)
									.setTitle(R.string.warning)
									.setMessage(R.string.sysappuninstalltips)
									.setPositiveButton(
											R.string.goonuninstall,
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													dialog.dismiss();
													// 静默卸载
													SilentUninstallTaskThread taskThread = new SilentUninstallTaskThread(
															MainActivity.this,
															mHandler, list);
													taskThread.start();
												}
											})
									.setNegativeButton(
											R.string.cancel,
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													dialog.dismiss();
												}
											});
							builder.show();
						}
					}
				});
			}
		};
		thread.start();
	}

	/**
	 * 解冻应用
	 */
	private void unfreezeApp(final List<ListDataBean> list) {
		// 检查是否有选中的项目
		if (list == null || list.size() <= 0) {
			Toast.makeText(MainActivity.this, R.string.selectitem,
					Toast.LENGTH_SHORT).show();
			return;
		}
		final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		dialog.setMessage(getString(R.string.requestroot));
		dialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		if (RootShell.hasInit()) {
		} else if (mHasCheckRootRom && !mIsRootRom) {
		} else {
			dialog.show();
		}

		Thread thread = new Thread() {
			@Override
			public void run() {
				// 先获取root权限
				boolean root = true;
				if (!mHasCheckRootRom) {
					mIsRootRom = Util.isRootRom(MainActivity.this);
					mHasCheckRootRom = true;
				}
				if (mIsRootRom) {
					root = RootShell.isRootValid();
				} else {
					root = false;
				}
				final boolean root_ = root;
				runOnUiThread(new Runnable() {
					public void run() {
						dialog.dismiss();
						if (!root_) {
							noRootError(R.string.norootmsg);
						} else {
							AppTaskThread taskThread = new AppTaskThread(
									MainActivity.this,
									AppTaskThread.ACTION_UNFREEZE_APPS,
									mHandler, list);
							taskThread.start();
						}

					}
				});
			}
		};
		thread.start();
	}

	/**
	 * 冻结应用
	 */
	private void freezeApp(final List<ListDataBean> list) {
		// 检查是否有选中的项目
		if (list == null || list.size() <= 0) {
			Toast.makeText(MainActivity.this, R.string.selectitem,
					Toast.LENGTH_SHORT).show();
			return;
		}
		final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		dialog.setMessage(getString(R.string.requestroot));
		dialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		if (RootShell.hasInit()) {
		} else if (mHasCheckRootRom && !mIsRootRom) {
		} else {
			dialog.show();
		}

		Thread thread = new Thread() {
			@Override
			public void run() {
				// 先获取root权限
				boolean root = true;
				if (!mHasCheckRootRom) {
					mIsRootRom = Util.isRootRom(MainActivity.this);
					mHasCheckRootRom = true;
				}
				if (mIsRootRom) {
					root = RootShell.isRootValid();
				} else {
					root = false;
				}
				final boolean root_ = root;
				runOnUiThread(new Runnable() {
					public void run() {
						dialog.dismiss();
						if (!root_) {
							noRootError(R.string.norootmsg);
						} else {
							AlertDialog.Builder builder = new AlertDialog.Builder(
									MainActivity.this)
									.setTitle(R.string.tips)
									.setMessage(R.string.disabletips)
									.setPositiveButton(
											R.string.goon,
											new DialogInterface.OnClickListener() {

												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													dialog.dismiss();
													// 弹框提示
													AppTaskThread taskThread = new AppTaskThread(
															MainActivity.this,
															AppTaskThread.ACTION_FREEZE_APPS,
															mHandler, list);
													taskThread.start();
												}
											});
							builder.show();
						}

					}
				});
			}
		};
		thread.start();
	}

	/**
	 * 提示用户没有root权限
	 */
	private void noRootError(int msgID) {
		AlertDialog.Builder builder = new Builder(MainActivity.this);
		builder.setTitle(getString(R.string.noroottitle));
		builder.setMessage(getString(msgID));
		builder.setPositiveButton(getString(R.string.norootbtn),
				new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	/**
	 * 展示排序选项对话框，回收站的“按内存排序”相当于“按名字排序”
	 */
	private void showSortDialog(final String key,
			final List<ListDataBean> list, final IListAdapter adapter) {
		SharedPreferences preferences = getSharedPreferences(getPackageName(),
				MODE_PRIVATE);
		final int sortType = preferences.getInt(key, -1);
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(
				R.string.sort).setItems(R.array.sortitem,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						switch (which) {
						case 0:
							// 名字
							if (sortType == NAME_ASCENDING) {
								sortList(key, list, NAME_DESCENDING);
							} else {
								sortList(key, list, NAME_ASCENDING);
							}
							break;
						case 1:
							// 大小
							if (sortType == SIZE_ASCENDING) {
								sortList(key, list, SIZE_DESCENDING);
							} else {
								sortList(key, list, SIZE_ASCENDING);
							}
							break;
						case 2:
							// 日期
							if (sortType == DATE_ASCENDING) {
								sortList(key, list, DATE_DESCENDING);
							} else {
								sortList(key, list, DATE_ASCENDING);
							}
							break;
						case 3:
							// 运行内存
							if (sortType == MEMORY_ASCENDING) {
								sortList(key, list, MEMORY_DESCENDING);
							} else {
								sortList(key, list, MEMORY_ASCENDING);
							}
							break;
						default:
							break;
						}
						adapter.update(list);
					}
				});
		builder.show();
	}

	/**
	 * 对列表进行排序
	 */
	private void sortList(String key, List<ListDataBean> list, int sortType) {
		if (list == null) {
			return;
		}
		if (sortType == NAME_ASCENDING) {
			Collections.sort(list, new NameAscendingComparator());
		} else if (sortType == NAME_DESCENDING) {
			Collections.sort(list, new NameDescendingComparator());
		} else if (sortType == SIZE_ASCENDING) {
			Collections.sort(list, new SizeAscendingComparator());
		} else if (sortType == SIZE_DESCENDING) {
			Collections.sort(list, new SizeDescendingComparator());
		} else if (sortType == DATE_ASCENDING) {
			Collections.sort(list, new DateAscendingComparator());
		} else if (sortType == DATE_DESCENDING) {
			Collections.sort(list, new DateDescendingComparator());
		} else if (sortType == MEMORY_ASCENDING) {
			Collections.sort(list, new MemoryDescendingComparator());
		} else if (sortType == MEMORY_DESCENDING) {
			Collections.sort(list, new MemoryAscendingComparator());
		} else {
			// 默认名字升序
			sortType = DATE_DESCENDING;
			Collections.sort(list, new DateDescendingComparator());
		}
		SharedPreferences preferences = getSharedPreferences(getPackageName(),
				MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putInt(key, sortType);
		editor.commit();
	}

	/**
	 * 刷新列表(重新加载数据)
	 */
	private void refreshList() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// 更新可用内存信息
				String availableInternalMemorySize = Formatter.formatFileSize(
						MainActivity.this,
						Util.getAvailableInternalMemorySize());
				String totalInternalMemorySize = Formatter.formatFileSize(
						MainActivity.this, Util.getTotalInternalMemorySize());
				String msg = " " + availableInternalMemorySize + " "
						+ getString(R.string.remain) + "/"
						+ totalInternalMemorySize;
				mRomSpace.setText(msg);
			}
		});

		final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage(getString(R.string.readingapps));
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.show();

		new Thread() {
			public void run() {

				// 获取应用列表
				mUserListDataBean = new ArrayList<ListDataBean>();
				mSystemListDataBean = new ArrayList<ListDataBean>();
				mRecycleListDataBean = new ArrayList<ListDataBean>();

				MainDataController.getMainAppList(MainActivity.this,
						mUserListDataBean, mSystemListDataBean,
						mRecycleListDataBean, mHandler);

				// 对3个列表排序
				SharedPreferences preferences = getSharedPreferences(
						getPackageName(), MODE_PRIVATE);
				int userSortType = preferences.getInt(USER_LIST_SORT_KEY, -1);
				sortList(USER_LIST_SORT_KEY, mUserListDataBean, userSortType);
				int systemSortType = preferences.getInt(SYSTEM_LIST_SORT_KEY,
						-1);
				sortList(SYSTEM_LIST_SORT_KEY, mSystemListDataBean,
						systemSortType);
				int recycleSortType = preferences.getInt(
						RECYCLEBIN_LIST_SORT_KEY, -1);
				sortList(RECYCLEBIN_LIST_SORT_KEY, mRecycleListDataBean,
						recycleSortType);

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						dialog.dismiss();

						// 展示列表
						mUserAppAdapter.update(mUserListDataBean);

						mSystemAppAdapter.update(mSystemListDataBean);

						mRecycleBinAdapter.update(mRecycleListDataBean);

						new Thread() {
							public void run() {
								final Map<String, Integer> map = MainDataController
										.getRunningAppMemory(MainActivity.this);
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										if (mUserListDataBean != null) {
											for (ListDataBean bean : mUserListDataBean) {
												String pkgName = bean.mInfo.packageName;
												if (map.containsKey(pkgName)
														&& map.get(pkgName) > 0) {
													int size = map.get(pkgName);
													bean.mRunningMemoryInt = size;
													if (size >= 1024) {
														bean.mRunningMemory = ((int) (map
																.get(pkgName) / 1024.0f * 100.0f))
																/ 100.0f + "MB";
													} else {
														bean.mRunningMemory = size
																+ "KB";
													}
												}
											}
										}
										if (mSystemListDataBean != null) {
											for (ListDataBean bean : mSystemListDataBean) {
												String pkgName = bean.mInfo.packageName;
												if (map.containsKey(pkgName)
														&& map.get(pkgName) > 0) {
													int size = map.get(pkgName);
													bean.mRunningMemoryInt = size;
													if (size >= 1024) {
														bean.mRunningMemory = ((int) (map
																.get(pkgName) / 1024.0f * 100.0f))
																/ 100.0f + "MB";
													} else {
														bean.mRunningMemory = size
																+ "KB";
													}
												}
											}
										}
										// 展示列表
										mUserAppAdapter.notifyDataSetChanged();
										mSystemAppAdapter
												.notifyDataSetChanged();
									}
								});
							};
						}.start();
					}
				});
			};
		}.start();
	}

	/**
	 * 计算listview的宽度
	 */
	private int measurePopupMenuWidth(ListView listView) {
		if (listView == null) {
			return 0;
		}

		final BaseAdapter adapter = (BaseAdapter) listView.getAdapter();
		if (adapter == null) {
			return 0;
		}

		int width = 0;
		View itemView = null;
		int itemType = 0;
		final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0,
				MeasureSpec.UNSPECIFIED);
		final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0,
				MeasureSpec.UNSPECIFIED);

		int start = 0;
		final int end = adapter.getCount();
		for (int i = start; i < end; i++) {
			final int positionType = adapter.getItemViewType(i);
			if (positionType != itemType) {
				itemType = positionType;
				itemView = null;
			}
			itemView = adapter.getView(i, itemView, listView);
			if (itemView.getLayoutParams() == null) {
				itemView.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
			}
			itemView.measure(widthMeasureSpec, heightMeasureSpec);
			width = Math.max(width, itemView.getMeasuredWidth());
		}

		// Add background padding to measured width
		final Drawable background = listView.getBackground();
		Rect tempRect = new Rect();
		if (background != null) {
			background.getPadding(tempRect);
			width += tempRect.left + tempRect.right;
		}

		return width;
	}

	/**
	 * 生成弹出菜单
	 */
	private PopupWindow buildMenu() {
		String[] items = getResources().getStringArray(R.array.setting_more);
		PopAdapter adapter = new PopAdapter(this, Arrays.asList(items));

		ListView popListView = new ListView(MainActivity.this);
		popListView.setAdapter(adapter);
		popListView.setSelector(R.drawable.item_bg);
		popListView.setCacheColorHint(0);
		popListView.setDivider(getResources().getDrawable(
				R.drawable.listview_big_deliver));
		popListView.setDividerHeight(Util.dip2px(MainActivity.this, 0.5f));
		popListView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		popListView.setBackgroundColor(0xFF2e2e2e);

		final PopupWindow popMenu = new PopupWindow(MainActivity.this);
		popMenu.setContentView(popListView);
		// popMenu.setBackgroundDrawable(getResources().getDrawable(
		// R.drawable.pop_menu_bg));

		int width = measurePopupMenuWidth(popListView);
		Drawable popupBackground = popMenu.getBackground();
		if (popupBackground != null) {
			Rect tempRect = new Rect();
			popupBackground.getPadding(tempRect);
			width += tempRect.left + tempRect.right;
		}

		popMenu.setWidth(width);
		popMenu.setHeight(LayoutParams.WRAP_CONTENT);
		popMenu.setWindowLayoutMode(width, LayoutParams.WRAP_CONTENT);
		popMenu.setFocusable(true);
		popMenu.setAnimationStyle(-1);
		popMenu.setTouchable(true);
		popMenu.setOutsideTouchable(true);

		popListView.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU
						&& event.getAction() == KeyEvent.ACTION_UP) {
					hideMenu();
					return true;
				}
				return false;
			}
		});

		popListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				hideMenu();
				switch (position) {
				case 0:
					// 刷新
					refreshList();
					break;
				case 1:
					// 反馈
					Intent emailIntent = new Intent(
							android.content.Intent.ACTION_SEND);
					emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					String[] receiver = new String[] { "yijiajia1988@gmail.com" };
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
							receiver);
					String subject = "Root Unstaller Feedback";
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
							subject);
					String body = "\n\n";
					body += "\nTotalMemSize="
							+ (Util.getTotalInternalMemorySize() / 1024 / 1024)
							+ "MB";
					body += "\nAndroidVersion="
							+ android.os.Build.VERSION.RELEASE;
					body += "\nBoard=" + android.os.Build.BOARD;
					body += "\nFreeMemSize="
							+ (Util.getAvailableInternalMemorySize() / 1024 / 1024)
							+ "MB";
					body += "\nRom App Heap Size="
							+ Integer.toString((int) (Runtime.getRuntime()
									.maxMemory() / 1024L / 1024L)) + "MB";
					body += "\nROM=" + android.os.Build.DISPLAY;
					body += "\nKernel=" + Util.getLinuxKernel();
					body += "\nwidthPixels="
							+ getResources().getDisplayMetrics().widthPixels;
					body += "\nheightPixels="
							+ getResources().getDisplayMetrics().heightPixels;
					body += "\nDensity="
							+ getResources().getDisplayMetrics().density;
					body += "\ndensityDpi="
							+ getResources().getDisplayMetrics().densityDpi;
					body += "\nPackageName=" + getPackageName();
					body += "\nProduct=" + android.os.Build.PRODUCT;
					body += "\nPhoneModel=" + android.os.Build.MODEL;
					body += "\nDevice=" + android.os.Build.DEVICE + "\n\n";
					emailIntent.putExtra(Intent.EXTRA_TEXT, body);
					emailIntent.setType("plain/text");
					try {
						startActivity(emailIntent);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case 2:
					// 评分
					try {
						Uri uri = Uri.parse("market://details?id="
								+ getPackageName());
						Intent it = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(it);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case 3:
					// 分享
					String link = "Google Play : https://play.google.com/store/apps/details?id="
							+ getPackageName();
					final String extraText = getString(R.string.app_name)
							+ "  " + link;
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_SUBJECT, getResources()
							.getText(R.string.app_name));
					intent.putExtra(Intent.EXTRA_TEXT, extraText);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(Intent.createChooser(intent, "Share"));
					break;
				case 4:
					// 其他应用
					try {
						Uri uri = Uri.parse("market://search?q=pub:yijiajia");
						Intent it = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(it);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;

				default:
					break;
				}
			}
		});

		return popMenu;
	}

	private void hideMenu() {
		if (mMenu == null) {
			return;
		}
		mMenu.dismiss();
	}

	private void showOrHideMenu() {
		if (mMenu == null) {
			mMenu = buildMenu();
		}
		if (mMenu.isShowing()) {
			hideMenu();
		} else {
			int xoff = 0;
			final Drawable background = mMenu.getBackground();
			if (background != null) {
				Rect tempRect = new Rect();
				background.getPadding(tempRect);
				xoff = -tempRect.left;
			}
			mMenu.showAsDropDown(mSetting, xoff, 0);
		}
	}

	// @Override
	// public void onBackPressed() {
	// SharedPreferences sharedPreferences = getSharedPreferences(
	// getPackageName(), Context.MODE_PRIVATE);
	// boolean b = sharedPreferences.getBoolean("notshowdialog", false);
	// if (b) {
	// super.onBackPressed();
	// } else {
	// SpannableString text = new SpannableString(getResources()
	// .getString(R.string.gonow));
	// text.setSpan(new ForegroundColorSpan(0xbb0000ff), 0, text.length(),
	// 0);
	//
	// new AlertDialog.Builder(MainActivity.this)
	// .setTitle(getResources().getString(R.string.app_name))
	// .setCancelable(true)
	// .setMessage(getResources().getString(R.string.exittitlemsg))
	// .setPositiveButton(text,
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog,
	// int which) {
	// SharedPreferences sharedPreferences = getSharedPreferences(
	// getPackageName(),
	// Context.MODE_PRIVATE);
	// Editor editor = sharedPreferences.edit();
	// editor.putBoolean("notshowdialog", true);
	// editor.commit();
	//
	// try {
	// Uri uri = Uri
	// .parse("market://details?id="
	// + getPackageName());
	// Intent it = new Intent(
	// Intent.ACTION_VIEW, uri);
	// startActivity(it);
	// } catch (Exception e) {
	// e.printStackTrace();
	// finish();
	// }
	// }
	// })
	// .setNegativeButton(
	// getResources().getString(R.string.nexttime),
	// new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog,
	// int which) {
	// finish();
	// }
	// }).show();
	// }
	// }

	@Override
	protected void onDestroy() {
		adView.destroy();
		super.onDestroy();
		// // 杀掉进程
		// android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void onPause() {
		adView.pause();
		super.onPause();
	}
}

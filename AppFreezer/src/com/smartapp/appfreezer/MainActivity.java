package com.smartapp.appfreezer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.view.KeyEvent;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.smartapp.appfreezer.ui.Aa;
import com.smartapp.appfreezer.ui.ScreenScroller;
import com.smartapp.appfreezer.ui.ScreenScrollerListener;
import com.smartapp.appfreezer.ui.UIUtil;
import com.smartapp.appfreezer.ui.Xkds;

public class MainActivity extends Activity implements ScreenScrollerListener {
	/**
	 * 冻结应用action
	 */
	public static final int ACTION_FREEZE_APPS = 0x1001;
	/**
	 * 解冻应用action
	 */
	public static final int ACTION_UNFREEZE_APPS = 0x1002;

	private Aa mScrollerViewGroup;
	/**
	 * 顶部tab栏
	 */
	private Xkds mTabsBar = null;

	/**
	 * 已启用listview
	 */
	private ListView mEnableListView = null;
	private IAdapter mEnableAdapter = null;

	/**
	 * 已禁用listview
	 */
	private ListView mDisableListView = null;
	private IAdapter mDisableAdapter = null;

	private Button mFreezeBtn = null;
	/**
	 * 冻结按钮
	 */
	private ViewGroup mFreezeBtnFrame = null;

	private Button mUnFreezeBtn = null;
	/**
	 * 解冻按钮
	 */
	private ViewGroup mUnFreezeBtnFrame = null;
	/**
	 * 设置按钮
	 */
	private ImageButton mSettingButton;

	/**
	 * 弹出菜单
	 */
	private PopupWindow mMenu;

	/**
	 * 更新冻结/解冻按钮状态
	 */
	public static final int MSG_REFRESH_BTN = 1000;
	/**
	 * 刷新列表
	 */
	public static final int MSG_REFRESH_LIST = 1001;

	private FrameLayout mADContainer;

	/**
	 * 广告条
	 */
	private AdView adView;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_REFRESH_BTN:
				refreshBtn();
				break;
			case MSG_REFRESH_LIST:
				refreshList();
				break;
			default:
				break;
			}

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		mADContainer = (FrameLayout) findViewById(R.id.adcontainer);

		// 创建adView。
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-6335053266754945/1384266111");
		adView.setAdSize(AdSize.BANNER);
		// 在其中添加 adView
		mADContainer.addView(adView);
		// 启动一般性请求。
		AdRequest adRequest = new AdRequest.Builder().build();
		// 在adView中加载广告请求。
		adView.loadAd(adRequest);

		initScrollView();
		initTabView();

		mSettingButton = (ImageButton) findViewById(R.id.setting);
		mSettingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showOrHideMenu();
			}
		});

		mEnableListView = (ListView) findViewById(R.id.enable_listview);
		mEnableAdapter = new IAdapter(MainActivity.this, mHandler);
		mEnableListView.setAdapter(mEnableAdapter);
		mEnableListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 按下整项改变checkbox
				ListDataBean bean = (ListDataBean) view.getTag();
				bean.mIsSelect = !bean.mIsSelect;
				mEnableAdapter.notifyDataSetChanged();

				refreshBtn();
			}
		});

		mDisableListView = (ListView) findViewById(R.id.disable_listview);
		mDisableAdapter = new IAdapter(MainActivity.this, mHandler);
		mDisableListView.setAdapter(mDisableAdapter);
		mDisableListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 按下整项改变checkbox
				ListDataBean bean = (ListDataBean) view.getTag();
				bean.mIsSelect = !bean.mIsSelect;
				mDisableAdapter.notifyDataSetChanged();

				refreshBtn();
			}
		});
		// 刷新已启用和已禁用列表
		refreshList();

		mFreezeBtn = (Button) findViewById(R.id.freeze_btn);
		TextPaint tp = mFreezeBtn.getPaint();
		tp.setFakeBoldText(true);
		mFreezeBtn.setClickable(false);
		mUnFreezeBtn = (Button) findViewById(R.id.unfreeze_btn);
		TextPaint tp2 = mUnFreezeBtn.getPaint();
		tp2.setFakeBoldText(true);
		mUnFreezeBtn.setClickable(false);

		mFreezeBtnFrame = (ViewGroup) findViewById(R.id.freeze_btn_frame);
		mUnFreezeBtnFrame = (ViewGroup) findViewById(R.id.unfreeze_btn_frame);

		mFreezeBtnFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final ProgressDialog dialog = new ProgressDialog(
						MainActivity.this);
				dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
				dialog.setMessage(getString(R.string.requestroot));
				dialog.setCancelable(false);// 设置进度条是否可以按退回键取消
				if (RootShell.hasInit()) {
				} else {
					dialog.show();
				}

				Thread thread = new Thread() {
					@Override
					public void run() {
						// 先获取root权限
						boolean root = true;
						try {
							RootShell shell = RootShell.getInstance();
							if (shell != null) {
								root = shell.isRootValid();
							} else {
								root = false;
							}
						} catch (Exception e) {
							e.printStackTrace();
							root = false;
						}
						final boolean root_ = root;
						runOnUiThread(new Runnable() {
							public void run() {
								dialog.dismiss();
								if (!root_) {
									noRootError();
								} else {
									AppTaskThread thread = new AppTaskThread(
											MainActivity.this,
											ACTION_FREEZE_APPS, mHandler,
											mEnableAdapter.getSelectBeans());
									thread.start();
								}
							}
						});
					}
				};
				thread.start();

			}
		});

		mUnFreezeBtnFrame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final ProgressDialog dialog = new ProgressDialog(
						MainActivity.this);
				dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
				dialog.setMessage(getString(R.string.requestroot));
				dialog.setCancelable(false);// 设置进度条是否可以按退回键取消
				if (RootShell.hasInit()) {
				} else {
					dialog.show();
				}

				Thread thread = new Thread() {
					@Override
					public void run() {
						// 先获取root权限
						boolean root = true;
						try {
							RootShell shell = RootShell.getInstance();
							if (shell != null) {
								root = shell.isRootValid();
							} else {
								root = false;
							}
						} catch (Exception e) {
							e.printStackTrace();
							root = false;
						}
						final boolean root_ = root;
						runOnUiThread(new Runnable() {
							public void run() {
								dialog.dismiss();
								if (!root_) {
									noRootError();
								} else {
									AppTaskThread thread = new AppTaskThread(
											MainActivity.this,
											ACTION_UNFREEZE_APPS, mHandler,
											mDisableAdapter.getSelectBeans());
									thread.start();
								}
							}
						});
					}
				};
				thread.start();

			}
		});

		// 更新冻结/解冻按钮状态
		refreshBtn();
	}

	/**
	 * 提示用户没有root权限
	 */
	private void noRootError() {
		AlertDialog.Builder builder = new Builder(MainActivity.this);
		builder.setTitle(getString(R.string.noroottitle));
		builder.setMessage(getString(R.string.norootmsg));
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
	 * 更新冻结/解冻按钮状态
	 */
	private void refreshBtn() {
		int freezeCount = mEnableAdapter.getSelectCount();
		int unfreezeCount = mDisableAdapter.getSelectCount();

		if (freezeCount <= 0) {
			mFreezeBtn.setTextColor(0xFF909090);
			mFreezeBtnFrame.setEnabled(false);
		} else {
			mFreezeBtn.setTextColor(0xFF31b6e6);
			mFreezeBtnFrame.setEnabled(true);
		}
		mFreezeBtn
				.setText(getString(R.string.freeze) + "(" + freezeCount + ")");

		if (unfreezeCount <= 0) {
			mUnFreezeBtn.setTextColor(0xFF909090);
			mUnFreezeBtnFrame.setEnabled(false);
		} else {
			mUnFreezeBtn.setTextColor(0xFF31b6e6);
			mUnFreezeBtnFrame.setEnabled(true);
		}
		mUnFreezeBtn.setText(getString(R.string.unfreeze) + "(" + unfreezeCount
				+ ")");
	}

	/**
	 * 刷新已启用和已禁用列表
	 */
	private void refreshList() {
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		dialog.setMessage(getString(R.string.wait));
		dialog.setCancelable(false);// 设置进度条是否可以按退回键取消
		dialog.show();

		Thread thread = new Thread() {
			@Override
			public void run() {
				final List<ListDataBean> enableList = new ArrayList<ListDataBean>();
				final List<ListDataBean> disableList = new ArrayList<ListDataBean>();
				AppFreezer.getEnableAndDisableAppByApi(MainActivity.this,
						enableList, disableList);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						dialog.dismiss();
						mEnableAdapter.update(enableList);
						mDisableAdapter.update(disableList);
					}
				});

			}
		};
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}

	/**
	 * 初始化滚动功能页
	 */
	private void initScrollView() {
		mScrollerViewGroup = (Aa) findViewById(R.id.scrollerPageView);
		mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
		mScrollerViewGroup.setScreenScrollerListener(this);
		mScrollerViewGroup.gotoViewByIndex(0);

	}

	/**
	 * 初始化tab页
	 */
	private void initTabView() {
		FrameLayout tabFrame = (FrameLayout) findViewById(R.id.tabbar_frame);
		mTabsBar = new Xkds(MainActivity.this, new Xkds.TabObserver() {

			@Override
			public void handleChangeTab(int tabIndex) {
				// 控制scrollerViewGroup跳转
				mScrollerViewGroup.gotoViewByIndex(tabIndex);
			}
		});
		tabFrame.addView(mTabsBar);
		mTabsBar.cleanData();
		List<String> titles = new ArrayList<String>();
		titles.add(MainActivity.this.getResources().getString(R.string.enable));
		titles.add(MainActivity.this.getResources().getString(R.string.disable));
		mTabsBar.initTabsBar(titles);
		mTabsBar.setButtonSelected(0, false);

		mTabsBar.setBackgroundColor(0xFF202020);
	}

	@Override
	public void onPause() {
		adView.pause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		adView.resume();
	}

	@Override
	protected void onDestroy() {
		adView.destroy();
		super.onDestroy();
//		// 杀掉进程
//		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return null;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {

	}

	@Override
	public void onFlingIntercepted() {

	}

	@Override
	public void onScrollStart() {

	}

	@Override
	public void onFlingStart() {

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mTabsBar.setButtonSelected(newScreen, true);
	}

	@Override
	public void onScrollFinish(int currentScreen) {

	}

	@Override
	public void postInvalidate() {

	}

	@Override
	public void scrollBy(int x, int y) {

	}

	@Override
	public int getScrollX() {
		return 0;
	}

	@Override
	public int getScrollY() {
		return 0;
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
		popListView.setDividerHeight(UIUtil.dip2px(MainActivity.this, 0.5f));
		popListView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		final PopupWindow popMenu = new PopupWindow(MainActivity.this);
		popMenu.setContentView(popListView);
		popMenu.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.pop_menu_bg));

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
					try {
						Intent intent = new Intent();
						intent.setClassName("com.android.settings",
								"com.android.settings.RunningServices");
						startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case 2:
					// 反馈
					Intent emailIntent = new Intent(
							android.content.Intent.ACTION_SEND);
					emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					String[] receiver = new String[] { "yijiajia1988@gmail.com" };
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
							receiver);
					String subject = "App Freezer Feedback";
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
							subject);
					String body = "\n\n";
					body += "\nTotalMemSize="
							+ (AppFreezer.getTotalInternalMemorySize() / 1024 / 1024)
							+ "MB";
					body += "\nAndroidVersion="
							+ android.os.Build.VERSION.RELEASE;
					body += "\nBoard=" + android.os.Build.BOARD;
					body += "\nFreeMemSize="
							+ (AppFreezer.getAvailableInternalMemorySize() / 1024 / 1024)
							+ "MB";
					body += "\nRom App Heap Size="
							+ Integer.toString((int) (Runtime.getRuntime()
									.maxMemory() / 1024L / 1024L)) + "MB";
					body += "\nROM=" + android.os.Build.DISPLAY;
					body += "\nKernel=" + AppFreezer.getLinuxKernel();
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
				case 3:
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
				case 4:
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
				case 5:
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
			mMenu.showAsDropDown(mSettingButton, xoff, 0);
		}
	}

}

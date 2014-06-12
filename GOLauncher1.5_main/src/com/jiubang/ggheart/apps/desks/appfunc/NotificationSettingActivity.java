package com.jiubang.ggheart.apps.desks.appfunc;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.notification.NotificationControler;
import com.jiubang.ggheart.plugin.notification.NotificationInvoke;
import com.jiubang.ggheart.plugin.notification.NotificationType;

/**
 * 通讯统计设置Activity
 * @author wuziyi
 *
 */
//CHECKSTYLE:OFF
public class NotificationSettingActivity extends Activity
		implements
			OnClickListener,
			OnItemClickListener,
			IMessageHandler {

	// private static final int INITFINISH = 0;
	private static final int MORE_APP_TIP_ID = -3;
	private static final int SUB_TITLE_ID = -2;
	private static final int NORMAL_ITEM = 0;
	private static final int SUB_TITLE = 1;
	//	private static final int NONE_ITEM = 2;
	private static final String NOTIFICATION_TIP = "noticationTip";

//	private static final int MORE_APPLICATION = 0;
	/**
	 * 更多应用的ID从6开始
	 */
	private static final int MORE_APP_ID = 6;

	/**
	 * 原始六个通讯统计应用的已安装个数
	 */
	private int mInstalledNum;
	private Button notificationok, notificationcancle;
	private ListView mListView;
	private LayoutInflater mInflater;
	private RelativeLayout mContentLayout;
	private NotificationControler mNotificationControler;
//	private boolean mIsShowMoreAppTip;
	private ArrayList<NotificationSettingItem> mMoreAppItemList;

	private MyAdapter mAdapter;
	private boolean mIsStartAccessibility;
	/**
	 * 是否首次启动辅助功能标记
	 */
//	private boolean mIsFirstStart;
	private Context mContext;
	/**
	 * 提交时，与hashmap的勾选记录比较，获得是否改变了更多应用的选择
	 */
	private boolean mIsChangeSelected;

	/**
	 * 整个列表的数据（包含小标题，未读消息中的未安装应用）
	 * <br>注意：小标题只有title属性，其余为null
	 */
	private ArrayList<NotificationSettingItem> mListItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.notification_setting_list);
		mContentLayout = (RelativeLayout) findViewById(R.id.contentview);
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(R.string.menuitem_notification);
		notificationok = (Button) findViewById(R.id.notificationok);
		notificationcancle = (Button) findViewById(R.id.notificationcancle);
		mListView = (ListView) findViewById(R.id.notificationlist);
		notificationok.setOnClickListener(this);
		notificationcancle.setOnClickListener(this);
		mListView.setOnItemClickListener(this);
		mInflater = LayoutInflater.from(this);
		mListItems = new ArrayList<NotificationSettingItem>();
		AppCore appCore = AppCore.getInstance();
		if (appCore == null) {
			finish();
		} else {
			mNotificationControler = appCore.getNotificationControler();
			GoLauncher.registMsgHandler(this);
		}
	}

	@Override
	protected void onDestroy() {
		GoLauncher.unRegistMsgHandler(this);
		mInstalledNum = 0;
		super.onDestroy();
	}

	private void initList() {
		if (mListItems != null && !mListItems.isEmpty()) {
			mListItems.clear();
		}
		// 内容不多，不增加显示加载中对话框。
		boolean[] checkboxstauts = new boolean[] { ShortCutSettingInfo.mAutoMessageStatistic,
				ShortCutSettingInfo.mAutoMisscallStatistic,
				ShortCutSettingInfo.mAutoMissmailStatistic,
				ShortCutSettingInfo.mAutoMissk9mailStatistic,
//				ShortCutSettingInfo.mAutoMissfacebookStatistic,
				ShortCutSettingInfo.mAutoMissSinaWeiboStatistic, };
		String[] packageNames = { "SMS", "Call", PackageName.GMAIL,
				PackageName.K9MAIL, PackageName.SINA_WEIBO };
		int[] ids = new int[] { NotificationType.NOTIFICATIONTYPE_SMS,
				NotificationType.NOTIFICATIONTYPE_CALL,
				NotificationType.NOTIFICATIONTYPE_GMAIL,
				NotificationType.NOTIFICATIONTYPE_K9MAIL,
				NotificationType.NOTIFICATIONTYPE_SinaWeibo };
		//		PackageName.FACEBOOK,
		String[] titles = getResources().getStringArray(R.array.communication_arry);
		ArrayList<NotificationSettingItem> notInstalledAppList = new ArrayList<NotificationSettingItem>();
		ArrayList<NotificationSettingItem> installedAppList = new ArrayList<NotificationSettingItem>();
		ArrayList<NotificationSettingItem> selectedAppList = new ArrayList<NotificationSettingItem>();
		int size = packageNames.length;
		mInstalledNum = 0;
		// 电话、短信、Gmail使用自定义图标
		for (int i = 0; i < size; i++) {
			NotificationSettingItem item = new NotificationSettingItem();
			item.setPackageName(packageNames[i]);
			item.setCheckBoxStatus(checkboxstauts[i]);
			item.setTitle(titles[i]);
			item.setItemId(ids[i]);
			item.setClickable(false);
			if (item.getPackageName().equals("SMS")) { // 最上两项是电话和短信，固定属于已安装
				item.setInstalled(true);
				item.setIcon(getResources().getDrawable(R.drawable.messaging_4_def3));
			} else if (item.getPackageName().equals("Call")) {
				item.setInstalled(true);
				item.setIcon(getResources().getDrawable(R.drawable.phone_4_def3));
			} else if (item.getPackageName().equals(PackageName.GMAIL)) {
				if (AppUtils.isAppExist(this, PackageName.GMAIL)) {
					item.setInstalled(true);
					item.setIcon(getResources().getDrawable(R.drawable.gmail_4_def3));
				} else {
					item.setInstalled(false);
					item.setIcon(null);
				}
			} else {
				if (AppUtils.isAppExist(this, item.getPackageName())) {
					item.setInstalled(true);
					try {
						item.setIcon(getPackageManager().getApplicationIcon(item.getPackageName()));
					} catch (NameNotFoundException e) {
						item.setIcon(null);
					}
				} else {
					item.setInstalled(false);
					item.setIcon(null);
				}
			}
			if (item.isInstalled) {
				// 已安装的应用列表
				if (item.getCheckBoxStatus()) {
					selectedAppList.add(item);
				} else {
					installedAppList.add(item);
					//					mListItems.add(item);
				}
				mInstalledNum++;
			} else {
				// 未安装的应用列表
				notInstalledAppList.add(item);
			}
		}
		// 未读消息小标题
		NotificationSettingItem subTitle = new NotificationSettingItem();
		subTitle.setItemId(SUB_TITLE_ID);
		subTitle.setTitle(getString(R.string.notification_setting_subtitle));
		mListItems.add(subTitle);
		// 原始安装列表，勾选的排上面
		mListItems.addAll(selectedAppList);
		mListItems.addAll(installedAppList);
		if (!mIsStartAccessibility) {
			try {
				mNotificationControler.clearAllNotificationAppItems();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
		mNotificationControler.initNotificationHashMap();
		//			ArrayList<AppItemInfo> appItems = mNotificationControler.getNotificationAppItems();
		ArrayList<AppItemInfo> appItems = mNotificationControler
				.getAllAppItemInfos();
		if (appItems != null && !appItems.isEmpty()) {
			// 更多应用小标题
			NotificationSettingItem moreTitle = new NotificationSettingItem();
			moreTitle.setItemId(SUB_TITLE_ID);
			moreTitle.setTitle(getString(R.string.notification_setting_subtitle_more));
			mListItems.add(moreTitle);
			// 新增应用列表
			// 开启辅助功能提示 3.21恒定放置在更多应用列表的第一位（不管开启与否）
			NotificationSettingItem tipItem = new NotificationSettingItem();
			tipItem.setTitle(getString(R.string.notification_setting_accessbility_title));
			tipItem.setItemId(MORE_APP_TIP_ID);
			tipItem.setInstalled(false);
			tipItem.setClickable(false);
			mListItems.add(tipItem);
			ArrayList<String> intentList = new ArrayList<String>();
			ArrayList<String> appTitles = new ArrayList<String>();
			for (int j = 0; j < appItems.size(); j++) {
				appTitles.add(appItems.get(j).getTitle());
				intentList.add(appItems.get(j).mIntent.toURI());
			}
			initMoreAppList(intentList, appTitles);
			mListItems.addAll(mMoreAppItemList);
		} 
		setAdapter();
	}

//	private void addFooterMoreItem() {
//		View loadMoreView = getLayoutInflater().inflate(R.layout.notification_setting_footer_item,
//				null);
//		mListView.addFooterView(loadMoreView);
//		//		}
//	}

	/**
	 * 适配器
	 *
	 */
	private class MyAdapter extends BaseAdapter {

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			int type = getItemViewType(position);
			final NotificationSettingItem item = mListItems.get(position);
			ItemViewHolder itemViewHolder = null;
			SubTitleHolder subTitleHolder = null;
			if (convertView == null) {
				try {
					switch (type) {
						case NORMAL_ITEM :
							convertView = mInflater.inflate(R.layout.notification_setting_item,
									parent, false);
							if (item.getClickable()) {
								convertView.setClickable(true);
							} else {
								convertView.setClickable(false);
							}
							itemViewHolder = new ItemViewHolder();
							itemViewHolder.appIcon = (ImageView) convertView
									.findViewById(R.id.notificationAppIcon);
							itemViewHolder.appTitle = (TextView) convertView
									.findViewById(R.id.notificationAppTitle);
							itemViewHolder.appSummry = (TextView) convertView
									.findViewById(R.id.notificationAppSummry);
							itemViewHolder.checkStatus = (CheckBox) convertView
									.findViewById(R.id.notificationCheckBox);
							itemViewHolder.numImg = (ImageView) convertView
									.findViewById(R.id.numImageView);
							convertView.setTag(itemViewHolder);
							break;

						case SUB_TITLE :
							convertView = mInflater.inflate(R.layout.notification_setting_subtitle,
									parent, false);
							convertView.setClickable(true);
							subTitleHolder = new SubTitleHolder();
							subTitleHolder.subTitle = (TextView) convertView
									.findViewById(R.id.notification_setting_subtitle);
							convertView.setTag(subTitleHolder);
							break;

						default :
							break;
					}
				} catch (InflateException e) {
					e.printStackTrace();
					return null;
				}
			} else {
				// 绑定控件
				switch (type) {
					case NORMAL_ITEM :
						if (item.getClickable()) {
							convertView.setClickable(true);
						} else {
							convertView.setClickable(false);
						}
						itemViewHolder = (ItemViewHolder) convertView.getTag();
						break;

					case SUB_TITLE :
						convertView.setClickable(true);
						subTitleHolder = (SubTitleHolder) convertView.getTag();
						break;

					default :
						break;
				}
			}
			switch (type) {
				case NORMAL_ITEM :
					if (item.getClickable()) {
						itemViewHolder.checkStatus.setEnabled(false);
						ColorMatrix cm = new ColorMatrix();
						cm.setSaturation(0);
						ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);
						itemViewHolder.appIcon.setColorFilter(cf);
						itemViewHolder.numImg.setColorFilter(cf);
					} else {
						itemViewHolder.checkStatus.setEnabled(true);
						// 在三星I8530 2.3.6系统的机子出现置灰后再次开启图片颜色一直处于灰色状态，将setColorFilter（null）改成如下：
						ColorMatrix colorMatrix = new ColorMatrix();
						itemViewHolder.appIcon.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
						itemViewHolder.numImg.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
						itemViewHolder.checkStatus.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if (v instanceof CheckBox) {
									CheckBox checkBox = (CheckBox) v;
									//								NotificationSettingItem item = mListItems.get(position);
									item.setCheckBoxStatus(checkBox.isChecked());
									if (item.getItemId() == NotificationType.NOTIFICATIONTYPE_SinaWeibo) {
										if (item.getCheckBoxStatus()) {
											NotificationInvoke.startSinaWeiboMonitor(mContext);
										} else {
											NotificationInvoke.stopSinaWeiboMonitor(mContext);
										}
									} else if (item.getItemId() == NotificationType.NOTIFICATIONTYPE_FACEBOOK) {
										if (item.getCheckBoxStatus()) {
											NotificationInvoke.startFacebookMonitor(mContext);
										}
									}
								}
							}
						});
					}
					itemViewHolder.appTitle.setText(item.getTitle());
					if (item.isInstalled) {
						itemViewHolder.appIcon.setVisibility(View.VISIBLE);
						itemViewHolder.numImg.setVisibility(View.VISIBLE);
						itemViewHolder.checkStatus.setVisibility(View.VISIBLE);
						itemViewHolder.checkStatus.setChecked(item.getCheckBoxStatus());
						itemViewHolder.appTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
						itemViewHolder.appSummry.setVisibility(View.GONE);
						if (item.getIcon() != null) {
							itemViewHolder.appIcon.setImageDrawable(item.getIcon());
						}
						Drawable mNumPic= getNotificationNumPic();
						if (mNumPic != null) {
							itemViewHolder.numImg.setImageDrawable(mNumPic);
						}
						if (item.getPackageName().equals(PackageName.GMAIL)) { // Gmail特殊显示
							if (!AppUtils.canReadGmailLabels(NotificationSettingActivity.this)) {
								if (Build.VERSION.SDK_INT >= 11) {
									itemViewHolder.appSummry.setText(R.string.gmailtip_4_0);
								} else {
									itemViewHolder.appSummry.setText(R.string.gmailtip_2_0);
								}
								itemViewHolder.appSummry.setVisibility(View.VISIBLE);
								itemViewHolder.checkStatus.setChecked(false);
								itemViewHolder.checkStatus.setClickable(false);
							}
						}
					} else {
						// 原本用来显示为安装应用和开启辅助功能的实现，3.21开启辅助功能的界面调整，仅用来实现辅助功能界面特殊显示
						itemViewHolder.checkStatus.setVisibility(View.GONE);
						itemViewHolder.appIcon.setVisibility(View.GONE);
						itemViewHolder.numImg.setVisibility(View.GONE);
						itemViewHolder.appSummry.setVisibility(View.VISIBLE);
//						itemViewHolder.appTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
						if (mIsStartAccessibility) {
							itemViewHolder.appSummry.setText(R.string.notification_setting_accessbility_tip_open);
						} else {
							itemViewHolder.appSummry.setText(R.string.notification_setting_accessbility_tip_close);
						}
					}
					break;

				case SUB_TITLE :
					subTitleHolder.subTitle.setText(item.getTitle());
					break;

				default :
					break;
			}

			return convertView;
		}

		@Override
		public int getCount() {
			return mListItems.size();
		}

		@Override
		public int getItemViewType(int position) {
			// TODO 大概2种类型
			int type = NORMAL_ITEM;
			if (position == 0 || position == mInstalledNum + 1) {
				type = SUB_TITLE;
			}
			return type;
		}

		@Override
		public int getViewTypeCount() {
			// 布局类型的总数
			return 2;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			//			return mListItems.get(position).getItemId();
			return position;
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.notificationok :
				final ArrayList<String> addItemList = new ArrayList<String>();
				final ArrayList<Intent> addIntentList = new ArrayList<Intent>();
				//			ArrayList<Intent> delIntentList = new ArrayList<Intent>();
				for (NotificationSettingItem item : mListItems) {
					int type = item.getItemId();
					switch (type) {
						case NotificationType.NOTIFICATIONTYPE_SMS :
							ShortCutSettingInfo.setAutoMessageStatistic(item.getCheckBoxStatus());
							break;

						case NotificationType.NOTIFICATIONTYPE_CALL :
							ShortCutSettingInfo.setAutoMisscallStatistic(item.getCheckBoxStatus());
							break;

						case NotificationType.NOTIFICATIONTYPE_GMAIL :
							ShortCutSettingInfo.setAutoMissmailStatistic(item.getCheckBoxStatus());
							break;

						case NotificationType.NOTIFICATIONTYPE_K9MAIL :
							ShortCutSettingInfo
									.setAutoMissk9mailStatistic(item.getCheckBoxStatus());
							break;

						case NotificationType.NOTIFICATIONTYPE_FACEBOOK :
							ShortCutSettingInfo.setAutoMissfacebookStatistic(item
									.getCheckBoxStatus());
							break;

						case NotificationType.NOTIFICATIONTYPE_SinaWeibo :
							ShortCutSettingInfo.setAutoMissSinaWeiboStatistic(item
									.getCheckBoxStatus());
							break;

						default :
							if (item.getCheckBoxStatus()) {
								addIntentList.add(item.getIntent());
								// 任意一个应用与原来的选择发生改变
								if (!mNotificationControler.isMoreApp(item.getIntent())) {
									mIsChangeSelected = true;
								}
								// google talk的监听包名比较特殊，跟图标对应的包名的不一致
								if (item.getPackageName().equals(
										PackageName.GOOGLE_TALK_ANDROID_TALK)) {
									addItemList.add(PackageName.GOOGLE_TALK_ANDROID_GSF);
								} else {
									addItemList.add(item.getPackageName());
								}
							} else {
								// 任意一个应用与原来的选择发生改变
								if (mNotificationControler.isMoreApp(item.getIntent())) {
									mIsChangeSelected = true;
								}
							}
							break;
					}
				}
//				PreferencesManager sharedPreferences = new PreferencesManager(mContext,
//						IPreferencesIds.NOTIFICATION_MORE_APP_TIP, Context.MODE_PRIVATE);
//				mIsShowMoreAppTip = sharedPreferences.getBoolean(
//						IPreferencesIds.SHOULD_SHOW_MORE_APP_TIP, true);
				if (mIsChangeSelected) {
//					if (mIsShowMoreAppTip) {
//						final LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(
//								R.layout.notification_show_tip_dialog, null);
//						final CheckBox checkbox = (CheckBox) layout
//								.findViewById(R.id.notification_tip_checkBox);
//						// 出现功能解释弹窗
//						new AlertDialog.Builder(this)
//								.setTitle(R.string.notification_setting_tip_title)
//								.setView(layout)
//								.setMessage(R.string.notification_setting_tip_content)
//								.setIcon(android.R.drawable.ic_dialog_alert)
//								.setPositiveButton(R.string.ok,
//										new DialogInterface.OnClickListener() {
//
//											@Override
//											public void onClick(DialogInterface dialog, int which) {
//												if (checkbox.isChecked()) {
//													PreferencesManager sharedPreferences = new PreferencesManager(
//															mContext,
//															IPreferencesIds.NOTIFICATION_MORE_APP_TIP,
//															Context.MODE_PRIVATE);
//													sharedPreferences
//															.putBoolean(
//																	IPreferencesIds.SHOULD_SHOW_MORE_APP_TIP,
//																	false);
//													sharedPreferences.commit();
//												}
//												commitResult(addItemList, addIntentList);
//											}
//										})
//								.setNegativeButton(R.string.cancel,
//										new DialogInterface.OnClickListener() {
//
//											@Override
//											public void onClick(DialogInterface dialog, int which) {
//												// TODO Auto-generated method stub
//												mIsChangeSelected = false;
//											}
//
//										}).show();
//					} else {
					commitResult(addItemList, addIntentList);
//					}
				} else {
					commitResult(null, null);
				}
				break;
			case R.id.notificationcancle :
				finish();
				break;
			default :
				break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		android.view.ViewGroup.LayoutParams layoutParams = mContentLayout.getLayoutParams();
		layoutParams.height = (int) getResources().getDimension(R.dimen.folder_edit_view_height);
		layoutParams.width = (int) getResources().getDimension(R.dimen.folder_edit_view_width);
		mContentLayout.setLayoutParams(layoutParams);
	}

	private void setAdapter() {
		mAdapter = new MyAdapter();
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mListItems.get(position).getItemId() == MORE_APP_TIP_ID) {
			try {
				Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
				startActivity(intent);
			} catch (Exception e) {
				// 如果无辅助功能的rom
				DeskToast.makeText(mContext, R.string.notification_setting_noaccessbility,
						Toast.LENGTH_SHORT).show();
			}
			return;
		}
		NotificationSettingItem item = mListItems.get(position);
		if (item.isInstalled) {
			if (item.getItemId() == NotificationType.NOTIFICATIONTYPE_GMAIL) { // Gmail特殊显示
				if (!AppUtils.canReadGmailLabels(NotificationSettingActivity.this)) {
					return;
				}
			} else if (item.getItemId() == NotificationType.NOTIFICATIONTYPE_SinaWeibo) {
				if (!item.getCheckBoxStatus()) {
					NotificationInvoke.startSinaWeiboMonitor(this);
				} else {
					NotificationInvoke.stopSinaWeiboMonitor(this);
				}
			} else if (item.getItemId() == NotificationType.NOTIFICATIONTYPE_FACEBOOK) {
				if (!item.getCheckBoxStatus()) {
					NotificationInvoke.startFacebookMonitor(this);
				}
			}
			CheckBox checkStatus = (CheckBox) view.findViewById(R.id.notificationCheckBox);
			checkStatus.toggle();
			item.setCheckBoxStatus(checkStatus.isChecked());
		}
	}

	/**
	 * 通讯统计设置信息
	 * @author wuziyi
	 *
	 */
	private class NotificationSettingItem {
		private int itemId; // id用于判断重新排序后的item所对应的应用
		private Drawable icon;
		private String title;
		private boolean checkBoxStatus;
		private String packageName;
		private boolean isInstalled;
		private Intent intent;
		private boolean IsClickable = false;

		public Intent getIntent() {
			return intent;
		}

		public void setIntent(Intent intent) {
			this.intent = intent;
		}

		public int getItemId() {
			return itemId;
		}

		public void setItemId(int itemId) {
			this.itemId = itemId;
		}

		public Drawable getIcon() {
			return icon;
		}

		public void setIcon(Drawable icon) {
			this.icon = icon;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public boolean getCheckBoxStatus() {
			return checkBoxStatus;
		}

		public void setCheckBoxStatus(boolean checkBoxStatus) {
			this.checkBoxStatus = checkBoxStatus;
		}

		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		public void setInstalled(boolean isInstalled) {
			this.isInstalled = isInstalled;
		}
		
		public Boolean getClickable() {
			return IsClickable;
		}
		
		public void setClickable(boolean isClickable) {
			this.IsClickable = isClickable;
		}
		
	}

	private void initMoreAppList(ArrayList<String> intentList, ArrayList<String> titleList) {
		if (mMoreAppItemList != null) {
			mMoreAppItemList.clear();
		} else {
			mMoreAppItemList = new ArrayList<NotificationSettingActivity.NotificationSettingItem>();
		}
		for (int i = 0; i < intentList.size(); i++) {
			NotificationSettingItem item = new NotificationSettingItem();
			Intent intent = null;
			try {
				intent = Intent.parseUri(intentList.get(i), 0);
				item.setIntent(intent);
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
			item.setPackageName(item.getIntent().getComponent().getPackageName());
			boolean isSelected = mNotificationControler.isMoreApp(intent);
			item.setCheckBoxStatus(isSelected);
			item.setInstalled(true);
			item.setTitle(titleList.get(i));
			item.setItemId(i + MORE_APP_ID);
			try {
				item.setIcon(getPackageManager().getApplicationIcon(
						item.getPackageName().toString()));
			} catch (NameNotFoundException e) {
				item.setIcon(null);
				e.printStackTrace();
			}
			// 更多应用在未开启辅助服务时也显示，故在初始化list时就设置是否可以点击（置灰与否）
			if (mIsStartAccessibility) {
				item.setClickable(false);
			} else {
				item.setClickable(true);
			}
			mMoreAppItemList.add(item);
		}
	}

	// 检查辅助功能是否被开启
	private void checkAccessibilityState() {
		try {
			mIsStartAccessibility = mNotificationControler.isAccessibilityStart();
//			mIsFirstStart = mNotificationControler.isAccessibilityFirstRun();
		} catch (Exception e) {
			mIsStartAccessibility = false;
//			mIsFirstStart = true;
		}
	}
	/**
	 * <br>功能简述:将facebook移动到更多应用的提示
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void checkNeedShowTip() {
		// 此处添加将facebook移动到更多应用中的提示，仅在第一次升级时，且手机中有按照facebook的时候提示
		PreferencesManager sp = new PreferencesManager(mContext,
				NOTIFICATION_TIP, Context.MODE_PRIVATE);
		boolean needShowTip = sp.getBoolean("needShowTip", true);
		if (needShowTip) {
			if (null != GoLauncher.getContext()
					&& GoLauncher.getContext().getFirstRun()) {
				return;
			}
			if (!AppUtils.isAppExist(mContext, PackageName.FACEBOOK)) {
				return;
			}
			DeskToast.makeText(mContext,
					R.string.notification_setting_show_facebook_move,
					Toast.LENGTH_SHORT).show();
		}
		sp.putBoolean("needShowTip", false);
		sp.commit();
	}
	
	private void commitResult(ArrayList<String> addItemList, ArrayList<Intent> addIntentList) {
		// 按下确认时，把当前显示的，勾选的提交到数据库
		if (addIntentList != null) {
			try {
				mNotificationControler.addNotificationAppItems(addIntentList);
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}
		}
		if (addItemList != null) {
			if (mIsStartAccessibility) {
				Intent intent = new Intent(
						ICustomAction.ACTION_NOTIFICATIONACTION_REQUEST_APPLICATION);
				intent.putStringArrayListExtra("packagenames", addItemList);
				intent.putExtra("launcher", getPackageName());
				sendBroadcast(intent);
			}
		}
		ShortCutSettingInfo shortCutSettingInfo = GOLauncherApp.getSettingControler()
				.getShortCutSettingInfo();
		GOLauncherApp.getSettingControler().updateShortCutSetting_NonIndepenceTheme(
				shortCutSettingInfo);
		finish();
	}

	@Override
	protected void onStart() {
		checkAccessibilityState();
		checkNeedShowTip();
		initList();
		// 弹出提示对话框
//		if (!mIsFirstStart && !mIsStartAccessibility) {
//			new AlertDialog.Builder(this).setTitle(R.string.notification_setting_hint_title)
//					.setMessage(R.string.notification_setting_hint_content)
//					.setIcon(android.R.drawable.ic_dialog_alert)
//					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							Intent intent = new Intent(
//									ICustomAction.ACTION_NOTIFICATIONACTION_RESET_SHOW_DIALOG);
//							sendBroadcast(intent);
//							Intent accessbilityIntent = new Intent(
//									Settings.ACTION_ACCESSIBILITY_SETTINGS);
//							startActivity(accessbilityIntent);
//						}
//					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							Intent intent = new Intent(
//									ICustomAction.ACTION_NOTIFICATIONACTION_RESET_SHOW_DIALOG);
//							sendBroadcast(intent);
//						}
//					}).show();
//		}
		super.onStart();
	}

	@Override
	public int getId() {
		return IDiyFrameIds.NOTIFICATION_FRAME;
	}

	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		switch (msgId) {
			case IDiyMsgIds.SINA_WEIBO_LOGIN_FAIL :
				for (NotificationSettingItem item : mListItems) {
					if (item.getItemId() == NotificationType.NOTIFICATIONTYPE_SinaWeibo
							&& item.isInstalled) {
						item.checkBoxStatus = ShortCutSettingInfo.mAutoMissSinaWeiboStatistic;
						mAdapter.notifyDataSetChanged();
					}
				}
				break;

			case IDiyMsgIds.FACEBOOK_LOGIN_FAIL :
				for (NotificationSettingItem item : mListItems) {
					if (item.getItemId() == NotificationType.NOTIFICATIONTYPE_FACEBOOK
							&& item.isInstalled) {
						item.checkBoxStatus = ShortCutSettingInfo.mAutoMissfacebookStatistic;
						mAdapter.notifyDataSetChanged();
					}
				}
				break;

			default :
				break;
		}
		return false;
	}

	public final class ItemViewHolder {
		ImageView appIcon;
		TextView appTitle;
		TextView appSummry;
		CheckBox checkStatus;
		ImageView numImg;
	}

	public final class SubTitleHolder {
		TextView subTitle;
	}

	/**
	 * <br>功能简述:生成通讯统计计数标识的drawable对象
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private BitmapDrawable getNotificationNumPic() {
		BitmapDrawable NotificationNumPic = null;
		int FontSize;
		Paint countPaint = new Paint();
		try {
			String UnreadCount = "1";
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.stat_notify);
			int height = bitmap.getHeight();
			int width = bitmap.getWidth();
			Bitmap newBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas canvas = new Canvas(newBmp);
			canvas.drawBitmap(bitmap, 0, 0, countPaint);
			FontSize = mContext.getApplicationContext().getResources()
					.getDimensionPixelSize(R.dimen.dock_notify_font_size);
			countPaint.setTextSize(FontSize);
			countPaint.setColor(android.graphics.Color.WHITE);
			countPaint.setTextAlign(Paint.Align.CENTER);
			canvas.drawText(UnreadCount, width/2, height*2/3, countPaint);
			canvas.save(Canvas.ALL_SAVE_FLAG);

			// 存储
			canvas.restore();
			NotificationNumPic = new BitmapDrawable(newBmp);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Throwable e) {

		}
		return NotificationNumPic;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			// do nothing
		}
	}
}

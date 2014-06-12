package com.jiubang.ggheart.apps.desks.Preferences;

import java.util.ArrayList;
import java.util.Locale;

import org.acra.ErrorReporter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingQaGoLauncherView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingQaGoLockerView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingQaGoWidgetView;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.SensePreviewFrame;
import com.jiubang.ggheart.apps.desks.imagepreview.ChangeThemeMenu;
import com.jiubang.ggheart.components.DeskActivity;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.data.statistics.StaticTutorial;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:桌面设置-关于Go桌面EX-使用帮助Activity
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-12]
 */
public class DeskSettingQaTutorialActivity extends DeskActivity implements OnClickListener {
	public static final String URL_BASE = "http://smsftp.3g.cn/soft/3GHeart/golauncher/QAHtml/";
	private ChangeThemeMenu mChangeContentMenu = null; // 更换内容菜单
	private LinearLayout mChangeContentBtn; //切换内容按钮
	private TextView mContentName; //切换内容后显示的标题内容
	private LinearLayout mContentLayout = null; //存放内容的layout

	private ImageView mFeedbackBtn; //选额种类反馈按钮
	private ImageView mOperationTipsBtn; //操作提示按钮

	private DeskSettingQaGoLauncherView mGoLauncherView; //Go桌面
	private DeskSettingQaGoWidgetView mWidgetView; //Go widiget
	private DeskSettingQaGoLockerView mLockerView; //Go locker

	private int mCurrentPositon = -1; //当前选择显示的内容

//	private DialogSingleChoice mFeedBackAlertDialog; //意见fannk
//	private DialogSingleChoice mOperateAlertDialog; //操作提示对话框

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qahelptutorial);
		DrawUtils.resetDensity(this);
		initView();
		initChangeContentMenu();
		changeContent(0); //默认选择第一个
	}

	/**
	 * <br>功能简述:初始化控件
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initView() {
		mChangeContentBtn = (LinearLayout) findViewById(R.id.changeContentBtn);
		mChangeContentBtn.setOnClickListener(this);

		mContentName = (TextView) findViewById(R.id.contentName);
		mContentName.setText(R.string.setting_help_tab_title_desk);

		mContentLayout = (LinearLayout) findViewById(R.id.contentLayout);

		mFeedbackBtn = (ImageView) findViewById(R.id.helpfeedback);
		mFeedbackBtn.setOnClickListener(this);

		mOperationTipsBtn = (ImageView) findViewById(R.id.operationtips);
		mOperationTipsBtn.setOnClickListener(this);
	}

	/**
	 * <br>功能简述:设置菜单显示内容
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void initChangeContentMenu() {
		ArrayList<String> datas = new ArrayList<String>();
		datas.add(getString(R.string.setting_help_tab_title_desk)); //Go桌面
		datas.add(getString(R.string.setting_help_tab_title_widget)); //Go小部件
		datas.add(getString(R.string.setting_help_tab_title_golock)); //Go锁屏
		mChangeContentMenu = new ChangeThemeMenu(this, datas);
		mChangeContentMenu.setmItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		//mChangeContentMenu选择的内容
		if (v instanceof TextView && v.getTag() != null && v.getTag() instanceof Integer) {
			int position = (Integer) v.getTag();
			changeContent(position);
		}

		//点击更换内容
		else if (v == mChangeContentBtn) {
			if (mChangeContentMenu != null) {
				mChangeContentMenu.show(mChangeContentBtn);
			}
		}

		//选额种类反馈
		else if (v == mFeedbackBtn) {
			startFeedbackIntent(this);
		}

		//操作提示
		else if (v == mOperationTipsBtn) {
			showOperateTipsDialog(DeskSettingQaTutorialActivity.this);
		}
	}

	/**
	 * <br>功能简述:更换显示内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param position
	 */
	public void changeContent(int position) {
		if (mChangeContentMenu != null) {
			mChangeContentMenu.dismiss();
			mContentName.setText(mChangeContentMenu.getmStrings().get(position)); // 设置标题名称
		}

		//判断是否和当前选择一样
		if (mCurrentPositon == position) {
			return;
		}
		mCurrentPositon = position;
		mContentLayout.removeAllViews();
		recycle();
		switch (position) {
			case 0 :
				mGoLauncherView = new DeskSettingQaGoLauncherView(this);
				mContentLayout.addView(mGoLauncherView);
				break;

			case 1 :
				mWidgetView = new DeskSettingQaGoWidgetView(this);
				mContentLayout.addView(mWidgetView);
				break;

			case 2 :
				mLockerView = new DeskSettingQaGoLockerView(this);
				mContentLayout.addView(mLockerView);
				break;

			default :
				break;
		}
	}

	/**
	 * <br>功能简述:打开操作提示对话框
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 */
	public void showOperateTipsDialog(final Context context) {
		DialogSingleChoice mOperateAlertDialog = new DialogSingleChoice(context);
		mOperateAlertDialog.show();
		mOperateAlertDialog.setTitle(R.string.setting_help_operation_tips);
		final CharSequence[] items = context.getResources().getTextArray(
				R.array.qatutorial_list_items);
		mOperateAlertDialog.setItemData(items, -1, false);
		mOperateAlertDialog.setOnItemClickListener(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				goToOperateTips(item);
			}
		});
	}

	/**
	 * <br>功能简述:打开提示界面
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param item
	 */
	public void goToOperateTips(int item) {
		PreferencesManager sharedPreferences = new PreferencesManager(this,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		switch (item) {
			case 0 : {
				GoLauncher.setTutorialMask(LauncherEnv.MASK_TUTORIAL_CUSTOM_GESTURE);
				break;
			}
			case 1 : {
				boolean needStartutorial = sharedPreferences.getBoolean(
						IPreferencesIds.SHOULD_SHOW_PRIVIEW_GUIDE, true);
				if (!needStartutorial) {
					StaticTutorial.sCheckShowScreenEdit = true;
					sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_PRIVIEW_GUIDE, true);
					sharedPreferences.commit();
				}
				SensePreviewFrame.setIsEnterFromQA(true);
				GoLauncher.setTutorialMask(LauncherEnv.MASK_TUTORIAL_PREVIEW);
				break;
			}
			case 2 : {
				GoLauncher.setTutorialMask(LauncherEnv.MASK_TUTORIAL_DOCK_BAR_ICON);
				break;
			}
		}
		Intent intent = new Intent(DeskSettingQaTutorialActivity.this, GoLauncher.class);
		startActivity(intent);
		finish();
	}

	/**
	 * 获取当前语言和版本号
	 * @return
	 */
	public static String getCurLanguage(Context context) {
		Locale locale = null;
		if (DeskResourcesConfiguration.createInstance(context) != null) {
			locale = DeskResourcesConfiguration.getInstance().getmLocale();
		}
		//如果获取桌面的local为空。则获取系统的local
		if (locale == null) {
			locale = Locale.getDefault();
		}
		return String.format("%s-%s", locale.getLanguage(), locale.getCountry());
	}

	/**
	 * <br>功能简述:设置不同语言对应的页面内容
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public static String[] getPageList(int[] pageList, int defaultPage, Context context) {
		Resources resources = context.getResources();
		String[] languagesList = resources.getStringArray(R.array.help_support_languages);
		if (pageList.length != languagesList.length) {
			return null;
		}
		String curLanguage = DeskSettingQaTutorialActivity.getCurLanguage(context);
		for (int i = 0; i < languagesList.length; i++) {
			if (languagesList[i].equals(curLanguage)) {
				return resources.getStringArray(pageList[i]);
			}
		}
		return resources.getStringArray(pageList[defaultPage]);
	}

	/**
	 * <br>功能简述:退出时回收资源
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void recycle() {
		if (mGoLauncherView != null) {
			mGoLauncherView.recycle();
			mGoLauncherView = null;
		}

		if (mWidgetView != null) {
			mWidgetView.recycle();
			mWidgetView = null;
		}

		if (mLockerView != null) {
			mLockerView.recycle();
			mLockerView = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recycle();
		DeskSettingConstants.selfDestruct(getWindow().getDecorView());
		
		if (mContentLayout != null) {
			mContentLayout.removeAllViews();
			mContentLayout = null;
		}
		mChangeContentMenu = null;
	}

	/**
	 * <br>功能简述:开打反馈对话框
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 */
	public static DialogSingleChoice startFeedbackIntent(final Context context) {
		final DialogSingleChoice alertDialog = new DialogSingleChoice(context);
		alertDialog.show();
		alertDialog.setTitle(R.string.feedback_select_type_title);

		String bugString = context.getResources().getString(R.string.feedback_select_type_bug);
		String suggestionString = context.getResources().getString(
				R.string.feedback_select_type_suggestion);
		String questionString = context.getResources().getString(
				R.string.feedback_select_type_question);
		final CharSequence[] items = { bugString, suggestionString, questionString };
		alertDialog.setItemData(items, -1, false);
		alertDialog.setOnItemClickListener(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				String[] receiver = new String[] { "golauncher@goforandroid.com" };
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receiver);

				String bugForMailString = context.getResources().getString(
						R.string.feedback_select_type_bug_for_mail);
				String suggestionForMailString = context.getResources().getString(
						R.string.feedback_select_type_suggestion_for_mail);
				String questionForMailString = context.getResources().getString(
						R.string.feedback_select_type_question_for_mail);
				final CharSequence[] itemsForMail = { bugForMailString, suggestionForMailString,
						questionForMailString };
				String subject = "GO Launcher EX(v" + context.getString(R.string.curVersion)
						+ ") Feedback(" + itemsForMail[item] + ")";
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
				String content = context.getString(R.string.rate_go_launcher_mail_content) + "\n\n";
				StringBuffer body = new StringBuffer(content);
				body.append("\nProduct=" + android.os.Build.PRODUCT);
				body.append("\nPhoneModel=" + android.os.Build.MODEL);
				body.append("\nROM=" + android.os.Build.DISPLAY);
				body.append("\nBoard=" + android.os.Build.BOARD);
				body.append("\nDevice=" + android.os.Build.DEVICE);
				body.append("\nDensity="
						+ String.valueOf(context.getResources().getDisplayMetrics().density));
				body.append("\nPackageName=" + context.getPackageName());
				body.append("\nAndroidVersion=" + android.os.Build.VERSION.RELEASE);
				body.append("\nTotalMemSize="
						+ (ErrorReporter.getTotalInternalMemorySize() / 1024 / 1024) + "MB");
				body.append("\nFreeMemSize="
						+ (ErrorReporter.getAvailableInternalMemorySize() / 1024 / 1024) + "MB");
				body.append("\nRom App Heap Size="
						+ Integer.toString((int) (Runtime.getRuntime().maxMemory() / 1024L / 1024L))
						+ "MB");
				emailIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
				emailIntent.setType("plain/text");
				try {
					context.startActivity(emailIntent);
				} catch (Exception e) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("http://golauncher.goforandroid.com"));
					intent.setClassName("com.android.browser",
							"com.android.browser.BrowserActivity");
					try {
						context.startActivity(intent);
					} catch (Exception e2) {
						Toast.makeText(context, R.string.feedback_no_browser_tip, Toast.LENGTH_SHORT).show();
						Log.i("GoLauncher", "startActivityForResult have exception = " + e.getMessage());

					}
				}
			}
		});
		return alertDialog;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		DrawUtils.resetDensity(this);
	}
}

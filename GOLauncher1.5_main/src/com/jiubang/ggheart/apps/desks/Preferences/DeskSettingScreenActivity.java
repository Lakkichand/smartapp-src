package com.jiubang.ggheart.apps.desks.Preferences;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSeekBarItemInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSingleInfo;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemCheckBoxView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemListView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingTitleView;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DockFrame;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;
import com.jiubang.ggheart.components.advert.AdvertControl;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>类描述:界面设置Activity
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-10]
 */
public class DeskSettingScreenActivity extends DeskSettingBaseActivity
		implements
			ScreenScrollerListener {
	private DesktopSettingInfo mDesktopSettingInfo;
	private FunAppSetting mFunAppSettingInfo;
	private ShortCutSettingInfo mShortCutSettingInfo;
	public static final String IS_ONLY_SHOW_AFF_FUN = "is_only_show_app_fun";
	public final static int APP_FUN_ROWS_COLS_CUSTOM = 5; //功能表行列数自定义选项
	private boolean mOnlyShowAppFun = false;
	private RelativeLayout mLayoutFunction;
	private RelativeLayout mLayoutDesk;
	private LinearLayout mButtonLineFunction;
	private LinearLayout mButtonLineDesk;
	private TextView mTextFunction;
	private TextView mTextDesk;
	private int mSelectColor;
	private int mNoSelectColor;
	private final static int SCREEN_ROWS_COLS_CUSTOM = 4; //屏幕行列数自定义选项
	/**
	 * 显示桌面状态栏
	 */
	private DeskSettingItemCheckBoxView mSettingShowDeskStatusbar;

	/**
	 * 显示程序名称
	 */
	private DeskSettingItemCheckBoxView mSettingShowAppName;

	/**
	 * 显示名称背景
	 */
	private DeskSettingItemCheckBoxView mSettingShowAppNameAndBg;

	/**
	 * 显示Dock条
	 */
	private DeskSettingItemCheckBoxView mSettingShowDock;

	//功能表设置

	/**
	 * 显示功能表选项卡
	 */
	private DeskSettingItemCheckBoxView mSettingShowFunAppTab;

	/**
	 * 显示功能表程序名称
	 */
	private DeskSettingItemCheckBoxView mSettingShowFunAppName;

	/**
	 * 显示应用更新提示
	 */
	private DeskSettingItemCheckBoxView mSettingShowFunAppUpdateTips;

	/**
	 * 显示底部操作栏
	 */
	private DeskSettingItemCheckBoxView mSettingShowFunAppActionBar;

	/**
	 * 显示主页按钮
	 */
	private DeskSettingItemCheckBoxView mSettingShowFunAppHomeKey;
	/**
	 * 屏幕行列数
	 */
	private DeskSettingItemListView mSettingScreenRowsCols;
	/**
	 * 快捷条行数
	 */
	private DeskSettingItemListView mSettingDockRows;
	/**
	 * 功能表行列数
	 */
	private DeskSettingItemListView mSettingFunAppRowsCols;

	private ScrollerViewGroup mSettingScroller;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.desk_setting_screen);

		mOnlyShowAppFun = getIntent().getBooleanExtra(IS_ONLY_SHOW_AFF_FUN, false);

		GoSettingControler goSettingControler = GOLauncherApp.getSettingControler();
		mDesktopSettingInfo = goSettingControler.createDesktopSettingInfo();
		mFunAppSettingInfo = goSettingControler.getFunAppSetting();
		mShortCutSettingInfo = goSettingControler.getShortCutSettingInfo();

		mSettingScroller = (ScrollerViewGroup) findViewById(R.id.desk_setting_scroller_view_group);
		mSettingScroller.setScreenScrollerListener(this);
		mSettingScroller.setScreenCount(2);
		initTitle();
		// 桌面设置	
		if (!mOnlyShowAppFun) {
			mSettingShowDeskStatusbar = (DeskSettingItemCheckBoxView) findViewById(R.id.desk_statusbar);
			mSettingShowDeskStatusbar.setOnValueChangeListener(this);

			mSettingShowAppName = (DeskSettingItemCheckBoxView) findViewById(R.id.show_app_name);
			mSettingShowAppName.setOnValueChangeListener(this);

			mSettingShowAppNameAndBg = (DeskSettingItemCheckBoxView) findViewById(R.id.show_app_name_bg);
			mSettingShowAppNameAndBg.setOnValueChangeListener(this);

			mSettingShowDock = (DeskSettingItemCheckBoxView) findViewById(R.id.show_dock);
			mSettingShowDock.setOnValueChangeListener(this);

			mSettingScreenRowsCols = (DeskSettingItemListView) findViewById(R.id.screen_rows_cols);
			mSettingScreenRowsCols.setOnValueChangeListener(this);

			//桌面-快捷条设置
			mSettingDockRows = (DeskSettingItemListView) findViewById(R.id.dock_rows);
			mSettingDockRows.setOnValueChangeListener(this);
		} else {
			//如果只显示功能表。隐藏桌面设置和功能表标题
			LinearLayout deskLayout = (LinearLayout) findViewById(R.id.desk_setting_layout);
			deskLayout.setVisibility(View.GONE);

			DeskSettingTitleView appFunTitle = (DeskSettingTitleView) findViewById(R.id.appfun_setting_title);
			appFunTitle.setVisibility(View.GONE);
			
			LinearLayout titleLayout = (LinearLayout) findViewById(R.id.desk_setting_tab_title_layout);
			titleLayout.setVisibility(View.GONE);
			mSettingScroller.getScreenScroller().setCurrentScreen(1);
			mSettingScroller.setScreenCount(1);
		}
		//功能表设置
		mSettingShowFunAppTab = (DeskSettingItemCheckBoxView) findViewById(R.id.show_fun_app_tab);
		mSettingShowFunAppTab.setOnValueChangeListener(this);

		mSettingShowFunAppName = (DeskSettingItemCheckBoxView) findViewById(R.id.fun_app_show_app_name);
		mSettingShowFunAppName.setOnValueChangeListener(this);

		mSettingShowFunAppUpdateTips = (DeskSettingItemCheckBoxView) findViewById(R.id.func_app_update_tips);
		mSettingShowFunAppUpdateTips.setOnValueChangeListener(this);

		mSettingShowFunAppActionBar = (DeskSettingItemCheckBoxView) findViewById(R.id.func_app_action_bar);
		mSettingShowFunAppActionBar.setOnValueChangeListener(this);

		mSettingShowFunAppHomeKey = (DeskSettingItemCheckBoxView) findViewById(R.id.fun_app_home_key);
		mSettingShowFunAppHomeKey.setOnValueChangeListener(this);

		//功能表行列数
		mSettingFunAppRowsCols = (DeskSettingItemListView) findViewById(R.id.func_app_rows_cols);
		mSettingFunAppRowsCols.setOnValueChangeListener(this);

		load();
	}

	@Override
	public void load() {
		super.load();
		if (!mOnlyShowAppFun) {
			loadDesk();
		}
		loadFunApp();
	}
	/**
	 * <br>功能简述:初始化2个标题
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void initTitle() {
		mLayoutFunction = (RelativeLayout) findViewById(R.id.layout_function);
		mLayoutFunction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mSettingScroller.gotoViewByIndex(1);

			}
		});
		mLayoutDesk = (RelativeLayout) findViewById(R.id.layout_desk);
		mLayoutDesk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mSettingScroller.gotoViewByIndex(0);

			}
		});

		mTextFunction = (TextView) findViewById(R.id.text_function);
		mTextDesk = (TextView) findViewById(R.id.text_desk);

		mButtonLineFunction = (LinearLayout) findViewById(R.id.button_line_function);
		mButtonLineDesk = (LinearLayout) findViewById(R.id.button_line_desk);

		mSelectColor = getResources().getColor(R.color.desk_setting_tab_title_select);
		mNoSelectColor = getResources().getColor(R.color.desk_setting_tab_title_no_select);
	}
	/**
	 * <br>功能简述:加载桌面设置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void loadDesk() {
		if (mDesktopSettingInfo != null) {
			//设置桌面状态栏
			mSettingShowDeskStatusbar.setIsCheck(mDesktopSettingInfo.mShowStatusbar);

			//设置是否显示程序名称和背景 
			setSettingShowAppName(mDesktopSettingInfo.mTitleStyle);

			setSettingScreenRowsColsInfo();

			loadDock();
		}
		mSettingShowDock.setIsCheck(ShortCutSettingInfo.sEnable); //设置Dock条是否显示
	}

	/**
	 * <br>功能简述:加载功能表设置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void loadFunApp() {
		if (mFunAppSettingInfo != null) {
			mSettingShowFunAppTab
					.setIsCheck(mFunAppSettingInfo.getShowTabRow() == FunAppSetting.ON);

			mSettingShowFunAppName
					.setIsCheck(mFunAppSettingInfo.getAppNameVisiable() == FunAppSetting.ON);

			mSettingShowFunAppUpdateTips
					.setIsCheck(mFunAppSettingInfo.getAppUpdate() == FunAppSetting.ON);

			mSettingShowFunAppActionBar
					.setIsCheck(mFunAppSettingInfo.getShowActionBar() == FunAppSetting.ON);

			mSettingShowFunAppHomeKey
					.setIsCheck(mFunAppSettingInfo.getShowHomeKeyOnly() == FunAppSetting.ON);

			setFunAppRowsColsInfo(); //功能表行列数

			//判断显示底部操作栏是否开启
			if (mFunAppSettingInfo.getShowActionBar() != FunAppSetting.ON) {
				mSettingShowFunAppHomeKey.setEnabled(false);
				mSettingShowFunAppHomeKey.setIsCheck(false);
			}
		}
	}

	/**
	 * <br>功能简述:	设置是否显示程序名称和背景
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param titleStyle
	 */
	public void setSettingShowAppName(int titleStyle) {

		//显示程序名称背景
		if (titleStyle == 0) {
			mSettingShowAppName.setIsCheck(true);
			mSettingShowAppNameAndBg.setIsCheck(true);
		}

		//显示程序名称
		else if (titleStyle == 1) {
			mSettingShowAppName.setIsCheck(true);
			mSettingShowAppNameAndBg.setIsCheck(false);
		}
		//没有显示程序名称
		else if (titleStyle == 2) {
			mSettingShowAppName.setIsCheck(false);
			mSettingShowAppNameAndBg.setIsCheck(false);
			mSettingShowAppNameAndBg.setEnabled(false);
			mSettingShowAppNameAndBg.getCheckBox().setEnabled(false);
			mSettingShowAppNameAndBg.setTitleColor(R.color.desk_setting_item_summary_color);
		}
	}

	/**
	 * <br>功能简述:获取是否显示程序名称的设置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public int getSettingShowAppNameTitleStyle() {
		int titleStyle;
		if (mSettingShowAppName.getIsCheck()) {
			if (mSettingShowAppNameAndBg.getIsCheck()) {
				titleStyle = 0;
			} else {
				titleStyle = 1;
			}
		} else {
			titleStyle = 2;
		}
		return titleStyle;
	}

	public void save() {
		super.save();
		if (!mOnlyShowAppFun) {
			saveDeskSetting(); //保存桌面设置
		}
		saveFunAppSetting(); //保存功能表设置
	}

	/**
	 * <br>功能简述:保存桌面设置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void saveDeskSetting() {
		if (mDesktopSettingInfo != null) {
			boolean isChangeDesk = false;

			//桌面显示状态栏
			if (mDesktopSettingInfo.mShowStatusbar != mSettingShowDeskStatusbar.getIsCheck()) {
				mDesktopSettingInfo.mShowStatusbar = mSettingShowDeskStatusbar.getIsCheck();
				isChangeDesk = true;
				GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
						IDiyMsgIds.SHOW_STATUS_BAR_SHOW_CHANGE, -1,
						mDesktopSettingInfo.mShowStatusbar, null);
			}

			//是否显示程序名称和背景
			int titleStyle = getSettingShowAppNameTitleStyle();
			if (mDesktopSettingInfo.mTitleStyle != titleStyle) {
				mDesktopSettingInfo.mTitleStyle = titleStyle;
				isChangeDesk = true;
			}
			saveScreenRowsCols();
			saveDock();
			if (isChangeDesk) {
				//更改数据库数据
				GOLauncherApp.getSettingControler().updateDesktopSettingInfo(mDesktopSettingInfo);
			}
		}

		// 显示Dock条：判断当前状态是否等于数据库状态，然后在做完动画后才修改数据库
		if (ShortCutSettingInfo.sEnable != mSettingShowDock.getIsCheck()) {
			if (ShortCutSettingInfo.sEnable) { // 在DOCK条那里更改数据库
				// 参数1：做动画
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_HIDE,
						DockFrame.HIDE_ANIMATION_NO, null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.DOCK_HIDE,
						DockFrame.HIDE_ANIMATION_NO, null, null);
			} else {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_SHOW,
						DockFrame.HIDE_ANIMATION, null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.DOCK_SHOW,
						DockFrame.HIDE_ANIMATION, null, null);
			}
		}
	}

	/**
	 * <br>功能简述:保存功能表设置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void saveFunAppSetting() {
		if (mFunAppSettingInfo != null) {
			int value;
			int isCheck;

			value = mFunAppSettingInfo.getShowTabRow();
			isCheck = DeskSettingConstants.boolean2Int(mSettingShowFunAppTab.getIsCheck());
			if (value != isCheck) {
				mFunAppSettingInfo.setShowTabRow(isCheck); //直接更改数据,保护数据库
			}

			value = mFunAppSettingInfo.getAppNameVisiable();
			isCheck = DeskSettingConstants.boolean2Int(mSettingShowFunAppName.getIsCheck());
			if (value != isCheck) {
				mFunAppSettingInfo.setAppNameVisiable(isCheck);
			}

			value = mFunAppSettingInfo.getAppUpdate();
			isCheck = DeskSettingConstants.boolean2Int(mSettingShowFunAppUpdateTips.getIsCheck());
			if (value != isCheck) {
				mFunAppSettingInfo.setAppUpdate(isCheck);
			}

			value = mFunAppSettingInfo.getShowActionBar();
			isCheck = DeskSettingConstants.boolean2Int(mSettingShowFunAppActionBar.getIsCheck());
			if (value != isCheck) {
				mFunAppSettingInfo.setShowActionBar(isCheck);
			}

			value = mFunAppSettingInfo.getShowHomeKeyOnly();
			isCheck = DeskSettingConstants.boolean2Int(mSettingShowFunAppHomeKey.getIsCheck());
			if (value != isCheck) {
				mFunAppSettingInfo.setShowHomeKeyOnly(isCheck);
			}
		}
	}

	@Override
	public boolean onValueChange(DeskSettingItemBaseView baseView, Object value) {
		boolean bRet = true;
		//显示桌面状态栏
		if (baseView == mSettingShowDeskStatusbar) {
			save(); // 桌面状态栏，则需立刻保存，因为需要立即生效
		}

		else if (baseView == mSettingShowAppName) {
			if ((Boolean) value == true) {
				mSettingShowAppNameAndBg.setEnabled(true);
			} else {
				mSettingShowAppNameAndBg.setIsCheck(false);
				mSettingShowAppNameAndBg.setEnabled(false);
			}
		}

		//显示底部操作栏
		else if (baseView == mSettingShowFunAppActionBar) {
			if ((Boolean) value == true) {
				mSettingShowFunAppHomeKey.setEnabled(true);
			} else {
				if (getShowActionBarPre()) {
					showActionBarConfirmDialog();
				} else {
					mSettingShowFunAppHomeKey.setEnabled(false);
					mSettingShowFunAppHomeKey.setIsCheck(false);
				}
			}
		} else if (baseView == mSettingScreenRowsCols) {
			changeScreenRowsCols(baseView, value);
		} else if (baseView == mSettingDockRows) {
			changeDockRows(baseView);
		} else if (baseView == mSettingFunAppRowsCols) {
			saveFunAppRowsCols(baseView); //保存功能表行列数
		}

		return bRet;
	}

	/**
	 * <br>功能简述:显示是否提示关闭"底部操作栏"对话框
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void showActionBarConfirmDialog() {
		final DialogConfirm mActionBarConfirmDialog = new DialogConfirm(this);
		mActionBarConfirmDialog.show();
		mActionBarConfirmDialog.setTitle(R.string.setting_dialog_show_action_bar_title);
		mActionBarConfirmDialog.setMessage(R.string.setting_dialog_show_action_bar_message);
		mActionBarConfirmDialog.setTipCheckBoxText(R.string.setting_dialog_dont_show_again);
		mActionBarConfirmDialog.showTipCheckBox();
		mActionBarConfirmDialog.setPositiveButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mSettingShowFunAppHomeKey.setEnabled(false);
				mSettingShowFunAppHomeKey.setIsCheck(false);
				CheckBox dontShowAgain = mActionBarConfirmDialog.getTipCheckBox();
				if (dontShowAgain.isChecked()) {
					setShowActionBarPre();
				}
			}
		});

		mActionBarConfirmDialog.setNegativeButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mSettingShowFunAppActionBar.setIsCheck(true); //重新设置勾选状态
				mActionBarConfirmDialog.dismiss();
			}
		});

		mActionBarConfirmDialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				//返回按钮
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					mSettingShowFunAppActionBar.setIsCheck(true); //重新设置勾选状态
					dialog.dismiss();
					return true;
				}
				return false;
			}
		});
	}

	/**
	 * <br>功能简述:判断是否需要提示对话框
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean getShowActionBarPre() {
		SharedPreferences sharedPref = getSharedPreferences(
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		if (sharedPref.getBoolean(IPreferencesIds.SHOW_ALERT_DIALOG_FOR_ACTION_BAR_SETTING, true)) {
			return true;
		} else {
			return false;
		}
	}

	public void setShowActionBarPre() {
		SharedPreferences sharedPref = getSharedPreferences(
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		Editor edit = sharedPref.edit();
		edit.putBoolean(IPreferencesIds.SHOW_ALERT_DIALOG_FOR_ACTION_BAR_SETTING, false);
		edit.commit();
	}
	/**
	 * <br>功能简述:显示桌面设置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void showDeskTab() {
		mButtonLineFunction.setVisibility(View.GONE);
		mButtonLineDesk.setVisibility(View.VISIBLE);
		mTextFunction.setTextColor(mNoSelectColor);
		mTextDesk.setTextColor(mSelectColor);

	}
	/**
	 * <br>功能简述:显示功能表设置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void showFunAppTab() {
		mButtonLineFunction.setVisibility(View.VISIBLE);
		mButtonLineDesk.setVisibility(View.GONE);
		mTextFunction.setTextColor(mSelectColor);
		mTextDesk.setTextColor(mNoSelectColor);
	}

	/**
	 * <br>功能简述:保存屏幕行列数
	 * <br>功能详细描述:
	 * <br>注意:需要放到退出时才保存。因为执行保存会修改桌面东西。导致很慢
	 * @param view
	 * @param value
	 */
	public void saveScreenRowsCols() {
		if (mDesktopSettingInfo != null) {
			boolean changeRowsCols = false;

			//单选确认框
			int curValue = 0;
			if (mSettingScreenRowsCols != null) {
				curValue = Integer
						.parseInt(String.valueOf(mSettingScreenRowsCols.getSelectValue()));
			}

			//自定义
			if (curValue == SCREEN_ROWS_COLS_CUSTOM) {
				ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList = DeskSettingConstants
						.getSecondSeekBarItemInfo(mSettingScreenRowsCols);
				if (mSettingScreenRowsCols != null) {
					//获取选择的seekbar值
					int rowsValue = seekBarItemInfoList.get(0).getSelectValue();
					int colsValue = seekBarItemInfoList.get(1).getSelectValue();
					if (mDesktopSettingInfo.mRow != rowsValue
							|| mDesktopSettingInfo.mColumn != colsValue) {
						mDesktopSettingInfo.mRow = rowsValue;
						mDesktopSettingInfo.mColumn = colsValue;
						mDesktopSettingInfo.mStyle = curValue;
						changeRowsCols = true;
					}
				}
			} else {
				int oldValue = mDesktopSettingInfo.mStyle;
				if (curValue != oldValue) {
					mDesktopSettingInfo.setRows(curValue);
					mDesktopSettingInfo.setColumns(curValue);
					mDesktopSettingInfo.mStyle = curValue;
					changeRowsCols = true;
				}
			}

			//判断是否修改自动调整小部件和图片位置
			boolean autofit = true;
			if (mSettingScreenRowsCols != null) {
				autofit = mSettingScreenRowsCols.getDeskSettingInfo().getSingleInfo()
						.getCheckBoxIsCheck();
			}
			if (mDesktopSettingInfo.mAutofit != autofit) {
				mDesktopSettingInfo.mAutofit = autofit;
				changeRowsCols = true;
			}

			if (changeRowsCols) {
				//屏幕数做过修改。15屏广告就不再请求了
				AdvertControl.getAdvertControlInstance(this).setCanRequestAdvertState(false);
				//24小时重新请求
				AdvertControl.getAdvertControlInstance(this).setCanRequestAgainState(false);
				GOLauncherApp.getSettingControler().updateDesktopSettingInfo(mDesktopSettingInfo); //保存数据
			}
		}
	}
	/**
	 * <br>功能简述:修改屏幕行列数
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 * @param value
	 */
	public void changeScreenRowsCols(DeskSettingItemBaseView view, Object value) {
		if (view == mSettingScreenRowsCols) {
			//判断是否从seekBarDialog那里返回，特殊处理
			if (value instanceof String[]) {
				updateSceenRowsCoslEntriesCustomTemp(); //更新临时显示内容，等刷新SingleDialogWithCheckBox内容时可以调用
				mSettingScreenRowsCols.updateSingleDialogWithCheckBox();
			} else {
				//单选确认框
				int curValue = Integer.parseInt(String.valueOf(mSettingScreenRowsCols
						.getSelectValue()));
				//判断是否自定义
				if (curValue == SCREEN_ROWS_COLS_CUSTOM) {
					updateSceenRowsCoslEntriesCustomTemp();
				} else {
					updateSceenRowsCoslEntries(curValue); //更新显示内容
				}
			}
			mSettingScreenRowsCols.updateSumarryText(); //更新Summary
		}
	}
	/**
	 * <br>功能简述:更新屏幕行列数的Entries-自定义—临时更新
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param value
	 */
	private void updateSceenRowsCoslEntriesCustomTemp() {
		ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList = DeskSettingConstants
				.getSecondSeekBarItemInfo(mSettingScreenRowsCols);
		if (mSettingScreenRowsCols != null) {
			//获取选择的seekbar值
			int rowsValue = seekBarItemInfoList.get(0).getSelectValue();
			int colsValue = seekBarItemInfoList.get(1).getSelectValue();

			CharSequence[] entries = mSettingScreenRowsCols.getDeskSettingInfo().getSingleInfo()
					.getEntries();
			int entriesSize = entries.length;
			entries[entriesSize - 1] = getString(R.string.screen_grid_diy) + " (" + rowsValue + "×"
					+ colsValue + ")";
		}

	}
	/**
	 * <br>功能简述:加载Dock条数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void loadDock() {
		if (mShortCutSettingInfo != null) {

			//快捷条行数
			String oldSelectValue = String.valueOf(mShortCutSettingInfo.mRows);
			DeskSettingSingleInfo singleInfo = mSettingDockRows.getDeskSettingInfo()
					.getSingleInfo();
			if (singleInfo != null) {
				singleInfo.setSelectValue(oldSelectValue);
				mSettingDockRows.updateSumarryText();
			}
		}
	}
	/**
	 * <br>功能简述:更新屏幕行列数的Entries
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param value
	 */
	private void updateSceenRowsCoslEntries(int type) {
		CharSequence[] entries = mSettingScreenRowsCols.getDeskSettingInfo().getSingleInfo()
				.getEntries();
		int entriesSize = entries.length;

		//设置自定义的显示值为：自定义行列（6X6）
		if (type == SCREEN_ROWS_COLS_CUSTOM) {
			entries[entriesSize - 1] = getString(R.string.screen_grid_diy) + " ("
					+ mDesktopSettingInfo.mRow + "×" + mDesktopSettingInfo.mColumn + ")";
		} else {
			entries[entriesSize - 1] = getString(R.string.screen_grid_diy);
		}
	}

	/**
	 * <br>功能简述:设置桌面行列数
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void setSettingScreenRowsColsInfo() {
		if (mDesktopSettingInfo != null) {

			//屏幕行列数
			//设置自动调整小部件和图标大小
			boolean autofit = mDesktopSettingInfo.mAutofit;
			mSettingScreenRowsCols.getDeskSettingInfo().getSingleInfo().setCheckBoxIsCheck(autofit);

			int screenRowsColsType = mDesktopSettingInfo.mStyle;
			updateSceenRowsCoslEntries(screenRowsColsType);
			DeskSettingConstants.setSingleInfoValueAndSummary(screenRowsColsType,
					mSettingScreenRowsCols);

			//行数seekBar
			DeskSettingSeekBarItemInfo rowSeekBarItemInfo = new DeskSettingSeekBarItemInfo();
			rowSeekBarItemInfo.setTitle(getString(R.string.screen_row_dialog_msg)); //设置标题
			rowSeekBarItemInfo.setMinValue(DeskSettingConstants.ROWS_COLS_MIN_SIZE); //设置最小值
			rowSeekBarItemInfo.setMaxValue(DeskSettingConstants.ROWS_COLS_MAX_SIZE); //设置最大值
			rowSeekBarItemInfo.setSelectValue(mDesktopSettingInfo.mRow); //设置默认选择值

			//列数seekBar
			DeskSettingSeekBarItemInfo colSeekBarItemInfo = new DeskSettingSeekBarItemInfo();
			colSeekBarItemInfo.setTitle(getString(R.string.screen_column_dialog_msg)); //设置标题
			colSeekBarItemInfo.setMinValue(DeskSettingConstants.ROWS_COLS_MIN_SIZE); //设置最小值
			colSeekBarItemInfo.setMaxValue(DeskSettingConstants.ROWS_COLS_MAX_SIZE); //设置最大值
			colSeekBarItemInfo.setSelectValue(mDesktopSettingInfo.mColumn);; //设置默认选择值

			//创建SeekBarInfo队列
			ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList = new ArrayList<DeskSettingSeekBarItemInfo>();
			seekBarItemInfoList.add(rowSeekBarItemInfo);
			seekBarItemInfoList.add(colSeekBarItemInfo);
			DeskSettingConstants.setSecondInfoOfSeekBar(this, seekBarItemInfoList,
					R.array.screen_rows_cols_title, mSettingScreenRowsCols);
		}

	}
	/**
	 * <br>功能简述:保存Dock条设置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void saveDock() {
		if (mShortCutSettingInfo != null) {
			//快捷条行数
			boolean isChangeDockRows = false;
			int rowValue = Integer.parseInt(String.valueOf(mSettingDockRows.getSelectValue()));
			if (mShortCutSettingInfo.mRows != rowValue) {
				mShortCutSettingInfo.mRows = rowValue;
				isChangeDockRows = true;
			}
			// 全局设置保存
			if (isChangeDockRows) {
				GOLauncherApp.getSettingControler().updateShortCutSetting_NonIndepenceTheme(
						mShortCutSettingInfo);
				if (isChangeDockRows) {
					GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
							IDiyMsgIds.DOCK_SETTING_CHANGED_ROW, -1, null, null);
					GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
							IDiyMsgIds.DOCK_SETTING_CHANGED_ROW, -1, null, null);
				}
			}
		}
	}
	/**
	 * <br>功能简述:修改Dock条数的显示
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 */
	public void changeDockRows(DeskSettingItemBaseView view) {
		if (view == mSettingDockRows) {
			mSettingDockRows.updateSumarryText();
		}
	}
	/**
	 * <br>功能简述:设置功能表行列数
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void setFunAppRowsColsInfo() {
		if (mFunAppSettingInfo == null) {
			return;
		}

		boolean isSmallScreen = DeskSettingConstants.checkIsSmallScreen(); //是否小屏幕

		//检查是否小屏幕手机，是的话就重新设置选项
		DeskSettingSingleInfo singleInfo = mSettingFunAppRowsCols.getDeskSettingInfo()
				.getSingleInfo();
		if (isSmallScreen) {
			CharSequence[] entries = getResources().getTextArray(R.array.qvga_fun_app_rows_cols);
			CharSequence[] entryValues = getResources().getTextArray(
					R.array.qvga_fun_app_rows_cols_value);
			singleInfo.setEntries(entries);
			singleInfo.setEntryValues(entryValues);
		}

		int funAppRowsColsType = mFunAppSettingInfo.getLineColumnNum();
		updateFunAppRowsCoslEntries(funAppRowsColsType);
		DeskSettingConstants.setSingleInfoValueAndSummary(funAppRowsColsType,
				mSettingFunAppRowsCols);

		//行数seekBar
		DeskSettingSeekBarItemInfo rowSeekBarItemInfo = new DeskSettingSeekBarItemInfo();
		rowSeekBarItemInfo.setTitle(getString(R.string.screen_row_dialog_msg)); //设置标题
		rowSeekBarItemInfo.setMinValue(DeskSettingConstants.ROWS_COLS_MIN_SIZE); //设置最小值
		rowSeekBarItemInfo.setMaxValue(DeskSettingConstants.ROWS_COLS_MAX_SIZE); //设置最大值
		rowSeekBarItemInfo.setSelectValue(mFunAppSettingInfo.getRowNum()); //设置默认选择值,默认值为4

		//列数seekBar
		DeskSettingSeekBarItemInfo colSeekBarItemInfo = new DeskSettingSeekBarItemInfo();
		colSeekBarItemInfo.setTitle(getString(R.string.screen_column_dialog_msg)); //设置标题
		colSeekBarItemInfo.setMinValue(DeskSettingConstants.ROWS_COLS_MIN_SIZE); //设置最小值
		colSeekBarItemInfo.setMaxValue(DeskSettingConstants.ROWS_COLS_MAX_SIZE); //设置最大值
		colSeekBarItemInfo.setSelectValue(mFunAppSettingInfo.getColNum());; //设置默认选择值

		//创建SeekBarInfo队列
		ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList = new ArrayList<DeskSettingSeekBarItemInfo>();
		seekBarItemInfoList.add(rowSeekBarItemInfo);
		seekBarItemInfoList.add(colSeekBarItemInfo);

		//小屏幕
		if (isSmallScreen) {
			DeskSettingConstants.setSecondInfoOfSeekBar(this, seekBarItemInfoList,
					R.array.qvga_fun_app_rows_cols, mSettingFunAppRowsCols);
		} else {
			DeskSettingConstants.setSecondInfoOfSeekBar(this, seekBarItemInfoList,
					R.array.fun_app_rows_cols, mSettingFunAppRowsCols);
		}
	}

	/**
	 * <br>功能简述:更新功能表行列数的Entries
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param value
	 */
	private void updateFunAppRowsCoslEntries(int type) {
		CharSequence[] entries = mSettingFunAppRowsCols.getDeskSettingInfo().getSingleInfo()
				.getEntries();
		int entriesSize = entries.length;

		//设置自定义的显示值为：自定义行列（6X6）
		if (type == APP_FUN_ROWS_COLS_CUSTOM) {
			entries[entriesSize - 1] = getString(R.string.screen_grid_diy) + " ("
					+ mFunAppSettingInfo.getRowNum() + "×" + mFunAppSettingInfo.getColNum() + ")";
		} else {
			entries[entriesSize - 1] = getString(R.string.screen_grid_diy);
		}
	}
	/**
	 * <br>功能简述:保存功能表行列数
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 */
	public void saveFunAppRowsCols(DeskSettingItemBaseView view) {
		if (view == mSettingFunAppRowsCols && mFunAppSettingInfo != null) {
			int curValue = Integer
					.parseInt(String.valueOf(mSettingFunAppRowsCols.getSelectValue()));
			//自定义
			if (curValue == APP_FUN_ROWS_COLS_CUSTOM) {
				ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList = DeskSettingConstants
						.getSecondSeekBarItemInfo(mSettingFunAppRowsCols);
				if (seekBarItemInfoList != null) {
					//获取选择的seekbar值
					int rowsValue = seekBarItemInfoList.get(0).getSelectValue();
					int colsValue = seekBarItemInfoList.get(1).getSelectValue();
					//判断自定义的值是否改变
					if (mFunAppSettingInfo.getRowNum() != rowsValue
							|| mFunAppSettingInfo.getColNum() != colsValue) {
						mFunAppSettingInfo.setRowNum(rowsValue);
						mFunAppSettingInfo.setColNum(colsValue);
						mFunAppSettingInfo.setLineColumnNum(curValue); //设置当前选择类型并保持	
					}
				}
			} else {
				//先判断选项是否有更改
				int oldValue = mFunAppSettingInfo.getLineColumnNum();
				if (curValue != oldValue) {
					// 只改变FunAppSetting数据库中行列数的值，没对grid行列数进行修改。
					AppFuncUtils.getInstance(this).setGridStandard(curValue, null,
							AppFuncConstants.ALLAPPS_GRID);
					mFunAppSettingInfo.setLineColumnNum(curValue); //设置当前选择类型	
				}
			}

			//更新Sumarry
			updateFunAppRowsCoslEntries(curValue); //设置单选列表的选择值和更新summary
			mSettingFunAppRowsCols.updateSumarryText(); //更新summary	
		}
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public ScreenScroller getScreenScroller() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFlingStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		if (newScreen == 0) {
			showDeskTab();
		} else if (newScreen == 1) {
			showFunAppTab();
		}

	}

	@Override
	public void onScrollFinish(int currentScreen) {
		// TODO Auto-generated method stub

	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void scrollBy(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getScrollX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getScrollY() {
		// TODO Auto-generated method stub
		return 0;
	}
}

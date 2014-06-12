package com.jiubang.ggheart.apps.desks.Preferences.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.window.OrientationControl;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingGestureScreenActivity;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSeekBarItemInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSingleInfo;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.data.info.GravitySettingInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>类描述:桌面设置-应用设置-桌面View
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-19]
 */
public class DeskSettingScreenDeskTabView extends DeskSettingVisualAbsTabView {
	/**
	 * 屏幕方向
	 */
	private DeskSettingItemListView mSettingScreenOrientation;

//	/**
//	 * 屏幕行列数
//	 */
//	private DeskSettingItemListView mSettingScreenRowsCols;

	/**
	 * 屏幕切换特效
	 */
	private DeskSettingItemListView mSettingScreenChangeEffects;

	/**
	 * 屏幕循环切换
	 */
	private DeskSettingItemCheckBoxView mSettingScreenChangeLoop;

	/**
	 * 屏幕切换速度
	 */
	private DeskSettingItemListView mSettingScreenChangeSpeed;

	/**
	 * 桌面手势设置
	 */
	private DeskSettingItemBaseView mSettingGestrueScreen;

//	/**
//	 * 快捷条行数
//	 */
//	private DeskSettingItemListView mSettingDockRows;

	/**
	 * 快捷条循环切换
	 */
	private DeskSettingItemCheckBoxView mSettingDockChangeLoop;

	/**
	 * 快捷条图标位置自适应
	 */
	private DeskSettingItemCheckBoxView mSettingDockAutoFit;

	/**
	 * 屏幕切换特效
	 */
	int[] mScreenChangeEffectsImageId = { 
			R.drawable.screenedit_effect01_moren,
			R.drawable.screenedit_effect02_radom, 
			R.drawable.screenedit_effect14_gun,
			R.drawable.screenedit_effect18_ball,
			R.drawable.screenedit_effect04_wave, 
			R.drawable.screenedit_effect17_cylinder,
			R.drawable.screenedit_effect11_bounce,
			R.drawable.screenedit_effect10_boxout,
			R.drawable.screenedit_effect09_boxin,
			R.drawable.screenedit_effect08_windmills,
			R.drawable.screenedit_effect13_doublechild,
			R.drawable.screenedit_effect06_easyroll,
			R.drawable.screenedit_effect_stack,
			R.drawable.screenedit_effect12_push,
			R.drawable.screenedit_effect05_roll,
			R.drawable.screenedit_effect16_xuan,
			R.drawable.screenedit_effect15_shutter, 
			R.drawable.screenedit_effect07_wallpicroll,
			R.drawable.screenedit_effect03_userdefine,
	};

	private GoSettingControler mGoSettingControler;
	private GravitySettingInfo mGravitySettingInfo;
//	private DesktopSettingInfo mDesktopSettingInfo;
	private ScreenSettingInfo mScreenSettingInfo;
	private ShortCutSettingInfo mShortCutSettingInfo;
	private EffectSettingInfo mEffectSettingInfo;

	public Context mContext;

//	private final static int SCREEN_ROWS_COLS_CUSTOM = 4; //屏幕行列数自定义选项

	private final static int GRID_EFFECTOR_TYPE_CYLINDER = 15; //圆柱特效

	private final static int GRID_EFFECTOR_TYPE_SPHERE = 16; //球特效

	private final static int GRID_EFFECTOR_TYPE_CUSTOM = -2;

	public DeskSettingScreenDeskTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		findView();
	}

	public DeskSettingScreenDeskTabView(Context context) {
		super(context);
		mContext = context;
		findView();
	}

	protected void findView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);

		View mDeskView = inflater.inflate(R.layout.desk_setting_app_of_desk, null);
		mSettingScreenOrientation = (DeskSettingItemListView) mDeskView
				.findViewById(R.id.screen_orientation);
		mSettingScreenOrientation.setOnValueChangeListener(this);

//		mSettingScreenRowsCols = (DeskSettingItemListView) mDeskView
//				.findViewById(R.id.screen_rows_cols);
//		mSettingScreenRowsCols.setOnValueChangeListener(this);

		mSettingScreenChangeEffects = (DeskSettingItemListView) mDeskView
				.findViewById(R.id.desktop_transition);
		mSettingScreenChangeEffects.setOnValueChangeListener(this);

		mSettingScreenChangeLoop = (DeskSettingItemCheckBoxView) mDeskView
				.findViewById(R.id.screen_looping);
		mSettingScreenChangeLoop.setOnValueChangeListener(this);

		mSettingScreenChangeSpeed = (DeskSettingItemListView) mDeskView
				.findViewById(R.id.screen_transition_effect);
		mSettingScreenChangeSpeed.setOnValueChangeListener(this);

		mSettingGestrueScreen = (DeskSettingItemBaseView) mDeskView
				.findViewById(R.id.gesture_screen);
		mSettingGestrueScreen.setOpenIntent(new Intent(mContext,
				DeskSettingGestureScreenActivity.class));

		//桌面-快捷条设置
//		mSettingDockRows = (DeskSettingItemListView) mDeskView.findViewById(R.id.dock_rows);
//		mSettingDockRows.setOnValueChangeListener(this);

		mSettingDockChangeLoop = (DeskSettingItemCheckBoxView) mDeskView
				.findViewById(R.id.dock_change_loop);

		mSettingDockAutoFit = (DeskSettingItemCheckBoxView) mDeskView
				.findViewById(R.id.dock_auto_fit);
		addView(mDeskView);
	}

	public void load() {
		mGoSettingControler = GOLauncherApp.getSettingControler();
		mGravitySettingInfo = mGoSettingControler.getGravitySettingInfo();
//		mDesktopSettingInfo = mGoSettingControler.getDesktopSettingInfo();
		mScreenSettingInfo = mGoSettingControler.getScreenSettingInfo();
		mShortCutSettingInfo = mGoSettingControler.getShortCutSettingInfo();
		mEffectSettingInfo = mGoSettingControler.getEffectSettingInfo();

		loadDesk();
		loadDock();
	}

	/**
	 * <br>功能简述:加载桌面设置数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void loadDesk() {
		if (mGravitySettingInfo != null) {

			//屏幕方向
			int oldSelectValue = mGravitySettingInfo.mOrientationType;
			DeskSettingConstants.setSingleInfoValueAndSummary(oldSelectValue,
					mSettingScreenOrientation);
		}

		//设置桌面行列数
		setSettingScreenRowsColsInfo();

		//设置桌面切换特效
		setSettingScreenChangeEffectsInfo();

		//设置屏幕切换速度
		setSettingScreenChangeSpeed();

		if (mScreenSettingInfo != null) {
			mSettingScreenChangeLoop.setIsCheck(mScreenSettingInfo.mScreenLooping);
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
//			DeskSettingSingleInfo singleInfo = mSettingDockRows.getDeskSettingInfo()
//					.getSingleInfo();
//			if (singleInfo != null) {
//				singleInfo.setSelectValue(oldSelectValue);
//				mSettingDockRows.updateSumarryText();
//			}

			//快捷条循环切换
			mSettingDockChangeLoop.setIsCheck(mShortCutSettingInfo.mAutoRevolve);

			//快捷条图标位置自适应
			mSettingDockAutoFit.setIsCheck(mShortCutSettingInfo.mAutoFit);
		}
	}

	public void save() {
		saveDesk();
		saveDock();
	}

	/**
	 * <br>功能简述:保存桌面设置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void saveDesk() {
		saveScreenRowsCols(); //保存屏幕行列数
		saveScreenChangeLoop(); //保存屏幕循环切换
	}

	/**
	 * <br>功能简述:保存屏幕循环切换
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void saveScreenChangeLoop() {
		if (mScreenSettingInfo != null) {
			if (mScreenSettingInfo.mScreenLooping != mSettingScreenChangeLoop.getIsCheck()) {
				mScreenSettingInfo.mScreenLooping = mSettingScreenChangeLoop.getIsCheck();
				GOLauncherApp.getSettingControler().updateScreenSettingInfo(mScreenSettingInfo); //保存
			}
		}
	}

	/**
	 * <br>功能简述:保存屏幕行列数
	 * <br>功能详细描述:
	 * <br>注意:需要放到退出时才保存。因为执行保存会修改桌面东西。导致很慢
	 * @param view
	 * @param value
	 */
	public void saveScreenRowsCols() {
//		if (mDesktopSettingInfo != null) {
//			boolean changeRowsCols = false;
//			
//			//单选确认框
//			int curValue = 0;
//			if (mSettingScreenRowsCols != null) {
//				curValue = Integer.parseInt(String.valueOf(mSettingScreenRowsCols.getSelectValue()));
//			}
//			
//
//			//自定义
//			if (curValue == SCREEN_ROWS_COLS_CUSTOM) {
//				ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList = DeskSettingConstants
//						.getSecondSeekBarItemInfo(mSettingScreenRowsCols);
//				if (mSettingScreenRowsCols != null) {
//					//获取选择的seekbar值
//					int rowsValue = seekBarItemInfoList.get(0).getSelectValue();
//					int colsValue = seekBarItemInfoList.get(1).getSelectValue();
//					if (mDesktopSettingInfo.mRow != rowsValue
//							|| mDesktopSettingInfo.mColumn != colsValue) {
//						mDesktopSettingInfo.mRow = rowsValue;
//						mDesktopSettingInfo.mColumn = colsValue;
//						mDesktopSettingInfo.mStyle = curValue;
//						changeRowsCols = true;
//					}
//				}
//			} else {
//				int oldValue = mDesktopSettingInfo.mStyle;
//				if (curValue != oldValue) {
//					mDesktopSettingInfo.setRows(curValue);
//					mDesktopSettingInfo.setColumns(curValue);
//					mDesktopSettingInfo.mStyle = curValue;
//					changeRowsCols = true;
//				}
//			}
//
//			//判断是否修改自动调整小部件和图片位置
//			boolean autofit = true; 
//			if (mSettingScreenRowsCols != null) {
//				autofit = mSettingScreenRowsCols.getDeskSettingInfo().getSingleInfo()
//						.getCheckBoxIsCheck();
//			}
//			if (mDesktopSettingInfo.mAutofit != autofit) {
//				mDesktopSettingInfo.mAutofit = autofit;
//				changeRowsCols = true;
//			}
//
//			if (changeRowsCols) {
//				//屏幕数做过修改。15屏广告就不再请求了
//				AdvertControl.getAdvertControlInstance(mContext).setCanRequestAdvertState(false);
//				//24小时重新请求
//				AdvertControl.getAdvertControlInstance(mContext).setCanRequestAgainState(false);
//				GOLauncherApp.getSettingControler().updateDesktopSettingInfo(mDesktopSettingInfo); //保存数据
//			}
//		}
	}

	/**
	 * <br>功能简述:保存Dock条设置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void saveDock() {
		if (mShortCutSettingInfo != null) {
			//快捷条行数
//			boolean isChangeDockRows = false;
//			int rowValue = Integer.parseInt(String.valueOf(mSettingDockRows.getSelectValue()));
//			if (mShortCutSettingInfo.mRows != rowValue) {
//				mShortCutSettingInfo.mRows = rowValue;
//				isChangeDockRows = true;
//			}

			boolean isChangeDockChangeLoop = false;
			//快捷条循环切换
			if (mShortCutSettingInfo.mAutoRevolve != mSettingDockChangeLoop.getIsCheck()) {
				mShortCutSettingInfo.mAutoRevolve = mSettingDockChangeLoop.getIsCheck();
				isChangeDockChangeLoop = true;
			}

			//快捷条图标位置自适应
			boolean isChangeDockAutoFit = false;
			if (mShortCutSettingInfo.mAutoFit != mSettingDockAutoFit.getIsCheck()) {
				mShortCutSettingInfo.mAutoFit = mSettingDockAutoFit.getIsCheck();
				isChangeDockAutoFit = true;
			}

			// 全局设置保存
			if (isChangeDockChangeLoop || isChangeDockAutoFit) {
				GOLauncherApp.getSettingControler().updateShortCutSetting_NonIndepenceTheme(
						mShortCutSettingInfo);
//				if (isChangeDockRows) {
//					GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
//							IDiyMsgIds.DOCK_SETTING_CHANGED_ROW, -1, null, null);
//				}
			}
		}
	}

	@Override
	public boolean onValueChange(DeskSettingItemBaseView view, Object value) {
		saveScreenOrientation(view); //保存屏幕方向

		changeScreenRowsCols(view, value); //修改屏幕行列数

		changeDockRows(view); //修改Dock条数的显示

		saveScreenEffect(view); //屏幕切换特效

		saveScreenChangeSpeed(view); //屏幕切换速度

		return true;
	}

	/**
	 * <br>功能简述:保存屏幕方向
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 * @param value
	 */
	public void saveScreenOrientation(DeskSettingItemBaseView view) {
		if (view == mSettingScreenOrientation) {
			mSettingScreenOrientation.updateSumarryText();
			mGravitySettingInfo.mOrientationType = Integer.parseInt(String
					.valueOf(mSettingScreenOrientation.getSelectValue()));
			mGoSettingControler.updateGravitySettingInfo(mGravitySettingInfo);
			// 检查屏幕翻转设置，并应用
			OrientationControl.setOrientation((Activity) mContext);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SCREEN_RESET_ORIENTATION, -1, null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
					IDiyMsgIds.SCREEN_ORIENTATION_CHANGE, -1, null, null);
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
//		if (view == mSettingScreenRowsCols) {
//			//判断是否从seekBarDialog那里返回，特殊处理
//			if (value instanceof String[]) {
//				updateSceenRowsCoslEntriesCustomTemp(); //更新临时显示内容，等刷新SingleDialogWithCheckBox内容时可以调用
//				mSettingScreenRowsCols.updateSingleDialogWithCheckBox();
//			} else {
//				//单选确认框
//				int curValue = Integer.parseInt(String.valueOf(mSettingScreenRowsCols
//						.getSelectValue()));
//				//判断是否自定义
//				if (curValue == SCREEN_ROWS_COLS_CUSTOM) {
//					updateSceenRowsCoslEntriesCustomTemp();
//				} else {
//					updateSceenRowsCoslEntries(curValue); //更新显示内容
//				}
//			}
//			mSettingScreenRowsCols.updateSumarryText(); //更新Summary
//		}
	}

	/**
	 * <br>功能简述:修改Dock条数的显示
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 */
	public void changeDockRows(DeskSettingItemBaseView view) {
//		if (view == mSettingDockRows) {
//			mSettingDockRows.updateSumarryText();
//		}
	}

	/**
	 * <br>功能简述:屏幕切换特效
	 * <br>功能详细描述:
	 * <br>注意:在改变值为自定时刷新的原因：如果本来是自定义。在save时再保存就判断没有更改值。无法保存
	 * @param view
	 */
	public void saveScreenEffect(DeskSettingItemBaseView view) {
		if (view == mSettingScreenChangeEffects && mEffectSettingInfo != null) {
			mSettingScreenChangeEffects.updateSumarryText(); //更新summary

			int curValue = Integer.parseInt(String.valueOf(mSettingScreenChangeEffects
					.getSelectValue()));

			//自定义特效
			if (curValue == GRID_EFFECTOR_TYPE_CUSTOM) {
				//获取2级多选勾选的的值
				int[] curSecondValue = DeskSettingConstants
						.getSecondInfoMultiSelectValue(mSettingScreenChangeEffects);
				if (curSecondValue != null) {
					mEffectSettingInfo.mEffectCustomRandomEffects = curSecondValue;
				}
				mEffectSettingInfo.mEffectorType = GRID_EFFECTOR_TYPE_CUSTOM; //设置自定义类型
				mGoSettingControler.updateEffectSettingInfo(mEffectSettingInfo); //保存
			} else {
				// 考虑到部分用户的机子性能不好，不能很好支持实现圆柱和球特效，所以提醒用户
				if (curValue == GRID_EFFECTOR_TYPE_CYLINDER
						|| curValue == GRID_EFFECTOR_TYPE_SPHERE) {
					Toast.makeText(mContext, mContext.getString(R.string.effect_warn),
							Toast.LENGTH_LONG).show();
				}

				if (mEffectSettingInfo.mEffectorType != curValue) {
					mEffectSettingInfo.mEffectorType = curValue;
					mGoSettingControler.updateEffectSettingInfo(mEffectSettingInfo); //保存
				}
			}
		}
	}

	/**
	 * <br>功能简述:屏幕切换速度
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 */
	public void saveScreenChangeSpeed(DeskSettingItemBaseView view) {
		if (view == mSettingScreenChangeSpeed && mEffectSettingInfo != null) {
			int curValue = Integer.parseInt(String.valueOf(mSettingScreenChangeSpeed
					.getSelectValue()));
			//自定义类型-调节条队列数据
			ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList = DeskSettingConstants
					.getSecondSeekBarItemInfo(mSettingScreenChangeSpeed);

			if (seekBarItemInfoList != null) {
				//自定义类型
				if (curValue == DeskSettingConstants.SCREEN_CHANGE_SPEED_TYPE_CUSTOM) {

					//获取选择的seekbar值
					int speedValue = seekBarItemInfoList.get(0).getSelectValue();
					int elasticValue = seekBarItemInfoList.get(1).getSelectValue();

					//判断值是否有改变
					if (mEffectSettingInfo.mScrollSpeed != speedValue
							|| mEffectSettingInfo.mBackSpeed != elasticValue) {
						mEffectSettingInfo.mScrollSpeed = speedValue;
						mEffectSettingInfo.mBackSpeed = elasticValue;

						mGoSettingControler.updateEffectSettingInfo(mEffectSettingInfo); //保存
					}
				} else {
					//通过选择得值（快速、普通、缓慢）获取速度和弹力值
					int speedValue = DeskSettingConstants.getScreenChangeSpeedSize(curValue);
					int elasticValue = DeskSettingConstants.getScreenChangeSpeedNormalElastic();

					//判断值是否有改变
					if (mEffectSettingInfo.mScrollSpeed != speedValue
							|| mEffectSettingInfo.mBackSpeed != elasticValue) {
						mEffectSettingInfo.mScrollSpeed = speedValue;
						mEffectSettingInfo.mBackSpeed = elasticValue;

						//设置自定义条的默认值，但选择默认速度60。再点击自定义时就默认选择60
						seekBarItemInfoList.get(0).setSelectValue(speedValue);
						seekBarItemInfoList.get(1).setSelectValue(elasticValue);

						mGoSettingControler.updateEffectSettingInfo(mEffectSettingInfo); //保存
					}
				}

				//更新Sumarry
				updateScreenChangeSpeedEntries(curValue); //设置单选列表的选择值和更新summary
				mSettingScreenChangeSpeed.updateSumarryText(); //设置单选列表的选择值和更新summary
			}
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	public void changeOrientation() {
//		mSettingScreenOrientation.dismissDialog();
//		mSettingScreenRowsCols.dismissDialog();
//		mSettingScreenChangeEffects.dismissDialog();
//		mSettingScreenChangeSpeed.dismissDialog();
//		mSettingDockRows.dismissDialog();
	}

	public void onResume() {

	}

	/**
	 * <br>功能简述:设置桌面行列数
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void setSettingScreenRowsColsInfo() {
//		if (mDesktopSettingInfo != null) {
//
//			//屏幕行列数
//			//设置自动调整小部件和图标大小
//			boolean autofit = mDesktopSettingInfo.mAutofit;
//			mSettingScreenRowsCols.getDeskSettingInfo().getSingleInfo().setCheckBoxIsCheck(autofit);
//
//			int screenRowsColsType = mDesktopSettingInfo.mStyle;
//			updateSceenRowsCoslEntries(screenRowsColsType);
//			DeskSettingConstants.setSingleInfoValueAndSummary(screenRowsColsType,
//					mSettingScreenRowsCols);
//
//			//行数seekBar
//			DeskSettingSeekBarItemInfo rowSeekBarItemInfo = new DeskSettingSeekBarItemInfo();
//			rowSeekBarItemInfo.setTitle(mContext.getString(R.string.screen_row_dialog_msg)); //设置标题
//			rowSeekBarItemInfo.setMinValue(DeskSettingConstants.ROWS_COLS_MIN_SIZE); //设置最小值
//			rowSeekBarItemInfo.setMaxValue(DeskSettingConstants.ROWS_COLS_MAX_SIZE); //设置最大值
//			rowSeekBarItemInfo.setSelectValue(mDesktopSettingInfo.mRow); //设置默认选择值
//
//			//列数seekBar
//			DeskSettingSeekBarItemInfo colSeekBarItemInfo = new DeskSettingSeekBarItemInfo();
//			colSeekBarItemInfo.setTitle(mContext.getString(R.string.screen_column_dialog_msg)); //设置标题
//			colSeekBarItemInfo.setMinValue(DeskSettingConstants.ROWS_COLS_MIN_SIZE); //设置最小值
//			colSeekBarItemInfo.setMaxValue(DeskSettingConstants.ROWS_COLS_MAX_SIZE); //设置最大值
//			colSeekBarItemInfo.setSelectValue(mDesktopSettingInfo.mColumn);; //设置默认选择值
//
//			//创建SeekBarInfo队列
//			ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList = new ArrayList<DeskSettingSeekBarItemInfo>();
//			seekBarItemInfoList.add(rowSeekBarItemInfo);
//			seekBarItemInfoList.add(colSeekBarItemInfo);
//			DeskSettingConstants.setSecondInfoOfSeekBar(mContext, seekBarItemInfoList,
//					R.array.screen_rows_cols_title, mSettingScreenRowsCols);
//		}

	}

	/**
	 * <br>功能简述:设置桌面切换特效
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void setSettingScreenChangeEffectsInfo() {
		if (mEffectSettingInfo != null) {
			int oldSelectValue = mEffectSettingInfo.mEffectorType; //获取数据库值
			DeskSettingConstants.setSingleInfoValueAndSummary(oldSelectValue,
					mSettingScreenChangeEffects); //设置值和更新summary

			//设置图片列表
			DeskSettingSingleInfo singleInfo = mSettingScreenChangeEffects.getDeskSettingInfo()
					.getSingleInfo();
			if (singleInfo != null) {
				singleInfo.setImageId(mScreenChangeEffectsImageId);
				singleInfo.setIsAddImageBg(true); //设置需要合成背景图
			}

			int customPosition = 18; //多选2级菜单的位置
			int contentIndex[] = new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 }; // 二级菜单内容项索引
			int entrisId = R.array.select_desktop_transition; //对应显示的内容列表
			int entryValuesId = R.array.desktop_transition_value; //对应显示的内容列表的值
			int[] selectValue = mEffectSettingInfo.mEffectCustomRandomEffects; //已选的值
			mSettingScreenChangeEffects.setSecondInfoMulti(entrisId, entryValuesId, mScreenChangeEffectsImageId, contentIndex,
					selectValue, customPosition);
		}
	}

	/**
	 * <br>功能简述:设置屏幕切换速度
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void setSettingScreenChangeSpeed() {
		if (mEffectSettingInfo != null) {
			//获取选择的类型值
			int screenChangeSpeedType = DeskSettingConstants.getScreenChangeSpeedType(
					mEffectSettingInfo.mScrollSpeed, mEffectSettingInfo.mBackSpeed);
			updateScreenChangeSpeedEntries(screenChangeSpeedType); //设置单选列表的选择值和更新summary

			DeskSettingConstants.setSingleInfoValueAndSummary(screenChangeSpeedType,
					mSettingScreenChangeSpeed); //设置单选列表的选择值和更新summary

			//速度seekBar
			DeskSettingSeekBarItemInfo speedSeekBarItemInfo = new DeskSettingSeekBarItemInfo();
			speedSeekBarItemInfo.setTitle(mContext.getString(R.string.screen_speed)); //设置标题
			speedSeekBarItemInfo.setMinValue(DeskSettingConstants.SCREEN_CHANGE_SPEED_MIN); //设置最小值
			speedSeekBarItemInfo.setMaxValue(DeskSettingConstants.SCREEN_CHANGE_SPEED_MAX); //设置最大值
			speedSeekBarItemInfo.setSelectValue(mEffectSettingInfo.mScrollSpeed);; //设置默认选择值

			//弹力seekBar
			DeskSettingSeekBarItemInfo elasticSeekBarItemInfo = new DeskSettingSeekBarItemInfo();
			elasticSeekBarItemInfo.setTitle(mContext.getString(R.string.screen_elastic)); //设置标题
			elasticSeekBarItemInfo
					.setMinValue(DeskSettingConstants.SCREEN_CHANGE_SPEED_ELASTIC_MIN); //设置最小值
			elasticSeekBarItemInfo
					.setMaxValue(DeskSettingConstants.SCREEN_CHANGE_SPEED_ELASTIC_MAX); //设置最大值
			elasticSeekBarItemInfo.setSelectValue(mEffectSettingInfo.mBackSpeed); //设置默认选择值

			//创建SeekBarInfo队列
			ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList = new ArrayList<DeskSettingSeekBarItemInfo>();
			seekBarItemInfoList.add(speedSeekBarItemInfo);
			seekBarItemInfoList.add(elasticSeekBarItemInfo);

			DeskSettingConstants.setSecondInfoOfSeekBar(mContext, seekBarItemInfoList,
					R.array.screen_transition_entris, mSettingScreenChangeSpeed);

		}
	}

	/**
	 * <br>功能简述:更新屏幕行列数的Entries
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param value
	 */
	private void updateSceenRowsCoslEntries(int type) {
//		CharSequence[] entries = mSettingScreenRowsCols.getDeskSettingInfo().getSingleInfo()
//				.getEntries();
//		int entriesSize = entries.length;
//
//		//设置自定义的显示值为：自定义行列（6X6）
//		if (type == SCREEN_ROWS_COLS_CUSTOM) {
//			entries[entriesSize - 1] = mContext.getString(R.string.screen_grid_diy) + " ("
//					+ mDesktopSettingInfo.mRow + "×" + mDesktopSettingInfo.mColumn + ")";
//		} else {
//			entries[entriesSize - 1] = mContext.getString(R.string.screen_grid_diy);
//		}
	}

	/**
	 * <br>功能简述:更新屏切换速度的summary
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param value
	 */
	private void updateScreenChangeSpeedEntries(int type) {
		CharSequence[] entries = mSettingScreenChangeSpeed.getDeskSettingInfo().getSingleInfo()
				.getEntries();
		int entriesSize = entries.length;

		//设置自定义的显示值为：自定义（速度：100）
		if (type == DeskSettingConstants.SCREEN_CHANGE_SPEED_TYPE_CUSTOM) {
			entries[entriesSize - 1] = mContext.getString(R.string.desk_setting_custom_string)
					+ " (" + mContext.getString(R.string.screen_speed) + ":"
					+ mEffectSettingInfo.mScrollSpeed + ")";
		} else {
			entries[entriesSize - 1] = mContext.getString(R.string.desk_setting_custom_string);
		}
	}

	/**
	 * <br>功能简述:更新屏幕行列数的Entries-自定义—临时更新
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param value
	 */
	private void updateSceenRowsCoslEntriesCustomTemp() {
//		ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList = DeskSettingConstants
//				.getSecondSeekBarItemInfo(mSettingScreenRowsCols);
//		if (mSettingScreenRowsCols != null) {
//			//获取选择的seekbar值
//			int rowsValue = seekBarItemInfoList.get(0).getSelectValue();
//			int colsValue = seekBarItemInfoList.get(1).getSelectValue();
//
//			CharSequence[] entries = mSettingScreenRowsCols.getDeskSettingInfo().getSingleInfo()
//					.getEntries();
//			int entriesSize = entries.length;
//			entries[entriesSize - 1] = mContext.getString(R.string.screen_grid_diy) + " ("
//					+ rowsValue + "×" + colsValue + ")";
//		}

	}

}
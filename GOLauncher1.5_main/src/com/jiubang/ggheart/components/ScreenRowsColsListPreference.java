package com.jiubang.ggheart.components;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceManager.OnActivityDestroyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 在桌面设置-屏幕设置-桌面行列数做了修改
 * @author shenjinbao
 *
 */
public class ScreenRowsColsListPreference extends DeskListPreference
		implements
			OnSeekBarChangeListener {

	private CharSequence[] mEntries;
	private CharSequence[] mEntryValues;
	private int mSelectedId;

	private Context mContext;
	private DesktopSettingInfo mDesktopInfo;
	private CheckBox mCheckBox;

	// 用于记录用户singlechoice和checkBox的最后一个选择
	private int mLastChoice;
	private boolean mCheckBoxChoice;

	private AlertDialog mDialog;
	private DeskBuilder mBuilder;
	private final int mCustomChoice = 3;
	private int mRows;
	private int mCols;
	private int mTempRows;
	private int mTempCols;
	// 自定义行列数相关
	private AlertDialog mAlertDialog;
	private SeekBar mRowSeekBar;
	private SeekBar mColumnSeekBar;
	private DeskTextView mRowValueText;
	private DeskTextView mColumnValueText;
	private final static int sMAX = 7;
	private final static int sMIN = 3;

	public ScreenRowsColsListPreference(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public ScreenRowsColsListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public void init() {

		GoSettingControler controler = GOLauncherApp.getSettingControler();
		mDesktopInfo = controler.createDesktopSettingInfo();

		mRows = mDesktopInfo.mRow;
		mCols = mDesktopInfo.mColumn;
		mTempRows = mDesktopInfo.mRow;
		mTempCols = mDesktopInfo.mColumn;
		mCheckBoxChoice = mDesktopInfo.mAutofit;
		mSelectedId = Integer.valueOf(mDesktopInfo.mStyle) - 1;
		mLastChoice = Integer.valueOf(mDesktopInfo.mStyle) - 1;

		screenGridSetEntries(mSelectedId);
		mEntryValues = getEntryValues();
	}

	@Override
	protected BaseAdapter createAdapter() {
		CharSequence[] entries = getEntries();
		if (null == entries) {
			return null;
		}
		// 如果是自定义选项的
		if (mLastChoice == mCustomChoice) {
			CharSequence diyGrid = mContext.getString(R.string.screen_grid_diy) + "(" + mTempRows
					+ "×" + mTempCols + ")";
			entries = new CharSequence[] { entries[0], entries[1], entries[2], diyGrid };
		}
		return new ArrayAdapter<CharSequence>(getContext(),
				R.layout.desk_select_dialog_singlechoice, R.id.radio_textview, entries);
	}

	@Override
	protected void showDialog(Bundle state) {
		try {
			// CharSequence title message postext negtext
			Field titleField = DialogPreference.class.getDeclaredField("mDialogTitle");
			titleField.setAccessible(true);
			Object titleObject = titleField.get(this);
			Field messageField = DialogPreference.class.getDeclaredField("mDialogMessage");
			messageField.setAccessible(true);
			Object messageObject = messageField.get(this);
			Field posField = DialogPreference.class.getDeclaredField("mPositiveButtonText");
			posField.setAccessible(true);
			Object posObject = posField.get(this);
			Field negField = DialogPreference.class.getDeclaredField("mNegativeButtonText");
			negField.setAccessible(true);
			Object negObject = negField.get(this);
			// Drawable icon
			Field iconField = DialogPreference.class.getDeclaredField("mDialogIcon");
			iconField.setAccessible(true);
			Object iconObject = iconField.get(this);

			if (mBuilder == null) {
				mBuilder = new DeskBuilder(getContext());
			}
			mBuilder.setTitle((CharSequence) titleObject);
			mBuilder.setIcon((Drawable) iconObject);

			mBuilder.setPositiveButton((CharSequence) posObject, this);
			mBuilder.setNegativeButton(null, null);

			View contentView = onCreateDialogView();
			if (contentView != null) {
				onBindDialogView(contentView);
				mBuilder.setView(contentView);
			} else {
				mBuilder.setMessage((CharSequence) messageObject);
			}

			onPrepareDialogBuilder(mBuilder);

			// Create the dialog
			final Dialog dialog = mDialog = mBuilder.create();

			dialog.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						// 用户按下back键，将相关数值复位
						resetData();
					}
					return false;
				}

			});

			if (state != null) {
				dialog.onRestoreInstanceState(state);
			}
			// TODO 软键盘先放放
			// if (needInputMethod()) {
			// requestInputMethod(dialog);
			// }
			dialog.setOnDismissListener(this);

			// 设置 mBuilder mDialog mWhichButtonClicked
			Field buildField = DialogPreference.class.getDeclaredField("mBuilder");
			buildField.setAccessible(true);
			buildField.set(this, mBuilder);
			Field dialogField = DialogPreference.class.getDeclaredField("mDialog");
			dialogField.setAccessible(true);
			dialogField.set(this, dialog);
			Field clickField = DialogPreference.class.getDeclaredField("mWhichButtonClicked");
			clickField.setAccessible(true);
			clickField.set(this, DialogInterface.BUTTON_NEGATIVE);

			// 注册监听
			PreferenceManager manager = getPreferenceManager();
			// Method method =
			// manager.getClass().getMethod("registerOnActivityDestroyListener",
			// this.getClass());
			// method.invoke(manager, this);
			// TODO 非公有函数，不能获取，采取拿值直接搞方案
			// 如果不行就算了，看源码只是消除弹出框
			Field managerField = PreferenceManager.class
					.getDeclaredField("mActivityDestroyListeners");
			managerField.setAccessible(true);
			Object managerObject = managerField.get(manager);
			// if (managerObject instanceof List<OnActivityResultListener>)
			{
				ArrayList<OnActivityDestroyListener> list = (ArrayList<OnActivityDestroyListener>) managerObject;
				if (list == null) {
					list = new ArrayList<OnActivityDestroyListener>();
				}

				if (!list.contains(this)) {
					list.add(this);
				}
				managerField.set(manager, list);
			}

			dialog.show();
		} catch (Exception e) {
			Log.i("ScreenRowsColsListPreference", "showDialog() has exception = " + e.getMessage());
			super.showDialog(state);
		}
	}

	// 重写这个方法，添加一个OK按钮
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {

		BaseAdapter mBaseAdapter = createAdapter();
		if (null != mBaseAdapter) {
			builder.setAdapter(mBaseAdapter, null);
		}

		// super.onPrepareDialogBuilder(builder);//不能调用父类的这个方法，否则点击列表项会关闭对话框
		builder.setSingleChoiceItems(mEntries, mLastChoice, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				mLastChoice = which;
				if (mLastChoice == mCustomChoice) {
					// 当用户点击了自定义之后,调用自定义对话框
					showDiyScreenGrid();
				}
			}

		});
		builder.setPositiveButton(getPositiveButtonText() == null ? "OK" : getPositiveButtonText(),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt) {

						mCheckBoxChoice = mCheckBox.isChecked();
						mSelectedId = mLastChoice;
						mRows = mTempRows;
						mCols = mTempCols;

						if (mSelectedId >= 0 && mSelectedId < mCustomChoice) {
							setSummary(mEntries[mSelectedId]);
							ScreenRowsColsListPreference.this
									.persistString(mEntryValues[mSelectedId].toString());
							ScreenRowsColsListPreference.this
									.callChangeListener(mEntryValues[mSelectedId]);
							mEntries = getEntries();

							int index = Integer.parseInt(mEntryValues[mSelectedId].toString());
							mDesktopInfo.setRows(index);
							mDesktopInfo.setColumns(index);
							mRows = mDesktopInfo.getRows();
							mCols = mDesktopInfo.getColumns();
						} else if (mSelectedId == mCustomChoice) {
							mDesktopInfo.mRow = mRows;
							mDesktopInfo.mColumn = mCols;
							ScreenRowsColsListPreference.this
									.persistString(mEntryValues[mSelectedId].toString());

							CharSequence[] gridStyles = mContext.getResources().getStringArray(
									R.array.screen_rows_cols_title);
							CharSequence diyGrid = mContext.getString(R.string.screen_grid_diy)
									+ "(" + mDesktopInfo.mRow + "×" + mDesktopInfo.mColumn + ")";
							setValue(mEntryValues[mSelectedId].toString());
							setEntries(new CharSequence[] { gridStyles[0], gridStyles[1],
									gridStyles[2], diyGrid });
							mEntries = getEntries();
							String currentText = mContext.getString(R.string.screen_grid_diy) + "("
									+ mDesktopInfo.mRow + "×" + mDesktopInfo.mColumn + ")";
							setSummary(currentText);
							// GOLauncherApp.getSettingControler().updateDesktopSettingInfo(mDesktopInfo);
						}

						paramDialogInterface.dismiss();
					}
				});

		// 自适应的单选框checkbox
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.display_setting_adaptive, null);
		mCheckBox = (CheckBox) layout.findViewById(R.id.check_adaptive);

		mCheckBox.setChecked(mCheckBoxChoice);
		mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub

			}

		});

		builder.setView(layout);
		builder.setNegativeButton(null, null);
	}

	// 获取调整小部件和图标的大小 checkbox的boolean 值
	public Boolean getCheckBoxAutofit() {
		return mCheckBoxChoice;
	}

	// 刷新一下数据
	public void setRowsCols(int row, int col, DesktopSettingInfo desktopInfo) {
		mDesktopInfo = desktopInfo;
		mTempRows = row;
		mTempCols = col;

		CharSequence[] gridStyles = mContext.getResources().getStringArray(
				R.array.screen_rows_cols_title);
		CharSequence diyGrid = mContext.getString(R.string.screen_grid_diy) + "(" + mTempRows + "×"
				+ mTempCols + ")";
		setEntries(new CharSequence[] { gridStyles[0], gridStyles[1], gridStyles[2], diyGrid });
		// 刷新数据显示
		refresh();
	}

	// 获取子项的显示内容
	private void screenGridSetEntries(int choice) {
		CharSequence[] gridStyles = mContext.getResources().getStringArray(
				R.array.screen_rows_cols_title);
		CharSequence diyGrid = mContext.getString(R.string.screen_grid_diy) + "("
				+ mDesktopInfo.mRow + "×" + mDesktopInfo.mColumn + ")";
		// 如果不是自定义选项的
		if (choice < mCustomChoice) {
			mEntries = gridStyles;
		} else {
			mEntries = new CharSequence[] { gridStyles[0], gridStyles[1], gridStyles[2], diyGrid };
		}
		setEntries(mEntries);
		setSummary(mEntries[mSelectedId]);
	}

	// 将相关变量数值复位
	public void resetData() {
		// 用户按下back键和取消时，将数值复位
		mLastChoice = mSelectedId;
		mTempRows = mRows;
		mTempCols = mCols;

		setValue(mEntryValues[mSelectedId].toString());
		setEntries(mEntries);
		setSummary(getEntry());
	}

	// 刷新listview中的SingleChoiceItems的显示内容
	public void refresh() {
		if (mDialog.isShowing()) {
			mDialog.dismiss();
		}
		onPrepareDialogBuilder(mBuilder);
		mDialog = mBuilder.create();
		mDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					// 用户按下back键，将相关数值复位
					resetData();
				}
				return false;
			}
		});
		mDialog.show();
	}

	// 将相关变量数值复位
	public void resetData(DesktopSettingInfo desktopInfo) {
		desktopInfo.mRow = mRows;
		desktopInfo.mColumn = mCols;

		setValue(mEntryValues[mSelectedId].toString());
	}

	// 获取对话框的组件
	private void findAndSetCmps(View view) {
		mRowSeekBar = (SeekBar) view.findViewById(R.id.mRowBar);
		mColumnSeekBar = (SeekBar) view.findViewById(R.id.mColumnBar);
		mRowValueText = (DeskTextView) view.findViewById(R.id.rowActualValue);
		mColumnValueText = (DeskTextView) view.findViewById(R.id.columnActualValue);
		// 设置
		mRowSeekBar.setOnSeekBarChangeListener(this);
		mRowSeekBar.setMax(sMAX);
		mColumnSeekBar.setOnSeekBarChangeListener(this);
		mColumnSeekBar.setMax(sMAX);
		if (null != mDesktopInfo) {
			mRowSeekBar.setProgress(mDesktopInfo.mRow - sMIN);
			String value = String.valueOf(mDesktopInfo.mRow);
			mRowValueText.setText(value);

			mColumnSeekBar.setProgress(mDesktopInfo.mColumn - sMIN);
			value = String.valueOf(mDesktopInfo.mColumn);
			mColumnValueText.setText(value);
		}
	}

	private void showDiyScreenGrid() {
		// 如果对话框为空就新建一个
		if (mAlertDialog == null) {
			// create对话框
			mAlertDialog = new AlertDialog(mContext) {
			};
			LayoutInflater inflater = LayoutInflater.from(mContext);
			View layout = inflater.inflate(R.layout.seekbar_double, null);
			mAlertDialog.setView(layout);
			mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
			mAlertDialog.setTitle(mContext.getString(R.string.screen_grid_diy));
			findAndSetCmps(layout);
			// 添加确定按钮
			mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
					mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							// 更改当前的行列数
							mTempRows = mRowSeekBar.getProgress() + sMIN;
							mTempCols = mColumnSeekBar.getProgress() + sMIN;
							ScreenRowsColsListPreference.this.setRowsCols(mTempRows, mTempCols,
									mDesktopInfo);
							// //要显示的小行字体
							// String currentText =
							// getString(R.string.screen_grid_diy)+
							// "("+mDesktopInfo.mRow+"×"+mDesktopInfo.mColumn+")";
							// //设置小行字体
							// mScreenRowsColsPref.setSummary(currentText);

							// screenGridSetEntries(false);
							// //刷新数据
							// GOLauncherApp.getSettingControler().updateDesktopSettingInfo(mDesktopInfo);

							ScreenRowsColsListPreference.this.setRowsCols(mRowSeekBar.getProgress()
									+ sMIN, mColumnSeekBar.getProgress() + sMIN, mDesktopInfo);
							dialog.dismiss();
						}
					});
			// 添加取消按钮
			mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
					mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					});
		}
		// 对话框没打开时才执行
		if (!mAlertDialog.isShowing()) {
			mRowSeekBar.setProgress(mTempRows - sMIN);
			mRowValueText.setText(String.valueOf(mTempRows));
			mColumnSeekBar.setProgress(mTempCols - sMIN);
			mColumnValueText.setText(String.valueOf(mTempCols));
			mAlertDialog.show();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		String value = null;
		if (seekBar == mRowSeekBar) {
			mTempRows = progress + sMIN;
			value = String.valueOf(mTempRows);
			mRowValueText.setText(value);
		} else if (seekBar == mColumnSeekBar) {
			mTempCols = progress + sMIN;
			value = String.valueOf(mTempCols);
			mColumnValueText.setText(value);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}
}

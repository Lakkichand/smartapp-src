package com.jiubang.ggheart.apps.desks.Preferences.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.OnValueChangeListener;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DeskSettingBaseDialog;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DeskSettingSingleChoiceWithCheckboxDialog;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogFactory;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogTypeId;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.OnDialogSelectListener;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingFontSingleInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingGestureInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingMultiInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSingleInfo;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author licanhui
 * @date [2012-9-10]
 */
public class DeskSettingItemListView extends DeskSettingItemBaseView
		implements
			OnDialogSelectListener {
	private Context mContext;
	private DeskSettingInfo mDeskSettingInfo;
	private OnValueChangeListener mOnValueChangeListener; //变值监听器
	private OnClickListener mOnListClickListner; //点击监听器
	private DeskSettingBaseDialog mDialog;

	public DeskSettingItemListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		mDeskSettingInfo = new DeskSettingInfo();

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeskSettingItemView);

		//对话框标题
		String dialogTitle = a.getString(R.styleable.DeskSettingItemView_dialogTitle);
		//显示的内容列表
		CharSequence[] entries = a.getTextArray(R.styleable.DeskSettingItemView_listEntries);
		
		//显示内容列表对应的值
		CharSequence[] entryValues = a
				.getTextArray(R.styleable.DeskSettingItemView_listEntryValues);
		int type = a.getInt(R.styleable.DeskSettingItemView_dialogType, -1);

		mDeskSettingInfo.setType(type);

		//单选
		if (type == DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE
				|| type == DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE_WITH_CHECKBOX) {
			DeskSettingSingleInfo singleInfo = new DeskSettingSingleInfo();
			singleInfo.setTitle(dialogTitle);
			singleInfo.setEntries(entries);
			singleInfo.setEntryValues(entryValues);
			if (type == DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE_WITH_CHECKBOX) {
				String singleDialogTips = a
						.getString(R.styleable.DeskSettingItemView_singleDialogTips);
				singleInfo.setCheckBoxString(singleDialogTips);
			}

			mDeskSettingInfo.setSingleInfo(singleInfo);
		}

		//多选
		else if (type == DialogTypeId.TYPE_DESK_SETTING_MULTICHOICE) {
			DeskSettingMultiInfo multiInInfo = new DeskSettingMultiInfo();
			multiInInfo.setTitle(dialogTitle);
			multiInInfo.setEntries(entries);
			multiInInfo.setEntryValues(entryValues);
			mDeskSettingInfo.setMultiInInfo(multiInInfo);
		}

		//手势
		else if (type == DialogTypeId.TYPE_DESK_SETTING_GESTURE) {
			DeskSettingGestureInfo gestureInfo = new DeskSettingGestureInfo();
			gestureInfo.setTitle(dialogTitle);
			gestureInfo.setEntries(entries);
			gestureInfo.setEntryValues(entryValues);
			mDeskSettingInfo.setmGestureInfo(gestureInfo);
		}

		//字体单选
		else if (type == DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE_FONT) {
			DeskSettingFontSingleInfo fontSingleInfoInfo = new DeskSettingFontSingleInfo();
			fontSingleInfoInfo.setTitle(dialogTitle);
			mDeskSettingInfo.setSingleInfo(fontSingleInfoInfo);
		}

		a.recycle();

		setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (mOnListClickListner != null) {
			mOnListClickListner.onClick(this);
		}
		if (mOnValueChangeListener != null) {
			if (mDialog != null && mDialog.isShowing()) {
				return;	
			}
			mDialog = DialogFactory.produceDialog(mContext, mDeskSettingInfo, this);
			mDialog.show();
		}
	}

	/**
	 * <br>功能简述:关闭对话框
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void dismissDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	/**
	 * <br>功能简述:如果是带CheckBox的对话框。要更新里面的内容
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void updateSingleDialogWithCheckBox() {
		if (mDialog != null && mDialog instanceof DeskSettingSingleChoiceWithCheckboxDialog) {
			((DeskSettingSingleChoiceWithCheckboxDialog) mDialog).updateView();
		}
	}

	@Override
	public boolean onDialogSelectValue(Object value) {
		if (mOnValueChangeListener != null) {
			mOnValueChangeListener.onValueChange(this, value);
		}
		return false;
	}

	/**
	 * <br>功能简述:设置值更改监听器
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param onValueChangeListener
	 */
	public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
		mOnValueChangeListener = onValueChangeListener;
	}

	public DeskSettingInfo getDeskSettingInfo() {
		return mDeskSettingInfo;
	}

	/**
	 * <br>功能简述:设置点击监听器
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param onValueChangeListener
	 */
	public void setOnListClickListener(OnClickListener onListClickListner) {
		mOnListClickListner = onListClickListner;
	}

	public void setDeskSettingInfo(DeskSettingInfo deskSettingInfo) {
		this.mDeskSettingInfo = deskSettingInfo;
	}

	/**
	 * <br>功能简述:刷新sumarry
	 * <br>功能详细描述:根据不同类型获取不同显示的值
	 * <br>注意:
	 */
	public void updateSumarryText() {
		if (mDeskSettingInfo != null) {
			switch (mDeskSettingInfo.getType()) {
				case DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE :
				case DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE_WITH_CHECKBOX :
					DeskSettingSingleInfo singleInfo = mDeskSettingInfo.getSingleInfo();
					if (singleInfo != null) {
						super.setSummaryText(singleInfo.getSelectValueText());
					}
					break;

				case DialogTypeId.TYPE_DESK_SETTING_MULTICHOICE :

					break;

				default :
					break;
			}

		}
	}

	/**
	 * <br>功能简述:刷新sumarry
	 * <br>功能详细描述:根据不同类型获取不同显示的值
	 * <br>注意:
	 */
	public Object getSelectValue() {
		Object selectValue = null;
		if (mDeskSettingInfo != null) {
			switch (mDeskSettingInfo.getType()) {
				case DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE :
				case DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE_WITH_CHECKBOX :
					DeskSettingSingleInfo singleInfo = mDeskSettingInfo.getSingleInfo();
					if (singleInfo != null) {
						selectValue = singleInfo.getSelectValue();
					}
					break;

				case DialogTypeId.TYPE_DESK_SETTING_MULTICHOICE :

					break;

				case DialogTypeId.TYPE_DESK_SETTING_GESTURE :
					DeskSettingGestureInfo deskSettingGestureInfo = mDeskSettingInfo
							.getmGestureInfo();
					if (deskSettingGestureInfo != null) {
						selectValue = deskSettingGestureInfo.getSelectValue();
					}
					break;
				default :
					break;
			}

		}
		return selectValue;
	}

	@Override
	public boolean onPositiveClick(View view) {
		return true;
	}

	@Override
	public boolean onNegativeClick(View view) {
		return true;
	}

	/**
	 * <br>功能简述:设置二级菜单为多选
	 * <br>功能详细描述:
	 * <br>注意:需要设置父类引用
	 * @param entrisId 显示类容列表ID
	 * @param entryValuesId 显示内容值列表ID
	 * @param cutSize 裁剪的大小
	 * @param selectValue 勾选的值
	 * @param customPosition 自定义菜单在哪个位置
	 */
	public void setSecondInfoMulti(int entrisId, int entryValuesId, int[] imageId, int cutSize,
			int[] selectValue, int customPosition) {
		DeskSettingInfo customDeskSettingInfo = new DeskSettingInfo();
		customDeskSettingInfo.setType(DialogTypeId.TYPE_DESK_SETTING_MULTICHOICE); //设置多选类型

		CharSequence[] entries = DeskSettingConstants.getSecondEffectsTextArray(entrisId, cutSize,
				mContext); //获取多选显示的内容
		CharSequence[] entryValues = DeskSettingConstants.getSecondEffectsTextArray(entryValuesId,
				cutSize, mContext); //获取多选显示内容的值
		String[] selectValues = DeskSettingConstants.intArray2StringArray(selectValue); //已勾选的值

		DeskSettingMultiInfo multiInfo = new DeskSettingMultiInfo(); //创建多选对象
		multiInfo.setEntries(entries);
		multiInfo.setEntryValues(entryValues);
		
		CharSequence[] textArrayRes = mContext.getResources().getTextArray(entrisId);
		if (customPosition < textArrayRes.length) {
			multiInfo.setTitle(textArrayRes[customPosition].toString()); //设置多选标题
		}
		multiInfo.setSelectValues(selectValues);

		if (imageId != null) {
			multiInfo.setImageId(getSecondImageId(imageId, cutSize));
			multiInfo.setIsAddImageBg(true); //合成添加背景图片
		}
		customDeskSettingInfo.setMultiInInfo(multiInfo);
		customDeskSettingInfo.setParentInfo(mDeskSettingInfo); //设置子类的父类为当前的mDeskSettingInfo

		mDeskSettingInfo.setSecondInfo(customDeskSettingInfo); //设置自定菜单
		mDeskSettingInfo.setCustomPosition(customPosition); //设置自定义的位置
	}

	/**
	 * <br>功能简述:设置二级菜单为多选
	 * <br>功能详细描述:
	 * <br>注意:需要设置父类引用
	 * @param entrisId 显示类容列表ID
	 * @param entryValuesId 显示内容值列表ID
	 * @param contentIndex 列表内容项的索引
	 * @param selectValue 勾选的值
	 * @param customPosition 自定义菜单在哪个位置
	 */
	public void setSecondInfoMulti(int entrisId, int entryValuesId, int[] imageId, int[] contentIndex,
			int[] selectValue, int customPosition) {
		DeskSettingInfo customDeskSettingInfo = new DeskSettingInfo();
		customDeskSettingInfo.setType(DialogTypeId.TYPE_DESK_SETTING_MULTICHOICE); //设置多选类型

		CharSequence[] entries = DeskSettingConstants.getSecondEffectsTextArray(entrisId, contentIndex,
				mContext); //获取多选显示的内容
		CharSequence[] entryValues = DeskSettingConstants.getSecondEffectsTextArray(entryValuesId,
				contentIndex, mContext); //获取多选显示内容的值
		String[] selectValues = DeskSettingConstants.intArray2StringArray(selectValue); //已勾选的值

		DeskSettingMultiInfo multiInfo = new DeskSettingMultiInfo(); //创建多选对象
		multiInfo.setEntries(entries);
		multiInfo.setEntryValues(entryValues);
		
		CharSequence[] textArrayRes = mContext.getResources().getTextArray(entrisId);
		if (customPosition < textArrayRes.length) {
			multiInfo.setTitle(textArrayRes[customPosition].toString()); //设置多选标题
		}
		multiInfo.setSelectValues(selectValues);

		if (imageId != null) {
			multiInfo.setImageId(getSecondImageId(imageId, contentIndex));
			multiInfo.setIsAddImageBg(true); //合成添加背景图片
		}
		customDeskSettingInfo.setMultiInInfo(multiInfo);
		customDeskSettingInfo.setParentInfo(mDeskSettingInfo); //设置子类的父类为当前的mDeskSettingInfo

		mDeskSettingInfo.setSecondInfo(customDeskSettingInfo); //设置自定菜单
		mDeskSettingInfo.setCustomPosition(customPosition); //设置自定义的位置
	}
	
	/**
	 * <br>功能简述:获取裁剪后的2级菜单图片列表
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param imageId	图片列表
	 * @param cutSize 需要裁剪的大小
	 * @return
	 */
	public int[] getSecondImageId(int[] imageId, int cutSize) {
		if (imageId != null) {
			int imageIdSize = imageId.length;
			if (imageIdSize > cutSize) {
				int secondImageIdSize = imageIdSize - cutSize;
				int[] secondImageId = new int[secondImageIdSize];
				for (int i = 0; i < secondImageIdSize; i++) {
					secondImageId[i] = imageId[i + cutSize];
				}
				return secondImageId;
			}
		}
		return null;
	}
	
	/**
	 * <br>功能简述:获取裁剪后的2级菜单图片列表
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param imageId	图片列表
	 * @param contentIndex 需要图片的项在imageId列表中的索引
	 * @return contentIndex对应索引的新图片列表，列表顺序为contentIndex中的顺序
	 */
	public int[] getSecondImageId(int[] imageId, int[] contentIndex) {
		if (imageId != null && contentIndex != null) {
			try {
				int[] secondImageId = new int[contentIndex.length];
				for (int i = 0; i < contentIndex.length; i++) {
					secondImageId[i] = imageId[contentIndex[i]];
				}
				return secondImageId;
			} catch (IndexOutOfBoundsException e) {
			}
		}
		return null;
	}
}

package com.jiubang.ggheart.apps.desks.Preferences.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.launcher.colorpicker.ColorPickerDialog;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DeskSettingFontScanDialog;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogTypeId;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingFontSingleInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSeekBarInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSeekBarItemInfo;
import com.jiubang.ggheart.apps.desks.settings.IFontScanPreferenceListener;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>
 * 类描述:个性化设置－字体tab view <br>
 * 功能详细描述:
 * 
 * @author ruxueqin
 * @date [2012-9-25]
 */
public class DeskSettingVisualFontTabView extends DeskSettingVisualAbsTabView
		implements IFontScanPreferenceListener {

	// views
	private DeskSettingItemBaseView mAppNameColor; // 程序名颜色设置
	private DeskSettingItemListView mAppNameSize; // 程序名大小
	private DeskSettingItemBaseView mScanFont; // 扫描字体
	private DeskSettingItemListView mFont; // 字体

	private ColorPickerDialog mColorPickerDialog; // 颜色选择器

	private DesktopSettingInfo mDesktopInfo; // 桌面设置信息

	private boolean mCustomTitleColor;
	private int mTitleColor;
	private boolean mIsSave;

	public static final int FONTSIZE_MAX = 30; // 字体最大值
	public static final int FONTSIZE_MIN = 10; // 字体最小值
	public static final int DEFAULT_FONT_SIZE = 0x1; // 默认字体大小
	public static final int DIY_FONT_SIZE = 0x2; // 自定义字体大小
	private int mOriginalSize; // 进入此界面时字体大小，用于判断退出时是否要更新字体大小

	private int mLastFontSizeType; // 修改前字体大小类型，用于设置不成功时回滚
	private HashMap<String, FontBean> mFontBeanMap;
	private ArrayList<FontBean> mFontBeans;

	public static final int MSG_FONT_SCAN_FINISH = 0x00000001; // 扫描字体框，用于通知扫描完成
	public static final int DIALOG_FINISH = 0x00000002;

	private boolean mFlag = true; //用于判断字体大小是否符合存储条件

	public DeskSettingVisualFontTabView(Context context, AttributeSet attrs) {
		super(context, attrs);

		GoSettingControler controler = GOLauncherApp.getSettingControler();
		mFontBeanMap = new HashMap<String, FontBean>();
		mFontBeans = controler.createFontBeans();
	}

	@Override
	protected void findView() {
		mAppNameColor = (DeskSettingItemBaseView) findViewById(R.id.appnamecolor);
		mAppNameColor.setOnClickListener(this);
		mAppNameSize = (DeskSettingItemListView) findViewById(R.id.appnamesize);
		mAppNameSize.setOnValueChangeListener(this);
		mScanFont = (DeskSettingItemBaseView) findViewById(R.id.scanfont);
		mScanFont.setOnClickListener(this);
		mFont = (DeskSettingItemListView) findViewById(R.id.showfont);
		mFont.setOnValueChangeListener(this);
	}

	@Override
	public void load() {
		mLastFontSizeType = mDesktopInfo.getFontSizeStyle();
		DeskSettingConstants.updateSingleChoiceListView(mAppNameSize,
				Integer.toString(mLastFontSizeType));

		if (mLastFontSizeType == 1) {
			// 默认
			mFlag = true;
		} else {
			// 自定义
			mFlag = false;
		}

		initCustomFontSize();
		mCustomTitleColor = mDesktopInfo.mCustomTitleColor;
		mTitleColor = mDesktopInfo.mTitleColor;
		loadFont();
	}

	@Override
	public void onClick(View v) {
		if (v == mAppNameColor) {
			if (mColorPickerDialog != null && mColorPickerDialog.isShowing()) {
				return;
			}
			mTitleColor = mDesktopInfo.mTitleColor;
			mColorPickerDialog = new ColorPickerDialog(getContext(),
					mTitleColorListener, mCustomTitleColor, mTitleColor,
					ColorPickerDialog.FONT, ColorPickerDialog.NOXY,
					ColorPickerDialog.NOXY);
			mColorPickerDialog.show();
		} else if (v == mScanFont) {
			DialogConfirm mScanFontConfirmDialog = new DialogConfirm(
					getContext());
			mScanFontConfirmDialog.show();
			mScanFontConfirmDialog.setTitle(getContext().getString(
					R.string.clearDefault_title));
			mScanFontConfirmDialog.setMessage(getContext().getString(
					R.string.choosefont_warning));
			mScanFontConfirmDialog.setPositiveButton(
					getContext().getString(R.string.foregound_yes),
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							DeskSettingFontScanDialog mDeskSettingFontScanDialog = new DeskSettingFontScanDialog(
									getContext());
							mDeskSettingFontScanDialog
									.setOnFontScanPreferenceListener(DeskSettingVisualFontTabView.this);
							mDeskSettingFontScanDialog.show();
						}
					});
		}

	}

	ColorPickerDialog.OnColorChangedListener mTitleColorListener = new ColorPickerDialog.OnColorChangedListener() {
		@Override
		public void colorChanged(int color) {
			mTitleColor = color;
		}

		@Override
		public void useCustom(boolean custom) {
			mCustomTitleColor = custom;
		}

		@Override
		public void colorIsSave(boolean isSave) {
			// TODO Auto-generated method stub
			mIsSave = isSave;
			if (mIsSave) {
				if (mDesktopInfo != null) {

					if (mDesktopInfo.mCustomTitleColor != mCustomTitleColor) {
						mDesktopInfo.mCustomTitleColor = mCustomTitleColor;
					}

					if (mDesktopInfo.mTitleColor != mTitleColor) {
						mDesktopInfo.mTitleColor = mTitleColor;
					}
					GOLauncherApp.getSettingControler()
							.updateDesktopSettingInfo(mDesktopInfo);
				}

			}
		}
	};

	@Override
	public void save() {
		boolean bChanged = false;
		if (mDesktopInfo != null) {
			int index = Integer.parseInt(mAppNameSize.getDeskSettingInfo()
					.getSingleInfo().getSelectValue());

			if (mFlag) {
				if (mDesktopInfo.getFontSizeStyle() != index
						|| (index == DIY_FONT_SIZE)) {
					if (index == DEFAULT_FONT_SIZE) {
						mDesktopInfo.setFontSize(0);
					}
					bChanged = true;
				}
			} else {
				if (mDesktopInfo.getFontSizeStyle() != index
						|| (index == DIY_FONT_SIZE && mDesktopInfo
								.getFontSize() != mOriginalSize)) {
					if (index == DEFAULT_FONT_SIZE) {
						mDesktopInfo.setFontSize(0);
					}
					bChanged = true;
				}
			}

			// if (mDesktopInfo.mCustomTitleColor != mCustomTitleColor) {
			// mDesktopInfo.mCustomTitleColor = mCustomTitleColor;
			// bChanged = true;
			// }
			//
			// if (mDesktopInfo.mTitleColor != mTitleColor) {
			// mDesktopInfo.mTitleColor = mTitleColor;
			// bChanged = true;
			// }
		}
		if (bChanged) {
			GOLauncherApp.getSettingControler().updateDesktopSettingInfo(
					mDesktopInfo);
		}

	}

	@Override
	public boolean onValueChange(DeskSettingItemBaseView baseView,
			Object newValue) {
		if (mAppNameSize == baseView) {
			if (newValue instanceof String) {
				DeskSettingConstants.updateSingleChoiceListView(mAppNameSize,
						(String) newValue);
			} else if (newValue instanceof String[]) {
				String select = mAppNameSize.getDeskSettingInfo()
						.getSingleInfo().getEntryValues()[1].toString();
				DeskSettingConstants.updateSingleChoiceListView(mAppNameSize,
						select);

				String valueStr = ((String[]) newValue)[0];
				int valueInt = Integer.valueOf(valueStr);

				mDesktopInfo.setFontSize(valueInt);
				mLastFontSizeType = DIY_FONT_SIZE;
			}
		}
		if (mFont == baseView) {
			if (newValue instanceof String) {
				DeskSettingConstants.updateSingleChoiceListView(mFont,
						(String) newValue);
				int selectInt = Integer.parseInt((String) newValue);
				GOLauncherApp.getSettingControler().updateUsedFontBean(
						mFontBeans.get(selectInt));
			}
		}
		return true;
	}

	/**
	 * <br>
	 * 功能简述:载入字体选项集 <br>
	 * 功能详细描述: <br>
	 * 注意:调用时机:1:在刚进入此界面,从db读取;2:扫描完字体，从字体扫描框触发
	 */
	private void loadFont() {
		GoSettingControler control = GOLauncherApp.getSettingControler();
		if (null != control) {

			String[][] fonts = getFonts();
			DeskSettingFontSingleInfo deskSettingFontSingleInfo = (DeskSettingFontSingleInfo) mFont
					.getDeskSettingInfo().getSingleInfo();

			deskSettingFontSingleInfo.setEntries(fonts[0]);
			deskSettingFontSingleInfo.setEntryValues(fonts[1]);
			boolean bUsedExsit = false;
			FontBean used = control.getUsedFontBean();
			// 找出已选择的选项
			for (int i = 0; i < mFontBeans.size(); i++) {
				FontBean bean = mFontBeans.get(i);
				if (null == bean) {
					continue;
				}
				if (bean.equals(used)) {
					DeskSettingConstants.updateSingleChoiceListView(mFont,
							Integer.valueOf(i).toString());
					mFont.setSummaryText(deskSettingFontSingleInfo.getEntries()[i]);
					bUsedExsit = true;
					break;
				}
			}
			// 如果没有选择则选中第一项
			if (!bUsedExsit) {
				DeskSettingConstants.updateSingleChoiceListView(mFont, Integer
						.valueOf(0).toString());
				mFont.setSummaryText(deskSettingFontSingleInfo.getEntries()[0]);
			}
			deskSettingFontSingleInfo.setmFontBeanMap(mFontBeanMap);
		}
	}

	private String[][] getFonts() {
		mFontBeanMap.clear();
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> files = new ArrayList<String>();
		int sz = mFontBeans.size();

		for (int i = 0; i < sz; i++) {
			FontBean bean = mFontBeans.get(i);
			if (null != bean.mFileName) {
				StringBuilder builder = new StringBuilder();
				if (FontBean.FONTFILETYPE_SYSTEM == bean.mFontFileType) {
					builder.append(bean.mFileName).append(" [")
							.append(bean.mApplicationName).append("]");
				} else {
					builder.append(getFileName(bean.mFileName)).append(" [")
							.append(bean.mApplicationName).append("]");
				}
				names.add(builder.toString());
				mFontBeanMap.put(builder.toString(), bean);
				files.add(bean.mFileName);
			}
		}

		int fSz = files.size();
		if (fSz > 0) {
			String[][] ret = new String[2][fSz];
			for (int i = 0; i < fSz; i++) {
				ret[0][i] = names.get(i);
				ret[1][i] = Integer.valueOf(i).toString();
			}
			return ret;
		}
		return null;
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (null == msg) {
				return;
			}
			if (MSG_FONT_SCAN_FINISH == msg.what) {
				mFontBeans = (ArrayList<FontBean>) msg.obj;
				loadFont();
			}
		};
	};

	private String getFileName(String fileName) {
		if (null == fileName) {
			return null;
		}
		int index = fileName.indexOf("/");
		while (-1 != index) {
			fileName = fileName.substring(index + 1);
			index = fileName.indexOf("/");
		}
		index = fileName.indexOf(".ttf");
		if (-1 != index) {
			fileName = fileName.substring(0, index);
		}
		return fileName;
	}

	@Override
	public void onFontScanChanged(ArrayList<FontBean> beans) {
		if (null == beans) {
			return;
		}

		// 写入数据库
		GOLauncherApp.getSettingControler().updateFontBeans(beans);

		Message msg = new Message();
		msg.obj = beans;
		msg.what = MSG_FONT_SCAN_FINISH;
		mHandler.handleMessage(msg);
	}

	/**
	 * <br>
	 * 功能简述:初始化自定义字体大小调节条 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void initCustomFontSize() {
		DeskSettingSeekBarItemInfo sizeSeekBarItemInfo = new DeskSettingSeekBarItemInfo();
		sizeSeekBarItemInfo.setTitle(getResources().getString(
				R.string.font_size_setting_dialog_title)); // 设置标题
		sizeSeekBarItemInfo.setMinValue(FONTSIZE_MIN);
		sizeSeekBarItemInfo.setMaxValue(FONTSIZE_MAX);
		mOriginalSize = mDesktopInfo.getFontSize();
		sizeSeekBarItemInfo.setSelectValue(mDesktopInfo.getFontSize());

		ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfos = new ArrayList<DeskSettingSeekBarItemInfo>();
		seekBarItemInfos.add(sizeSeekBarItemInfo);

		// seekBarInfo
		DeskSettingSeekBarInfo seekBarInfo = new DeskSettingSeekBarInfo();
		seekBarInfo.setSeekBarItemInfos(seekBarItemInfos);
		seekBarInfo.setTitle(getResources().getString(
				R.string.desk_setting_visual_tab_font_size_title));

		DeskSettingInfo customDeskSettingInfo = new DeskSettingInfo();
		customDeskSettingInfo.setSeekBarInfo(seekBarInfo);
		customDeskSettingInfo.setType(DialogTypeId.TYPE_DESK_SETTING_SEEKBAR); // 设置seekbar类型

		mAppNameSize.getDeskSettingInfo().setSecondInfo(customDeskSettingInfo);
		mAppNameSize.getDeskSettingInfo()
				.setCustomPosition(
						mAppNameSize.getDeskSettingInfo().getSingleInfo()
								.getEntries().length - 1); // 设置自定义的位置
	}

	@Override
	public void changeOrientation() {
		// 横竖屏切换就关闭掉颜色选择器然后重新打开
		if (mColorPickerDialog != null && mColorPickerDialog.isShowing()) {

			float x = mColorPickerDialog.getmPointX();
			float y = mColorPickerDialog.getmPointY();

			mTitleColor = mColorPickerDialog.getPickerViewColor();
			mColorPickerDialog.dismiss();
			mColorPickerDialog = null;
			mColorPickerDialog = new ColorPickerDialog(getContext(),
					mTitleColorListener, mCustomTitleColor, mTitleColor,
					ColorPickerDialog.FONT, x, y);
			mColorPickerDialog.show();
		}
	}

	public void setInfo(DesktopSettingInfo desktopInfo) {
		mDesktopInfo = desktopInfo;
	}
}
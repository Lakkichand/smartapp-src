package com.jiubang.ggheart.apps.appfunc.component;

import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.jiubang.core.mars.MImage;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 应用程序名称控件
 * 
 * @author tanshu
 * 
 */
public class AppText extends XComponent implements BroadCasterObserver {
	private Activity mActivity;
	// 控件名称
	private String mName;
	// 控件绘制的名称
	private String mDrawName_1;
	// 控件绘制的名称
	private String mDrawName_2;
	// 背景图
	private MImage mBg;
	// 画笔
	private Paint mPaint;
	// 文字宽度
	private int mTextWidth_1;
	// 文字宽度
	private int mTextWidth_2;
	// 文字高度
	private int mTextHeight;
	// 是否在行列数为稀疏或者适中时显示两行名称
	private boolean mShowTwoLines;
	// 是否需要重绘
	private boolean mIsRePaint;
	// private double mfactor;
	/**
	 * 时候绘制文字
	 */
	private boolean mIsDrawText = true;

	private AppFuncUtils mUtils;

	private boolean mCutText = false;

	public AppText(Activity activity, int tickCount, int x, int y, int width, int height,
			int txtHeight, String name, boolean showTwoLines) {
		super(tickCount, x, y, width, height);
		mActivity = activity;
		mUtils = AppFuncUtils.getInstance(activity);
		if (name != null) {
			mName = name.trim();
		} else {
			mName = "";
		}
		mTextHeight = height;
		// mfactor =
		// (double)AppFuncUtils.getInstance(mActivity).getSmallerBound() / 480;
		mPaint = new Paint();
		mPaint.setColor(BaseAppIcon.sTxtColor);
		mPaint.setTextSize(txtHeight);
		mPaint.setAntiAlias(true);
		mShowTwoLines = showTwoLines;
		// 计算显示的字符串
		calculateDrawName();
	}

	@Override
	protected void onShow() {
		super.onShow();
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		controler.registerObserver(this);
		initTypeface(controler.getUsedFontBean().mFontTypeface,
				controler.getUsedFontBean().mFontStyle);
	}

	@Override
	protected void onHide() {
		super.onHide();
		GOLauncherApp.getSettingControler().unRegisterObserver(this);
	}

	private void initTypeface(Typeface typeface, int style) {
		setTypeface(typeface, style);
	}

	public void setTypeface(Typeface tf, int style) {
		if (style > 0) {
			if (tf == null) {
				tf = Typeface.defaultFromStyle(style);
			} else {
				tf = Typeface.create(tf, style);
			}

			setTypeface(tf);
			// now compute what (if any) algorithmic styling is needed
			int typefaceStyle = tf != null ? tf.getStyle() : 0;
			int need = style & ~typefaceStyle;
			mPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
			mPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
		} else {
			mPaint.setFakeBoldText(false);
			mPaint.setTextSkewX(0);
			setTypeface(tf);
		}
		calculateDrawName();
	}

	public void setTypeface(Typeface tf) {
		if (mPaint.getTypeface() != tf) {
			mPaint.setTypeface(tf);
			calculateDrawName();
		}

	}

	public void setTextSize(int textSize) {
		if (mPaint != null) {
			mPaint.setTextSize(textSize);
		}
	}

	@Override
	public void setSize(int width, int height) {

		boolean reCalculate = false;;
		if ((width != mWidth) || (height != mHeight)) {
			reCalculate = true;
		}
		super.setSize(width, height);
		if (reCalculate) {
			calculateDrawName();
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mIsDrawText) {
			// if (AppFuncFrame.getDataHandler() == null) {
			// return;
			// }
			// int standard = AppFuncFrame.getDataHandler().getStandard();
			if (mDrawName_1 != null) {
				if (!mShowTwoLines) {
					canvas.drawText(mDrawName_1, (mWidth - mTextWidth_1) / 2,
							mHeight - mUtils.getStandardSize(6), mPaint);
				} else {
					canvas.drawText(mDrawName_1, (mWidth - mTextWidth_1) / 2,
							mHeight / 2 - mUtils.getStandardSize(4), mPaint);
				}
			}
			if (mDrawName_2 != null) {
				if (mShowTwoLines) {
					canvas.drawText(mDrawName_2, (mWidth - mTextWidth_2) / 2,
							mHeight - mUtils.getStandardSize(6), mPaint);
				}
			}
		}

		// Log.e("AppText", mPaint.getAlpha() + "");
	}

	@Override
	protected boolean animate() {
		if (mIsRePaint) {
			mIsRePaint = false;
			return true;
		}
		return false;
	}

	public void setNameTxt(String name) {
		if (name != null) {
			if ((mName != null) && (mName.compareTo(name.trim()) == 0)) {
				return;
			}
			mName = name.trim();
		} else {
			mName = "";
		}
		calculateDrawName();
		mIsRePaint = true;
	}

	/**
	 * 计算需要显示的名字
	 */
	private void calculateDrawName() {
		// 清空缓存
		invalidate();
		mDrawName_1 = null;
		mDrawName_2 = null;
		if (mName == null || AppFuncFrame.getDataHandler() == null) {
			return;
		}
		mCutText = false;
		// 字体显示行数特殊处理
		// boolean showTwoLines;
		// int smallerBound = mUtils.getSmallerBound();
		// int standard = AppFuncFrame.getDataHandler().getStandard();
		// if(smallerBound <= 320){
		// if ((standard == FunAppSetting.LINECOLUMNNUMXY_SPARSE)
		// && mShowTwoLines == true){
		// showTwoLines = true;
		// }else{
		// showTwoLines = false;
		// }
		// showTwoLines = false;
		// }else{
		// FunAppSetting setting =
		// GOLauncherApp.getSettingControler().getFunAppSetting();
		// if (setting.getRowNum() >= 5
		// || mShowTwoLines == false){
		// showTwoLines = false;
		// }else{
		// showTwoLines = true;
		// }
		// }
		if (!mShowTwoLines) {
			if ((int) mPaint.measureText(mName) > mWidth) {
				int length;
				for (int i = 1; i <= mName.length(); i++) {
					length = (int) (mPaint.measureText(mName.substring(0, i)));
					if (length > mWidth) {
						mCutText = true;
						mDrawName_1 = mName.substring(0, i - 1);
						break;
					}
				}

				if (mDrawName_1 == null) {
					mDrawName_1 = mName;
				}

			} else {
				mDrawName_1 = mName;
			}
		} else {
			setShowStr();
		}
		if (mDrawName_1 != null) {
			mTextWidth_1 = (int) mPaint.measureText(mDrawName_1);
		}
		if (mDrawName_2 != null) {
			mTextWidth_2 = (int) mPaint.measureText(mDrawName_2);
		}
	}

	public void setPaintAlpha(int alpha) {
		if (mPaint != null && mPaint.getAlpha() != alpha) {
			mPaint.setAlpha(alpha);
			invalidate();
		}
		// setDrawingCacheAlpha(alpha);
	}

	public int getPaintAlpha() {
		int alpha = 255;
		if (mPaint != null) {
			mPaint.getAlpha();
		}
		return alpha;
	}

	public void setPaintColor(int color) {
		if (mPaint != null) {
			mPaint.setColor(color);
		}
	}

	private void setShowStr() {
		String[] strList = mName.split(" ");
		int strLength = 0;
		int strIndex = 0;
		int charIndex = 0;
		int length;
		// 构造第一行的字符串
		for (int i = 0; i < strList.length; i++) {
			if (i == 0) {
				strLength = (int) mPaint.measureText(strList[0]);
			} else {
				strLength += mPaint.measureText(mDrawName_1 + " " + strList[i]);
			}

			if (strLength <= mWidth) {
				if (i == 0) {
					mDrawName_1 = strList[i];
				} else {
					mDrawName_1 = mDrawName_1 + " " + strList[i];
					strIndex = i;
				}
			} else {
				// 第一个字符串做截断处理
				if (i == 0) {
					for (int x = 1; x <= strList[0].length(); x++) {
						length = (int) (mPaint.measureText(strList[0].substring(0, x)));
						if (length > mWidth) {
							mDrawName_1 = strList[0].substring(0, x - 1);
							charIndex = x - 1;
							break;
						}
					}
				}
				boolean flag = false;
				if (Pattern
						.matches(
								"[^[\\\\\\/\\:\\*\\?\\\"\\<\\>\\|\\.]]+[.][^[\\\\\\/\\:\\*\\?\\\"\\<\\>\\|\\.]]+",
								strList[0])) {
					// 带扩展名的文件名，扩展名截断放在第二行显示
					int dotIndex = strList[0].lastIndexOf(".");
					if (dotIndex > 0) {
						if (charIndex > dotIndex) {
							mDrawName_1 = strList[0].substring(0, dotIndex);
							charIndex = dotIndex;
							flag = true;
						}
					}
				}
				if (!flag) {
					// 第一个字符串如果超出一行一个或两个字符(中文的话忽略)的话，不再显示截断字符
					if (charIndex == (strList[0].length() - 1)) {
						if (!isWiderThanChinese(strList[0].substring(charIndex))) {
							charIndex = 0;
						}
					}

					if (charIndex == (strList[0].length() - 2)) {
						// 最后的两个字符
						String st1 = strList[0].substring(charIndex, charIndex + 1);
						String st2 = strList[0].substring(charIndex + 1);
						if (!!isWiderThanChinese(st1) && !isWiderThanChinese(st2)) {
							charIndex = 0;
						}
					}
				}

				break;

			}
		}
		// 构造第二行的字符串
		if (charIndex != 0) {
			// 第一个字符串剩余字符小于控件宽度
			if (mPaint.measureText(strList[0].substring(charIndex)) <= mWidth) {
				mDrawName_2 = strList[0].substring(charIndex);
				// 从第二个字符串开始继续轮询
				for (int i = 1; i < strList.length; i++) {
					if (mPaint.measureText(mDrawName_2 + " " + strList[i]) <= mWidth) {
						mDrawName_2 = mDrawName_2 + " " + strList[i];
					} else {
						mCutText = true;
						for (int x = 1; x <= strList[i].length(); x++) {
							length = (int) (mPaint.measureText(mDrawName_2 + " "
									+ strList[i].substring(0, x)));
							if (length > mWidth) {
								if (x > 1) {
									mDrawName_2 = mDrawName_2 + " "
											+ strList[i].substring(0, x - 1);
								}
								return;
							}
						}
					}
				}
			} else {
				// 第一个字符串剩余字符大于控件宽度，继续截断
				String leftStr = strList[0].substring(charIndex);
				for (int x = 1; x <= leftStr.length(); x++) {
					length = (int) (mPaint.measureText(leftStr.substring(0, x)));
					if (length > mWidth) {
						mCutText = true;
						mDrawName_2 = leftStr.substring(0, x - 1);
						return;
					}
				}
			}
		} else {
			for (int i = strIndex + 1; i < strList.length; i++) {
				if (i == strIndex + 1) {
					strLength = (int) mPaint.measureText(strList[i]);
				} else {
					strLength += mPaint.measureText(mDrawName_2 + " " + strList[i]);
				}
				if (strLength <= mWidth) {
					if (i == strIndex + 1) {
						mDrawName_2 = strList[i];
					} else {
						mDrawName_2 = mDrawName_2 + " " + strList[i];
					}
				} else {
					mCutText = true;
					for (int x = 1; x <= strList[i].length(); x++) {
						if (mDrawName_2 == null) {
							length = (int) (mPaint.measureText(strList[i].substring(0, x)));
						} else {
							length = (int) (mPaint.measureText(mDrawName_2 + " "
									+ strList[i].substring(0, x)));
						}
						if (length > mWidth) {
							if (x > 1) {
								if (mDrawName_2 == null) {
									mDrawName_2 = strList[i].substring(0, x - 1);
								} else {
									mDrawName_2 = mDrawName_2 + " "
											+ strList[i].substring(0, x - 1);
								}
							}
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
	}

	/* 判断该字符的长度是否大于等于中文的长度，如果大于等于的话返回true */
	private boolean isWiderThanChinese(String c) {
		// 给出标准的样例
		String a = "A";
		String b = "文";

		// int aL
		int wide = (int) mPaint.measureText(c);

		if (wide <= (int) mPaint.measureText(a)) {
			return false;
		}

		if (wide >= (int) mPaint.measureText(b)) {
			return true;
		}

		return false;
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case IDiyMsgIds.APPCORE_DATACHANGE :
				if (DataType.DATATYPE_DESKFONTCHANGED == param) {
					if (object instanceof FontBean) {
						FontBean bean = (FontBean) object;
						initTypeface(bean.mFontTypeface, bean.mFontStyle);
					}
				}
				break;
			default :
				break;
		}
	}

	public void setIsDrawText(boolean draw) {
		mIsDrawText = draw;
	}

	public boolean getIsDrawText() {
		return mIsDrawText;
	}

	public void setShowTwoLines(boolean showTwoLines) {
		mShowTwoLines = showTwoLines;
		calculateDrawName();
	}

	public boolean getCutText() {
		return mCutText;
	}
}

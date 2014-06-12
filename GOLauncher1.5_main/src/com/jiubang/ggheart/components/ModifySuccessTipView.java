package com.jiubang.ggheart.components;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
/**
 * 
 * <br>类描述:修改成功提示控件
 * <br>功能详细描述:
 * 
 * @author  wuziyi
 * @date  [2012-10-16]
 */
public class ModifySuccessTipView extends LinearLayout {

	private Context mContext;
	private LayoutInflater mInflater;
	private ImageView mSuccessImage;
	private TextView mSuccessTip;
	private TextView mSuccessTitle;
	private String mTextString;
	private String mHightLightText;
	private boolean mIsBold;
	private int mColor;
	private static final int DEFAULT = -1;

	public ModifySuccessTipView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.modify_success_tip, this);
	}

	public ModifySuccessTipView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mColor = DEFAULT;
		mInflater = LayoutInflater.from(mContext);
		mInflater.inflate(R.layout.modify_success_tip, this);
		mSuccessImage = (ImageView) findViewById(R.id.modify_success_image);
		mSuccessTip = (TextView) findViewById(R.id.modify_success_text);
		mSuccessTitle = (TextView) findViewById(R.id.modify_success_title);
	}

	/**
	 * <br>功能简述:设置资源
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param imageID 图片资源
	 * @param textID 文字资源
	 */
	public void setResource(int imageID, int textID, int titleID) {
		mSuccessImage.setImageResource(imageID);
		mTextString = mContext.getString(textID);
		mSuccessTip.setText(mTextString);
		mSuccessTitle.setText(titleID);
	}

	/**
	 * <br>
	 * 功能简述:设置字符串高亮部分 <br>
	 * 功能详细描述: 传入参数为高亮部分<br>
	 * 注意:参数为null或者不匹配的情况下无效，默认高亮颜色绿色
	 * 
	 * @param hightLightString 高亮部分文字
	 */
	public void setHightLightText(String hightLightString) {
		if (hightLightString == null) {
			return;
		}
		hightLightString = hightLightString.trim();
		if (hightLightString.equals("")) {
			return;
		}
		mHightLightText = hightLightString;
		SpannableStringBuilder style = new SpannableStringBuilder(mTextString);
		int start = mTextString.indexOf(mHightLightText);
		if (start != -1) {
			if (mColor == DEFAULT) {
				style.setSpan(new ForegroundColorSpan(0xff3bbf1c), start, start + mHightLightText.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				//粗体
//				StyleSpan sp = new StyleSpan(android.graphics.Typeface.BOLD_ITALIC);
//				style.setSpan(sp, start, start + mHightLightText.length(),
//						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			} else {
				ForegroundColorSpan textColor = new ForegroundColorSpan(mColor);
				style.setSpan(textColor, start, start + mHightLightText.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			mSuccessTip.setText(style);
		}
	}
	
	/**
	 * <br>功能简述:设置高亮字体部分的颜色
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param color 高亮颜色
	 */
	public void setHightLightColor(int color) {
		mColor = color;
		if (mHightLightText == null || mHightLightText.equals("")) {
			return;
		}
		SpannableStringBuilder style = new SpannableStringBuilder(mTextString);
		int start = mTextString.indexOf(mHightLightText);
		if (start != -1) {
			ForegroundColorSpan textColor = new ForegroundColorSpan(mColor);
			style.setSpan(textColor, start, start + mHightLightText.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	/**
	 * <br>功能简述:设置高亮字体部分是否粗体
	 * <br>功能详细描述:
	 * <br>注意:对中文无效
	 * @param isBoldText 是否粗体
	 */
	public void setHightLightBold(boolean isBoldText) {
		mIsBold = isBoldText;
		if (mHightLightText == null || mHightLightText.equals("")) {
			return;
		}
		SpannableStringBuilder style = new SpannableStringBuilder(mTextString);
		int start = mTextString.indexOf(mHightLightText);
		if (start != -1) {
			StyleSpan sp;
			if (mIsBold) {
				sp = new StyleSpan(android.graphics.Typeface.BOLD);
			} else {
				sp = new StyleSpan(android.graphics.Typeface.NORMAL);
			}
			style.setSpan(sp, start, start + mHightLightText.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
}

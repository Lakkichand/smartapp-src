package com.jiubang.ggheart.apps.desks.Preferences.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;

/**
 * 
 * <br>类描述:每项控件的基础View
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-13]
 */
public class DeskSettingItemBaseView extends RelativeLayout implements OnClickListener {
	private Context mContext;
	private Intent mIntent; //执行的Intent
	protected View mBaseView; //布局View
	private TextView mTitleTextView; //主要内容
	private TextView mSummagyTextView; //summary内容
	private ImageView mBottomLine; //底部线条

	public DeskSettingItemBaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeskSettingItemView);

		//显示图片
		Drawable imageDrawable = a.getDrawable(R.styleable.DeskSettingItemView_image);

		//标题内容
		CharSequence titleText = a.getText(R.styleable.DeskSettingItemView_titleText);

		//标题颜色
		int titleTextColor = a.getColor(R.styleable.DeskSettingItemView_titleTextColor,
				getResources().getColor(R.color.desk_setting_item_title_color));

		//标题字体大小，获取的字体大小为PX
		float titleTextSize = a.getDimension(R.styleable.DeskSettingItemView_titleTextSize, context
				.getResources().getDimension(R.dimen.desk_setting_item_title_text_default_size));

		//注解内容
		CharSequence summaryText = a.getText(R.styleable.DeskSettingItemView_summaryText);

		//注解字体颜色
		int summaryTextColor = a.getColor(R.styleable.DeskSettingItemView_summaryTextColor, context
				.getResources().getColor(R.color.desk_setting_item_summary_color));

		//注解字体大小
		float summaryTextSize = a.getDimension(
				R.styleable.DeskSettingItemView_summaryTextSize,
				context.getResources().getDimension(
						R.dimen.desk_setting_item_summary_text_default_size));

		//是否隐藏底部白线
		Boolean isHiddenBottomLine = a.getBoolean(
				R.styleable.DeskSettingItemView_isHiddenBottomLine, false);

		a.recycle();

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBaseView = inflater.inflate(R.layout.desk_setting_item_base_view, this);
		mBaseView.setBackgroundDrawable(context.getResources().getDrawable(
				R.drawable.change_icon_tab_selector));

		//图片
		ImageView imageView = (ImageView) mBaseView.findViewById(R.id.image);
		imageView.setImageDrawable(imageDrawable);

		//内容
		mTitleTextView = (TextView) mBaseView.findViewById(R.id.title);
		if (titleText != null) {
			mTitleTextView.setText(titleText);
		}
		mTitleTextView.setTextSize(DrawUtils.px2sp(titleTextSize)); //需要把PX转化成SP
		mTitleTextView.setTextColor(titleTextColor);

		//summary
		mSummagyTextView = (TextView) mBaseView.findViewById(R.id.summary);
		mSummagyTextView.setTextSize(DrawUtils.px2sp(summaryTextSize)); //需要把PX转化成SP
		mSummagyTextView.setTextColor(summaryTextColor);
		if (summaryText != null && !summaryText.equals("")) {
			mSummagyTextView.setVisibility(View.VISIBLE);
			mSummagyTextView.setText(summaryText);
		} else {
			mSummagyTextView.setVisibility(View.GONE);
		}

		//是否隐藏底部分割线
		if (isHiddenBottomLine) {
			mBottomLine = (ImageView) mBaseView.findViewById(R.id.bottomLine);
			mBottomLine.setVisibility(View.INVISIBLE);
		}

		setOnClickListener(this);
		
		//支持独立语言包
		if (null != DeskResourcesConfiguration.getInstance()) {
			DeskResourcesConfiguration.getInstance().configurationDeskSettingItemBaseView(this, attrs);
		}
	}

	@Override
	public void onClick(View v) {
		if (mIntent != null) {
			mContext.startActivity(mIntent);
		}
	}

	/**
	 * <br>功能简述:设置需要打开的Intent
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param intent
	 */
	public void setOpenIntent(Intent intent) {
		mIntent = intent;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			setTitleColor(R.color.desk_setting_item_title_color);
		} else {
			setTitleColor(R.color.desk_setting_item_summary_color); //设置title颜色变灰
		}
	}

	/**
	 * <br>功能简述:设置底部线条是否显示
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param visible
	 */
	public void setBottomLineVisible(int visible) {
		if (mBottomLine != null) {
			mBottomLine.setVisibility(visible);
		}
	}

	/**
	 * <br>功能简述:设置标题颜色
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param color
	 */
	public void setTitleColor(int color) {
		if (mTitleTextView != null) {
			mTitleTextView.setTextColor(mContext.getResources().getColor(color));
		}
	}

	/**
	 * <br>功能简述:设置summary颜色
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param color
	 */
	public void setSummaryColor(int color) {
		if (mSummagyTextView != null) {
			mSummagyTextView.setTextColor(mContext.getResources().getColor(color));
		}
	}

	/**
	 * <br>功能简述:设置标题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param resId 资源ID
	 */
	public void setTitleText(int resId) {
		setTitleText(mContext.getString(resId));
	}

	/**
	 * <br>功能简述:设置标题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param summaryString 显示string
	 */
	public void setTitleText(CharSequence titleString) {
		if (mTitleTextView != null && !titleString.equals("")) {
			mTitleTextView.setText(titleString);
		}
	}

	/**
	 * <br>功能简述:设置简介的显示
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param resId 资源ID
	 */
	public void setSummaryText(int resId) {
		setSummaryText(mContext.getString(resId));
	}

	/**
	 * <br>功能简述:设置简介的显示
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param summaryString 显示string
	 */
	public void setSummaryText(CharSequence summaryString) {
		if ((summaryString == null && mSummagyTextView != null)
				|| (summaryString != null && !summaryString.equals(mSummagyTextView.getText()))) {
			mSummagyTextView.setText(summaryString);
			int visibility = summaryString == null ? View.GONE : View.VISIBLE;
			mSummagyTextView.setVisibility(visibility);
		}
	}

	/**
	 * <br>功能简述:设置summary可见性
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param enable
	 */
	public void setSummaryEnabled(boolean enable) {
		if (enable) {
			mSummagyTextView.setVisibility(View.VISIBLE);
		} else {
			mSummagyTextView.setVisibility(View.GONE);
		}
	}
}

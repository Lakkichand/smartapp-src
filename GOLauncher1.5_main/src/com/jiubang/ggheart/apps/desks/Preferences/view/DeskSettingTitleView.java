package com.jiubang.ggheart.apps.desks.Preferences.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author licanhui
 * @date [2012-9-10]
 */
public class DeskSettingTitleView extends RelativeLayout {
	public DeskSettingTitleView(Context context) {
		super(context);
	}

	public DeskSettingTitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeskSettingItemView);
	
		//标题字体内容
		CharSequence titleText = a.getText(R.styleable.DeskSettingItemView_titleText);

		//标题字体大小
		float titleTextSize = a.getDimension(R.styleable.DeskSettingItemView_titleTextSize, context
			.getResources().getDimension(R.dimen.desk_setting_title_text_default_size));
		
		a.recycle();
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.desk_setting_title_view, this);

		TextView titleTextView = (TextView) view.findViewById(R.id.title_name);
		titleTextView.setText(titleText);
		titleTextView.setTextSize(DrawUtils.px2sp(titleTextSize));

	}
}

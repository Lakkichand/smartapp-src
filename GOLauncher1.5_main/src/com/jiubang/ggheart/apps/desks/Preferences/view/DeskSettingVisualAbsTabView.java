package com.jiubang.ggheart.apps.desks.Preferences.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.jiubang.ggheart.apps.desks.Preferences.OnValueChangeListener;

/**
 * 
 * <br>类描述:个性化设置tab基类
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-9-13]
 */
public class DeskSettingVisualAbsTabView extends LinearLayout
		implements
			OnClickListener,
			OnValueChangeListener {
	
	public DeskSettingVisualAbsTabView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public DeskSettingVisualAbsTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		findView();
	}
	
	protected void findView() {
		
	}

	public void load() {

	}

	public void save() {

	}

	@Override
	public boolean onValueChange(DeskSettingItemBaseView baseView, Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	public void changeOrientation() {
		
	}
	
	public void onResume() {
		
	}
}

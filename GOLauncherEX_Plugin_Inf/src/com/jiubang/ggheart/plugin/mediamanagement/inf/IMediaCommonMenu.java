package com.jiubang.ggheart.plugin.mediamanagement.inf;

import android.view.View;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * <br>类描述: 多媒体公共菜单接口
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-23]
 */
public interface IMediaCommonMenu {
	
	public void show(View parent, int x, int y, int width, int height, String[] itemNames,
			OnItemClickListener listener);
	
	public void showByVerticalAnimation(View parent, int x, int y, int width,
			int height, String[] itemNames, OnItemClickListener listener);

	public boolean isShowing();

	public void dismiss();
}

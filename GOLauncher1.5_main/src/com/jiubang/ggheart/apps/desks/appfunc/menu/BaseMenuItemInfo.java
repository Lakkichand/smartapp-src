package com.jiubang.ggheart.apps.desks.appfunc.menu;

import android.graphics.drawable.Drawable;

/**
 * 
 * <br>类描述: 列表菜单项信息
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-23]
 */
public class BaseMenuItemInfo {
	public final static int DRAWABLE_POS_NONE = -1;
	public final static int DRAWABLE_POS_LEFT = 0;
	public final static int DRAWABLE_POS_TOP = 1;
	public final static int DRAWABLE_POS_RIGHT = 2;
	public final static int DRAWABLE_POS_BOTTOM = 3;

	public int mDrawablePos = DRAWABLE_POS_NONE;
	public int mDrawableId = -1;
	public int mTextId = -1;
	public int mActionId;
	
	public Drawable mDrawable;
	public String mText;
}

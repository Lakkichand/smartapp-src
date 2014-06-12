package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.unfit;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;


/**
 * 
 * <br>类描述:dock非自适应模式，+号资源管理器
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-10-16]
 */
public interface IAddIconResHandler {

	/**
	 * 获取+号图片
	 * 
	 * @return
	 */
	public abstract Drawable getAddDrawable();

	/**
	 * 获取点击发光图片
	 * 
	 * @return
	 */
	public abstract Drawable getLightDrawable();

	/**
	 * 获取空白显示位置信息　
	 * 
	 * @param lineID
	 *            　第几行，以在数据库在存储的行数为准
	 * @return
	 */
	public abstract ArrayList<Integer> getBlanks(int lineID);
}

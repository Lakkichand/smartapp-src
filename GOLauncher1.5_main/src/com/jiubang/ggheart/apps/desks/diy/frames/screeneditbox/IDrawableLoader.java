package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 
 * 类描述:Drawable 加载的接口 功能详细描述:
 * 
 * @author guoyiqing
 * @date [2012-8-9]
 */
public interface IDrawableLoader {
				/**
				 * 异步方法load图
				 * @param position
				 * @param arg
				 * @return
				 */
	public Drawable loadDrawable(int position, Object arg);
				/**
				 * 加载完成后显示图片
				 * @param view
				 * @param drawable
				 */
	public void displayResult(View view, Drawable drawable);
}

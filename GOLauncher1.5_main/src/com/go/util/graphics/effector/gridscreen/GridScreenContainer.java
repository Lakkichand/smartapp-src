package com.go.util.graphics.effector.gridscreen;

import android.graphics.Canvas;

/**
 * 网格分屏的容器
 * 
 * @author dengweiming
 * 
 */
public interface GridScreenContainer {

	/**
	 * 绘制单元格
	 * 
	 * @param canvas
	 * @param index
	 *            单元格的索引
	 */
	void drawGridCell(Canvas canvas, int index);

	/**
	 * 绘制半透明的单元格
	 * 
	 * @param canvas
	 * @param index
	 *            单元格的索引
	 * @param alpha
	 *            不透明度[0, 255]
	 */
	void drawGridCell(Canvas canvas, int index, int alpha);

	/**
	 * 获取分屏的宽度
	 * 
	 * @return
	 */
	int getWidth();

	/**
	 * 获取分屏的高度
	 * 
	 * @return
	 */
	int getHeight();

	/**
	 * 获取单元格的数目
	 * 
	 * @return
	 */
	int getCellCount();

	/**
	 * 获取单元格的宽度
	 * 
	 * @return
	 */
	int getCellWidth();

	/**
	 * 获取单元格的高度
	 * 
	 * @return
	 */
	int getCellHeight();

	/**
	 * 获取分屏里面单元格的行数
	 * 
	 * @return
	 */
	int getCellRow();

	/**
	 * 获取分屏里面单元格的列数
	 * 
	 * @return
	 */
	int getCellCol();

	/**
	 * 获取分屏里面的左空白
	 * 
	 * @return
	 */
	int getPaddingLeft();

	/**
	 * 获取分屏里面的右空白
	 * 
	 * @return
	 */
	int getPaddingRight();

	/**
	 * 获取分屏里面的上空白
	 * 
	 * @return
	 */
	int getPaddingTop();

	/**
	 * 获取分屏里面的下空白
	 * 
	 * @return
	 */
	int getPaddingBottom();

}
package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderGridView.FolderTargetPosition;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderGridView.FolderdragItem;

public interface IFolderCellGridViewListner {
	/**
	 * 列数
	 * 
	 * @return
	 */
	public int getNumColumns();

	/**
	 * 一页最多放几个
	 * 
	 * @return
	 */
	public int getItemsCountPerScreen();

	/**
	 * 回调换位结束
	 */
	public void onReplaceFinish();

	/**
	 * 获取拖动数据
	 * 
	 * @return
	 */
	public FolderdragItem getDragItemData();

	/**
	 * 获取目标位置数据
	 * 
	 * @return
	 */
	public FolderTargetPosition getTargetPosition();

	/**
	 * 开始换位动画
	 */
	public void startReplace();

	/**
	 * 得到一个图标宽度
	 * 
	 * @return
	 */
	public int getmItemViewWidth();

	/**
	 * 得到一个图标高度
	 * 
	 * @return
	 */
	public int getmItemViewHeight();

	/**
	 * 获取当前是否在长按状态
	 * 
	 * @return
	 */
	public boolean ismIsLongClick();

	/**
	 * 当前操作区域是否超出响应边界
	 * 
	 * @return
	 */
	public boolean isOutOfBound();

	/**
	 * 是否滚动结束
	 * 
	 * @return
	 */
	public boolean isScrollFinished();
}

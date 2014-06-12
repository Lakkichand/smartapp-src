package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle;

import java.util.ArrayList;

import android.graphics.Rect;


/**
 * 
 * <br>类描述:dock换位动画位置记录类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-16]
 */
public class AreaModel {
	private ArrayList<Rect> mAreasList; // ICON空间
	private ArrayList<Rect> mIconsList; // 图标空间
	private int mAreaPosition; // 哪个区域
	private int mMoveToDirection; // 在区域的哪个方向： 左中右
	private ArrayList<Integer> mAddIconList; // ICON空间

	public AreaModel() {
		mAddIconList = new ArrayList<Integer>();
	}

	public ArrayList<Rect> getAreasList() {
		return mAreasList;
	}

	public void setAreasList(ArrayList<Rect> areasList) {
		this.mAreasList = areasList;
	}

	public ArrayList<Rect> getIconsList() {
		return mIconsList;
	}

	public void setIconsList(ArrayList<Rect> iconsList) {
		this.mIconsList = iconsList;
	}

	public int getAreaPosition() {
		return mAreaPosition;
	}

	public void setAreaPosition(int areaPosition) {
		this.mAreaPosition = areaPosition;
	}

	public int getMoveToDirection() {
		return mMoveToDirection;
	}

	public void setMoveToDirection(int moveToDirection) {
		this.mMoveToDirection = moveToDirection;
	}

	public ArrayList<Integer> getAddIconList() {
		return mAddIconList;
	}

	public void setAddIconList(ArrayList<Integer> addIconList) {
		this.mAddIconList = addIconList;
	}

}

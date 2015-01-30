package com.zhidian.wifibox.file.album;

import java.io.Serializable;
import java.util.List;

public class ImageItemGroup implements Serializable {

	public static final long serialVersionUID = 1L;
	
	private String group;
	private List<ImageItem> children;
	private int count;
	private boolean isSelected = false;
	
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public List<ImageItem> getChildren() {
		return children;
	}
	public void setChildren(List<ImageItem> children) {
		this.children = children;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public boolean getIsSelected() {
		return isSelected;
	}
	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	@Override
	public String toString() {
		return "ImageItemGroup [group=" + group + ", children=" + children
				+ "]";
	}

}

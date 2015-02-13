package com.zhidian.wifibox.file.album;

import java.io.Serializable;

public class HeaderBean implements Serializable {
	
	private static final long serialVersionUID = 9171256445108669452L;
	private int sectionTotal = 1;
	private boolean isSelect = false;
	
	public int getSectionTotal() {
		return sectionTotal;
	}
	public void setSectionTotal(int section) {
		this.sectionTotal = section;
	}
	public boolean getIsSelect() {
		return isSelect;
	}
	public void setIsSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}
	
	@Override
	public String toString() {
		return "HeaderBean [sectionTotal=" + sectionTotal + ", isSelect="
				+ isSelect + "]";
	}

}

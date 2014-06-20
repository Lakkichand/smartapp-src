package com.smartapp.rootuninstaller.comparator;

import java.util.Comparator;

import com.smartapp.rootuninstaller.ListDataBean;

/**
 * 大小升序比较器
 * 
 * @author xiedezhi
 * 
 */
public class SizeAscendingComparator implements Comparator<ListDataBean> {

	@Override
	public final int compare(ListDataBean aa, ListDataBean ab) {
		return (int) (aa.mTotalSize - ab.mTotalSize);
	}

}

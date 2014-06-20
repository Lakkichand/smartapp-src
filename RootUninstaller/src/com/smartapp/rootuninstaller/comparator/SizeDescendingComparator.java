package com.smartapp.rootuninstaller.comparator;

import java.util.Comparator;

import com.smartapp.rootuninstaller.ListDataBean;

/**
 * 大小降序比较器
 * 
 * @author xiedezhi
 * 
 */
public class SizeDescendingComparator implements Comparator<ListDataBean> {

	@Override
	public final int compare(ListDataBean aa, ListDataBean ab) {
		return (int) (ab.mTotalSize - aa.mTotalSize);
	}

}

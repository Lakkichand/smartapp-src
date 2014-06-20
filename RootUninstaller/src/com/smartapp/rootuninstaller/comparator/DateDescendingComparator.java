package com.smartapp.rootuninstaller.comparator;

import java.util.Comparator;

import com.smartapp.rootuninstaller.ListDataBean;

/**
 * 日期降序比较器
 * 
 * @author xiedezhi
 * 
 */
public class DateDescendingComparator implements Comparator<ListDataBean> {

	@Override
	public int compare(ListDataBean aa, ListDataBean ab) {
		return ab.mDate.compareTo(aa.mDate);
	}

}

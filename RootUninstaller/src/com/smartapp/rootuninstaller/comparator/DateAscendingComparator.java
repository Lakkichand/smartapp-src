package com.smartapp.rootuninstaller.comparator;

import java.util.Comparator;

import com.smartapp.rootuninstaller.ListDataBean;

/**
 * 日期升序比较器
 * 
 * @author xiedezhi
 * 
 */
public class DateAscendingComparator implements Comparator<ListDataBean> {

	@Override
	public int compare(ListDataBean aa, ListDataBean ab) {
		return aa.mDate.compareTo(ab.mDate);
	}

}

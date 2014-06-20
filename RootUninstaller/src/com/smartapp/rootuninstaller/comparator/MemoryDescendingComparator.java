package com.smartapp.rootuninstaller.comparator;

import java.text.Collator;
import java.util.Comparator;

import com.smartapp.rootuninstaller.ListDataBean;

/**
 * 使用内存降序比较器
 * 
 * @author xiedezhi
 * 
 */
public class MemoryDescendingComparator implements Comparator<ListDataBean> {

	private final Collator sCollator = Collator.getInstance();

	@Override
	public int compare(ListDataBean aa, ListDataBean ab) {
		int ret = ab.mRunningMemoryInt - aa.mRunningMemoryInt;
		if (ret == 0) {
			CharSequence sa = aa.mAppName;
			if (sa == null) {
				sa = "";
			}
			CharSequence sb = ab.mAppName;
			if (sb == null) {
				sb = "";
			}
			ret = sCollator.compare(sa.toString(), sb.toString());
		}
		return ret;
	}

}

package com.smartapp.rootuninstaller.comparator;

import java.text.Collator;
import java.util.Comparator;

import com.smartapp.rootuninstaller.ListDataBean;

/**
 * 名字降序比较器
 * 
 * @author xiedezhi
 * 
 */
public class NameDescendingComparator implements Comparator<ListDataBean> {

	private final Collator sCollator = Collator.getInstance();

	@Override
	public final int compare(ListDataBean aa, ListDataBean ab) {
		CharSequence sa = aa.mAppName;
		if (sa == null) {
			sa = "";
		}
		CharSequence sb = ab.mAppName;
		if (sb == null) {
			sb = "";
		}
		return -sCollator.compare(sa.toString(), sb.toString());
	}

}

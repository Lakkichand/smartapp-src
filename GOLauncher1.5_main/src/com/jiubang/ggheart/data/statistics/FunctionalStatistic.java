package com.jiubang.ggheart.data.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 键名||次数\r\n键名||次数
 * 
 */
public class FunctionalStatistic {
	public enum Rule {
		/*
		 * add count
		 */
		RULE_ADDVALUE,
		/*
		 * update count
		 */
		RULE_UPDATEVALUE
	}

	private static final String SEPARATE = "||";
	private static final String LINE_BREAK = "\r\n";

	private Map<String, Integer> mContent;

	public FunctionalStatistic() {
		mContent = new HashMap<String, Integer>();
	}

	public Map<String, Integer> getContent() {
		return mContent;
	}

	public boolean statistic(Rule rule, String keyStr, int valueInt) {
		if (null == keyStr) {
			return false;
		}
		Integer value = mContent.get(keyStr);
		int newValueInt = 0;
		if (null == value) {
			newValueInt = valueInt;
		} else {
			switch (rule) {
				case RULE_ADDVALUE :
					newValueInt = value.intValue() + valueInt;
					break;
				case RULE_UPDATEVALUE :
					newValueInt = valueInt;
					break;
				default :
					newValueInt = valueInt;
					break;
			}
		}
		value = Integer.valueOf(newValueInt);
		mContent.put(keyStr, value);
		return true;
	}

	public String statisticContent() {
		String ret = null;
		Set<String> keySet = mContent.keySet();
		if (null != keySet) {
			Object[] objects = keySet.toArray();
			int sz = objects.length;
			String keyStr = null;
			Integer value = null;
			for (int i = 0; i < sz; i++) {
				keyStr = (String) objects[i];
				if (null == keyStr) {
					continue;
				}
				value = mContent.get(keyStr);
				if (null == value) {
					continue;
				}

				if (null == ret) {
					ret = keyStr;
				} else {
					ret += LINE_BREAK;
					ret += keyStr;
				}
				ret += SEPARATE;
				ret += value.intValue();
			}
		}
		return ret;
	}
}

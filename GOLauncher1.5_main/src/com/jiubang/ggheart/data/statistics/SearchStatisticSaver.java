package com.jiubang.ggheart.data.statistics;

import java.util.Map;

import android.content.Context;

import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-27]
 */
public class SearchStatisticSaver {
	// MenuKey_Long
	// SearchKey
	public static void saveStatistic(Context context, Map<String, Integer> content) {
		PreferencesManager preferences = new PreferencesManager(context, "search_statistic",
				Context.MODE_PRIVATE);
		Integer value = content.get("long_menu_key");
		if (null == value) {
			preferences.putInt("long_menu_key", 0);
		} else {
			preferences.putInt("long_menu_key", value.intValue());
		}
		value = content.get("search_key");
		if (null == value) {
			preferences.putInt("search_key", 0);
		} else {
			preferences.putInt("search_key", value.intValue());
		}
		value = content.get("widget_search_key");
		if (null == value) {
			preferences.putInt("widget_search_key", 0);
		} else {
			preferences.putInt("widget_search_key", value.intValue());
		}
		// value = content.get("gowidget_search_key");
		// if (null == value)
		// {
		// editor.putInt("gowidget_search_key", 0);
		// }
		// else
		// {
		// editor.putInt("gowidget_search_key", value.intValue());
		// }
		preferences.commit();
	}

	// MenuKey_Long
	// SearchKey
	public static void initStatistic(Context context, Map<String, Integer> content) {
		PreferencesManager preferences = new PreferencesManager(context, "search_statistic",
				Context.MODE_PRIVATE);
		int value = preferences.getInt("long_menu_key", 0);
		content.put("long_menu_key", Integer.valueOf(value));
		value = preferences.getInt("search_key", 0);
		content.put("search_key", Integer.valueOf(value));
		value = preferences.getInt("widget_search_key", 0);
		content.put("widget_search_key", Integer.valueOf(value));
		// value = preferences.getInt("gowidget_search_key", 0);
		// content.put("gowidget_search_key", Integer.valueOf(value));
	}
}

package com.jiubang.ggheart.launcher;

import android.app.ListActivity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

public class PreferenceConfigurationHandler {
	public static void handlePreferenceAppearance(ListActivity activity) {
		PreferenceConfiguration configuration = PreferenceConfiguration.getInstance();
		boolean valid = null == configuration ? false : configuration
				.isPreferenceConfigurationValid();
		if (valid) {
			int style = configuration.getTitleStyle();
			if (style > 0) {
				activity.setTheme(style);
			}

			int color = configuration.getTitleColor();
			if (color != 0) {
				TextView titleView = (TextView) activity.getWindow().getDecorView()
						.findViewById(android.R.id.title);
				if (null != titleView) {
					titleView.setTextColor(color);
				}
			}

			Drawable line = configuration.getSeparateLine();
			if (null != line) {
				activity.getListView().setDivider(line);
			}

			Drawable bg = configuration.getBackground();
			if (null != bg) {
				activity.getListView().setCacheColorHint(0x00000000);
				// activity.getListView().setBackgroundDrawable(bg);
				activity.getWindow().setBackgroundDrawable(bg);
			}

			// 滚动条等有图之后再看看
		}
	}

	public static void handlePreferenceItem(View itemView) {
		PreferenceConfiguration configuration = PreferenceConfiguration.getInstance();
		boolean valid = null == configuration ? false : configuration
				.isPreferenceConfigurationValid();
		if (valid) {
			int color = configuration.getItemTitleColor();
			if (color != 0) {
				TextView titleView = (TextView) itemView.findViewById(android.R.id.title);
				if (null != titleView) {
					titleView.setTextColor(color);
				}
			}

			color = configuration.getItemSummaryColor();
			if (color != 0) {
				TextView summaryView = (TextView) itemView.findViewById(android.R.id.summary);
				if (null != summaryView) {
					summaryView.setTextColor(color);
				}
			}

			Drawable background = configuration.getItemBackground();
			if (null != background) {
				itemView.setBackgroundDrawable(background);
			}
		}
	}

	public static void handlePreferenceCategory(View categoryView) {
		PreferenceConfiguration configuration = PreferenceConfiguration.getInstance();
		boolean valid = null == configuration ? false : configuration
				.isPreferenceConfigurationValid();
		if (valid) {
			int color = configuration.getCategoryColor();
			if (color != 0) {
				TextView titleView = (TextView) categoryView.findViewById(android.R.id.title);
				if (null != titleView) {
					titleView.setTextColor(color);
				}
			}

			Drawable background = configuration.getCategoryBackground();
			if (null != background) {
				categoryView.setBackgroundDrawable(background);
			}
		}
	}
}

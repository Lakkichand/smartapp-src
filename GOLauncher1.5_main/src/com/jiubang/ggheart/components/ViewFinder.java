package com.jiubang.ggheart.components;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewFinder {
	public static void findView(View view, List<TextView> views) {
		if (null == view) {
			return;
		}

		if (view instanceof TextView) {
			((TextView) view).setSingleLine(false);
			views.add((TextView) view);
		} else if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			int count = group.getChildCount();
			for (int i = 0; i < count; i++) {
				findView(group.getChildAt(i), views);
			}
		}
	}
}

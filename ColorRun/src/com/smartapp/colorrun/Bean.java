package com.smartapp.colorrun;

import android.graphics.Rect;

public class Bean {

	public Rect rectself = new Rect(0, 0, 0, 0);

	public int[] colors = new int[4];

	public Rect[] rects = new Rect[4];

	public Bean() {
		for (int i = 0; i < colors.length; i++) {
			colors[i] = 0;
		}
		for (int i = 0; i < rects.length; i++) {
			rects[i] = new Rect(0, 0, 0, 0);
		}
	}

}

package com.dahl.brendan.wordsearch.model;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import com.dahl.brendan.wordsearch.view.R;

public enum Theme {
	ORIGINAL(0, Color.parseColor("white"), Color.parseColor("green"), Color.parseColor("yellow"), Color.parseColor("red"), Color.parseColor("blue")),
	NIGHTSKY(R.drawable.background1, Color.parseColor("white"), Color.parseColor("green"), Color.parseColor("yellow"), Color.parseColor("red"), Color.parseColor("blue")),
	SKY(R.drawable.background2, Color.parseColor("black"), Color.parseColor("#33cc33"), Color.parseColor("#00cc00"), Color.parseColor("#ff0000"), Color.parseColor("#f0ff00")),
	PINK(R.drawable.background3, Color.parseColor("black"), Color.parseColor("#05a9a7"), Color.parseColor("#00d0ce"), Color.parseColor("#0000ff"), Color.parseColor("#00ff00")),
	SEAGREEN(R.drawable.background4, Color.parseColor("yellow"), Color.parseColor("#ffba00"), Color.parseColor("red"), Color.parseColor("#ff00fc"), Color.parseColor("#ffc000"));

	final public int background;
	final public ColorStateList normal;
	final public ColorStateList picked;
	final private int focus;
	private int greenDelta;
	private int redDelta;
	private int blueDelta;
	final private int foundStart;
	final private int foundEnd;
	private int foundCurrent;

	private Theme(int background, int normal, int focus, int picked, int foundStart, int foundEnd) {
		this.background = background;
		this.foundStart = foundStart;
		this.foundEnd = foundEnd;
		this.focus = focus;
		this.normal =  new ColorStateList(new int[][] {
				new int[] { 
					android.R.attr.state_focused,
					android.R.attr.state_enabled
				},
				new int[0]
			},
			new int[] {
				focus,
				normal
			}
		);
		this.picked = new ColorStateList(
				new int[][] {new int[0] },
				new int[] { picked }
				);
		this.reset(20);
	}
	
	public void reset(int count) {
		if (count == 0) {
			count = 20;
		}
		foundCurrent = foundStart;
		this.greenDelta = (Color.green(foundEnd)-Color.green(foundStart))/count;
		this.redDelta = (Color.red(foundEnd)-Color.red(foundStart))/count;
		this.blueDelta = (Color.blue(foundEnd)-Color.blue(foundStart))/count;
	}

	public ColorStateList getCurrentFound() {
		ColorStateList current = new ColorStateList(new int[][] {
				new int[] { 
					android.R.attr.state_focused,
					android.R.attr.state_enabled
				},
				new int[0]
			},
			new int[] {
				this.focus,
				this.foundCurrent
			}
		);
		return current;
	}
	
	public void updateCurrentFound() {
		foundCurrent = Color.rgb(
				Color.red(foundCurrent) + redDelta,
				Color.green(foundCurrent) + greenDelta,
				Color.blue(foundCurrent) + blueDelta);
	}
	
	final static private String BUNDLE_GREEN_DELTA = "green_delta";
	final static private String BUNDLE_BLUE_DELTA = "blue_delta";
	final static private String BUNDLE_RED_DELTA = "red_delta";
	final static private String BUNDLE_CURRENT = "current";
	
	public Bundle toBundle() {
		Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_GREEN_DELTA, greenDelta);
		bundle.putInt(BUNDLE_BLUE_DELTA, blueDelta);
		bundle.putInt(BUNDLE_RED_DELTA, redDelta);
		bundle.putInt(BUNDLE_CURRENT, foundCurrent);
		return bundle;
	}
	
	public void fromBundle(Bundle bundle) {
		greenDelta = bundle.getInt(BUNDLE_GREEN_DELTA, greenDelta);
		blueDelta = bundle.getInt(BUNDLE_BLUE_DELTA, blueDelta);
		redDelta = bundle.getInt(BUNDLE_RED_DELTA, redDelta);
		foundCurrent = bundle.getInt(BUNDLE_CURRENT, foundCurrent);
	}
}

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiubang.ggheart.apps.gowidget.taskmanager;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

/**
 * An animation that controls the scale of an object. You can specify the point
 * to use for the center of scaling.
 * 
 */
public class ScaleAddAnimation extends Animation {
	private int mFromX;
	private int mToX;

	private long mUsedMemory;
	private TextView mShowText;
	private TextView mProgressView;

	/**
	 * Constructor to use when building a ScaleAnimation from code
	 * 
	 * @param fromX
	 *            Horizontal scaling factor to apply at the start of the
	 *            animation
	 * @param toX
	 *            Horizontal scaling factor to apply at the end of the animation
	 * @param fromY
	 *            Vertical scaling factor to apply at the start of the animation
	 * @param toY
	 *            Vertical scaling factor to apply at the end of the animation
	 * @param pivotXType
	 *            Specifies how pivotXValue should be interpreted. One of
	 *            Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
	 *            Animation.RELATIVE_TO_PARENT.
	 * @param pivotXValue
	 *            The X coordinate of the point about which the object is being
	 *            scaled, specified as an absolute number where 0 is the left
	 *            edge. (This point remains fixed while the object changes
	 *            size.) This value can either be an absolute number if
	 *            pivotXType is ABSOLUTE, or a percentage (where 1.0 is 100%)
	 *            otherwise.
	 * @param pivotYType
	 *            Specifies how pivotYValue should be interpreted. One of
	 *            Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
	 *            Animation.RELATIVE_TO_PARENT.
	 * @param pivotYValue
	 *            The Y coordinate of the point about which the object is being
	 *            scaled, specified as an absolute number where 0 is the top
	 *            edge. (This point remains fixed while the object changes
	 *            size.) This value can either be an absolute number if
	 *            pivotYType is ABSOLUTE, or a percentage (where 1.0 is 100%)
	 *            otherwise.
	 */
	public ScaleAddAnimation(int fromLenth, int toLenth, TextView progressView, TextView showText,
			long usedMemory) {
		mFromX = fromLenth;
		mToX = toLenth;
		mUsedMemory = usedMemory;

		mShowText = showText;
		mProgressView = progressView;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		int sx = 1;

		if (mProgressView.getWidth() < mToX) {
			sx = (int) ((mToX - mFromX) * interpolatedTime);
			mShowText.setText((int) (mUsedMemory * interpolatedTime) + "M");
			mProgressView.setWidth(mFromX + sx);
		} else {
			mProgressView.setWidth(mToX);
			mShowText.setText(mUsedMemory + "M");
		}

	}
}

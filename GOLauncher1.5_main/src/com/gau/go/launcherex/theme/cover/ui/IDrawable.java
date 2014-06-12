package com.gau.go.launcherex.theme.cover.ui;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
/**
 * 可绘制景物的接口
 * @author jiangxuwen
 *
 */
public interface IDrawable {

	public abstract void doDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint);
}

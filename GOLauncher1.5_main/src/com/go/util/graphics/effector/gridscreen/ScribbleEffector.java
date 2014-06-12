package com.go.util.graphics.effector.gridscreen;

public class ScribbleEffector extends SphereEffector {

}

// class ScribbleEffector extends BaseGridScreenEffector {
// static final float MAX_SCALE = 1.5f;
// float mRatio;
//
// @Override
// public void onLayout(int w, int h) {
// super.onLayout(w, h);
// mRatio = 1.0f / w;
// }
//
// @Override
// public void drawScreen(Canvas canvas, int screen, int scroll, int left) {
// float t = (left - scroll) * mRatio;
// float scale = 1 - Math.abs(Math.abs(t) - 0.5f) * 2;
// final GridScreenContainer grid = mContainer;
// final int row = grid.getCellRow();
// final int col = grid.getCellCol();
// int index = row * col * screen;
// final int end = Math.min(grid.getCellCount(), index + row * col);
// final int cellWidth = grid.getCellWidth();
// final int cellHeight = grid.getCellHeight();
// final int cellCenterX = cellWidth / 2;
// final int cellCenterY = cellHeight / 2;
// final int paddingLeft = grid.getPaddingLeft();
// final int paddingTop = grid.getPaddingTop();
// // canvas.translate(scroll - left + mCenterX, mCenterY);
// final DrawFilter filter = canvas.getDrawFilter();
// canvas.setDrawFilter(DRAW_FILTER);
//
// int j1 = 0, j2 = col, dj = 1, mid = 0;
// if(left < scroll){
// j1 = col - 1;
// j2 = -1;
// dj = -1;
// mid = col;
// index += j1;
// }
// float ratio = (float)Math.PI / (2 * 2 + 1);
// // 从两屏的中间往左右的顺序绘制（左屏从右到左绘制，右屏从左到右绘制）
// for(int j = j1; j != j2; j += dj, index += dj){
// int cellX = paddingLeft + cellWidth * j;
// float cos = (float)Math.cos((j + 0.5 - mid) * ratio);
// float s = interpolate(1, interpolate(1, MAX_SCALE, scale), cos * cos);
// for(int i = 0, cellY = paddingTop, index2 = index; i < row && index2 < end;
// ++i) {
// canvas.save();
// canvas.translate(cellX, cellY);
// canvas.scale(s, s, cellCenterX, cellCenterY);
// grid.drawGridCell(canvas, index2);
// canvas.restore();
// cellY += cellHeight;
// index2 += col;
// }
// }
// canvas.setDrawFilter(filter); // restore filter
// }
// }
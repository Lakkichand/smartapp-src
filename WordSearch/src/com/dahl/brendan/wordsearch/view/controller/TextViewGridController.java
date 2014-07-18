//    This file is part of Open WordSearch.
//
//    Open WordSearch is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Open WordSearch is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Open WordSearch.  If not, see <http://www.gnu.org/licenses/>.
//
//	  Copyright 2009, 2010 Brendan Dahl <dahl.brendan@brendandahl.com>
//	  	http://www.brendandahl.com

package com.dahl.brendan.wordsearch.view.controller;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.dahl.brendan.wordsearch.model.ColorState;
import com.dahl.brendan.wordsearch.model.Grid;
import com.dahl.brendan.wordsearch.model.Selection;
import com.dahl.brendan.wordsearch.model.Theme;
import com.dahl.brendan.wordsearch.model.Word;
import com.dahl.brendan.wordsearch.util.ConversionUtil;
import com.dahl.brendan.wordsearch.view.R;
import com.dahl.brendan.wordsearch.view.WordSearchActivity;

/**
 * 
 * @author Brendan Dahl
 * 
 * contains the game logic of the text view grid and interactions
 * most complex stuff happens here
 *
 */
public class TextViewGridController implements OnTouchListener, OnKeyListener, Callback {
	class EventHandlerCallback implements Callback {
		public boolean handleMessage(Message msg) {
			if (handlerEvents.hasMessages(MSG_CLEAR)) {
				return true;
			}
			switch (msg.what) {
			case MSG_KEY: {
				selectionStartEnd((TextView)msg.obj);
				break;
			}
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE: {
				if (handlerEvents.hasMessages(MotionEvent.ACTION_UP) || handlerEvents.hasMessages(MotionEvent.ACTION_DOWN)) {
					return true;
				}
			}
			case MotionEvent.ACTION_UP: {
				MotionEvent event = (MotionEvent)msg.obj;
				setupDemensionPoints();
				Point point = new Point();// row and column of the grid that was touched
				Point pointPadding = new Point();// the x and y offset within the
													// touched grid TextView where the
													// touch occurred
				point.y = (int) (Math.round(event.getY()) / pointDemension.y);
				pointPadding.y = (int) (Math.round(event.getY()) % pointDemension.y);
				if (pointPadding.y == 0 && point.y != 0) {
					point.y--;
				}
				point.x = (int) (Math.round(event.getX()) / pointDemension.x);
				pointPadding.x = (int) (Math.round(event.getX()) % pointDemension.x);
				if (pointPadding.x == 0 && point.x != 0) {
					point.x--;
				}
				if (touchMode) {
					handleTextViewEventTouch(point, pointPadding, event.getAction());
				} else {
					handleTextViewEventClick(point, pointPadding, event.getAction());
				}
				break;
			}
			default: {
				return false;
			}
			}
			return true;
		}
	}

	final private static Paint mPaintFound = new Paint();
	final private static Paint mPaintMiss = new Paint();
	final private static String LOG_TAG = "TextViewGridController";
	/**
	 * populated by the setupgridview in {@link WordSearchActivity.java}
	 */
	private TextView[][] gridView = null;
	/**
	 * overall word search game controller
	 */
	final private WordSearchActivityController control;

	/**
	 * holds the user's current selection progress
	 */
	final private Selection selection = new Selection();
	/**
	 * width and height of one grid TextView
	 */
	private PointF pointDemension;
	final private PointF pointValidMin = new PointF();
	final private PointF pointValidMax = new PointF();

	final private Handler handler;
	final private HandlerThread threadEvents;
	final private Handler handlerEvents;
	
	private boolean touchMode = true;
	final private static int MSG_SET_TEXT_COLOR = 0;
	final private static int MSG_CLEAR = -1;
	final private static int MSG_KEY = -2;
	final private static String MSG_DATA_COLOR = "data_color";
	final private static String MSG_DATA_FOUND = "data_found";

	protected TextViewGridController(WordSearchActivityController control) {
		this.control = control;
		this.threadEvents = new HandlerThread("word-search-events");
		this.threadEvents.start();
		this.handlerEvents = new Handler(threadEvents.getLooper(), new EventHandlerCallback());
		this.handler = new Handler(this);
	}

	public void clearPointDemension() {
		this.pointDemension = null;
	}
	
	public TextView[][] getGridView() {
		return gridView;
	}

	private String getSelectionWord(Point pointStart, Point pointEnd, Point delta) {
		String selectionWord = "";
		Point point = new Point();
		point.x = pointStart.x;
		point.y = pointStart.y;
		if (!Selection.isValidPoint(point, gridView.length)) {
			throw new NullPointerException("point: "+point.x+","+point.y+"; delta: "+delta.x+","+delta.y + "; length: "+gridView.length);
		}
		selectionWord += this.gridView[point.y][point.x].getText();
		do {
			point.x += delta.x;
			point.y += delta.y;
			if (!point.equals(pointStart) && Selection.isValidPoint(point, gridView.length)) {
				selectionWord += this.gridView[point.y][point.x].getText();
			}
		} while (!point.equals(pointEnd) && Selection.isValidPoint(point, gridView.length));
		return selectionWord;
	}
	
	private Theme getTheme() {
		return control.getTheme();
	}

	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_SET_TEXT_COLOR: {
			TextView view = (TextView)msg.obj;
			ColorState color = msg.getData().getParcelable(MSG_DATA_COLOR);
			ColorStateList found = msg.getData().getParcelable(MSG_DATA_FOUND);
			if (color == null) {
				color = ColorState.NORMAL;
			}
			switch (color) {
			case SELECTED:
				if (this.handler.hasMessages(msg.what, msg.obj)) {
					return true;
				}
				if (view.getTag() == null) {
					view.setTag(view.getTextColors());
					view.setTextColor(getTheme().picked);
				}
				break;
			case FOUND:
				view.setTag(null);
				view.setTextColor(found);
				break;
			case NORMAL:
			default:
				Object tag = view.getTag();
				view.setTag(null);
				if (tag instanceof ColorStateList) {
					view.setTextColor((ColorStateList) tag);
				} else if (getTheme().picked.equals(view.getTextColors())) {
					view.setTextColor(getTheme().normal);
				}
				break;
			}
			break;
		}
		default: {
			return false;
		}
		}
		return true;
	}

	private void handleTextViewEventClick(Point point, Point pointPadding, int action) {
		if (Selection.isValidPoint(point, gridView.length)) {
			TextView views2 = gridView[point.y][point.x];
			switch (action) {
			case MotionEvent.ACTION_UP:
				selectionStartEnd(views2);
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_OUTSIDE:
				control.setLetter(null);
				break;
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				control.setLetter(views2.getText());
				break;
			default:
			}
		} else {
			Log.e(LOG_TAG, "INVALID ONTOUCH POINT, " + point);
			control.setLetter(null);
		}
	}
	
	private void handleTextViewEventTouch(Point point, Point pointPadding, int action) {
		// if touch happened outside the middle 1/2 of the width of the TextView
		// or outside the middle 3/5 of the height of the TextView
		// ignore touch unless it is a down or up event in which case force an
		// end to the selection
		if (pointPadding.x < pointValidMin.x
				|| pointPadding.x > pointValidMax.x
				|| pointPadding.y < pointValidMin.y
				|| pointPadding.y > pointValidMax.y) {
			switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_UP:
				selectionEnd();
				break;
			}
			return;
		}

		// if touch was within the gridView assign gridView else if up or down
		// event force a selection end
		TextView views2 = null;
		if (Selection.isValidPoint(point, gridView.length)) {
			views2 = gridView[point.y][point.x];
		} else {
			switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_UP:
				selectionEnd();
				break;
			}
			Log.e(LOG_TAG, "INVALID ONTOUCH POINT, " + point);
			return;
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			selectionStartEnd(views2);
			break;
		case MotionEvent.ACTION_UP:
			if (selection.hasBegun()) {
				selectionAdd(views2);
				selectionEnd();
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			selectionEnd();
			break;
		case MotionEvent.ACTION_OUTSIDE:
			selectionEnd();
			break;
		case MotionEvent.ACTION_MOVE:
			if (selection.hasBegun()) {
				if (selection.getEnd().getId() != views2.getId()) {
					selectionAdd(views2);
				}
			} else {
				selectionStart(views2);
			}
			break;
		default:
		}
	}
	
	private Paint isValidClick(int x, int y) {
		setupDemensionPoints();
		Point point = new Point();// row and column of the grid that was touched
		Point pointPadding = new Point();// the x and y offset within the
											// touched grid TextView where the
											// touch occurred
		point.y = (int) (Math.round(y) / pointDemension.y);
		pointPadding.y = (int) (Math.round(y) % pointDemension.y);
		if (pointPadding.y == 0 && point.y != 0) {
			point.y--;
		}
		point.x = (int) (Math.round(x) / pointDemension.x);
		pointPadding.x = (int) (Math.round(x) % pointDemension.x);
		if (pointPadding.x == 0 && point.x != 0) {
			point.x--;
		}
		// if touch happened outside the middle 1/2 of the width of the TextView
		// or outside the middle 3/5 of the height of the TextView
		// ignore touch unless it is a down or up event in which case force an
		// end to the selection
		if (pointPadding.x < pointValidMin.x
				|| pointPadding.x > pointValidMax.x
				|| pointPadding.y < pointValidMin.y
				|| pointPadding.y > pointValidMax.y) {
			int num = ((point.x+point.y)*15)%255;
			mPaintMiss.setColor(Color.rgb(num, num, num));
			return mPaintMiss;
		}

		// if touch was within the gridView assign gridView else if up or down
		// event force a selection end
		if (Selection.isValidPoint(point, gridView.length)){
			return mPaintFound;
		} else {
			return null;
		}
	}
	
	public Drawable makeBackground(int height, int width) {
		mPaintFound.setColor(0xff00ff00);
		mPaintMiss.setColor(0xffff0000);
		Bitmap mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas mCanvas = new Canvas(mBitmap);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Paint paint = isValidClick(x, y);
				if (paint != null) {
					mCanvas.drawPoint(x, y, paint);
				}
			}
		}
		return new BitmapDrawable(mBitmap);
	}

	/**
	 * allow normal operation for arrows
	 * 
	 * enter key and space causes selection
	 */
	public boolean onKey(View view, int keyCode, KeyEvent event) {
//		Log.v(LOG_TAG, "onKey," + view.getId() + ":" + keyCode + ", event:"	+ event.toString());
		switch (keyCode) {
		case 19:
		case 20:
		case 21:
		case 22:
		case 82:
			return false;
		case 23:
		case 62:
		case 66:
			if (event.getAction() == KeyEvent.ACTION_UP && view instanceof TextView) {
				Message.obtain(handlerEvents, MSG_KEY, view).sendToTarget();
			}
			return true;
		default:
			return true;
		}
	}

	public boolean onTouch(View view, MotionEvent event) {
		Message.obtain(handlerEvents, event.getAction(), view.getId(), 0, event).sendToTarget();
		return true;
	}

	/**
	 * 
	 * @param grid word search model of all the letters in a grid
	 */
	public void reset(Grid grid) {
		handlerEvents.sendEmptyMessage(MSG_CLEAR);
		Point point = new Point();
		for (point.y = 0; point.y < gridView.length; point.y++) {
			for (point.x = 0; point.x < gridView[point.y].length; point.x++) {
				gridView[point.y][point.x].setText(grid.getLetterAt(point).toString());
				gridView[point.y][point.x].setTag(null);
				gridView[point.y][point.x].setTextColor(getTheme().normal);
			}
		}
		for (Word wordFound : grid.getWordsFound()) {
			this.selectionPaint(wordFound.getPointStart(), wordFound.getPointEnd(), ColorState.FOUND);
			control.getTheme().updateCurrentFound();
		}
	}

	/**
	 * adds a TextView to the current selection is it fits in the current selection
	 * 
	 * @param view TextView from grid to add to the current selection list
	 * @return false if selection failed
	 */
	private boolean selectionAdd(TextView view) {
		if (view == null) {
			return false;
		}
		if (!selection.hasBegun()) {// starting
			selection.setStart(view);
			setTextViewColor(view, ColorState.SELECTED);
			control.setLetter(selection.getEnd().getText());
		} else if (!view.equals(selection.getStart())
				&& !view.equals(selection.getEnd())) {
			Point pointStart = ConversionUtil.convertIDToPoint(selection.getStart().getId(), control.getGridSize());
			Point pointEnd = ConversionUtil.convertIDToPoint(selection.getEnd().getId(), control.getGridSize());
			Point pointNew = ConversionUtil.convertIDToPoint(view.getId(), control.getGridSize());
			Point delta = Selection.getDeltas(pointStart, pointNew);
			if (delta == null) {
				return false;
			}
			if (!Selection.getDeltas(pointStart, pointEnd).equals(delta)) {
				this.selectionPaint(pointStart, pointEnd, null);
				pointEnd = pointStart;
			}
			int length = Selection.getLength(pointStart, pointNew);
			int lengthOld = Selection.getLength(pointStart, pointEnd);
			int lengthDiff = length - lengthOld;
			if (lengthDiff > 0) {// growing
				this.selectionPaint(pointEnd, pointNew, ColorState.SELECTED);
			} else {// shrinking
				this.selectionPaint(pointNew, pointEnd, null);
				this.setTextViewColor(view, ColorState.SELECTED);
			}
			selection.setEnd(view);
			control.setLetter(getSelectionWord(pointStart, pointNew, delta));
		}
		return true;
	}

	/**
	 * takes the current selection and checks to see if it is a real word in the grid
	 * also resets the selection
	 */
	private void selectionEnd() {
		if (this.selection.hasBegun()) {// if selection has been started
			Point pointStart = ConversionUtil.convertIDToPoint(this.selection.getStart().getId(), control.getGridSize());
			Point pointEnd = ConversionUtil.convertIDToPoint(this.selection.getEnd().getId(), control.getGridSize());
			String word = control.guessWord(pointStart, pointEnd);
			if (word == null) {// selection was not a word in the grid so revert the colors of all leters in the selection
				this.selectionPaint(pointStart, pointEnd, ColorState.NORMAL);
			} else {// highlight found word in grid and pass found back to control main
				this.selectionPaint(pointStart, pointEnd, ColorState.FOUND);
				control.getTheme().updateCurrentFound();
				control.foundWord(word);
			}
		}
		this.selection.reset();
		control.setLetter(null);
	}

	/**
	 * will iterate from pointStart to pointEnd and change each TextView's color to supplied color
	 * 
	 * @param pointStart first point in selection
	 * @param pointEnd last point in selection
	 * @param color null to revert a previouscolorPicked
	 * 				colorPicked to set the color to colorPicked
	 * 				colorFound to set the color to currentColor
	 */
	private void selectionPaint(Point pointStart, Point pointEnd, ColorState color) {
		Point delta = Selection.getDeltas(pointStart, pointEnd);
		if (delta == null) {
			return;
		}
		Point point = new Point();
		point.x = pointStart.x;
		point.y = pointStart.y;
		if (!Selection.isValidPoint(point, gridView.length)) {
			throw new NullPointerException("point: "+point.x+","+point.y+"; delta: "+delta.x+","+delta.y + "; length: "+gridView.length);
		}
		this.setTextViewColor(this.gridView[point.y][point.x], color);
		do {
			point.x += delta.x;
			point.y += delta.y;
			if (!point.equals(pointStart) && Selection.isValidPoint(point, gridView.length)) {
				this.setTextViewColor(this.gridView[point.y][point.x], color);
			}
		} while (!point.equals(pointEnd) && Selection.isValidPoint(point, gridView.length));
	}

	/**
	 * reset a selection if needed, then start a new one with selectionAdd
	 * 
	 * @param view TextView to pass into selectionAdd
	 */
	private void selectionStart(TextView view) {
		if (this.selection.hasBegun()) {
			Point pointStart = ConversionUtil.convertIDToPoint(this.selection.getStart().getId(), control.getGridSize());
			Point pointEnd = ConversionUtil.convertIDToPoint(this.selection.getEnd().getId(), control.getGridSize());
			this.selectionPaint(pointStart, pointEnd, null);
			this.selection.reset();
		}
		this.selectionAdd(view);
	}
	/**
	 * Will either start or end a selection based on whether the selection has already begun
	 * 
	 * @param view TextView to act upon
	 */
	private void selectionStartEnd(TextView view) {
		if (!this.selection.hasBegun()) {// start selection
			selectionStart(view);
		} else {// end selection
			selectionAdd(view);
			selectionEnd();
		}
	}
	public void setGridView(TextView[][] gridViewNew) {
		this.gridView = gridViewNew;
	}
	/**
	 * Changes a single TextView's color; and saves the old color within the TextView for later reverting if passing colorPicked
	 * 
	 * @param view TextView in the grid to change the color of
	 * @param color change the view to this color; or pass in null to revert the color to its old color
	 */
	private void setTextViewColor(TextView view, ColorState color) {
		Message msg = Message.obtain(handler, MSG_SET_TEXT_COLOR, view);
		msg.getData().putParcelable(MSG_DATA_COLOR, color);
		msg.getData().putParcelable(MSG_DATA_FOUND, getTheme().getCurrentFound());
		msg.sendToTarget();
	}
	protected void setTouchMode(boolean touchMode2) {
		if (touchMode2 != touchMode) {
			this.touchMode = touchMode2;
			this.selectionEnd();
		}
	}
	// defines grid TextView's height and width for later calculations if it isn't already saved
	private void setupDemensionPoints() {
		if (pointDemension == null) {
			View t = gridView[0][0].getRootView().findViewById(R.id.gridTable);
			PointF p = new PointF();
			p.x = t.getWidth()/(float)gridView.length;
			p.y = t.getHeight()/(float)gridView.length;
			pointDemension = p;
			pointValidMin.x = pointDemension.x / 4;
			pointValidMin.y = (pointDemension.y-gridView[0][0].getTextSize())/2;
			pointValidMax.x = pointDemension.x * 3 / 4;
			pointValidMax.y = gridView[0][0].getTextSize() + pointValidMin.x;
		}
	}
}

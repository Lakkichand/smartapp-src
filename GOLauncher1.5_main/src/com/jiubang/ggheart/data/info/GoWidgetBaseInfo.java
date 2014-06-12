package com.jiubang.ggheart.data.info;

import android.content.ContentValues;
import android.database.Cursor;

import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.data.tables.GoWidgetTable;
import com.jiubang.ggheart.data.theme.ThemeManager;

public class GoWidgetBaseInfo implements IDatabaseObject {
	/**
	 * 内置widget原型，用来唯一区分内置widget的属性，新增的widget在此处添加一个id {@link #mPrototype}
	 * 在res/xml/inner_widget.xml中每个节点的
	 */
	public final static int PROTOTYPE_NORMAL = 0;
	public final static int PROTOTYPE_TASKMAN = 1;
	public final static int PROTOTYPE_GOSTORE = 2;
	public final static int PROTOTYPE_APPGAME = 3;

	public static final int DEFAULT_THEME_ID = -1;
	public static final int NEW_THEME_ID = 0;

	/**
	 * widget状态，用于通知widget进入离开屏幕
	 */
	public static final int STATE_NONE = 0;
	public static final int STATE_ENTER = 1;
	public static final int STATE_LEAVE = 2;

	public int mWidgetId;
	public int mType;
	public String mLayout;
	public String mPackage;
	public String mClassName;
	public String mTheme;
	public int mThemeId;
	public int mPrototype;

	/**
	 * 进入/离开屏幕状态，不需要写入数据库
	 */
	public int mState = STATE_NONE;

	public GoWidgetBaseInfo() {
		mWidgetId = GoWidgetManager.INVALID_GOWIDGET_ID;
		mType = -1;
		mPrototype = PROTOTYPE_NORMAL;
		mTheme = ThemeManager.DEFAULT_THEME_PACKAGE;
		mThemeId = DEFAULT_THEME_ID;
	}

	@Override
	public void writeObject(ContentValues values, String table) {
		if (null == values) {
			return;
		}

		values.put(GoWidgetTable.WIDGETID, mWidgetId);
		values.put(GoWidgetTable.TYPE, mType);
		values.put(GoWidgetTable.LAYOUT, mLayout);
		values.put(GoWidgetTable.PACKAGE, mPackage);
		values.put(GoWidgetTable.CLASSNAME, mClassName);
		values.put(GoWidgetTable.THEME, mTheme);
		values.put(GoWidgetTable.THEMEID, mThemeId);
		values.put(GoWidgetTable.PROTOTYPE, mPrototype);
	}

	@Override
	public void readObject(Cursor cursor, String table) {
		if (GoWidgetTable.TABLENAME.equals(table)) {
			int index = cursor.getColumnIndex(GoWidgetTable.WIDGETID);
			mWidgetId = cursor.getInt(index);

			index = cursor.getColumnIndex(GoWidgetTable.TYPE);
			mType = cursor.getInt(index);

			index = cursor.getColumnIndex(GoWidgetTable.LAYOUT);
			mLayout = cursor.getString(index);

			index = cursor.getColumnIndex(GoWidgetTable.PACKAGE);
			mPackage = cursor.getString(index);

			index = cursor.getColumnIndex(GoWidgetTable.CLASSNAME);
			mClassName = cursor.getString(index);

			index = cursor.getColumnIndex(GoWidgetTable.THEME);
			mTheme = cursor.getString(index);

			index = cursor.getColumnIndex(GoWidgetTable.THEMEID);
			mThemeId = cursor.getInt(index);

			index = cursor.getColumnIndex(GoWidgetTable.PROTOTYPE);
			mPrototype = cursor.getInt(index);
		}
	}

}

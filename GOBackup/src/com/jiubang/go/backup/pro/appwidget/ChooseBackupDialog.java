package com.jiubang.go.backup.pro.appwidget;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * @author jiangpeihe
 *
 */
public class ChooseBackupDialog extends AlertDialog {
	private ListView mListView;
	private BaseAdapter mBaseAdapter;
	protected ChooseBackupDialog(Context context) {
		super(context);
	}

	public ChooseBackupDialog(Context context, ListView view, BaseAdapter baseAdapter) {
		super(context);
		mListView = view;
		mBaseAdapter = baseAdapter;
	}

	public void showDilog() {
		if (mListView != null && mBaseAdapter != null) {
			mListView.setAdapter(mBaseAdapter);
			setView(mListView);
		}
	}

}

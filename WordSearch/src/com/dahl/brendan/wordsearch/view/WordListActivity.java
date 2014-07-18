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

package com.dahl.brendan.wordsearch.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dahl.brendan.wordsearch.Constants;
import com.dahl.brendan.wordsearch.view.WordDictionaryProvider.Word;

/**
 * 
 * @author Brendan Dahl
 * 
 *         Activity to allow user to edit the WordDictionaryProvider's database
 *         of words
 */
public class WordListActivity extends ListActivity implements
		OnItemClickListener, OnClickListener {
	class ExportListener implements View.OnClickListener, OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				AlertDialog ad = (AlertDialog) dialog;
				boolean overwrite = ((CheckBox) ad
						.findViewById(R.id.dialog_io_overwrite)).isChecked();
				Intent service = new Intent(WordListActivity.this,
						IOService.class);
				service.putExtra(IOService.EXTRA_ACTION_TYPE,
						IOService.ACTION_WRITE);
				service.putExtra(IOService.EXTRA_FILENAME, ((TextView) ad
						.findViewById(R.id.dialog_io_filename)).getText());
				service.putExtra(IOService.EXTRA_OVERWRITE, overwrite);
				startService(service);
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				break;
			}
		}

		public void onClick(View v) {
			Intent intent = new Intent(Constants.ACTION_PICK_FILE);
			// Construct URI from file name.
			intent.setData(Uri.parse("file://" + exportFilename.getText()));
			// Set fancy title and button (optional)
			intent.putExtra(Constants.EXTRA_BUTTON_TEXT, getString(R.string.EXPORT));
			 if (isPickFileIntentAvailable()) {
				 startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY_EXPORT);
			 } else {
					showMissingDialog();
			 }
		}
	}
	class ImportListener implements View.OnClickListener, OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				AlertDialog ad = (AlertDialog) dialog;
				boolean overwrite = ((CheckBox) ad.findViewById(R.id.dialog_io_overwrite)).isChecked();
				Intent service = new Intent(WordListActivity.this, IOService.class);
				service.putExtra(IOService.EXTRA_ACTION_TYPE, IOService.ACTION_READ);
				service.putExtra(IOService.EXTRA_FILENAME, ((TextView) ad.findViewById(R.id.dialog_io_filename)).getText());
				service.putExtra(IOService.EXTRA_OVERWRITE, overwrite);
				startService(service);
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				break;
			}
		}

		public void onClick(View v) {
			Intent intent = new Intent(Constants.ACTION_PICK_FILE);
			// Construct URI from file name.
			intent.setData(Uri.parse("file://" + importFilename.getText()));
			// Set fancy title and button (optional)
			intent.putExtra(Constants.EXTRA_BUTTON_TEXT, getString(R.string.IMPORT));

			if (isPickFileIntentAvailable()) {
				startActivityForResult(intent, REQUEST_CODE_PICK_FILE_OR_DIRECTORY_IMPORT);
			} else {
				showMissingDialog();
			}
		}
	}

	public void showMissingDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.MISSING_FILE_MANAGER);
		builder.setNeutralButton(R.string.DEFAULT, this);
		builder.setPositiveButton(R.string.custom, new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.openintents.filemanager"));
				startActivity(intent2);
			}
		});
		builder.show();
	}
	public static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY_IMPORT = 1;
	public static final int REQUEST_CODE_PICK_FILE_OR_DIRECTORY_EXPORT = 2;

	public TextView importFilename;
	public TextView exportFilename;

	public static final int DIALOG_ID_CLICK = 0;
	public static final int DIALOG_ID_ADD = 1;
	public static final int DIALOG_ID_NO_WORDS = 2;
	public static final int DIALOG_ID_IMPORT = 3;
	public static final int DIALOG_ID_EXPORT = 4;
	public static final int EDIT_ID = 10;
	private static final String LOG_TAG = "WordList";
	/**
	 * text is a field used in edit and add dialogs that is here to allow retrieval from alertdialog
	 */
	private EditText text;
	/**
	 * index of word being edited or inserted by alert dialog
	 */
	private long index;

	/**
	 * constant used in above index to indicate that the dialog is doing an insert
	 */
	private static final long INSERT_INDEX = -1L;
	private boolean isPickFileIntentAvailable() {
		return this
				.getPackageManager()
				.queryIntentActivities(new Intent(Constants.ACTION_PICK_FILE),
						0).size() > 0;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && data != null) {
			// obtain the filename
			String filename = data.getDataString();
			if (filename != null) {
				// Get rid of URI prefix:
				if (filename.startsWith("file://")) {
					filename = filename.substring(7);
				}
				switch (requestCode) {
				case REQUEST_CODE_PICK_FILE_OR_DIRECTORY_IMPORT:
					importFilename.setText(filename);
					break;
				case REQUEST_CODE_PICK_FILE_OR_DIRECTORY_EXPORT:
					exportFilename.setText(filename);
					break;
				}
			}
		}
	}

	public void onClick(DialogInterface dialog, int whichButton) {
		switch (whichButton) {
		case Dialog.BUTTON_NEGATIVE: {
			Uri uri = ContentUris.withAppendedId(getIntent().getData(), index);
			getContentResolver().delete(uri, null, null);
			break;
		}
		case Dialog.BUTTON_NEUTRAL:
			break;
		case Dialog.BUTTON_POSITIVE: {
			EditText text = (EditText) ((AlertDialog) dialog)
					.findViewById(EDIT_ID);
			String str = text.getText().toString();
			String str2 = "";
			for (int i = 0; i < str.length() && str2.length() <= 10; i++) {
				Character c = str.charAt(i);
				if (Character.isLetter(c)) {
					str2 += Character.toUpperCase(c);
				}
			}
			if (str2 != null && str2.trim().length() >= 4) {
				ContentValues values = new ContentValues();
				values.put(Word.WORD, str2);
				if (index == INSERT_INDEX) {
					getContentResolver().insert(getIntent().getData(), values);
				} else {
					Uri uri = ContentUris.withAppendedId(getIntent().getData(),
							index);
					getContentResolver().update(uri, values, null, null);
				}
			} else {
				Toast.makeText(this, R.string.invalid_word, Toast.LENGTH_LONG)
						.show();
			}
			break;
		}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(LOG_TAG, "bad menuInfo", e);
			return false;
		}

		switch (item.getItemId()) {
		case R.id.menu_delete:
			Uri wordUri = ContentUris.withAppendedId(getIntent().getData(),
					info.id);
			getContentResolver().delete(wordUri, null, null);
			return true;
		}
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		text = new EditText(this);
		text.setSingleLine();
		text.setId(EDIT_ID);
		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(Word.CONTENT_URI);
		}

		Cursor cursor = managedQuery(getIntent().getData(), new String[] {
				Word._ID, Word.WORD }, null, null, Word.DEFAULT_SORT_ORDER);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, cursor,
				new String[] { Word.WORD }, new int[] { android.R.id.text1 });
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
		getListView().setOnCreateContextMenuListener(this);

		if (cursor.getCount() == 0) {
			this.showDialog(DIALOG_ID_NO_WORDS);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
			AdapterView.AdapterContextMenuInfo info;
			try {
				info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			} catch (ClassCastException e) {
				Log.e(LOG_TAG, "bad menuInfo", e);
				return;
			}

			Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
			if (cursor == null) {
				// For some reason the requested item isn't available, do
				// nothing
				return;
			}
			this.getMenuInflater().inflate(R.menu.wordlist_context, menu);
			// Setup the menu header
			menu.setHeaderTitle(cursor.getString(1));
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_ID_CLICK: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(text);
			builder.setTitle(R.string.edit_word);
			builder.setNegativeButton(R.string.delete, this);
			builder.setNeutralButton(android.R.string.cancel, this);
			builder.setPositiveButton(android.R.string.ok, this);
			dialog = builder.create();
			break;
		}
		case DIALOG_ID_ADD: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			EditText text = new EditText(this);
			text.setSingleLine();
			text.setId(EDIT_ID);
			builder.setView(text);
			builder.setTitle(R.string.new_word);
			builder.setNeutralButton(android.R.string.cancel, this);
			builder.setPositiveButton(android.R.string.ok, this);
			dialog = builder.create();
			break;
		}
		case DIALOG_ID_NO_WORDS: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.wordlist_no_words);
			builder.setNeutralButton(android.R.string.ok, this);
			dialog = builder.create();
			break;
		}
		case DIALOG_ID_IMPORT: {
			ImportListener listener = new ImportListener();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			ViewGroup view = (ViewGroup) this.getLayoutInflater().inflate(
					R.layout.dialog_io, null);
			builder.setView(view);
			builder.setPositiveButton(R.string.IMPORT, listener);
			builder.setNeutralButton(android.R.string.cancel, listener);
			dialog = builder.create();
			((Button) view.findViewById(R.id.dialog_io_browse))
					.setOnClickListener(listener);
			importFilename = (TextView) view
					.findViewById(R.id.dialog_io_filename);
			importFilename.setText(Constants.DEFAULT_FILE_LOCATION);
			((CheckBox) view.findViewById(R.id.dialog_io_overwrite))
					.setText(R.string.IMPORT_OVERWRITE);
			break;
		}
		case DIALOG_ID_EXPORT: {
			ExportListener listener = new ExportListener();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			ViewGroup view = (ViewGroup) this.getLayoutInflater().inflate(
					R.layout.dialog_io, null);
			builder.setView(view);
			builder.setPositiveButton(R.string.EXPORT, listener);
			builder.setNeutralButton(android.R.string.cancel, listener);
			dialog = builder.create();
			((Button) view.findViewById(R.id.dialog_io_browse))
					.setOnClickListener(listener);
			exportFilename = (TextView) view
					.findViewById(R.id.dialog_io_filename);
			exportFilename.setText(Constants.DEFAULT_FILE_LOCATION);
			break;
		}
		default:
			dialog = super.onCreateDialog(id);
			break;
		}
		return dialog;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.wordlist_options, menu);
		menu.findItem(R.id.menu_insert).setIcon(android.R.drawable.ic_menu_add);
		menu.findItem(R.id.menu_quit).setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		menu.findItem(R.id.menu_import).setIcon(
				android.R.drawable.ic_menu_upload);
		menu.findItem(R.id.menu_export)
				.setIcon(android.R.drawable.ic_menu_save);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * called when editing a word in the list
	 */
	public void onItemClick(AdapterView<?> data, View view, int position,
			long rowid) {
		index = rowid;
		text.setText(((TextView) view).getText());
		this.showDialog(DIALOG_ID_CLICK);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_quit:
			this.finish();
			return true;
		case R.id.menu_insert:
			this.showDialog(DIALOG_ID_ADD);
			return true;
		case R.id.menu_import:
			this.showDialog(DIALOG_ID_IMPORT);
			return true;
		case R.id.menu_export:
			this.showDialog(DIALOG_ID_EXPORT);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DIALOG_ID_ADD: {
			index = INSERT_INDEX;
			EditText text = (EditText) ((AlertDialog) dialog)
					.findViewById(EDIT_ID);
			text.setText("");
			break;
		}
		case DIALOG_ID_CLICK:
			break;
		default:
			break;
		}
	}
}
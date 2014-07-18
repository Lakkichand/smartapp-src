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

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/**
 * 
 * @author Brendan Dahl
 *
 * ContentProvider for a sql database of words
 */
public class WordDictionaryProvider extends ContentProvider {

	/**
	 * used when the logger engine is called
	 */
    private static final String TAG = "WordDictionaryProvider";

    private static final String DATABASE_NAME = "wordsDictionary.db";
    private static final int DATABASE_VERSION = 1;
    private static final String WORDS_TABLE_NAME = "words";

    private static HashMap<String, String> sWordsProjectionMap;

    private static final int WORDS = 1;
    private static final int WORD_ID = 2;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + WORDS_TABLE_NAME + " ("
                    + Word._ID + " INTEGER PRIMARY KEY,"
                    + Word.WORD + " TEXT,"
                    + Word.CREATED_DATE + " INTEGER,"
                    + Word.MODIFIED_DATE + " INTEGER"
                    + ");");
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+WORDS_TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case WORDS:
            qb.setTables(WORDS_TABLE_NAME);
            qb.setProjectionMap(sWordsProjectionMap);
            break;

        case WORD_ID:
            qb.setTables(WORDS_TABLE_NAME);
            qb.setProjectionMap(sWordsProjectionMap);
            qb.appendWhere(Word._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Word.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case WORDS:
            return Word.CONTENT_TYPE;

        case WORD_ID:
            return Word.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != WORDS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(Word.CREATED_DATE) == false) {
            values.put(Word.CREATED_DATE, now);
        }

        if (values.containsKey(Word.MODIFIED_DATE) == false) {
            values.put(Word.MODIFIED_DATE, now);
        }

        validateWord(values);
        if (!values.containsKey(Word.WORD)) {
            throw new SQLException("Failed to insert row into " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(WORDS_TABLE_NAME, Word.WORD, values);
        if (rowId > 0) {
            Uri wordUri = ContentUris.withAppendedId(Word.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(wordUri, null);
            return wordUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case WORDS:
            count = db.delete(WORDS_TABLE_NAME, where, whereArgs);
            break;

        case WORD_ID:
            String wordId = uri.getPathSegments().get(1);
            count = db.delete(WORDS_TABLE_NAME, Word._ID + "=" + wordId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private void validateWord(ContentValues values) {
        if (values.containsKey(Word.WORD)) {
        	String str = (String)values.get(Word.WORD);
        	String str2 = "";
        	for (int i = 0; i < str.length() && str2.length() <= 10; i++) {
        		Character c = str.charAt(i);
        		if (Character.isLetter(c)) {
            		str2 += Character.toUpperCase(c);
        		}
        	}
        	if (str2 != null && str2.trim().length() >= 4) {
	        	values.put(Word.WORD, str2);
        	} else {
        		values.remove(Word.WORD);
        	}
        }
    }
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        Long now = Long.valueOf(System.currentTimeMillis());
        if (values.containsKey(Word.MODIFIED_DATE) == false) {
            values.put(Word.MODIFIED_DATE, now);
        }
        validateWord(values);
        if (!values.containsKey(Word.WORD)) {
        	return 0;
        }
        switch (sUriMatcher.match(uri)) {
        case WORDS:
            count = db.update(WORDS_TABLE_NAME, values, where, whereArgs);
            break;

        case WORD_ID:
            String wordId = uri.getPathSegments().get(1);
            count = db.update(WORDS_TABLE_NAME, values, Word._ID + "=" + wordId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Word.AUTHORITY, "words", WORDS);
        sUriMatcher.addURI(Word.AUTHORITY, "words/#", WORD_ID);

        sWordsProjectionMap = new HashMap<String, String>();
        sWordsProjectionMap.put(Word._ID, Word._ID);
        sWordsProjectionMap.put(Word.WORD, Word.WORD);
        sWordsProjectionMap.put(Word.CREATED_DATE, Word.CREATED_DATE);
        sWordsProjectionMap.put(Word.MODIFIED_DATE, Word.MODIFIED_DATE);
    }
    public static final class Word implements BaseColumns {
        public static final String AUTHORITY = "com.brendan.dahl.wordsearch.provider.words";
        // This class cannot be instantiated
        private Word() {}

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/words");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of words.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.dahl.brendan.word";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single word.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.dahl.brendan.word";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "word DESC";

        /**
         * The word
         * <P>Type: TEXT</P>
         */
        public static final String WORD = "word";

        /**
         * The timestamp for when the word was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the word was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }
}

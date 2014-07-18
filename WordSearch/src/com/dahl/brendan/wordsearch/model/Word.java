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

package com.dahl.brendan.wordsearch.model;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author Brendan Dahl
 *
 * this class stores a single instance of a word in the game
 *
 */
public class Word implements Parcelable {
	/**
	 * string contents of the word
	 */
	final private String string;
	/**
	 * point in the game grid where the word begins
	 */
	final private Point pointStart;
	/**
	 * point in the game grid where the word ends
	 */
	final private Point pointEnd;
	public static final Parcelable.Creator<Word> CREATOR
            = new Parcelable.Creator<Word>() {
        public Word createFromParcel(Parcel in) {
            return new Word(in);
        }

        public Word[] newArray(int size) {
            return new Word[size];
        }
    };
	private Word(Parcel in) {
    	string = in.readString();
		this.pointStart = new Point(in.readInt(), in.readInt());
		this.pointEnd = new Point(in.readInt(), in.readInt());
    }
	public Word(String string2) {
		this.string = new String(string2);
		this.pointStart = new Point();
		this.pointEnd = new Point();
	}
	public Word(String string, Point pointStart, Point pointEnd) {
		this.string = string;
		this.pointStart = pointStart;
		this.pointEnd = pointEnd;
	}
	public Word(Word word) {
		this.string = new String(word.getString());
		this.pointStart = new Point(word.getPointStart());
		this.pointEnd = new Point(word.getPointEnd());
	}
	public int describeContents() {
		return 0;
	}
	public Point getPointEnd() {
		return pointEnd;
	}
    public Point getPointStart() {
		return pointStart;
	}

    public String getString() {
		return string;
	}
    
    public String toString() {
		return this.string + "; s = "+pointStart+", e = "+pointEnd+"\n";
	}
	public void writeToParcel(Parcel out, int flags) {
    	out.writeString(string);
    	out.writeInt(pointStart.x);
    	out.writeInt(pointStart.y);
    	out.writeInt(pointEnd.x);
    	out.writeInt(pointEnd.y);
    }
}

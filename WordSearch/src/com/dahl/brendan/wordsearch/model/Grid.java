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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

import com.dahl.brendan.wordsearch.model.dictionary.IDictionary;

/**
 * 
 * @author Brendan Dahl
 *
 * this class stores every required for a grid in the word search game
 * 
 * this class also handles the generation of new grids
 */
public class Grid implements Parcelable {
	final static private Random random = new Random();
	final static private Point deltaNN = new Point(-1, -1);
	final static private Point deltaNP = new Point(-1, 1);
	final static private Point deltaPN = new Point(1, -1);
	final static private Point deltaPP = new Point(1, 1);
	final static private Point deltaPZ = new Point(1, 0);
	final static private Point deltaZP = new Point(0, 1);
	final static private Point deltaNZ = new Point(-1, 0);
	final static private Point deltaZN = new Point(0, -1);
//	private static final String LOG_TAG = "GRID";

	private static boolean attemptWord(Word word, Grid grid) {
		// Log.v(LOG_TAG, "attemptAddWord: "+word.toString());
		Integer wordIndexStart = random.nextInt(word.getString().length());
		Integer wordIndex = wordIndexStart;
		do {
			// look at best points for letter in word
			for (Point point : grid.getPointsForLetter(word.getString().charAt(
					wordIndex))) {
				if (attemptWordPoint(word, grid, wordIndex, point)) {
					return true;
				}
			}
			wordIndex = (wordIndex + 1) % word.getString().length();
		} while (wordIndex != wordIndexStart);
		do {
			// look for random points
			for (int count = 0; count < 10; count++) {
				Point point = new Point(random.nextInt(grid.size), random
						.nextInt(grid.size));
				if (attemptWordPoint(word, grid, wordIndex, point)) {
					return true;
				}
			}
			wordIndex = (wordIndex + 1) % word.getString().length();
		} while (wordIndex != wordIndexStart);
		return false;
	}

	private static boolean attemptWordDelta(Word word, Grid grid,
			int wordIndex, Point point, Point delta) {
		word.getPointStart().x = (delta.x * -1) * wordIndex + point.x;
		word.getPointStart().y = (delta.y * -1) * wordIndex + point.y;
		return grid.validateWord(word, delta);
	}

	private static boolean attemptWordPoint(Word word, Grid grid,
			int wordIndex, Point point) {
		if (attemptWordDelta(word, grid, wordIndex, point, deltaNN)) {
			return true;
		}
		if (attemptWordDelta(word, grid, wordIndex, point, deltaNP)) {
			return true;
		}
		if (attemptWordDelta(word, grid, wordIndex, point, deltaPN)) {
			return true;
		}
		if (attemptWordDelta(word, grid, wordIndex, point, deltaPP)) {
			return true;
		}
		if (attemptWordDelta(word, grid, wordIndex, point, deltaZP)) {
			return true;
		}
		if (attemptWordDelta(word, grid, wordIndex, point, deltaPZ)) {
			return true;
		}
		if (attemptWordDelta(word, grid, wordIndex, point, deltaZN)) {
			return true;
		}
		if (attemptWordDelta(word, grid, wordIndex, point, deltaNZ)) {
			return true;
		}
		return false;
	}

	public static Grid generateGrid(IDictionary dic, Integer maxWords,
			Integer minLength, int size) {
		// Log.v(LOG_TAG, "generatedGrid:");
		Grid grid;
		int tries = 0;
		do {
			grid = new Grid(size);
			int maxLength = size;
			String word;
			int missedCount = 0;
			for (int wordsAdded = 0; wordsAdded < maxWords
					&& (word = dic.getNextWord(minLength, maxLength)) != null
					&& minLength != maxLength;) {
				if (!grid.wordExists(word)) {
					Word word2 = new Word(word, new Point(), new Point());
					if (attemptWord(word2, grid)) {
						// Log.v(LOG_TAG, grid.toString());
						grid.addWord(word2);
						wordsAdded++;
					} else {
						// Log.v(LOG_TAG, "Word Couldn't Fit");
						missedCount++;
						maxLength--;
					}
				} else {
					missedCount++;
				}
				if (missedCount == 2) {
					wordsAdded++;
					missedCount = 0;
				}
			}
			grid.fillEmpty();
			tries++;
		} while(grid.hasDups() && tries <= 2);
		return grid;
	}

	private HashMap<Character, LinkedList<Point>> letterPoints = new HashMap<Character, LinkedList<Point>>();
	private final Character[] gridInternals;
	public final Integer size;
	private final List<Word> wordsHidden = new LinkedList<Word>();
	private final List<Word> wordsFound = new LinkedList<Word>();
	private boolean replaying = false;

	public static final Parcelable.Creator<Grid> CREATOR
            = new Parcelable.Creator<Grid>() {
        public Grid createFromParcel(Parcel in) {
            return new Grid(in);
        }

        public Grid[] newArray(int size) {
            return new Grid[size];
        }
    };

	private Grid(Integer size) {
		// Log.v(LOG_TAG, "Grid created size: "+size);
		this.size = size;
		this.gridInternals = new Character[size * size];
	}

	private Grid(Parcel in) {
    	this.size = in.readInt();
    	in.readTypedList(wordsHidden, Word.CREATOR);
    	in.readTypedList(wordsFound, Word.CREATOR);
    	this.gridInternals = new Character[size*size];
    	for (int index = 0; index < this.gridInternals.length; index++) {
    		this.gridInternals[index] = (char)in.readByte();
    	}
    	this.replaying = Boolean.valueOf(in.readString());
    }

	private void addWord(Word word) {
		// Log.v(LOG_TAG, "addWord: "+word.toString());
		wordsHidden.add(word);
		Point point = new Point(word.getPointStart());
		Point delta = Selection.getDeltas(word.getPointStart(), word
				.getPointEnd());
		for (int i = 0; i < word.getString().length(); i++) {
			this.setLetterAt(point, word.getString().charAt(i));
			point.offset(delta.x, delta.y);
		}
	}

	public int describeContents() {
		return 0;
	}

	private void fillEmpty() {
		// Log.v(LOG_TAG, "fillEmpty");
		LinkedList<Character> letters = new LinkedList<Character>(
				this.letterPoints.keySet());
//		Log.v(LOG_TAG, "letters:"+letters);
//		Log.v(LOG_TAG, "random:"+random);
		for (int i = 0; i < gridInternals.length; i++) {
			if (gridInternals[i] == null) {
				if (letters.size() != 0) {
					gridInternals[i] = letters.get(random.nextInt(letters.size()));
				} else {
					gridInternals[i] = ' ';
				}
			}
		}
		this.letterPoints.clear();
	}

	public Character getLetterAt(Point point) {
		return gridInternals[point.x + point.y * size];
	}

	private LinkedList<Point> getPointsForLetter(char charAt) {
		LinkedList<Point> points = letterPoints.get(charAt);
		if (points == null) {
			points = new LinkedList<Point>();
		}
		return points;
	}

	public int getSize() {
		return size;
	}

	public List<String> getWordFound() {
		LinkedList<String> lists = new LinkedList<String>();
		for (Word word : wordsFound) {
			lists.add(new String(word.getString()));
		}
		return lists;
	}

	public List<String> getWordList() {
		LinkedList<String> lists = new LinkedList<String>();
		for (Word word : wordsHidden) {
			lists.add(new String(word.getString()));
		}
		return lists;
	}

	public int getWordListLength() {
		return wordsHidden.size() + wordsFound.size();
	}

	public boolean isRunning() {
		return wordsHidden.size() != 0;
	}

	public List<Word> getWordsFound() {
		List<Word> lists = new LinkedList<Word>();
		for (Word word : wordsFound) {
			Word word2 = new Word(word.getString(), word.getPointStart(), word.getPointEnd());
			lists.add(word2);
		}
		return lists;
	}
	
	final public String guessWord(Point pointStart, Point pointEnd) {
		for (int i = 0; i < wordsHidden.size(); i++) {
			Word word = wordsHidden.get(i);
			if ((word.getPointStart().equals(pointStart) && word.getPointEnd()
					.equals(pointEnd))
					|| (word.getPointEnd().equals(pointStart) && word
							.getPointStart().equals(pointEnd))) {
				wordsHidden.remove(i);
				wordsFound.add(word);
				return word.getString();
			}
		}
		return null;
	}

	private boolean hasDupDelta(String subWord, Point location, Point delta) {
    	if (subWord.charAt(0) == this.getLetterAt(location)) {
    		if (subWord.length() == 1) {
    			return true;
    		} else {
        		location.offset(delta.x, delta.y);
        		return hasDupDelta(subWord.substring(1), location, delta);
    		}
    	}
    	return false;
    }
    private boolean hasDups() {
    	for (Word word : this.wordsHidden) {
    		LinkedList<Point> points = getPointsForLetter(word.getString().charAt(0));
    		points.remove(word.getPointStart());
    		for (Point p : points) {
    			int len = word.getString().length();
    			Point test = new Point();
    			test.set(p.x, p.y);
    			test.offset(deltaNN.x*len, deltaNN.y*len);
    			if (Selection.isValidPoint(test, getSize()) && hasDupDelta(word.getString(), new Point(p), deltaNN)) {
    				return true;
    			}
    			test.set(p.x, p.y);
    			test.offset(deltaNP.x*len, deltaNP.y*len);
    			if (Selection.isValidPoint(test, getSize()) && hasDupDelta(word.getString(), new Point(p), deltaNP)) {
    				return true;
    			}
    			test.set(p.x, p.y);
    			test.offset(deltaPN.x*len, deltaPN.y*len);
    			if (Selection.isValidPoint(test, getSize()) && hasDupDelta(word.getString(), new Point(p), deltaPN)) {
    				return true;
    			}
    			test.set(p.x, p.y);
    			test.offset(deltaPP.x*len, deltaPP.y*len);
    			if (Selection.isValidPoint(test, getSize()) && hasDupDelta(word.getString(), new Point(p), deltaPP)) {
    				return true;
    			}
    			test.set(p.x, p.y);
    			test.offset(deltaPZ.x*len, deltaPZ.y*len);
    			if (Selection.isValidPoint(test, getSize()) && hasDupDelta(word.getString(), new Point(p), deltaPZ)) {
    				return true;
    			}
    			test.set(p.x, p.y);
    			test.offset(deltaZP.x*len, deltaZP.y*len);
    			if (Selection.isValidPoint(test, getSize()) && hasDupDelta(word.getString(), new Point(p), deltaZP)) {
    				return true;
    			}
    			test.set(p.x, p.y);
    			test.offset(deltaPP.x*len, deltaPP.y*len);
    			if (Selection.isValidPoint(test, getSize()) && hasDupDelta(word.getString(), new Point(p), deltaPP)) {
    				return true;
    			}
    			test.set(p.x, p.y);
    			test.offset(deltaNZ.x*len, deltaNZ.y*len);
    			if (Selection.isValidPoint(test, getSize()) && hasDupDelta(word.getString(), new Point(p), deltaNZ)) {
    				return true;
    			}
    			test.set(p.x, p.y);
    			test.offset(deltaZN.x*len, deltaZN.y*len);
    			if (Selection.isValidPoint(test, getSize()) && hasDupDelta(word.getString(), new Point(p), deltaZN)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }

    public boolean isReplaying() {
		return replaying;
	}
    
    final public void reset() {
		replaying = true;
		this.wordsHidden.addAll(this.wordsFound);
		this.wordsFound.clear();
	}
    
    private void setLetterAt(Point point, Character letter) {
		int index = point.x + point.y * size;
		if (gridInternals[index] == null) {
			LinkedList<Point> points = getPointsForLetter(letter);
			points.add(point);
			letterPoints.put(letter, points);
		}
		gridInternals[index] = letter;
	}
    
    public String toString() {
		String str = "";
		for (int i = 0; i < gridInternals.length; i++) {
			str += gridInternals[i];
			if ((i + 1) % size == 0) {
				str += "\n";
			} else {
				str += "_";
			}
		}
		for (Word word : wordsHidden) {
			str += word.toString();
		}
		return str;
	}

	private boolean validateWord(Word word, Point delta) {
		if (!Selection.isValidPoint(word.getPointStart(), size)) {
			return false;
		}
		int length = word.getString().length();
		word.getPointEnd().x = word.getPointStart().x + delta.x * (length - 1);
		word.getPointEnd().y = word.getPointStart().y + delta.y * (length - 1);
		if (!Selection.isValidPoint(word.getPointEnd(), size)) {
			return false;
		}
		Point point = new Point(word.getPointStart());
		for (int index = 0; index < word.getString().length(); index++, point
				.offset(delta.x, delta.y)) {
			if (!Selection.isValidPoint(point, size)) {
				return false;
			}
			Character letter = this.getLetterAt(point);
			if (letter != null && letter != word.getString().charAt(index)) {
				return false;
			}
		}
		return true;
	}

	private boolean wordExists(String str) {
		for (Word word : wordsHidden) {
			if (word.getString().equals(str)) {
				return true;
			}
		}
		return false;
	}

	public void writeToParcel(Parcel out, int flags) {
    	out.writeInt(this.size);
    	out.writeTypedList(wordsHidden);
    	out.writeTypedList(wordsFound);
    	for (Character c : this.gridInternals) {
    		out.writeByte((byte)c.charValue());
    	}
    	out.writeString(Boolean.toString(this.replaying));
    }
}

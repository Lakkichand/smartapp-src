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

import com.dahl.brendan.wordsearch.model.Grid;
import com.dahl.brendan.wordsearch.model.Theme;

/**
 * handles all interactions that affect the word list part of the wordsearch activity
 * 
 * @author dahlb
 *
 */
public interface IWordBoxController {
	/**
	 * 
	 * @param charSequence sets the letter to show the user which letter is being touched
	 * 						null to hide the preview letter
	 */
	public void setLetter(CharSequence charSequence);

	/**
	 * removes a word from the list of words to find
	 * 
	 * @param str word to remove the list of words
	 * @return number of words left to find
	 */
	public void wordFound(String str);

	/**
	 * resets the list of words available to the user
	 * 
	 * @param wordList new list of available words
	 */
	public void resetWords(Grid grid);

	/**
	 * applies a theme to the implementation's views
	 * 
	 * @param theme new theme to apply
	 */
	public void updateTheme(Theme theme);
}

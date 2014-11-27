package com.emoji;

import java.util.ArrayList;
import java.util.HashMap;

/***
 * 
 * 表情控件传入参数实体
 * 
 * @author Zhengcw
 * 
 */
public class EmojiInfo {
	// 表情的类型
	private int emojiType;
	// 表情数据
	private ArrayList<HashMap<String, Object>> emojiData;
	// 表情显示的列数
	private int emojiColumns;
	// 表情显示的列数
	private int emojiLines;
	// 表情选择栏的图标
	private int tabIconId;

	public int getEmojiType() {
		return emojiType;
	}

	public void setEmojiType(int emojiType) {
		this.emojiType = emojiType;
	}

	public ArrayList<HashMap<String, Object>> getEmojiData() {
		return emojiData;
	}

	public void setEmojiData(ArrayList<HashMap<String, Object>> emojiData) {
		this.emojiData = emojiData;
	}

	public int getEmojiColumns() {
		return emojiColumns;
	}

	public void setEmojiColumns(int emojiColumns) {
		this.emojiColumns = emojiColumns;
	}

	public int getEmojiLines() {
		return emojiLines;
	}

	public void setEmojiLines(int emojiLines) {
		this.emojiLines = emojiLines;
	}

	public int getTabIconId() {
		return tabIconId;
	}

	public void setTabIconId(int tabIconId) {
		this.tabIconId = tabIconId;
	}

}


package com.go.util;

import java.util.ArrayList;

import android.graphics.Paint;

public class TextUtil {

	/***
	 * 文字排版(（文字过长时可优化成二维整型数组）)
	 */
	public static String[] typeString(String str, int w, Paint paint) {
		if (str == null) {
			return null;
		}
		int strLength = str.length();
		float fontW = paint.measureText("宽"); // 抛过==0的异常
		if (fontW <= 1) {
			if (paint.getTextSize() >= 1) {
				fontW = paint.getTextSize();
			} else {
				fontW = 32;
			}
		}

		int oneLineNum = (int) (w / fontW);

		ArrayList<String> textList;
		textList = new ArrayList<String>();
		int startIndex = 0;
		int endIndex = oneLineNum;
		int tempTextW = 0;
		String atom = null;

		if (endIndex > strLength && strLength > 0) {
			endIndex = strLength - 1;
		}

		try {
			while (endIndex < strLength) {
				tempTextW = (int) paint.measureText(str.substring(startIndex, endIndex));
				if (tempTextW <= w) {
					while (endIndex < strLength && tempTextW <= w) {
						atom = str.substring(endIndex - 1, endIndex);
						if (endIndex <= strLength && tempTextW <= w) {
							endIndex++;
							tempTextW += (int) paint.measureText(atom);
						} else {
							endIndex--;
							tempTextW -= (int) paint.measureText(atom);
							break;
						}
					}
					// 检查行尾断词
					if (atom != null) {
						char ch = atom.charAt(0);
						if ((ch > 97 && ch < 122) || (ch > 65 && ch < 122)) {
							int dec = 0;
							int decW = 0;
							String letter = null;
							boolean deal = true;
							while ((ch > 97 && ch < 122) || (ch > 65 && ch < 122)) {
								letter = str.substring(endIndex - 1 - dec, endIndex - dec);
								ch = letter.charAt(0);
								dec++;
								decW += (int) paint.measureText(letter);
								if (decW > w) {
									deal = false;
									break;
								}
							}
							if (dec > 0 && null != letter) {
								dec--;
								decW -= (int) paint.measureText(atom);
							}
							if (deal) {
								endIndex -= dec;
								tempTextW -= decW;
							}
						}
					}
				} else {
					while (tempTextW > w) {
						atom = str.substring(endIndex - 1, endIndex);
						if (endIndex > startIndex) {
							endIndex--;
							tempTextW -= (int) paint.measureText(atom);
						} else {
							break;
						}
						// 检查行尾断词
						if (atom != null) {
							char ch = atom.charAt(0);
							if ((ch > 97 && ch < 122) || (ch > 65 && ch < 122)) {
								int dec = 0;
								int decW = 0;
								String letter = null;
								boolean deal = true;
								while ((ch > 97 && ch < 122) || (ch > 65 && ch < 122)) {
									letter = str.substring(endIndex - 1 - dec, endIndex - dec);
									ch = letter.charAt(0);
									dec++;
									decW += (int) paint.measureText(letter);
									if (decW > w) {
										deal = false;
										break;
									}
								}
								if (dec > 0 && null != letter) {
									dec--;
									decW -= (int) paint.measureText(atom);
								}
								if (deal) {
									endIndex -= dec;
									tempTextW -= decW;
								}
							}
						}
					}
				}
				textList.add(str.substring(startIndex, endIndex));
				startIndex = endIndex;
				endIndex = startIndex + oneLineNum;
			}
			if (strLength > startIndex) {
				textList.add(str.substring(startIndex, strLength));
			}

			String[] text = new String[textList.size()];
			for (int i = 0; i < text.length; i++) {
				text[i] = textList.get(i).toString();
			}

			return text;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// /***
	// * 文字排版(（文字过长时可优化成二维整型数组）)
	// */
	// public static String[] typeString(String str, int w, Paint paint) {
	// if (str == null && Width > 0) {
	// return null;
	// }
	// int strLength = str.length();
	// int fontW = (int) paint.measureText("宽");
	// int oneLineNum = w / fontW;
	//
	// ArrayList<String> textList;
	// textList = new ArrayList<String>();
	// int startIndex = 0;
	// int endIndex = oneLineNum;
	// int tempTextW = 0;
	//
	// while (endIndex < strLength) {
	// tempTextW = (int) paint.measureText(str.substring(startIndex,endIndex));
	// if (tempTextW <= w) {
	// while (tempTextW <= w) {
	// if (endIndex <= strLength && tempTextW <= w) {
	// endIndex++;
	// tempTextW += (int) paint.measureText(str.substring(endIndex-1,
	// endIndex));
	// } else {
	// endIndex--;
	// tempTextW -= (int) paint.measureText(str.substring(endIndex-1,endIndex));
	// break;
	// }
	// }
	// } else {
	// while (tempTextW > w) {
	// if (endIndex > startIndex) {
	// endIndex--;
	// tempTextW -= (int) paint.measureText(str.substring(endIndex-1,endIndex));
	// } else {
	// break;
	// }
	// }
	// }
	// textList.add(str.substring(startIndex, endIndex));
	// startIndex = endIndex;
	// endIndex = startIndex + oneLineNum;
	// }
	// if (strLength > startIndex) {
	// textList.add(str.substring(startIndex, strLength));
	// }
	//
	// String[] text = new String[textList.size()];
	// for (int i = 0; i < text.length; i++) {
	// text[i] = textList.get(i).toString();
	// }
	//
	// return text;
	// }

}

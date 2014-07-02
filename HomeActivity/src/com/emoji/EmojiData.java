package com.emoji;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.youle.gamebox.ui.R;

import android.content.Context;

public class EmojiData {

	private ArrayList<HashMap<String, Object>> emojiData;
	private Map<String, String> faseMap = new HashMap<String, String>();;
	private ArrayList<HashMap<String, Object>> animationData;

	private int[] emojiId = new int[] { R.drawable.face1, R.drawable.face2,
			R.drawable.face3, R.drawable.face4, R.drawable.face5,
			R.drawable.face6, R.drawable.face7, R.drawable.face8,
			R.drawable.face9, R.drawable.face10, R.drawable.face11,
			R.drawable.face12, R.drawable.face13, R.drawable.face14,
			R.drawable.face15, R.drawable.face16, R.drawable.face17,
			R.drawable.face18, R.drawable.face19, R.drawable.face20,
			R.drawable.face21, R.drawable.face22, R.drawable.face23,
			R.drawable.face24, R.drawable.face25, R.drawable.face26,
			R.drawable.face27, R.drawable.face28, R.drawable.face29,
			R.drawable.face30, R.drawable.face31, R.drawable.face32,
			R.drawable.face33, R.drawable.face34, R.drawable.face35,
			R.drawable.face36, R.drawable.face37, R.drawable.face38,
			R.drawable.face39, R.drawable.face40, R.drawable.face41,
			R.drawable.face42, R.drawable.face43, R.drawable.face44,
			R.drawable.face45, R.drawable.face46, R.drawable.face47,
			R.drawable.face48, R.drawable.face49, R.drawable.face50,
			R.drawable.face51, R.drawable.face52, R.drawable.face53,
			R.drawable.face54, R.drawable.face55, R.drawable.face56,
			R.drawable.face57, R.drawable.face58, R.drawable.face59,
			R.drawable.face60, R.drawable.face61, R.drawable.face62,
			R.drawable.face63, R.drawable.face64, R.drawable.face65,
			R.drawable.face66, R.drawable.face67, R.drawable.face68 };
	private static EmojiData instance = null;

	public static EmojiData getInstance(Context context) {
		if (instance == null) {
			instance = new EmojiData(context);
		}
		return instance;
	}

	public ArrayList<HashMap<String, Object>> getEmojiData() {
		return emojiData;
	}

	/**
	 * 判段是否存在表情
	 * 
	 * @param faceStr
	 * @return
	 */
	public boolean isExit(String faceStr) {
		if (faseMap.containsKey(faceStr)) {
			return true;
		}
		return false;
	}

	private EmojiData(Context context) {
		final String[] faceArray = context.getResources().getStringArray(
				R.array.face_arr);
		emojiData = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < faceArray.length; i++) {
			faseMap.put(faceArray[i], null);// 为空即可
			HashMap<String, Object> map = new HashMap<String, Object>();
			try {
				// Field f = (Field) R.drawable.class.getDeclaredField("face"
				// + (i + 1));
				// map.put("imageItem", f.getInt(R.drawable.class));
				map.put("imageItem", emojiId[i]);
				map.put("srcItem", "face" + (i + 1));
			} catch (SecurityException e) {

				e.printStackTrace();
			} catch (IllegalArgumentException e) {

				e.printStackTrace();
			}
			map.put("textItem", faceArray[i]);
			emojiData.add(map);
		}
		// 暂时没用到的一些
		// final String[] animationArray =
		// getResources().getStringArray(R.array.animation_arr);
		// animationData = new ArrayList<HashMap<String, Object>>();
		// for (int i = 0; i < animationArray.length; i++) {
		// HashMap<String, Object> map = new HashMap<String, Object>();
		// try {
		// Field f = (Field) R.drawable.class.getDeclaredField("animation" + (i
		// + 1));
		// map.put("imageItem", f.getInt(R.drawable.class));
		// } catch (SecurityException e) {
		//
		// e.printStackTrace();
		// } catch (NoSuchFieldException e) {
		//
		// e.printStackTrace();
		// } catch (IllegalArgumentException e) {
		//
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		//
		// e.printStackTrace();
		// }
		// map.put("textItem", faceArray[i]);
		// animationData.add(map);
		// }
	}

}

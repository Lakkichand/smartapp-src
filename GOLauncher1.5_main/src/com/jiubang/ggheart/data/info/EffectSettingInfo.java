package com.jiubang.ggheart.data.info;

import android.content.ContentValues;
import android.database.Cursor;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.data.tables.DynamicEffectTable;

public class EffectSettingInfo {
	public boolean mEnable;
	public int mScrollSpeed;
	public int mBackSpeed;
	public int mType;
	public int mEffectorType;
	public boolean mAutoTweakElasticity;
	public int[] mEffectCustomRandomEffects;

	// public int mEffectSelectItem;
	public EffectSettingInfo() {
		mEnable = true;
		mBackSpeed = 0;
		mScrollSpeed = 60;
		mType = 1;
		mEffectorType = 0;
		mAutoTweakElasticity = true;
		mEffectCustomRandomEffects = new int[] { -1 };
	}

	/**
	 * 加入键值对
	 * 
	 * @param values
	 *            键值对
	 */
	public void contentValues(ContentValues values) {
		if (null == values) {
			return;
		}
		values.put(DynamicEffectTable.EABLE, ConvertUtils.boolean2int(mEnable));
		values.put(DynamicEffectTable.SCROLLSPEED, mScrollSpeed);
		values.put(DynamicEffectTable.BACKSPEED, mBackSpeed);
		values.put(DynamicEffectTable.EFFECT, mType);
		values.put(DynamicEffectTable.EFFECTORTYPE, mEffectorType);
		values.put(DynamicEffectTable.AUTOTWEAKELASTICITY,
				ConvertUtils.boolean2int(mAutoTweakElasticity));
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < mEffectCustomRandomEffects.length; i++) {
			buffer.append(mEffectCustomRandomEffects[i]);
			buffer.append(";");
		}
		values.put(DynamicEffectTable.EFFECTORRANDOMITEMS, buffer.toString());
		// values.put(DynamicEffectTable.EFFECTORITEM, mEffectSelectItem);
	}

	/**
	 * 解析数据
	 * 
	 * @param cursor
	 *            数据集
	 */
	public boolean parseFromCursor(Cursor cursor) {
		if (null == cursor) {
			return false;
		}

		boolean bData = cursor.moveToFirst();
		if (bData) {
			int enableIndex = cursor.getColumnIndex(DynamicEffectTable.EABLE);
			int scrollspeedIndex = cursor.getColumnIndex(DynamicEffectTable.SCROLLSPEED);
			int backspeedIndex = cursor.getColumnIndex(DynamicEffectTable.BACKSPEED);
			int effectIndex = cursor.getColumnIndex(DynamicEffectTable.EFFECT);
			int effectorTypeIndex = cursor.getColumnIndex(DynamicEffectTable.EFFECTORTYPE);
			int tweakElasticityIndex = cursor
					.getColumnIndex(DynamicEffectTable.AUTOTWEAKELASTICITY);
			int effectRandomItemsIndex = cursor
					.getColumnIndex(DynamicEffectTable.EFFECTORRANDOMITEMS);
			// int effectItemIndex =
			// cursor.getColumnIndex(DynamicEffectTable.EFFECTORITEM);

			if (enableIndex >= 0) {
				mEnable = ConvertUtils.int2boolean(cursor.getInt(enableIndex));
			}

			if (scrollspeedIndex >= 0) {
				mScrollSpeed = cursor.getInt(scrollspeedIndex);
			}

			if (backspeedIndex >= 0) {
				mBackSpeed = cursor.getInt(backspeedIndex);
			}

			if (effectIndex >= 0) {
				mType = cursor.getInt(effectIndex);
			}

			if (effectorTypeIndex >= 0) {
				mEffectorType = cursor.getInt(effectorTypeIndex);
			}

			if (tweakElasticityIndex >= 0) {
				mAutoTweakElasticity = ConvertUtils
						.int2boolean(cursor.getInt(tweakElasticityIndex));
			}
			if (effectRandomItemsIndex >= 0) {
				String buff = cursor.getString(effectRandomItemsIndex);
				String[] items = buff.split(";");
				if (items != null) {
					mEffectCustomRandomEffects = new int[items.length];
					for (int i = 0; i < items.length; i++) {
						try {
							mEffectCustomRandomEffects[i] = Integer.valueOf(items[i]);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			// if(effectItemIndex >= 0)
			// {
			// mEffectSelectItem = cursor.getInt(effectItemIndex);
			// }
		}
		return bData;
	}

	// TODO 临时处理
	// 设置 UI 转换
	// 设置0---100
	// UI 0---2000ms
	public int getDuration() {
		int speed = mScrollSpeed;
		if (speed < 0 || speed > 100) {
			speed = 0;
		}
		// return 2000 * (100 - speed) / 100;
		final int duration = speed * speed / 10 - 20 * speed + 1200;
		return duration;
	}

	public int getOvershootAmount() {
		return mBackSpeed / 6; // 从[0, 100]转换为[0, 16]
	}
}

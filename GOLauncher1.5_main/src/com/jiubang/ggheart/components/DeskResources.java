package com.jiubang.ggheart.components;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
/**
 * 
 * @author jiangxuwen
 *
 */
public class DeskResources extends Resources implements ISelfObject {
	private String mLanguagePackage;
	private Resources mLanguageResources;

	public DeskResources(Resources resources) {
		super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());

		// CompatibilityInfo
		try {
			Field field = Resources.class.getDeclaredField("mCompatibilityInfo");
			field.setAccessible(true);
			Object object = field.get(resources);
			if (null != object) {
				field = Resources.class.getDeclaredField("mCompatibilityInfo");
				field.setAccessible(true);
				field.set(this, object);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		final DisplayMetrics metrics = resources.getDisplayMetrics();
		final DisplayMetrics localMetrics = getDisplayMetrics();
		if (metrics.density != localMetrics.density) {
			DrawUtils.setVirtualDensity(localMetrics.density);
			DrawUtils.setVirtualDensityDpi(localMetrics.densityDpi);
			// 在一些机型上面会出现density不相等的情况，这时候需要update
			updateConfiguration(getConfiguration(), metrics);
		}
		selfConstruct();
	}

	public void setLanguage(String languagePackage, Resources languageResources) {
		mLanguagePackage = languagePackage;
		mLanguageResources = languageResources;

		// mStringBlocks
		// try
		// {
		// Field field = AssetManager.class.getDeclaredField("mStringBlocks");
		// field.setAccessible(true);
		// Object object = field.get(mLanguageResources.getAssets());
		// if (null != object)
		// {
		// field = AssetManager.class.getDeclaredField("mStringBlocks");
		// field.setAccessible(true);
		// field.set(getAssets(), object);
		// }
		// }
		// catch (Exception e)
		// {
		// e.printStackTrace();
		// }
	}

	@Override
	public void selfConstruct() {

	}

	@Override
	public void selfDestruct() {
		mLanguagePackage = null;
		mLanguageResources = null;
	}

	@Override
	public CharSequence getText(int id) throws NotFoundException {
		if (null != mLanguageResources) {
			String resName = getResourceEntryName(id);
			CharSequence ret = getLanguageText(resName);
			if (null != ret) {
				return ret;
			}
		}
		return super.getText(id);
	}

	@Override
	public CharSequence[] getTextArray(int id) throws NotFoundException {
		CharSequence[] textArray = super.getTextArray(id);
		if (null != mLanguageResources) {
			for (int i = 0; i < textArray.length; i++) {
				String languageResName = replaceRegEx(textArray[i].toString());
				if (null != languageResName) {
					CharSequence languageResValue = getLanguageText(languageResName);
					if (null != languageResValue) {
						textArray[i] = languageResValue;
					}
				}
			}
		}
		return textArray;
	}

	@Override
	public String[] getStringArray(int id) throws NotFoundException {
		String[] stringArray = super.getStringArray(id);
		if (null != mLanguageResources) {
			for (int i = 0; i < stringArray.length; i++) {
				String languageResName = replaceRegEx(stringArray[i]);
				if (null != languageResName) {
					CharSequence languageResValue = getLanguageText(languageResName);
					if (null != languageResValue) {
						stringArray[i] = languageResValue.toString();
					}
				}
			}
		}
		return stringArray;
	}

	private CharSequence getLanguageText(String resName) {
		CharSequence ret = null;
		try {
			int remoteResId = mLanguageResources.getIdentifier(resName, "string", mLanguagePackage);
			ret = mLanguageResources.getText(remoteResId);
		} catch (NotFoundException e) {
			// e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 去掉特殊字符
	 * 
	 * @param resName
	 * @return
	 */
	private String replaceRegEx(String resName) {
		String regEx = "[×`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？ -]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(resName);
		String regDx = "^\\d";
		p = Pattern.compile(regDx);
		m = p.matcher(resName = m.replaceAll("_"));
		if (m.find()) {
			resName = '_' + resName;
		}
		return resName;
	}
}

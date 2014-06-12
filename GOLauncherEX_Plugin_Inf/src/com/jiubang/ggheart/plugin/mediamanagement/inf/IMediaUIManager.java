package com.jiubang.ggheart.plugin.mediamanagement.inf;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-16]
 */
public interface IMediaUIManager {
	public Context getContext();
	public MediaContext getMediaContext();

	public void setRootView(View view);
	public void setIndicatorSetting(String pkg);
	public void setIndicatorShowMode(String showMode);
	public void setIndicatorScrollH(Drawable drawable);
	public void setIndicatorScrollV(Drawable drawable);
	public void setIndicatorCurrentHor(Drawable drawable);
	public void setIndicatorHor(Drawable drawable);
	public void setIndicatorTextSize(float size);
	public void setIndicatorPos(String pos);
	public void setFontSize(int size);
	public void setFontType(Typeface typeface, int style);
	public void setTitleColor(int color);
	public void setTurnScreenDirection(int direction);
	public void setVerticalScrollEffect(int effect);
	public void setContainerHeight(int height);
	public void setContainerWidth(int width);
	public void setScreenWidth(int width);
	public void setScreenHeight(int height);
	public void setStandardIconSize(int size);
	public void setIconTextDst(int dst);
	public void setOrientationType(int type);
	public void setAppFuncBottomHeight(int height);
//	public void setMenuBgV(Drawable drawable);
//	public void setMenuBgH(Drawable drawable);
//	public void setMenuDividerV(Drawable drawable);
//	public void setMenuDividerH(Drawable drawable);
//	public void setMenuTextColor(int color);
//	public void setMenuItemSelectedBg(Drawable drawable);
	public void setActionBarBgV(Drawable drawable);
	public void setActionBarBgH(Drawable drawable);
	public void setActionBarBgDrawingWay(byte way);
//	public void setImageButtonIcon(Drawable drawable);
//	public void setImageButtonIconPressed(Drawable drawable);
//	public void setMusicButtonIcon(Drawable drawable);
//	public void setMusicButtonIconPressed(Drawable drawable);
//	public void setVideoButtonIcon(Drawable drawable);
//	public void setVideoButtonIconPressed(Drawable drawable);
	public void setHomeButtonIcon(Drawable drawable);
	public void setHomeButtonIconPressed(Drawable drawable);
	public void setMenuButtonIcon(Drawable drawable);
	public void setMenuButtonIconPressed(Drawable drawable);
	public void setSwitchButtonImageIcon(Drawable drawable);
	public void setSwitchButtonImageIconPressed(Drawable drawable);
	public void setSwitchButtonMusicIcon(Drawable drawable);
	public void setSwitchButtonMusicIconPressed(Drawable drawable);
	public void setSwitchButtonVideoIcon(Drawable drawable);
	public void setSwitchButtonVideoIconPressed(Drawable drawable);
	public void setShowStatusBar(boolean show);
	public void setCurrentThemePackage(String pkg);
	public void setGridEffector(int gridEffect);
	public void setCustomRandomEffects(int[] effects);
	public void setScrollLoop(int scrollLoop);
	public void setNeedHideMusicPlayer(boolean needHide);
	public void setImgBrowserAnimation(List<Animation> animations);
	
	public View getRootView();
	public String getIndicatorSetting();
	public Drawable getIndicatorScrollH();
	public Drawable getIndicatorScrollV();
	public Drawable getIndicatorCurrentHor();
	public Drawable getIndicatorHor();
	public float getIndicatorTextSize();
	public String getIndicatorPos();
	public int getFontSize();
	public Typeface getFontTypeface();
	public int getFontStyle();
	public int getTitleColor();
	public int getTurnScreenDirection();
	public int getVerticalScrollEffect();
	public int getContainerHeight();
	public int getContainerWidth();
	public int getScreenWidth();
	public int getScreenHeight();
	public int getStandardIconSize();
	public int getIconTextDst();
	public int getOrientationType();
	public int getAppFuncBottomHeight();
//	public Drawable getMenuBgV();
//	public Drawable getMenuBgH();
//	public Drawable getMenuDividerV();
//	public Drawable getMenuDividerH();
//	public int getMenuTextColor();
//	public Drawable getMenuItemSelectedBg();
	public Drawable getActionBarBgV();
	public Drawable getActionBarBgH();
	public byte getActionBarBgDrawingWay();
//	public Drawable getImageButtonIcon();
//	public Drawable getImageButtonIconPressed();
//	public Drawable getMusicButtonIcon();
//	public Drawable getMusicButtonIconPressed();
//	public Drawable getVideoButtonIcon();
//	public Drawable getVideoButtonIconPressed();
	public Drawable getHomeButtonIcon();
	public Drawable getHomeButtonIconPressed();
	public Drawable getMenuButtonIcon();
	public Drawable getMenuButtonIconPressed();
	public Drawable getSwitchButtonImageIcon();
	public Drawable getSwitchButtonImageIconPressed();
	public Drawable getSwitchButtonMusicIcon();
	public Drawable getSwitchButtonMusicIconPressed();
	public Drawable getSwitchButtonVideoIcon();
	public Drawable getSwitchButtonVideoIconPressed();
	public boolean isShowStatusBar();
	public String getCurrentThemePackage();
	public int getGridEffector();
	public int[] getCustomRandomEffects();
	public int getScrollLoop();
	public boolean isNeedHideMusicPlayer();
	public void onConfiguractionChange(int width, int height);
	public List<Animation> getImgBrowserAnimation();
	public boolean setFeature(int id, int param, Object... objects);
}

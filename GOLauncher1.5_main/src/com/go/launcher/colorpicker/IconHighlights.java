package com.go.launcher.colorpicker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;

public class IconHighlights {
	public static final int TYPE_DESKTOP = 1;
	public static final int TYPE_DOCKBAR = 2;
	public static final int TYPE_DRAWER = 3;

	public IconHighlights(Context context) {
		// TODO Auto-generated constructor stub
	}

	private static Drawable newSelector(/* Context context, */int selColor, int pressColor) {
		GradientDrawable mDrawPressed;
		GradientDrawable mDrawSelected;
		StateListDrawable drawable = new StateListDrawable();
		// int
		// selectedColor=AlmostNexusSettingsHelper.getHighlightsColorFocus(context);
		// int
		// pressedColor=AlmostNexusSettingsHelper.getHighlightsColor(context);
		int selectedColor = selColor;
		int pressedColor = pressColor;
		int stateFocused = android.R.attr.state_focused;
		int statePressed = android.R.attr.state_pressed;
		int stateWindowFocused = android.R.attr.state_window_focused;

		mDrawSelected = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
				selectedColor, selectedColor, selectedColor, selectedColor });
		mDrawSelected.setShape(GradientDrawable.RECTANGLE);
		mDrawSelected.setGradientType(GradientDrawable.RADIAL_GRADIENT);
		mDrawSelected.setGradientRadius((float) (Math.sqrt(2) * 60));
		mDrawSelected.setCornerRadius(8);

		mDrawPressed = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
				pressedColor, pressedColor, pressedColor, pressedColor });
		mDrawPressed.setStroke(1, 0x20000000);
		mDrawPressed.setShape(GradientDrawable.RECTANGLE);
		mDrawPressed.setGradientType(GradientDrawable.RADIAL_GRADIENT);
		mDrawPressed.setGradientRadius((float) (Math.sqrt(2) * 60));
		mDrawPressed.setCornerRadius(8);

		drawable.addState(new int[] { statePressed }, mDrawPressed);
		drawable.addState(new int[] { stateFocused, stateWindowFocused }, mDrawSelected);
		drawable.addState(new int[] { stateFocused, -stateWindowFocused }, null);
		drawable.addState(new int[] { -stateFocused, stateWindowFocused }, null);
		drawable.addState(new int[] { -stateFocused, -stateWindowFocused }, null);
		return drawable;
	}

	/*
	 * private static Drawable oldSelector(Context context, int type, int
	 * selColor, int pressColor){ // int
	 * selectedColor=AlmostNexusSettingsHelper.getHighlightsColorFocus(context);
	 * // int
	 * pressedColor=AlmostNexusSettingsHelper.getHighlightsColor(context); int
	 * pressedColor = pressColor; //ADW: Load the specified theme // String
	 * themePackage=AlmostNexusSettingsHelper.getThemePackageName(context,
	 * Launcher.THEME_DEFAULT); String themePackage =
	 * ThemeManager.getPackageNameFromSharedpreference(context); Resources
	 * themeResources = null;
	 * if(!themePackage.equals(ThemeManager.DEFAULT_THEME_PACKAGE)){
	 * PackageManager pm = context.getPackageManager(); try { themeResources =
	 * pm.getResourcesForApplication(themePackage); } catch
	 * (NameNotFoundException e) { // TODO Auto-generated catch block
	 * themeResources = context.getResources(); } }else{ themeResources =
	 * context.getResources(); } Drawable drawable = null; //use_drawer_icons_bg
	 * if(themeResources != null){ boolean use_drawer_icons_bgs = false; if(type
	 * == TYPE_DRAWER){ int use_drawer_icons_bgs_id =
	 * themeResources.getIdentifier("use_drawer_icons_bg", "bool",
	 * themePackage); if(use_drawer_icons_bgs_id != 0){ use_drawer_icons_bgs =
	 * themeResources.getBoolean(use_drawer_icons_bgs_id); }
	 * if(use_drawer_icons_bgs){ int resource_id =
	 * themeResources.getIdentifier("normal_application_background", "drawable",
	 * themePackage); if(resource_id!=0){ drawable =
	 * themeResources.getDrawable(resource_id); } } }else{ int resource_id = 0;
	 * if(type == TYPE_DOCKBAR){ resource_id =
	 * themeResources.getIdentifier("dockbar_selector", "drawable",
	 * themePackage); }else{ resource_id =
	 * themeResources.getIdentifier("shortcut_selector", "drawable",
	 * themePackage); } if(resource_id != 0){ drawable =
	 * themeResources.getDrawable(resource_id); }else{ if(type == TYPE_DOCKBAR){
	 * // drawable = themeResources.getDrawable(R.drawable.dockbar_selector);
	 * }else{ // drawable =
	 * themeResources.getDrawable(R.drawable.shortcut_selector); } }
	 * drawable.setColorFilter(pressedColor, Mode.SRC_ATOP); } } return
	 * drawable; }
	 */
	public static Drawable getDrawable(/* Context context, */int type, int selColor, int pressColor) {
		// 返回新的selector
		// if(AlmostNexusSettingsHelper.getUINewSelectors(context))
		// {
		Drawable drawable = null;
		try {
			drawable = newSelector(/* context, */selColor, pressColor);

		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {

		}
		return drawable;
		// }
		// else
		// {
		//
		// //返回旧的
		// return oldSelector(context, type);
		// }
	}
}

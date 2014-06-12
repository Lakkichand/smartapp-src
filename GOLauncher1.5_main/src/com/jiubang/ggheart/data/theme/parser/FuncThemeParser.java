package com.jiubang.ggheart.data.theme.parser;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.graphics.Color;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.data.theme.ThemeConfig;
import com.jiubang.ggheart.data.theme.bean.AppFuncBaseThemeBean.AbsTabIconBean;
import com.jiubang.ggheart.data.theme.bean.AppFuncThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;

/**
 * 
 * 类说明
 */
public class FuncThemeParser extends IParser {
	/************************ TAG ************************/
	// private static final String THEME = "Theme";
	private static final String WALLPAPER = "Wallpaper";
	private static final String FOLDERICON = "Foldericon";
	private static final String FOLDER = "Folder";
	private static final String ALLTABS = "AllTabs";
	private static final String TAB = "Tab";
	private static final String TABICON = "TabIcon";
	private static final String TABTITLE = "TabTitle";
	private static final String HOME = "Home";
	private static final String ALLAPP = "AllAppDock";
	private static final String ALLAPPMENU = "AllAppMenu";
	private static final String RECENTAPP = "RecentDock";
	private static final String RUNINGAPP = "RuningDock";
	private static final String MOVETODESK = "MoveToDesk";
	private static final String CLEARHISTORY = "ClearHistory";
	private static final String CLOSERUNNING = "CloseRunning";
	private static final String INDICATOR = "Indicator";
	private static final String APPICON = "AppIcon";
	private static final String APPSETTING = "AppSetting";
	private static final String SWITCHMENUBEAN = "SwitchMenuBean";
	private static final String SWITCHBUTTONBEAN = "SwitchButtonBean";
	/************************ Wall paper Attribute ***************************/
	private static final String BGC = "bg_color";
	private static final String IMAGE = "image";
	/************************ Folder Icon Attribute **************************/
	private static final String BOTTOM = "bottom";
	private static final String TOP_OPEN = "top_open";
	private static final String TOP_CLOSED = "top_closed";
	/************************ Folder Attribute ******************************/
	private static final String BG_FRAME_IMAGE = "bg_frame_image";
	private static final String BG_FRAME_WAY_OF_DRAWING = "bg_frame_way_of_drawing";
	private static final String EDITBOX = "editbox";
	// private static final String UP_BUTTON = "up_button";
	// private static final String UP_BUTTON_SELECTED = "up_button_selected";
	private static final String LINE_ENABLED = "line_enabled";
	private static final String FOLDER_OPEN_BG_COLOR = "folder_open_bg_color";
	private static final String FOLDER_EDIT_TEXT_COLOR = "edittext_color";
	private static final String BG_FRAME_IMAGE_BOTTOM_HEIGHT = "bg_frame_image_bottom_heigth";
	private static final String ADD_BUTON = "add_buton";
	private static final String ADD_BUTTON_LIGHT = "add_button_light";
	private static final String SORT_BUTTON = "sort_button";
	private static final String SORT_BUTTON_LIGHT = "sort_button_light";
	/************************ AllTabs Attribute ******************************/
	private static final String BG_V_IMAGE = "bg_v_image";
	private static final String BG_H_IMAGE = "bg_h_image";
	private static final String BG_WAY_OF_DRAWING = "bg_way_of_drawing";
	private static final String HOME_DELIVER_LINE_V = "home_deliver_line_v";
	private static final String HOME_DELIVER_LINE_H = "home_deliver_line_h";
	/************************ Tab Attribute ******************************/
	private static final String SELECTED_V_IMAGE = "selected_v_image";
	private static final String SELECTED_H_IMAGE = "selected_h_image";
	private static final String SELECTED_WAY_OF_DRAWING = "selected_way_of_drawing";
	private static final String FOCUSED_V_IMAGE = "focused_v_image";
	private static final String FOCUSED_H_IMAGE = "focused_h_image";
	private static final String FOCUSED_WAY_OF_DRAWING = "focused_way_of_drawing";
	private static final String CUTLINE_ENABLED = "cutLine_enabled";
	private static final String ORIENTATION_ENABLED = "orientation_enabled";
	/************************ TABICON Attribute ******************************/
	private static final String TABICON_NAME = "name";
	private static final String UNSELECTED = "unselected";
	private static final String SELECTED = "selected";
	private static final String CURRENT = "current";
	/************************ TabTitle Attribute ******************************/
	private static final String GAP_V = "gap_v";
	private static final String GAP_H = "gap_h";
	/************************ ClearHistory Attribute ******************************/
	private static final String BG_V_UNSELECTED_IMAGE = "bg_v_unselected_image";
	private static final String BG_V_SELECTED_IMAGE = "bg_v_selected_image";
	private static final String BG_H_UNSELECTED_IMAGE = "bg_h_unselected_image";
	private static final String BG_H_SELECTED_IMAGE = "bg_h_selected_image";
	private static final String BG_SELECTED_WAY_OF_DRAWING = "bg_selected_way_of_drawing";
	private static final String BG_UNSELECTED_WAY_OF_DRAWING = "bg_unselected_way_of_drawing";
	/************************ Indicator Attribute ******************************/
	private static final String INDICATOR_H_CURRENT = "indicator_h_current";
	private static final String INDICATOR_H = "indicator_h";
	/************************ AppIcon Attribute ******************************/
	private static final String DELETE_APP = "delete_app";
	private static final String DELETE_APP_HIGHLIGHT = "delete_app_highlight";
	private static final String EDIT_FOLDER = "edit_folder";
	private static final String EDIT_FOLDER_HIGHLIGHT = "edit_folder_highlight";
	private static final String TEXT_COLOR = "text_color";
	private static final String TEXT_BG_COLOR = "text_bg_color";
	private static final String NEW_APP_ICON = "new_app_icon";
	private static final String UPDATE_ICON = "update_icon";
	private static final String LOCKER_ICON = "locker_icon";
	private static final String CLOSE_APP_ICON = "close_app_icon";
	private static final String CLOSE_APP_LIGHT = "close_app_light";
	/************************ AllAppMenuBean ******************************/
	private static final String MENU_BG_V = "menu_bg_v";
	private static final String MENU_BG_H = "menu_bg_h";
	private static final String MENU_DIVIDER_V = "menu_divider_v";
	private static final String MENU_DIVIDER_H = "menu_divider_h";
	private static final String MENU_TEXT_COLOR = "menu_text_color";
	private static final String MENU_ITEM_SELECTED = "menu_item_selected";
	/************************ AppSetting Attribute ******************************/
	private static final String GRID_FORMAT = "grid_format";
	/************************ RecentDockBean ******************************/
	private static final String HOME_RECENT_CLEAR = "home_recent_clear";
	private static final String HOME_RECENT_CLEARS_ELECTED = "home_recent_clear_selected";
	private static final String HOME_RECENT_NODATA_BG = "home_recent_nodata_Bg";
	private static final String HOME_RECENT_NODATA_TEXT_COLOR = "home_recent_nodata_text_color";
	/************************ RuningDockBean ******************************/
	private static final String HOME_MEMORY_BG = "home_memory_bg";
	private static final String HOME_MEMORY_PROCESS_LOW = "home_memory_process_low";
	private static final String HOME_MEMORY_PROCESS_MIDDLE = "home_memory_process_middle";
	private static final String HOME_MEMORY_PROCESS_HIGH = "home_memory_process_high";
	private static final String HOME_CLEAN_NORMAL = "home_clean_normal";
	private static final String HOME_CLEAN_LIGHT = "home_clean_light";
	private static final String HOME_LOCK_LIST_NORMAL = "home_lock_list_normal";
	private static final String HOME_LOCK_LIST_LIGHT = "home_lock_list_light";
	private static final String HOME_RUNNING_INFO_IMG = "home_running_info_img";
	private static final String HOME_RUNNING_LOCK_IMG = "home_running_lock_img";
	private static final String HOME_EDIT_DOCK_TOUCH_BG_V = "home_edit_dock_touch_bg_v";
	private static final String HOME_EDIT_DOCK_TOUCH_BG_H = "home_edit_dock_touch_bg_h";
	private static final String HOME_EDIT_DOCK_BG_V = "home_edit_dock_bg_v";
	private static final String HOME_EDIT_DOCK_BG_H = "home_edit_dock_bg_h";
	private static final String HOME_LINE_IMG_V = "home_line_img_v";
	private static final String HOME_LINE_IMG_H = "home_line_img_h";
	private static final String HOME_RUNNING_UNLOCK_IMG = "home_running_unlock_img";
	private static final String HOME_RUNNING_TEXT_COLOR = "home_running_text_color";
	/************************ AllAppDock ****************************/
	private static final String SEARCH_UNSELECTED = "search_unselected";
	private static final String SEARCH_SELECTED = "search_selected";
	private static final String MENU_UNSELECTED = "menu_unselected";
	private static final String MENU_SELECTED = "menu_selected";
	private static final String HOME_MYAPP = "home_myapp";
	private static final String HOME_MYAPP_LIGHT = "home_myapp_light";
	/************************ SwitchMenuBean ****************************/
	private static final String MEDIA_MENU_BG_V = "media_menu_bg_v";
	private static final String MEDIA_MENU_BG_H = "media_menu_bg_h";
	private static final String MEDIA_MENU_DIVIDER_V = "media_menu_divider_v";
	private static final String MEDIA_MENU_DIVIDER_H = "media_menu_divider_h";
	private static final String MEDIA_MENU_ITEM_SEARCH_SELECTOR = "media_menu_item_search_selector";
	private static final String MEDIA_MENU_ITEM_GALLERY_SELECTOR = "media_menu_item_gallery_selector";
	private static final String MEDIA_MENU_ITEM_MUSIC_SELECTOR = "media_menu_item_music_selector";
	private static final String MEDIA_MENU_ITEM_VIDEO_SELECTOR = "media_menu_item_video_selector";
	private static final String MEDIA_MENU_ITEM_APP_SELECTOR = "media_menu_item_app_selector";
	private static final String MEDIA_MENU_TEXT_COLOR = "media_menu_text_color";
	/************************ SwitchButtonBean ****************************/
	private static final String BUTTON_GALLERYICON = "button_galleryicon";
	private static final String BUTTON_GALLERYLIGHTICON = "button_gallerylighticon";
	private static final String BUTTON_MUSICICON = "button_musicicon";
	private static final String BUTTON_MUSICLIGHTICON = "button_musiclighticon";
	private static final String BUTTON_VIDEOICON = "button_videoicon";
	private static final String BUTTON_VIDEOLIGHTICON = "button_videolighticon";
	private static final String BUTTON_APPICON = "button_appicon";
	private static final String BUTTON_APPICONLIGHT = "button_appiconlight";
	private static final String BUTTON_SEARCH = "button_search";
	private static final String BUTTON_SEARCHLIGHT = "button_searchlight";

	public FuncThemeParser() {
		mAutoParserFileName = ThemeConfig.APPFUNCTHEMEFILENAME;
	}

	@Override
	protected ThemeBean createThemeBean(String pkgName) {
		// TODO Auto-generated method stub
		return new AppFuncThemeBean(pkgName);
	}

	@Override
	public void parseXml(XmlPullParser xmlPullParser, ThemeBean bean) {
		XmlPullParser parser = xmlPullParser;
		AppFuncThemeBean themeBean = (AppFuncThemeBean) bean;
		// 测试代码
		// AppFuncThemeBean themeBean = new AppFuncThemeBean();
		// XmlResourceParser parser = mActivity.getResources().getXml(
		// R.xml.app_func_theme);
		themeBean.mFoldericonBean.mPackageName = bean.getPackageName();
		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					System.out.println("Start tag " + tagName);
					if (tagName.equals(WALLPAPER)) {
						String attributeValue = parser.getAttributeValue(null, BGC);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mWallpaperBean.mBackgroudColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, IMAGE);
						if (attributeValue != null) {
							themeBean.mWallpaperBean.mImagePath = attributeValue;
						}
					} else if (tagName.equals(FOLDERICON)) {
						String attributeValue = parser.getAttributeValue(null, BOTTOM);
						if (attributeValue != null) {
							themeBean.mFoldericonBean.mFolderIconBottomPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, TOP_OPEN);
						if (attributeValue != null) {
							themeBean.mFoldericonBean.mFolderIconTopOpenPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, TOP_CLOSED);
						if (attributeValue != null) {
							themeBean.mFoldericonBean.mFolderIconTopClosedPath = attributeValue;
						}
					} else if (tagName.equals(FOLDER)) {
						String attributeValue = parser.getAttributeValue(null, BG_FRAME_IMAGE);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderBgPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, EDITBOX);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderEditBgPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, ADD_BUTON);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderAddButton = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, ADD_BUTTON_LIGHT);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderAddButtonLight = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, SORT_BUTTON);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderSortButton = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, SORT_BUTTON_LIGHT);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderSortButtonLight = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_FRAME_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mFolderBean.mFolderBgDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, LINE_ENABLED);
						if (attributeValue != null) {
							try {
								int enabled = Integer.parseInt(attributeValue);
								themeBean.mFolderBean.mFolderLineEnabled = (byte) enabled;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						String folderBgColor = parser.getAttributeValue(null, FOLDER_OPEN_BG_COLOR);
						if (folderBgColor != null) {
							try {
								int color = Color.parseColor(folderBgColor);
								themeBean.mFolderBean.mFolderOpenBgColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						String bgImageBottomHeigth = parser.getAttributeValue(null,
								BG_FRAME_IMAGE_BOTTOM_HEIGHT);
						if (bgImageBottomHeigth != null) {
							try {
								int heigth = Integer.parseInt(bgImageBottomHeigth);
								themeBean.mFolderBean.mImageBottomH = (byte) heigth;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						String folderEditTextColor = parser.getAttributeValue(null,
								FOLDER_EDIT_TEXT_COLOR);
						if (folderEditTextColor != null) {
							try {
								int color = Color.parseColor(folderEditTextColor);
								themeBean.mFolderBean.mFolderEditTextColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, ADD_BUTON);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderAddButton = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, ADD_BUTTON_LIGHT);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderAddButtonLight = attributeValue;
						}

					} else if (tagName.equals(ALLTABS)) {
						String attributeValue = parser.getAttributeValue(null, BG_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mAllTabsBean.mAllTabsBgBottomVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mAllTabsBean.mAllTabsBgBottomHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mAllTabsBean.mAllTabsBgDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(TAB)) {
						String attributeValue = parser.getAttributeValue(null, BG_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabBgBottomVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabBgBottomHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mTabBean.mTabBgDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, SELECTED_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabSelectedBottomVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, SELECTED_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabSelectedBottomHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, SELECTED_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mTabBean.mTabSelectedDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, FOCUSED_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabFocusedBottomVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, FOCUSED_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabFocusedBottomHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, FOCUSED_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mTabBean.mTabFocusedDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, CUTLINE_ENABLED);
						if (attributeValue != null) {
							try {
								int enabled = Integer.parseInt(attributeValue);
								themeBean.mTabBean.mTabCutLineEnabled = (byte) enabled;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, ORIENTATION_ENABLED);
						if (attributeValue != null) {
							try {
								int enabled = Integer.parseInt(attributeValue);
								themeBean.mTabBean.mTabOrientationEnabled = enabled;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(TABICON)) {
						String name = parser.getAttributeValue(null, TABICON_NAME);
						if (name != null) {
							AbsTabIconBean tabIconBean = themeBean.mTabIconBeanMap.get(name);
							if (tabIconBean != null) {
								String attributeValue = parser.getAttributeValue(null, UNSELECTED);
								if (attributeValue != null) {
									tabIconBean.mTabIconUnSelected = attributeValue;
								}
								attributeValue = parser.getAttributeValue(null, SELECTED);
								if (attributeValue != null) {
									tabIconBean.mTabIconSelected = attributeValue;
								}
								attributeValue = parser.getAttributeValue(null, CURRENT);
								if (attributeValue != null) {
									tabIconBean.mTabIconCurrent = attributeValue;
								}
								themeBean.mTabIconBeanMap.put(tabIconBean.name, tabIconBean);
							}
						}
					} else if (tagName.equals(TABTITLE)) {
						String attributeValue = parser.getAttributeValue(null, SELECTED);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mTabTitleBean.mTabTitleColorSelected = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						attributeValue = parser.getAttributeValue(null, UNSELECTED);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mTabTitleBean.mTabTitleColorUnSelected = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						attributeValue = parser.getAttributeValue(null, GAP_V);
						if (attributeValue != null) {
							try {
								int gap = Integer.parseInt(attributeValue);
								themeBean.mTabTitleBean.mTabTitleGapVer = gap;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						attributeValue = parser.getAttributeValue(null, GAP_H);
						if (attributeValue != null) {
							try {
								int gap = Integer.parseInt(attributeValue);
								themeBean.mTabTitleBean.mTabTitleGapHor = gap;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(HOME)) {

						String attributeValue = parser.getAttributeValue(null, SELECTED);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeSelected = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, UNSELECTED);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeUnSelected = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_DELIVER_LINE_V);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeDeliverLineV = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_DELIVER_LINE_H);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeDeliverLineH = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeBgVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeBgHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mHomeBean.mHomeBgDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, BGC);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mHomeBean.mHomeBgColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

					} else if (tagName.equals(ALLAPP)) {
						// 对应所有程序dock
						String attriValue = parser.getAttributeValue(null, HOME_MYAPP);
						if (attriValue != null) {
							themeBean.mAllAppDockBean.mHomeMyApp = attriValue;
						}

						attriValue = parser.getAttributeValue(null, HOME_MYAPP_LIGHT);
						if (attriValue != null) {
							themeBean.mAllAppDockBean.mHomeMyAppLight = attriValue;
						}

						attriValue = parser.getAttributeValue(null, SEARCH_UNSELECTED);
						if (attriValue != null) {
							themeBean.mAllAppDockBean.mHomeSearch = attriValue;
						}

						attriValue = parser.getAttributeValue(null, SEARCH_SELECTED);
						if (attriValue != null) {
							themeBean.mAllAppDockBean.mHomeSearchSelected = attriValue;
						}

						attriValue = parser.getAttributeValue(null, MENU_UNSELECTED);
						if (attriValue != null) {
							themeBean.mAllAppDockBean.mHomeMenu = attriValue;
						}

						attriValue = parser.getAttributeValue(null, MENU_SELECTED);
						if (attriValue != null) {
							themeBean.mAllAppDockBean.mHomeMenuSelected = attriValue;
						}
					} else if (tagName.equals(ALLAPPMENU)) {
						String attributeValue = parser.getAttributeValue(null, MENU_BG_V);
						if (attributeValue != null) {
							themeBean.mAllAppMenuBean.mMenuBgV = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, MENU_BG_H);
						if (attributeValue != null) {
							themeBean.mAllAppMenuBean.mMenuBgH = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, MENU_DIVIDER_V);
						if (attributeValue != null) {
							themeBean.mAllAppMenuBean.mMenuDividerV = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, MENU_DIVIDER_H);
						if (attributeValue != null) {
							themeBean.mAllAppMenuBean.mMenuDividerH = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, MENU_ITEM_SELECTED);
						if (attributeValue != null) {
							themeBean.mAllAppMenuBean.mMenuItemSelected = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, MENU_TEXT_COLOR);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mAllAppMenuBean.mMenuTextColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(RECENTAPP)) {
						// 对应正在打开dock
						String attributeValue = parser.getAttributeValue(null, HOME_RECENT_CLEAR);
						if (attributeValue != null) {
							themeBean.mRecentDockBean.mHomeRecentClear = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_RECENT_CLEARS_ELECTED);
						if (attributeValue != null) {
							themeBean.mRecentDockBean.mHomeRecentClearSelected = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_RECENT_NODATA_BG);
						if (attributeValue != null) {
							themeBean.mRecentDockBean.mHomeRecentNoDataBg = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null,
								HOME_RECENT_NODATA_TEXT_COLOR);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mRecentDockBean.mHomeRecentNoDataTextColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(RUNINGAPP)) {
						// 对应正在运行dock
						String attributeValue = parser.getAttributeValue(null, HOME_MEMORY_BG);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeMemoryBg = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_MEMORY_PROCESS_LOW);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeMemoryProcessLow = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_MEMORY_PROCESS_MIDDLE);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeMemoryProcessMiddle = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_MEMORY_PROCESS_HIGH);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeMemoryProcessHigh = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_CLEAN_NORMAL);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeCleanNormal = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_CLEAN_LIGHT);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeCleanLight = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_LOCK_LIST_NORMAL);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeLockListNormal = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_LOCK_LIST_LIGHT);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeLockListLight = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_RUNNING_INFO_IMG);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeRunningInfoImg = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_RUNNING_LOCK_IMG);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeRunningLockImg = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_EDIT_DOCK_BG_H);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeEditDockBgH = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_EDIT_DOCK_BG_V);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeEditDockBgV = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_EDIT_DOCK_TOUCH_BG_H);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeEditDockTouchBgH = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_EDIT_DOCK_TOUCH_BG_V);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeEditDockTouchBgV = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_LINE_IMG_H);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeLineImgH = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_LINE_IMG_V);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeLineImgV = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_RUNNING_UNLOCK_IMG);
						if (attributeValue != null) {
							themeBean.mRuningDockBean.mHomeRunningUnLockImg = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_RUNNING_TEXT_COLOR);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mRuningDockBean.mHomeTextColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

					} else if (tagName.equals(MOVETODESK)) {
						String attributeValue = parser.getAttributeValue(null, BG_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mMoveToDeskBean.mMoveToDeskBgBottomVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mMoveToDeskBean.mMoveToDeskBgBottomHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mMoveToDeskBean.mMoveToDeskBgDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, BGC);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mMoveToDeskBean.mMoveToDeskBgColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(CLEARHISTORY)) {
						String attributeValue = parser.getAttributeValue(null, BG_V_SELECTED_IMAGE);
						if (attributeValue != null) {
							themeBean.mClearHistoryBean.mClearHistoryBottomSelectedVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_V_UNSELECTED_IMAGE);
						if (attributeValue != null) {
							themeBean.mClearHistoryBean.mClearHistoryBottomUnselectedVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_SELECTED_IMAGE);
						if (attributeValue != null) {
							themeBean.mClearHistoryBean.mClearHistoryBottomSelectedHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_UNSELECTED_IMAGE);
						if (attributeValue != null) {
							themeBean.mClearHistoryBean.mClearHistoryBottomUnselectedHorPath = attributeValue;
						}

						attributeValue = parser.getAttributeValue(null, BG_SELECTED_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mClearHistoryBean.mClearHistorySelectedDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null,
								BG_UNSELECTED_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mClearHistoryBean.mClearHistoryUnselectedDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, BGC);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mClearHistoryBean.mClearHistoryTextColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(CLOSERUNNING)) {
						String attributeValue = parser.getAttributeValue(null, BG_V_SELECTED_IMAGE);
						if (attributeValue != null) {
							themeBean.mCloseRunningBean.mCloseRunningBottomSelectVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_V_UNSELECTED_IMAGE);
						if (attributeValue != null) {
							themeBean.mCloseRunningBean.mCloseRunningBottomUnselectVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_SELECTED_IMAGE);
						if (attributeValue != null) {
							themeBean.mCloseRunningBean.mCloseRunningBottomSelectHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_UNSELECTED_IMAGE);
						if (attributeValue != null) {
							themeBean.mCloseRunningBean.mCloseRunningBottomUnselectHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_SELECTED_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mCloseRunningBean.mCloseRunningSelectDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null,
								BG_UNSELECTED_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mCloseRunningBean.mCloseRunningUnselectDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, BGC);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mCloseRunningBean.mCloseRunningTextColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(INDICATOR)) {
						String attributeValue = parser.getAttributeValue(null, INDICATOR_H_CURRENT);
						if (attributeValue != null) {
							themeBean.mIndicatorBean.indicatorCurrentHor = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, INDICATOR_H);
						if (attributeValue != null) {
							themeBean.mIndicatorBean.indicatorHor = attributeValue;
						}
					} else if (tagName.equals(APPICON)) {
						String attributeValue = parser.getAttributeValue(null, TEXT_COLOR);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mAppIconBean.mTextColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, TEXT_BG_COLOR);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mAppIconBean.mIconBgColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, DELETE_APP);
						if (attributeValue != null) {
							themeBean.mAppIconBean.mDeletApp = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, DELETE_APP_HIGHLIGHT);
						if (attributeValue != null) {
							themeBean.mAppIconBean.mDeletHighlightApp = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, EDIT_FOLDER);
						if (attributeValue != null) {
							themeBean.mAppIconBean.mEditFolder = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, EDIT_FOLDER_HIGHLIGHT);
						if (attributeValue != null) {
							themeBean.mAppIconBean.mEditHighlightFolder = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, NEW_APP_ICON);
						if (attributeValue != null) {
							themeBean.mAppIconBean.mNewApp = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, UPDATE_ICON);
						if (attributeValue != null) {
							themeBean.mAppIconBean.mUpdateIcon = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, LOCKER_ICON);
						if (attributeValue != null) {
							themeBean.mAppIconBean.mLockApp = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, CLOSE_APP_ICON);
						if (attributeValue != null) {
							themeBean.mAppIconBean.mKillApp = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, CLOSE_APP_LIGHT);
						if (attributeValue != null) {
							themeBean.mAppIconBean.mKillAppLight = attributeValue;
						}
					} else if (tagName.equals(APPSETTING)) {
						String attributeValue = parser.getAttributeValue(null, GRID_FORMAT);
						if (attributeValue != null) {
							try {
								int format = Integer.parseInt(attributeValue);
								themeBean.mAppSettingBean.mGridFormat = format;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(SWITCHMENUBEAN)) {
						themeBean.mSwitchMenuBean.mPackageName = bean.getPackageName();
						String attributeValue = parser.getAttributeValue(null, MEDIA_MENU_BG_V);
						if (attributeValue != null) {
							themeBean.mSwitchMenuBean.mMenuBgV = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, MEDIA_MENU_BG_H);
						if (attributeValue != null) {
							themeBean.mSwitchMenuBean.mMenuBgH = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, MEDIA_MENU_DIVIDER_V);
						if (attributeValue != null) {
							themeBean.mSwitchMenuBean.mMenuDividerV = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, MEDIA_MENU_DIVIDER_H);
						if (attributeValue != null) {
							themeBean.mSwitchMenuBean.mMenuDividerH = attributeValue;
						}
						String search = null;
						String gallery = null;
						String video = null;
						String audio = null;
						String app = null;
						attributeValue = parser.getAttributeValue(null,
								MEDIA_MENU_ITEM_SEARCH_SELECTOR);
						if (attributeValue != null) {
							search = attributeValue;
						} else {
							search = Integer.toString(R.drawable.switch_menu_search_selector);
						}
						attributeValue = parser.getAttributeValue(null,
								MEDIA_MENU_ITEM_GALLERY_SELECTOR);
						if (attributeValue != null) {
							gallery = attributeValue;
						} else {
							gallery = Integer.toString(R.drawable.switch_menu_image_selector);
						}
						attributeValue = parser.getAttributeValue(null,
								MEDIA_MENU_ITEM_MUSIC_SELECTOR);
						if (attributeValue != null) {
							audio = attributeValue;
						} else {
							audio = Integer.toString(R.drawable.switch_menu_audio_selector);
						}
						attributeValue = parser.getAttributeValue(null,
								MEDIA_MENU_ITEM_VIDEO_SELECTOR);
						if (attributeValue != null) {
							video = attributeValue;
						} else {
							video = Integer.toString(R.drawable.switch_menu_video_selector);
						}
						attributeValue = parser.getAttributeValue(null,
								MEDIA_MENU_ITEM_APP_SELECTOR);
						if (attributeValue != null) {
							app = attributeValue;
						} else {
							app = Integer.toString(R.drawable.switch_menu_app_selector);
						}
						themeBean.mSwitchMenuBean.mItemLabelAppSelectors = new String[] { gallery,
								audio, video, search };
						themeBean.mSwitchMenuBean.mItemLabelImageSelectors = new String[] { app,
								audio, video, search };
						themeBean.mSwitchMenuBean.mItemLabelAudioSelectors = new String[] { app,
								gallery, video, search };
						themeBean.mSwitchMenuBean.mItemLabelVedioSelectors = new String[] { app,
								gallery, audio, search };
						themeBean.mSwitchMenuBean.mItemLabelSearchSelectors = new String[] { app,
								gallery, audio, video };
						attributeValue = parser.getAttributeValue(null, MEDIA_MENU_TEXT_COLOR);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mSwitchMenuBean.mMenuTextColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(SWITCHBUTTONBEAN)) {
						String attributeValue = parser.getAttributeValue(null, BUTTON_GALLERYICON);
						if (attributeValue != null) {
							themeBean.mSwitchButtonBean.mGalleryIcon = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BUTTON_GALLERYLIGHTICON);
						if (attributeValue != null) {
							themeBean.mSwitchButtonBean.mGalleryLightIcon = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BUTTON_MUSICICON);
						if (attributeValue != null) {
							themeBean.mSwitchButtonBean.mMusicIcon = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BUTTON_MUSICLIGHTICON);
						if (attributeValue != null) {
							themeBean.mSwitchButtonBean.mMusicLightIcon = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BUTTON_VIDEOICON);
						if (attributeValue != null) {
							themeBean.mSwitchButtonBean.mVideoIcon = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BUTTON_VIDEOLIGHTICON);
						if (attributeValue != null) {
							themeBean.mSwitchButtonBean.mVideoLightIcon = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BUTTON_APPICON);
						if (attributeValue != null) {
							themeBean.mSwitchButtonBean.mAppIcon = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BUTTON_APPICONLIGHT);
						if (attributeValue != null) {
							themeBean.mSwitchButtonBean.mAppIconLight = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BUTTON_SEARCH);
						if (attributeValue != null) {
							themeBean.mSwitchButtonBean.mSearchIcon = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BUTTON_SEARCHLIGHT);
						if (attributeValue != null) {
							themeBean.mSwitchButtonBean.mSearchIconLight = attributeValue;
						}
					}
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void parseIndicatorXml(XmlPullParser xmlPullParser, ThemeBean bean) {
		XmlPullParser parser = xmlPullParser;
		AppFuncThemeBean themeBean = (AppFuncThemeBean) bean;

		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					if (tagName.equals(INDICATOR)) {
						String attributeValue = parser.getAttributeValue(null, INDICATOR_H_CURRENT);
						if (attributeValue != null) {
							themeBean.mIndicatorBean.indicatorCurrentHor = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, INDICATOR_H);
						if (attributeValue != null) {
							themeBean.mIndicatorBean.indicatorHor = attributeValue;
						}
					}
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void parseTabHomeXml(XmlPullParser xmlPullParser, ThemeBean bean) {
		XmlPullParser parser = xmlPullParser;
		AppFuncThemeBean themeBean = (AppFuncThemeBean) bean;

		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					if (tagName.equals(ALLTABS)) {
						String attributeValue = parser.getAttributeValue(null, BG_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mAllTabsBean.mAllTabsBgBottomVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mAllTabsBean.mAllTabsBgBottomHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mAllTabsBean.mAllTabsBgDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(TAB)) {
						String attributeValue = parser.getAttributeValue(null, BG_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabBgBottomVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabBgBottomHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mTabBean.mTabBgDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, SELECTED_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabSelectedBottomVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, SELECTED_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabSelectedBottomHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, SELECTED_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mTabBean.mTabSelectedDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, FOCUSED_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabFocusedBottomVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, FOCUSED_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mTabBean.mTabFocusedBottomHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, FOCUSED_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mTabBean.mTabFocusedDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, CUTLINE_ENABLED);
						if (attributeValue != null) {
							try {
								int enabled = Integer.parseInt(attributeValue);
								themeBean.mTabBean.mTabCutLineEnabled = (byte) enabled;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, ORIENTATION_ENABLED);
						if (attributeValue != null) {
							try {
								int enabled = Integer.parseInt(attributeValue);
								themeBean.mTabBean.mTabOrientationEnabled = enabled;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(TABICON)) {
						String name = parser.getAttributeValue(null, TABICON_NAME);
						if (name != null) {
							AbsTabIconBean tabIconBean = themeBean.mTabIconBeanMap.get(name);
							if (tabIconBean != null) {
								String attributeValue = parser.getAttributeValue(null, UNSELECTED);
								if (attributeValue != null) {
									tabIconBean.mTabIconUnSelected = attributeValue;
								}
								attributeValue = parser.getAttributeValue(null, SELECTED);
								if (attributeValue != null) {
									tabIconBean.mTabIconSelected = attributeValue;
								}
								attributeValue = parser.getAttributeValue(null, CURRENT);
								if (attributeValue != null) {
									tabIconBean.mTabIconCurrent = attributeValue;
								}
								themeBean.mTabIconBeanMap.put(tabIconBean.name, tabIconBean);
							}
						}
					} else if (tagName.equals(TABTITLE)) {
						String attributeValue = parser.getAttributeValue(null, SELECTED);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mTabTitleBean.mTabTitleColorSelected = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						attributeValue = parser.getAttributeValue(null, UNSELECTED);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mTabTitleBean.mTabTitleColorUnSelected = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						attributeValue = parser.getAttributeValue(null, GAP_V);
						if (attributeValue != null) {
							try {
								int gap = Integer.parseInt(attributeValue);
								themeBean.mTabTitleBean.mTabTitleGapVer = gap;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						attributeValue = parser.getAttributeValue(null, GAP_H);
						if (attributeValue != null) {
							try {
								int gap = Integer.parseInt(attributeValue);
								themeBean.mTabTitleBean.mTabTitleGapHor = gap;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(HOME)) {
						String attributeValue = parser.getAttributeValue(null, SELECTED);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeSelected = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, UNSELECTED);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeUnSelected = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_DELIVER_LINE_V);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeDeliverLineV = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, HOME_DELIVER_LINE_H);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeDeliverLineH = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeBgVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mHomeBean.mHomeBgHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mHomeBean.mHomeBgDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, BGC);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mHomeBean.mHomeBgColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (tagName.equals(MOVETODESK)) {
						String attributeValue = parser.getAttributeValue(null, BG_V_IMAGE);
						if (attributeValue != null) {
							themeBean.mMoveToDeskBean.mMoveToDeskBgBottomVerPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_H_IMAGE);
						if (attributeValue != null) {
							themeBean.mMoveToDeskBean.mMoveToDeskBgBottomHorPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mMoveToDeskBean.mMoveToDeskBgDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, BGC);
						if (attributeValue != null) {
							try {
								int color = Color.parseColor(attributeValue);
								themeBean.mMoveToDeskBean.mMoveToDeskBgColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void parseFolderXml(XmlPullParser xmlPullParser, ThemeBean bean) {
		XmlPullParser parser = xmlPullParser;
		AppFuncThemeBean themeBean = (AppFuncThemeBean) bean;
		try {
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					System.out.println("Start tag " + tagName);
					if (tagName.equals(FOLDERICON)) {
						String attributeValue = parser.getAttributeValue(null, BOTTOM);
						if (attributeValue != null) {
							themeBean.mFoldericonBean.mFolderIconBottomPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, TOP_OPEN);
						if (attributeValue != null) {
							themeBean.mFoldericonBean.mFolderIconTopOpenPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, TOP_CLOSED);
						if (attributeValue != null) {
							themeBean.mFoldericonBean.mFolderIconTopClosedPath = attributeValue;
						}
					} else if (tagName.equals(FOLDER)) {
						String attributeValue = parser.getAttributeValue(null, BG_FRAME_IMAGE);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderBgPath = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, EDITBOX);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderEditBgPath = attributeValue;
						}
						// attributeValue = parser.getAttributeValue(null,
						// UP_BUTTON);
						// if (attributeValue != null) {
						// themeBean.mFolderBean.mFolderUpButtonPath =
						// attributeValue;
						// }
						// attributeValue = parser.getAttributeValue(null,
						// UP_BUTTON_SELECTED);
						// if (attributeValue != null) {
						// themeBean.mFolderBean.mFolderUpButtonSelectedPath =
						// attributeValue;
						// }
						attributeValue = parser.getAttributeValue(null, ADD_BUTON);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderAddButton = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, ADD_BUTTON_LIGHT);
						if (attributeValue != null) {
							themeBean.mFolderBean.mFolderAddButtonLight = attributeValue;
						}
						attributeValue = parser.getAttributeValue(null, BG_FRAME_WAY_OF_DRAWING);
						if (attributeValue != null) {
							try {
								int way = Integer.parseInt(attributeValue);
								themeBean.mFolderBean.mFolderBgDrawingWay = (byte) way;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						attributeValue = parser.getAttributeValue(null, LINE_ENABLED);
						if (attributeValue != null) {
							try {
								int enabled = Integer.parseInt(attributeValue);
								themeBean.mFolderBean.mFolderLineEnabled = (byte) enabled;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						String folderBgColor = parser.getAttributeValue(null, FOLDER_OPEN_BG_COLOR);
						if (folderBgColor != null) {
							try {
								int color = Color.parseColor(folderBgColor);
								themeBean.mFolderBean.mFolderOpenBgColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						String bgImageBottomHeigth = parser.getAttributeValue(null,
								BG_FRAME_IMAGE_BOTTOM_HEIGHT);
						if (bgImageBottomHeigth != null) {
							try {
								int heigth = Integer.parseInt(bgImageBottomHeigth);
								themeBean.mFolderBean.mImageBottomH = (byte) heigth;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						String folderEditTextColor = parser.getAttributeValue(null,
								FOLDER_EDIT_TEXT_COLOR);
						if (folderEditTextColor != null) {
							try {
								int color = Color.parseColor(folderEditTextColor);
								themeBean.mFolderBean.mFolderEditTextColor = color;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
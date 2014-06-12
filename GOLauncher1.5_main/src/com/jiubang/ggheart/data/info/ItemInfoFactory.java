package com.jiubang.ggheart.data.info;

public class ItemInfoFactory {
	public static ItemInfo createItemInfo(int type) {
		ItemInfo info = null;
		switch (type) {
			case IItemType.ITEM_TYPE_APPLICATION :
			case IItemType.ITEM_TYPE_SHORTCUT :
				info = new ShortCutInfo();
				break;

			case IItemType.ITEM_TYPE_USER_FOLDER :
				info = new UserFolderInfo();
				break;

			case IItemType.ITEM_TYPE_LIVE_FOLDER :
				info = new ScreenLiveFolderInfo();
				break;

			case IItemType.ITEM_TYPE_APP_WIDGET :
				info = new ScreenAppWidgetInfo(0);
				break;

			case IItemType.ITEM_TYPE_FAVORITE :
				info = new FavoriteInfo();
				break;

			default :
				break;
		}
		return info;
	}
}

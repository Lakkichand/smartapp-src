package com.jiubang.ggheart.plugin.mediamanagement.inf;


/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-16]
 */
public interface ISwitchMenuControler {
	public void popupAppMenu(OnSwitchMenuItemClickListener listener);
	public void popupImageMenu(OnSwitchMenuItemClickListener listener);
	public void popupMusicMenu(OnSwitchMenuItemClickListener listener);
	public void popupVideoMenu(OnSwitchMenuItemClickListener listener);
	public void popupSearchMenu(OnSwitchMenuItemClickListener listener);
}

package com.jiubang.ggheart.plugin.mediamanagement.inf;

import android.app.Activity;
import android.content.Context;

import com.go.util.file.media.FileEngine;
import com.go.util.file.media.FileInfo;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.mars.XPanel;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-16]
 */
public interface IMediaManager {

	public Context getContext();
	
	public MediaContext getMediaContext();

	public Activity getGoLauncher();

//	public void refreshAllMediaData(OnMediaDataRefreshListener listener);

//	public Bitmap getThumbnail(MediaBroadCasterObserver observer, String type, int id,
//			String filePath);
//
//	public Bitmap getThumbnail(MediaBroadCasterObserver observer, String type, int id,
//			String filePath, int imgWidth);

	public XPanel getMediaManagementContainer();

	public void switchContent(Object[] params);

	public void openMusicPlayer(Object[] params);

	public AbstractFrame openImageBrowser(Activity activity, IFrameManager frameManager, int frameId);

	public void setSwitchMenuControler(ISwitchMenuControler controler);
	
	public ISwitchMenuControler getSwitchMenuControler();

	public void setMediaMessageManager(IMediaMessageManager messageManager);

	public void locateMediaItem(FileInfo fileInfo, boolean needFocus);
	
	public void setMediaDialog(IMediaBaseDialog mediaDialog);
	
	public void setImageDefaultOpenWay(String way);

	public void setMusicDefaultOpenWay(String way);

	public String getImageDefaultOpenWay();

	public String getMusicDefaultOpenWay();
	
	public void showMenu(boolean show);
	
	public void setFileEngine(FileEngine fileEngine);
	
	public void setMediaCommonMenu(IMediaCommonMenu menu);
	
	public IMediaCommonMenu getMediaCommonMenu();
	
	public boolean getIsMediaDataBeCopy();
	
	public void setIsMediaDataBeCopy(boolean isCopy);
	
	public void onExitMediaManagement();
	
	public boolean action(int actionId, int param, Object... objects);
}

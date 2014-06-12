package com.jiubang.ggheart.apps.desks.diy;

import android.app.Activity;

import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.WidgetStyleChooseFrame;
import com.jiubang.ggheart.apps.desks.appfunc.search.AppFuncSearchFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.animation.AnimationFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.cover.CoverFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DockFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.autofit.DockAddIconFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.ReplaceDragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.preview.SensePreviewFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DeskUserFolderFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.WidgetEditFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditBoxFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl.GuideForGlFrame;
import com.jiubang.ggheart.apps.desks.share.ShareFrame;
import com.jiubang.ggheart.apps.desks.snapshot.SnapShotFrame;
import com.jiubang.ggheart.apps.gowidget.widgetThemeChoose.WidgetThemeChooseFrame;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.shell.ShellPluginFactory;

/**
 * 帧产生工厂
 * 
 * @author yuankai
 * @version 1.0
 */
class FrameFactory {
	/**
	 * 通过ID产生相应的frame
	 * 
	 * @param frameId
	 *            id
	 * @param theme
	 *            创建frame所需的主题数据
	 * @param globalContext
	 *            全局上下文
	 * @return 创建之后的对象，如果创建失败为return null;
	 */
	static AbstractFrame produce(Activity activity, IFrameManager frameManager, int frameId) {
		AbstractFrame ret = null;
		switch (frameId) {
			case IDiyFrameIds.SCREEN_FRAME : {
				ret = new ScreenFrame(activity, frameManager, frameId);
			}
				break;

			case IDiyFrameIds.APPFUNC_FRAME : {
				ret = new AppFuncFrame(activity, frameManager, frameId);
			}
				break;

			case IDiyFrameIds.DOCK_FRAME : {
				ret = new DockFrame(activity, frameManager, frameId);
			}
				break;

			case IDiyFrameIds.SCREEN_PREVIEW_FRAME : {
				ret = new SensePreviewFrame(activity, frameManager, frameId);
			}
				break;

			case IDiyFrameIds.SCREEN_WIDGET_EDIT_FRAME : {
				ret = new WidgetEditFrame(activity, frameManager, frameId);
				break;
			}

			case IDiyFrameIds.DRAG_FRAME : {
				ret = new DragFrame(activity, frameManager, frameId);
			}
				break;

			case IDiyFrameIds.REPLACE_DRAG_FRAME : {
				ret = new ReplaceDragFrame(activity, frameManager, frameId);
			}
				break;

			case IDiyFrameIds.WIDGET_STYLE_CHOOSE : {
				ret = new WidgetStyleChooseFrame(activity, frameManager, frameId);
			}
				break;

			case IDiyFrameIds.WIDGET_THEME_CHOOSE : {
				ret = new WidgetThemeChooseFrame(activity, frameManager, frameId);
			}
				break;

			case IDiyFrameIds.DESK_USER_FOLDER_FRAME : {
				ret = new DeskUserFolderFrame(activity, frameManager, frameId);
			}
				break;

			case IDiyFrameIds.ANIMATION_FRAME : {
				ret = new AnimationFrame(activity, frameManager, frameId);
			}
				break;

			case IDiyFrameIds.GUIDE_GL_FRAME : {
				ret = new GuideForGlFrame(activity, frameManager, frameId);
			}
				break;

			case IDiyFrameIds.SCREEN_EDIT_BOX_FRAME : {
				ret = new ScreenEditBoxFrame(activity, frameManager, frameId);
			}
				break;
			case IDiyFrameIds.IMAGE_BROWSER_FRAME : {
				ret = MediaPluginFactory.getMediaManager().openImageBrowser(activity,
						frameManager, frameId);
			}
				break;
			case IDiyFrameIds.APPFUNC_SEARCH_FRAME : {
				ret = new AppFuncSearchFrame(activity, frameManager, frameId);
			}
				break;
			case IDiyFrameIds.SHARE_FRAME : {
				ret = new ShareFrame(activity, frameManager, frameId);
			}
				break;
			case IDiyFrameIds.COVER_FRAME : {
				ret = new CoverFrame(activity, frameManager, frameId);
			}
				break;
			case IDiyFrameIds.SNAPSHOT_FRAME : {
				ret = new SnapShotFrame(activity, frameManager, frameId);
			}
				break;
			case IDiyFrameIds.DOCK_ADD_ICON_FRAME : {
				ret = new DockAddIconFrame(activity, frameManager, frameId);
			}
				break;
			case IDiyFrameIds.SHELL_FRAME : {
				ret = ShellPluginFactory.getShellManager().getShellFrame(activity, frameManager,
						frameId);
			}
				break;
			default :
				break;
		}
		return ret;
	}
}

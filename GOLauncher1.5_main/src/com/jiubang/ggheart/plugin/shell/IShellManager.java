package com.jiubang.ggheart.plugin.shell;

import android.app.Activity;
import android.view.View;

import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenControler;

/**
 * ShellManager接口，用于主包与3D插件包进行交互
 * @author yangguanxiang
 *
 */
public interface IShellManager {
	public AbstractFrame getShellFrame(Activity activity, IFrameManager frameManager, int id);
	public View getCompatibleView();
	public View getOverlayedViewGroup();
	public ScreenControler getScreenControler();
}

package com.jiubang.ggheart.appgame.base.component;

import java.util.List;

import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.download.DownloadTask;

/**
 * 应用中心/游戏中心子屏幕基类，所有子屏幕类都需要继承这个接口
 * 
 * @author xiedezhi
 * 
 */
public interface IContainer extends ICleanable, SDCardEventHandler, ScreenActiveHandler,
		IMenuHandler {

	/**
	 * view所在activity发生onResume时调用
	 */
	public void onResume();

	/**
	 * view所在activity发生onStop时的调用
	 */
	public void onStop();

	/**
	 * 当系统有安装，卸载，更新应用等操作时回调该接口
	 * 
	 * @param packName
	 *            安装/卸载/更新的包名
	 * 
	 * @param appAction
	 *            代表应用的操作码，详情看{@link MainViewGroup}
	 */
	public void onAppAction(String packName, int appAction);

	/**
	 * 更新container的数据。每个container的数据由外部填充，container只负责数据展示。MyAppsContainer,
	 * AppsUpdateContainer,MyGameContainer除外，这几个container的数据由自己管理。
	 * 
	 * @param bean
	 *            数据封装类
	 * @param isPrevLoadRefresh
	 *            是否预加载后子container数据有更新调用的updateContent
	 */
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh);

	/**
	 * 初始化入口值，表示用户从哪个入口进入应用游戏中心，入口值定义见MainViewGroup
	 * {@link MainViewGroup}
	 * 
	 * @param access
	 *            入口值，标示用户从哪个入口进入应用游戏中心
	 */
	public void initEntrance(int access);

	/**
	 * 获取该container的分类id
	 */
	public int getTypeId();

	/**
	 * 在TabManageView的updateContent方法最后被调用 这时所有的界面已经初始化完毕
	 */
	public void onFinishAllUpdateContent();

	/**
	 * 当收到下载进度更新的消息，把消息发到每个container里
	 */
	public void notifyDownloadState(DownloadTask downloadTask);

	/**
	 * 把DownloadManager里所有的DownloadTask列表传到每个container里
	 */
	public void setDownloadTaskList(List<DownloadTask> taskList);

	/**
	 * 省流量模式发生改变后的回调接口
	 */
	public void onTrafficSavingModeChange();
	
	/**
	 * 把可更新应用数据设到container里
	 * 
	 * @param value 可更新数据或者异常信息
	 * @param state 状态：请看 {@link AppsManageViewController.MSG_ID_NOT_START,AppsManageViewController.MSG_ID_START,AppsManageViewController.MSG_ID_FINISH,AppsManageViewController.MSG_ID_EXCEPTION}
	 */
	public void setUpdateData(Object value, int state);
	
	/**
	 * 填充MultiContainer，该方法只对MultiContainer有效，其他container不用处理
	 * 
	 * @param cBeans 按钮tab栏标题
	 * @param containers 子container
	 */
	public void fillupMultiContainer(List<CategoriesDataBean> cBeans, List<IContainer> containers);
	
	/**
	 * 从view结构移除子container并把子container列表清空，该方法只对MultiContainer有效，其他container不用处理
	 */
	public void removeContainers();
	
	/**
	 * 获取该container的所有子container，如果没用子container，则返回自己本身
	 */
	public List<IContainer> getSubContainers();
	
	/**
	 * 如果当前container是MultiContainer的子container时，在MultiContainer切换界面时会通知被移除和被加载这两个页面它们的可见性发生变化
	 * 
	 * @param visiable false表示该container从可见状态被移除（会先removeView再通知子container)
	 * 				   true表示该container从不可见变成可见（会先addView再通知子container）
	 */
	public void onMultiVisiableChange(boolean visiable);
	
	/**
	 * 通知container后台正在加载最新数据
	 */
	public void prevLoading();
	
	/**
	 * 通知container后台加载最新数据完毕
	 */
	public void prevLoadFinish();
	
	/**
	 * 设置container工厂，暂时只用在EmptyContainer
	 */
	public void setBuilder(ContainerBuiler builder);
}

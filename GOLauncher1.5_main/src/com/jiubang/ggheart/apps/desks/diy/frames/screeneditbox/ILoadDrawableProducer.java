package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

/**
 * 
 * 类描述:{@link LoadingDrawableItem} 的生产者 功能详细描述:
 * 
 * @author guoyiqing
 * @date [2012-8-11]
 */
public interface ILoadDrawableProducer {

	/**
	 * 功能简述:异步按需加载Drawable 功能详细描述: 注意:
	 * 
	 * @param position
	 */
	public void addLoadTaskItem(int position);

}

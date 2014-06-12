/*
 * 文 件 名:  ThemeBannerBean.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  rongjinsong
 * 修改时间:  2012-10-18
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.data.theme.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-10-18]
 */
public class ThemeBannerBean extends BaseBean {
	public int mLoopNum;
	public ArrayList<BannerElement> mElements;
	public int mType;
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2012-10-18]
	 */
	public class BannerElement implements Serializable {
		public int mId; //分类id
		public String mSDate; //起始日期
		public String mEDate; //结束日期
		/**
		 * 分类属性id,值保存在表t_type_property中
		 * 1：桌面主题
		 * 2：GO锁屏
		 * 3：插件
		 * 4：GO系列软件
		 * 5：专题
		 * 6：静态壁纸
		 * 7：游戏
		 * 8：动态壁纸
		 */
		public int mPropertyid;
		public String mName; //分类名称
		public String[] mImgids; //该分类使用的背景图，为空时，客户端采用默认图片
		public int mSource;
		public List<String> mImgUrl;
		public String mGroup;
		public String[] mPkgs;
	}

}

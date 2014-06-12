/**
 * 
 */
package com.jiubang.ggheart.appgame.base.bean;

import java.util.ArrayList;

/**
 * 安全认证信息
 * @author liguoliang
 *
 */
public class SecurityInfo {

	/**
	 * 是否有安全认证
	 */
	public int mScore;

	/**
	 * Go认证名称
	 */
	public String mName;

	/**
	 * Go认证图标
	 */
	public String mIcon;

	/**
	 * Go认证大图标识
	 */
	public String mPic;

	/**
	 * Go认证结果描述
	 */
	public String mResultMsg;

	/**
	 * 第三方认证列表
	 */
	public ArrayList<ThirdSecurityItem> mThirdSecurityList;

	/**
	 * 第三方安全认证单元
	 * @author liguoliang
	 *
	 */
	public class ThirdSecurityItem {
		/**
		 * 认证软件包名
		 */
		public String mThirdPkgName;

		/**
		 * 认证软件ICON
		 */
		public String mThirdIconUrl;

		/**
		 * 认证软件名
		 */
		public String mThirdName;

		/**
		 * 认证结果
		 */
		public String mThirdResultMsg;
	}
}

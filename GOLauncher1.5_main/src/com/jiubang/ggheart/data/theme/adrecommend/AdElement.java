package com.jiubang.ggheart.data.theme.adrecommend;

import java.io.Serializable;

import android.graphics.Bitmap;

/**
 * 广告信息
 * 
 * @author HuYong
 * @version 1.0
 * 
 */
public class AdElement implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1000L;

	public static final int DISPLAY_TYPE_PURE_TEXT = 0x01;
	public static final int DISPLAY_TYPE_PURE_ICON = 0x02;
	public static final int DISPLAY_TYPE_TEXT_ICON = 0x03;
	// public static final int OPT_NULL = 0x00;
	// public static final int OPT_DOWNLOAD = 0x01;
	// public static final int OPT_INSTALL = 0x02;
	// public static final int OPT_CALL = 0x03;
	// public static final int OPT_SMS = 0x04;
	// public static final int OPT_BROWSE = 0x05;

	public String mAdName; // 广告名称
	public int mAdID; // 广告ID
	public String mAppID; // 软件UID
	public int mAdDisplayType; // 展示类型
	public byte[] mIconData; // 广告图片
	public String mAdText; // 广告内容
	public int mMaxDisplayCount; // 最多显示次数
	public int mDelay; // 弹出间隔
	public int mPriority; // 优先级
	public int mAdOptCode; // 操作码
	public int mSrcSize; // 包大小
	public String mAdOptData; // 操作数

	public transient Bitmap mIcon = null;// 不进行序列化
	public String mIconFormat = null;

	/**
	 * 用户统计数据
	 * 
	 * @author HuYong
	 * @version 1.0
	 */
	public int mDisplayCount; // 显示计数
	public int mClickCount; // 点击计数
	public long mDisplayTime; // 上一次显示的时间
	public long mClickTime; // 上一次点击时间

}

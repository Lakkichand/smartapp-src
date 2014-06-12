package com.jiubang.ggheart.apps.gowidget.gostore.net.databean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 列表数据
 * 
 * @author huyong
 * 
 */
@SuppressWarnings("serial")
public class ListElementBean extends BaseBean {

	public static final int ELEMENTTYPE_SOFTWARE = 1;
	public static final int ELEMENTTYPE_LINKED = 2;
	public static final int ELEMENTTYPE_TOPIC = 3;
	public static final int ELEMENTTYPE_SORT = 4;
	public static final int ELEMENTTYPE_LIVEWALLPAPER = 5;
	public static final int ELEMENTTYPE_WALLPAPER = 6;

	public int mTotalNum = 0; // 元素项个数
	public int mTypeId = 0; // 所属类别ID
	public String mBannerId = ""; // 所属Banner图片ID
	public int mCurrentPage = 0; // 当前页码
	public int mTotalPage = 0; // 总页数
	public int mElementCount = 0; // 下发数据条数
	public byte mElementType = 0; // 元素类型
	public int mSearchId = 0;
	public int mPropertyId = 0; // 父分类类型ID

	/**
	 * 
	 * <br>类描述:Element的父类
	 * <br>功能详细描述:
	 * 
	 * @author  zhouxuewen
	 * @date  [2012-9-12]
	 */
	public class Element implements Serializable {
		public int mElementType = 0; // 类型
		public int mDateType = 0; // 内容类型
		public int mIsHot = 0; // 是否热门标识，1：是 0：否
		public int mIsNew = 0; // 是否新应用标识，1：是 0：否
		public String mStar = null; // 特定渠道区域下软件的星级
		public String mLogoIconId = null; // 应用iconid

	}

	public ArrayList<Element> mElementsList = null;

	/**
	 * 
	 * <br>类描述:软件类的Element
	 * <br>功能详细描述:
	 * 
	 * @author  zhouxuewen
	 * @date  [2012-9-12]
	 */
	public class SoftwareElement extends Element {

		public int mId = 0; // 软件id
		public int mSource = 0; // 来源id 【0：精品 1：木瓜】
		public int mStyle = 0; // 详情风格【0：默认风格 1：大图风格】
		public String mCallbackUrl = ""; // 回调URL
		public String mName = null; // 产品名称
		public String mType = null; // 产品类别
		public String mDetail = null; // 产品描述
		public String mVersion = null; // 版本
		public int mVersionCode = 0; // 版本code
		public String mPkgName = null; // 包名
		public String mPrice = null; // 价格（单位美元）
		public String mSize = null; // 包大小
		public int mUrlNum = 0; // 下发url地址个数
		public HashMap<Integer, String> mUrlMap = null; // 链接地址
		public String mImgId = null; // 缩略图id
		public String mUpdateTime = null; // 更新时间
		public String mDownloadCount = null; // 下载量
		public int mSortId = 0; // 分类id
		public int mParentId = 0; // 分类夫分类ID
		public int mPropertyId = 0; // 分类属性ID
		public byte mChildtype = 0; // 分类类别
		public String mSortName = null; // 分类名字
		public String[] mPaytype = null; // 付费类型
		public String mPayid = null; // 电子市场付费ID
		public String mDownurl = null; // 资源包下载url(zip包)
		public String mLocker = null; // 配套锁屏包名
		public String mWidget = null; // 配套插件包名

		public SoftwareElement() {
			mElementType = ELEMENTTYPE_SOFTWARE;
		}
	}

	/**
	 * 
	 * <br>类描述:链接类的Element
	 * <br>功能详细描述:
	 * 
	 * @author  zhouxuewen
	 * @date  [2012-9-12]
	 */
	public class LinkedElement extends Element {
		public int mId = 0; // 链接id
		public String mName = null; // 标题名称
		public String mType = null; // 所属类别
		public String mDetail = null; // 描述
		public String mSource = null; // 来源
		public String mUrl = null; // 跳转链接
		public String mImgId = null; // 缩略图id
		public String mUpdateTime = null; // 更新时间

		public LinkedElement() {
			mElementType = ELEMENTTYPE_LINKED;
		}
	}

	/**
	 * 
	 * <br>类描述:壁纸类的Element
	 * <br>功能详细描述:
	 * 
	 * @author  zhouxuewen
	 * @date  [2012-9-12]
	 */
	public class WallpaperElement extends Element {
		public int mIndex = 0; // 元素所有列表中位置
		public int mId = 0; // 图片唯一id
		public String mImgid = null; // 缩略图ID(最小图）
		public String mPreimgid = null; // 预览图ID(中等图）
		public String mDownimgurl = null; // 图片下载地址（最大图）
		public String mSize = null; // 图片大小

		public WallpaperElement() {
		}
	}
}

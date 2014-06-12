package com.jiubang.ggheart.apps.desks.imagepreview;

/**
 * 图片浏览返回类型
 * 
 * @author masanbing
 * 
 */
public class ImagePreviewResultType {
	/**
	 * 资源
	 */
	public static final int TYPE_RESOURCE_ID = 0;
	/**
	 * 文件
	 */
	public static final int TYPE_IMAGE_FILE = 1;
	/**
	 * 默认图
	 */
	public static final int TYPE_DEFAULT = 2;
	/**
	 * 主题包
	 */
	public static final int TYPE_PACKAGE_RESOURCE = 3;
	/**
	 * 第三方应用URI
	 */
	public static final int TYPE_IMAGE_URI = 4;
	/**
	 * 文件夹换应用程序图标
	 */
	public static final int TYPE_APP_ICON = 5;

	/**
	 * 图标类型
	 */
	public static final String TYPE_STRING = "type";

	/**
	 * 图标ID
	 */
	public static final String IMAGE_ID_STRING = "imageid";

	/**
	 * 图标file path
	 */
	public static final String IMAGE_PATH_STRING = "imagepath";

	/**
	 * 主题包名
	 */
	public static final String IMAGE_PACKAGE_NAME = "imagepackagename";
}

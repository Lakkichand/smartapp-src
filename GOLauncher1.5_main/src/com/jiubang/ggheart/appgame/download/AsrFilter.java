/*
 * 文 件 名:  AsrFilter.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-9-21
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-9-21]
 */
public class AsrFilter {
	
	/**
	 * 过滤运营商数据
	 * "text/html"是连接CMCC但未通过验证，这时候进行下载行为返回的contentType, 因此过滤
	 * 下载GO文件, contentType == null ， 所以返回false
	 * 以后下载服务遇到需要过滤的其它信息，在filterArray进行添加
	 */
	private String [] mFilterArray = new String[]{"text/html"};
	
	public boolean isAsrResponse(String contentType) {
		if (contentType == null) {
			return false;
		}
		boolean flag = false;
		for (int i = 0; i < mFilterArray.length; i++) {
			if (contentType.indexOf(mFilterArray[i]) >= 0) {
				flag = true;
				break;
			}
		}
		return flag;
	}
}

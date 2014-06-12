/*
 * 文 件 名:  ICompress.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liguoliang
 * 修改时间:  2012-9-25
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.gau.utils.cache.compress;

/**
 * <br>类描述:压缩接口
 * <br>功能详细描述:
 * 
 * @author  liguoliang
 * @date  [2012-9-25]
 */
public interface ICompress {
	/** <br>功能简述:压缩
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param src	原数据
	 * @return
	 */
	byte[] compress(byte[] src);
	
	/** <br>功能简述:解压
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param src
	 * @return
	 */
	byte[] decompress(byte[] src);
}

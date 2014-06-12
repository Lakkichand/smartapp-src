package com.go.util;

/**
 * 对数组进行操作的工具类
 * 
 * @author wangzhuobin
 * 
 */
public class ArrayUtils {

	/**
	 * 从int数组里面删除某一项 返回一个全新的数组引用 如果源数组没有包含要删除的项，就返回源数组
	 * 
	 * @param array
	 * @param deletValue
	 * @return
	 */
	public static int[] delete(int[] array, int deletValue) {
		int[] result = null;
		if (array != null) {
			int i = 0;
			int length = array.length;
			// 先找找有没有那一项
			for (; i < length; i++) {
				if (deletValue == array[i]) {
					break;
				}
			}
			if (i < length) {
				// 如果有那一项
				result = new int[length - 1];
				for (int j = 0, k = 0; j < length; j++) {
					if (j != i) {
						result[k] = array[j];
						k++;
					} else {
						continue;
					}
				}
			} else {
				// 如果没有那一项
				result = array;
			}
		}
		return result;
	}
}

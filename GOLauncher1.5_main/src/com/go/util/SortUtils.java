package com.go.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.gau.go.launcherex.R;
import com.jb.util.pylib.Hanzi2Pinyin;

/**
 * 
 * <br>类描述: 排序工具类
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-22]
 */
public class SortUtils {

	public static final int COMPARE_TYPE_STRING = 0;
	public static final int COMPARE_TYPE_INT = 1;
	public static final int COMPARE_TYPE_LONG = 2;

	@SuppressWarnings("unchecked")
	public static <T> void sortSomePriority(List<T> list, final String pritMethod,
			final String method, final Class[] methodArgsClass, final Object[] methodArgs,
			final String order, final int compareType) {
		Collections.sort(list, new Comparator() {

			@Override
			public int compare(Object object1, Object object2) {
				try {
					Method compareMethod1 = object1.getClass().getMethod(pritMethod);
					Method compareMethod2 = object2.getClass().getMethod(pritMethod);

					// if (null == compareMethod1.invoke(object1, null)
					// || null == compareMethod2.invoke(object2, null)) {
					// return -1;
					// }
					if (null == compareMethod1.invoke(object1, null)
							|| null == compareMethod2.invoke(object2, null)) {
						return 0;
					}

					boolean prit1 = (Boolean) compareMethod1.invoke(object1, null);
					boolean prit2 = (Boolean) compareMethod2.invoke(object2, null);
					if (prit1 && !prit2) {
						return -1;
					} else if (!prit1 && prit2) {
						return 1;
					}

				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return doCompare(method, methodArgsClass, methodArgs, order, object1, object2,
						compareType);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static <T> void sort(List<T> list, final String method, final Class[] methodArgsClass,
			final Object[] methodArgs, final String order) {
		// TODO: 宽松汉字拼音排序
		Collections.sort(list, new Comparator() {

			@Override
			public int compare(Object object1, Object object2) {
				return doCompare(method, methodArgsClass, methodArgs, order, object1, object2,
						COMPARE_TYPE_STRING);
			}
		});
	}

	private static <T> int doCompare(final String method, final Class[] methodArgsClass,
			final Object[] methodArgs, final String order, Object object1, Object object2,
			int compareType) {
		// TODO Auto-generated method stub
		int result = 0;
		try {
			Method compareMethod1 = object1.getClass().getMethod(method, methodArgsClass);
			Method compareMethod2 = object2.getClass().getMethod(method, methodArgsClass);

			if (null == compareMethod1.invoke(object1, methodArgs)
					|| null == compareMethod2.invoke(object2, methodArgs)) {
				return result;
			}

			if (compareType == COMPARE_TYPE_INT) {
				// 按int类型比较
				int value1 = (Integer) compareMethod1.invoke(object1, methodArgs);
				int value2 = (Integer) compareMethod2.invoke(object2, methodArgs);
				if (value1 == value2) {
					result = 0;
					return result;
				}
				if (order != null && "DESC".equals(order)) {
					result = value2 > value1 ? 1 : -1;
				} else {
					result = value1 > value2 ? 1 : -1;
				}

			} else if (compareType == COMPARE_TYPE_LONG) {
				// 按long类型比较
				long value1 = (Long) compareMethod1.invoke(object1, methodArgs);
				long value2 = (Long) compareMethod2.invoke(object2, methodArgs);
				if (value1 == value2) {
					result = 0;
					return result;
				}
				if (order != null && "DESC".equals(order)) {
					result = value2 > value1 ? 1 : -1;
				} else {
					result = value1 > value2 ? 1 : -1;
				}
			} else {
				// 按字符串类型比较
				String str1 = compareMethod1.invoke(object1, methodArgs).toString();
				String str2 = compareMethod2.invoke(object2, methodArgs).toString();
				Collator collator = null;

				/**
				 * @edit by huangshaotao
				 * @date 2012-7-31
				 *       在4.1的系统使用Locale.CHINESE按名称排序时会把汉字排在英文前面（原因未明），
				 *       因此针对4.1或以上系统，使用Locale.ENGLISH
				 */
				if (Build.VERSION.SDK_INT < 16) {
					collator = Collator.getInstance(Locale.CHINESE);
				} else {
					collator = Collator.getInstance(Locale.ENGLISH);
				}

				if (collator == null) {
					collator = Collator.getInstance(Locale.getDefault());
				}
				//
				// collator = Collator.getInstance(Locale.getDefault());
				if (order != null && "DESC".equals(order)) {
					result = collator.compare(str2.toUpperCase(), str1.toUpperCase());
				} else {
					result = collator.compare(str1.toUpperCase(), str2.toUpperCase());
				}
			}

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			// e.printStackTrace();
		}
		return result;
	}

	/**
	 * 比较两个字符串
	 * 
	 * @param str1
	 * @param str2
	 * @param order
	 * @return 大于0表示str1应该在str2的后面，小于0则在前面
	 */
	public static int compareInLetter(final String str1, final String str2) {
		int result = 0;
		if (str1 != null && str2 != null) {
			Collator collator = null;
			try {
				collator = Collator.getInstance(Locale.CHINESE);
			} catch (Throwable e) {

			}
			if (collator == null) {
				collator = Collator.getInstance(Locale.getDefault());
			}
			result = collator.compare(str1.toUpperCase(), str2.toUpperCase());
		}
		return result;
	}

	// public static void sortItemInfo(ArrayList<ItemInfo> itemInfoList, final
	// Context context)
	// {
	// //获取排列方式
	// final int sortType =
	// AppCore.getInstance().getSettingControler().getFunAppSetting().getSortType();
	//
	// final Comparator<Object> comparator = new Comparator<Object>(){
	//
	// private Collator collator = Collator.getInstance(Locale.CHINESE);
	// @Override
	// public final int compare(Object object1, Object object2)
	// {
	// if(!(object1 instanceof ShortCutInfo && object2 instanceof ShortCutInfo))
	// {
	// return 0;
	// }
	// switch (sortType)
	// {
	// //按字母方式排序
	// case FunAppSetting.SORTTYPE_LETTER:
	// CharSequence str1 = ((ShortCutInfo)object1).mTitle;
	// CharSequence str2 = ((ShortCutInfo)object2).mTitle;
	// //如果获取到的title为空则
	// if(str1 == null)
	// {
	// return -1;
	// }
	// else if(str2 == null)
	// {
	// return 1;
	// }
	// return collator.compare(str1, str2);//负值的话str1<str2
	// //按时间由近到远排序
	// case FunAppSetting.SORTTYPE_TIMENEAR:
	//
	// //按时间由远到近排序
	// case FunAppSetting.SORTTYPE_TIMEREMOTE:
	// Long time1 = getAppTime(((ShortCutInfo)object1).mIntent, context);
	// Long time2 = getAppTime(((ShortCutInfo)object2).mIntent, context);
	// int result = 0;
	// if(time1<time2)
	// {
	// result = -1;
	// }
	// else if(time1>time2)
	// {
	// result = 1;
	// }
	// if(result==0)
	// {
	// CharSequence strAgain1 = ((ShortCutInfo)object1).mTitle;
	// CharSequence strAgain2 = ((ShortCutInfo)object2).mTitle;
	// //防止空指针异常
	// if(strAgain1 == null)
	// {
	// return -1;
	// }
	// else if(strAgain2 == null)
	// {
	// return 1;
	// }
	// return collator.compare(strAgain1, strAgain2);//负值的话strAgain1<strAgain2
	// }
	// else
	// {
	// return sortType==FunAppSetting.SORTTYPE_TIMENEAR? -result:result;//type2
	// 与 type1 的排序方式相反
	// }
	//
	// default:
	// break;
	// }
	// return 0;
	// }
	//
	// };//end mComparator
	// //排序
	// Collections.sort(itemInfoList, comparator);
	// }

	/**
	 * 获取安装时间
	 * 
	 * @param packageMgr
	 * @return
	 */
	private static long getAppTime(Intent intent, Context context) {
		String sourceDir = null;
		long modifyTime = 0;
		try {
			PackageManager packageMgr = context.getPackageManager();
			sourceDir = packageMgr.getActivityInfo(intent.getComponent(), 0).applicationInfo.sourceDir;
			File file = new File(sourceDir);
			modifyTime = file.lastModified();
			file = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return modifyTime;
	}

	/**
	 * 通过int值进行比较
	 * 
	 * @author huyong
	 * @param <T>
	 * @param list
	 * @param method
	 * @param methodArgsClass
	 * @param methodArgs
	 * @param order
	 */
	public static <T> void sortByInt(List<T> list, final String method,
			final Class[] methodArgsClass, final Object[] methodArgs, final String order) {
		// TODO: 宽松汉字拼音排序
		Collections.sort(list, new Comparator() {

			@Override
			public int compare(Object object1, Object object2) {
				return doCompare(method, methodArgsClass, methodArgs, order, object1, object2,
						COMPARE_TYPE_INT);
			}
		});
	}

	public static <T> void sortByLong(List<T> list, final String method,
			final Class[] methodArgsClass, final Object[] methodArgs, final String order) {
		// TODO: 宽松汉字拼音排序
		Collections.sort(list, new Comparator() {

			@Override
			public int compare(Object object1, Object object2) {
				return doCompare(method, methodArgsClass, methodArgs, order, object1, object2,
						COMPARE_TYPE_LONG);
			}
		});
	}

	/**
	 * <br>功能简述:将一个包含有汉字的字符串转换成拼音字符串（如果有的话）
	 * <br>功能详细描述:将一个包含有汉字的字符串转换成拼音字符串（如果有的话）
	 * <br>注意:
	 * @param text 
	 * @return 有可能为null，使用前先判断
	 */
	public static String changeChineseToSpell(final Context context, String text) {
		if (text == null || "".equals(text)) {
			return text;
		}

		StringBuffer results = new StringBuffer();
		Hanzi2Pinyin pInstance = Hanzi2Pinyin.getInstance(context, R.raw.unicode2pinyin);
		if (pInstance == null) {
			return text;
		}

		int size = text.length();
		for (int i = 0; i < size; i++) {
			// 是汉字则转成拼音，不是则原样转成数组返回
			char key = text.charAt(i);
			if (isChinese(key)) {
				int code = key;
				String[] temp = pInstance.GetPinyin(code);
				if (temp != null && temp.length > 0) {
					results.append(temp[0]);
				}
			} else {
				results.append(String.valueOf(key));
			}
		}
		return results.toString();
	}

	/**
	 * <br>功能简述:判断一个字符是否汉字
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param key
	 * @return
	 */
	private static boolean isChinese(char key) {
		boolean isHanzi = false;
		if (key >= 0x4e00 && key <= 0x9fa5) {
			isHanzi = true;
		}
		return isHanzi;
	}

	private static boolean isContainChinese(String text) {
		boolean ret = false;
		if (text == null || "".equals(text)) {
			ret = false;
		} else {
			int size = text.length();
			for (int i = 0; i < size; i++) {
				if (isChinese(text.charAt(i))) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}
	/**
	 * <br>功能简述:功能表应用程序排序，汉字会按拼音进行排序
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param list
	 * @param pritMethod
	 * @param method
	 * @param methodArgsClass
	 * @param methodArgs
	 * @param order
	 */
	public static <T> void sortForApps(final Context context, List<T> list,
			final String pritMethod, final String method, final Class[] methodArgsClass,
			final Object[] methodArgs, final String order) {
		Collections.sort(list, new Comparator() {

			@Override
			public int compare(Object object1, Object object2) {
				try {
					Method compareMethod1 = object1.getClass().getMethod(pritMethod);
					Method compareMethod2 = object2.getClass().getMethod(pritMethod);

					// if (null == compareMethod1.invoke(object1, null)
					// || null == compareMethod2.invoke(object2, null)) {
					// return -1;
					// }
					if (null == compareMethod1.invoke(object1)
							|| null == compareMethod2.invoke(object2)) {
						return 0;
					}

					boolean prit1 = (Boolean) compareMethod1.invoke(object1);
					boolean prit2 = (Boolean) compareMethod2.invoke(object2);
					if (prit1 && !prit2) {
						return -1;
					} else if (!prit1 && prit2) {
						return 1;
					}

				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return doCompareForApps(context, method, methodArgsClass, methodArgs, order,
						object1, object2);
			}
		});
	}

	private static <T> int doCompareForApps(final Context context, final String method,
			final Class[] methodArgsClass, final Object[] methodArgs, final String order,
			Object object1, Object object2) {
		int result = 0;
		try {
			Method compareMethod1 = object1.getClass().getMethod(method, methodArgsClass);
			Method compareMethod2 = object2.getClass().getMethod(method, methodArgsClass);

			if (null == compareMethod1.invoke(object1, methodArgs)
					|| null == compareMethod2.invoke(object2, methodArgs)) {
				return result;
			}

			// 按字符串类型比较
			String str1 = compareMethod1.invoke(object1, methodArgs).toString();
			String str2 = compareMethod2.invoke(object2, methodArgs).toString();
			Collator collator = null;

			//			//如果两个字符串都包含有汉字，则将其转成拼音
			//			if (isContainHanzi(str1)&&isContainHanzi(str2)&&str1.length()>0&&str2.length()>0) {
			//				if (!isChinese(str1.charAt(0))||!isChinese(str2.charAt(0))) {
			//					if (!isChinese(str1.charAt(0))&&!isChinese(str2.charAt(0))) {
			//						str1 = changeChineseToSpell(context, str1);
			//						str2 = changeChineseToSpell(context, str2);
			//					}else if(!isChinese(str1.charAt(0))&&isChinese(str2.charAt(0))){
			//						return 1;
			//					}else if(isChinese(str1.charAt(0))&&!isChinese(str2.charAt(0))){
			//						return -1;
			//					}
			//				}else {
			//					str1 = changeChineseToSpell(context, str1);
			//					str2 = changeChineseToSpell(context, str2);
			//				}				
			//			}

			if (str1.length() > 0 && str2.length() > 0) {
				if (isChinese(str1.charAt(0)) && isChinese(str2.charAt(0))) {
					str1 = changeChineseToSpell(context, str1);
					str2 = changeChineseToSpell(context, str2);
				} else if (!isChinese(str1.charAt(0)) && isChinese(str2.charAt(0))) {
					if (order != null && "DESC".equals(order)) {
						return 1;
					} else {
						return -1;
					}
				} else if (isChinese(str1.charAt(0)) && !isChinese(str2.charAt(0))) {
					if (order != null && "DESC".equals(order)) {
						return -1;
					} else {
						return 1;
					}
				} else if (isContainChinese(str1) && isContainChinese(str2)) {
					str1 = changeChineseToSpell(context, str1);
					str2 = changeChineseToSpell(context, str2);
				}
			}

			/**
			 * @edit by huangshaotao
			 * @date 2012-7-31
			 *       在4.1的系统使用Locale.CHINESE按名称排序时会把汉字排在英文前面（原因未明），
			 *       因此针对4.1或以上系统，使用Locale.ENGLISH
			 */
			if (Build.VERSION.SDK_INT < 16) {
				collator = Collator.getInstance(Locale.CHINESE);
			} else {
				collator = Collator.getInstance(Locale.ENGLISH);
			}

			if (collator == null) {
				collator = Collator.getInstance(Locale.getDefault());
			}
			//
			// collator = Collator.getInstance(Locale.getDefault());
			if (order != null && "DESC".equals(order)) {
				result = collator.compare(str2.toUpperCase(), str1.toUpperCase());
			} else {
				result = collator.compare(str1.toUpperCase(), str2.toUpperCase());
			}

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			// e.printStackTrace();
		}
		return result;
	}
}

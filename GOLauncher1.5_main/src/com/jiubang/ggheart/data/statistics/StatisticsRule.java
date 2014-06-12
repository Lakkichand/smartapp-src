package com.jiubang.ggheart.data.statistics;

/**
 * 统计规则
 * 
 * @author masanbing
 * 
 */
public class StatisticsRule {
	/**
	 * 自增
	 */
	public static final int STATISTICSRULE_INCREMENT = 0;
	/**
	 * 增加一定值
	 */
	public static final int STATISTICSRULE_ADDVALUE = 1;
	/**
	 * 更新值
	 */
	public static final int STATISTICSRULE_UPDATEVALUE = 2;

	/**
	 * 规则个数
	 */
	public static final int STATISTICSRULE_COUNT = 3;

	/**
	 * 判断规则是否满足统计规则
	 * 
	 * @param rule
	 *            统计规则
	 * @return 是否满足
	 */
	public static boolean checkRule(int rule) {
		if (rule >= 0 && rule < STATISTICSRULE_COUNT) {
			return true;
		}
		return false;
	}
}

package com.jiubang.ggheart.apps.security.modle;
/**
 * 
 * <br>类描述:用于保存多个引擎查杀总结果的表格
 * <br>功能详细描述:
 * 
 * @author  huangshaotao
 * @date  [2012-9-17]
 */
public class AllEngineCheckResultsTable {

	static public String TABLENAME = "allenginecheckresultstable";
	static public String DANGERLEVEL = "dangerlevel"; //危险等级
	static public String DANGERRESULTCOUNT = "drcount"; // 危险子结果个数
	static public String PKNAME = "pkname"; //包名

	public static String CREATETABLESQL = "create table " + TABLENAME + "(" + DANGERRESULTCOUNT
			+ " numeric, " + DANGERLEVEL + " numeric, " + PKNAME + " text " + ");";
}

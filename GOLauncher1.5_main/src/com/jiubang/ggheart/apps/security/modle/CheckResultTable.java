package com.jiubang.ggheart.apps.security.modle;
/**
 * 
 * <br>类描述:用于保存单个引擎查杀结果的表格
 * <br>功能详细描述:
 * 
 * @author  huangshaotao
 * @date  [2012-9-17]
 */
public class CheckResultTable {

	static public String TABLENAME = "checkresulttable";
	static public String PKNAME = "pkname"; //包名
	static public String RESULTCODE = "resultcode"; // 结果代码
	static public String ENGINEKEY = "enginekey"; //引擎对应的key
	static public String MD5 = "md5";
	static public String DES = "des"; //描述信息	

	public static String CREATETABLESQL = "create table " + TABLENAME + "(" + PKNAME + " text, "
			+ RESULTCODE + " numeric, " + ENGINEKEY + " text, " + MD5 + " text, " + DES + " text "
			+ ");";
}

package com.escape.local.http.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegxTest {
	public static void main(String[] args) {
		Pattern p = Pattern.compile("f(.+?)k");
		Matcher m = p.matcher("xxxxxxxxxfckfkkfkf");
		while (m.find()) {
			String s0 = m.group();
			String s1 = m.group(1);
			System.out.println(s0 + "||" + s1);
		}
		System.out.println("---------");
		m.reset("fucking!");
		while (m.find()) {
			System.out.println(m.group());
		}

		Pattern p1 = Pattern.compile("f(.+?)i(.+?)h");
		Matcher m1 = p1.matcher("finishabigfishfrish");
		while (m1.find()) {
			String s0 = m1.group();
			String s1 = m1.group(1);
			String s2 = m1.group(2);
			System.out.println(s0 + "||" + s1 + "||" + s2);
		}

		System.out.println("---------");
		Pattern p3 = Pattern
				.compile("(19|20)\\d\\d([- /.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])");
		Matcher m3 = p3
				.matcher("1900-01-01 2007/08/13 1900.01.01 1900 01 01 1900-01.01 1900 13 01 1900 02 31");
		while (m3.find()) {
			System.out.println(m3.group(0) + " : " + m3.group(1) + " : "
					+ m3.group(2) + " : " + m3.group(3));
		}
	}
}
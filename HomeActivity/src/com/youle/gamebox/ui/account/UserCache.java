package com.youle.gamebox.ui.account;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.youle.gamebox.ui.bean.LogAccount;
import com.youle.gamebox.ui.util.SDKUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;


public class UserCache implements UserOperation {
	public static String CACHE_PATH = "/zhidian/a/d/";//需要和SDK2.0兼容
	public static String FILE_NAME = "data.xml";
	public static final String USRE_NAME = "username";
	public static final String PASS_WORD = "password";
	public static final String ACCOUNT = "account";
	public static final String LAST_TIME = "lastLoginTime";
	public static final String LIST = "list";
	public static final String OPTION = "option";

	public void saveAcount(LogAccount account) {
		List<LogAccount> list = getAccountList();
		if (list == null) {
			list = new ArrayList<LogAccount>();
			list.add(account);
		} else {// 如果是同一个账号，则只需要更新时间
			LogAccount targetLogAccount = getAccountByLogNameFromList(list,
					account.getUserName());
			if (targetLogAccount != null) {
				targetLogAccount.setLastLogin(account.getLastLogin());
				list.remove(targetLogAccount);
			}
			list.add(account);
		}
		saveList(list);
	};

	public void deleteAccount(String username) {
		List<LogAccount> list = getAccountList();
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				LogAccount account = list.get(i);
				if (username.equals(account.getUserName())) {
					list.remove(account);
				}
			}
			saveList(list);
		}
	}

	public List<LogAccount> getAccountList() {
		File file = new File(SDKUtils.getSKCardPath() + CACHE_PATH + FILE_NAME);
		if (!file.exists()) {
			return null;
		} else {
			try {
				InputStream in = new FileInputStream(file);
				return getAccountOrderList(in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	private LogAccount getAccountByLogNameFromList(List<LogAccount> list,
			String name) {
		for (LogAccount accunt : list) {
			if (accunt.getUserName().equals(name)) {
				return accunt;
			}
		}
		return null;
	}

	private void saveList(List<LogAccount> list) {
		File file = new File(SDKUtils.getSKCardPath() + CACHE_PATH + FILE_NAME);
		if (!file.exists()) {
			File parent = file.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
		}
		try {
			OutputStream os = new FileOutputStream(file);
			Writer writer = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(writer);
			String content = produceXml(list);
			bw.write(content);
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return 生成的xml文件的字符串表示
	 */
	private String produceXml(List<LogAccount> accountList) {

		StringWriter stringWriter = new StringWriter();
		try {
			// 获取XmlSerializer对象
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlSerializer xmlSerializer = factory.newSerializer();
			// 设置输出流对象
			xmlSerializer.setOutput(stringWriter);
			xmlSerializer.startDocument("utf-8", false);
			xmlSerializer.startTag(null, LIST);
			for (LogAccount logaccout : accountList) {
				xmlSerializer.startTag(null, ACCOUNT);
				
				xmlSerializer.startTag(null, USRE_NAME);
				xmlSerializer.text(logaccout.getUserName()+"");
				xmlSerializer.endTag(null, USRE_NAME);
			
				if(logaccout.getPassword()!=null){
					xmlSerializer.startTag(null, PASS_WORD);
					xmlSerializer.text(logaccout.getEncodePassword());
					xmlSerializer.endTag(null, PASS_WORD);
				}

				xmlSerializer.startTag(null, LAST_TIME);
				xmlSerializer.text(logaccout.getLastLogin() + "");
				xmlSerializer.endTag(null, LAST_TIME);

				xmlSerializer.startTag(null, OPTION);
				xmlSerializer.text(logaccout.getOption()+"");
				xmlSerializer.endTag(null, OPTION);

				xmlSerializer.endTag(null, ACCOUNT);
			}
			xmlSerializer.endTag(null, LIST);
			xmlSerializer.endDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringWriter.toString();
	}

	private List<LogAccount> getAccountOrderList(InputStream stream)
			throws ParserConfigurationException, SAXException, IOException {
		// 实例化一个SAXParserFactory对象
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		// 实例化SAXParser对象，创建XMLReader对象，解析器
		parser = factory.newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		// 实例化handler，事件处理器
		UserSAXPraserHelper helperHandler = new UserSAXPraserHelper();
		// 解析器注册事件
		xmlReader.setContentHandler(helperHandler);
		InputSource is = new InputSource(stream);
		// 解析文件
		xmlReader.parse(is);
		List<LogAccount> list = helperHandler.getList();
		if (list != null) {
			Collections.sort(list, new LastTimeOrder());
		}
		return list;
	}

	static class LastTimeOrder implements Comparator<LogAccount> {

		@Override
		public int compare(LogAccount lhs, LogAccount rhs) {
			return (int) (rhs.getLastLogin() - lhs.getLastLogin());
		}

	}
}

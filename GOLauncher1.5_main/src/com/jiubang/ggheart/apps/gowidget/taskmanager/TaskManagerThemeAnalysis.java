package com.jiubang.ggheart.apps.gowidget.taskmanager;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TaskManagerThemeAnalysis extends DefaultHandler {

	private String styleType = null;

	private boolean isFinish = true;

	private int themeId;

	public TaskManagerThemeAnalysis(String styleType, int themeId) {
		this.styleType = styleType;
		this.themeId = themeId;
	}

	protected StringBuffer buf = new StringBuffer();

	/** 存放一个样式 */
	public TaskManagerThemeBean taskManagerThemeBean = null;

	@Override
	public void startDocument() throws SAXException {

		taskManagerThemeBean = new TaskManagerThemeBean();
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
			throws SAXException {
		if (localName.equalsIgnoreCase("widget_item") && atts.getValue("style").equals(styleType)
				&& atts.getValue("theme_id").equals(Integer.toString(themeId))) {
			isFinish = false;
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

		if (!isFinish) {
			if (localName.equalsIgnoreCase("widget_bg")) {
				taskManagerThemeBean.widgetBg = buf.toString().trim();
				// System.out.println("widgetBg----" + buf.toString().trim());

			} else if (localName.equalsIgnoreCase("widget_wrap")) {
				taskManagerThemeBean.wrap = buf.toString().trim();
			} else if (localName.equalsIgnoreCase("widget_point")) {
				taskManagerThemeBean.point = buf.toString().trim();
			} else if (localName.equalsIgnoreCase("widget_kill_botton")) {
				taskManagerThemeBean.killBtn = buf.toString().trim();
				// System.out.println("killBtn----" + buf.toString().trim());
			} else if (localName.equalsIgnoreCase("widget_icolayout_bg")) {
				taskManagerThemeBean.icoLayoutBg = buf.toString().trim();
				// System.out.println("icoLayoutBg----" +
				// buf.toString().trim());

			} else if (localName.equalsIgnoreCase("widget_font_color")) {
				taskManagerThemeBean.fontColor = buf.toString().trim();
				// System.out.println("fontColor----" + buf.toString().trim());
			} else if (localName.equalsIgnoreCase("widget_font_size")) {
				taskManagerThemeBean.fontSize = buf.toString().trim();
			} else if (localName.equalsIgnoreCase("widget_refresh_botton")) {
				taskManagerThemeBean.refreshBtn = buf.toString().trim();
				// System.out.println("refreshBtn----" + buf.toString().trim());
			} else if (localName.equalsIgnoreCase("widget_bottom_layout_bg")) {
				taskManagerThemeBean.bottomLayoutBg = buf.toString().trim();
			} else if (localName.equalsIgnoreCase("widget_progress_1")) {
				taskManagerThemeBean.porcess1 = buf.toString().trim();
			} else if (localName.equalsIgnoreCase("widget_progress_2")) {
				taskManagerThemeBean.porcess2 = buf.toString().trim();
			} else if (localName.equalsIgnoreCase("widget_progress_3")) {
				taskManagerThemeBean.porcess3 = buf.toString().trim();
			} else if (localName.equalsIgnoreCase("widget_progress_4")) {
				taskManagerThemeBean.porcess4 = buf.toString().trim();
			} else if (localName.equalsIgnoreCase("widget_progress_5")) {
				taskManagerThemeBean.porcess5 = buf.toString().trim();
			}
			// else if(localName.equalsIgnoreCase("widget_text_key"))
			// {
			// taskManagerThemeBean.widgetTextKey = buf.toString().trim();
			// }

			// else if(localName.equalsIgnoreCase("widget_show_link"))
			// {
			// taskManagerThemeBean.showLink = buf.toString().trim();
			// }
			if (localName.equalsIgnoreCase("widget_item")) {
				isFinish = true;
			}
		}
		buf.setLength(0);

	}

	@Override
	public void characters(char ch[], int start, int length) {
		buf.append(ch, start, length);
	}

}

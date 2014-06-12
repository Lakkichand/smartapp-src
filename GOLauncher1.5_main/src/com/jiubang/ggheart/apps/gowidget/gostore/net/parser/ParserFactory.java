package com.jiubang.ggheart.apps.gowidget.gostore.net.parser;

import java.io.DataInputStream;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.data.theme.parser.FeaturedThemeDetailStreamParser;
import com.jiubang.ggheart.data.theme.parser.FeaturedThemeStreamParser;
import com.jiubang.ggheart.data.theme.parser.ThemeBannerStreamParser;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-10-9]
 */
public class ParserFactory {

	public static final int PARSERTYPE_RECOMMEND = 2;
	public static final int PARSERTYPE_SORTLIST = 3;
	public static final int PARSERTYPE_SEARCH = 4;
	public static final int PARSERTYPE_PRODUCT_DETAIL = 5;
	public static final int PARSERTYPE_IMAGES = 6;
	public static final int PARSERTYPE_CHANNEL = 7;
	public static final int PARSERTYPE_NEW_LIST = 8;
	public static final int PARSERTYPE_CHANNEL_CHECK = 12;
	public static final int PARSERTYPE_UPDATE_CHECK = 13;
	public static final int PARSERTYPE_APPS_UPDATE = 15;
	public static final int PARSERTYPE_FEATUREDTHEME_LIST = 25; // 精品主题
	public static final int PARSERTYPE_FEATUREDTHEME_DETAIL = 28; // 精品主题详情
	public static final int PARSERTYPE_THEME_BANNER = 31; // 主题Banner
	public static final int PARSERTYPE_THEME_SPEC = 32; // 主题专题
	public static final int PARSERTYPE_COMPLE_SORT = 34; // 复合

	public static BaseBean parseStream(DataInputStream dis, int funid) {
		BaseBean bean = null;
		switch (funid) {
			case PARSERTYPE_RECOMMEND : {
				bean = new ListStreamParser().parseHttpStreamData(dis);
			}
				break;
			case PARSERTYPE_SORTLIST : {
				ListStreamParser listStreamParser = new ListStreamParser();
				listStreamParser.mFunid = 3;
				bean = listStreamParser.parseHttpStreamData(dis);
			}
				break;
			case PARSERTYPE_SEARCH :
			case PARSERTYPE_NEW_LIST : {
				ListStreamParser listStreamParser = new ListStreamParser();
				listStreamParser.mFunid = funid;
				bean = listStreamParser.parseHttpStreamData(dis);
			}
				break;
			case PARSERTYPE_PRODUCT_DETAIL :
				bean = new DetailStreamParser().parseHttpStreamData(dis);
				break;
			case PARSERTYPE_IMAGES : {
				bean = new ImageStreamParser().parseHttpStreamData(dis);
			}
				break;
			case PARSERTYPE_CHANNEL : {
				bean = new ChannelStreamParser().parseHttpStreamData(dis);
			}
				break;
			case PARSERTYPE_CHANNEL_CHECK : {
				bean = new ChannelCheckStreamParser().parseHttpStreamData(dis);
			}
				break;
			case PARSERTYPE_UPDATE_CHECK : {
				bean = new UpdateCheckStreamParser().parseHttpStreamData(dis);
			}
				break;
			case PARSERTYPE_APPS_UPDATE : {
				bean = new AppsUpdateParser().parseHttpStreamData(dis);
			}
				break;
			case PARSERTYPE_FEATUREDTHEME_LIST : {
				bean = new FeaturedThemeStreamParser().parseHttpStreamData(dis);
			}
				break;
			case PARSERTYPE_FEATUREDTHEME_DETAIL : {
				bean = new FeaturedThemeDetailStreamParser().parseHttpStreamData(dis);
			}
			break;
			case PARSERTYPE_THEME_BANNER : {
				bean = new ThemeBannerStreamParser().parseHttpStreamData(dis);
			}
			break;
			case PARSERTYPE_THEME_SPEC : {
				bean = new SpecThemeStreamParser().parseHttpStreamData(dis);
			}
			break;
			default :
				break;
		}

		return bean;
	}

}

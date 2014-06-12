/**
 * 
 */
package com.jiubang.ggheart.data.theme.bean;

import java.util.ArrayList;
import java.util.List;

import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.WallpaperBean;

/**
 * 共用图标样式
 * 
 * @author liyuehui
 */
public class CommonStylesBean extends ThemeBean {
	public List<Layer> mIconItems;

	public CommonStylesBean() {
		mIconItems = new ArrayList<Layer>();
	}
}

/**
 * 垂直对齐
 * 
 * @author liyuehui
 * 
 */
enum Valign {
	None, Top, Mid, Botton
}

/**
 * 水平对齐
 * 
 * @author liyuehui
 * 
 */
enum Lalign {
	None, Left, Center, Right
}

class Margins {
	int Left = 0;
	int Top = 0;
	int Right = 0;
	int Botton = 0;

	Margins(String pMargins) {
		String[] margin = pMargins.split(",");
		if (margin.length >= 1) {
			Left = Integer.parseInt(margin[0]);
		}
		if (margin.length >= 2) {
			Top = Integer.parseInt(margin[2]);
		}
		if (margin.length >= 3) {
			Right = Integer.parseInt(margin[3]);
		}
		if (margin.length >= 4) {
			Botton = Integer.parseInt(margin[4]);
		}
	}

	Margins(int[] pMargins) {
		Left = pMargins.length >= 1 ? pMargins[0] : 0;
		Top = pMargins.length >= 2 ? pMargins[1] : 0;
		Right = pMargins.length >= 3 ? pMargins[2] : 0;
		Botton = pMargins.length >= 4 ? pMargins[3] : 0;
	}

	Margins(int pleft, int ptop, int pright, int pbotton) {
		Left = pleft;
		Top = ptop;
		Right = pright;
		Botton = pbotton;
	}
}

/**
 * 程序图标层
 * 
 * @author liyuehui
 * 
 */
class Layer {
	int IconWidth;
	int IconHeight;
	Valign mValign;
	Lalign mLalign;
	Margins mMargins;

	boolean IsApplicationIcon = true;
}

/**
 * 图标以外的层
 * 
 * @author liyuehui
 */
class ShowItemLayer extends Layer {
	WallpaperBean mBackImage;
	WallpaperBean mForeImage;

	ShowItemLayer() {
		IsApplicationIcon = false;
	}
}

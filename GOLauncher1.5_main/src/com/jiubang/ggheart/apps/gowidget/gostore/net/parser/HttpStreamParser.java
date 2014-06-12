package com.jiubang.ggheart.apps.gowidget.gostore.net.parser;

import java.io.DataInputStream;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;

public abstract class HttpStreamParser {

	// public abstract void parseHttpStreamData(final InputStream inputStream,
	// BaseBean baseBean);
	public abstract BaseBean parseHttpStreamData(DataInputStream dis);
}

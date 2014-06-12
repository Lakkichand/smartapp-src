package com.jiubang.ggheart.apps.gowidget.gostore.net;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import com.gau.utils.net.operator.StreamHttpOperator;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.BasicResponse;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.parser.ParserFactory;

/**
 * 广告协议数据处理
 * 
 * @author HuYong
 * @version 1.0
 */
public class MainDataHttpOperator extends StreamHttpOperator {

	@Override
	public IResponse operateHttpResponse(THttpRequest request, HttpResponse response)
			throws IllegalStateException, IOException {
		HttpEntity entity = response.getEntity();
		InputStream ins = entity.getContent();
		ArrayList<BaseBean> reponse = parseHttpStreamData(ins);
		BasicResponse result = new BasicResponse(IResponse.RESPONSE_TYPE_STREAM, reponse);
		return result;
	}

	/**
	 * 解析http流数据
	 * 
	 * @param inStream
	 */
	private ArrayList<BaseBean> parseHttpStreamData(final InputStream inStream) {
		DataInputStream dis = new DataInputStream(inStream);
		int funNum = 0;
		try {
			funNum = dis.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<BaseBean> listBeans = new ArrayList<BaseBean>(funNum);
		try {
			int funid = 0;
			BaseBean baseBean = null;
			for (int i = 0; i < funNum; i++) {
				funid = dis.readInt();
				baseBean = ParserFactory.parseStream(dis, funid);
				if (baseBean != null) {
					baseBean.mFunId = funid;
					listBeans.add(baseBean);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dis = null;
			}
		}
		return listBeans;
	}
}

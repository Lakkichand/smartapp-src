package com.jiubang.ggheart.apps.desks.net;

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

public class VersionHttpOperator extends StreamHttpOperator {
	@Override
	public IResponse operateHttpResponse(THttpRequest request, HttpResponse response)
			throws IllegalStateException, IOException {
		HttpEntity entity = response.getEntity();
		InputStream ins = entity.getContent();
		ArrayList<ServiceIpInfoBean> reponse = parseHttpStreamData(ins);
		BasicResponse result = new BasicResponse(IResponse.RESPONSE_TYPE_STREAM, reponse);
		return result;
	}

	/**
	 * 解析http流数据
	 * 
	 * @param inStream
	 */
	private ArrayList<ServiceIpInfoBean> parseHttpStreamData(final InputStream inStream) {
		DataInputStream dis = new DataInputStream(inStream);
		int serviceNum = 0;
		try {
			serviceNum = dis.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<ServiceIpInfoBean> listBeans = new ArrayList<ServiceIpInfoBean>(serviceNum);
		try {
			ServiceIpInfoBean serviceIpInfoBean = null;
			int ipNum = 0;
			for (int i = 0; i < serviceNum; i++) {
				serviceIpInfoBean = new ServiceIpInfoBean();
				ipNum = 0;
				serviceIpInfoBean.setServerId(dis.readInt());
				ipNum = dis.readInt();
				for (int j = 0; j < ipNum; j++) {
					serviceIpInfoBean.addIp(dis.readUTF());
				}
				listBeans.add(serviceIpInfoBean);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listBeans;
	}

	/**
	 * 服务IP信息数据BEAN
	 * 
	 * @author wangzhuobin
	 * 
	 */
	public static class ServiceIpInfoBean {

		// 服务ID
		public static final int SERVICE_ID_SERVER = 1; // 服务器列表服务ID
		public static final int SERVICE_ID_VERSION_CONTROL = 11; // 版本控制服务ID

		private int mServerId; // 服务ID
		private ArrayList<String> mIps = null; // 服务IP集合

		public int getServerId() {
			return mServerId;
		}

		public void setServerId(int serverId) {
			this.mServerId = serverId;
		}

		public ArrayList<String> getIps() {
			return mIps;
		}

		public void setIps(ArrayList<String> ips) {
			this.mIps = ips;
		}

		public void addIp(String ip) {
			if (ip != null && !"".equals(ip.trim())) {
				if (null == mIps) {
					mIps = new ArrayList<String>();
				}
				if (!mIps.contains(ip)) {
					mIps.add(ip);
				}
			}
		}
	}
}

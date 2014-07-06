package com.escape.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.apache.http.util.EntityUtils;

public class PackageRequest {

	private static final int POST_DATA_LIMIT = 0x100000;

	private String mProtocol;
	private String mMethod;
	private String mUri;

	private HeaderGroup headergroup;

	private byte[] mBody;
	private boolean mAddedRangeHeader;//TODO 这个好像没用了

	public PackageRequest() {
		headergroup = new HeaderGroup();
	}

	public boolean isAddedRangeHeader() {
		return mAddedRangeHeader;
	}

	public void performAddRangeHeader() {
		mAddedRangeHeader = true;
	}

	public String getProtocol() {
		return mProtocol;
	}

	public void setProtocol(String protocol) {
		this.mProtocol = protocol.trim();
	}

	public String getMethod() {
		return mMethod;
	}

	public void setMethod(String method) {
		this.mMethod = method.trim();
	}

	public String getUri() {
		return mUri;
	}

	public void setUri(String uri) {
		this.mUri = uri;
	}

	public byte[] getEnity() {
		return mBody;
	}

	public void setEnity(byte[] entity) {
		mBody = entity;
	}

	public boolean containsHeader(String name) {
		return this.headergroup.containsHeader(name);
	}

	public Header[] getHeaders(final String name) {
		return this.headergroup.getHeaders(name);
	}

	public Header getFirstHeader(final String name) {
		return this.headergroup.getFirstHeader(name);
	}

	public Header getLastHeader(final String name) {
		return this.headergroup.getLastHeader(name);
	}

	public Header[] getAllHeaders() {
		return this.headergroup.getAllHeaders();
	}

	public void addHeader(final Header header) {
		this.headergroup.addHeader(header);
	}

	public void addHeader(final String name, final String value) {
		if (name == null) {
			throw new IllegalArgumentException("Header name may not be null");
		}
		this.headergroup.addHeader(new BasicHeader(name, value));
	}

	public void setHeader(final Header header) {
		this.headergroup.updateHeader(header);
	}

	public void setHeader(final String name, final String value) {
		if (name == null) {
			throw new IllegalArgumentException("Header name may not be null");
		}
		this.headergroup.updateHeader(new BasicHeader(name, value));
	}

	public void setHeaders(final Header[] headers) {
		this.headergroup.setHeaders(headers);
	}

	public void removeHeader(final Header header) {
		this.headergroup.removeHeader(header);
	}

	public void removeHeaders(final String name) {
		if (name == null) {
			return;
		}
		for (Iterator i = this.headergroup.iterator(); i.hasNext();) {
			Header header = (Header) i.next();
			if (name.equalsIgnoreCase(header.getName())) {
				i.remove();
			}
		}
	}

	public int[] parseRange() {
		if (containsHeader(HttpHeaders.RANGE)) {
			String value = getFirstHeader(HttpHeaders.RANGE).getValue();
			try {
				Pattern pattern = Pattern.compile("(\\d+)?-(\\d+)?");
				Matcher matcher = pattern.matcher(value);
				matcher.find();
				int[] m = new int[2];
				for (int i = 1; i <= 2; i++) {
					String s = matcher.group(i);
					if (s != null) {
						m[i - 1] = Integer.valueOf(s);
					} else {
						m[i - 1] = -1;
					}
				}
				return m;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public byte[] packaged() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeUTF(mProtocol == null ? "" : mProtocol);
			dos.writeUTF(mMethod == null ? "" : mMethod);
			dos.writeUTF(mUri == null ? "" : mUri);
			dos.writeUTF("||");
			Header[] headers = headergroup.getAllHeaders();
			for (Header header : headers) {
				dos.writeUTF(header.getName());
				dos.writeUTF(header.getValue());
			}
			dos.writeUTF("||");
			if (mBody != null) {
				dos.writeBoolean(true);
				dos.writeInt(mBody.length);
				dos.write(mBody);
			} else {
				dos.writeBoolean(false);
			}
//			return MessageHelper.compress(bos.toByteArray());
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				dos.flush();
				bos.flush();
				dos.close();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String toString() {
		String result = "";
		result += ("Method = " + mMethod + "    Protocol = " + mProtocol
				+ "    Uri = " + mUri + "\n");
		HeaderIterator iterator = headergroup.iterator();
		while (iterator.hasNext()) {
			Header header = iterator.nextHeader();
			result += (header.getName() + " : " + header.getValue() + "\n");
		}
		if (mBody != null) {
			result += ("RealRequest body length = " + mBody.length + "\n");
		}
		result += ("=========================================================="
				+ "\n");
		return result;
	}

	public static PackageRequest getInstance(HttpRequest request) {
		if (request == null) {
		    	System.err.println("PackageRequest getInstance  HttpRequest == null)");
			return null;
		}
		String method = request.getRequestLine().getMethod();
//		if ((!method.equalsIgnoreCase("get"))
//				&& (!method.equalsIgnoreCase("post"))) {
//		    	System.err.println("method != get && method != post method = " + method);
//			return null;
//		}
		PackageRequest ret = new PackageRequest();
		String protocol = request.getRequestLine().getProtocolVersion()
				.toString();
		String uri = request.getRequestLine().getUri();
		ret.setMethod(method);
		ret.setProtocol(protocol);
		ret.setUri(uri);
		ret.setHeaders(request.getAllHeaders());
		if (request instanceof HttpEntityEnclosingRequest) {
			HttpEntity entity = ((HttpEntityEnclosingRequest) request)
					.getEntity();
			byte[] entityContent;
			try {
				entityContent = EntityUtils.toByteArray(entity);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			int length = entityContent.length;
			if (length >= POST_DATA_LIMIT) {
				// TODO 怎样处理？
			}
			ret.setEnity(entityContent);
		}
		return ret;
	}
}

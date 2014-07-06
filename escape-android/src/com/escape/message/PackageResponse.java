package com.escape.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;

public class PackageResponse {

	public static PackageResponse getInstance(byte[] content) {
		if (content == null) {
			return null;
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(content);
		DataInputStream dis = new DataInputStream(bis);
		try {
			boolean isError = dis.readBoolean();
			if (isError) {
				String message = dis.readUTF();
				PackageResponse ret = new PackageResponse();
				ret.setError(true);
				ret.setMsg(message);
				return ret;
			}
			String protocol = dis.readUTF();
			int responsecode = dis.readInt();
			String message = dis.readUTF();
			String gap = dis.readUTF();
			if (!gap.equals("||")) {
				return null;
			}
			PackageResponse ret = new PackageResponse();
			ret.setProtocol(protocol);
			ret.setResponseCode(responsecode);
			ret.setMsg(message);
			ret.setError(false);
			while (true) {
				String name = dis.readUTF();
				if (name.equals("||")) {
					break;
				}
				String value = dis.readUTF();
				ret.addHeader(name, value);
			}
			boolean hasBody = dis.readBoolean();
			if (hasBody) {
				int length = dis.readInt();
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				byte[] buff = new byte[1024];
				int len = -1;
				while ((len = dis.read(buff)) != -1) {
					buffer.write(buff, 0, len);
				}
				byte[] body = buffer.toByteArray();
				if (length != body.length) {
					// TODO
				}
				ret.setEnity(body);
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				dis.close();
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean mIsError = false;
	private String mProtocol = null;
	private int mResponseCode;
	private String mMessage = null;

	private HeaderGroup headergroup;

	private byte[] mContent = null;

	public PackageResponse() {
		headergroup = new HeaderGroup();
	}

	public String getProtocol() {
		return mProtocol;
	}

	public void setProtocol(String protocol) {
		this.mProtocol = protocol.trim();
	}

	public int getResponseCode() {
		return mResponseCode;
	}

	public void setResponseCode(int responseCode) {
		this.mResponseCode = responseCode;
	}

	public String getMsg() {
		return mMessage;
	}

	public void setMsg(String msg) {
		if (msg == null) {
			return;
		}
		this.mMessage = msg.trim();
	}

	public byte[] getEnity() {
		return mContent;
	}

	public void setEnity(byte[] enity) {
		this.mContent = enity;
	}

	public boolean isError() {
		return mIsError;
	}

	public void setError(boolean isError) {
		this.mIsError = isError;
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

	public int[] parseContentRange() {
		try {
			if (!containsHeader(HttpHeaders.CONTENT_RANGE)) {
				return null;
			}
			String value = getFirstHeader(HttpHeaders.CONTENT_RANGE).getValue();
			Pattern pattern = Pattern.compile("bytes\\s+(\\d+)-(\\d+)/(\\d+)");
			Matcher matcher = pattern.matcher(value);
			matcher.find();
			int[] m = new int[3];
			for (int i = 1; i <= 3; i++) {
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

	public String toString() {
		String result = "";
		result += ("Is error = " + mIsError + "    Response code = "
				+ mResponseCode + "    Protocol = " + mProtocol
				+ "    Message = " + mMessage + "\n");
		HeaderIterator iterator = headergroup.iterator();
		while (iterator.hasNext()) {
			Header header = iterator.nextHeader();
			result += (header.getName() + " : " + header.getValue() + "\n");
		}
		if (mContent != null) {
			result += ("RealResponse body length = " + mContent.length + "\n");
		}
		result += ("=========================================================="
				+ "\n");
		return result;
	}

	public void toHttpResponse(HttpResponse response) {
		response.setStatusCode(mResponseCode);
		response.setReasonPhrase(mMessage);
		Header[] headers = getAllHeaders();
		for (Header header : headers) {
			response.addHeader(header.getName(), header.getValue());
		}
		if (mContent != null) {
			HttpEntity entity = new ByteArrayEntity(mContent);
			response.setEntity(entity);
		}
	}

}

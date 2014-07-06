package com.escape.local.http.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CompressTest {

	private static final String example = "http://o-o.preferred.yyz06s01.v16.lscache4.c.youtube.com/videoplayback?sparams=id%2Cexpire%2Cip%2Cipbits%2Citag%2Csource%2Calgorithm%2Cburst%2Cfactor%2Ccp&fexp=912401%2C912300&algorithm=throttle-factor&itag=34&ip=74.0.0.0&burst=40&sver=3&signature=39F936ADE13188D73040FD801F1CD69C34F2723E.6359DF326E319C96072574F777B5934A25EDA133&source=youtube&expire=1320091200&key=yt1&ipbits=8&factor=1.25&cp=U0hRRVBUT19FSkNOMV9ITlNDOkxqUWFfc2k3bDZr&id=06f66c260f3e8da8";

	public static void main(String[] args) {

	}
}

class RealRequest_ implements Serializable {
	private static final long serialVersionUID = 1L;

	public String mProtocol;
	public String mMethod;
	public String mUri;

	public List<String> mHeaders = new ArrayList<String>();
	public byte[] mBody;
}
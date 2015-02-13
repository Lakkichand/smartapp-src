package com.zhidian.wifibox.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Intent;
import android.util.Log;

import com.ta.TAApplication;
import com.zhidian.wifibox.activity.MainActivity;

/**
 * 监听网络端口
 * 
 * @author xiedezhi
 * 
 */
public class ServerUtil {

	/**
	 * http://127.0.0.1:36742/exists 判断是否存在装机大师 返回var
	 * json={\"status\":\"success\",\"msg\":\"EXISTS!\"};\r\n
	 * 
	 * http://127.0.0.1:36742/open 打开装机大师 返回var
	 * json={\"status\":\"success\",\"msg\":\"OK!\"};\r\n
	 */
	public static void listenOpenApp() {
		new Thread("ServerThread") {
			public void run() {
				ServerSocket serverSocket = null;
				try {
					serverSocket = new ServerSocket();
					serverSocket
							.bind(new InetSocketAddress("127.0.0.1", 36742));
					while (true) {
						Socket socket = null;
						try {
							socket = serverSocket.accept();
							socket.setTcpNoDelay(true);
							InputStream inStream = socket.getInputStream();
							ByteArrayOutputStream outStream = new ByteArrayOutputStream();
							byte[] buffer = new byte[2048];
							int len = inStream.read(buffer);
							outStream.write(buffer, 0, len);
							outStream.close();
							String request = new String(outStream.toByteArray())
									.toLowerCase();
							Log.e("", "===========\n" + request
									+ "\n===========");
							if (request.contains("get /exists")) {
								// 判断存在装机大师
								StringBuffer sb = new StringBuffer();
								String body = "var json={\"status\":\"success\",\"msg\":\"EXISTS!\"};\r\n";
								sb.append(body);
								socket.getOutputStream().write(
										sb.toString().getBytes());
								socket.getOutputStream().flush();
								socket.getOutputStream().close();
							} else if (request.contains("get /open")) {
								// 打开装机大师
								StringBuffer sb = new StringBuffer();
								String body = "var json={\"status\":\"success\",\"msg\":\"OK!\"};\r\n";
								sb.append(body);
								socket.getOutputStream().write(
										sb.toString().getBytes());
								socket.getOutputStream().flush();
								socket.getOutputStream().close();
								Intent intent = new Intent(
										TAApplication.getApplication(),
										MainActivity.class);
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								TAApplication.getApplication().startActivity(
										intent);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (socket != null) {
								try {
									socket.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (serverSocket != null) {
						try {
							serverSocket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			};
		}.start();
	}
}

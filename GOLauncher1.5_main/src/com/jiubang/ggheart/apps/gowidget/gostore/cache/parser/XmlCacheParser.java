package com.jiubang.ggheart.apps.gowidget.gostore.cache.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Log;

import com.gau.utils.net.request.THttpRequest;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreAppInforUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;

public class XmlCacheParser implements ICacheParser {

	// XML元素节点名称
	// 根节点
	private final String ELEMENT_ROOT = "Cache";
	private final String ELEMENT_REQUEST = "Request";

	// 属性名称
	private final String ATTRIBUTE_GOLAUNCHER_VERSIONCODE = "golauncher_versioncode";
	private final String ATTRIBUTE_TIMESTAMP = "timestamp";
	private final String ATTRIBUTE_URL = "url";
	private final String ATTRIBUTE_POST_DATA = "post_data";
	private final String ATTRIBUTE_DATA_FILE_PATH = "bean_data_file_path";

	// 默认编码
	private final String DEFAULT_ENCODING = "UTF-8";

	//同步锁
	private final byte[] mLock = new byte[0];

	/**
	 * 保存缓存数据的方法
	 * 
	 * @param context
	 * @param request
	 * @param listBeans
	 */
	public void saveCacheDataToXml(Context context, String cacheFilePath, THttpRequest request,
			Serializable serializable) {
		synchronized (mLock) {
			if (context != null && GoStorePhoneStateUtil.isSDCardAccess() && cacheFilePath != null
					&& !"".equals(cacheFilePath.trim()) && request != null && serializable != null) {
				// 存放缓存数据的文件夹
				File parentFile = new File(GoStorePublicDefine.GOSTORE_VIEW_CACHE_FILE_PATH);
				if (!parentFile.exists()) {
					// 如果父文件夹不存在，则创建
					parentFile.mkdirs();
				}
				File file = new File(cacheFilePath);
				try {
					DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
					Document document = null;
					if (!file.exists()) {
						// 如果保存缓存数据的文件不存在
						document = documentBuilder.newDocument();
						// 添加根节点
						Element root = document.createElement(ELEMENT_ROOT);
						document.appendChild(root);
						// 添加Request节点
						Element element = document.createElement(ELEMENT_REQUEST);
						// 桌面版本号
						element.setAttribute(ATTRIBUTE_GOLAUNCHER_VERSIONCODE,
								String.valueOf(GoStoreAppInforUtil.getThisAppVersionCode(context)));
						// 时间戳
						element.setAttribute(ATTRIBUTE_TIMESTAMP,
								String.valueOf(System.currentTimeMillis()));
						// url
						element.setAttribute(ATTRIBUTE_URL, request.getUrl().toString());
						// post data
						element.setAttribute(ATTRIBUTE_POST_DATA, new String(request.getPostData()));
						// 该请求对应BEAN数据的保存文件路径
						String dataFilePath = GoStorePublicDefine.GOSTORE_VIEW_CACHE_FILE_PATH
								+ "/" + System.currentTimeMillis() + ".data";
						element.setAttribute(ATTRIBUTE_DATA_FILE_PATH, dataFilePath);
						root.appendChild(element);
						// 保存BEAN信息
						saveSerializableDataToFile(dataFilePath, serializable);
					} else {
						// 如果保存缓存数据的文件存在
						try {
							document = documentBuilder.parse(file);
						} catch (Exception e) {
							// 如果文件存在却解释出错，就删除
							file.delete();
						}
						if (document != null) {
							// Request节点
							NodeList nodeList = document.getElementsByTagName(ELEMENT_REQUEST);
							if (nodeList != null && nodeList.getLength() > 0) {
								int length = nodeList.getLength();
								Element requestElement = null;
								String url = null;
								String postData = null;
								int i = 0;
								for (; i < length; i++) {
									requestElement = (Element) nodeList.item(i);
									if (requestElement != null) {
										url = requestElement.getAttribute(ATTRIBUTE_URL);
										postData = requestElement.getAttribute(ATTRIBUTE_POST_DATA);
										if (request.getUrl().toString().equals(url)
												&& new String(request.getPostData())
														.equals(postData)) {
											// 如果请求相同
											break;
										}
									}
								}
								if (i < length) {
									// 有相同请求记录
									if (requestElement != null) {
										// 把原来的BEAN数据文件删除
										String dataFilePath = requestElement
												.getAttribute(ATTRIBUTE_DATA_FILE_PATH);
										File dataFile = new File(dataFilePath);
										if (dataFile.exists()) {
											dataFile.delete();
										}
										// 把原来的移除
										document.getFirstChild().removeChild(requestElement);
									}
								}
								// 添加Request节点
								Element newRequestElement = document.createElement(ELEMENT_REQUEST);
								// 桌面版本号
								newRequestElement.setAttribute(ATTRIBUTE_GOLAUNCHER_VERSIONCODE,
										String.valueOf(GoStoreAppInforUtil
												.getThisAppVersionCode(context)));
								newRequestElement.setAttribute(ATTRIBUTE_TIMESTAMP,
										String.valueOf(System.currentTimeMillis()));
								newRequestElement.setAttribute(ATTRIBUTE_URL, request.getUrl()
										.toString());
								newRequestElement.setAttribute(ATTRIBUTE_POST_DATA, new String(
										request.getPostData()));
								String dataFilePath = GoStorePublicDefine.GOSTORE_VIEW_CACHE_FILE_PATH
										+ "/" + System.currentTimeMillis() + ".data";
								newRequestElement.setAttribute(ATTRIBUTE_DATA_FILE_PATH,
										dataFilePath);
								document.getFirstChild().appendChild(newRequestElement);
								// 保存BEAN信息
								saveSerializableDataToFile(dataFilePath, serializable);
							}
						}
					}
					writeXmlFile(document, cacheFilePath, DEFAULT_ENCODING);
				} catch (Throwable e) {
					// TODO: handle exception
					Log.i("scale", "e" + e);
				}
			}
		}
	}

	/**
	 * 从XML文件中获取缓存数据的方法
	 * 
	 * @param context
	 * @param request
	 * @return
	 */
	public Object getCacheDataFromXml(Context context, String cacheFilePath, THttpRequest request) {
		Object object = null;
		synchronized (mLock) {
			if (context != null && GoStorePhoneStateUtil.isSDCardAccess() && cacheFilePath != null
					&& !"".equals(cacheFilePath.trim()) && request != null) {
				File file = new File(cacheFilePath);
				if (file.exists()) {
					try {
						DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
								.newInstance();
						DocumentBuilder documentBuilder = documentBuilderFactory
								.newDocumentBuilder();
						Document document = documentBuilder.parse(file);
						// Request节点
						NodeList nodeList = document.getElementsByTagName(ELEMENT_REQUEST);
						if (nodeList != null && nodeList.getLength() > 0) {
							int length = nodeList.getLength();
							Element requestElement = null;
							String url = null;
							String postData = null;
							int i = 0;
							for (; i < length; i++) {
								requestElement = (Element) nodeList.item(i);
								if (requestElement != null) {
									url = requestElement.getAttribute(ATTRIBUTE_URL);
									postData = requestElement.getAttribute(ATTRIBUTE_POST_DATA);
									if (request.getUrl().toString().equals(url)
											&& new String(request.getPostData()).equals(postData)) {
										// 如果请求相同
										break;
									}
								}
							}
							if (i < length) {
								// 有相同请求记录
								if (requestElement != null) {
									// 取出该请求记录中的桌面版本号
									int launcherVersionCode = -1;
									String verCode = requestElement
											.getAttribute(ATTRIBUTE_GOLAUNCHER_VERSIONCODE);
									if (verCode != null && !"".equals(verCode.trim())) {
										launcherVersionCode = Integer.parseInt(verCode);
									}
									if (launcherVersionCode == GoStoreAppInforUtil
											.getThisAppVersionCode(context)) {
										// 如果版本号跟现在相同，则取出缓存记录
										String dataFilePath = requestElement
												.getAttribute(ATTRIBUTE_DATA_FILE_PATH);
										object = getObjectDataToFile(dataFilePath);
									} else {
										// 如果记录不相同
										// 把原来的BEAN数据文件删除
										String dataFilePath = requestElement
												.getAttribute(ATTRIBUTE_DATA_FILE_PATH);
										File dataFile = new File(dataFilePath);
										if (dataFile.exists()) {
											dataFile.delete();
										}
										// 把原来的移除
										document.getFirstChild().removeChild(requestElement);
									}
								}
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
						Log.i("scale", "e:" + e);
					}
				}
			}
		}
		return object;
	}

	/**
	 * 写入XML文件的方法
	 * 
	 * @param doc
	 * @param w
	 * @param encoding
	 */
	public void writeXmlFile(Document doc, String filePath, String encoding) {
		if (doc != null && filePath != null && !"".equals(filePath.trim())) {
			if (encoding == null || "".equals(encoding.trim())) {
				encoding = DEFAULT_ENCODING;
			}
			FileOutputStream fileOutputStream = null;
			OutputStreamWriter outputStreamWriter = null;
			try {
				fileOutputStream = new FileOutputStream(filePath, false);
				outputStreamWriter = new OutputStreamWriter(fileOutputStream, encoding);
				Source source = new DOMSource(doc);
				Result result = new StreamResult(outputStreamWriter);
				Transformer xformer = TransformerFactory.newInstance().newTransformer();
				xformer.setOutputProperty(OutputKeys.ENCODING, encoding);
				xformer.transform(source, result);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (outputStreamWriter != null) {
						outputStreamWriter.close();
					}
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}

	/**
	 * 保存可序列化数据到文件的方法
	 * 
	 * @param filePath
	 * @param serializable
	 */
	public void saveSerializableDataToFile(String filePath, Serializable serializable) {
		if (filePath != null && !"".equals(filePath.trim()) && serializable != null) {
			FileOutputStream fileOutputStream = null;
			ObjectOutputStream objectOutputStream = null;
			try {
				File file = FileUtil.createNewFile(filePath, false);
				fileOutputStream = new FileOutputStream(file, false);
				objectOutputStream = new ObjectOutputStream(fileOutputStream);
				objectOutputStream.writeObject(serializable);
				objectOutputStream.close();
				fileOutputStream.close();
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				try {
					if (objectOutputStream != null) {
						objectOutputStream.close();
					}
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}

	/**
	 * 从文件中取可序列化数据的方法
	 * 
	 * @param filePath
	 * @param serializable
	 */
	public Object getObjectDataToFile(String filePath) {
		Object data = null;
		synchronized (mLock) {
			if (filePath != null && !"".equals(filePath.trim())) {
				File dataFile = new File(filePath);
				if (dataFile.exists()) {
					FileInputStream fileInputStream = null;
					ObjectInputStream objectInputStream = null;
					try {
						fileInputStream = new FileInputStream(dataFile);
						objectInputStream = new ObjectInputStream(fileInputStream);
						data = objectInputStream.readObject();
						objectInputStream.close();
						fileInputStream.close();
					} catch (Exception e) {
						// TODO: handle exception
					} finally {
						try {
							if (objectInputStream != null) {
								objectInputStream.close();
							}
							if (fileInputStream != null) {
								fileInputStream.close();
							}
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				}
			}
		}
		return data;
	}

	@Override
	public synchronized void saveCacheData(Context context, String cacheFilePath,
			THttpRequest request, Object object) {
		// TODO Auto-generated method stub
		if (object != null && object instanceof Serializable) {
			Serializable serializable = (Serializable) object;
			saveCacheDataToXml(context, cacheFilePath, request, serializable);
		}
	}

	@Override
	public synchronized Object getCacheData(Context context, String cacheFilePath,
			THttpRequest request) {
		// TODO Auto-generated method stub
		return getCacheDataFromXml(context, cacheFilePath, request);
	}

	@Override
	public synchronized void cleanAllCacheData(Context context) {
		synchronized (mLock) {
			if (GoStorePhoneStateUtil.isSDCardAccess()) {
				File parentfile = new File(GoStorePublicDefine.GOSTORE_VIEW_CACHE_FILE_PATH);
				if (parentfile != null && parentfile.exists() && parentfile.isDirectory()) {
					File[] files = parentfile.listFiles();
					if (files != null && files.length > 0) {
						for (File file : files) {
							if (file != null && file.exists() && file.isFile()) {
								file.delete();
							}
						}
					}
				}
			}
		}
	}
}

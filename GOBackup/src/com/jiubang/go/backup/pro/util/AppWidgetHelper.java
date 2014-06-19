package com.jiubang.go.backup.pro.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Xml;

/**
 * @author GoBackup Dev Team
 */
public class AppWidgetHelper {
	private static final String TAG = "AppWidgetHelper";

	/**
	 * @author GoBackup Dev Team
	 */
	static public class AppWidgetConfig {
		ArrayList<WidgetHost> mAppWidgetHosts;
		ArrayList<AppWidget> mAppWidgets;
		ArrayList<WidgetProvider> mAppWidgetProviders;

		public AppWidgetConfig() {
			mAppWidgetHosts = new ArrayList<AppWidgetHelper.WidgetHost>();
			mAppWidgetProviders = new ArrayList<AppWidgetHelper.WidgetProvider>();
			mAppWidgets = new ArrayList<AppWidgetHelper.AppWidget>();
		}
	}

	/**
	 * @author GoBackup Dev Team
	 */
	static class WidgetHost {
		String mPkg;
		int mId;

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof WidgetHost)) {
				return false;
			}

			WidgetHost host = (WidgetHost) o;
			if (!isValid() || !host.isValid()) {
				return false;
			}

			if (mPkg.equals(host.mPkg) && host.mId == mId) {
				return true;
			}
			return false;
		}

		public boolean isValid() {
			return (mPkg == null || mId == -1) ? false : true;
		}
	}

	/**
	 * @author GoBackup Dev Team
	 */
	static class AppWidget {
		int mId;
		WidgetHost mHost;
		WidgetProvider mProvider;

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof AppWidget)) {
				return false;
			}

			AppWidget widget = (AppWidget) o;

			if (mId == widget.mId) {
				return true;
			}
			return false;
		}

		public boolean isValid() {
			return (mId == -1 || mHost == null || !mHost.isValid()) ? false : true;
		}
	}

	/**
	 * @author GoBackup Dev Team
	 */
	static class WidgetProvider {
		String mPkg;
		String mCl;

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof WidgetProvider)) {
				return false;
			}

			WidgetProvider provider = (WidgetProvider) o;
			if (!isValid() || !provider.isValid()) {
				return false;
			}

			if (mPkg.equals(provider.mPkg) && mCl.equals(provider.mCl)) {
				return true;
			}
			return false;
		}

		public boolean isValid() {
			return (mPkg == null || mCl == null) ? false : true;
		}
	}

	public static AppWidgetConfig readAppWidgetStateFromFile(Context context, FileInputStream stream) {
		if (context == null || stream == null) {
			return null;
		}
		final int m16 = 16;
		boolean success = false;
		AppWidgetConfig config = new AppWidgetConfig();
		ArrayList<WidgetProvider> providers = config.mAppWidgetProviders;
		ArrayList<WidgetHost> hosts = config.mAppWidgetHosts;
		ArrayList<AppWidget> widgets = config.mAppWidgets;

		AppWidgetManager am = AppWidgetManager.getInstance(context);
		List<AppWidgetProviderInfo> installWidgetProvider = am.getInstalledProviders();

		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(stream, null);

			int type;
			do {
				type = parser.next();
				if (type == XmlPullParser.START_TAG) {
					String tag = parser.getName();
					if ("p".equals(tag)) {
						String pkg = parser.getAttributeValue(null, "pkg");
						String cl = parser.getAttributeValue(null, "cl");

						final PackageManager packageManager = context.getPackageManager();
						try {
							packageManager.getReceiverInfo(new ComponentName(pkg, cl), 0);
						} catch (PackageManager.NameNotFoundException e) {
							String[] pkgs = packageManager
									.currentToCanonicalPackageNames(new String[] { pkg });
							pkg = pkgs[0];
						}

						WidgetProvider provider = new WidgetProvider();
						provider.mPkg = pkg;
						provider.mCl = cl;
						if (isProviderValid(installWidgetProvider, provider)) {
							providers.add(provider);
						}
					} else if ("h".equals(tag)) {
						WidgetHost host = new WidgetHost();
						host.mPkg = parser.getAttributeValue(null, "pkg");
						host.mId = Integer.parseInt(parser.getAttributeValue(null, "id"), m16);
						hosts.add(host);
					} else if ("g".equals(tag)) {
						AppWidget widget = new AppWidget();
						widget.mId = Integer.parseInt(parser.getAttributeValue(null, "id"), m16);

						String providerString = parser.getAttributeValue(null, "p");
						if (providerString != null) {
							int pIndex = Integer.parseInt(providerString, m16);
							if (pIndex < 0 || pIndex >= config.mAppWidgetProviders.size()) {
								continue;
							}
							widget.mProvider = config.mAppWidgetProviders.get(pIndex);
						}

						int h = Integer.parseInt(parser.getAttributeValue(null, "h"), m16);
						if (h < 0 || h >= config.mAppWidgetHosts.size()) {
							continue;
						}

						widget.mHost = config.mAppWidgetHosts.get(h);
						widgets.add(widget);
					}
				}
			} while (type != XmlPullParser.END_DOCUMENT);
			success = true;
		} catch (NullPointerException e) {
			Log.w(TAG, "failed parsing " + e);
		} catch (NumberFormatException e) {
			Log.w(TAG, "failed parsing " + e);
		} catch (XmlPullParserException e) {
			Log.w(TAG, "failed parsing " + e);
		} catch (IOException e) {
			Log.w(TAG, "failed parsing " + e);
		} catch (IndexOutOfBoundsException e) {
			Log.w(TAG, "failed parsing " + e);
		}
		if (!success) {
			config = null;
		}
		return config;
	}

	private static boolean isProviderValid(List<AppWidgetProviderInfo> installWidgetProvider,
			WidgetProvider provider) {
		for (AppWidgetProviderInfo item : installWidgetProvider) {
			ComponentName cn = item.provider;
			if (cn != null) {
				String packageName = cn.getPackageName();
				String cl = cn.getClassName();
				if (packageName.equals(provider.mPkg) && cl.equals(provider.mCl)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean writeAppWidgetStateToFile(FileOutputStream stream, AppWidgetConfig config) {
		if (stream == null || config == null) {
			return false;
		}

		ArrayList<WidgetProvider> providers = config.mAppWidgetProviders;
		ArrayList<WidgetHost> hosts = config.mAppWidgetHosts;
		ArrayList<AppWidget> widgets = config.mAppWidgets;

		int mN;

		try {
			XmlSerializer out = new FastXmlSerializer();
			out.setOutput(stream, "utf-8");
			out.startDocument(null, true);
			out.startTag(null, "gs");

			mN = providers.size();
			for (int i = 0; i < mN; i++) {
				WidgetProvider p = providers.get(i);
				out.startTag(null, "p");
				out.attribute(null, "pkg", p.mPkg);
				out.attribute(null, "cl", p.mCl);
				out.endTag(null, "p");
			}

			mN = hosts.size();
			for (int i = 0; i < mN; i++) {
				WidgetHost host = hosts.get(i);
				out.startTag(null, "h");
				out.attribute(null, "pkg", host.mPkg);
				out.attribute(null, "id", Integer.toHexString(host.mId));
				out.endTag(null, "h");
			}

			mN = widgets.size();
			for (int i = 0; i < mN; i++) {
				AppWidget widget = widgets.get(i);
				out.startTag(null, "g");
				out.attribute(null, "id", Integer.toHexString(widget.mId));

				WidgetHost host = widget.mHost;
				if (host != null && hosts.contains(host)) {
					out.attribute(null, "h", Integer.toHexString(hosts.indexOf(host)));
				}

				WidgetProvider provider = widget.mProvider;
				if (provider != null && providers.contains(provider)) {
					out.attribute(null, "p", Integer.toHexString(providers.indexOf(provider)));
				}
				out.endTag(null, "g");
			}
			out.endTag(null, "gs");

			out.endDocument();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 将第一第二个config合并到第一个config
	 * 
	 * @param configOne
	 * @param configTwo
	 * @return 如果数据改变，返回true，否则，返回false
	 */
	public static boolean mergeAppWidgetConfig(Context context, AppWidgetConfig configOne,
			AppWidgetConfig configTwo) {
		if (configTwo == null) {
			return false;
		}

		boolean dataChanged = false;

		int sizeOne = 0;
		int sizeTwo = 0;
		ArrayList<WidgetHost> hostOne = configOne.mAppWidgetHosts;
		ArrayList<WidgetHost> hostTwo = configTwo.mAppWidgetHosts;
		sizeTwo = hostTwo.size();
		sizeOne = hostOne.size();
		for (int i = 0; i < sizeTwo; i++) {
			boolean contain = false;
			WidgetHost host = hostTwo.get(i);
			for (int j = 0; j < sizeOne; j++) {
				if (host.equals(hostOne.get(j))) {
					contain = true;
					break;
				}
			}
			if (!contain) {
				hostOne.add(host);
				dataChanged = true;
			}
		}

		ArrayList<WidgetProvider> providerOne = configOne.mAppWidgetProviders;
		ArrayList<WidgetProvider> providerTwo = configTwo.mAppWidgetProviders;
		sizeTwo = providerTwo.size();
		sizeOne = providerOne.size();
		for (int i = 0; i < sizeTwo; i++) {
			boolean contain = false;
			WidgetProvider provider = providerTwo.get(i);
			for (int j = 0; j < sizeOne; j++) {
				if (provider.equals(providerOne.get(j))) {
					contain = true;
					break;
				}
			}
			if (!contain) {
				providerOne.add(provider);
				dataChanged = true;
			}
		}

		ArrayList<AppWidget> appWidgetOne = configOne.mAppWidgets;
		ArrayList<AppWidget> appWidgetTwo = configTwo.mAppWidgets;
		sizeTwo = appWidgetTwo.size();
		sizeOne = appWidgetOne.size();
		for (int i = 0; i < sizeTwo; i++) {
			boolean contain = false;
			AppWidget widget = appWidgetTwo.get(i);
			for (int j = 0; j < sizeOne; j++) {
				AppWidget widgetOne = appWidgetOne.get(j);
				if (widget.equals(widgetOne)) {
					// 完全相等
					contain = true;
					break;
				} else {
					// id相等，其他不想等，替换
					if (widget.mId == widgetOne.mId) {
						// 在one中删除该项
						appWidgetOne.remove(widgetOne);
						contain = false;
						break;
					}
				}
			}
			if (!contain) {
				appWidgetOne.add(widget);
				dataChanged = true;
			}
		}

		/*
		 * List<Integer> widgetids = getAllWidgetIds(context); for(AppWidget
		 * widget : appWidgetOne){ if(!widgetids.contains(widget.id)){
		 * dataChanged = true; break; } }
		 */

		return dataChanged;
	}

	public static boolean releaseAppWidgetConfig(AppWidgetConfig config) {
		if (config == null) {
			return false;
		}

		if (config.mAppWidgetHosts != null) {
			config.mAppWidgetHosts.clear();
		}
		if (config.mAppWidgets != null) {
			config.mAppWidgets.clear();
		}
		if (config.mAppWidgetProviders != null) {
			config.mAppWidgetProviders.clear();
		}
		return true;
	}
}

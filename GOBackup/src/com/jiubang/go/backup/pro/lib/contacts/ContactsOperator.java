package com.jiubang.go.backup.pro.lib.contacts;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.DisplayPhoto;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.lib.contacts.ContactStruct.ContactMethod;
import com.jiubang.go.backup.pro.lib.contacts.ContactStruct.OrganizationData;
import com.jiubang.go.backup.pro.lib.contacts.ContactStruct.PhoneData;
import com.jiubang.go.backup.pro.lib.contacts.vcard.VCardComposer;
import com.jiubang.go.backup.pro.lib.contacts.vcard.VCardException;
import com.jiubang.go.backup.pro.lib.contacts.vcard.VCardParser;
import com.jiubang.go.backup.pro.lib.contacts.vcard.VDataBuilder;
import com.jiubang.go.backup.pro.lib.contacts.vcard.VNode;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.LogUtil;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author zhankai maiyongshen
 */
// CHECKSTYLE:OFF
public class ContactsOperator {
	private static final int COLUMN_CONTACT_ID = 0;
	private static final int COLUMN_MIMETYPE = 1;
	private static final int COLUMN_IS_PRIMARY = 2;
	private static final int COLUMN_DATA1 = 3;
	private static final int COLUMN_DATA2 = 4;
	private static final int COLUMN_DATA3 = 5;
	private static final int COLUMN_DATA4 = 6;
	private static final int COLUMN_DATA5 = 7;
	private static final int COLUMN_DATA6 = 8;
	private static final int COLUMN_DATA14 = 9;
	// private static final int COLUMN_DATA15 = 10;

	private static final int MAX_FILE_SIZE = 512 * 1024; // 512KB

	private static final String MARK_BEGIN = "BEGIN:VCARD";
	private static final String MARK_END = "END:VCARD";

	private final static String[] PROJECTION = new String[] { Data.CONTACT_ID, Data.MIMETYPE,
			Data.IS_PRIMARY, Data.DATA1, // Phone,Email,ADDRESS,COMPANY,IM,Note
			Data.DATA2, // TYPE
			Data.DATA3, // LABEL
			Data.DATA4, // TITLE
			Data.DATA5, Data.DATA6, Data.DATA14, // photo file id
	/* Data.DATA15 */};
	// 系统中存在的联系人
	private Set<ContactStruct> mSystemContacts;
	private Map<ContactStruct, Long> mSystemContactMap;
	// 本轮恢复已经插入的联系人，用于去除备份记录中本来就存在的重复项
	private Set<ContactStruct> mInsertedContacts;

	private static ContactsOperator mInstance;
	private Map<Long, Long> mRawContactIdMap;

	private boolean mStopBackupFlag = false;
	private boolean mStopRestoreFalg = false;
	private static boolean mcheckGroup = false;

	private int mRestoreCount;
	private int mDiscardCount;

	private ExecutorService mThreadPool = null;

	/**
	 * @author maiyongshen
	 * 
	 */
	public static class BackupArg {
		public boolean ignoreContactsWithoutNumber;
		public boolean backupContactsAvatar;
		public String parentDir;
		public String backupFileName;
		public String displayPhotoDir;
	}

	/**
	 * @author maiyongshen
	 * 
	 */
	public static class RestoreArg {
		public File backupFile;
		public File displayPhotoDir;
		public boolean discardDuplicateContacts;
	}

	public static synchronized ContactsOperator getInstance() {
		if (mInstance == null) {
			mInstance = new ContactsOperator();
		}
		return mInstance;
	}

	private ContactsOperator() {
		mRawContactIdMap = new HashMap<Long, Long>();
		mThreadPool = Executors.newFixedThreadPool(1);
	}

	// 获取已被收藏的人的contact_id
	public static List<Integer> getAllStarred(Context context) {
		List<Integer> starredList = new ArrayList<Integer>();
		ContentResolver resolver = context.getContentResolver();
		String[] projection = new String[] { ContactsContract.RawContacts.CONTACT_ID };
		String selection = ContactsContract.Contacts.STARRED + "=1";
		Cursor cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI, projection,
				selection, null, null);
		try {
			if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
				return null;
			}
			do {
				int contactId = cursor.getInt(0);
				starredList.add(contactId);
			} while (cursor.moveToNext());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return starredList;
	}

	// 获取系统所有联系人条目
	public static List<ContactStruct> getAllSystemContacts(Context context) {
		if (context == null) {
			return null;
		}
		List<Integer> starredContactIdList = getAllStarred(context);
		Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, PROJECTION, null,
				null, Data.CONTACT_ID);
		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		Map<Integer, ContactStruct> contactsMap = new HashMap<Integer, ContactStruct>();
		try {
			do {
				int contactID = cursor.getInt(COLUMN_CONTACT_ID);
				ContactStruct struct = contactsMap.get(contactID);
				if (struct == null) {
					struct = new ContactStruct();
					struct.contactId = contactID;
					contactsMap.put(contactID, struct);
				}
				if (starredContactIdList != null && starredContactIdList.contains(contactID)) {
					// 如果ContactId是收藏的，则在联系人中加入已经被收藏标志
					struct.starred = true;
					// 删除已经标志了的ContactID
					starredContactIdList.remove(starredContactIdList.indexOf(contactID));
				}
				String mimeType = cursor.getString(COLUMN_MIMETYPE);
				int isPrimary = cursor.getInt(COLUMN_IS_PRIMARY);
				String data1 = cursor.getString(COLUMN_DATA1);

				int data2 = cursor.getInt(COLUMN_DATA2);
				String data3 = cursor.getString(COLUMN_DATA3);
				String data4 = cursor.getString(COLUMN_DATA4);
				String data5 = cursor.getString(COLUMN_DATA5);
				String data6 = cursor.getString(COLUMN_DATA6);

				if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) { // 姓名
					// 空数据记录视为无效
					if (isEmptyString(data1)) {
						continue;
					}
					struct.mName = data1;
					 struct.mGivenName = cursor.getString(COLUMN_DATA2);
					 struct.mFamilyName = data3;
					 struct.mMiddleName = data5;
				} else if (mimeType.equals(Photo.CONTENT_ITEM_TYPE)) { // 头像
					// TODO 屏蔽头像的备份
					// struct.photoBytes = data15;
					// struct.photoType = "PNG";
					if (Util.getAndroidSystemVersion() >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
						struct.photoFileId = cursor.getLong(COLUMN_DATA14);
					}
				} else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) { // 电话
					ContactMethod phone = new ContactMethod();
					phone.kind = ContactDataKind.PHONE;
					phone.isPrimary = isPrimary;
					phone.data = data1;
					phone.type = data2;
					phone.label = data3;
					struct.addPhone(phone);
				} else if (mimeType.equals(Email.CONTENT_ITEM_TYPE)) { // 邮件
					ContactMethod email = new ContactMethod();
					email.kind = ContactDataKind.EMAIL;
					email.isPrimary = isPrimary;
					email.data = data1;
					email.type = data2;
					email.label = data3;
					struct.addContactmethod(email);
				} else if (mimeType.equals(StructuredPostal.CONTENT_ITEM_TYPE)) { // 地址
					ContactMethod postal = new ContactMethod();
					postal.kind = ContactDataKind.POSTAL;
					postal.isPrimary = isPrimary;
					postal.data = data1;
					postal.type = data2;
					postal.label = data3;
					struct.addContactmethod(postal);
				} else if (mimeType.equals(Event.CONTENT_ITEM_TYPE)) { // 事件
					ContactMethod event = new ContactMethod();
					event.kind = ContactDataKind.EVENT;
					event.isPrimary = isPrimary;
					event.data = data1;
					event.type = data2;
					event.label = data3;
					struct.addContactmethod(event);
				} else if (mimeType.equals(Im.CONTENT_ITEM_TYPE)) { // IM
					ContactMethod im = new ContactMethod();
					im.kind = ContactDataKind.IM;
					im.isPrimary = isPrimary;
					im.data = data1;
					im.type = data2;
					im.label = data3;
					im.protocol = data5;
					im.custom_Protocol = data6;
					struct.addContactmethod(im);
				} else if (mimeType.equals(Nickname.CONTENT_ITEM_TYPE)) { // 昵称
					ContactMethod nickName = new ContactMethod();
					nickName.kind = ContactDataKind.NICKNAME;
					nickName.isPrimary = isPrimary;
					nickName.data = data1;
					nickName.type = data2;
					nickName.label = data3;
					struct.addContactmethod(nickName);
				} else if (mimeType.equals(Note.CONTENT_ITEM_TYPE)) { // 备注
					struct.notes.add(data1);
				} else if (mimeType.equals(Organization.CONTENT_ITEM_TYPE)) { // 组织
					OrganizationData organization = new OrganizationData();
					organization.isPrimary = isPrimary;
					organization.companyName = data1;
					organization.type = data2;
					organization.label = data3;
					organization.positionName = data4;
					struct.addOrganization(organization);
				} else if (mimeType.equals(Relation.CONTENT_ITEM_TYPE)) { // 关系
					ContactMethod relation = new ContactMethod();
					relation.kind = ContactDataKind.RELATION;
					relation.isPrimary = isPrimary;
					relation.data = data1;
					relation.type = data2;
					relation.label = data3;
					struct.addContactmethod(relation);
				} else if (mimeType.equals(Website.CONTENT_ITEM_TYPE)) { // 网站
					ContactMethod website = new ContactMethod();
					website.kind = ContactDataKind.WEBSITE;
					website.isPrimary = isPrimary;
					website.data = data1;
					website.type = data2;
					website.label = data3;
					struct.addContactmethod(website);
				} else if (mimeType.equals(GroupMembership.CONTENT_ITEM_TYPE)) { // 分组信息
					if (isEmptyString(data1)) {
						continue;
					}
					String groupTitle = getGroupTitle(context, Long.parseLong(data1));
					if (groupTitle != null) {
						struct.addGroup(groupTitle);
					}
				}
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}

		List<ContactStruct> contactsList = new ArrayList<ContactStruct>(contactsMap.values());
		contactsMap.clear();
		if (starredContactIdList != null) {
			starredContactIdList.clear();
		}
		return contactsList;
	}

	public static long[] getAllContactsID(Context context) {
		if (context == null) {
			return null;
		}
		final ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
				new String[] { ContactsContract.Contacts._ID }, null, null, null);
		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() < 1 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		long[] contactsId = new long[cursor.getCount()];
		try {
			for (int i = 0; cursor.moveToNext(); i++) {
				contactsId[i] = cursor.getLong(0);
			}
		} finally {
			cursor.close();
		}
		return contactsId;
	}

	public static byte[] getThumbnailPhotoData(Context context, long contactId) {
		if (context == null) {
			return null;
		}

		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
				contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri,
				ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
		final ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(photoUri,
				new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO }, null, null, null);
		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() < 1 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		try {
			byte[] data = cursor.getBlob(0);
			return data;
		} finally {
			cursor.close();
		}
	}

	public static InputStream openThumbnailPhoto(Context context, long contactId) {
		byte[] data = getThumbnailPhotoData(context, contactId);
		if (data != null) {
			return new ByteArrayInputStream(data);
		}
		return null;
	}

	public static InputStream openDisplayPhoto(Context context, long photoFileId) {
		// 不允许4.0以下的系统调用
		if (Util.getAndroidSystemVersion() < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return null;
		}
		if (context == null || photoFileId < 0) {
			return null;
		}
		final ContentResolver cr = context.getContentResolver();
		Uri displayPhotoUri = ContentUris.withAppendedId(DisplayPhoto.CONTENT_URI, photoFileId);
		try {
			AssetFileDescriptor fd = cr.openAssetFileDescriptor(displayPhotoUri, "r");
			return fd.createInputStream();
		} catch (IOException e) {
			return null;
		}
	}

	public static void writeDisplayPhoto(Context context, long rawContactId, byte[] photo) {
		// 不允许4.0以下的系统调用
		if (Util.getAndroidSystemVersion() < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return;
		}
		if (context == null || photo == null) {
			return;
		}

		final ContentResolver cr = context.getContentResolver();
		Uri rawContactPhotoUri = Uri.withAppendedPath(
				ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
				RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
		try {
			AssetFileDescriptor fd = cr.openAssetFileDescriptor(rawContactPhotoUri, "rw");
			try {
				OutputStream os = fd.createOutputStream();
				try {
					os.write(photo);
				} finally {
					os.close();
				}
			} finally {
				fd.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeDisplayPhoto(Context context, long rawContactId,
			InputStream photoDataStream) {
		// 不允许4.0以下的系统调用
		if (Util.getAndroidSystemVersion() < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return;
		}
		if (context == null || photoDataStream == null || rawContactId < 0) {
			return;
		}

		final ContentResolver cr = context.getContentResolver();
		Uri rawContactPhotoUri = Uri.withAppendedPath(
				ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
				RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
		byte[] buffer = new byte[4096];
		int readCount = 0;
		int total = 0;
		try {
			AssetFileDescriptor fd = cr.openAssetFileDescriptor(rawContactPhotoUri, "rw");
			try {
				OutputStream os = fd.createOutputStream();
				try {
					while ((readCount = photoDataStream.read(buffer)) > 0) {
						os.write(buffer, 0, readCount);
						total += readCount;
					}
				} finally {
					os.close();
					buffer = null;
				}
			} finally {
				fd.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取系统联系人个数
	 * 
	 * @param context
	 * @return
	 */
	public static int getSystemContactsCount(Context context) {
		List<ContactStruct> contacts = getAllSystemContacts(context);
		int count = 0;
		if (!Util.isCollectionEmpty(contacts)) {
			count = contacts.size();
			contacts.clear();
		}
		return count;
	}

	public static long createGroup(Context context, String groupTitle, String accountName,
			String accountType) {
		if (context == null || TextUtils.isEmpty(groupTitle)) {
			return -1;
		}
		Cursor cursor = null;
		Long groupId = (long) -1;
		Uri groupUri = null;
		ContentResolver resolver = context.getContentResolver();
		try {
			ContentValues values = new ContentValues();
			values.put(Groups.TITLE, groupTitle);
			values.put(Groups.GROUP_VISIBLE, 1);
			values.put(Groups.ACCOUNT_NAME, accountName);
			values.put(Groups.ACCOUNT_TYPE, accountType);
			groupUri = resolver.insert(Groups.CONTENT_URI, values);
		} catch (Exception e) {
			e.printStackTrace();
			ContentValues values = new ContentValues();
			values.put(Groups.TITLE, groupTitle);
			values.put(Groups.GROUP_VISIBLE, true);
			values.put(Groups.ACCOUNT_NAME, accountName);
			values.put(Groups.ACCOUNT_TYPE, accountType);
			groupUri = resolver.insert(Groups.CONTENT_URI, values);
		}
		if (groupUri != null) {
			groupId = ContentUris.parseId(groupUri);
			try {
				if (!mcheckGroup) {
					cursor = resolver.query(Groups.CONTENT_URI, new String[] { Groups.ACCOUNT_NAME,
							Groups.ACCOUNT_TYPE }, Groups._ID + "=" + groupId, null, null);
					if (cursor != null && cursor.moveToFirst()) {
						String accountName1 = cursor.getString(0);
						String accountType1 = cursor.getString(1);
						if (TextUtils.isEmpty(accountType1)) {
							accountType1 = "localContacts";
						} else {
							mcheckGroup = true;
							return groupId;
						}
						if (TextUtils.isEmpty(accountName1)) {
							accountName1 = "localContacts";
						}
						ContentValues cv = new ContentValues();
						cv.put(Groups.ACCOUNT_NAME, accountName1);
						cv.put(Groups.ACCOUNT_TYPE, accountType1);
						resolver.update(Groups.CONTENT_URI, cv, Groups._ID + "=" + groupId, null);
					}
					return groupId;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return groupId;
	}

	public static void deleteGroup(Context context, long groupId) {
		if (context == null || groupId < 0) {
			return;
		}
		context.getContentResolver().delete(
				ContentUris.withAppendedId(Groups.CONTENT_URI, groupId), null, null);
	}

	public static long getGroupId(Context context, String accountType, String accountName,
			String groupTitle) {
		if (context == null || TextUtils.isEmpty(groupTitle)) {
			return -1;
		}
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		try {
			String selection = "( (" + Groups.ACCOUNT_TYPE + " NOT LIKE '%com.google" + "%' AND "
					+ Groups.ACCOUNT_TYPE + " NOT LIKE '%com.android.exchange" + "%'  ) OR "
					+ Groups.ACCOUNT_TYPE + " IS NULL OR " + Groups.ACCOUNT_NAME
					+ " NOT LIKE '%.com%'" + " ) AND " + Groups.TITLE + "='" + groupTitle
					+ "' AND " + Groups.DELETED + "=0";
			cursor = resolver.query(Groups.CONTENT_URI, new String[] { Groups._ID }, selection,
					null, Groups._ID);
			if (cursor != null && cursor.moveToFirst()) {
				return cursor.getLong(0);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return -1;
	}

	public static String getGroupTitle(Context context, long groupId) {
		if (context == null || groupId < 0) {
			return null;
		}
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = null;
		try {
			String selection = Groups._ID + "=" + groupId + " AND " + Groups.DELETED + "=0";
			cursor = resolver.query(Groups.CONTENT_URI, new String[] { Groups.TITLE }, selection,
					null, null);
			if (cursor != null && cursor.moveToFirst()) {
				return cursor.getString(0);
			}
		} catch (SQLiteException e) {

		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	public static void addMembersToGroup(Context context, long[] rawContactsToAdd, long groupId) {
		if (context == null || rawContactsToAdd == null) {
			return;
		}
		ContentResolver resolver = context.getContentResolver();
		final ArrayList<ContentProviderOperation> rawContactOperations = new ArrayList<ContentProviderOperation>();
		for (long rawContactId : rawContactsToAdd) {
			try {
				// Build an assert operation to ensure the contact is not
				// already in the group
				final ContentProviderOperation.Builder assertBuilder = ContentProviderOperation
						.newAssertQuery(Data.CONTENT_URI);
				assertBuilder.withSelection(Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE
						+ "=? AND " + GroupMembership.GROUP_ROW_ID + "=?",
						new String[] { String.valueOf(rawContactId),
								GroupMembership.CONTENT_ITEM_TYPE, String.valueOf(groupId) });
				assertBuilder.withExpectedCount(0);
				rawContactOperations.add(assertBuilder.build());

				// Build an insert operation to add the contact to the group
				final ContentProviderOperation.Builder insertBuilder = ContentProviderOperation
						.newInsert(Data.CONTENT_URI);
				insertBuilder.withValue(Data.RAW_CONTACT_ID, rawContactId);
				insertBuilder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
				insertBuilder.withValue(GroupMembership.GROUP_ROW_ID, groupId);
				rawContactOperations.add(insertBuilder.build());

				ContentProviderResult[] results = null;
				if (!rawContactOperations.isEmpty()) {
					results = resolver.applyBatch(ContactsContract.AUTHORITY, rawContactOperations);
				}
			} catch (RemoteException e) {
				// 联系人关联分组不成功
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				// 联系人可能已经与该分组相关联
				e.printStackTrace();
			} finally {
				rawContactOperations.clear();
			}
		}
	}

	private static void removeMembersFromGroup(Context context, long[] rawContactsToRemove,
			long groupId) {
		if (context == null || rawContactsToRemove == null) {
			return;
		}
		ContentResolver resolver = context.getContentResolver();
		for (long rawContactId : rawContactsToRemove) {
			resolver.delete(Data.CONTENT_URI, Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE
					+ "=? AND " + GroupMembership.GROUP_ROW_ID + "=?",
					new String[] { String.valueOf(rawContactId), GroupMembership.CONTENT_ITEM_TYPE,
							String.valueOf(groupId) });
		}
	}

	public static void setStarred(Context context, long rawContactId, boolean starred) {
		if (context == null || rawContactId < 0) {
			return;
		}
		Uri contactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
		try {
			final ContentValues values = new ContentValues();
			values.put(RawContacts.STARRED, starred);
			context.getContentResolver().update(contactUri, values, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			final ContentValues values = new ContentValues();
			values.put(RawContacts.STARRED, starred ? 1 : 0);
			context.getContentResolver().update(contactUri, values, null, null);
		}
	}

	public static void updateTheStarred(Context context, long contactId, boolean starred) {
		if (context == null || contactId < 0) {
			return;
		}
		long rawContactId = getRawContactId(context, contactId);
		if (rawContactId <= 0) {
			return;
		}
		Uri contactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
		try {
			final ContentValues values = new ContentValues();
			values.put(RawContacts.STARRED, starred);
			context.getContentResolver().update(contactUri, values, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			final ContentValues values = new ContentValues();
			values.put(RawContacts.STARRED, starred ? 1 : 0);
			context.getContentResolver().update(contactUri, values, null, null);
		}

	}

	public void backupContacts(final Context context, final BackupArg arg,
			final IAsyncTaskListener listener) {
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				exportContacts(context, arg, listener);
			}
		});
	}

	public synchronized void stopBackupContacts() {
		mStopBackupFlag = true;
	}

	private void exportContacts(Context context, BackupArg arg, IAsyncTaskListener listener) {
		if (listener != null) {
			listener.onStart(null, null);
		}
		if (context == null || arg == null) {
			if (listener != null) {
				listener.onEnd(false, null, null);
			}
			return;
		}

		List<ContactStruct> existedContacts = null;
		try {
			// 防止读取系统联系人出错时程序直接挂掉
			existedContacts = getAllSystemContacts(context);
		} catch (Exception e) {
			e.printStackTrace();
			existedContacts = null;
		}
		if (Util.isCollectionEmpty(existedContacts)) {
			if (listener != null) {
				listener.onEnd(false, null, null);
			}
			return;
		}

		File destFile = new File(arg.parentDir, arg.backupFileName);
		if (!destFile.exists()) {
			try {
				File parent = new File(arg.parentDir);
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				destFile.createNewFile();
			} catch (IOException e) {
				if (listener != null) {
					listener.onEnd(false, null, null);
				}
				return;
			}
		}

		reset();
		boolean ret = false;
		boolean cancel = false;
		final int times = 10;
		int exportContactsCount = 0;
		try {
			VCardComposer composer = new VCardComposer();
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(destFile), "UTF-8");
			final int count = existedContacts.size();
			// Log.d("GoBackup", "export contacts count = " + count);
			int portion = count / times;
			if (portion <= 0) {
				portion = count;
			}
			int i;
			for (i = 0; i < count; i++) {
				if (mStopBackupFlag) {
					throw new IllegalStateException();
				}
				final ContactStruct cs = existedContacts.get(i);

				if (i % portion == 0) {
					notifyListenerProgress(listener, i, count);
				}
				// 只备份有电话号码的联系人
				if (arg.ignoreContactsWithoutNumber) {
					if (Util.isCollectionEmpty(cs.phoneList)) {
						continue;
					}
				}

				if (arg.backupContactsAvatar) {
					// 4.0以上备份高清头像
					backupDisplayPhoto(context, arg, cs);

					cs.photoBytes = getThumbnailPhotoData(context, cs.contactId);
					cs.photoType = "PNG";
				}
				// long t = System.currentTimeMillis();
				String vcardString = composer.createVCard(cs, VCardComposer.VERSION_VCARD30_INT);
				// t = System.currentTimeMillis() -t ;
				// Log.d("GoBackup", "backup one contact time = " + t);
				osw.write(vcardString);
				osw.write("\n");
				// 头像备份完成后清空数据，释放内存
				cs.clearPhotoData();
				exportContactsCount++;
			}
			notifyListenerProgress(listener, i, count);
			osw.close();
			osw = null;
			ret = true;
			// time = System.currentTimeMillis() - time;
			// Log.d("GoBackup", "export contacts time = " + time);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			// TODO 取消时应该删除备份文件，目前备份操作不允许取消
			cancel = true;
			ret = false;
			exportContactsCount = 0;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			// 可能所有的联系人号码为空，全部过滤掉了
			if (exportContactsCount <= 0 && destFile.exists()) {
				ret = false;
				destFile.delete();
			}
			if (listener != null) {
				listener.onEnd(ret, cancel, exportContactsCount);
			}
			existedContacts.clear();
		}
	}

	private String generateContactPhotoFileName(ContactStruct contact) {
		long id = -1;
		if (!TextUtils.isEmpty(contact.mName)) {
			id = contact.mName.hashCode();
		}
		if (id < 0) {
			id = System.currentTimeMillis();
		}
		return String.valueOf(id ^ (contact.photoFileId << 7 | contact.photoFileId >>> (64 - 7)));
	}

	private void backupDisplayPhoto(Context context, BackupArg arg, ContactStruct contact) {
		if (Util.getAndroidSystemVersion() < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return;
		}
		File photoDir = new File(arg.parentDir, arg.displayPhotoDir);
		if (!photoDir.exists()) {
			photoDir.mkdirs();
		}
		InputStream is = openDisplayPhoto(context, contact.photoFileId);
		if (is == null) {
			return;
		}
		contact.photoFileName = generateContactPhotoFileName(contact);
		File photo = new File(photoDir, contact.photoFileName);
		final int bufferSize = 4096;
		byte[] buffer = new byte[bufferSize];
		try {
			try {
				OutputStream os = new BufferedOutputStream(new FileOutputStream(photo), bufferSize);
				try {
					int count = 0;
					while ((count = is.read(buffer)) > 0) {
						os.write(buffer, 0, count);
					}
					os.flush();
				} finally {
					os.close();
				}
			} finally {
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		buffer = null;
	}

	public void restoreContacts(final Context context, final RestoreArg arg,
			final IAsyncTaskListener listener) {
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				importContactsFromFile(context, arg, listener);
			}
		});
	}

	public synchronized void stopRestoreContacts() {
		mStopRestoreFalg = true;
	}

	private boolean existDuplicateContacts(Context context, ContactStruct newContact) {
		if (newContact == null) {
			return false;
		}

		if (mSystemContacts == null || mSystemContacts.size() < 1) {
			mSystemContacts = new HashSet<ContactStruct>();
			List<ContactStruct> allContacts = getAllSystemContacts(context);
			if (allContacts != null && allContacts.size() > 0) {
				mSystemContacts.addAll(allContacts);
			}
			for (Iterator<ContactStruct> it = mSystemContacts.iterator(); it.hasNext();) {
				ContactStruct contact = it.next();
				if (mSystemContactMap == null) {
					mSystemContactMap = new HashMap<ContactStruct, Long>();
				}
				if (contact != null) {
					mSystemContactMap.put(contact, contact.contactId);
				}
			}
		}
		if (mInsertedContacts == null) {
			mInsertedContacts = new HashSet<ContactStruct>();
		}

		if (mSystemContacts.contains(newContact)) {
			return true;
		}

		if (mInsertedContacts.contains(newContact)) {
			return true;
		}
		return false;
	}

	private void importContactsFromFile(Context context, RestoreArg arg, IAsyncTaskListener listener) {
		if (listener != null) {
			listener.onStart(null, null);
		}
		if (context == null || arg == null || arg.backupFile == null || !arg.backupFile.exists()) {
			if (listener != null) {
				listener.onEnd(false, null, null);
			}
			return;
		}

		reset();
		// long t = System.currentTimeMillis();
		boolean ret = false;
		boolean cancel = false;
		final File vCardFile = arg.backupFile;
		try {
			// 大文件的导入
			importLargeFile(context, arg, listener);
			// if (vCardFile.length() > MAX_FILE_SIZE) {
			// importLargeFile(context, arg, listener);
			// } else {
			// importSmallFile(context, arg, listener);
			// }
			ret = true;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			cancel = true;
			ret = false;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			clearContactSet();
			if (listener != null) {
				Integer[] result = cancel ? null : new Integer[] { mRestoreCount, mDiscardCount };
				listener.onEnd(ret, cancel, result);
			}
		}
		// t = System.currentTimeMillis() - t;
		// Log.d("GoBackup", "import contacts time = " + t);
	}

	/*
	 * private void importLargeFile(Context context, RestoreArg arg,
	 * IAsyncTaskListener listener) throws Exception { final int count =
	 * scanContactsNumInVcard(arg.backupFile); final int MAX_COUNT_PER_TIME =
	 * 30; final int INSERT_TIMES = 20; // 每一次最多插入的联系人数量 int
	 * addContactsNumPerTime = count / INSERT_TIMES <= MAX_COUNT_PER_TIME ?
	 * count / INSERT_TIMES : MAX_COUNT_PER_TIME; if (addContactsNumPerTime <=
	 * 0) { addContactsNumPerTime = count; } // Log.d("GoBackup",
	 * "contacts count = " + count); BufferedReader reader = null; reader = new
	 * BufferedReader(new InputStreamReader(new
	 * FileInputStream(arg.backupFile))); StringBuffer vcardString = new
	 * StringBuffer(); String line; boolean isSeekEnd = false; int index = 0;
	 * List<ContactStruct> contactStructs = new ArrayList<ContactStruct>();
	 * while ((line = reader.readLine()) != null) { if
	 * (line.contains(MARK_BEGIN)) { isSeekEnd = true; vcardString.delete(0,
	 * vcardString.length()); // 清空 } if (isSeekEnd) { vcardString.append(line +
	 * "\n"); } if (line.contains(MARK_END)) { isSeekEnd = false;
	 * 
	 * if (mStopRestoreFalg) { try { contactStructs.clear(); reader.close(); }
	 * catch (Exception e) { e.printStackTrace(); } finally { throw new
	 * IllegalStateException(); } }
	 * 
	 * if (vcardString.length() < 1) { continue; }
	 * 
	 * // 导入 VCardParser parser = new VCardParser(); VDataBuilder builder = new
	 * VDataBuilder(); // 解析 if (!parser.parse(vcardString.toString(), "UTF-8",
	 * builder)) { continue; } if (Util.isCollectionEmpty(builder.vNodeList)) {
	 * continue; }
	 * 
	 * // 加入数据库 ContactStruct contactStruct =
	 * ContactStruct.constructContactFromVNode( builder.vNodeList.get(0),
	 * ContactStruct.NAME_ORDER_TYPE_ENGLISH);
	 * 
	 * boolean shouldInsert = true; if (arg.discardDuplicateContacts &&
	 * existDuplicateContacts(context, contactStruct)) { shouldInsert = false;
	 * mDiscardCount++; }
	 * 
	 * if (index % addContactsNumPerTime != 0 || index + 1 >= count) { if
	 * (shouldInsert) { // 将准备插入的联系人放入Set中，用于判断以后插入的是否有重复 if (mInsertedContacts
	 * != null) { mInsertedContacts.add(contactStruct); }
	 * contactStructs.add(contactStruct); } if (index + 1 < count) { index++;
	 * continue; } }
	 * 
	 * // Log.d("GoBackup", "addContact index = " + index); // long t =
	 * System.currentTimeMillis(); List<Uri> contactsUri =
	 * batchInsertContacts(context, contactStructs); if
	 * (!Util.isCollectionEmpty(contactsUri)) { mRestoreCount +=
	 * contactsUri.size(); // 若存在关联的头像，则进行恢复 restoreAssociatedPhoto(context,
	 * contactStructs, arg.displayPhotoDir); } // t = System.currentTimeMillis()
	 * - t; // Log.d("GoBackup", "batch add contacts time = " + t);
	 * contactStructs.clear();
	 * 
	 * if (index % addContactsNumPerTime == 0 && index + 1 < count &&
	 * shouldInsert) { // 将准备插入的联系人放入Set中，用于判断以后插入的是否有重复 if (mInsertedContacts
	 * != null) { mInsertedContacts.add(contactStruct); }
	 * contactStructs.add(contactStruct); }
	 * 
	 * if (index % addContactsNumPerTime == 0) {
	 * notifyListenerProgress(listener, index, count); } index++; } } //
	 * 系统中已存在的联系人，若该备份记录存在相关联的头像，也进行恢复 restoreAssociatedPhoto(context,
	 * mSystemContacts, arg.displayPhotoDir); notifyListenerProgress(listener,
	 * index, count); contactStructs.clear(); reader.close(); }
	 */

	private void importLargeFile(Context context, RestoreArg arg, IAsyncTaskListener listener)
			throws Exception {
		final int count = scanContactsNumInVcard(arg.backupFile);
		final int MAX_COUNT_PER_TIME = 30;
		final int INSERT_TIMES = 20;
		// 每一次最多插入的联系人数量
		int addContactsNumPerTime = count / INSERT_TIMES <= MAX_COUNT_PER_TIME ? count
				/ INSERT_TIMES : MAX_COUNT_PER_TIME;
		if (addContactsNumPerTime <= 0) {
			addContactsNumPerTime = count;
		}

		int index = 0;
		List<ContactStruct> contactBundle = new ArrayList<ContactStruct>();
		ContactReader reader = new ContactReader(arg.backupFile);
		reader.beginParsing();
		try {
			while (reader.avaliable()) {
				if (mStopRestoreFalg) {
					throw new IllegalStateException();
				}
				ContactStruct contact = reader.readNextContact();
				if (contact == null) {
					continue;
				}
				boolean shouldInsert = true;
				if (arg.discardDuplicateContacts && existDuplicateContacts(context, contact)) {
					shouldInsert = false;
					mDiscardCount++;
					// 若存在关联的头像则恢复
					Long contactId = mSystemContactMap != null ? mSystemContactMap.get(contact) : null;
					if (contactId != null) {
						contact.contactId = contactId.longValue();
						restoreAssociatedPhoto(context, contact, arg.displayPhotoDir);
						// 恢复分组
						updateOneMemerToGroup(context, contact);
						// 恢复收藏
						updateContactStarred(context, contact);
					}
				}

				if (index % addContactsNumPerTime != 0 || index + 1 >= count) {
					if (shouldInsert) {
						LogUtil.d(contact.mName);
						// 将准备插入的联系人放入Set中，用于判断以后插入的是否有重复
						if (mInsertedContacts != null) {
							mInsertedContacts.add(contact);
						}
						contactBundle.add(contact);
					}
					if (index + 1 < count) {
						index++;
						continue;
					}
				}

				List<Uri> contactsUri = batchInsertContacts(context, contactBundle);
				if (!Util.isCollectionEmpty(contactsUri)) {
					mRestoreCount += contactsUri.size();
					// 若存在关联的头像，则进行恢复
					restoreAssociatedPhoto(context, contactBundle, arg.displayPhotoDir);
					// 恢复分组
					addMemerToGroup(context, contactBundle);
					// 恢复收藏
					setContactsStarred(context, contactBundle);
				}
				contactBundle.clear();

				if (index % addContactsNumPerTime == 0 && index + 1 < count && shouldInsert) {
					// 将准备插入的联系人放入Set中，用于判断以后插入的是否有重复
					if (mInsertedContacts != null) {
						mInsertedContacts.add(contact);
					}
					contactBundle.add(contact);
				}

				if (index % addContactsNumPerTime == 0) {
					notifyListenerProgress(listener, index, count);
				}
				index++;
			}
		} finally {
			reader.endParsing();
			contactBundle.clear();
		}
		notifyListenerProgress(listener, index, count);
	}

	private void setContactsStarred(Context context, Collection<ContactStruct> contacts) {
		if (Util.isCollectionEmpty(contacts)) {
			return;
		}
		for (ContactStruct contact : contacts) {
			if (contact.starred) {
				setStarred(context, contact.rawContactId, true);
			}
		}
	}

	private void updateContactStarred(Context context, ContactStruct contact) {
		if (contact == null || !contact.starred) {
			return;
		}
		updateTheStarred(context, contact.contactId, contact.starred);
	}

	private void updateOneMemerToGroup(Context context, ContactStruct contact) {
		if (contact == null) {
			return;
		}
		//		 key 为group title, value是其对应的groupId
		Map<String, Long> groupIdMap = new HashMap<String, Long>();
		if (Util.isCollectionEmpty(contact.associatedGroups)) {
			return;
		}
		for (String groupName : contact.associatedGroups) {
			Long groupId = getGroupId(context, null, null, groupName);
			if (groupId.longValue() < 0) {
				groupId = createGroup(context, groupName, null, null);
			}
			if (groupId.longValue() >= 0) {
				groupIdMap.put(groupName, groupId);
			} else {
				// 创建分组失败
				continue;
			}
		}
		Collection<Long> groups = groupIdMap.values();
		if (Util.isCollectionEmpty(groups)) {
			return;
		}
		for (Long groupId : groups) {
			updateMemberToGroup(context, contact.contactId, groupId);
		}
		if (mRawContactIdMap != null) {
			mRawContactIdMap.clear();
		}
	}

	// public int getMimetypeNumber(Context context){
	// ContentResolver resolver = context.getContentResolver();
	// resolver.query(MIMETYPE., projection, selection, selectionArgs,
	// sortOrder)
	// return
	// }

	private void updateMemberToGroup(Context context, long contactId, Long groupId) {
		Cursor cursor = null;
		ContentResolver resolver = context.getContentResolver();
		Long rawContactId = (long) 0;
		int queryCount = 0;
		try {
			if (mRawContactIdMap == null || !mRawContactIdMap.containsKey(contactId)) {
				rawContactId = getRawContactId(context, contactId);
				if (rawContactId > 0) {
					mRawContactIdMap.put(contactId, rawContactId);
				}
			} else if (mRawContactIdMap.containsKey(contactId)) {
				rawContactId = mRawContactIdMap.get(contactId);
			}
			String project = Data.RAW_CONTACT_ID + "=" + rawContactId + " AND " + Data.MIMETYPE
					+ "= '" + GroupMembership.CONTENT_ITEM_TYPE + "' AND " + Data.DATA1 + "="
					+ groupId;
			cursor = resolver.query(Data.CONTENT_URI, new String[] { Data._ID }, project, null,
					null);
			if (cursor != null) {
				queryCount = cursor.getCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		try {

			if (rawContactId == 0) {
				return;
			}
			if (queryCount > 0) {
				return;
			}
			ContentResolver resolver1 = context.getContentResolver();
			ContentValues insertCv = new ContentValues();
			insertCv.put(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
			insertCv.put(Data.DATA1, groupId);
			// insertCv.put(Data.CONTACT_ID, contactId);
			insertCv.put(Data.RAW_CONTACT_ID, rawContactId);
			insertCv.put(Data.IS_PRIMARY, 0);
			if (resolver1 != null) {
				resolver1.insert(Data.CONTENT_URI, insertCv);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addMemerToGroup(Context context, Collection<ContactStruct> contacts) {
		if (Util.isCollectionEmpty(contacts)) {
			return;
		}
		// key 为groupId, value是与其相关的rawContactId列表
		Map<Long, List<Long>> groupMap = new HashMap<Long, List<Long>>();
		// key 为group title, value是其对应的groupId
		Map<String, Long> groupIdMap = new HashMap<String, Long>();
		for (ContactStruct contact : contacts) {
			if (Util.isCollectionEmpty(contact.associatedGroups)) {
				continue;
			}
			for (String groupName : contact.associatedGroups) {
				Long groupId = groupIdMap.get(groupName);
				if (groupId == null) {
					groupId = getGroupId(context, null, null, groupName);
					if (groupId.longValue() < 0) {
						groupId = createGroup(context, groupName, null, null);
					}
					if (groupId.longValue() >= 0) {
						groupIdMap.put(groupName, groupId);
					} else {
						// 创建分组失败
						continue;
					}
				}
				List<Long> contactsId = groupMap.get(groupId);
				if (contactsId == null) {
					contactsId = new ArrayList<Long>();
					groupMap.put(groupId, contactsId);
				}
				contactsId.add(contact.rawContactId);
			}
		}

		Set<Long> groups = groupMap.keySet();
		if (Util.isCollectionEmpty(groups)) {
			return;
		}
		for (Long groupId : groups) {
			List<Long> value = groupMap.get(groupId);
			if (value != null) {
				int size = value.size();
				long[] contactsId = new long[size];
				for (int i = 0; i < size; i++) {
					contactsId[i] = value.get(i);
				}
				addMembersToGroup(context, contactsId, groupId);
			}
		}
	}

	private void restoreAssociatedPhoto(Context context, Collection<ContactStruct> contacts,
			File photoDir) {
		if (Util.isCollectionEmpty(contacts) || photoDir == null || !photoDir.exists()
				|| !photoDir.isDirectory()) {
			return;
		}
		for (ContactStruct contact : contacts) {
			restoreAssociatedPhoto(context, contact, photoDir);
		}
	}

	private void restoreAssociatedPhoto(Context context, ContactStruct contact, File photoDir) {
		if (TextUtils.isEmpty(contact.photoFileName)) {
			return;
		}
		File photoFile = new File(photoDir, contact.photoFileName);
		if (!photoFile.exists()) {
			return;
		}
		restoreContactsDisplayPhoto(context, contact, photoFile);
	}

	private void notifyListenerProgress(IAsyncTaskListener listener, int index, int count) {
		if (listener == null) {
			return;
		}
		final int sleepInterval = 50;
		listener.onProceeding((float) index / (float) count, index, count, null);
		try {
			Thread.sleep(sleepInterval);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * 导入较小的文件，一次性解析
	 * 
	 * @param vCardFile
	 * @throws Exception
	 */
	private void importSmallFile(Context context, RestoreArg arg, IAsyncTaskListener listener)
			throws Exception {
		// 小文件
		List<VNode> pimContacts = parseFile(arg.backupFile);

		if (pimContacts == null || pimContacts.size() < 1) {
			return;
		}

		List<ContactStruct> contactStructs = new ArrayList<ContactStruct>();
		final int maxContactsPerTime = 30;
		final int times = 20;
		final int count = pimContacts.size();
		// 每一次最多插入的联系人数量
		int addContactsNumPerTime = count / times <= maxContactsPerTime
				? count / times
				: maxContactsPerTime;
		if (addContactsNumPerTime <= 0) {
			addContactsNumPerTime = count;
		}
		int i;
		for (i = 0; i < count; i++) {
			if (mStopRestoreFalg) {
				contactStructs.clear();
				pimContacts.clear();
				throw new IllegalStateException();
			}

			final VNode contactNode = pimContacts.get(i);
			ContactStruct contactStruct = ContactStruct.constructContactFromVNode(contactNode,
					ContactStruct.NAME_ORDER_TYPE_ENGLISH);

			boolean shouldInsert = true;
			if (arg.discardDuplicateContacts && existDuplicateContacts(context, contactStruct)) {
				shouldInsert = false;
				mDiscardCount++;
			}

			if (i % addContactsNumPerTime != 0 || i + 1 >= count) {
				if (shouldInsert) {
					// 将准备插入的联系人放入Set中，用于判断以后插入的是否有重复
					if (mInsertedContacts != null) {
						mInsertedContacts.add(contactStruct);
					}
					contactStructs.add(contactStruct);
				}
				if (i + 1 < count) {
					continue;
				}
			}

			// long t = System.currentTimeMillis();
			// 添加联系人信息
			List<Uri> contactsUri = batchInsertContacts(context, contactStructs);
			if (!Util.isCollectionEmpty(contactsUri)) {
				mRestoreCount += contactsUri.size();
				// 若存在关联的头像，则进行恢复
				restoreAssociatedPhoto(context, contactStructs, arg.displayPhotoDir);
			}
			// t = System.currentTimeMillis() - t;
			// Log.d("GoBackup", "batch add contacts time = " + t);

			contactStructs.clear();
			if (i % addContactsNumPerTime == 0 && i + 1 < count && shouldInsert) {
				// 将准备插入的联系人放入Set中，用于判断以后插入的是否有重复
				if (mInsertedContacts != null) {
					mInsertedContacts.add(contactStruct);
				}
				contactStructs.add(contactStruct);
			}

			if (i % addContactsNumPerTime == 0) {
				notifyListenerProgress(listener, i, count);
			}
		}
		// 系统中已存在的联系人，若该备份记录存在相关联的头像，也进行恢复
		restoreAssociatedPhoto(context, mSystemContacts, arg.displayPhotoDir);
		notifyListenerProgress(listener, i, count);
		contactStructs.clear();
		pimContacts.clear();
	}

	private void addContactOperations(ArrayList<ContentProviderOperation> ops, ContactStruct struct) {
		if (ops == null) {
			return;
		}
		int index = ops.size();

		ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
				.withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED)
				.withYieldAllowed(true).build());

		// 名字
		ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
				.withValueBackReference(StructuredName.RAW_CONTACT_ID, index)
				.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
				.withValue(StructuredName.GIVEN_NAME, struct.mGivenName)
				.withValue(StructuredName.FAMILY_NAME, struct.mFamilyName)
				.withValue(StructuredName.MIDDLE_NAME, struct.mMiddleName)
				.withValue(StructuredName.PREFIX, struct.mPrefix)
				.withValue(StructuredName.SUFFIX, struct.mSuffix)
				.withValue(StructuredName.PHONETIC_GIVEN_NAME, struct.mPhoneticGivenName)
				.withValue(StructuredName.PHONETIC_FAMILY_NAME, struct.mPhoneticFamilyName)
				.withValue(StructuredName.PHONETIC_MIDDLE_NAME, struct.mPhoneticMiddleName)
				.withValue(StructuredName.DISPLAY_NAME, struct.mName).withYieldAllowed(true)
				.build());

		// 头像
		if (struct.photoBytes != null) {
			ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
					.withValueBackReference(Photo.RAW_CONTACT_ID, index)
					.withValue(Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
					.withValue(Photo.PHOTO, struct.photoBytes).withYieldAllowed(true).build());
		}

		// 插入电话号码
		if (struct.phoneList != null && struct.phoneList.size() > 0) {
			for (PhoneData phoneData : struct.phoneList) {
				int primary = phoneData.isPrimary;
				ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
						.withValueBackReference(Phone.RAW_CONTACT_ID, index)
						.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
						.withValue(Phone.NUMBER, phoneData.data)
						.withValue(Phone.TYPE, phoneData.type).withValue(Data.IS_PRIMARY, primary)
						.withValue(Phone.LABEL, phoneData.label).withYieldAllowed(true).build());
			}
		}

		// 插入邮件、地址、IM、EVENT、WEBSITE、RELATION
		if (struct.contactmethodList != null && struct.contactmethodList.size() > 0) {
			for (ContactMethod contactMethod : struct.contactmethodList) {
				String mContentItemType = null;
				if (contactMethod.kind == ContactDataKind.EMAIL) {
					mContentItemType = Email.CONTENT_ITEM_TYPE;
				} else if (contactMethod.kind == ContactDataKind.POSTAL) {
					mContentItemType = StructuredPostal.CONTENT_ITEM_TYPE;
				} else if (contactMethod.kind == ContactDataKind.IM) {
					mContentItemType = Im.CONTENT_ITEM_TYPE;
					int primary = contactMethod.isPrimary;
					ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
							.withValueBackReference(Data.RAW_CONTACT_ID, index)
							.withValue(Data.MIMETYPE, mContentItemType)
							.withValue(Data.DATA1, contactMethod.data)
							.withValue(Data.DATA2, contactMethod.type)
							.withValue(Data.IS_PRIMARY, primary).withYieldAllowed(true).build());
					continue;
				} else if (contactMethod.kind == ContactDataKind.NICKNAME) {
					mContentItemType = Nickname.CONTENT_ITEM_TYPE;
				} else if (contactMethod.kind == ContactDataKind.WEBSITE) {
					mContentItemType = Website.CONTENT_ITEM_TYPE;
				} else {
					continue;
				}

				int primary = contactMethod.isPrimary;
				ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
						.withValueBackReference(Data.RAW_CONTACT_ID, index)
						.withValue(Data.MIMETYPE, mContentItemType)
						.withValue(Data.DATA1, contactMethod.data)
						.withValue(Data.DATA2, contactMethod.type)
						.withValue(Data.DATA3, contactMethod.label)
						.withValue(Data.IS_PRIMARY, primary).withYieldAllowed(true).build());
			}
		}

		// 插入组织
		if (struct.organizationList != null && struct.organizationList.size() > 0) {
			for (OrganizationData organizationData : struct.organizationList) {
				int primary = organizationData.isPrimary;
				ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
						.withValueBackReference(Organization.RAW_CONTACT_ID, index)
						.withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
						.withValue(Organization.COMPANY, organizationData.companyName)
						.withValue(Organization.IS_PRIMARY, primary)
						.withValue(Organization.TITLE, organizationData.positionName)
						.withValue(Organization.TYPE, organizationData.type)
						.withValue(Organization.LABEL, organizationData.label)
						.withYieldAllowed(true).build());
			}
		}

		// 插入备注
		if (struct.notes != null && struct.notes.size() > 0) {
			for (String note : struct.notes) {
				ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
						.withValueBackReference(Note.RAW_CONTACT_ID, index)
						.withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
						.withValue(Note.NOTE, note).withYieldAllowed(true).build());
			}
		}

	}
	// 添加单个联系人
	/*
	 * private boolean addContact(Context context, ContactStruct struct) { if
	 * (context == null) { return false; } if (struct == null) { return true; }
	 * 
	 * ArrayList<ContentProviderOperation> ops = new
	 * ArrayList<ContentProviderOperation>(); addContactOperations(ops, struct);
	 * if (ops == null || ops.size() <= 0) { return true; } try {
	 * context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
	 * } catch (Exception e) { e.printStackTrace(); return false; }
	 * 
	 * return true; }
	 */

	/**
	 * 批量插入联系人到数据库
	 * 
	 * @param context
	 * @param contactStructs
	 * @return 插入的联系人的raw contact uri
	 */
	private List<Uri> batchInsertContacts(Context context, List<ContactStruct> contactStructs) {
		if (context == null || Util.isCollectionEmpty(contactStructs)) {
			return null;
		}
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		for (ContactStruct oneContact : contactStructs) {
			addContactOperations(ops, oneContact);
		}
		if (Util.isCollectionEmpty(ops)) {
			return null;
		}
		ContentProviderResult[] results = null;
		try {
			results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			ops.clear();
			ops = null;
		}

		List<Uri> contactsUri = new ArrayList<Uri>();
		final int count = results != null ? results.length : 0;
		for (int i = 0; i < count; i++) {
			final Uri uri = results[i].uri;
			if (uri != null) {
				String path = uri.getPath();
				if (path != null && path.contains("raw_contacts")) {
					contactsUri.add(uri);
				}
			}
		}

		if (contactsUri.size() == contactStructs.size()) {
			final int uriCount = contactsUri.size();
			for (int i = 0; i < uriCount; i++) {
				contactStructs.get(i).rawContactId = ContentUris.parseId(contactsUri.get(i));
			}
		}

		return contactsUri;
	}

	public static Set<ContactStruct> loadContactsFromVCard(File vCardFile) {
		if (vCardFile == null || !vCardFile.exists()) {
			return null;
		}
		// TODO 读入所有联系人信息，可能会有内存溢出的问题
		/*
		 * List<VNode> vNodes = parseFile(vCardFile); if (vNodes == null ||
		 * vNodes.size() < 1) { return null; } Set<ContactStruct> contacts = new
		 * HashSet<ContactStruct>(); for (VNode node : vNodes) { ContactStruct
		 * contactStruct = ContactStruct.constructContactFromVNode(node,
		 * ContactStruct.NAME_ORDER_TYPE_ENGLISH); if (contactStruct != null) {
		 * contacts.add(contactStruct); } } vNodes.clear();
		 */
		Set<ContactStruct> contacts = new HashSet<ContactStruct>();
		ContactReader reader = new ContactReader(vCardFile);
		List<ContactStruct> allContacts = reader.readAllContacts();
		if (!Util.isCollectionEmpty(allContacts)) {
			contacts.addAll(allContacts);
		}
		return contacts;
	}

	public static boolean saveContactsToVCardFile(Collection<ContactStruct> contactStructs,
			File destFile) {
		if (contactStructs == null || contactStructs.size() < 1 || destFile == null) {
			return false;
		}
		OutputStreamWriter osw = null;
		boolean result = false;
		try {
			if (destFile.exists()) {
				destFile.delete();
			}
			VCardComposer composer = new VCardComposer();
			osw = new OutputStreamWriter(new FileOutputStream(destFile), "UTF-8");
			try {
				for (ContactStruct cs : contactStructs) {
					String vcardString = composer
							.createVCard(cs, VCardComposer.VERSION_VCARD30_INT);
					osw.write(vcardString);
					osw.write("\n");
				}
			} finally {
				osw.close();
			}
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (VCardException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 解析文件
	 * 
	 * @param fileName
	 *            ,文件名
	 * @return 联系人结点列表
	 */
	private static List<VNode> parseFile(File vCardFile) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(vCardFile)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (reader == null) {
			return null;
		}

		StringBuffer vcardString = new StringBuffer();
		String line;
		try {
			try {
				while ((line = reader.readLine()) != null) {
					vcardString.append(line + "\n");
				}
			} finally {
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (vcardString.length() < 1) {
			return null;
		}

		// 解析
		VCardParser parser = new VCardParser();
		VDataBuilder builder = new VDataBuilder();
		boolean parsed = false;
		try {
			parsed = parser.parse(vcardString.toString(), "UTF-8", builder);
			if (!parsed) {
				try {
					throw new VCardException("Could not parse vCard file: " + vCardFile.getName());
				} catch (VCardException e) {
					e.printStackTrace();
				}
			}
		} catch (VCardException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.vNodeList;
	}

	/**
	 * 获得文件联系人个数,大文件使用
	 * 
	 * @param vCardFile
	 * @return
	 */
	public static int scanContactsNumInVcard(File vCardFile) {
		if (vCardFile == null || !vCardFile.exists()) {
			return 0;
		}
		return new ContactReader(vCardFile).getContactsCount();
	}

	private synchronized void reset() {
		mStopBackupFlag = false;
		mStopRestoreFalg = false;
		mRestoreCount = 0;
		mDiscardCount = 0;
	}

	private void clearContactSet() {
		if (mSystemContacts != null) {
			mSystemContacts.clear();
			mSystemContacts = null;
		}
		if (mInsertedContacts != null) {
			mInsertedContacts.clear();
			mInsertedContacts = null;
		}
		if (mSystemContactMap != null) {
			mSystemContactMap.clear();
			mSystemContactMap = null;
		}
	}

	private static boolean isEmptyString(String string) {
		return string == null || string.trim().equals("");
	}

	/**
	 * 从contact id 转换为 raw contact id
	 * 
	 * @param context
	 * @param contactId
	 * @return
	 */
	public static long getRawContactId(Context context, long contactId) {
		if (context == null || contactId < 0) {
			throw new IllegalArgumentException("invalid argument");
		}
		String selection = RawContacts.CONTACT_ID + "=" + contactId;
		final Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI,
				new String[] { RawContacts._ID, RawContacts.CONTACT_ID }, selection, null, null);
		if (cursor == null) {
			return -1;
		}
		try {
			if (cursor != null && cursor.moveToFirst()) {
				return cursor.getLong(0);
			}
		} finally {
			cursor.close();
		}
		return -1;
	}

	public void restoreContactsDisplayPhoto(Context context, ContactStruct contact, File photoFile) {
		if (context == null || contact == null || photoFile == null || !photoFile.exists()) {
			return;
		}
		try {
			FileInputStream is = new FileInputStream(photoFile);
			if (contact.rawContactId < 0) {
				contact.rawContactId = getRawContactId(context, contact.contactId);
			}
			try {
				writeDisplayPhoto(context, contact.rawContactId, is);
			} finally {
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 一个解析VCF文件，生成联系人结构的解析类 逐个解析联系人结构，避免一次性读入导致内存溢出的问题
	 * 
	 * @author maiyongshen
	 * 
	 */
	private static class ContactReader {
		private static final int DEFAULT_BUFFER_SIZE = 256;
		private BufferedReader mReader;
		private StringBuilder mStringBuilder;
		private File mVCardFile;
		private VCardParser mParser;
		private boolean mInited = false;
		private int mContactsCount = -1;
		private boolean mEnded = false;

		public ContactReader(File vCardFile) {
			if (vCardFile == null || !vCardFile.exists()) {
				throw new IllegalArgumentException("invalid argument");
			}
			mVCardFile = vCardFile;
		}

		private boolean hasInited() {
			return mInited;
		}

		private void closeBufferedReader() {
			if (mReader != null) {
				try {
					mReader.close();
				} catch (IOException e) {
				}
				mReader = null;
			}
		}

		private void resetReader() {
			closeBufferedReader();
			try {
				mReader = new BufferedReader(new InputStreamReader(new FileInputStream(mVCardFile)));
			} catch (IOException e) {
				throw new IllegalStateException("Initialize reader failed");
			}
		}

		public void beginParsing() {
			if (mInited && mEnded) {
				throw new IllegalStateException(
						"Contact Reader has finished parsing work(you should create a new reader)");
			}
			resetReader();
			mStringBuilder = new StringBuilder(DEFAULT_BUFFER_SIZE);
			mParser = new VCardParser();
			mInited = true;
		}

		public void endParsing() {
			closeBufferedReader();
			if (mStringBuilder != null) {
				mStringBuilder.delete(0, mStringBuilder.length());
				mStringBuilder = null;
			}
			mVCardFile = null;
		}

		public ContactStruct readNextContact() {
			if (!hasInited()) {
				throw new IllegalStateException(
						"ContactReader hasn't been initialized, should call beginParsing() first");
			}
			if (!avaliable()) {
				return null;
			}
			ContactStruct result = null;
			String line = null;
			boolean possibleEnd = false;
			try {
				boolean seekingEnd = false;
				while ((line = mReader.readLine()) != null) {
					if (line.contains(MARK_BEGIN)) {
						seekingEnd = true;
						mStringBuilder.delete(0, mStringBuilder.length()); // 清空
					}
					if (seekingEnd) {
						mStringBuilder.append(line + "\n");
					}
					if (line.contains(MARK_END)) {
						VDataBuilder dataBuilder = new VDataBuilder();
						if (mParser.parse(mStringBuilder.toString(), "UTF-8", dataBuilder)
								&& !Util.isCollectionEmpty(dataBuilder.vNodeList)) {
							result = ContactStruct.constructContactFromVNode(
									dataBuilder.vNodeList.get(0),
									ContactStruct.NAME_ORDER_TYPE_ENGLISH);
						}
						break;
					}
				}
				if (line == null) {
					possibleEnd = true;
				}
			} catch (IOException e) {

			} catch (VCardException e) {

			}
			if (result == null && possibleEnd) {
				mEnded = true;
			}
			return result;
		}

		public boolean avaliable() {
			return !mEnded;
		}

		public List<ContactStruct> readAllContacts() {
			beginParsing();
			List<ContactStruct> contacts = new ArrayList<ContactStruct>();
			while (avaliable()) {
				// long t = System.currentTimeMillis();
				ContactStruct contact = readNextContact();
				// t = System.currentTimeMillis() - t;
				// Log.d("GoBackup", "load single contact time =" + t);
				if (contact != null) {
					contacts.add(contact);
				}
			}
			endParsing();
			return !Util.isCollectionEmpty(contacts) ? contacts : null;
		}

		public int getContactsCount() {
			if (mContactsCount < 0) {
				boolean isSeekEnd = false;
				String line = null;
				int sum = 0;
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(
							new FileInputStream(mVCardFile)));
					try {
						while ((line = reader.readLine()) != null) {
							if (line.contains(MARK_BEGIN)) {
								isSeekEnd = true;
							}
							if (line.contains(MARK_END) && isSeekEnd) {
								sum++;
								isSeekEnd = false;
							}
						}
					} finally {
						reader.close();
					}
				} catch (IOException e) {
					sum = 0;
				}
				mContactsCount = sum;
			}
			return mContactsCount;
		}
	}
}

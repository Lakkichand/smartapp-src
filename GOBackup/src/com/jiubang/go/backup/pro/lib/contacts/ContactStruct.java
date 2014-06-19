/*
 * Copyright (C) 2007 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiubang.go.backup.pro.lib.contacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.provider.Contacts;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.lib.contacts.vcard.PropertyNode;
import com.jiubang.go.backup.pro.lib.contacts.vcard.VNode;
import com.jiubang.go.backup.pro.util.LogUtil;
import com.jiubang.go.backup.pro.util.Util;
//CHECKSTYLE:OFF
@SuppressWarnings("deprecation")
public class ContactStruct {
	private static final int NAME_ORDER_TYPE_JAPANESE = 1;
	private static final String MOBILE_EMAIL_TYPE_NAME = "_AUTO_CELL";
	public static final int NAME_ORDER_TYPE_ENGLISH = 0;

	public static final String PHOTO_FILE_NAME = "PHOTO_FILE_NAME";
	public static final String GROUP = "X-GROUP";
	public static final String STARRED = "X-STARRED";

	public String mName;
	public String mGivenName;
	public String mFamilyName;
	public String mMiddleName;
	public String mPrefix;
	public String mSuffix;
	public String mPhoneticGivenName;
	public String mPhoneticMiddleName;
	public String mPhoneticFamilyName;

	/** MUST exist */
	public String phoneticName;
	/** maybe folding */
	public List<String> notes = new ArrayList<String>();
	/** maybe folding */
	public String title;
	/** binary bytes of pic. */
	public byte[] photoBytes;
	/** The type of Photo (e.g. JPEG, BMP, etc.) */
	public String photoType;
	/** Only for GET. Use addPhoneList() to PUT. */
	public List<PhoneData> phoneList;
	/** Only for GET. Use addContactmethodList() to PUT. */
	public List<ContactMethod> contactmethodList;
	/** Only for GET. Use addOrgList() to PUT. */
	public List<OrganizationData> organizationList;
	/** Only for GET. Use addExtension() to PUT */
	public Map<String, List<String>> extensionMap;

	// 铃声
	public String ringTone;

	// 联系人ID
	public long contactId = -1;
	// raw contact id
	public long rawContactId = -1;
	// 高清头像文件ID，4.0以上系统才有
	public long photoFileId = -1;
	// 高清头像的存放文件名
	public String photoFileName;
	// 所关联的分组
	public List<String> associatedGroups;
	// 是否被收藏
	public boolean starred;

	public static class PhoneData {
		public int type;
		/** maybe folding */
		public String data;
		public String label;
		public int isPrimary;

		@Override
		public int hashCode() {
			int hashCode = 17;
			// hashCode = 31 * hashCode + type;
			hashCode = 31 * hashCode + (TextUtils.isEmpty(data) ? 0 : data.hashCode());
			// hashCode = 31 * hashCode + (TextUtils.isEmpty(label) ? 0 :
			// label.hashCode());
			// hashCode = 31 * hashCode + isPrimary;
			return hashCode;
		}

		@Override
		public boolean equals(Object object) {
			if (object == this) {
				return true;
			}
			if (!(object instanceof PhoneData)) {
				return false;
			}
			PhoneData other = (PhoneData) object;
			/*
			 * if (type != other.type) { return false; }
			 */
			if (TextUtils.isEmpty(data) ? !TextUtils.isEmpty(other.data) : !TextUtils.equals(data,
					other.data)) {
				return false;
			}
			/*
			 * if (TextUtils.isEmpty(label) ? !TextUtils.isEmpty(other.label) :
			 * !TextUtils.equals(label, other.label)) { return false; }
			 */
			return true;
		}
	}

	public static class ContactMethod {
		public int kind;
		public int type = -1;
		public String data;
		public String label;
		public int isPrimary;
		public String title;
		public String custom_Protocol;
		public String protocol;

		@Override
		public int hashCode() {
			int hashCode = 17;
			hashCode = 31 * hashCode + kind;
			hashCode = 31 * hashCode + type;
			hashCode = 31 * hashCode + (TextUtils.isEmpty(data) ? 0 : data.hashCode());
			hashCode = 31 * hashCode + (TextUtils.isEmpty(label) ? 0 : label.hashCode());
			// hashCode = 31 * hashCode + isPrimary;
			hashCode = 31 * hashCode + (TextUtils.isEmpty(title) ? 0 : title.hashCode());
			hashCode = 31 * hashCode
					+ (TextUtils.isEmpty(custom_Protocol) ? 0 : custom_Protocol.hashCode());
			hashCode = 31 * hashCode + (TextUtils.isEmpty(protocol) ? 0 : protocol.hashCode());
			return hashCode;
		}

		@Override
		public boolean equals(Object object) {
			if (object == this) {
				return true;
			}
			if (!(object instanceof ContactMethod)) {
				return false;
			}
			ContactMethod other = (ContactMethod) object;
			if (type != other.type || kind != other.kind) {
				return false;
			}
			if (TextUtils.isEmpty(data) ? !TextUtils.isEmpty(other.data) : !TextUtils.equals(data,
					other.data)) {
				return false;
			}
			if (TextUtils.isEmpty(label) ? !TextUtils.isEmpty(other.label) : !TextUtils.equals(
					label, other.label)) {
				return false;
			}
			if (TextUtils.isEmpty(title) ? !TextUtils.isEmpty(other.title) : !TextUtils.equals(
					title, other.title)) {
				return false;
			}
			if (TextUtils.isEmpty(custom_Protocol)
					? !TextUtils.isEmpty(other.custom_Protocol)
					: !TextUtils.equals(custom_Protocol, other.custom_Protocol)) {
				return false;
			}
			if (TextUtils.isEmpty(protocol) ? !TextUtils.isEmpty(other.protocol) : !TextUtils
					.equals(protocol, other.protocol)) {
				return false;
			}
			return true;
		}
	}

	public static class OrganizationData {
		public int type;
		// Used only when TYPE is TYPE_CUSTOM.
		public String label;
		public String companyName;
		public String positionName;
		public int isPrimary;

		@Override
		public int hashCode() {
			int hashCode = 17;
			hashCode = 31 * hashCode + type;
			hashCode = 31 * hashCode + (TextUtils.isEmpty(label) ? 0 : label.hashCode());
			hashCode = 31 * hashCode
					+ (TextUtils.isEmpty(companyName) ? 0 : companyName.hashCode());
			hashCode = 31 * hashCode
					+ (TextUtils.isEmpty(positionName) ? 0 : positionName.hashCode());
			// hashCode = 31 * hashCode + isPrimary;
			return hashCode;
		}

		@Override
		public boolean equals(Object object) {
			if (object == this) {
				return true;
			}
			if (!(object instanceof OrganizationData)) {
				return false;
			}
			OrganizationData other = (OrganizationData) object;
			if (type != other.type) {
				return false;
			}
			if (TextUtils.isEmpty(label) ? !TextUtils.isEmpty(other.label) : !TextUtils.equals(
					label, other.label)) {
				return false;
			}
			if (TextUtils.isEmpty(companyName) ? !TextUtils.isEmpty(other.companyName) : !TextUtils
					.equals(companyName, other.companyName)) {
				return false;
			}
			if (TextUtils.isEmpty(positionName)
					? !TextUtils.isEmpty(other.positionName)
					: !TextUtils.equals(positionName, other.positionName)) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Add a phone info to phoneList.
	 *
	 * @param data
	 *            phone number
	 * @param type
	 *            type col of content://contacts/phones
	 * @param label
	 *            lable col of content://contacts/phones
	 */
	public void addPhone(int type, String data, String label, int isPrimary) {
		if (phoneList == null) {
			phoneList = new ArrayList<PhoneData>();
		}
		PhoneData phoneData = new PhoneData();
		phoneData.type = type;

		StringBuilder builder = new StringBuilder();
		if (data == null) {
			return;
		}
		String trimed = data.trim();
		if (trimed == null) {
			return;
		}
		int length = trimed.length();
		for (int i = 0; i < length; i++) {
			char ch = trimed.charAt(i);
			if (('0' <= ch && ch <= '9') || (ch == '+') || (ch == '.') || (ch == '-')
					|| (ch == '(') || (ch == ')') || (ch == '*') || (ch == '#') || (ch == ' ')
					|| (ch == 'p') || (ch == 'P') || (ch == 'w') || (ch == 'W')) {
				builder.append(ch);
			}
		}

		// 解决设置主要号码失败的BUG
		// phoneData.data = PhoneNumberUtils.formatNumber(builder.toString());
		phoneData.data = builder.toString();
		phoneData.label = label;
		phoneData.isPrimary = isPrimary;
		phoneList.add(phoneData);
	}

	/**
	 * Add a contactmethod info to contactmethodList.
	 *
	 * @param kind
	 *            integer value defined in Contacts.java (e.g.
	 *            Contacts.KIND_EMAIL)
	 * @param type
	 *            type col of content://contacts/contact_methods
	 * @param data
	 *            contact data
	 * @param label
	 *            extra string used only when kind is Contacts.KIND_CUSTOM.
	 */
	public void addContactmethod(int kind, int type, String data, String label, int isPrimary) {
		if (contactmethodList == null) {
			contactmethodList = new ArrayList<ContactMethod>();
		}
		ContactMethod contactMethod = new ContactMethod();
		contactMethod.kind = kind;
		contactMethod.type = type;
		contactMethod.data = data;
		contactMethod.label = label;
		contactMethod.isPrimary = isPrimary;
		contactmethodList.add(contactMethod);
	}

	/**
	 * Add a Organization info to organizationList.
	 */
	public void addOrganization(int type, String label, String companyName, String positionName,
			int isPrimary) {
		if (organizationList == null) {
			organizationList = new ArrayList<OrganizationData>();
		}
		OrganizationData organizationData = new OrganizationData();
		organizationData.type = type;
		organizationData.label = label;
		organizationData.companyName = companyName;
		organizationData.positionName = positionName;
		organizationData.isPrimary = isPrimary;
		organizationList.add(organizationData);
	}

	/**
	 * Set "position" value to the appropriate data. If there's more than one
	 * OrganizationData objects, the value is set to the last one. If there's no
	 * OrganizationData object, a new OrganizationData is created, whose company
	 * name is empty. TODO: incomplete logic. fix this: e.g. This assumes ORG
	 * comes earlier, but TITLE may come earlier like this, though we do not
	 * know how to handle it in general cases... ---- TITLE:Software Engineer
	 * ORG:Google ----
	 */
	public void setPosition(String positionValue) {
		if (organizationList == null) {
			organizationList = new ArrayList<OrganizationData>();
		}
		int size = organizationList.size();
		if (size == 0) {
			addOrganization(Contacts.OrganizationColumns.TYPE_OTHER, null, "", null, 0);
			size = 1;
		}
		OrganizationData lastData = organizationList.get(size - 1);
		lastData.positionName = positionValue;
	}

	public void addExtension(PropertyNode propertyNode) {
		if (propertyNode.propValue.length() == 0) {
			return;
		}
		// Now store the string into extensionMap.
		List<String> list;
		String name = propertyNode.propName;
		if (extensionMap == null) {
			extensionMap = new HashMap<String, List<String>>();
		}
		if (!extensionMap.containsKey(name)) {
			list = new ArrayList<String>();
			extensionMap.put(name, list);
		} else {
			list = extensionMap.get(name);
		}

		list.add(propertyNode.encode());
	}

	private static String getNameFromNProperty(List<String> elems, int nameOrderType) {
		// Family, Given, Middle, Prefix, Suffix. (1 - 5)
		int size = elems.size();
		if (size > 1) {
			StringBuilder builder = new StringBuilder();
			boolean builderIsEmpty = true;
			// Prefix
			if (size > 3 && elems.get(3).length() > 0) {
				builder.append(elems.get(3));
				builderIsEmpty = false;
			}
			String first, second;
			if (nameOrderType == NAME_ORDER_TYPE_JAPANESE) {
				first = elems.get(0);
				second = elems.get(1);
			} else {
				first = elems.get(1);
				second = elems.get(0);
			}
			if (first.length() > 0) {
				if (!builderIsEmpty) {
					builder.append(' ');
				}
				builder.append(first);
				builderIsEmpty = false;
			}
			// Middle name
			if (size > 2 && elems.get(2).length() > 0) {
				if (!builderIsEmpty) {
					builder.append(' ');
				}
				builder.append(elems.get(2));
				builderIsEmpty = false;
			}
			if (second.length() > 0) {
				if (!builderIsEmpty) {
					builder.append(' ');
				}
				builder.append(second);
				builderIsEmpty = false;
			}
			// Suffix
			if (size > 4 && elems.get(4).length() > 0) {
				if (!builderIsEmpty) {
					builder.append(' ');
				}
				builder.append(elems.get(4));
				builderIsEmpty = false;
			}
			return builder.toString();
		} else if (size == 1) {
			return elems.get(0);
		} else {
			return "";
		}
	}

	public static ContactStruct constructContactFromVNode(VNode node, int nameOrderType) {
		if (!node.VName.equals("VCARD")) {
			// Impossible in current implementation. Just for safety.
			return null;
		}

		// For name, there are three fields in vCard: FN, N, NAME.
		// We prefer FN, which is a required field in vCard 3.0 , but not in
		// vCard 2.1.
		// Next, we prefer NAME, which is defined only in vCard 3.0.
		// Finally, we use N, which is a little difficult to parse.
		String fullName = null;
		String nameFromNProperty = null;

		ContactStruct contact = new ContactStruct();

		// Each Column of four properties has ISPRIMARY field
		// (See android.provider.Contacts)
		// If false even after the following loop, we choose the first
		// entry as a "primary" entry.
		boolean prefIsSetAddress = false;
		boolean prefIsSetPhone = false;
		boolean prefIsSetEmail = false;
		boolean prefIsSetOrganization = false;

		for (PropertyNode propertyNode : node.propList) {
			String name = propertyNode.propName;

			if (TextUtils.isEmpty(propertyNode.propValue)) {
				continue;
			}

			if (name.equals(StructuredName.DISPLAY_NAME)) {
				contact.mName = propertyNode.propValue;
			} else if (name.equals("VERSION")) {
				// vCard version. Ignore this.
			} else if (name.equals("FN")) {
				fullName = propertyNode.propValue;
			} else if (name.equals("NAME") && fullName == null) {
				// Only in vCard 3.0. Use this if FN does not exist.
				// Though, note that vCard 3.0 requires FN.
				fullName = propertyNode.propValue;
			} else if (name.equals("N")) {
				nameFromNProperty = getNameFromNProperty(propertyNode.propValue_vector,
						nameOrderType);
				int size = propertyNode.propValue_vector.size();
				if (size >= 5) {
					// Family, Given, Middle, Prefix, Suffix
					contact.mFamilyName = propertyNode.propValue_vector.get(0);
					contact.mGivenName = propertyNode.propValue_vector.get(1);
					contact.mMiddleName = propertyNode.propValue_vector.get(2);
					contact.mPrefix = propertyNode.propValue_vector.get(3);
					contact.mSuffix = propertyNode.propValue_vector.get(4);
				}
			} else if (name.equals("SORT-STRING")) {
				contact.phoneticName = propertyNode.propValue;
			} else if (name.equals("SOUND")) {
				if (propertyNode.paramMap_TYPE.contains("X-IRMC-N") && contact.phoneticName == null) {
					// Some Japanese mobile phones use this field for phonetic
					// name,
					// since vCard 2.1 does not have "SORT-STRING" type.
					// Also, in some cases, the field has some ';' in it.
					// We remove them.
					StringBuilder builder = new StringBuilder();
					String value = propertyNode.propValue;
					int length = value.length();
					for (int i = 0; i < length; i++) {
						char ch = value.charAt(i);
						if (ch != ';') {
							builder.append(ch);
						}
					}
					contact.phoneticName = builder.toString();
				} else {
					contact.addExtension(propertyNode);
				}
			} else if (name.equals("ADR")) {
				List<String> values = propertyNode.propValue_vector;
				boolean valuesAreAllEmpty = true;
				for (String value : values) {
					if (value.length() > 0) {
						valuesAreAllEmpty = false;
						break;
					}
				}
				if (valuesAreAllEmpty) {
					continue;
				}

				int kind = ContactDataKind.POSTAL;
				int type = -1;
				String label = "";
				int isPrimary = 0;
				for (String typeString : propertyNode.paramMap_TYPE) {
					if (typeString.equals("PREF") && !prefIsSetAddress) {
						// Only first "PREF" is considered.
						prefIsSetAddress = true;
						isPrimary = 1;
					} else if (typeString.equalsIgnoreCase("HOME")) {
						type = Contacts.ContactMethodsColumns.TYPE_HOME;
						label = "";
					} else if (typeString.equalsIgnoreCase("WORK")
							|| typeString.equalsIgnoreCase("COMPANY")) {
						// "COMPANY" seems emitted by Windows Mobile, which is
						// not
						// specifically supported by vCard 2.1. We assume this
						// is same
						// as "WORK".
						type = Contacts.ContactMethodsColumns.TYPE_WORK;
						label = "";
					} else if (typeString.equalsIgnoreCase("POSTAL")) {
						kind = ContactDataKind.POSTAL;
					} else if (typeString.equalsIgnoreCase("PARCEL")
							|| typeString.equalsIgnoreCase("DOM")
							|| typeString.equalsIgnoreCase("INTL")) {
						// We do not have a kind or type matching these.
						// TODO: fix this. We may need to split entries into
						// two.
						// (e.g. entries for KIND_POSTAL and KIND_PERCEL)
					} else if (typeString.toUpperCase().startsWith("X-") && type < 0) {
						type = Contacts.ContactMethodsColumns.TYPE_CUSTOM;
						label = typeString.substring(2);
					} else if (type < 0) {
						// vCard 3.0 allows iana-token. Also some vCard 2.1
						// exporters
						// emit non-standard types. We do not handle their
						// values now.
						type = Contacts.ContactMethodsColumns.TYPE_CUSTOM;
						label = typeString;
					}
				}

				// adr-value = 0*6(text-value ";") text-value
				// ; PO Box, Extended Address, Street, Locality, Region, Postal
				// ; Code, Country Name
				String address;
				List<String> list = propertyNode.propValue_vector;
				int size = list.size();
				if (size > 1) {
					StringBuilder builder = new StringBuilder();
					boolean builderIsEmpty = true;
					if (Locale.getDefault().getCountry().equals(Locale.JAPAN.getCountry())) {
						// In Japan, the order is reversed.
						for (int i = size - 1; i >= 0; i--) {
							String addressPart = list.get(i);
							if (addressPart.length() > 0) {
								if (!builderIsEmpty) {
									builder.append(' ');
								}
								builder.append(addressPart);
								builderIsEmpty = false;
							}
						}
					} else {
						for (int i = 0; i < size; i++) {
							String addressPart = list.get(i);
							if (addressPart.length() > 0) {
								if (!builderIsEmpty) {
									builder.append(' ');
								}
								builder.append(addressPart);
								builderIsEmpty = false;
							}
						}
					}
					address = builder.toString().trim();
				} else {
					address = propertyNode.propValue;
				}
				contact.addContactmethod(kind, type, address, label, isPrimary);
			} else if (name.equals("ORG")) {
				// vCard specification does not specify other types.
				int type = -1;
				int isPrimary = 0;

				for (String typeString : propertyNode.paramMap_TYPE) {
					if (typeString.equals("PREF") && !prefIsSetOrganization) {
						// vCard specification officially does not have PREF in
						// ORG.
						// This is just for safety.
						prefIsSetOrganization = true;
						isPrimary = 1;
					}
					// XXX: Should we cope with X- words?
				}

				List<String> list = propertyNode.propValue_vector;
				StringBuilder builder = new StringBuilder();
				for (Iterator<String> iter = list.iterator(); iter.hasNext();) {
					builder.append(iter.next());
					if (iter.hasNext()) {
						builder.append(' ');
					}
				}

				contact.addOrganization(type, null, builder.toString(), "", isPrimary);
			} else if (name.equals("TITLE")) {
				contact.setPosition(propertyNode.propValue);
			} else if (name.equals("RINGTONE")) {
				contact.ringTone = propertyNode.propValue;
			} else if (name.equals("ROLE")) {
				contact.setPosition(propertyNode.propValue);
			} else if (name.equals("PHOTO")) {
				// We prefer PHOTO to LOGO.
				String valueType = propertyNode.paramMap.getAsString("VALUE");
				if (valueType != null && valueType.equals("URL")) {
					// TODO: do something.
				} else {
					// Assume PHOTO is stored in BASE64. In that case,
					// data is already stored in propValue_bytes in binary form.
					// It should be automatically done by VBuilder
					// (VDataBuilder/VCardDatabuilder)
					contact.photoBytes = propertyNode.propValue_bytes;
					String type = propertyNode.paramMap.getAsString("TYPE");
					if (type != null) {
						contact.photoType = type;
					}
				}
			} else if (name.equals("LOGO")) {
				// When PHOTO is not available this is not URL,
				// we use this instead of PHOTO.
				String valueType = propertyNode.paramMap.getAsString("VALUE");
				if (valueType != null && valueType.equals("URL")) {
					// TODO: do something.
				} else if (contact.photoBytes == null) {
					contact.photoBytes = propertyNode.propValue_bytes;
					String type = propertyNode.paramMap.getAsString("TYPE");
					if (type != null) {
						contact.photoType = type;
					}
				}
			} else if (name.equals("EMAIL")) {
				int type = -1;
				String label = null;
				int isPrimary = 0;
				for (String typeString : propertyNode.paramMap_TYPE) {
					if (typeString.equals("PREF") && !prefIsSetEmail) {
						// Only first "PREF" is considered.
						prefIsSetEmail = true;
						isPrimary = 1;
					} else if (typeString.equalsIgnoreCase("HOME")) {
						type = Contacts.ContactMethodsColumns.TYPE_HOME;
					} else if (typeString.equalsIgnoreCase("WORK")) {
						type = Contacts.ContactMethodsColumns.TYPE_WORK;
					} else if (typeString.equalsIgnoreCase("CELL")) {
						// We do not have
						// Contacts.ContactMethodsColumns.TYPE_MOBILE yet.
						type = Contacts.ContactMethodsColumns.TYPE_CUSTOM;
						label = MOBILE_EMAIL_TYPE_NAME;
					} else if (typeString.toUpperCase().startsWith("X-") && type < 0) {
						type = Contacts.ContactMethodsColumns.TYPE_CUSTOM;
						label = typeString.substring(2);
					} else if (type < 0) {
						// vCard 3.0 allows iana-token.
						// We may have INTERNET (specified in vCard spec),
						// SCHOOL, etc.
						type = Contacts.ContactMethodsColumns.TYPE_CUSTOM;
						label = typeString;
					}
				}
				// We use "OTHER" as default.
				if (type < 0) {
					type = Contacts.ContactMethodsColumns.TYPE_OTHER;
				}
				contact.addContactmethod(ContactDataKind.EMAIL, type, propertyNode.propValue,
						label, isPrimary);
			} else if (name.equals("TEL")) {
				int type = -1;
				String label = null;
				int isPrimary = 0;
				boolean isFax = false;
				for (String typeString : propertyNode.paramMap_TYPE) {
					if (typeString.equals("PREF") && !prefIsSetPhone) {
						// Only first "PREF" is considered.
						prefIsSetPhone = true;
						isPrimary = 1;
					} else if (typeString.equalsIgnoreCase("HOME")) {
						type = Contacts.PhonesColumns.TYPE_HOME;
					} else if (typeString.equalsIgnoreCase("WORK")) {
						type = Contacts.PhonesColumns.TYPE_WORK;
					} else if (typeString.equalsIgnoreCase("CELL")) {
						type = Contacts.PhonesColumns.TYPE_MOBILE;
					} else if (typeString.equalsIgnoreCase("PAGER")) {
						type = Contacts.PhonesColumns.TYPE_PAGER;
					} else if (typeString.equalsIgnoreCase("FAX")) {
						isFax = true;
					} else if (typeString.equalsIgnoreCase("VOICE")
							|| typeString.equalsIgnoreCase("MSG")) {
						// Defined in vCard 3.0. Ignore these because they
						// conflict with "HOME", "WORK", etc.
						// XXX: do something?
					} else if (typeString.toUpperCase().startsWith("X-") && type < 0) {
						type = Contacts.PhonesColumns.TYPE_CUSTOM;
						label = typeString.substring(2);
					} else if (type < 0) {
						// We may have MODEM, CAR, ISDN, etc...
						type = Contacts.PhonesColumns.TYPE_CUSTOM;
						label = typeString;
					}
				}
				// We use "HOME" as default
				if (type < 0) {
					type = Contacts.PhonesColumns.TYPE_HOME;
				}
				if (isFax) {
					if (type == Contacts.PhonesColumns.TYPE_HOME) {
						type = Contacts.PhonesColumns.TYPE_FAX_HOME;
					} else if (type == Contacts.PhonesColumns.TYPE_WORK) {
						type = Contacts.PhonesColumns.TYPE_FAX_WORK;
					}
				}

				contact.addPhone(type, propertyNode.propValue, label, isPrimary);
			} else if (name.equals("NOTE")) {
				contact.notes.add(propertyNode.propValue);
			} else if (name.equals("WEBSITE")) {
				int type = 1;
				for (String typeString : propertyNode.paramMap_TYPE) {
					type = Integer.parseInt(typeString);
				}
				contact.addContactmethod(ContactDataKind.WEBSITE, type, propertyNode.propValue,
						null, 1);
			} else if (name.equals("IM")) {
				int type = 1;
				for (String typeString : propertyNode.paramMap_TYPE) {
					type = Integer.parseInt(typeString);
				}
				contact.addContactmethod(ContactDataKind.IM, type, propertyNode.propValue, null, 1);
			} else if (name.equals("NICKNAME")) {
				int type = 1;
				for (String typeString : propertyNode.paramMap_TYPE) {
					type = Integer.parseInt(typeString);
				}
				contact.addContactmethod(ContactDataKind.NICKNAME, type, propertyNode.propValue,
						null, 1);
			} else if (name.equals("EVENT")) {
				int type = 1;
				for (String typeString : propertyNode.paramMap_TYPE) {
					type = Integer.parseInt(typeString);
				}
				contact.addContactmethod(ContactDataKind.EVENT, type, propertyNode.propValue, null,
						1);
			} else if (name.equals("BDAY")) {
				contact.addExtension(propertyNode);
			} else if (name.equals("URL")) {
				contact.addExtension(propertyNode);
			} else if (name.equals("REV")) {
				// Revision of this VCard entry. I think we can ignore this.
				contact.addExtension(propertyNode);
			} else if (name.equals("UID")) {
				contact.addExtension(propertyNode);
			} else if (name.equals("KEY")) {
				// Type is X509 or PGP? I don't know how to handle this...
				contact.addExtension(propertyNode);
			} else if (name.equals("MAILER")) {
				contact.addExtension(propertyNode);
			} else if (name.equals("TZ")) {
				contact.addExtension(propertyNode);
			} else if (name.equals("GEO")) {
				contact.addExtension(propertyNode);
			} else if (name.equals("NICKNAME")) {
				// vCard 3.0 only.
				contact.addExtension(propertyNode);
			} else if (name.equals("CLASS")) {
				// vCard 3.0 only.
				// e.g. CLASS:CONFIDENTIAL
				contact.addExtension(propertyNode);
			} else if (name.equals("PROFILE")) {
				// VCard 3.0 only. Must be "VCARD". I think we can ignore this.
				contact.addExtension(propertyNode);
			} else if (name.equals("CATEGORIES")) {
				// VCard 3.0 only.
				// e.g. CATEGORIES:INTERNET,IETF,INDUSTRY,INFORMATION TECHNOLOGY
				contact.addExtension(propertyNode);
			} else if (name.equals("SOURCE")) {
				// VCard 3.0 only.
				contact.addExtension(propertyNode);
			} else if (name.equals("PRODID")) {
				// VCard 3.0 only.
				// To specify the identifier for the product that created
				// the vCard object.
				contact.addExtension(propertyNode);
			} else if (name.equals("X-PHONETIC-FIRST-NAME")) {
				contact.mPhoneticGivenName = propertyNode.propValue;
			} else if (name.equals("X-PHONETIC-MIDDLE-NAME")) {
				contact.mPhoneticMiddleName = propertyNode.propValue;
			} else if (name.equals("X-PHONETIC-LAST-NAME")) {
				contact.mPhoneticFamilyName = propertyNode.propValue;
			} else {
				// Unknown X- words and IANA token.
				contact.addExtension(propertyNode);
			}
		}

		if (fullName != null) {
			contact.mName = fullName;
		} else if (nameFromNProperty != null) {
			contact.mName = nameFromNProperty;
		} else {
			contact.mName = "";
		}

		if (contact.phoneticName == null
				&& (contact.mPhoneticGivenName != null || contact.mPhoneticMiddleName != null || contact.mPhoneticFamilyName != null)) {
			// Note: In Europe, this order should be "LAST FIRST MIDDLE". See
			// the comment around
			// NAME_ORDER_TYPE_* for more detail.
			String first;
			String second;
			if (nameOrderType == NAME_ORDER_TYPE_JAPANESE) {
				first = contact.mPhoneticFamilyName;
				second = contact.mPhoneticGivenName;
			} else {
				first = contact.mPhoneticGivenName;
				second = contact.mPhoneticFamilyName;
			}
			StringBuilder builder = new StringBuilder();
			if (first != null) {
				builder.append(first);
			}
			if (contact.mPhoneticMiddleName != null) {
				builder.append(contact.mPhoneticMiddleName);
			}
			if (second != null) {
				builder.append(second);
			}
			contact.phoneticName = builder.toString();
		}

		// Remove unnecessary white spaces.
		// It is found that some mobile phone emits phonetic name with just one
		// white space
		// when a user does not specify one.
		// This logic is effective toward such kind of weird data.
		if (contact.phoneticName != null) {
			contact.phoneticName = contact.phoneticName.trim();
		}

		// If there is no "PREF", we choose the first entries as primary.
		if (!prefIsSetPhone && contact.phoneList != null && contact.phoneList.size() > 0) {
			contact.phoneList.get(0).isPrimary = 1;
		}

		if (!prefIsSetAddress && contact.contactmethodList != null) {
			for (ContactMethod contactMethod : contact.contactmethodList) {
				if (contactMethod.kind == ContactDataKind.POSTAL) {
					contactMethod.isPrimary = 1;
					break;
				}
			}
		}
		if (!prefIsSetEmail && contact.contactmethodList != null) {
			for (ContactMethod contactMethod : contact.contactmethodList) {
				if (contactMethod.kind == ContactDataKind.EMAIL) {
					contactMethod.isPrimary = 1;
					break;
				}
			}
		}
		if (!prefIsSetOrganization && contact.organizationList != null
				&& contact.organizationList.size() > 0) {
			contact.organizationList.get(0).isPrimary = 1;
		}

		// 自定义字段：头像文件ID
		List<String> photoFilesName = parseExtensionValues(contact, PHOTO_FILE_NAME);
		if (!Util.isCollectionEmpty(photoFilesName)) {
			contact.photoFileName = photoFilesName.get(0);
		}

		// 自定义字段：分组信息
		List<String> groups = parseExtensionValues(contact, GROUP);
		contact.associatedGroups = groups;

		// 自定义字段：收藏信息
		List<String> starred = parseExtensionValues(contact, STARRED);
		if (!Util.isCollectionEmpty(starred)) {
			try {
				contact.starred = Integer.parseInt(starred.get(0)) > 0;
			} catch (NumberFormatException e) {}
		}

		return contact;
	}

	public static List<String> parseExtensionValues(ContactStruct contact, String extentsionKey) {
		if (contact == null || contact.extensionMap == null) {
			return null;
		}
		List<String> values = contact.extensionMap.get(extentsionKey);
		if (Util.isCollectionEmpty(values)) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		for (String value : values) {
			PropertyNode node = PropertyNode.decode(value);
			if (node != null) {
				result.add(node.propValue);
			}
		}
		return !Util.isCollectionEmpty(result) ? result : null;
	}

	public boolean isIgnorable() {
		return TextUtils.isEmpty(mName) && TextUtils.isEmpty(phoneticName)
				&& (phoneList == null || phoneList.size() == 0)
				&& (contactmethodList == null || contactmethodList.size() == 0);
	}

	public void addContactmethod(ContactMethod im) {
		addContactmethod(im.kind, im.type, im.data, im.label, im.isPrimary);
	}

	public void addPhone(ContactMethod phone) {
		addPhone(phone.type, phone.data, phone.label, phone.isPrimary);
	}

	public void addOrganization(OrganizationData organization) {
		addOrganization(organization.type, organization.label, organization.companyName,
				organization.positionName, organization.isPrimary);
	}

	@Override
	public int hashCode() {
		int hashCode = 17;
		hashCode = 31 * hashCode + (TextUtils.isEmpty(mName) ? 0 : mName.hashCode());
//		hashCode = 31 * hashCode + (TextUtils.isEmpty(mGivenName) ? 0 : mGivenName.hashCode());
//		hashCode = 31 * hashCode + (TextUtils.isEmpty(mFamilyName) ? 0 : mFamilyName.hashCode());
//		hashCode = 31 * hashCode + (TextUtils.isEmpty(mMiddleName) ? 0 : mMiddleName.hashCode());
		// 此处不能使用ArrayList自已的hashCode计算方法，因为ArrayList自己的计算方法会跟内部元素的排放顺序相关，得出来的结果不同
		hashCode = 31 * hashCode
				+ (phoneList != null ? Util.calcCollectionsHashCode(phoneList) : 0);
		hashCode = 31 * hashCode
				+ (contactmethodList != null ? Util.calcCollectionsHashCode(contactmethodList) : 0);
		hashCode = 31 * hashCode
				+ (organizationList != null ? Util.calcCollectionsHashCode(organizationList) : 0);
//		hashCode = 31 * hashCode
//				+ (associatedGroups != null ? Util.calcCollectionsHashCode(associatedGroups) : 0);
//	//不知道这个starred=ture,定义为何值
//		hashCode = 31 * hashCode
//				+ (starred ? 1 : 0);
		// TODO 可能还需判断其他字段
		LogUtil.d(Integer.toString(hashCode));
		return hashCode;
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof ContactStruct)) {
			return false;
		}

		// 名字必须相同
		final ContactStruct other = (ContactStruct) object;
		if (TextUtils.isEmpty(mName) ? !TextUtils.isEmpty(other.mName) : !mName.equals(other.mName)) {
			return false;
		}
//		if (TextUtils.isEmpty(mGivenName) ? !TextUtils.isEmpty(other.mGivenName) : !mGivenName
//				.equals(other.mGivenName)) {
//			return false;
//		}
//		if (TextUtils.isEmpty(mFamilyName) ? !TextUtils.isEmpty(other.mFamilyName) : !mFamilyName
//				.equals(other.mGivenName)) {
//			return false;
//		}
//		if (TextUtils.isEmpty(mMiddleName) ? !TextUtils.isEmpty(other.mMiddleName) : !mMiddleName
//				.equals(other.mMiddleName)) {
//			return false;
//		}

//		//收藏必须相同
//		final boolean otherStarred = other.starred;
//		if (starred == false ? ! otherStarred : ! starred==otherStarred) {
//			return false;
//		}
		// 电话号码必须相同
		final List<PhoneData> otherPhoneList = other.phoneList;
		if (phoneList == null ? otherPhoneList != null : otherPhoneList == null) {
			return false;
		}
		if (phoneList != null && otherPhoneList != null) {
			final int otherPhoneDataCount = otherPhoneList.size();
			final int thisPhoneDataCount = phoneList.size();
			if (thisPhoneDataCount != otherPhoneDataCount) {
				return false;
			}
			/*
			 * Map<String, PhoneData> phoneDataMap = new HashMap<String,
			 * PhoneData>(phoneList.size()); for (PhoneData phone : phoneList) {
			 * if (phone.data != null) { phoneDataMap.put(phone.data, phone); }
			 * }
			 */
			Set<PhoneData> phoneDataSet = new HashSet<PhoneData>(phoneList);
			for (int i = 0; i < otherPhoneDataCount; i++) {
				/*
				 * if (!phoneDataMap.containsKey(otherPhoneList.get(i).data)) {
				 * return false; }
				 */
				final PhoneData phone = otherPhoneList.get(i);
				if (!phoneDataSet.contains(phone)) {
					return false;
				}
			}
			// phoneDataMap.clear();
			phoneDataSet.clear();
		}
		// 联系方式相同
		final List<ContactMethod> otherContactMethods = other.contactmethodList;
		if (contactmethodList == null ? otherContactMethods != null : otherContactMethods == null) {
			return false;
		}
		if (contactmethodList != null && otherContactMethods != null) {
			final int otherContactMethodsCount = otherContactMethods.size();
			final int thisContactMethodsCount = contactmethodList.size();
			if (thisContactMethodsCount != otherContactMethodsCount) {
				return false;
			}
			/*
			 * Map<String, ContactMethod> contactMethodMap = new HashMap<String,
			 * ContactMethod>(thisContactMethodsCount); for (ContactMethod
			 * method : contactmethodList) { if (method.data != null) {
			 * contactMethodMap.put(method.data, method); } }
			 */
			Set<ContactMethod> contactsMethodSet = new HashSet<ContactMethod>(contactmethodList);
			for (int i = 0; i < otherContactMethodsCount; i++) {
				final ContactMethod otherMethod = otherContactMethods.get(i);
				/*
				 * ContactMethod thisMethod =
				 * contactMethodMap.get(otherMethod.data); if (thisMethod ==
				 * null || thisMethod.kind != otherMethod.kind) { return false;
				 * }
				 */
				if (!contactsMethodSet.contains(otherMethod)) {
					return false;
				}
			}
			// contactMethodMap.clear();
			contactsMethodSet.clear();
		}
		// 组织关系相同
		final List<OrganizationData> otherOrganizations = other.organizationList;
		if (organizationList == null ? otherOrganizations != null : otherOrganizations == null) {
			return false;
		}
		if (organizationList != null && otherOrganizations != null) {
			final int otherOrganizationsCount = otherOrganizations.size();
			final int thisOrganizationsCount = organizationList.size();
			if (thisOrganizationsCount != otherOrganizationsCount) {
				return false;
			}
			/*
			 * Map<Integer, OrganizationData> organizationsMap = new
			 * HashMap<Integer, OrganizationData>(organizationList.size()); for
			 * (OrganizationData organization : organizationList) { if
			 * (organization.companyName == null && organization.positionName ==
			 * null) { continue; } int hashCode = organization.companyName ==
			 * null ? 0 : organization.companyName.hashCode(); hashCode +=
			 * (organization.positionName == null ? 0 :
			 * organization.positionName.hashCode());
			 * organizationsMap.put(hashCode, organization); }
			 */
			Set<OrganizationData> organizationSet = new HashSet<OrganizationData>(organizationList);
			for (int i = 0; i < otherOrganizationsCount; i++) {
				/*
				 * final OrganizationData otherOrganization =
				 * otherOrganizations.get(i); int hashCode =
				 * otherOrganization.companyName == null ? 0 :
				 * otherOrganization.companyName.hashCode(); hashCode +=
				 * (otherOrganization.positionName == null ? 0 :
				 * otherOrganization.positionName.hashCode()); if
				 * (!organizationsMap.containsKey(hashCode)) { return false; }
				 */
				final OrganizationData org = otherOrganizations.get(i);
				if (!organizationSet.contains(org)) {
					return false;
				}
			}
			// organizationsMap.clear();
			organizationSet.clear();
		}

//		//分组相同
//				final List<String> otherAssociatedGroups = other.associatedGroups;
//				if (associatedGroups == null ? otherAssociatedGroups != null : otherAssociatedGroups == null) {
//					return false;
//				}
//				if (associatedGroups != null && otherAssociatedGroups != null) {
//					final int otherAssociatedGroupsCount = otherAssociatedGroups.size();
//					final int thisAssociatedGroupsCount = associatedGroups.size();
//					if (thisAssociatedGroupsCount != otherAssociatedGroupsCount) {
//						return false;
//					}
//					/*
//					 * Map<Integer, OrganizationData> organizationsMap = new
//					 * HashMap<Integer, OrganizationData>(organizationList.size()); for
//					 * (OrganizationData organization : organizationList) { if
//					 * (organization.companyName == null && organization.positionName ==
//					 * null) { continue; } int hashCode = organization.companyName ==
//					 * null ? 0 : organization.companyName.hashCode(); hashCode +=
//					 * (organization.positionName == null ? 0 :
//					 * organization.positionName.hashCode());
//					 * organizationsMap.put(hashCode, organization); }
//					 */
//					Set<String> associatedGroupSet = new HashSet<String>(associatedGroups);
//					for (int i = 0; i < otherAssociatedGroupsCount; i++) {
//						/*
//						 * final OrganizationData otherOrganization =
//						 * otherOrganizations.get(i); int hashCode =
//						 * otherOrganization.companyName == null ? 0 :
//						 * otherOrganization.companyName.hashCode(); hashCode +=
//						 * (otherOrganization.positionName == null ? 0 :
//						 * otherOrganization.positionName.hashCode()); if
//						 * (!organizationsMap.containsKey(hashCode)) { return false; }
//						 */
//						final String org = otherAssociatedGroups.get(i);
//						if (!associatedGroupSet.contains(org)) {
//							return false;
//						}
//					}
//					// organizationsMap.clear();
//					associatedGroupSet.clear();
//				}

		return true;
	}

	public void addGroup(String groupTitle) {
		if (TextUtils.isEmpty(groupTitle)) {
			return;
		}
		if (associatedGroups == null) {
			associatedGroups = new ArrayList<String>();
		}
		associatedGroups.add(groupTitle);
	}

	public void clearPhotoData() {
		photoBytes = null;
		photoType = null;
	}
}

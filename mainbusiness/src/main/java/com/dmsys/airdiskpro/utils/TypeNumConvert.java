package com.dmsys.airdiskpro.utils;

import com.dmsys.airdiskpro.model.TypeNum;


public class TypeNumConvert {

	public static int Organization_toInt(String typeString) {
		int type = TypeNum.TYPE_Organization_OTHER;
		if (typeString.equalsIgnoreCase(TypeNum.String_Organization_WORK)) {
			type = TypeNum.TYPE_Organization_WORK;
		} else if (typeString
				.equalsIgnoreCase(TypeNum.String_Organization_OTHER)) {
			type = TypeNum.TYPE_Organization_OTHER;
		}
		return type;
	}

	public static String Organization_toString(int type) {
		String typeStr = TypeNum.String_Organization_OTHER;
		if (type == TypeNum.TYPE_Organization_WORK) {
			typeStr = TypeNum.String_Organization_WORK;
		} else if (type == TypeNum.TYPE_Organization_OTHER) {
			typeStr = TypeNum.String_Organization_OTHER;
		}
		return typeStr;
	}

	public static int Event_toInt(String typeString) {
		int type = TypeNum.TYPE_Event_OTHER;
		if (typeString.equalsIgnoreCase(TypeNum.String_Event_ANNIVERSARY)) {
			type = TypeNum.TYPE_Event_ANNIVERSARY;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_Event_OTHER)) {
			type = TypeNum.TYPE_Event_OTHER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_Event_BIRTHDAY)) {
			type = TypeNum.TYPE_Event_BIRTHDAY;
		}
		return type;
	}

	public static String Event_toString(int type) {
		String typeStr = TypeNum.String_Event_OTHER;
		if (type == TypeNum.TYPE_Event_ANNIVERSARY) {
			typeStr = TypeNum.String_Event_ANNIVERSARY;
		} else if (type == TypeNum.TYPE_Event_OTHER) {
			typeStr = TypeNum.String_Event_OTHER;
		} else if (type == TypeNum.TYPE_Event_BIRTHDAY) {
			typeStr = TypeNum.String_Event_BIRTHDAY;
		}
		return typeStr;
	}

	public static int IM_toInt(String typeString) {
		int type = TypeNum.TYPE_IM_AIM;
		if (typeString.equalsIgnoreCase(TypeNum.String_IM_AIM)) {
			type = TypeNum.TYPE_IM_AIM;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_IM_GOOGLE_TALK)) {
			type = TypeNum.TYPE_IM_GOOGLE_TALK;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_IM_ICQ)) {
			type = TypeNum.TYPE_IM_ICQ;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_IM_JABBER)) {
			type = TypeNum.TYPE_IM_JABBER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_IM_MSN)) {
			type = TypeNum.TYPE_IM_MSN;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_IM_NETMEETING)) {
			type = TypeNum.TYPE_IM_NETMEETING;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_IM_QQ)) {
			type = TypeNum.TYPE_IM_QQ;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_IM_SKYPE)) {
			type = TypeNum.TYPE_IM_SKYPE;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_IM_YAHOO)) {
			type = TypeNum.TYPE_IM_YAHOO;
		}
		return type;
	}

	public static String IM_toString(int type) {
		String typeStr = TypeNum.String_IM_AIM;
		if (type == TypeNum.TYPE_IM_AIM) {
			typeStr = TypeNum.String_IM_AIM;
		} else if (type == TypeNum.TYPE_IM_GOOGLE_TALK) {
			typeStr = TypeNum.String_IM_GOOGLE_TALK;
		} else if (type == TypeNum.TYPE_IM_ICQ) {
			typeStr = TypeNum.String_IM_ICQ;
		} else if (type == TypeNum.TYPE_IM_JABBER) {
			typeStr = TypeNum.String_IM_JABBER;
		} else if (type == TypeNum.TYPE_IM_MSN) {
			typeStr = TypeNum.String_IM_MSN;
		} else if (type == TypeNum.TYPE_IM_NETMEETING) {
			typeStr = TypeNum.String_IM_NETMEETING;
		} else if (type == TypeNum.TYPE_IM_QQ) {
			typeStr = TypeNum.String_IM_QQ;
		} else if (type == TypeNum.TYPE_IM_SKYPE) {
			typeStr = TypeNum.String_IM_SKYPE;
		} else if (type == TypeNum.TYPE_IM_YAHOO) {
			typeStr = TypeNum.String_IM_YAHOO;
		}
		return typeStr;
	}

	public static int Address_toInt(String typeString) {
		int type = TypeNum.TYPE_ADDR_OTHER;
		if (typeString.equalsIgnoreCase(TypeNum.String_ADDR_HOME)) {
			type = TypeNum.TYPE_ADDR_HOME;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_ADDR_OTHER)) {
			type = TypeNum.TYPE_ADDR_OTHER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_ADDR_WORK)) {
			type = TypeNum.TYPE_ADDR_WORK;
		}
		return type;
	}

	public static String Address_toString(int type) {
		String typeStr = TypeNum.String_ADDR_OTHER;
		if (type == TypeNum.TYPE_ADDR_HOME) {
			typeStr = TypeNum.String_ADDR_HOME;
		} else if (type == TypeNum.TYPE_ADDR_OTHER) {
			typeStr = TypeNum.String_ADDR_OTHER;
		} else if (type == TypeNum.TYPE_ADDR_WORK) {
			typeStr = TypeNum.String_ADDR_WORK;
		}
		return typeStr;
	}

	public static int TEL_toInt(String typeString) {
		int type = TypeNum.TYPE_TEL_OTHER;
		if (typeString.equalsIgnoreCase(TypeNum.String_TEL_HOME)) {
			type = TypeNum.TYPE_TEL_HOME;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_WORK)) {
			type = TypeNum.TYPE_TEL_WORK;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_MOBILE)) {
			type = TypeNum.TYPE_TEL_MOBILE;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_MAIN)) {
			type = TypeNum.TYPE_TEL_MAIN;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_FAX_HOME)) {
			type = TypeNum.TYPE_TEL_FAX_HOME;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_FAX_WORK)) {
			type = TypeNum.TYPE_TEL_FAX_WORK;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_PAGER)) {
			type = TypeNum.TYPE_TEL_PAGER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_OTHER)) {
			type = TypeNum.TYPE_TEL_OTHER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_CALLBACK)) {
			type = TypeNum.TYPE_TEL_CALLBACK;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_CAR)) {
			type = TypeNum.TYPE_TEL_CAR;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_COMPANY_MAIN)) {
			type = TypeNum.TYPE_TEL_COMPANY_MAIN;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_ISDN)) {
			type = TypeNum.TYPE_TEL_ISDN;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_OTHER_FAX)) {
			type = TypeNum.TYPE_TEL_OTHER_FAX;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_RADIO)) {
			type = TypeNum.TYPE_TEL_RADIO;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_TELEX)) {
			type = TypeNum.TYPE_TEL_TELEX;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_TTY_TDD)) {
			type = TypeNum.TYPE_TEL_TTY_TDD;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_WORK_MOBILE)) {
			type = TypeNum.TYPE_TEL_WORK_MOBILE;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_WORK_PAGER)) {
			type = TypeNum.TYPE_TEL_WORK_PAGER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_ASSISTANT)) {
			type = TypeNum.TYPE_TEL_ASSISTANT;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_TEL_MMS)) {
			type = TypeNum.TYPE_TEL_MMS;
		}
		return type;
	}

	public static String TEL_toString(int type) {
		String typeStr = TypeNum.String_TEL_OTHER;
		if (type == TypeNum.TYPE_TEL_HOME) {
			typeStr = TypeNum.String_TEL_HOME;
		} else if (type == TypeNum.TYPE_TEL_WORK) {
			typeStr = TypeNum.String_TEL_WORK;
		} else if (type == TypeNum.TYPE_TEL_MOBILE) {
			typeStr = TypeNum.String_TEL_MOBILE;
		} else if (type == TypeNum.TYPE_TEL_MAIN) {
			typeStr = TypeNum.String_TEL_MAIN;
		} else if (type == TypeNum.TYPE_TEL_FAX_HOME) {
			typeStr = TypeNum.String_TEL_FAX_HOME;
		} else if (type == TypeNum.TYPE_TEL_FAX_WORK) {
			typeStr = TypeNum.String_TEL_FAX_WORK;
		} else if (type == TypeNum.TYPE_TEL_PAGER) {
			typeStr = TypeNum.String_TEL_PAGER;
		} else if (type == TypeNum.TYPE_TEL_OTHER) {
			typeStr = TypeNum.String_TEL_OTHER;
		} else if (type == TypeNum.TYPE_TEL_CALLBACK) {
			typeStr = TypeNum.String_TEL_CALLBACK;
		} else if (type == TypeNum.TYPE_TEL_CAR) {
			typeStr = TypeNum.String_TEL_CAR;
		} else if (type == TypeNum.TYPE_TEL_COMPANY_MAIN) {
			typeStr = TypeNum.String_TEL_COMPANY_MAIN;
		} else if (type == TypeNum.TYPE_TEL_ISDN) {
			typeStr = TypeNum.String_TEL_ISDN;
		} else if (type == TypeNum.TYPE_TEL_OTHER_FAX) {
			typeStr = TypeNum.String_TEL_OTHER_FAX;
		} else if (type == TypeNum.TYPE_TEL_RADIO) {
			typeStr = TypeNum.String_TEL_RADIO;
		} else if (type == TypeNum.TYPE_TEL_TELEX) {
			typeStr = TypeNum.String_TEL_TELEX;
		} else if (type == TypeNum.TYPE_TEL_TTY_TDD) {
			typeStr = TypeNum.String_TEL_TTY_TDD;
		} else if (type == TypeNum.TYPE_TEL_WORK_MOBILE) {
			typeStr = TypeNum.String_TEL_WORK_MOBILE;
		} else if (type == TypeNum.TYPE_TEL_WORK_PAGER) {
			typeStr = TypeNum.String_TEL_WORK_PAGER;
		} else if (type == TypeNum.TYPE_TEL_ASSISTANT) {
			typeStr = TypeNum.String_TEL_ASSISTANT;
		} else if (type == TypeNum.TYPE_TEL_MMS) {
			typeStr = TypeNum.String_TEL_MMS;
		}

		return typeStr;
	}

	public static int Email_toInt(String typeString) {
		int type = TypeNum.TYPE_EMAIL_OTHER;
		if (typeString.equalsIgnoreCase(TypeNum.String_EMAIL_HOME)) {
			type = TypeNum.TYPE_EMAIL_HOME;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_EMAIL_OTHER)) {
			type = TypeNum.TYPE_EMAIL_OTHER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_EMAIL_WORK)) {
			type = TypeNum.TYPE_EMAIL_WORK;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_EMAIL_MOBILE)) {
			type = TypeNum.TYPE_EMAIL_MOBILE;
		}
		return type;
	}

	public static String Email_toString(int type) {
		String typeStr = TypeNum.String_EMAIL_OTHER;
		if (type == TypeNum.TYPE_EMAIL_HOME) {
			typeStr = TypeNum.String_EMAIL_HOME;
		} else if (type == TypeNum.TYPE_EMAIL_OTHER) {
			typeStr = TypeNum.String_EMAIL_OTHER;
		} else if (type == TypeNum.TYPE_EMAIL_WORK) {
			typeStr = TypeNum.String_EMAIL_WORK;
		} else if (type == TypeNum.TYPE_EMAIL_MOBILE) {
			typeStr = TypeNum.String_EMAIL_MOBILE;
		}
		return typeStr;
	}

	public static int URL_toInt(String typeString) {
		int type = TypeNum.TYPE_URL_OTHER;
		if (typeString.equalsIgnoreCase(TypeNum.String_URL_HOMEPAGE)) {
			type = TypeNum.TYPE_URL_HOMEPAGE;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_URL_HOME)) {
			type = TypeNum.TYPE_URL_HOME;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_URL_WORK)) {
			type = TypeNum.TYPE_URL_WORK;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_URL_OTHER)) {
			type = TypeNum.TYPE_URL_OTHER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_URL_BLOG)) {
			type = TypeNum.TYPE_URL_BLOG;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_URL_PROFILE)) {
			type = TypeNum.TYPE_URL_PROFILE;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_URL_FTP)) {
			type = TypeNum.TYPE_URL_FTP;
		}
		return type;
	}

	public static String URL_toString(int type) {
		String typeStr = TypeNum.String_URL_OTHER;
		if (type == TypeNum.TYPE_URL_HOMEPAGE) {
			typeStr = TypeNum.String_URL_HOMEPAGE;
		} else if (type == TypeNum.TYPE_URL_HOME) {
			typeStr = TypeNum.String_URL_HOME;
		} else if (type == TypeNum.TYPE_URL_WORK) {
			typeStr = TypeNum.String_URL_WORK;
		} else if (type == TypeNum.TYPE_URL_OTHER) {
			typeStr = TypeNum.String_URL_OTHER;
		} else if (type == TypeNum.TYPE_URL_BLOG) {
			typeStr = TypeNum.String_URL_BLOG;
		} else if (type == TypeNum.TYPE_URL_PROFILE) {
			typeStr = TypeNum.String_URL_PROFILE;
		} else if (type == TypeNum.TYPE_URL_FTP) {
			typeStr = TypeNum.String_URL_FTP;
		}
		return typeStr;
	}

	public static int NickName_toInt(String typeString) {
		int type = TypeNum.TYPE_NICKNAME_DEFAULT;
		if (typeString.equalsIgnoreCase(TypeNum.String_NICKNAME_DEFAULT)) {
			type = TypeNum.TYPE_NICKNAME_DEFAULT;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_NICKNAME_OTHER_NAME)) {
			type = TypeNum.TYPE_NICKNAME_OTHER_NAME;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_NICKNAME_MAIDEN_NAME)) {
			type = TypeNum.TYPE_NICKNAME_MAIDEN_NAME;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_NICKNAME_SHORT_NAME)) {
			type = TypeNum.TYPE_NICKNAME_SHORT_NAME;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_NICKNAME_INITIALS)) {
			type = TypeNum.TYPE_NICKNAME_INITIALS;
		}
		return type;
	}

	public static String NickName_toString(int type) {
		String typeStr = TypeNum.String_NICKNAME_DEFAULT;
		if (type == TypeNum.TYPE_NICKNAME_DEFAULT) {
			typeStr = TypeNum.String_NICKNAME_DEFAULT;
		} else if (type == TypeNum.TYPE_NICKNAME_OTHER_NAME) {
			typeStr = TypeNum.String_NICKNAME_OTHER_NAME;
		} else if (type == TypeNum.TYPE_NICKNAME_MAIDEN_NAME) {
			typeStr = TypeNum.String_NICKNAME_MAIDEN_NAME;
		} else if (type == TypeNum.TYPE_NICKNAME_SHORT_NAME) {
			typeStr = TypeNum.String_NICKNAME_SHORT_NAME;
		} else if (type == TypeNum.TYPE_NICKNAME_INITIALS) {
			typeStr = TypeNum.String_NICKNAME_INITIALS;
		}
		return typeStr;
	}

	public static int Relation_toInt(String typeString) {
		int type = TypeNum.TYPE_RELATION_RELATIVE;
		if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_MOTHER)) {
			type = TypeNum.TYPE_RELATION_MOTHER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_FATHER)) {
			type = TypeNum.TYPE_RELATION_FATHER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_PARENT)) {
			type = TypeNum.TYPE_RELATION_PARENT;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_BROTHER)) {
			type = TypeNum.TYPE_RELATION_BROTHER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_SISTER)) {
			type = TypeNum.TYPE_RELATION_SISTER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_CHILD)) {
			type = TypeNum.TYPE_RELATION_CHILD;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_FRIEND)) {
			type = TypeNum.TYPE_RELATION_FRIEND;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_SPOUSE)) {
			type = TypeNum.TYPE_RELATION_SPOUSE;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_PARTNER)) {
			type = TypeNum.TYPE_RELATION_PARTNER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_ASSISTANT)) {
			type = TypeNum.TYPE_RELATION_ASSISTANT;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_MANAGER)) {
			type = TypeNum.TYPE_RELATION_MANAGER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_DOMESTIC_PARTNER)) {
			type = TypeNum.TYPE_RELATION_DOMESTIC_PARTNER;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_REFERRED_BY)) {
			type = TypeNum.TYPE_RELATION_REFERRED_BY;
		} else if (typeString.equalsIgnoreCase(TypeNum.String_RELATION_RELATIVE)) {
			type = TypeNum.TYPE_RELATION_RELATIVE;
		}
		return type;
	}

	public static String Relation_toString(int type) {
		String typeStr = TypeNum.String_RELATION_RELATIVE;
		if (type == TypeNum.TYPE_RELATION_MOTHER) {
			typeStr = TypeNum.String_RELATION_MOTHER;
		} else if (type == TypeNum.TYPE_RELATION_FATHER) {
			typeStr = TypeNum.String_RELATION_FATHER;
		} else if (type == TypeNum.TYPE_RELATION_PARENT) {
			typeStr = TypeNum.String_RELATION_PARENT;
		} else if (type == TypeNum.TYPE_RELATION_BROTHER) {
			typeStr = TypeNum.String_RELATION_BROTHER;
		} else if (type == TypeNum.TYPE_RELATION_SISTER) {
			typeStr = TypeNum.String_RELATION_SISTER;
		} else if (type == TypeNum.TYPE_RELATION_CHILD) {
			typeStr = TypeNum.String_RELATION_CHILD;
		} else if (type == TypeNum.TYPE_RELATION_FRIEND) {
			typeStr = TypeNum.String_RELATION_FRIEND;
		} else if (type == TypeNum.TYPE_RELATION_SPOUSE) {
			typeStr = TypeNum.String_RELATION_SPOUSE;
		} else if (type == TypeNum.TYPE_RELATION_PARTNER) {
			typeStr = TypeNum.String_RELATION_PARTNER;
		} else if (type == TypeNum.TYPE_RELATION_ASSISTANT) {
			typeStr = TypeNum.String_RELATION_ASSISTANT;
		} else if (type == TypeNum.TYPE_RELATION_MANAGER) {
			typeStr = TypeNum.String_RELATION_MANAGER;
		} else if (type == TypeNum.TYPE_RELATION_DOMESTIC_PARTNER) {
			typeStr = TypeNum.String_RELATION_DOMESTIC_PARTNER;
		} else if (type == TypeNum.TYPE_RELATION_REFERRED_BY) {
			typeStr = TypeNum.String_RELATION_REFERRED_BY;
		} else if (type == TypeNum.TYPE_RELATION_RELATIVE) {
			typeStr = TypeNum.String_RELATION_RELATIVE;
		}
		return typeStr;
	}
	public static int SipAddress_toInt(String typeString) {
		int type = TypeNum.TYPE_SipAddress_OTHER;
		if (typeString.equalsIgnoreCase(TypeNum.String_SipAddress_HOME)) {
			type = TypeNum.TYPE_SipAddress_HOME;
		} else if (typeString
				.equalsIgnoreCase(TypeNum.String_SipAddress_WORK)) {
			type = TypeNum.TYPE_SipAddress_WORK;
		} else if (typeString
				.equalsIgnoreCase(TypeNum.String_SipAddress_OTHER)) {
			type = TypeNum.TYPE_SipAddress_OTHER;
		}
		return type;
	}

	public static String SipAddress_toString(int type) {
		String typeStr = TypeNum.String_SipAddress_OTHER;
		if (type == TypeNum.TYPE_SipAddress_HOME) {
			typeStr = TypeNum.String_SipAddress_HOME;
		} else if (type == TypeNum.TYPE_SipAddress_WORK) {
			typeStr = TypeNum.String_SipAddress_WORK;
		} else if (type == TypeNum.TYPE_SipAddress_OTHER) {
			typeStr = TypeNum.String_SipAddress_OTHER;
		}
		return typeStr;
	}
}

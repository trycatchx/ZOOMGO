package com.dmsys.airdiskpro.model;

/**
 * 
 * @ClassName: MimetypeData
 * @Description: 系统数据库中各mimetype对应的data数据
 * @author: Alan.wang
 * @date: 2014-09-29 10:29 
 * version:1.0.0
 */
public class MimetypeData {
	
	//mimetype对应的值
	public static final int MIMETYPE_CPOTHER   	    = 1  ; //1 |vnd.android.cursor.item/cpother
	public static final int MIMETYPE_EMAIL   		= 2  ; //2 |vnd.android.cursor.item/email_v2
	public static final int MIMETYPE_IM      		= 3  ; //3 |vnd.android.cursor.item/im
	public static final int MIMETYPE_POSTAL_ADDRESS = 4  ; //4 |vnd.android.cursor.item/postal-address_v2
	public static final int MIMETYPE_PHONE   		= 5  ; //5 |vnd.android.cursor.item/phone_v2
	public static final int MIMETYPE_NAME    		= 6  ; //6 |vnd.android.cursor.item/name
	public static final int MIMETYPE_PHOTO   		= 7  ; //7 |vnd.android.cursor.item/photo
	public static final int MIMETYPE_GROUP    	    = 8  ; //8 |vnd.android.cursor.item/group_membership
	public static final int MIMETYPE_ORGANIZATION   = 9  ; //9 |vnd.android.cursor.item/organization
	public static final int MIMETYPE_NICKNAME    	= 10 ; //10|vnd.android.cursor.item/nickname
	public static final int MIMETYPE_NOTE    	    = 11 ; //11|vnd.android.cursor.item/note
	public static final int MIMETYPE_SNS    	    = 12 ; //12|vnd.android.cursor.item/sns
	public static final int MIMETYPE_SIP_ADDRESS    = 13 ; //13|vnd.android.cursor.item/sip-address
	public static final int MIMETYPE_IDENTITY    	= 14 ; //14|vnd.android.cursor.item/identity
	public static final int MIMETYPE_CONTACT_EVENT  = 15 ; //15|vnd.android.cursor.item/contact-event
	public static final int MIMETYPE_WEBSITE    	= 16 ; //16|vnd.android.cursor.item/website
	public static final int MIMETYPE_RELATION    	= 17 ; //17|vnd.android.cursor.item/relation

	//1:Cpother对应的data数据(无)
	
	//2:Emial对应的data数据，data1,data2,data3
	public static final int EMAIL_ADDRESS = 1;
	public static final int EMAIL_TYPE = 2;
	public static final int EMAIL_LABLE = 3;
	
	//3:Im对应的data数据
	public static final int IM_DATA = 1;
	public static final int IM_TYPE = 2;
	public static final int IM_LABLE = 3;
	public static final int IM_PROTOCAL = 4;
	public static final int IM_CUSTOM_PROTOCAL = 5;
	
	//4:Postal_address对应的data数据
	public static final int POST_ADDR_FORMATTED_ADDRESS = 1;
	public static final int POST_ADDR_TYPE = 2;
	public static final int POST_ADDR_LABLE = 3;
	public static final int POST_ADDR_STREET = 4;
	public static final int POST_ADDR_POBOX = 5;
	public static final int POST_ADDR_NEIGHBORHOOD = 6;
	public static final int POST_ADDR_CITY = 7;
	public static final int POST_ADDR_REGION = 8;
	public static final int POST_ADDR_POSTCODE = 9;
	public static final int POST_ADDR_COUNTRY = 10;
	
	//5:Phone对应的data数据
	public static final int PHONE_NUMBER = 1;
	public static final int PHONE_TYPE = 2;
	public static final int PHONE_LABLE = 3;
	
	//6:Name对应的data数据
	public static final int NAME_DISPLAY_NAME = 1;
	public static final int NAME_GIVEN_NAME = 2;
	public static final int NAME_FAMILY_NAME = 3;
	public static final int NAME_PREFIX = 4;
	public static final int NAME_MIDDLE_NAME = 5;
	public static final int NAME_SUFFIX = 6;
	public static final int NAME_PHONETIC_GIVEN_NAME = 7;
	public static final int NAME_PHONETIC_MIDDLE_NAME = 8;
	public static final int NAME_PHONETIC_FAMILY_NAME = 9;
	
	//7:Photo对应的data数据
	public static final int PHOTO_DIY = 13;//data中没有的，自定义的，Info中没有下面两个ID
	public static final int PHOTO_FILE_ID = 14;
	public static final int PHOTO = 15;
	
	//8:Group对应的data数据
	public static final int GROUP_ROW_ID = 1;
	public static final int GROUP_SOURCE_ID = 2;
	public static final int GROUP_DIY = 13;//data中没有的，自定义的，Info中没有上面两个ID
	
	//9:Organization对应的data数据
	public static final int ORGANIZATION_COMPANY = 1;
	public static final int ORGANIZATION_TYPE = 2;
	public static final int ORGANIZATION_LABLE = 3;
	public static final int ORGANIZATION_TITLE = 4;
	public static final int ORGANIZATION_DEPARTMENT = 5;
	public static final int ORGANIZATION_JOB_DESCRIPTION = 6;
	public static final int ORGANIZATION_SYMBOL = 7;
	public static final int ORGANIZATION_PHONETIC_NAME = 8;
	public static final int ORGANIZATION_OFFICE_LOCATION = 9;
	public static final int ORGANIZATION_PHONETIC_NAME_STYLE = 10;
	
	//10:Nickname对应的data数据
	public static final int NICKNAME_NAME = 1;
	public static final int NICKNAME_TYPE = 2;
	public static final int NICKNAME_LABLE = 3;
	
	//11:Note对应的data数据
	public static final int NOTO = 1;
	
	//12:SNS对应的data数据(无)
	
	//13:Sip_address对应的data数据
	public static final int SIP_ADDR_NAME = 1;
	public static final int SIP_ADDR_TYPE = 2;
	public static final int SIP_ADDR_LABLE = 3;
	
	//14:Identity对应的data数据
	public static final int IDENTITY_LABEL = 1;
	public static final int IDENTITY_NAMESPACE = 2;
	
	//15:Event对应的data数据
	public static final int EVENT_START_DATE = 1;
	public static final int EVENT_TYPE = 2;
	public static final int EVENT_LABLE = 3;
	
	//16:Website对应的data数据
	public static final int WEBSITE_URL = 1;
	public static final int WEBSITE_TYPE = 2;
	public static final int WEBSITE_LABLE = 3;
	
	//17:Relation对应的data数据
	public static final int RELATION_NAME = 1;
	public static final int RELATION_TYPE = 2;
	public static final int RELATION_LABLE = 3;
}

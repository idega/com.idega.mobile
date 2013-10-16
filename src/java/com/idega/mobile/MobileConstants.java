package com.idega.mobile;

public class MobileConstants {

	private MobileConstants() {}

	public static final String	IW_BUNDLE_IDENTIFIER = "com.idega.mobile",

								URI = "/mobile",
								URI_LOGIN = "/login",
								URI_BANK_ID_LOGIN = URI_LOGIN + "/bankid",
								URI_LOGOUT = "/logout",
								URI_PING = "/ping",
								URI_GET_REPOSITORY_ITEM = "/repository/item",
								URI_NOTIFICATION = "/notification",
								URI_SUBSCRIBE = "/subscribe",

								DEVICE_IOS = "iOS",
								DEVICE_ANDROID = "Android",

								NOTIFY_ON_ALL = "all",

								PARAM_TOKEN = "token",
								PARAM_MSG = "msg",
								PARAM_LOCALE = "locale",
								PARAM_COUNTRY = "country",
								PARAM_URL = "url",
								PARAM_USER_ID = "userId",
								PARAM_NOTIFY_ON = "notifyOn",
								PARAM_PERSONAL_ID = "personalId",

								LOGIN_TYPE_FACEBOOK = "facebook",
								LOGIN_TYPE_BANK = "bank";
}
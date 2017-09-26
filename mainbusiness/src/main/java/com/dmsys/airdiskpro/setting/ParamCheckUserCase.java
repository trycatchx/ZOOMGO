package com.dmsys.airdiskpro.setting;

import android.text.TextUtils;

import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMOpenVault;

/**
 * 
 * @author jiong103 检验密码是否符合规格
 *
 */
public class ParamCheckUserCase {

	protected static final int NORMAL_CODE = 0;
	protected static final int ERROR_PASSWORD_UNMATCHED = NORMAL_CODE + 1;
	protected static final int ERROR_PASSWORD_TOOSHORT = NORMAL_CODE + 2;
	protected static final int ERROR_PASSWORD_EMPTY = NORMAL_CODE + 3;
	protected static final int ERROR_PATAMATER = NORMAL_CODE + 4;
	protected static final int ERROR_NOT_CHANGED = NORMAL_CODE + 5;
	protected static final int ERROR_CHAR_ILLEGAL = NORMAL_CODE + 6;
	protected static final int ERROR_PASSWORD_TOOLONG = NORMAL_CODE + 7;
	protected static final int ERROR_PASSWORD_INCORRECT = NORMAL_CODE + 8;

	public int checkParameter(String password, String password1) {
		int ret = 0;

		// if (StringUtil.compareSafe(ssid1, mWifiSettings.getSsid()) &&
		// StringUtil.compareSafe(password, mWifiSettings.getWifiPassword())) {
		// showErrorNote(ERROR_NOT_CHANGED);
		// return false;
		// }

		// 密码进行判断
		if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password1)) {
			// 为空，没有密码
			return ERROR_PASSWORD_EMPTY;
		} else {
			if (!password1.equals(password)) {
				return ERROR_PASSWORD_UNMATCHED;
			}
			// 密码不能为空，长度需要大于8，小于32.
			if (password.length() < 8) {
				return ERROR_PASSWORD_TOOSHORT;
			} else if (password.length() > 32) {
				return ERROR_PASSWORD_TOOLONG;
			}
		}

		if (pwdHasIllegalChar(password)) {
			return ERROR_CHAR_ILLEGAL;
		}

		return ret;
	}

	public int checkParameter(String oldPassword, String password,
			String password1) {
		int ret = 0;

		// 密码进行判断
		if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password1)
				|| TextUtils.isEmpty(oldPassword)) {
			// 为空，没有密码
			return ERROR_PASSWORD_EMPTY;
		} else {
			// 密码不能为空，长度需要大于8，小于32.
			if (password.length() < 8 || oldPassword.length() <8) {
				return ERROR_PASSWORD_TOOSHORT;
			} else if (password.length() > 32 ||oldPassword.length() > 32) {
				return ERROR_PASSWORD_TOOLONG;
			}
			if (oldPassword.equals(password)) {
				return ERROR_NOT_CHANGED;
			}

			if (oldPassword.equals(password)) {
				return ERROR_NOT_CHANGED;
			}

			if (!password1.equals(password)) {
				return ERROR_PASSWORD_UNMATCHED;
			}
		}

		if (pwdHasIllegalChar(password)) {
			return ERROR_CHAR_ILLEGAL;
		}
		return ret;
	}
	
	
	public int reSetPasswordAndTips(String oldPassword ,String password, String password1, String tips) {
		int ret = checkParameter(oldPassword,password, password1);
		if (ret == 0) {
			return DMSdk.getInstance().reSetVaultSetting(oldPassword,password1,tips);
		} else {
			return ret;
		}
	}


	public int setPasswordAndTips(String password, String password1, String tips) {
		int ret = checkParameter(password, password1);
		if (ret == 0) {
			DMOpenVault DMOpenVaultRet = DMSdk.getInstance().openVault(password, tips);
			if(DMOpenVaultRet != null) {
				return  DMOpenVaultRet.errorCode;
			} else {
				return -1;
			}
		} else {
			return ret;
		}
	}
	
	
	private boolean pwdHasIllegalChar(String str) {
		String pwdLegalChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLIMNOPQRSTUVWXYZ(`-=[]\\;',./~!@#$%^&*()_+{}|:<>?)";
		for (int i = 0; i < str.length(); i++) {
			if (!(pwdLegalChar.indexOf(str.charAt(i)) >= 0)) {
				return true;
			}
		}
		return false;
	}


}

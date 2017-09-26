package com.dmsys.airdiskpro.backup;

/**
 * 
 * @ClassName:  BackupListener   
 * @Description:   备份监听器，监听当前状态
 * @author: yoy  
 * @date:   2014年9月10日 下午4:46:03   
 * version:1.0.0
 */
public interface BackupInfoListener {
	/**
	 * 进度回调
	 * @param progress 当前进度
	 * @param max	进度的最大值
	 */
	public boolean  onProgress(long progress, long max);
	
	/**
	 * 备份完成后调用
	 * @param result 结果，ture:备份成功，false:未成功
	 * @param errorCode 错误码，为BackupErrorCode中定义的值
	 * @param msg	错误信息，一般调试使用，提示细节错误定位
	 */
	public void onCompleted(boolean result, int errorCode, long total);
	
}

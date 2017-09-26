package com.dmsys.airdiskpro.backup;

/**
 * 
 * @ClassName:  IBackup   
 * @Description:   
 * @author: yoy  
 * @date:   2014年9月22日 下午7:58:28   
 * version:1.0.0
 */
public interface IBackup {
	/**
	 * 开始备份,返回本次是否需要备份
	 */
	public int backup() throws Exception;
	/**
	 * 暂停备份
	 */
	public void pause();
	/** 
	* 继续 备份
	*/ 
	public void resume(); 
	/**
	 * 取消备份
	 */
	public void cancel();	
}

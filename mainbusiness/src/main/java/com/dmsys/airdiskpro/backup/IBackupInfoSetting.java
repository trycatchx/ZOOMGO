package com.dmsys.airdiskpro.backup;

import java.util.List;

/**
 * 
 * @ClassName:  IBackupInfoDscreption   
 * @Description:   描述信息接口
 * @author: yoy  
 * @date:   2014年9月14日 上午12:33:43   
 * version:1.0.0
 */
public interface IBackupInfoSetting extends IBackupSetting{
	/**
	 * 获取备份描述信息列表
	 * @return 描述信息列表
	 * @throws Exception 返回的异常
	 */
	public List<? extends AbstractBackupInfoDscreption> getBackupInfoDscreptionList() throws Exception;

}

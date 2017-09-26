package com.dmsys.airdiskpro.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作SDcard的工具
 * @author liuzongyao
 *
 */
public class SDCardUtil
{
    
    private static String slaverSDCard = "";
    
    private static String TAG = SDCardUtil.class.getSimpleName();
    
    private static boolean isChecked = false;
    
    
    public static String getSlaverSDCard(Context context)
    {
        
    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
    		if (isSDCardChecking())
            {
                return slaverSDCard;
            }
            
            if (!TextUtils.isEmpty(slaverSDCard))
            { // 已经找到外置SD卡的路径
                File f = new File(slaverSDCard);
                if (getTotalSizeOf(slaverSDCard) != 0 && f.exists())
                { // 如果外置SD卡存在
                    return slaverSDCard;
                }
                else
                { // 如果外置SD卡不存在
                    return "";
                }
            }
            else
            {
                if (isChecked)
                {
                    return slaverSDCard;
                }
            }
            
            isChecked = true;
            long maxSize = 0L;
            
            String primary = getPrimarySDCard();
            long primarySDCardSize = getTotalSizeOf(primary);
            
            Runtime runtime = Runtime.getRuntime();
            
            try
            {
                Process process = runtime.exec("df");
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                String line = null;
                while (null != (line = reader.readLine()))
                {
                    DMLog.log(TAG, "line:" + line);
                    
                    String[] params = line.split(" ");
                    if (null == params)
                    {
                        continue;
                    }
                    
                    for (int i = 0; i < params.length; i++)
                    {
                        String mount = params[i].trim();
                        if (mount.startsWith(File.separator + "mnt")
                                || mount.startsWith(File.separator + "storage"))
                        {
                            while (mount.endsWith(":"))
                            { // HTC某款机器得到的路径后面有个分号，把分好去掉
                                mount = mount.substring(0, mount.length() - 1);
                            }
                            if (!isPrimary(primary, mount)
                                    && !"/mnt/secure/asec".equals(mount))
                            {
                                long size = getTotalSizeOf(mount);
                                if (size != 0 && size != primarySDCardSize
                                        && size > maxSize)
                                {
                                    File file = new File(mount);
                                    if (FileUtil.isSymlink(file))
                                    {
                                        continue;
                                    }
                                    if (file.exists() && file.canRead() && file.canWrite())
                                    {
                                        slaverSDCard = mount;
                                        if (!slaverSDCard.endsWith(File.separator))
                                        {
                                            slaverSDCard = slaverSDCard
                                                    + File.separator;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                process.destroy();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
		}else {
			
			try
	        {
	            Class class_StorageManager = StorageManager.class;
	            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
	            Method method_getVolumes = class_StorageManager.getMethod("getVolumes");
	            Method method_getDisks = class_StorageManager.getMethod("getDisks");

	            Class<?> class_VolumeInfo = Class.forName("android.os.storage.VolumeInfo");
	            Method method_getPath = class_VolumeInfo.getMethod("getPath");
	            Method method_getDisk = class_VolumeInfo.getMethod("getDisk");

	            Class<?> class_DiskInfo = Class.forName("android.os.storage.DiskInfo");
	            Method method_isSd = class_DiskInfo.getMethod("isSd");

	            List<Object> volumes = (List<Object>) method_getVolumes.invoke(storageManager);

	            for (Object volumeInfo : volumes)
	            {
	                File file = (File) method_getPath.invoke(volumeInfo);
	                Log.e("sdcard util", "file=" + file);
	                if (file != null)
	                {
	                    Object diskInfo = method_getDisk.invoke(volumeInfo);
	                    if (diskInfo != null)
	                    {
	                        boolean isSd = (boolean) method_isSd.invoke(diskInfo);
	                        Log.e("sdcard util", "isSd=" + isSd);
	                        return file.getAbsolutePath();
	                    }
	                }
	            }

	        }
	        catch (NoSuchMethodException e)
	        {
	            e.printStackTrace();
	        }
	        catch (ClassNotFoundException e)
	        {
	            e.printStackTrace();
	        }
	        catch (InvocationTargetException e)
	        {
	            e.printStackTrace();
	        }
	        catch (IllegalAccessException e)
	        {
	            e.printStackTrace();
	        }
			
			
		}
    	
    	
        
        
        return slaverSDCard;
    }
    
    public static void setCheckAgain(Context context)
    {
        isChecked = false;
        getSlaverSDCard(context);
    }
    
    private static boolean isPrimary(String primary, String path)
    {
        if (!path.endsWith(File.separator))
        {
            path = path + File.separator;
        }
        if (!primary.endsWith(File.separator))
        {
            primary = primary + File.separator;
        }
        return primary.equals(path);
    }
    
    //从Util移过来的-----------------------------
    
    /**
     * 返回内置sd卡路径,带后置分隔符 要获取下载目录, 请使用DownloadConfig.getDownloadPath +yqm
     * 这里返回的不一定是内置sd卡，看注释。应该理解为：首要保存用户数据的存储media/shared storage，即是ROM厂商希望
     * 用户级的数据默认是存放这里。 Traditionally this is an SD card, but it may also be
     * implemented as built-in storage in a device that is distinct from the
     * protected internal storage and can be mounted as a filesystem on a
     * computer.
     * 
     * @return
     */
    public static String getPrimarySDCard()
    {
        // return Environment.getExternalStorageDirectory().getPath();
        String sdcardPath = Environment.getExternalStorageDirectory().getPath();
        if (null != sdcardPath && !sdcardPath.endsWith("/"))
        {
            sdcardPath = sdcardPath + "/";
            
            if (isSDCardChecking())
            {
                return "";
            }
            
            // 有的机型(MOTO XT860)，插入外置SD卡过后，系统就把内置SD卡卸载了。系统的bug。
            if (getTotalSizeOf(sdcardPath) == 0)
            {
                return "";
            }
            
        }
        else if (null != sdcardPath)
        {
            sdcardPath = "";
        }
        
        return sdcardPath;
    }
    
    /**
     * 返回外置sd卡路径,带后置分隔符 返回除内置存储器外, 可用空间最大的存储器
     * 
     * @return
     */
    public static String getSlaveSDCard(Context context)
    {
        String temp = SDCardUtil.getSlaverSDCard(context);
        return temp;
    }
 
    // old
    public static boolean isSDCardExist()
    {
        if (isSDCardChecking())
        {
            return false;
        }
        try
        {
            return Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment.getExternalStorageState());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    // ... new by zxl
    public static boolean isSDCardUnmounted()
    {
        if (isSDCardChecking())
        {
            return true;
        }
        try
        {
            return Environment.MEDIA_UNMOUNTED.equalsIgnoreCase(Environment.getExternalStorageState());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean isSDCardRemoved()
    {
        if (isSDCardChecking())
        {
            return true;
        }
        try
        {
            return Environment.MEDIA_REMOVED.equalsIgnoreCase(Environment.getExternalStorageState());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean isSDCardShared()
    {
        if (isSDCardChecking())
        {
            return true;
        }
        try
        {
            return Environment.MEDIA_SHARED.equalsIgnoreCase(Environment.getExternalStorageState());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean isSDCardChecking()
    {
        try
        {
            return Environment.MEDIA_CHECKING.equalsIgnoreCase(Environment.getExternalStorageState());
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public static long getTotalSizeOf(final String storagePath)
    {
        if (TextUtils.isEmpty(storagePath))
        {
            return 0;
        }
        
        //尝试多加判断，如果无效的参数  StatFs 会报错
        File file = new File(storagePath);
        boolean isSymLink = false;
        
        try
        {
            isSymLink = FileUtil.isSymlink(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        if (!file.exists() || !file.isDirectory() || isSymLink)
        {
            return 0;
        }
        
        StatFs stat = new StatFs(storagePath);
        long blockSize = stat.getBlockSize();
        long blockCount = stat.getBlockCount();
        return blockCount * blockSize;
    }
    
    public static long getUsedSizeOf(final String storagePath)
    {
        return getTotalSizeOf(storagePath) - getAvailableSizeOf(storagePath);
    }
    
    /**
     * 通过读取/etc/vold.fstab文件来解析出已加载的存储器, 得到其路径列表
     * 
     * @return 可用存储器路径列表
     */
    public static ArrayList<String> getMountedDevicesList()
    {
        
        // mount配置文件: /etc/vold.fstab
        final File VOLD_FSTAB = new File(Environment.getRootDirectory()
                .getAbsoluteFile()
                + File.separator
                + "etc"
                + File.separator
                + "vold.fstab");
        
        // mount命令语法: dev_mount <label> <mount_point> <part> <sysfs_path1...>
        // mount命令示例: dev_mount sdcard /mnt/sdcard 1
        // /devices/platform/mmci-omap-hs.1/mmc_host/mmc0
        final String MOUNT = "dev_mount";
        // final int INDEX_LABEL = 1;
        final int INDEX_MOUNT_POINT = 2;
        // final int INDEX_PARTITION = 3;
        final int INDEX_SYSFS_PATH = 4;
        
        ArrayList<String> volumnPathList = new ArrayList<String>();
        if (VOLD_FSTAB.exists() == false)
        {
            DMLog.log(TAG, "/etc/vold.fstab not exist");
            return volumnPathList;
        }
        
        ArrayList<String> mountCmdLines = new ArrayList<String>();
        try
        {
            mountCmdLines.clear();
            BufferedReader reader = new BufferedReader(new FileReader(
                    VOLD_FSTAB));
            String textLine = null;
            while ((textLine = reader.readLine()) != null)
            {
                if (textLine.startsWith(MOUNT))
                {
                    mountCmdLines.add(textLine);
                }
            }
            reader.close();
            mountCmdLines.trimToSize();
        }
        catch (IOException e)
        {
            DMLog.log(TAG, "failed to parse /etc/vold.fstab");
        }
        
        for (final String mountCmdLine : mountCmdLines)
        {
            if (mountCmdLine == null)
            {
                continue;
            }
            
            String[] infos = mountCmdLine.split(" ");
            if (infos == null || infos.length < INDEX_SYSFS_PATH)
            {
                DMLog.log(TAG, "command unrecognize, mountCmdLine = "
                        + mountCmdLine);
                continue;
            }
            
            String path = infos[INDEX_MOUNT_POINT];
            if (path == null)
            {
                continue;
            }
            
            if (!new File(path).exists())
            {
                DMLog.log(TAG, "storage not exist : " + path);
                continue;
            }
            
            volumnPathList.add(path);
            DMLog.log(TAG, "add path : " + path + ", availableSize = "
                    + getAvailableSizeOf(path) + ", totalSize = "
                    + getTotalSizeOf(path));
        }
        
        return volumnPathList;
    }
    
    public static int getSDCardsNum()
    {
        ArrayList<String> list = getMountedDevicesList();
        int size = list.size();
        int i = 0;
        for (i = 0; i < size; i++)
        {
            String path = list.get(i);
            File file = new File(path);
            if (file.exists() && file.isDirectory() && file.canWrite()
                    && file.length() > 0)
            {
            }
            else
            {
                size--;
                i--;
            }
        }
        return size;
    }
    
    public static long getAvailableSizeOf(final String storagePath)
    {
        if (TextUtils.isEmpty(storagePath))
        {
            return 0;
        }
        
        //尝试多加判断，如果无效的参数  StatFs 会报错
        File file = new File(storagePath);
        boolean isSymLink = false;
        
        try
        {
            isSymLink = FileUtil.isSymlink(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        if (!file.exists() || !file.isDirectory() || isSymLink)
        {
            return 0;
        }
        
        StatFs stat = new StatFs(storagePath);
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }
    
}

/*
 * 文件名称 : FileUtil.java
 * <p>
 * 作者信息 : liuzongyao
 * <p>
 * 创建时间 : 2013-9-10, 下午7:38:58
 * <p>
 * 版权声明 : Copyright (c) 2009-2012 Hydb Ltd. All rights reserved
 * <p>
 * 评审记录 :
 * <p>
 */

package com.dmsys.airdiskpro.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.dm.baselib.BaseValue;
import com.dmsys.airdiskpro.BrothersApplication;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.dmsdk.model.DMFileCategoryType;
import com.dmsys.dmsdk.util.DMFileTypeUtil;
import com.dmsys.mainbusiness.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 请在这里增加文件描述
 * <p>
 */
public class FileUtil
{
    
    /**
     * @param subPaths
     * @return
     */
    public static String joinPath(String... subPaths) {
        StringBuilder path = new StringBuilder();

        for (int i = 0; i < subPaths.length; i++) {
            String onePath = subPaths[i].trim();
            while (onePath.endsWith("\\") || onePath.endsWith("/")) {
                onePath = onePath.substring(0, onePath.length() - 1);
            }
            if (i == 0) {

            } else {
                while (onePath.endsWith("\\") || onePath.endsWith("/")) {
                    onePath = onePath.substring(1, onePath.length());
                }
                path.append(File.separator);
            }
            path.append(onePath);
        }

        return path.toString();
    }

    public static boolean ensureDir(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        boolean ret = false;

        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            try {
                ret = file.mkdirs();
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        } else {
            ret = true;
        }

        return ret;
    }

    public static boolean isFileExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        return true;
    }

    public static boolean deleteFile(String path) {
        if (null == path) {
            return false;
        }
        boolean ret = false;

        File file = new File(path);
        if (file.exists()) {
            ret = file.delete();
        }
        return ret;
    }

    public static boolean ensureFile(String path) {
        if (null == path) {
            return false;
        }

        boolean ret = false;

        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            try {
                file.createNewFile();
                ret = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ret = true;
        }

        return ret;
    }
    
    
    /**
     * 获取文件后缀名
     * 
     * @param fileName
     * @return
     */
    public static String getFileSuffix(String fileName) {
        String fileType = null;
        if (fileName != null) {
            int idx = fileName.lastIndexOf(".");
            if (idx > 0) {
                fileType = fileName.substring(idx + 1, fileName.length()).toLowerCase();
            }
        }
        return fileType;
    }

    public static String getFileNameFromPath(String filePath) {
        String name = null;
        if (filePath != null) {
            int idx = filePath.lastIndexOf("/");
            if (idx > 0) {
                name = filePath.substring(idx + 1, filePath.length())
                        .toLowerCase();
            }else{
                name = filePath;
            }
        }
        return name;
    }
    
    /**
     * 返回文件的所在的目录的绝对路径
     * 
     * @param filePath
     * @return 返回文件的所在的目录的绝对路径,不含最后的斜杠分隔符
     */
    public static String getFileParentAbsolutePath(String filePath) {
        File file = new File(filePath);
        return file.getParent();
    }
    
    public static void copySdcardFile(InputStream is, OutputStream os) throws IOException {
        byte bt[] = new byte[1024];
        int c;
        while ((c = is.read(bt)) > 0) {
            os.write(bt, 0, c);
        }
    }
    
    /**
     * 判断两个路径是否相等 大小写不敏感 : 存储卡的文件系统一般为FAT, 大小写不敏感
     * 
     * @param pathSrc
     * @param pathDst
     * @return
     */
    public static boolean isPathEqual(final String pathSrc, final String pathDst) {
        if (pathSrc == null || pathDst == null) {
            return false;
        }

        String path1 = pathSrc.endsWith("/") ? pathSrc : pathSrc + "/";
        String path2 = pathDst.endsWith("/") ? pathDst : pathDst + "/";
        boolean isEqual = path1.equalsIgnoreCase(path2);
        return isEqual;
    }
    
    
    /**
     * 压缩文件到zip. 如果耗时可以放在子线程里进行
     * 
     * @param srcFilePath
     * @return 如果成功，zip文件名，失败null
     */
    public static String zipFile(final String srcFilePath) {
        if (srcFilePath == null)
            return null;

        File srcFile = new File(srcFilePath);
        if (!srcFile.exists())
            return null;
        String destFileName = null;
        try {
            FileInputStream srcInput = new FileInputStream(srcFile);
            BufferedInputStream srcBuffer = new BufferedInputStream(srcInput);
            byte[] buf = new byte[1024];
            int len;
            destFileName = srcFilePath + ".zip";
            File destFile = new File(destFileName);
            if (destFile.exists())
                destFile.delete();

            FileOutputStream destFileStream = new FileOutputStream(destFileName);
            BufferedOutputStream destBuffer = new BufferedOutputStream(destFileStream);
            ZipOutputStream zipStream = new ZipOutputStream(destBuffer);// 压缩包
            ZipEntry zipEntry = new ZipEntry(srcFile.getName());// 这是压缩包名里的文件名
            zipStream.putNextEntry(zipEntry);// 写入新的 ZIP 文件条目并将流定位到条目数据的开始处

            while ((len = srcBuffer.read(buf)) != -1) {
                zipStream.write(buf, 0, len);
                zipStream.flush();
            }

            srcBuffer.close();
            zipStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return destFileName;
    }

    
    /**
     * 获取文件类型（后缀）
     * 
     * @param name
     * @param defaultValue
     * @return
     */
    public static String getFileTypeByName(String name, String defaultValue) {
        String type = defaultValue;
        if (name != null) {
            int idx = name.lastIndexOf(".");
            if (idx != -1) {
                type = name.substring(idx + 1, name.length());
            }
        }
        return type;
    }
    

    public static boolean isSymlink(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("File must not be null");
        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    public static int getFileLogo(DMFileCategoryType type) {
		int resId = 0;
		switch (type) {
		case E_VIDEO_CATEGORY:
			resId = R.drawable.bt_download_manager_video;
			break;
		case E_MUSIC_CATEGORY:
			resId = R.drawable.bt_download_manager_music;
			break;
		case E_BOOK_CATEGORY:
			resId = R.drawable.bt_download_manager_text;
			break;
		case E_SOFTWARE_CATEGORY:
			resId = R.drawable.bt_download_manager_apk;
			break;
		case E_PICTURE_CATEGORY:
			resId = R.drawable.bt_download_manager_image;
			break;
		case E_ZIP_CATEGORY:
			resId = R.drawable.bt_download_manager_zip;
			break;
		case E_TORRENT_CATEGORY:
			resId = R.drawable.bt_nhpa_torrent;
			break;
		case E_OTHER_CATEGORY:
			resId = R.drawable.bt_download_manager_other;
			break;
		case E_XLDIR_CATEGORY:
			resId = R.drawable.bt_download_manager_folder;
			break;
		default:
			resId = R.drawable.bt_download_manager_other;
			break;
		}
		return resId;
	}

	public static int getFileLogo(String filename) {
		DMFileCategoryType type = DMFileTypeUtil.getFileCategoryTypeByName(filename);
		if (type != DMFileCategoryType.E_BOOK_CATEGORY) {
			return getFileLogo(type);
		} else {
			String normalName = filename.toLowerCase();
			if (normalName.endsWith(".pdf")) {
				return R.drawable.pdf;
			} else if (normalName.endsWith(".ppt") || normalName.endsWith(".pptx")) {
				return R.drawable.ppt;
			} else if (normalName.endsWith(".xls") || normalName.endsWith(".xlsx")) {
				return R.drawable.bt_download_manager_xls;
			}else if (normalName.endsWith(".doc") || normalName.endsWith(".docx")) {
				return R.drawable.bt_download_manager_doc;
			} else {
				return getFileLogo(type);
			}
		}
	}
	
	public static int getFileLogo(String filename, int tag) {
		DMFileCategoryType type = DMFileTypeUtil.getFileCategoryTypeByName(filename);
		if (type != DMFileCategoryType.E_VIDEO_CATEGORY) {
			if (tag == DMFileTypeUtil.ICON_TAG_NORMAL) {
				return R.drawable.bt_download_manager_video;
			} else {
				return R.drawable.bt_download_manager_video_big;
			}
		} else {
			return getFileLogo(filename);
		}
	}
	
	public static int getFileLogo(DMFile file) {
		if (file.isDir()) {
			return R.drawable.bt_download_manager_folder;
		} else {
			return getFileLogo(file.getName());
		}
	}
	
	public static int getFileLogo(DMFile file, int tag) {
		if (file.isDir()) {
			return R.drawable.bt_download_manager_folder;
		} else if (file.getType() == DMFileCategoryType.E_VIDEO_CATEGORY) {
			if (tag == DMFileTypeUtil.ICON_TAG_NORMAL) {
				return R.drawable.bt_download_manager_video;
			} else {
				return R.drawable.bt_download_manager_video_big;
			}
		} else {
			return getFileLogo(file.getName());
		}
	}

	public static int getFileIconResId(String filePath) {
		int resId = R.drawable.bt_download_manager_other;
		if (filePath != null) {
			DMFileCategoryType type = DMFileTypeUtil.getFileCategoryTypeByName(filePath);
			switch (type) {
			case E_VIDEO_CATEGORY:
				resId = R.drawable.bt_download_manager_video;
				break;
			case E_MUSIC_CATEGORY:
				resId = R.drawable.bt_download_manager_music;
				break;
			case E_BOOK_CATEGORY:
				resId = R.drawable.bt_download_manager_text;
				break;
			case E_SOFTWARE_CATEGORY:
				resId = R.drawable.bt_download_manager_apk;
				break;
			case E_PICTURE_CATEGORY:
				resId = R.drawable.bt_download_manager_image;
				break;
			case E_ZIP_CATEGORY:
				resId = R.drawable.bt_download_manager_zip;
				break;
			case E_TORRENT_CATEGORY:
				resId = R.drawable.bt_nhpa_torrent;
				break;
			case E_OTHER_CATEGORY:
				resId = R.drawable.bt_download_manager_other;
				break;
			default:
				resId = R.drawable.bt_download_manager_other;
				break;
			}
		}
		return resId;
	}

	public static Drawable getFileIcon(Context ctx, String filePath) {
		int resId = 0;
		Drawable d = null;
		if (filePath != null) {
			DMFileCategoryType type = DMFileTypeUtil.getFileCategoryTypeByName(filePath); 
			switch (type) {
			case E_VIDEO_CATEGORY:
				resId = R.drawable.bt_download_manager_video;
				break;
			case E_MUSIC_CATEGORY:
				resId = R.drawable.bt_download_manager_music;
				break;
			case E_BOOK_CATEGORY:
				resId = R.drawable.bt_download_manager_text;
				break;
			case E_SOFTWARE_CATEGORY:
				resId = R.drawable.bt_download_manager_apk;
				break;
			case E_PICTURE_CATEGORY:
				resId = R.drawable.bt_download_manager_image;
				break;
			case E_ZIP_CATEGORY:
				resId = R.drawable.bt_download_manager_zip;
				break;
			case E_TORRENT_CATEGORY:
				resId = R.drawable.bt_nhpa_torrent;
				break;
			case E_OTHER_CATEGORY:
				resId = R.drawable.bt_download_manager_other;
				break;
			default:
				resId = R.drawable.bt_download_manager_other;
				break;
			}
			d = ctx.getResources().getDrawable(resId);

			if (resId == R.drawable.bt_download_manager_apk) {
//				AppHelper.ApkInfo info = AppHelper.getApkInfo(ctx, filePath);
//				if (info != null) {
//					Drawable icon = info.getApkIcon();
//					if (icon != null) {
//						d = icon;
//					}
//				}
			}
		}
		return d;
	}

	public static String getFileTypeString(DMFile file)
	{
		DMFileCategoryType type = file.getType();
		return categoryType2TypeString(type);
	}
	
	public static String categoryType2TypeString(DMFileCategoryType type) {
		String resString;
		switch (type) {
		case E_VIDEO_CATEGORY:
			resString = BrothersApplication.getInstance().getString(R.string.DM_More_Detail_Video);
			break;
		case E_MUSIC_CATEGORY:
			resString = BrothersApplication.getInstance().getString(R.string.DM_More_Detail_Audio);
			break;
		case E_BOOK_CATEGORY:
			resString = BrothersApplication.getInstance().getString(R.string.DM_More_Detail_Document);
			break;
		case E_SOFTWARE_CATEGORY:
			resString = BrothersApplication.getInstance().getString(R.string.DM_Attribute_Apk_Type);
			break;
		case E_PICTURE_CATEGORY:
			resString = BrothersApplication.getInstance().getString(R.string.DM_More_Detail_Image);
			break;
		case E_ZIP_CATEGORY:
			resString = BrothersApplication.getInstance().getString(R.string.DM_More_Detail_Archive);
			break;
		case E_TORRENT_CATEGORY:
			resString = BrothersApplication.getInstance().getString(R.string.DM_Attribute_Torrent_Type);
			break;
		case E_OTHER_CATEGORY:
			resString = BrothersApplication.getInstance().getString(R.string.DM_More_Detail_Other);
			break;
		case E_XLDIR_CATEGORY:
			resString = BrothersApplication.getInstance().getString(R.string.DM_More_Detail_Folder);
			break;
		default:
			resString = BrothersApplication.getInstance().getString(R.string.DM_More_Detail_Other);
			break;
		}
		return resString;
	}
	
	public static void thirdPartOpen(DMFile file, Context context) {
		// TODO Auto-generated method stub
		String filePath = file.mPath;

		if (file.mLocation == DMFile.LOCATION_UDISK) {
			filePath = "http://" + BaseValue.Host + File.separator +filePath;
			filePath = FileInfoUtils.encodeUri(filePath);
		} else {
			filePath = "file://" + filePath;
		}
		
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.parse(filePath);
		System.out.println("thirdparty:"+uri);
		String type = getMIMEType(file.getName());
		intent.setDataAndType(uri, type);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// TODO: handle exception
			Toast.makeText(context, context.getString(R.string.DM_Remind_Share_No_App), Toast.LENGTH_SHORT).show();
		}
	}
	
	public static String getMIMEType(String fName) {

		String type = "*/*";
		int dotIndex = fName.lastIndexOf(".");
		if (dotIndex < 0) {
			return type;
		}
		String end = fName.substring(dotIndex, fName.length()).toLowerCase();
		if (end == "")
			return type;
		for (int i = 0; i < MIME_MapTable.length; i++) {
			if (end.equals(MIME_MapTable[i][0]))
				type = MIME_MapTable[i][1];
		}
		return type;
	}
	
	
	private static String[][] MIME_MapTable = {
			// {后缀名，MIME类型}
			{ ".3gp", "video/3gpp" }, { ".apk", "application/vnd.android.package-archive" }, { ".asf", "video/x-ms-asf" },
			{ ".avi", "video/x-msvideo" }, { ".bin", "application/octet-stream" }, { ".bmp", "image/*" }, { ".c", "text/plain" },
			{ ".class", "application/octet-stream" }, { ".conf", "text/plain" }, { ".cpp", "text/plain" }, { ".doc", "application/msword" },
			{ ".docx", "application/msword" }, { ".xls", "application/vnd.ms-excel" },
			{ ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" }, { ".exe", "application/octet-stream" },
			{ ".gif", "image/*" }, { ".gtar", "application/x-gtar" }, { ".gz", "application/x-gzip" }, { ".h", "text/plain" },
			{ ".htm", "text/html" }, { ".html", "text/html" }, { ".jar", "application/java-archive" }, { ".java", "text/plain" },
			{ ".jpeg", "image/*" }, { ".jpg", "image/*" }, { ".js", "application/x-javascript" }, { ".log", "text/plain" },
			{ ".m3u", "audio/x-mpegurl" }, { ".m4a", "audio/mp4a-latm" }, { ".m4b", "audio/mp4a-latm" }, { ".m4p", "audio/mp4a-latm" },
			{ ".m4u", "video/vnd.mpegurl" }, { ".m4v", "video/x-m4v" }, { ".mov", "video/quicktime" }, { ".mp2", "audio/x-mpeg" },
			{ ".mp3", "audio/x-mpeg" }, { ".mp4", "video/mp4" }, { ".mpc", "application/vnd.mpohun.certificate" }, { ".mpe", "video/mpeg" },
			{ ".mpeg", "video/mpeg" }, { ".mpg", "video/mpeg" }, { ".mpg4", "video/mp4" }, { ".mpga", "audio/mpeg" },{ ".m2ts", "video/mp4" },
			{ ".msg", "application/vnd.ms-outlook" }, { ".ogg", "audio/ogg" }, { ".pdf", "application/pdf" }, { ".png", "image/*" },
			{ ".pps", "application/vnd.ms-powerpoint" }, { ".ppt", "application/vnd.ms-powerpoint" },
			{ ".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation" }, { ".prop", "text/plain" },
			{ ".rc", "text/plain" }, { ".rmvb", "audio/x-pn-realaudio" }, { ".rtf", "application/rtf" }, { ".sh", "text/plain" },
			{ ".tar", "application/x-tar" }, { ".tgz", "application/x-compressed" }, { ".txt", "text/plain" }, { ".wav", "audio/x-wav" },
			{ ".wma", "audio/x-ms-wma" }, { ".wmv", "audio/x-ms-wmv" }, { ".wps", "application/vnd.ms-works" }, { ".xml", "text/plain" },
			{ ".z", "application/x-compress" }, { ".zip", "application/x-zip-compressed" }, { "", "*/*" } };
	
	/*
	 * 对文件列表进行排序 **
	 */
	public static void sortFileListByName(List<DMFile> list) {
		Collections.sort(list, FileComparator);
	}
	
	public static void sortFileListByTime(List<DMFile> list) {
		Collections.sort(list, FileLastModifyComparator);
	}
	
	/*
	 * 对一个目录下面的所有文件和文件夹排序 *
	 */
	public static Comparator<DMFile> FileComparator = new Comparator<DMFile>() {

		@Override
		public int compare(DMFile f1, DMFile f2) {
			if (f1 == null || f2 == null) {
				if (f1 == null) {
					return -1;
				} else {
					return 1;
				}
			} else {
				if (f1.mType == DMFileCategoryType.E_XLFILE_UPPER || f2.mType == DMFileCategoryType.E_XLFILE_UPPER) {
					// 排序时将返回上一级放到最前面
					return 1;
				}

				boolean d1 = f1.isDir();
				boolean d2 = f2.isDir();

				if (!d1 && !d2) { // 比较文件夹
					return f1.getName().compareToIgnoreCase(f2.getName());
				} else if (!d1 && d2) {
					return 1;
				} else if (d1 && !d2) {
					return -1;
				} else {
					return f1.getName().compareToIgnoreCase(f2.getName());
				}
			}
		}
	};
	
	static Comparator<DMFile> FileLastModifyComparator = new Comparator<DMFile>() {

		@Override
		public int compare(DMFile f1, DMFile f2) {
			if (f1 == null || f2 == null) {
				if (f1 == null) {
					return -1;
				} else if(f2 == null) {
					return 1;
				} else {
					return 0;
				}
			} else {
				if (f1.mType == DMFileCategoryType.E_XLFILE_UPPER || f2.mType == DMFileCategoryType.E_XLFILE_UPPER) {
					// 排序时将返回上一级放到最前面
					if(f1.mType == DMFileCategoryType.E_XLFILE_UPPER) {
						return 1;
					} else if(f2.mType == DMFileCategoryType.E_XLFILE_UPPER) {
						return -1;
					} else {
						return 0;
					}
				} 

				boolean d1 = f1.isDir();
				boolean d2 = f2.isDir();

				if (!d1 && !d2) { // 比较文件夹
					long time2 = f2.mLastModify;
					long time1 = f1.mLastModify;
					if (time2 > time1)
						return 1;
					else if(time2 == time1) {
						return 0;
					} else {
						return -1;
					}
				} else if (!d1 && d2) {
					return 1;
				} else if (d1 && !d2) {
					return -1;
				} else {
					long time2 = f2.mLastModify;
					long time1 = f1.mLastModify;
					if (time2 > time1)
						return 1;
					else if(time2 == time1) {
						return 0;
					} else {
						return -1;
					}
						
				}
			}
		}
	};

    
}

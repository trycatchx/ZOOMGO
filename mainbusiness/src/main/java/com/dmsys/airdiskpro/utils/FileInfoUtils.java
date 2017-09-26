package com.dmsys.airdiskpro.utils;

import android.content.Context;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.text.ParseException;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提供与文件信息相关的静态工具函数。
 */
public class FileInfoUtils {

	private static Context sContext;

	private FileInfoUtils() {
	}

	public static void scanFile(File file) {
		if (file.exists()) {
			/*
			 * Log.e("SCANFILE", file.getPath()); if
			 * (mDBOperator.isFileExists(file)) { Log.e("UPDATEFILE",
			 * file.getPath()); updateFile(file); } else { Log.e("INSERTFILE",
			 * file.getPath()); insertFile(file); }
			 */
		} else {
			/*
			 * if (mDBOperator.isFileExists(file)) { Log.e("REMOVEFILE",
			 * file.getPath()); removeFile(file); }
			 */
		}
	}

	/**
	 * 完整扫描SD卡，重建文件索引。
	 */
	public static void scanAll() {
		long t_Folder_Num = 0;
		long t_File_Num = 0;
		long startTime = System.currentTimeMillis();
		long endTime;

		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return;
		}
		// 准备初始栈
		Stack<String> stack = new Stack<String>();
		stack.push(Environment.getExternalStorageDirectory().getPath());
		// 开始文件遍历
		while (!stack.isEmpty()) {
			String parent = stack.pop();
			File path = new File(parent);
			File[] files = path.listFiles();
			if (null == files)
				continue;
			for (File f : files) {
				if (f.isDirectory()) {
					if (isQualifiedDirectory(f)) {
						stack.push(f.getPath());
						t_Folder_Num++;
					}
				} else {
					if (true) {
						scanFile(f);
						t_File_Num++;
					}
				}
			}
		}
		endTime = System.currentTimeMillis();

		android.util.Log.e("FileInfo", "扫描全盘,使用时间:" + (endTime - startTime));
		android.util.Log.e("FileInfo", "扫描全盘,文件夹数:" + t_Folder_Num);
		android.util.Log.e("FileInfo", "扫描全盘,文件数:" + t_File_Num);
	}

	/**
	 * 判断一个文件夹是否应该压入待索引栈。
	 * 
	 * @param file
	 *            指向文件夹的文件对象
	 * @return 是否应该压栈
	 */
	private static boolean isQualifiedDirectory(File file) {
		if (file.getName().equals(".") || file.getName().equals(".."))
			return false;
		return true;
	}

	/**
	 * 获取给定文件名的路径部分
	 * 
	 * @param filename
	 *            源文件名
	 * @return 源文件名的路径部分
	 */
	public static String pathPart(String filename) {
		int end = filename.lastIndexOf("/");

		//android.util.Log.e("FileInfo--pathPart:" , filename);
		if (end != -1) {
			return filename.substring(0, end);
		} else {
			return "";
		}
	}
	
	/**
	 * 获取真实路径的 父路径
	 * @param url
	 * @return
	 */
	public static String pathParent(String url) {
		String path = url;
		if(url.endsWith("/"))
		{
			path = url.substring(0, url.length() - 1);
			
		}
		
		return pathPart(path);
		
	}

	/**
	 * 获取给定文件名的不包括路径,部份
	 * 
	 * @param filename
	 *            源文件名
	 * @return 源文件名的不包括路径
	 */
	public static String fileName(String filename) {
		int start = filename.lastIndexOf("/");

		if (start != -1) {
			return filename.substring(start + 1);
		} else {
			return filename;
		}
	}
	/**
	 * 获取新的文件名路径
	 * 
	 * @param filename
	 *            源文件名路径
	 * @return 修改后的路径名
	 */	
	public static String reName(String path , String newName){		
		int t_index = path.lastIndexOf("/");
		String t_NewPath;
		int t_newIdx = newName.lastIndexOf("/");
		
		t_NewPath = newName;
		if(t_index != -1){
			t_NewPath = path.substring(0,t_index + 1);
		}	
		if(t_newIdx != -1){
			t_NewPath += newName.substring(t_newIdx + 1);
		}else{
			t_NewPath += newName;
		}
		
		return t_NewPath;
		
	}


	/**
	 * 获取给定文件名的主文件名名部分
	 * 
	 * @param filename 如 /sdfsfd/sdfsdf/abd.jpg
	 *            源文件名
	 * @return 源文件名的主文件名名部分(不含路径及扩展名) adb
	 */
	public static String mainName(String filename) {
		int start = filename.lastIndexOf("/");
		int stop = filename.lastIndexOf(".");
		if (stop < start)
			stop = filename.length();
		if ((start >= -1) && (stop > -1)) {
			return filename.substring(start + 1, stop);
		} else {
			return filename;
		}
	}

	/**
	 * 获取给定文件名的扩展名部分
	 * 
	 * @param filename
	 *            源文件名
	 * @return 源文件名的扩展名部分(不含小数点)
	 */
	public static String extension(String filename) {
		int start = filename.lastIndexOf("/");
		int stop = filename.lastIndexOf(".");
		if (stop < start || stop >= filename.length() - 1) {
			return "";
		} else if (stop == -1) {
			return "";
		} else {
			return filename.substring(stop + 1, filename.length());
		}
	}

	/**
	 * 获取给定文件名的 MIME 类型
	 * 
	 * @param filename
	 *            源文件名
	 * @return 源文件名的 MIME 类型
	 */
	public static String mimeType(String filename) {
		String ext = extension(filename.toLowerCase());
		String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
		return (mime == null) ? "*.*" : mime;
	}
	
	
	/**
	 * 判断一个文件是否是隐藏文件
	 */
	public static boolean isHidden(String name)
	{
		String nameStr = fileName(name);
		
		if((nameStr != null) && (nameStr.startsWith(".")))
		{
			return true;
		}
		
		return false; 
	}

	/**
	 * 获取文件长度的智能可读字符串形式。
	 * 
	 * @param size
	 *            文件字节长度
	 * @return 文件长度的字符串表示
	 */
	public static String getLegibilityFileSize(long size) {
		if (size < 1024)
			return String.format("%d B", size);
		else if (size < 1024 * 1024)
			return String.format("%.2f KB", (double) size / 1024);
		else if (size < 1024 * 1024 * 1024)
			return String.format("%.2f MB", (double) size / (1024 * 1024));
		else if (size < 1024L * 1024 * 1024 * 1024)
			return String.format("%.2f GB", (double) size
					/ (1024 * 1024 * 1024));
		else
			return String.format("%.2f EB", (double) size
					/ (1024L * 1024 * 1024 * 1024));
	}

	
	
	/**
	 * 从文件长度的字符串形式转换为字节数表示。
	 * 
	 * @param sizeString
	 *            文件长度的字符串表示
	 * @return 文件字节长度
	 * @throws ParseException
	 *             给定字符串不是支持的形式，解析失败
	 */
	public static long stringToSize(String sizeString) throws ParseException {
		Pattern pattern = Pattern.compile("(-?\\d+\\.?\\d*)([\\w]{0,2})",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sizeString);
		if (matcher.matches()) {
			double baseSize = Double.parseDouble(matcher.group(1));
			String unit = matcher.group(2).toLowerCase();
			if (unit.equals("b") || unit.length() == 0) {
				return (long) baseSize;
			} else if (unit.equals("k") || unit.equals("kb")) {
				return (long) (baseSize * 1024);
			} else if (unit.equals("m") || unit.equals("mb")) {
				return (long) (baseSize * (1024 * 1024));
			} else if (unit.equals("g") || unit.equals("gb")) {
				return (long) (baseSize * (1024 * 1024 * 1024));
			} else if (unit.equals("e") || unit.equals("eb")) {
				return (long) (baseSize * (1024L * 1024 * 1024 * 1024));
			}
		}
		throw new ParseException(sizeString, 0);
	}

	


	/**
	 * 从时间跨度的字符串形式转换为毫秒数表示。
	 * 
	 * @param sizeString
	 *            时间跨度的字符串表示
	 * @return 毫秒数
	 * @throws ParseException
	 *             给定字符串不是支持的形式，解析失败
	 */
	public static long timespanToMillis(String timeString)
			throws ParseException {
		Pattern pattern = Pattern.compile("(-?\\d+\\.?\\d*)([\\w]{0,1})",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(timeString);
		if (matcher.matches()) {
			double baseMillis = Double.parseDouble(matcher.group(1));
			String unit = matcher.group(2).toLowerCase();
			if (unit.equals("d") || unit.length() == 0) {
				return (long) (baseMillis * 1000 * 3600 * 24);
			} else if (unit.equals("h")) {
				return (long) (baseMillis * 1000 * 3600);
			} else if (unit.equals("w")) {
				return (long) (baseMillis * 1000 * 3600 * 24 * 7);
			} else if (unit.equals("m")) {
				return (long) (baseMillis * 1000 * 3600 * 24 * 30);
			} else if (unit.equals("y")) {
				return (long) (baseMillis * 1000 * 3600 * 24 * 360);
			}
		}
		throw new ParseException(timeString, 0);
	}

	/**
	 * 初始化文件信息工具函数与程序的关联。主程序改变首选项后应再次调用此函数。
	 * 
	 * @param prefs
	 *            程序的首选项对象，从这里获得索引设置
	 * @param context
	 *            程序上下文(建议在 Activity 及其派生类中用 getApplicationContext() 获得)
	 */
	public static void init(Context context) {
		sContext = context;
	}

	/**
	 * URL-encodes everything between "/"-characters. Encodes spaces as '%20'
	 * instead of '+'.
	 */
	public static String encodeUri(String uri) {
		String newUri = "";
		
		if(uri.contains("http://")){
			int uriIP_end = uri.indexOf("/", "http://".length()) + 1;		
			
			newUri = uri.substring(0, uriIP_end);
			
			uri = uri.substring(uriIP_end);
		}
		
		StringTokenizer st = new StringTokenizer(uri, "/ ", true);
		
		
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.equals("/")) {
				// newUri += "%2F";
				newUri += "/";
			} else if (tok.equals(" ")){
				newUri += "%20";
			}else if (tok.equals("#")){
				newUri += "%23";
			}else if (tok.equals("%")){
				newUri += "%25";
			}else if (tok.equals("&")){
				newUri += "%26";
			}else {
				newUri += URLEncoder.encode(tok);
				// For Java 1.4 you'll want to use this instead:
				// try { newUri += URLEncoder.encode( tok, "UTF-8" ); } catch (
				// java.io.UnsupportedEncodingException uee ) {}
			}
		}
		return newUri;
	}

	/**
	 * Decodes the percent encoding scheme. <br/>
	 * For example: "an+example%20string" -> "an example string"
	 */
	public static String decodePercent(String str) {

		String org = str;
		
		try {				

			//str = URLDecoder.decode(str);
			str = decode(str);
			
			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				switch (c) {
/*
				case '+':
					sb.append("%2b");
					break;
*/
				case '%':
					sb.append((char) Integer.parseInt(
							str.substring(i + 1, i + 3), 16));
					i += 2;
					break;
				default:
					sb.append(c);
					break;
				}
			}
			return sb.toString();
		} catch (Exception e) {
			android.util.Log.e("FileInfo.java",
					"BAD REQUEST: Bad percent-encoding.");
			return org;
		}
	}
	
    static Charset defaultCharset;

    public static String decode(String s) {
    	 String ret = s;

        if (defaultCharset == null) {
            try {
                defaultCharset = Charset.forName(
                        System.getProperty("file.encoding")); //$NON-NLS-1$
            } catch (IllegalCharsetNameException e) {
                // Ignored
            } catch (UnsupportedCharsetException e) {
                // Ignored
            }

            if (defaultCharset == null) {
                defaultCharset = Charset.forName("ISO-8859-1"); //$NON-NLS-1$
            }
        }
        
        try {
			ret = URLDecoder.decode(s);
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
		}
		System.out.println("ret123"+ret);
        return ret;
    }	
	
	   private static String decode(String s, Charset charset) {
		 
	        char str_buf[] = new char[s.length()];
	        byte buf[] = new byte[s.length() / 3];
	        int buf_len = 0;

	        for (int i = 0; i < s.length();) {
	            char c = s.charAt(i);
	            if (c == '%') {

	                int len = 0;
	                do {
	                    if (i + 2 >= s.length()) {
	                        throw new IllegalArgumentException(
	                                // K01fe=Incomplete % sequence at\: {0}
	                                "K01fe ;i ="+ i); //$NON-NLS-1$
	                    }
	                    int d1 = Character.digit(s.charAt(i + 1), 16);
	                    int d2 = Character.digit(s.charAt(i + 2), 16);
	                    if (d1 == -1 || d2 == -1) {
	                        throw new IllegalArgumentException(
	                                // K01ff=Invalid % sequence ({0}) at\: {1}
	                               
	                                        "K01ff"+ //$NON-NLS-1$
	                                        s.substring(i, i + 3)+
	                                        String.valueOf(i));
	                    }
	                    buf[len++] = (byte) ((d1 << 4) + d2);
	                    i += 3;
	                } while (i < s.length() && s.charAt(i) == '%');

	                CharBuffer cb = charset.decode(ByteBuffer.wrap(buf, 0, len));
	                len = cb.length();
	                System.arraycopy(cb.array(), 0, str_buf, buf_len, len);
	                buf_len += len;
	                continue;
	            } else {
	                str_buf[buf_len] = c;
	            }
	            i++;
	            buf_len++;
	        }
	        return new String(str_buf, 0, buf_len);
	    }	
	
	
	
	
	

	/**
	 * 正则验证文件名是否合法
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isValidFileName(String fileName) {
		if (fileName == null || fileName.length() > 255) {
			return false;
		} else {
			if (fileName.indexOf('\\') != -1 || fileName.indexOf('/') != -1
					|| fileName.indexOf(':') != -1
					|| fileName.indexOf('*') != -1
					|| fileName.indexOf('?') != -1
					|| fileName.indexOf('"') != -1
					|| fileName.indexOf('<') != -1
					|| fileName.indexOf('>') != -1
					|| fileName.indexOf('|') != -1) {
				return false;
			} else {
				
				if (containsEmoji(fileName)) {
					return false;
				}
				
				return true;
			}
		}
	}

	/**
	 * 检测是否有emoji字符
     * @param source
     * @return 一旦含有就抛出
     */
    public static boolean containsEmoji(String source) {
 
        int len = source.length();
 
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
 
            if (!isNotEmojiCharacter(codePoint)) {
                //do nothing，判断到了这里表明，确认有表情字符
                return true;
            }
        }
        return false;
    }
	
    private static boolean isNotEmojiCharacter(char codePoint) {
        
    	return (codePoint == 0x0) ||
                (codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
    }
	
	/**
	 * 修改属性dialog中字节数的显示问题，每隔三个数加一个点
	 * 
	 * @param fileSize
	 *            传来的字节数
	 * @return lastresult 组装好的显示内容
	 * */
	public static String changeFileSizeShow(long fileSize) {
		String sSize = String.valueOf(fileSize);
		String[] arrSize = sSize.split("");
		Stack<String> size_stack = new Stack<String>();
		for (String ts : arrSize) {
			size_stack.push(ts);
			ts = null;
		}
		String result = "";
		String lastresult = "";
		for (int i = 1; i <= arrSize.length; i++) {
			String ts = size_stack.pop();
			if (i % 3 == 0) {
				ts = ts + ",";
			}
			result = result + ts;
			ts = null;
		}
		String[] arrres = result.split("");
		for (String ts : arrres) {
			size_stack.push(ts);
			ts = null;
		}
		for (int i = 1; i <= arrres.length; i++) {
			lastresult = lastresult + size_stack.pop();
		}
		if (lastresult.startsWith(",")) {
			lastresult = lastresult.substring(1);
		}
		return lastresult;
	}
	
	
	
	
	private static boolean isMatch(String str,String patternStr)
	{
		Pattern pattern = Pattern.compile(patternStr);
		Matcher isMac = pattern.matcher(str);
		if (!isMac.matches()) {
			return false;
		}
		return true;		
	}
}

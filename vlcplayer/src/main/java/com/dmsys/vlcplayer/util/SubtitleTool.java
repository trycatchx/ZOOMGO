package com.dmsys.vlcplayer.util;

import android.net.Uri;

import com.dmsys.dmsdk.DMSdk;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.vlcplayer.subtitle.ChooseSrtBean;
import com.dmsys.vlcplayer.subtitle.FormatASS;
import com.dmsys.vlcplayer.subtitle.FormatSCC;
import com.dmsys.vlcplayer.subtitle.FormatSRT;
import com.dmsys.vlcplayer.subtitle.FormatSTL;
import com.dmsys.vlcplayer.subtitle.FormatTTML;
import com.dmsys.vlcplayer.subtitle.TimedTextFileFormat;
import com.dmsys.vlcplayer.subtitle.TimedTextObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubtitleTool {
    private final static String EXPRESSION = "[0-9]+";
    private final static String EXPRESSION1 = "[0-9][0-9]:[0-5][0-9]:[0-5][0-9],[0-9][0-9][0-9] --> [0-9][0-9]:[0-5][0-9]:[0-5][0-9],[0-9][0-9][0-9]";

    public final static HashMap<String, String> AudioExtendion = new HashMap<String, String>() {
        {
            put("srt", "");
        }
    };

    @SuppressWarnings("deprecation")
    public static ArrayList<ChooseSrtBean> getChooseSrtBeanList(String dirFile) {

        ArrayList<ChooseSrtBean> retArrayList = new ArrayList<ChooseSrtBean>();
        if (dirFile == null)
            return retArrayList;

        if (dirFile != null) {
            // 本地的文件列表
            if (dirFile.startsWith("file://")) {
                dirFile = dirFile.substring(7);
                File file = new File(dirFile);
                if (file != null && file.isDirectory()) {

                    File[] tmpFiles = file.listFiles(new FileFilter() {

                        @Override
                        public boolean accept(File pathname) {
                            // TODO Auto-generated method stub
                            return pathname.isFile()
                                    && (pathname.getName().endsWith(".srt") || pathname
                                    .getName().endsWith(".ass"));
                        }
                    });
                    for (File f : tmpFiles) {
                        retArrayList.add(new ChooseSrtBean(f.getName(), f
                                .getPath()));
                    }
                }
            } else if (dirFile.startsWith("http://")
                    && !dirFile
                    .startsWith("http://127.0.0.1:6789/dropbox-vod-")) {
                // airdisk 文件列表
                // dirFile = dirFile.substring(7);
                // dirFile = dirFile.substring(dirFile.indexOf("/")+1);

                URL url = null;
                try {
                    url = new URL(dirFile);
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (url == null)
                    return retArrayList;
                String path = url.getPath();

                int preIndex = dirFile.indexOf(path);
                String preString = dirFile.substring(0, preIndex);

                // 私有协议不用编码，所以需要解码传下去 ,
                try {
                    path = URLDecoder.decode(path, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (path != null && path.startsWith("/")) {
                    path = path.substring(1);
                }
                List<DMFile> tmpDmFiles = DMSdk.getInstance().getFileList(path);

                String subTitlePath = null;
                if (tmpDmFiles != null && tmpDmFiles.size() > 0) {
                    for (int i = 0; i < tmpDmFiles.size(); i++) {
                        if ((tmpDmFiles.get(i).getName().endsWith(".srt") || tmpDmFiles
                                .get(i).getName().endsWith(".ass"))
                                && !tmpDmFiles.get(i).isDir) {
                            if (tmpDmFiles.get(i).getPath() == null) {
                                continue;
                            }
                            if (tmpDmFiles.get(i).getPath().startsWith("/")) {
                                subTitlePath = preString + tmpDmFiles.get(i).getPath();
                            } else {
                                subTitlePath = preString + "/" + tmpDmFiles.get(i).getPath();
                            }
                            retArrayList.add(new ChooseSrtBean(tmpDmFiles
                                    .get(i).getName(), subTitlePath));
                        }
                    }
                }


            } else {
                // 后续支持dropBox 的文件列表
            }

        }

        return retArrayList;
    }


    public static TimedTextObject parseCaption(String filePath) {


        String charset = getCharset(filePath);// 判断文件编码格式

        TimedTextObject tto = null;
        TimedTextFileFormat ttff;
        InputStream ins = null;
        try {
            if (filePath.startsWith("http://")) {
                filePath = UrilTools.encodeUri(filePath);
                URL url = new URL(filePath);

                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(5000);
                ins = conn.getInputStream();
            } else {
                Uri uri = Uri.parse(filePath);
                ins = new FileInputStream(new File(uri.getPath()));
            }
            String lowerCaseFilePathString = filePath.toLowerCase();
            if (lowerCaseFilePathString.endsWith(".srt")) {
                ttff = new FormatSRT();
            } else if (lowerCaseFilePathString.endsWith(".stl")) {
                ttff = new FormatSTL();
            } else if (lowerCaseFilePathString.endsWith(".scc")) {
                ttff = new FormatSCC();
            } else if (lowerCaseFilePathString.endsWith(".xml")) {
                ttff = new FormatTTML();
            } else if (lowerCaseFilePathString.endsWith(".ass")) {
                ttff = new FormatASS();
            } else {
                ttff = new FormatSRT();
            }

            tto = ttff.parseFile(filePath, ins, charset);

        } catch (Exception e) {

        }

        return tto;
    }

    /**
     * @description 时间轴转换为毫秒
     */
    private static int TimeToMs(String time) {
        int hour = Integer.parseInt(time.substring(0, 2));
        int mintue = Integer.parseInt(time.substring(3, 5));
        int scend = Integer.parseInt(time.substring(6, 8));
        int milli = Integer.parseInt(time.substring(9, 12));
        int msTime = (hour * 3600 + mintue * 60 + scend) * 1000 + milli;
        return msTime;
    }

    /**
     * @description 判断文件的编码格式
     */
    public static String getCharset(String fileUri) {
        String code = "UTF-8";
        BufferedInputStream bin = null;
        InputStream ins = null;
        try {
            if (fileUri.startsWith("http://")) {
                // URL mURL = new URL(fileUri);

                // URI mURL = new URI(url.getProtocol(), url.getUserInfo(),
                // url.getHost(), url.getPort(), url.getPath(),
                // url.getQuery(), url.getRef());

                fileUri = UrilTools.encodeUri(fileUri);
                URL url = new URL(fileUri);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(5000);
                ins = conn.getInputStream();
                bin = new BufferedInputStream(ins);
            } else {
                Uri uri = Uri.parse(fileUri);
                ins = new FileInputStream(uri.getPath());
                bin = new BufferedInputStream(ins);
            }

            int p = (bin.read() << 8) + bin.read();
            switch (p) {
                case 0xefbb:
                    code = "UTF-8";
                    break;
                case 0xfffe:
                    code = "Unicode";
                    break;
                case 0xfeff:
                    code = "UTF-16BE";
                    break;
                default:
                    code = "GBK";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (bin != null) {
                try {
                    bin.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return code;
    }
}

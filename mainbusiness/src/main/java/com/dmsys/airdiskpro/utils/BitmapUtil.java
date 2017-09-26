/*
 * 文件名称 : BitmapUtil.java
 * <p>
 * 作者信息 : liuzongyao
 * <p>
 * 创建时间 : 2013-10-11, 下午5:45:47
 * <p>
 * 版权声明 : Copyright (c) 2009-2012 Hydb Ltd. All rights reserved
 * <p>
 * 评审记录 :
 * <p>
 */

package com.dmsys.airdiskpro.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 位图工具
 * <p>
 */
public class BitmapUtil
{

    public static Bitmap creatBitmap(String path) {
        if (null == path) {
            return null;
        }

        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        return BitmapFactory.decodeFile(path);
    }

    public static void saveBitmap(Bitmap bitmap, String path, String name) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        File file = new File(path + name);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();

            FileOutputStream out = null;
            out = new FileOutputStream(file);
            bitmap.compress(CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 根据url生成图片对象，网络加载图片
    public static Bitmap returnBitMap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap getBitmapByUrl(String url) {
        URL fileUrl = null;
        Bitmap bitmap = null;
        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 将图片圆角化
     * 
     * @param bitmap 需要圆角化的图片
     * @param roundPx 圆角的像素大小， 值越大角度越明显
     * @return 圆角化后的图片
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int roundPx) {
        if (null == bitmap)
            return null;

        Bitmap output = Bitmap
                .createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        // 得到画布
        Canvas canvas = new Canvas(output);

        // 将画布的四角圆化
        final int color = Color.RED;
        final Paint paint = new Paint();
        // 得到与图像相同大小的区域 由构造的四个值决定区域的位置以及大小
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // drawRoundRect的第2,3个参数一样则画的是正圆的一角，如果数值不同则是椭圆的一角
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap drawToBitmap(final Drawable d, final int scaleWidth, final int scaleHeight) {
        Bitmap bmp = null;
        if (null != d) {
            final int width = d.getIntrinsicWidth();
            final int height = d.getIntrinsicHeight();
            if (width > 0 || height > 0) {
                d.setBounds(0, 0, width, height);
                Bitmap tmpBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                final Canvas canvas = new Canvas(tmpBmp);
                d.draw(canvas);
                if (scaleWidth > 0 && scaleHeight > 0) {
                    bmp = Bitmap.createScaledBitmap(tmpBmp, scaleWidth, scaleHeight, false);
                    if (!tmpBmp.isRecycled()) {
                        tmpBmp.recycle();
                    }
                } else {
                    bmp = tmpBmp;
                }
                tmpBmp = null;
            }
        }
        return bmp;
    }

    public static Bitmap scaleBitmap(Bitmap bmp, int width, int height) {
        return Bitmap.createScaledBitmap(bmp, width, height, false);
    }

    public static Bitmap createBitmapCenterCorp(Bitmap bmp, int width, int height) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        if (w == width && h == height) {
            return bmp;
        }

        // 计算压缩/拉升比。
        float xScale = (float) w / width;
        float yScale = (float) h / height;
        float scale = Math.min(xScale, yScale);

        int dstWidth = (int) (w / scale); // 这里强制类型转换，可能导致小数部分被截断。
        int dstHeight = (int) (h / scale);

        // 为了防止强转导致的小数部分被借去，所以在这里处理一下。以防在下一步截图图像的时候出现数组越界。
        dstWidth = dstWidth < width ? width : dstWidth;
        dstHeight = dstHeight < height ? height : dstHeight;

        Bitmap temp1 = null;
        try {
            // 拉升/压缩图片
            temp1 = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, false);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return bmp;
        }

        // 截取中间的一部分。
        w = temp1.getWidth();
        h = temp1.getHeight();

        int startX = (w - width) / 2;
        int startY = (h - height) / 2;
        Bitmap temp2 = null;
        try {
        	if (!temp1.isRecycled()) {
        		temp2 = Bitmap.createBitmap(temp1, startX, startY, width, height);
			}else {
				return bmp;
			}
            
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return temp1;
        }

        bmp.recycle();
        temp1.recycle();
        return temp2;
    }

    public static Bitmap drawableToBitmap(Drawable d) {
        if (d != null) {
            BitmapDrawable db = (BitmapDrawable) d;
            return db.getBitmap();
        }
        return null;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        if (bmp != null && !bmp.isRecycled()) {

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bmp.compress(CompressFormat.PNG, 100, output);
            if (needRecycle) {
                bmp.recycle();
            }

            byte[] result = output.toByteArray();
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
        /* 为了避免前面bitmap 在用的时候已经是被recycled了的，增加保护，如果已经被回收则返回一个空的byte数组 */
        return new byte[] {};
    }

}

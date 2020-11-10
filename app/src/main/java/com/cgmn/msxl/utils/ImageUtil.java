package com.cgmn.msxl.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ImageUtil {
    private static int default_width = 1200;

    public static byte[] getCompressBytes(String pathName, Integer reqWidth){
        try {
            if(reqWidth == null){
                reqWidth = default_width;
            }
            Bitmap bmp = decodeSampledBitmapFromFile(pathName, reqWidth, reqWidth);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            if(bmp != null && !bmp.isRecycled()){
                bmp.isRecycled();
                bmp = null;
            }
            return baos.toByteArray();
        } catch (Exception e) {
        }
        return null;
    }

    public static byte[] getSmallBytes(String pathName){
        try {
            Bitmap bmp = decodeSampledBitmapFromFile(pathName, 100, 100);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            if(bmp != null && !bmp.isRecycled()){
                bmp.isRecycled();
                bmp = null;
            }
            return baos.toByteArray();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 根据计算的inSampleSize，得到压缩后图片
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
        try {
            // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathName, options);

            // 调用上面定义的方法计算inSampleSize值
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            // 使用获取到的inSampleSize值再次解析图片
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(pathName, options);
        } catch (Exception e) {

            System.out.println("decodeSampledBitmapFromFile err in : " + e.toString());
        }

        return null;
    }

    /**
     * 计算inSampleSize，用于压缩图片
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (width > reqWidth || height > reqHeight) {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);

            inSampleSize = Math.max(widthRadio, heightRadio);
        }

        return inSampleSize;
    }
}

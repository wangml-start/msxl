package com.cgmn.msxl.comp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.utils.MessageUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class NetImageView extends ImageView {
    /**
     * 圆形模式
     */
    private static final int MODE_CIRCLE = 1;
    /**
     * 普通模式
     */
    private static final int MODE_NONE = 0;
    /**
     * 圆角模式
     */
    private static final int MODE_ROUND = 2;
    private Paint mPaint;
    private int currMode = MODE_CIRCLE;
    /**
     * 圆角半径
     */
    private int currRound = dp2px(10);

    private Context mContext;
    private String imagePath;
    private String imageName;
    private String path;

    //是否启用缓存
    public boolean isUseCache = true;

    //子线程不能操作UI，通过Handler设置图片

    private Handler handler;

    public NetImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NetImageView(Context context) {
        super(context);
    }

    public NetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        path = GlobalDataHelper.getPortraitCachePath();
        initHandler();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        // 获取设置的图标
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RoundImageView);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if(R.styleable.RoundImageView_image_type == attr){
                currMode = a.hasValue(R.styleable.RoundImageView_image_type) ? a.getInt(R.styleable.RoundImageView_image_type, MODE_NONE) : MODE_CIRCLE;
            }else if(R.styleable.RoundImageView_image_radius == attr){
                currRound = a.hasValue(R.styleable.RoundImageView_image_radius) ? a.getDimensionPixelSize(R.styleable.RoundImageView_image_radius, currRound) : currRound;
            }
        }
        a.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * 当模式为圆形模式的时候，我们强制让宽高一致
         */
        if (currMode == MODE_CIRCLE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int result = Math.min(getMeasuredHeight(), getMeasuredWidth());
            if(result > 0){
                currRound = (result-20) / 2;
            }
            setMeasuredDimension(result, result);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable mDrawable = getDrawable();
        Matrix mDrawMatrix = getImageMatrix();
        if (mDrawable == null) {
            return; // couldn't resolve the URI
        }

        if (mDrawable.getIntrinsicWidth() == 0 || mDrawable.getIntrinsicHeight() == 0) {
            return;     // nothing to draw (empty bounds)
        }

        if (mDrawMatrix == null && getPaddingTop() == 0 && getPaddingLeft() == 0) {
            mDrawable.draw(canvas);
        } else {
            final int saveCount = canvas.getSaveCount();
            canvas.save();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (getCropToPadding()) {
                    final int scrollX = getScrollX();
                    final int scrollY = getScrollY();
                    canvas.clipRect(scrollX + getPaddingLeft(), scrollY + getPaddingTop(),
                            scrollX + getRight() - getLeft() - getPaddingRight(),
                            scrollY + getBottom() - getTop() - getPaddingBottom());
                }
            }
            canvas.translate(getPaddingLeft(), getPaddingTop());
            if (currMode == MODE_CIRCLE) {//当为圆形模式的时候
                Bitmap bitmap = drawable2Bitmap(mDrawable);
                mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mPaint);
            } else if (currMode == MODE_ROUND) {//当为圆角模式的时候
                Bitmap bitmap = drawable2Bitmap(mDrawable);
                mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                canvas.drawRoundRect(new RectF(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom()),
                        currRound, currRound, mPaint);
            } else {
                if (mDrawMatrix != null) {
                    canvas.concat(mDrawMatrix);
                }
                mDrawable.draw(canvas);
            }
            canvas.restoreToCount(saveCount);
        }
    }

    /**
     * drawable转换成bitmap
     */
    private Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //根据传递的scaletype获取matrix对象，设置给bitmap
        Matrix matrix = getImageMatrix();
        if (matrix != null) {
            canvas.concat(matrix);
        }
        drawable.draw(canvas);
        return bitmap;
    }


    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private void initHandler(){
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.GET_USER_IMAGE_SUCCESS) {
                    Bitmap bitmap = (Bitmap) msg.obj;
                    setImageBitmap(bitmap);
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    Toast.makeText(getContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }


    public void setImageContent(byte[] bytes){
        Bitmap bitmap=null;
        if(bytes != null && bytes.length > 0){
            //复制新的输入流
            InputStream is = new ByteArrayInputStream(bytes);
            //调用压缩方法显示图片
            bitmap = getCompressBitmap(is);
            setImageBitmap(bitmap);
        }
    }

    //设置网络图片
    public void setImageURL(String path) {
        imagePath = path;
        if (isUseCache){
            useCacheImage();
        }else {
            useNetWorkImage();
        }
    }

    //使用网络图片显示
    public void useNetWorkImage(){
        //开启一个线程用于联网
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                OkHttpClientManager.getAsyn(imagePath,
                        new OkHttpClientManager.ResultCallback<BaseData>() {
                            @Override
                            public void onError(com.squareup.okhttp.Request request, Exception e) {
                                Message message = Message.obtain();
                                message.what = MessageUtil.EXCUTE_EXCEPTION;
                                message.obj = e;
                                handler.sendMessage(message);
                            }
                            @Override
                            public void onResponse(BaseData data) {
                                Message msg = Message.obtain();
                                try {
                                    byte[] bytes = data.getFilebyte();
                                    Bitmap bitmap=null;
                                    if(bytes != null && bytes.length > 0){
                                        if (isUseCache){
                                            //复制新的输入流
                                            InputStream is = new ByteArrayInputStream(bytes);
                                            InputStream is2 = new ByteArrayInputStream(bytes);
                                            //调用压缩方法显示图片
                                            bitmap = getCompressBitmap(is);
                                            //调用缓存图片方法
                                            cacheImage(is2);
                                        }else {
                                            //调用压缩方法
                                            InputStream is = new ByteArrayInputStream(bytes);
                                            bitmap = getCompressBitmap(is);
                                        }
                                    }
                                    msg.obj = bitmap;
                                    msg.what = MessageUtil.GET_USER_IMAGE_SUCCESS;
                                } catch (Exception e) {
                                    msg.what = MessageUtil.EXCUTE_EXCEPTION;
                                    msg.obj = e;
                                }
                                handler.sendMessage(msg);
                            }
                        });
            }
        });
    }

    //使用缓存图片
    public void useCacheImage() {
        //创建路径一样的文件
        File dirFile = new File(getURLPath());
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                Log.e("TAG", "文件夹创建失败");
            } else {
                Log.e("TAG", "文件夹创建成功:" + dirFile.getPath());
            }
        }
        File file = new File(dirFile, getFileName());
        //判断文件是否存在
        if (file.exists() && file.length() > 0) {
            //使用本地图片
            try {
                InputStream inputStream = new FileInputStream(file);
                //调用压缩方法显示图片
                Bitmap bitmap = getCompressBitmap(inputStream);
                //利用Message把图片发给Handler
                Message msg = Message.obtain();
                msg.obj = bitmap;
                msg.what = MessageUtil.GET_USER_IMAGE_SUCCESS;
                handler.sendMessage(msg);
                Log.e("NetImageView","使用缓存图片");
            } catch (FileNotFoundException e) {
                Log.e("NetImageView", "没有找到图片");
            }
        }else {
            //使用网络图片
            useNetWorkImage();
            Log.e("NetImageView","使用网络图片");
        }
    }

    /**
     * 缓存网络的图片
     * @param inputStream 网络的输入流
     */
    public void cacheImage(InputStream inputStream) {
        try {
            //创建路径一样的文件
            File dirFile = new File(getURLPath());
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e("TAG", "文件夹创建失败");
                    Log.e("NetImageView","缓存失败");
                    return;
                } else {
                    Log.e("TAG", "文件夹创建成功:" + dirFile.getPath());
                }
            }
            File file = new File(dirFile, getFileName());

            FileOutputStream fos = new FileOutputStream(file);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            Log.e("NetImageView","缓存成功");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("NetImageView","缓存失败");
        }
    }

    /**
     * 根据网址生成一个文件名
     * @return 文件名
     */
    public String getURLPath() {
        return path;
    }

    public String getFileName(){
        return imageName+".jpg";
    }


    /**
     * 根据输入流返回一个压缩的图片
     *
     * @param input 图片的输入流
     * @return 压缩的图片
     */
    public Bitmap getCompressBitmap(InputStream input) {
        //因为InputStream要使用两次，但是使用一次就无效了，所以需要复制两个
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //复制新的输入流
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

        //只是获取网络图片的大小，并没有真正获取图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        //获取图片并进行压缩
        options.inSampleSize = getInSampleSize(options);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is2, null, options);
    }

    /**
     * 获得需要压缩的比率
     *
     * @param options 需要传入已经BitmapFactory.decodeStream(is, null, options);
     * @return 返回压缩的比率，最小为1
     */
    public int getInSampleSize(BitmapFactory.Options options) {
        int inSampleSize = 1;
        int realWith = realImageViewWith();
        int realHeight = realImageViewHeight();

        int outWidth = options.outWidth;
        Log.e("网络图片实际的宽度", String.valueOf(outWidth));
        int outHeight = options.outHeight;
        Log.e("网络图片实际的高度", String.valueOf(outHeight));

        //获取比率最大的那个
        if (outWidth > realWith || outHeight > realHeight) {
            int withRadio = Math.round(outWidth / realWith);
            int heightRadio = Math.round(outHeight / realHeight);
            inSampleSize = withRadio > heightRadio ? withRadio : heightRadio;
        }
        Log.e("压缩比率", String.valueOf(inSampleSize));
        return inSampleSize;
    }


    /**
     * 获取ImageView实际的宽度
     *
     * @return 返回ImageView实际的宽度
     */
    public int realImageViewWith() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();

        //如果ImageView设置了宽度就可以获取实在宽带
        int width = getWidth();
        if (width <= 0) {
            //如果ImageView没有设置宽度，就获取父级容器的宽度
            width = layoutParams.width;
        }
        if (width <= 0) {
            //获取ImageView宽度的最大值
            width = getMaxWidth();
        }
        if (width <= 0) {
            //获取屏幕的宽度
            width = displayMetrics.widthPixels;
        }
        Log.e("ImageView实际的宽度", String.valueOf(width));
        return width;
    }

    /**
     * 获取ImageView实际的高度
     *
     * @return 返回ImageView实际的高度
     */
    public int realImageViewHeight() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();

        //如果ImageView设置了高度就可以获取实在宽度
        int height = getHeight();
        if (height <= 0) {
            //如果ImageView没有设置高度，就获取父级容器的高度
            height = layoutParams.height;
        }
        if (height <= 0) {
            //获取ImageView高度的最大值
            height = getMaxHeight();
        }
        if (height <= 0) {
            //获取ImageView高度的最大值
            height = displayMetrics.heightPixels;
        }
        Log.e("ImageView实际的高度", String.valueOf(height));
        return height;
    }



    private int dp2px(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }
}

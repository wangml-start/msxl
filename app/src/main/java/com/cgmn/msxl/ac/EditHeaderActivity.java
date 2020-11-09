package com.cgmn.msxl.ac;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.cgmn.msxl.R;
import com.cgmn.msxl.application.GlobalTreadPools;
import com.cgmn.msxl.comp.CustmerToast;
import com.cgmn.msxl.comp.NetImageView;
import com.cgmn.msxl.comp.pop.PhotoPop;
import com.cgmn.msxl.handdler.GlobalExceptionHandler;
import com.cgmn.msxl.receiver.ReceiverMessage;
import com.cgmn.msxl.server_interface.BaseData;
import com.cgmn.msxl.service.GlobalDataHelper;
import com.cgmn.msxl.service.OkHttpClientManager;
import com.cgmn.msxl.service.PropertyService;
import com.cgmn.msxl.utils.CommonUtil;
import com.cgmn.msxl.utils.FileUtil;
import com.cgmn.msxl.utils.ImageUtil;
import com.cgmn.msxl.utils.MessageUtil;
import com.squareup.okhttp.Request;
import org.apache.shiro.codec.Base64;

import java.io.File;

public class EditHeaderActivity extends BaseActivity{
    private static final String TAG = EditHeaderActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE_GET = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_BIG_IMAGE_CUTTING = 3;
    private static final String IMAGE_FILE_NAME = "icon.jpg";
    private static int imageSide = 1200;

    private NetImageView main_icon;
    private PhotoPop mPhotoPopupWindow;
    private File cutFile;

    protected Handler mHandler;
    protected ProgressDialog dialog;


    @Override
    protected void onRightTextClick(){
        showPop();
    };

    private void showPop(){
        mPhotoPopupWindow = new PhotoPop(EditHeaderActivity.this, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 权限申请
                if (ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //权限还没有授予，需要在这里写申请权限的代码
                    ActivityCompat.requestPermissions(EditHeaderActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
                } else {
                    // 如果权限已经申请过，直接进行图片选择
                    mPhotoPopupWindow.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    // 判断系统中是否有处理该 Intent 的 Activity
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                    } else {
                        CustmerToast.makeText(mContext, "未找到图片查看器").show();
                    }
                }
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 权限申请
                if (ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // 权限还没有授予，需要在这里写申请权限的代码
                    ActivityCompat.requestPermissions(EditHeaderActivity.this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300);
                } else {
                    // 权限已经申请，直接拍照
                    mPhotoPopupWindow.dismiss();
                    imageCapture();
                }
            }
        });
        View rootView = LayoutInflater.from(mContext)
                .inflate(R.layout.activity_main, null);
        mPhotoPopupWindow.showAtLocation(rootView,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    protected void init() {
        initView();
        //清空缓存图片
        if(FileUtil.DeleteFolder(GlobalDataHelper.getPortraitCachePath())){
            Log.d(TAG, "Clear portrait catche!");
        }
        if(FileUtil.DeleteFolder(GlobalDataHelper.getCachePath())){
            Log.d(TAG, "Clear catche!");
        }
    }

    private void initView(){
        main_icon = findViewById(R.id.main_icon);
        main_icon.setImageName(GlobalDataHelper.getUserAcc(mContext));
        main_icon.setImageURL(GlobalDataHelper.getUserPortraitUrl(mContext));

        dialog = new ProgressDialog(mContext);
        dialog.setMessage("正在提交...");
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MessageUtil.REQUEST_SUCCESS) {
                    Intent intents = new Intent(ReceiverMessage.EDIT_COMPLETED);
                    intents.putExtra("resource", "modify");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intents);
                    dialog.cancel();
                    finish();
                } else if (msg.what == MessageUtil.EXCUTE_EXCEPTION) {
                    dialog.cancel();
                    GlobalExceptionHandler.getInstance(mContext).handlerException((Exception) msg.obj);
                }
                return false;
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.edit_upic_layout;
    }

    @Override
    protected String setTitle() {
        return getString(R.string.head_img);
    }

    @Override
    protected boolean showComplate(){
        return false;
    };

    /**
     * 处理回调结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 回调成功
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                // 大图切割
                case REQUEST_BIG_IMAGE_CUTTING:
                    byte[] bs = ImageUtil.getCompressBytes(cutFile.getAbsolutePath());
                    byte[] small = ImageUtil.getSmallBytes(cutFile.getAbsolutePath());
                    main_icon.setImageContent(bs);
                    uploadPortrait(bs, small);
                    break;
                // 相册选取
                case REQUEST_IMAGE_GET:
                    try {
                        startBigPhotoZoom(data.getData());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    break;
                // 拍照
                case REQUEST_IMAGE_CAPTURE:
                    File temp = new File(Environment.getExternalStorageDirectory() + "/" + IMAGE_FILE_NAME);
                    startBigPhotoZoom(temp);
            }
        }
    }

    /**
     * 处理权限回调结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPhotoPopupWindow.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    // 判断系统中是否有处理该 Intent 的 Activity
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                    } else {
                        Toast.makeText(mContext, "未找到图片查看器", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mPhotoPopupWindow.dismiss();
                }
                break;
            case 300:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPhotoPopupWindow.dismiss();
                    imageCapture();
                } else {
                    mPhotoPopupWindow.dismiss();
                }
                break;
        }
    }

    /**
     * 判断系统及拍照
     */
    private void imageCapture() {
        Intent intent;
        Uri pictureUri;
        File pictureFile = new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
        // 判断当前系统
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pictureUri = FileProvider.getUriForFile(this,
                    "com.cgmn.msxl.fileProvider", pictureFile);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri = Uri.fromFile(pictureFile);
        }
        // 去拍照
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * 大图模式切割图片
     * 直接创建一个文件将切割后的图片写入
     */
    public void startBigPhotoZoom(File inputFile) {
        // 创建大图文件夹
        Uri imageUri = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(GlobalDataHelper.getCachePath());
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e("TAG", "文件夹创建失败");
                } else {
                    Log.e("TAG", "文件夹创建成功:" + dirFile.getPath());
                }
            }
            cutFile = new File(dirFile, System.currentTimeMillis() + ".jpg");
            imageUri = Uri.fromFile(cutFile);
        }

        // 开始切割
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(getImageContentUri(EditHeaderActivity.this, inputFile), "image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1); // 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", imageSide); // 输出图片大小
        intent.putExtra("outputY", imageSide);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false); // 不直接返回数据
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // 返回一个文件
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, REQUEST_BIG_IMAGE_CUTTING);
    }

    public void startBigPhotoZoom(Uri uri) {
        // 创建大图文件夹
        Uri imageUri = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dirFile = new File(GlobalDataHelper.getCachePath());
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e("TAG", "文件夹创建失败");
                } else {
                    Log.e("TAG", "文件夹创建成功:" + dirFile.getPath());
                }
            }
            cutFile = new File(dirFile, System.currentTimeMillis() + ".jpg");
            imageUri = Uri.fromFile(cutFile);
        }

        // 开始切割
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1); // 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", imageSide); // 输出图片大小
        intent.putExtra("outputY", imageSide);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false); // 不直接返回数据
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // 返回一个文件
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, REQUEST_BIG_IMAGE_CUTTING);
    }

    public Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    private void uploadPortrait(final byte[] normal, final byte[] small){
        if(CommonUtil.isEmpty(normal) || CommonUtil.isEmpty(small)){
            return;
        }
        //提交操作
        dialog.show();
        GlobalTreadPools.getInstance(mContext).execute(new Runnable() {
            @Override
            public void run() {
                final String token = GlobalDataHelper.getToken(mContext);
                OkHttpClientManager.Param[] params = new OkHttpClientManager.Param[]{
                        new OkHttpClientManager.Param("token", token),
                        new OkHttpClientManager.Param("normal", Base64.encodeToString(normal)),
                        new OkHttpClientManager.Param("small", Base64.encodeToString(small))
                };
                String url = String.format("%s%s",
                        PropertyService.getInstance().getKey("serverUrl"), "/user/upload_portrait");
                try {
                    OkHttpClientManager.postAsyn(url,
                            new OkHttpClientManager.ResultCallback<BaseData>() {
                                @Override
                                public void onError(Request request, Exception e) {
                                    Log.d(TAG, "UPLOAD TRADING FAILED!");
                                    Message message = Message.obtain();
                                    message.what = MessageUtil.EXCUTE_EXCEPTION;
                                    message.obj = e;
                                    mHandler.sendMessage(message);
                                }
                                @Override
                                public void onResponse(BaseData data) {
                                    Message message = Message.obtain();
                                    message.what = MessageUtil.REQUEST_SUCCESS;
                                    try {
                                        Integer status = data.getStatus();
                                        if (status == null || status == -1) {
                                            Log.d(TAG, "UPLOAD TRADING FAILED!");
                                            throw new Exception(data.getError());
                                        }
                                        Log.d(TAG, "UPLOAD TRADING SUCCESS!");
                                    } catch (Exception e) {
                                        message.what = MessageUtil.EXCUTE_EXCEPTION;
                                        message.obj = e;
                                    }
                                    mHandler.sendMessage(message);
                                }
                            },
                            params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e(TAG,"NAME="+Thread.currentThread().getName());
            }
        });
    }


    protected void onBackUpClick(){
        dialog.cancel();
        dialog.dismiss();
        finish();
    };

}
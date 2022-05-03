package com.cgmn.msxl.utils;

import android.content.Context;
import android.media.MediaPlayer;

import com.cgmn.msxl.R;
import com.cgmn.msxl.db.AppSqlHelper;

import java.util.Map;

public class PlayVoiceUtils {
    static PlayVoiceUtils entiy;
    private PlayVoiceUtils(){}
    String playVoiceSetting = null;

    public static PlayVoiceUtils getInstance(){
        if(entiy == null){
            entiy = new PlayVoiceUtils();
        }
        return entiy;
    }
    public void PlayMusic(Context mContxt) {
        AppSqlHelper dbHelper = new AppSqlHelper(mContxt);
        Map<String, String> map =  dbHelper.getSystenSettings();
        if(map.get("PLAY_VOICE") == null || "0".equals(map.get("PLAY_VOICE"))){
            final MediaPlayer music = MediaPlayer.create(mContxt, R.raw.btn_wav);
            music.start();
            // 播放完成可以释放资源
            music.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (music != null) {
                        music.release();
                    }
                }
            });
        }
    }


    private void releaseMediaPlayer() {

    }
}

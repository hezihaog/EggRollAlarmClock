package com.zh.android.eggrollalarmclock.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import java.io.IOException;

public class MusicPlayer {
    private volatile static MusicPlayer INSTANCE;
    private MediaPlayer mMediaPlayer;

    private MusicPlayer() {
    }

    public static MusicPlayer getInstance() {
        if (INSTANCE == null) {
            synchronized (MusicPlayer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MusicPlayer();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 播放音频
     *
     * @param assertMusicFileName assert目录下的音频文件名
     */
    public void play(Context context, String assertMusicFileName, boolean isLoop) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        try {
            //打开Asset目录
            AssetManager assetManager = context.getAssets();
            //打开音乐文件
            AssetFileDescriptor fileDescriptor = assetManager.openFd(assertMusicFileName);
            mMediaPlayer.reset();
            //是否循环
            mMediaPlayer.setLooping(isLoop);
            //设置媒体播放器的数据源
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }
}
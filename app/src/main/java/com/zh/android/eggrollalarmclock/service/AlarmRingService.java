package com.zh.android.eggrollalarmclock.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zh.android.eggrollalarmclock.R;
import com.zh.android.eggrollalarmclock.activity.AlarmShowActivity;
import com.zh.android.eggrollalarmclock.util.MusicPlayer;

import java.text.SimpleDateFormat;

/**
 * 闹铃
 */
public class AlarmRingService extends Service {
    private static final String STARTUP_ACTION = "startup_action";
    private static final String STOP_ACTION = "stop_action";

    private static final String CHANNEL_ID = "Alarm";
    private static final String CHANNEL_NAME = "闹钟通知";

    private static final int NOTIFICATION_ID = 10;

    private Vibrator mVibrator;

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, AlarmRingService.class);
        intent.setAction(AlarmRingService.STARTUP_ACTION);
        return intent;
    }

    public static Intent getStopIntent(Context context) {
        Intent intent = new Intent(context, AlarmRingService.class);
        intent.setAction(AlarmRingService.STOP_ACTION);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            if (STARTUP_ACTION.equals(intent.getAction())) {
                //到达闹钟时间
                Intent jumpIntent = new Intent(this, AlarmShowActivity.class);
                jumpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //跳转闹钟页面
                startActivity(jumpIntent);
                //显示通知
                showHighNotification(new Intent(jumpIntent), this);
                //播放闹铃
                playMusic();
                //开始振动
                startVibrate();
            } else if (STOP_ACTION.equals(intent.getAction())) {
                //取消通知
                cancelNotification();
                //停止播放
                stopMusic();
                //取消振动
                cancelVibrate();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 播放闹铃
     */
    private void playMusic() {
        MusicPlayer.getInstance().play(
                getApplicationContext(),
                "radar.mp3",
                true
        );
    }

    /**
     * 停止播放闹铃
     */
    private void stopMusic() {
        MusicPlayer.getInstance().stop();
    }

    /**
     * 显示通知栏的通知
     */
    private void showHighNotification(Intent intent, Context context) {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(false);
            channel.setSound(null, null);
        }
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        String title = getCurrentHourMinuteTimeFormatStr() + "，闹钟时间到";
        String content = "点我前往关闭闹钟";
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true).build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * 取消通知栏
     */
    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager)
                getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        stopForeground(true);
    }

    /**
     * 开始振动
     */
    protected void startVibrate() {
        if (mVibrator == null) {
            mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        }
        //振动时间，奇数位为振动时间，偶数为暂停时间，单位为毫秒
        long[] pattern = {100, 2000, 1000, 1000, 3000};
        //0为重复振动，-1为只振动一遍
        mVibrator.vibrate(pattern, 0);
    }

    /**
     * 取消振动
     */
    protected void cancelVibrate() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    /**
     * 获取（小时:分钟）格式化后的时间文本
     */
    @SuppressLint("SimpleDateFormat")
    protected String getCurrentHourMinuteTimeFormatStr() {
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(currentTimeMillis);
    }
}
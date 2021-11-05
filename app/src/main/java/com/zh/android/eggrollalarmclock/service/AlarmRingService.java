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
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zh.android.eggrollalarmclock.R;
import com.zh.android.eggrollalarmclock.activity.AlarmShowActivity;
import com.zh.android.eggrollalarmclock.util.MusicPlayer;

/**
 * 闹铃
 */
public class AlarmRingService extends Service {
    public static final String STARTUP_ACTION = "com.zh.android.eggrollalarmclock.action.alarmservice";
    public static final String STOP_ACTION = "com.zh.android.eggrollalarmclock.action.alarmservice.stop";

    private static final String CHANNEL_ID = "Alarm";
    private static final String CHANNEL_NAME = "AlarmRingService";

    private static final int NOTIFICATION_ID = 10;

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
                Intent jumpIntent = new Intent(this, AlarmShowActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //跳跳转闹钟页面
                startActivity(jumpIntent);
                //再显示通知
                showHighNotification(jumpIntent, this);
                //播放闹铃
                MusicPlayer.getInstance().play(
                        getApplicationContext(),
                        "alarm_duopule.mp3",
                        true
                );
            } else if (STOP_ACTION.equals(intent.getAction())) {
                //取消闹钟通知栏
                cancelNotification();
                stopForeground(true);
                stopSelf();
            }
        }
        return super.onStartCommand(intent, flags, startId);
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
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("蛋卷闹钟 唤醒服务")
                .setContentText("唤醒正在响起，点击查看详情")
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
    }
}
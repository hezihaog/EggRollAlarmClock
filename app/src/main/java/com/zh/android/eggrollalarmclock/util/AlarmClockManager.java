package com.zh.android.eggrollalarmclock.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zh.android.eggrollalarmclock.service.AlarmRingService;
import com.zh.android.minihandler.MiniHandler;
import com.zh.android.minihandler.MiniHandlerThread;

import java.util.concurrent.TimeUnit;

public class AlarmClockManager {
    /**
     * 单实例
     */
    private volatile static AlarmClockManager INSTANCE;
    /**
     * 定时策略
     */
    private final CountdownStrategy mCountdownStrategy = new MiniHandlerStrategy();

    private AlarmClockManager() {
    }

    public static AlarmClockManager getInstance() {
        if (INSTANCE == null) {
            synchronized (AlarmClockManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AlarmClockManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 设置一个闹钟
     */
    public void setAlarmClock(Context context, long time) {
        mCountdownStrategy.setAlarmClock(context, time);
    }

    /**
     * 获取到达闹钟设置时间时，要做的事情的Intent
     */
    private static Intent getAlarmArriveIntent(Context context) {
        Intent intent = new Intent();
        intent.setAction(AlarmRingService.STARTUP_ACTION);
        intent.setPackage(context.getPackageName());
        return intent;
    }

    /**
     * 定时策略
     */
    private abstract static class CountdownStrategy {
        /**
         * 设置一个闹钟
         *
         * @param time 设置的闹钟时间
         */
        protected abstract void setAlarmClock(Context context, long time);
    }

    /**
     * AlarmManager实现的定时服务
     */
    private static class AlarmManagerStrategy extends CountdownStrategy {
        @Override
        public void setAlarmClock(Context context, long time) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
            Intent alarmArriveIntent = getAlarmArriveIntent(context);
            if (time > 0) {
                PendingIntent pendingIntent = PendingIntent.getService(context, 0, alarmArriveIntent, 0);
                alarmManager.cancel(pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                }
            } else {
                alarmManager.cancel(PendingIntent.getService(context, 0, alarmArriveIntent, 0));
            }
        }
    }

    /**
     * MiniHandler实现
     */
    private static class MiniHandlerStrategy extends CountdownStrategy {
        private MiniHandler mEventHandler;
        private MiniHandlerThread mHandlerThread;

        @Override
        public void setAlarmClock(Context context, long time) {
            if (mHandlerThread == null) {
                mHandlerThread = new MiniHandlerThread("alarm-clock-handler-thread");
                mHandlerThread.start();
            }
            if (mEventHandler == null) {
                mEventHandler = new MiniHandler(mHandlerThread.getLooper());
            }
            startCountdownTimer(context, time);
        }

        /**
         * 开启定时器
         *
         * @param time 到期时间
         */
        private void startCountdownTimer(Context context, long time) {
            mEventHandler.removeCallbacksAndMessages();
            mEventHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //精确到秒值即可
                    boolean isArrive = (time / 1000) - (System.currentTimeMillis() / 1000) == 0;
                    //到时间了
                    if (isArrive) {
                        Intent alarmArriveIntent = getAlarmArriveIntent(context);
                        context.startService(alarmArriveIntent);
                    } else {
                        //没到时间，继续倒计时
                        mEventHandler.postDelayed(this, 1000);
                    }
                }
            }, 1000);
        }
    }

    /**
     * WorkManager实现
     */
    private static class WorkManagerStrategy extends CountdownStrategy {
        private static final String UNIQUE_WORK_NAME = "UNIQUE_WORK_NAME_ALARM";

        @Override
        protected void setAlarmClock(Context context, long time) {
            //开启之前，先取消之前的任务
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME);
            //新建任务
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(AlarmClockWork.class)
                    //延迟多久执行，目标时间 - 当前时间
                    .setInitialDelay(time - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .build();
            //任务加入队列
            WorkManager.getInstance(context)
                    .beginUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
                    .enqueue();
        }

        public static final class AlarmClockWork extends Worker {
            public AlarmClockWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
                super(context, workerParams);
            }

            @NonNull
            @Override
            public Result doWork() {
                Context context = getApplicationContext();
                Intent intent = getAlarmArriveIntent(context);
                context.startService(intent);
                return Result.success();
            }
        }
    }
}
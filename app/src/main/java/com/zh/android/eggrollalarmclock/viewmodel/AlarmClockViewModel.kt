package com.zh.android.eggrollalarmclock.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.zh.android.eggrollalarmclock.util.AlarmClockManager

class AlarmClockViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * 选择的闹钟时间
     */
    val selectAlarmDate = MutableLiveData<Long>()

    /**
     * 设置闹钟
     *
     * @param time 闹钟响起时间
     */
    fun setAlarmClock(time: Long) {
        AlarmClockManager.getInstance().setAlarmClock(getApplication(), time)
    }
}
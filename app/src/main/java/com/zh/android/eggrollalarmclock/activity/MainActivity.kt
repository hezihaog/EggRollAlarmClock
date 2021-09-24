package com.zh.android.eggrollalarmclock.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.zh.android.eggrollalarmclock.R
import com.zh.android.eggrollalarmclock.databinding.ActivityMainBinding
import com.zh.android.eggrollalarmclock.viewmodel.AlarmClockViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    @SuppressLint("SimpleDateFormat")
    private val mTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

    private lateinit var mBinding: ActivityMainBinding

    private val mViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(getActivity().application)
        ).get(AlarmClockViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        bindView()
        setData()
    }

    private fun bindView() {
        mBinding.pickerTimeBtn.setOnClickListener {
            val activity = getActivity()
            val calendar = Calendar.getInstance().apply {
                time = Date()
            }
            TimePickerBuilder(activity, OnTimeSelectListener { selectDate, _ ->
                val calendarInstance = Calendar.getInstance().apply {
                    time = Date(selectDate.time)
                    //去掉秒位和毫位
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val time = calendarInstance.timeInMillis
                setAlarmClock(time)
            })
                //处理有些机子的NavigationBar虚拟键在DecorView里面，导致和弹窗重合的问题
                .setDecorView(activity.findViewById(android.R.id.content))
                //限制只能选择今天以后的时间
                .setRangDate(calendar, null)
                //设置一开始选择处于的时间
                .setDate(calendar)
                //配置可选 年、月、日，现在需要：年、月、日、时、分
                .setType(booleanArrayOf(true, true, true, true, true, false))
                .setSubmitColor(ActivityCompat.getColor(activity, R.color.purple_500))
                .setCancelColor(ActivityCompat.getColor(activity, R.color.purple_500))
                .setTitleColor(ActivityCompat.getColor(activity, R.color.black))
                .setTitleText("请选择闹钟时间")
                //禁止循环
                .isCyclic(false)
                .build()
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setData() {
        mViewModel.selectAlarmDate.observe(this, {
            mBinding.selectAlarmTime.text = "选择的时间：${mTimeFormat.format(it)}"
        })
    }

    /**
     * 设置闹钟
     */
    private fun setAlarmClock(time: Long) {
        //保存选择的时间
        mViewModel.selectAlarmDate.value = time
        //设置闹钟
        mViewModel.setAlarmClock(time)
        toast("设置成功，" + getAlarmTimeString(time) + "唤醒你")
    }

    @SuppressLint("SimpleDateFormat")
    private fun getAlarmTimeString(time: Long): String {
        val dateFormat = SimpleDateFormat("HH:mm")
        return dateFormat.format(time)
    }

    /**
     * Toast
     */
    private fun toast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    private fun getActivity(): Activity {
        return this
    }
}
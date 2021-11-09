package com.zh.android.eggrollalarmclock.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.zh.android.eggrollalarmclock.databinding.ActivityAlarmShowBinding
import com.zh.android.eggrollalarmclock.service.AlarmRingService

/**
 * 闹铃页面
 */
class AlarmShowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAlarmShowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //返回键
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        //关闭闹钟
        binding.closeAlarm.setOnClickListener {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        //通知服务，关闭闹钟
        startService(AlarmRingService.getStopIntent(this))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
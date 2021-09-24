package com.zh.android.eggrollalarmclock.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.zh.android.eggrollalarmclock.databinding.ActivityAlarmShowBinding
import com.zh.android.eggrollalarmclock.service.AlarmRingService
import com.zh.android.eggrollalarmclock.util.MusicPlayer

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
        //取消闹铃通知栏
        startService(Intent(this, AlarmRingService::class.java).apply {
            action = AlarmRingService.STOP_ACTION
        })
        //关闭闹钟
        binding.closeAlarm.setOnClickListener {
            pauseMusic()
            finish()
        }
    }

    override fun finish() {
        super.finish()
        pauseMusic()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 停止播放音乐
     */
    private fun pauseMusic() {
        MusicPlayer.getInstance().pause()
    }
}
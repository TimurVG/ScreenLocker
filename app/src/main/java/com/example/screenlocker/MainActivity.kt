package com.timurvg.screenlockerfinal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.timurvg.screenlockerfinal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchLock.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startService(Intent(this, LockService::class.java))
            } else {
                stopService(Intent(this, LockService::class.java))
            }
        }
    }
}
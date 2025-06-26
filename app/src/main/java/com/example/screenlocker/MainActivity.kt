package com.example.screenlocker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.screenlocker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchLock.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Включение блокировки экрана
                startService(LockService.getIntent(this))
            } else {
                // Выключение блокировки экрана
                stopService(LockService.getIntent(this))
            }
        }
    }
}
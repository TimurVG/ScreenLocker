package com.timurvg.screenlocker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.timurvg.screenlocker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchLocker.setOnCheckedChangeListener { _, isChecked ->
            val intent = Intent(this, LockService::class.java).apply {
                action = if (isChecked) "START" else "STOP"
            }
            if (isChecked) startService(intent) else stopService(intent)
        }
    }
}
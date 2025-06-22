package com.timurvg.screenlocker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.timurvg.screenlocker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchOnOff.setOnCheckedChangeListener { _, isChecked ->
            val serviceIntent = Intent(this, LockService::class.java)
            if (isChecked) {
                startService(serviceIntent)
            } else {
                stopService(serviceIntent)
            }
        }
    }
}
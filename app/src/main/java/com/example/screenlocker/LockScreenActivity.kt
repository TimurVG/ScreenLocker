package com.example.screenlocker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.screenlocker.databinding.ActivityLockScreenBinding

class LockScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLockScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
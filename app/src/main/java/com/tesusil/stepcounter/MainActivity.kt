package com.tesusil.stepcounter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.tesusil.stepcounter.databinding.ActivityMainBinding
import com.tesusil.stepcounter.view.MainBindingModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.model = MainBindingModel(2500,10000)
        binding.mainProgressBar.progress = 25
    }
}
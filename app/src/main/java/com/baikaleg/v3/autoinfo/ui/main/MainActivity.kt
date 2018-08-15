package com.baikaleg.v3.autoinfo.ui.main

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val viewModel: MainActivityModel = MainActivityModel.create(this)
        val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.viewmodel = viewModel
        binding.setLifecycleOwner(this)
    }
}

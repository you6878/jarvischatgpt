package com.howlab.jarvischatgpt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.howlab.jarvischatgpt.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private val binding by lazy { ActivityDetailBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }
}
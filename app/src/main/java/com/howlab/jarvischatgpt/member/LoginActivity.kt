package com.howlab.jarvischatgpt.member

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.howlab.jarvischatgpt.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


    }
}
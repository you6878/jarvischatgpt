package com.howlab.jarvischatgpt

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.howlab.jarvischatgpt.databinding.ActivityChatBinding
import com.howlab.jarvischatgpt.network.ChatRequest
import com.howlab.jarvischatgpt.network.CompletionResponse
import com.howlab.jarvischatgpt.network.OpenAiApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private val binding by lazy { ActivityChatBinding.inflate(layoutInflater) }

    private lateinit var api: OpenAiApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        api = OpenAiApi.getInstance()

        api.getCompletion2(ChatRequest()).enqueue(object : Callback<CompletionResponse> {
            override fun onResponse(
                call: Call<CompletionResponse>,
                response: Response<CompletionResponse>
            ) {
                Log.e("TESTEST", "success")

                if (response.isSuccessful) {
                    Log.e("TESTEST", "success")
                    response.body()?.let {
                        binding.resultTextView.text = it.toString()
                    }
                }
            }

            override fun onFailure(call: Call<CompletionResponse>, t: Throwable) {
                Log.e("TESTEST", "fail $t" , t)
            }
        })
    }
}
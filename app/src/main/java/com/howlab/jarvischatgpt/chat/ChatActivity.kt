package com.howlab.jarvischatgpt.chat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.howlab.jarvischatgpt.databinding.ChatmainBinding
import com.howlab.jarvischatgpt.network.ChatRequest
import com.howlab.jarvischatgpt.network.CompletionResponse
import com.howlab.jarvischatgpt.network.OpenAiApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private val binding by lazy { ChatmainBinding.inflate(layoutInflater) }

    private lateinit var api: OpenAiApi

    private val chatAdapter by lazy { ChatAdapter() }

    private val chats = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        api = OpenAiApi.getInstance()

        binding.recyclerGroupChat.adapter = chatAdapter
        binding.recyclerGroupChat.itemAnimator = null

        chatAdapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    binding.recyclerGroupChat.scrollToPosition(chats.size - 1)
                }
            }
        )

        binding.buttonGroupChatEmotion.setOnClickListener {
            if (binding.edittextGroupChatMessage.text.isEmpty()) {
                return@setOnClickListener
            }

            addChat(binding.edittextGroupChatMessage.text.toString())
        }
    }


    private fun addChat(message: String) {
        chats.add(ChatMessage.user(message))
        binding.edittextGroupChatMessage.text.clear()
        chatAdapter.submitList(chats.toMutableList())

        // api 요청
        api.getCompletion2(ChatRequest(message)).enqueue(object : Callback<CompletionResponse> {
            override fun onResponse(
                call: Call<CompletionResponse>,
                response: Response<CompletionResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        chats.add(ChatMessage.ai(it.result))
                        chatAdapter.submitList(chats.toMutableList())
                    }
                }
            }

            override fun onFailure(call: Call<CompletionResponse>, t: Throwable) {
                Log.e("TESTEST", "fail $t", t)
            }
        })
    }
}
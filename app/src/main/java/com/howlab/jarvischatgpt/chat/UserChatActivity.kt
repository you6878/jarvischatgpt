package com.howlab.jarvischatgpt.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.howlab.jarvischatgpt.databinding.ActivityUserChatBinding
import com.howlab.jarvischatgpt.network.ChatRequest
import com.howlab.jarvischatgpt.network.CompletionResponse
import com.howlab.jarvischatgpt.network.OpenAiApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserChatActivity : AppCompatActivity() {

    private val binding by lazy { ActivityUserChatBinding.inflate(layoutInflater) }

    private val chatAdapter by lazy {
        ChatAdapter {}
    }

    private lateinit var api: OpenAiApi


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
        chatAdapter.submitList(chats.toMutableList())

        lifecycleScope.launch {
            delay(3000)
            when {
                message.contains("안녕") -> {
                    chats.add(ChatMessage.ai("안녕하세요"))
                    chatAdapter.submitList(chats.toMutableList())
                }

                message.contains("구매") -> {
                    chats.add(ChatMessage.ai("판매하겠습니다!"))
                    chatAdapter.submitList(chats.toMutableList())
                }

                message.contains("네고") -> {
                    chats.add(ChatMessage.ai("네고는 거절하겠습니다 죄송합니다 ㅠ"))
                    chatAdapter.submitList(chats.toMutableList())
                }
            }
        }

        binding.edittextGroupChatMessage.text.clear()
    }

    private fun apiRequest() {
        api.getCompletion2(ChatRequest(binding.edittextGroupChatMessage.text.toString()))
            .enqueue(object : Callback<CompletionResponse> {
                override fun onResponse(
                    call: Call<CompletionResponse>,
                    response: Response<CompletionResponse>
                ) {
                    if (response.isSuccessful) {
                        chats.add(ChatMessage.ai(response.body()?.result.orEmpty()))
                        chatAdapter.submitList(chats.toMutableList())
                    } else {
                        chats.add(ChatMessage.ai("실패했습니다."))
                        chatAdapter.submitList(chats.toMutableList())
                    }
                }

                override fun onFailure(call: Call<CompletionResponse>, t: Throwable) {
                    chats.add(ChatMessage.ai("실패했습니다."))
                    chatAdapter.submitList(chats.toMutableList())
                }
            })
    }
}
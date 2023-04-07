package com.howlab.jarvischatgpt.chat

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.howlab.jarvischatgpt.AiState
import com.howlab.jarvischatgpt.databinding.ChatmainBinding
import com.howlab.jarvischatgpt.network.ChatRequest
import com.howlab.jarvischatgpt.network.CompletionResponse
import com.howlab.jarvischatgpt.network.OpenAiApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private val binding by lazy { ChatmainBinding.inflate(layoutInflater) }

    private lateinit var api: OpenAiApi

    private val chatAdapter by lazy {
        ChatAdapter { chat ->
            val snackbar = Snackbar.make(binding.root, "적용하시겠습니까?", Snackbar.LENGTH_SHORT)

            snackbar.setAction("확인") {
                val intent = Intent()
                intent.putExtra("KEY_CHAT", chat.message)
                setResult(500, intent)
                finish()
            }

            snackbar.setActionTextColor(Color.parseColor("#ccff00"))

            snackbar.show()
        }
    }

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

        apiRequest()
        binding.edittextGroupChatMessage.text.clear()
    }

    private fun apiRequest() {
        api.getCompletionForm(ChatRequest(binding.edittextGroupChatMessage.text.toString()))
            .enqueue(object : Callback<CompletionResponse> {
                override fun onResponse(
                    call: Call<CompletionResponse>,
                    response: Response<CompletionResponse>
                ) {
                    if (response.isSuccessful) {
                        chats.add(ChatMessage.gpt(response.body()?.result.orEmpty()))
                        chatAdapter.submitList(chats.toMutableList())
                    } else {
                        chats.add(ChatMessage.gpt("실패했습니다."))
                        chatAdapter.submitList(chats.toMutableList())
                    }
                }

                override fun onFailure(call: Call<CompletionResponse>, t: Throwable) {
                    chats.add(ChatMessage.gpt("실패했습니다."))
                    chatAdapter.submitList(chats.toMutableList())
                }
            })
    }
}

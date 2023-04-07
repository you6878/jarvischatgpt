package com.howlab.jarvischatgpt

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.howlab.jarvischatgpt.chat.ChatAdapter
import com.howlab.jarvischatgpt.chat.ChatMessage
import com.howlab.jarvischatgpt.databinding.ActivityChatPriceBinding
import com.howlab.jarvischatgpt.network.ChatRequest
import com.howlab.jarvischatgpt.network.CompletionResponse
import com.howlab.jarvischatgpt.network.OpenAiApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatPriceActivity : AppCompatActivity() {

    private val binding by lazy { ActivityChatPriceBinding.inflate(layoutInflater) }

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

    val firestore = Firebase.firestore

    private fun apiRequest() {
        firestore.collection("")
    }
}
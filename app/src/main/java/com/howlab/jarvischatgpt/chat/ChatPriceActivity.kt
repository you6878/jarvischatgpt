package com.howlab.jarvischatgpt.chat

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.howlab.jarvischatgpt.databinding.ActivityChatPriceBinding
import com.howlab.jarvischatgpt.network.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class ChatPriceActivity : AppCompatActivity() {

    private val binding by lazy { ActivityChatPriceBinding.inflate(layoutInflater) }

    private lateinit var api: OpenAiApi

    var avgPrice = 0

    private val chatAdapter by lazy {
        ChatAdapter { chat ->
            val snackbar = Snackbar.make(binding.root, "평균가를 적용하시겠습니까?", Snackbar.LENGTH_SHORT)

            snackbar.setAction("확인") {
                val intent = Intent()
                intent.putExtra("KEY_CHAT", avgPrice.toString())
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

        intent.extras?.getString("KEY_NAME")?.let {
            addChat(it)
        }
    }

    private fun addChat(message: String) {
        chats.add(ChatMessage.user(message + "가격 알려줘"))
        chatAdapter.submitList(chats.toMutableList())

        apiRequest(message)
    }

    val firestore = Firebase.firestore

    private fun apiRequest(message: String) {
        val query = message
        lifecycleScope.launch {
            val price = runCatching {
                firestore.collection("PRODUCTPRICE")
                    .whereEqualTo("name", query)
                    .get()
                    .await()
            }.getOrDefault(emptyList())

            val history = runCatching {
                firestore.collection("EXCHANGEHISTORY")
                    .whereEqualTo("name", query)
                    .get()
                    .await()
            }.getOrDefault(emptyList())

            var name = ""
            var min = 0
            var avg = 0
            var max = 0

            price.forEach {
                val p = it.toObject(Price::class.java)
                name = p.name
                min = p.min_price
                avg = p.avg_price
                max = p.max_price

                avgPrice = p.avg_price
            }

            var date = ""
            var exchangePrice = 0
            history.forEach {
                val h = it.toObject(History::class.java)
                date = h.date
                exchangePrice = h.price
            }

            val input = "$name|${formatWithComma(min)}|${formatWithComma(avg)}|${formatWithComma(max)}|${exchangePrice}|$date"
            println("input ${input}")
            api.getCompletionPrice(ChatRequest(input))
                .enqueue(object : Callback<CompletionResponse> {
                    override fun onResponse(
                        call: Call<CompletionResponse>,
                        response: Response<CompletionResponse>
                    ) {

                        if (response.isSuccessful) {
                            response.body()?.let {
                                chats.add(ChatMessage.gpt(it.result))
                                chatAdapter.submitList(chats.toMutableList())
                            }
                        } else {
                            Toast.makeText(baseContext, "실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CompletionResponse>, t: Throwable) {
                        Toast.makeText(baseContext, "실패", Toast.LENGTH_SHORT).show()
                    }
                })

        }
    }
    fun formatWithComma(number: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return formatter.format(number)
    }

}
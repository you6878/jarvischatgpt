package com.howlab.jarvischatgpt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.howlab.jarvischatgpt.chat.ChatAdapter
import com.howlab.jarvischatgpt.chat.ChatMessage
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
        binding.recommendNearStation.setOnClickListener {
            val station = "강남역"
            val command = "${station} 출구에 가까이있는 카페좀 알려줘"

        }
        binding.recommendPlaceTime.setOnClickListener {
            val meDay = "4월 7일, 4월9일"
            val mePlace = "2호선 강남역"
            var otherDay ="4월 9일, 4월 11일"
            var otherPlace ="2호선 잠실역"

            //나는 4월 7일, 4월9일 상대방은 4월 9일 4월 11일 거래 만남가능한 공통된 날짜만 말해주고 공통되지 않는 날짜는 생략하고 2호선 강남역 2호선 잠실역 중간에 있는 지하철역을 알려주는데 여기서 만나야되는 이유를 100자 내외로 설명해줘
            val command = "나는 ${meDay} 상대방은 ${otherDay} 거래 만남가능한 공통된 날짜만 말해주고 공통되지 않는 날짜는 생략하고 ${mePlace} ${otherPlace} 중간에 있는 지하철역을 알려주는데 여기서 만나야되는 이유를 100자 내외로 설명해줘"
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
                    chats.add(ChatMessage.ai("판매하겠습니다!"))
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
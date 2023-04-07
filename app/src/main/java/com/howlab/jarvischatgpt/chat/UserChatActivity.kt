package com.howlab.jarvischatgpt.chat

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.howlab.jarvischatgpt.databinding.ActivityUserChatBinding
import com.howlab.jarvischatgpt.network.ChatRequest
import com.howlab.jarvischatgpt.network.ChatResponse
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

    var meDay = ""
    var mePlace = ""

    var otherDay = ""
    var otherPlace = ""

    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val otherUid = intent.extras?.getString("UID")
        otherUid?.let {
            firestore.collection("USER")
                .document(it)
                .get()
                .addOnSuccessListener {
//                    it.getString("stationLine")?.let {
//                        otherPlace = it
//                    }

                    it.getString("station")?.let {
                        otherPlace += it
                    }

                    it.getString("tradeAt")?.let {
                        otherDay = it
                    }
                }
        }

        firestore.collection("USER")
            .document(Firebase.auth.uid.orEmpty())
            .get()
            .addOnSuccessListener {
//                it.getString("stationLine")?.let {
//                    mePlace = it
//                }

                it.getString("station")?.let {
                    mePlace += it
                }

                it.getString("tradeAt")?.let {
                    meDay = it
                }
            }

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
            val command = "$mePlace 출구에 가까이있는 커피숍이랑 거리도 알려줘"
            apiRequestNear(command)
        }

        binding.recommendPlaceTime.setOnClickListener {
            //나는 4월 7일, 4월9일 상대방은 4월 9일 4월 11일 거래 만남가능한 공통된 날짜만 말해주고 공통되지 않는 날짜는 생략하고 2호선 강남역 2호선 잠실역 중간에 있는 지하철역을 알려주는데 여기서 만나야되는 이유를 100자 내외로 설명해줘
            val command =
                "나는 $meDay 상대방은 $otherDay 거래 만남가능한 공통된 날짜만 말해주고 공통되지 않는 날짜는 생략하고 ${mePlace}이랑 $otherPlace 중간에 있는 지하철역을 알려주는데 여기서 만나야되는 이유를 100자 내외로 설명해줘"

            val stationMent = "${mePlace}과 $otherPlace 중간지점 지하철역 알려줘"
            apiRequestStation(stationMent)
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

    private fun apiRequestStation(message: String) {
        api.getCompletionNearStation(ChatRequest(message))
            .enqueue(object : Callback<ChatRes> {
                override fun onResponse(
                    call: Call<ChatRes>,
                    response: Response<ChatRes>
                ) {
                    if (response.isSuccessful) {
                        val first = response.body()?.result?.content.orEmpty()

                        val dayMent = "나는 $meDay 상대방은 $otherDay 거래 만남가능한 공통된 날짜만 말해줘"

                        api.getCompletionNearStation(ChatRequest(dayMent))
                            .enqueue(object : Callback<ChatRes> {
                                override fun onResponse(
                                    call: Call<ChatRes>,
                                    response: Response<ChatRes>
                                ) {
                                    if (response.isSuccessful) {
                                        val second = response.body()?.result?.content.orEmpty()
                                        chats.add(ChatMessage.gpt("$first\n$second"))
                                        chatAdapter.submitList(chats.toMutableList())
                                    } else {
                                        Toast.makeText(
                                            baseContext,
                                            "${response.errorBody()}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onFailure(call: Call<ChatRes>, t: Throwable) {
                                    Toast.makeText(baseContext, "${t.message}", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            })
                    } else {
                        Toast.makeText(baseContext, "${response.errorBody()}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<ChatRes>, t: Throwable) {
                    Toast.makeText(baseContext, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun apiRequestNear(message: String) {
        api.getCompletionNearStation(ChatRequest(message))
            .enqueue(object : Callback<ChatRes> {
                override fun onResponse(
                    call: Call<ChatRes>,
                    response: Response<ChatRes>
                ) {
                    if (response.isSuccessful) {
                        val first = response.body()?.result?.content.orEmpty()

                        val second = response.body()?.result?.content.orEmpty()
                        chats.add(ChatMessage.gpt("$first\n$second"))
                        chatAdapter.submitList(chats.toMutableList())
                    } else {
                        Toast.makeText(baseContext, "${response.errorBody()}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<ChatRes>, t: Throwable) {
                    Toast.makeText(baseContext, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
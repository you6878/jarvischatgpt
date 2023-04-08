package com.howlab.jarvischatgpt.network

import com.howlab.jarvischatgpt.chat.ChatRes
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class CompletionResponse(
    val choices: List<Choice>,
    val message: String,
    val result: String
)

data class ChatResponse(
    val content: String
)

data class Choice(val text: String)

data class History(
    val date: String = "",
    val name: String = "",
    val price: Int = 0
)

data class ChatRequest(
//    val model: String = "text-davinci-003",
//    val prompt: String = "인사해줘",
//    val max_tokens: Int = 4000,
//    val temperature: Int = 0
    val inputData: String
)

interface OpenAiApi {

    @Headers("Authorization: Bearer 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")
    @POST("api/generate_chat")
    fun getCompletionNearStation(@Body requestBody: ChatRequest): Call<ChatRes>

    @Headers("Authorization: Bearer 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")
    @POST("api/generate_1")
    fun getCompletionPlaceTime(@Body requestBody: ChatRequest): Call<CompletionResponse>

    @Headers("Authorization: Bearer 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")
    @POST("api/generate_2")
    fun getCompletion2(@Body requestBody: ChatRequest): Call<CompletionResponse>

    @Headers("Authorization: Bearer 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")
    @POST("api/generate_form")
    fun getCompletionForm(@Body requestBody: ChatRequest): Call<CompletionResponse>

    @Headers("Authorization: Bearer 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")
    @POST("api/generate")
    fun getCompletionPrice(@Body requestBody: ChatRequest): Call<CompletionResponse>

    companion object {

        private var instance: OpenAiApi? = null
        private const val TIMEOUT_TIME = 60L

        fun getInstance(): OpenAiApi {
            if (instance == null) {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_TIME, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_TIME, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_TIME, TimeUnit.SECONDS)
                    .build()

                return Retrofit.Builder()
                    .baseUrl("http://43.200.49.199:3000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
                    .create(OpenAiApi::class.java)
            }

            return instance!!
        }
    }
}
// http://43.200.49.199:3000/api/generate
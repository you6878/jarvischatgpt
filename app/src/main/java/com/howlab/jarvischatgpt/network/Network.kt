package com.howlab.jarvischatgpt.network

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class CompletionResponse(val choices: List<Choice>)

data class Choice(val text: String)

data class ChatRequest(
    val model: String = "text-davinci-003",
    val prompt: String = "인사해줘",
    val max_tokens: Int = 4000,
    val temperature: Int = 0
)

interface OpenAiApi {
    @Headers("Authorization: Bearer My-Key")
    @POST("v1/completions")
    fun getCompletion2(@Body requestBody: ChatRequest): Call<CompletionResponse>

    companion object {

        private var instance: OpenAiApi? = null

        fun getInstance(): OpenAiApi {
            if (instance == null) {
                return Retrofit.Builder()
                    .baseUrl("https://api.openai.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(OpenAiApi::class.java)
            }

            return instance!!
        }
    }
}
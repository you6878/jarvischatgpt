package com.howlab.jarvischatgpt.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class CompletionResponse(
    val choices: List<Choice>,
    val message: String,
    val result: String
)

data class Choice(val text: String)

data class ChatRequest(
//    val model: String = "text-davinci-003",
//    val prompt: String = "인사해줘",
//    val max_tokens: Int = 4000,
//    val temperature: Int = 0
    val inputData: String
)

interface OpenAiApi {
    @Headers("Authorization: Bearer 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")
    @POST("api/generate")
    fun getCompletion2(@Body requestBody: ChatRequest): Call<CompletionResponse>

    companion object {

        private var instance: OpenAiApi? = null

        fun getInstance(): OpenAiApi {
            if (instance == null) {
                return Retrofit.Builder()
                    .baseUrl("http://43.200.49.199:3000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(OpenAiApi::class.java)
            }

            return instance!!
        }
    }
}
// http://43.200.49.199:3000/api/generate
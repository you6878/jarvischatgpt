package com.howlab.jarvischatgpt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.howlab.jarvischatgpt.chat.UserChatActivity
import com.howlab.jarvischatgpt.databinding.ActivityFeedBinding
import com.howlab.jarvischatgpt.network.Product
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FeedActivity : AppCompatActivity() {

    private val binding by lazy { ActivityFeedBinding.inflate(layoutInflater) }

    private val feedAdapter = FeedAdapter(onClick = {
        startActivity(Intent(baseContext, UserChatActivity::class.java).apply {
            putExtra("KEY", it.location)
            putExtra("UID", it.uid)
        })
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.recyclerView.adapter = feedAdapter

        lifecycleScope.launch {
            val products: List<Product> = Firebase.firestore.collection("PRODUCT")
                .get()
                .await()
                .map {
                    it.toObject()
                }

            feedAdapter.setProducts(products)
        }

        binding.setImage.setOnClickListener {
            startActivity(Intent(this, MyActivity::class.java))
        }

        binding.fab.setOnClickListener {
            startActivity(Intent(this, ProductRegisterActivity::class.java))
        }
    }
}
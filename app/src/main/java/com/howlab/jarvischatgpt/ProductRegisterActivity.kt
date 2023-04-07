package com.howlab.jarvischatgpt

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.howlab.jarvischatgpt.chat.ChatActivity
import com.howlab.jarvischatgpt.databinding.ActivityProductRegisterBinding
import com.howlab.jarvischatgpt.network.Product
import com.howlab.jarvischatgpt.network.StorageApi
import kotlinx.coroutines.launch

class ProductRegisterActivity : AppCompatActivity() {

    private val binding by lazy { ActivityProductRegisterBinding.inflate(layoutInflater) }

    private val photoListAdapter by lazy { PhotoListAdapter() }

    private val firestore = Firebase.firestore

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            val galleryUri = result.data?.data ?: return@registerForActivityResult
            photoListAdapter.addImage(galleryUri)
        }

    private val priceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.extras?.getString("KEY_CHAT")?.let {
            binding.descriptionText.setText(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.photoRecyclerView.adapter = photoListAdapter

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        supportFragmentManager.setFragmentResultListener("KEY_CONTENT", this) { _, bundle ->
            bundle.getString("KEY")?.let {
                priceLauncher.launch(Intent(baseContext, ChatActivity::class.java).apply {
                    putExtra("KEY", "CONTENT")
                })
            }
        }

        supportFragmentManager.setFragmentResultListener("KEY_PRICE", this) { _, bundle ->
            bundle.getString("KEY")?.let {
                priceLauncher.launch(Intent(baseContext, ChatActivity::class.java).apply {
                    putExtra("KEY", "PRICE")
                })
            }
        }

        binding.helperButton.setOnClickListener {
            AiSelectDialog().show(supportFragmentManager, null)
        }

        binding.addImageButton.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_PICK
            }
            launcher.launch(intent)
        }

        val api = StorageApi()

        val uid = Firebase.auth.uid.orEmpty()
        var location = ""

        firestore.collection("USER")
            .document(uid)
            .get()
            .addOnSuccessListener {
                it.getString("station")?.let {
                    location = it
                }
            }

        binding.productAdd.setOnClickListener {
            lifecycleScope.launch {
                val images = api.uploadImagesAsync(photoListAdapter.items).await()

                firestore.collection("PRODUCT")
                    .add(
                        Product(
                            title = binding.nameText.text.toString(),
                            location = location,
                            thumbnailImage = images.firstOrNull().orEmpty(),
                            price = binding.priceText.text.toString()
                        )
                    ).addOnSuccessListener {
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(baseContext, "상품 등록 실패", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}

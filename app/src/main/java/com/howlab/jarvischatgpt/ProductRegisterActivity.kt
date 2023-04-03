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
import com.howlab.jarvischatgpt.databinding.ActivityProductRegisterBinding
import com.howlab.jarvischatgpt.network.Product
import com.howlab.jarvischatgpt.network.StorageApi
import kotlinx.coroutines.launch

class ProductRegisterActivity : AppCompatActivity() {

    private val binding by lazy { ActivityProductRegisterBinding.inflate(layoutInflater) }

    private val photoListAdapter by lazy { PhotoListAdapter() }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->

        }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }

            val galleryUri = result.data?.data ?: return@registerForActivityResult
            photoListAdapter.addImage(galleryUri)
        }

    val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.photoRecyclerView.adapter = photoListAdapter

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
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
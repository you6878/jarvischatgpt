package com.howlab.jarvischatgpt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.howlab.jarvischatgpt.databinding.ActivityMyBinding

class MyActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMyBinding.inflate(layoutInflater) }

    val firestore = Firebase.firestore
    val auth = Firebase.auth

    var line = ""

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.getStringExtra("KEY_NAME")?.let {
            binding.stationEdit.setText(it + "역")
        }

        result.data?.getStringExtra("KEY_LINE")?.let {
            line = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore.collection("USER")
            .document(auth.currentUser?.uid.orEmpty())
            .get()
            .addOnSuccessListener {
                it.getString("station")?.let {
                    binding.stationEdit.setText(it)
                }

                it.getString("tradeAt")?.let {
                    binding.dateEdit.setText(it)
                }
            }

        binding.applyButton.setOnClickListener {
            firestore.collection("USER")
                .document(auth.currentUser?.uid.orEmpty())
                .update("station", "${binding.stationEdit.text}")

            firestore.collection("USER")
                .document(auth.currentUser?.uid.orEmpty())
                .update("stationLine", line)

            firestore.collection("USER")
                .document(auth.currentUser?.uid.orEmpty())
                .update("tradeAt", "${binding.dateEdit.text}")

            finish()
        }

        binding.stationModifyTextView.setOnClickListener {
            launcher.launch(Intent(this, StationActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                flags += Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}
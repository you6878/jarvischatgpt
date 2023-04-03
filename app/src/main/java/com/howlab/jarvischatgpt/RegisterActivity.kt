package com.howlab.jarvischatgpt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.howlab.jarvischatgpt.databinding.ActivityRegisterBinding
import com.howlab.jarvischatgpt.member.Member

class RegisterActivity : AppCompatActivity() {

    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }

    private val auth = Firebase.auth

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val station = it.data?.getStringExtra("KEY_NAME")

        station?.let {
            binding.selectStation.text = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.selectStation.setOnClickListener {
            launcher.launch(Intent(this, StationActivity::class.java))
        }

        binding.registerButton.setOnClickListener {
            auth.createUserWithEmailAndPassword(
                binding.emailEditText.text.toString(),
                binding.passwordEditText.text.toString()
            )
                .addOnSuccessListener {
                    Firebase.firestore.collection("USER")
                        .document(auth.currentUser?.uid.orEmpty())
                        .set(
                            Member(
                                auth.currentUser?.uid.orEmpty(),
                                binding.selectStation.text.toString()
                            )
                        )
                    Toast.makeText(baseContext, "회원 가입이 완료 되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(baseContext, "회원가입 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
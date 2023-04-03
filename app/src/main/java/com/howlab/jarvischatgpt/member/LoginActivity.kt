package com.howlab.jarvischatgpt.member

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.howlab.jarvischatgpt.FeedActivity
import com.howlab.jarvischatgpt.RegisterActivity
import com.howlab.jarvischatgpt.StationActivity
import com.howlab.jarvischatgpt.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private val auth = Firebase.auth

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val focusView: View? = currentFocus
        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            if (!rect.contains(x, y)) {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (auth.currentUser != null) {
            startActivity(Intent(this, StationActivity::class.java))
        }

        binding.loginButton.setOnClickListener {
            signIn()
        }

        binding.signUpLayout.setOnClickListener {
            signUp()
        }
    }

    fun signIn() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                startActivity(Intent(this, FeedActivity::class.java))
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "이메일 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    fun signUp() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}
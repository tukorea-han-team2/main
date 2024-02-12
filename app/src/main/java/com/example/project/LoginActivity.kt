package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.MainActivity
import com.example.project.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // FirebaseApp 초기화
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()

        // EditText 위젯들을 찾아 변수에 할당
        editTextEmail = findViewById(R.id.login_inputIdEdt)
        editTextPassword = findViewById(R.id.login_inputPwEdt)

        val loginButton = findViewById<Button>(R.id.login_loginBtn)
        loginButton.setOnClickListener {
            loginUser()
        }

        val joinButton = findViewById<Button>(R.id.login_joinBtn)
        joinButton.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

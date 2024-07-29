package com.example.cruddatabase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Default : AppCompatActivity() {
    private lateinit var login: Button
    private lateinit var register: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default)

        mAuth = FirebaseAuth.getInstance()

        login = findViewById(R.id.loginButton)
        register = findViewById(R.id.registerButton)

        login.setOnClickListener {
            startActivity(Intent(this@Default, Login::class.java))
        }
        register.setOnClickListener {
            startActivity(
                Intent(
                    this@Default, Register::class.java
                )
            )
        }
        checkUserSession()
    }

    private fun checkUserSession() {
        val currentUser: FirebaseUser? = mAuth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this@Default, MainActivity::class.java))
            finish()
        }
    }
}
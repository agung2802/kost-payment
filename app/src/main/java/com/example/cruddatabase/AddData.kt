package com.example.cruddatabase

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class AddData : AppCompatActivity() {
    lateinit var buttonAdd: Button;
    lateinit var name: EditText;
    lateinit var color: EditText
    private lateinit var db: FirebaseFirestore;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_data)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        buttonAdd = findViewById<Button>(R.id.addButton)
        name = findViewById<EditText>(R.id.name)
        color = findViewById<EditText>(R.id.color)

        val fruit: MutableMap<String, Any> = mutableMapOf()
        fruit["name"] = name
        fruit["color"] = color
        buttonAdd.setOnClickListener({
            db.collection("fruit")
                .add(fruit)
            db.collection("fruit")
                .add(fruit)
                .addOnSuccessListener { _ ->
                    Toast.makeText(this, "Add Data Success", Toast.LENGTH_SHORT).show()
                    name.setText("")
                    color.setText("")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Add Data Error : " + e.message, Toast.LENGTH_SHORT).show()
                    Log.w("NewsAdd", "Error adding document", e)
                }
        })


    }
}
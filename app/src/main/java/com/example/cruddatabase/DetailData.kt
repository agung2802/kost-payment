package com.example.cruddatabase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class DetailData : AppCompatActivity() {

    lateinit var newsTitle: TextView
    lateinit var newsSubTitle: TextView
    lateinit var newsImage: ImageView

    lateinit var edit: Button
    lateinit var hapus: Button
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_data)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        newsTitle = findViewById(R.id.newsTitle)
        newsSubTitle = findViewById(R.id.newsSubtitle)
        newsImage = findViewById(R.id.newsImage)
        edit = findViewById(R.id.editButton)
        hapus = findViewById(R.id.deleteButton)
        db = FirebaseFirestore.getInstance()

        val intent = intent
        val id = intent.getStringExtra("id")
        val title = intent.getStringExtra("name")
        val subTitle = intent.getStringExtra("color")
        val imageUrl = intent.getStringExtra("imageUrl")

        Log.w("detail image", imageUrl?:"")

        newsTitle.text = title
        newsSubTitle.text = subTitle
        Glide.with(this).load(imageUrl).into(newsImage)

        edit.setOnClickListener {
            val editIntent = Intent(this, AddData::class.java).apply {
                putExtra("id", id)
                putExtra("name", title)
                putExtra("color", subTitle)
                putExtra("imageUrl", imageUrl)
            }
            startActivity(editIntent)
        }

        hapus.setOnClickListener {
            id?.let { documentId ->
                db.collection("fruit").document(documentId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Fruit deleted successfully", Toast.LENGTH_SHORT).show()
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(mainIntent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error deleting fruit: " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.w("FruitDetails", "Error deleting document", e)
                    }
            }
        }
    }
}
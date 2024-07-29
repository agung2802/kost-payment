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

    lateinit var kostName: TextView
    lateinit var kostPrice: TextView
    lateinit var kostDesc: TextView
    lateinit var kostFacility: TextView
    lateinit var kostImage: ImageView

    lateinit var edit: Button
    lateinit var hapus: Button
    lateinit var buttonBack: Button
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

        kostName = findViewById(R.id.kostTitle)
        kostPrice = findViewById(R.id.kostPrice)
        kostDesc = findViewById(R.id.kostDesc)
        kostFacility = findViewById(R.id.kostFacility)
        kostImage = findViewById(R.id.kostImage)
        edit = findViewById(R.id.editButton)
        hapus = findViewById(R.id.deleteButton)
        buttonBack = findViewById(R.id.backButton)
        db = FirebaseFirestore.getInstance()

        val intent = intent

        val id = intent.getStringExtra("id")
        val Name = intent.getStringExtra("name")
        val Price = intent.getStringExtra("price")
        val Desc = intent.getStringExtra("desc")
        val Facility = intent.getStringExtra("facility")
        val imageUrl = intent.getStringExtra("imageUrl")

        Log.w("detail image", imageUrl?:"")


        kostName.text = Name
        kostPrice.text = "Rp." + Price + ",00"
        kostDesc.text = Desc
        kostFacility.text = Facility
        Glide.with(this).load(imageUrl).into(kostImage)

        edit.setOnClickListener {
            val editIntent = Intent(this, AddData::class.java).apply {
                putExtra("id", id)
                putExtra("name", Name)
                putExtra("price", Price)
                putExtra("desc", Desc)
                putExtra("facility", Facility)
                putExtra("imageUrl", imageUrl)
            }
            startActivity(editIntent)
        }

        hapus.setOnClickListener {
            id?.let { documentId ->
                db.collection("kost").document(documentId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Kost deleted successfully", Toast.LENGTH_SHORT).show()
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(mainIntent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error deleting kost: " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.w("KostDetails", "Error deleting document", e)
                    }
            }
        }

        buttonBack.setOnClickListener({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
    }
}
package com.example.cruddatabase

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddData : AppCompatActivity() {

    private var pickImageRequest: Int = 1

    private var id: String? = null
    private var fruitName: String? = null
    private var fruitColor: String? = null
    private var image: String? = null

    private lateinit var name: EditText
    private lateinit var color: EditText
    private lateinit var imageView: ImageView
    private lateinit var save: Button
    private lateinit var selectImage: Button
    private var imageUrl: Uri? = null

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog
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
        storage = FirebaseStorage.getInstance()

        save = findViewById<Button>(R.id.addButton)
        name = findViewById<EditText>(R.id.txtName)
        color = findViewById<EditText>(R.id.txtColor)
        imageView = findViewById<ImageView>(R.id.previewImage)
        selectImage = findViewById<Button>(R.id.selectImage)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading. . .")

        selectImage.setOnClickListener {
            openFileChooser()
        }

        val updateOption = intent
        if (updateOption != null) {
            id = updateOption.getStringExtra("id")
            fruitName = updateOption.getStringExtra("name")
            fruitColor = updateOption.getStringExtra("color")
            image = updateOption.getStringExtra("imageUrl")

            name.setText(fruitName)
            color.setText(fruitColor)
            Glide.with(this).load(image).into(imageView)
        }

        save.setOnClickListener {
            val name: String = name.text.toString().trim()
            val color: String = color.text.toString().trim()

            if (name.isEmpty() || color.isEmpty()) {
                Toast.makeText(this, "Name and color cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                progressDialog.show()
                if (imageUrl != null) {
                    uploadImageToStorage(name, color)
                } else {
                    saveData(name, color, image?:"")
                }

            }
        }
    }
    private fun openFileChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, this.pickImageRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageRequest && resultCode == RESULT_OK && data != null && data.data != null) {
            this.imageUrl = data.data
            imageView.setImageURI(imageUrl)
        }
    }

    private fun uploadImageToStorage(nameFruit: String, colorFruit: String) {
        if (imageUrl != null) {
            val storageRef: StorageReference = storage.reference.child("news_images/" + System.currentTimeMillis() + ".jpg")
            storageRef.putFile(imageUrl!!)
                .addOnSuccessListener { _ ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        saveData(nameFruit, colorFruit, imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to upload image: " + e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveData(nameFruit: String, colorFruit: String, imageUrl: String) {
        val fruit: MutableMap<String, Any> = mutableMapOf()
        fruit["name"] = nameFruit
        fruit["color"] = colorFruit
        fruit["imageUrl"] = imageUrl

        if (id != null) {
            db.collection("fruit").document(id?:"")
                .update(fruit)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Fruit Updated Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Fruit updating news: "+ e.message, Toast.LENGTH_SHORT).show()
                    Log.w("FruitEdit", "Error updating document", e)
                }
        } else {
            db.collection("fruit")
                .add(fruit)
                .addOnSuccessListener { _ ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Fruit Added Successfully", Toast.LENGTH_SHORT).show()
                    name.setText("")
                    color.setText("")
                    imageView.setImageResource(0)
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error adding fruit: " + e.message, Toast.LENGTH_SHORT).show()
                    Log.w("FruitAdd", "Error adding document", e)
                }
        }
    }
}
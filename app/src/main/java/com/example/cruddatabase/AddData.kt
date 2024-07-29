package com.example.cruddatabase

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
    private var kostName: String? = null
    private var kostFacility: String? = null
    private var kostPrice: String? = null
    private var kostDesc: String? = null
    private var image: String? = null

    private lateinit var name: EditText
    private lateinit var price: EditText
    private lateinit var desc: EditText
    private lateinit var facility: EditText
    private lateinit var screenTitle: TextView
    private lateinit var imageView: ImageView
    private lateinit var save: Button
    private lateinit var selectImage: Button
    private lateinit var buttonBack: Button
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
        price = findViewById<EditText>(R.id.txtPrice)
        desc = findViewById<EditText>(R.id.txtDesc)
        facility = findViewById<EditText>(R.id.txtFacility)
        imageView = findViewById<ImageView>(R.id.previewImage)
        selectImage = findViewById<Button>(R.id.selectImage)
        screenTitle = findViewById(R.id.headTitle)
        buttonBack = findViewById<Button>(R.id.backButton)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading. . .")

        selectImage.setOnClickListener {
            openFileChooser()
        }

        val updateOption = intent
        if (updateOption != null) {
            id = updateOption.getStringExtra("id")
            kostName = updateOption.getStringExtra("name")
            kostPrice = updateOption.getStringExtra("price")
            kostDesc = updateOption.getStringExtra("desc")
            kostFacility = updateOption.getStringExtra("facility")
            image = updateOption.getStringExtra("imageUrl")
            image?.let { Log.w("kost image", it) }

            if (id != null){
                screenTitle.text = "Edit Data Kost"
            } else {
                screenTitle.text = "Add Data Kost"
            }
            name.setText(kostName)
            price.setText(kostPrice)
            desc.setText(kostDesc)
            facility.setText(kostFacility)
            Glide.with(this).load(image).into(imageView)
        }

        save.setOnClickListener {
            val name: String = name.text.toString().trim()
            val price: String = price.text.toString().trim()
            val desc: String = desc.text.toString().trim()
            val facility: String = facility.text.toString().trim()

            if (name.isEmpty() || price.isEmpty() || desc.isEmpty() || facility.isEmpty()) {
                Toast.makeText(this, "Form cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                progressDialog.show()
                if (imageUrl != null) {
                    uploadImageToStorage(name, price, desc, facility)
                } else {
                    saveData(name, price, desc, facility, image?:"")
                }

            }
        }

        buttonBack.setOnClickListener({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })
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

    private fun uploadImageToStorage(nameKost: String, priceKost: String, descKost: String, facilityKost: String) {
        if (imageUrl != null) {
            val storageRef: StorageReference = storage.reference.child("kost_image/" + System.currentTimeMillis() + ".jpg")
            storageRef.putFile(imageUrl!!)
                .addOnSuccessListener { _ ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        saveData(nameKost, priceKost, descKost, facilityKost, imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to upload image: " + e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveData(nameKost: String, priceKost: String, descKost: String, facilityKost: String, imageUrl: String) {
        val kost: MutableMap<String, Any> = mutableMapOf()
        kost["title"] = nameKost
        kost["price"] = priceKost
        kost["desc"] = descKost
        kost["facility"] = facilityKost
        kost["imageUrl"] = imageUrl

        if (id != null) {
            db.collection("kost").document(id?:"")
                .update(kost)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Kost Updated Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Kost updating news: "+ e.message, Toast.LENGTH_SHORT).show()
                    Log.w("KostEdit", "Error updating document", e)
                }
        } else {
            db.collection("kost")
                .add(kost)
                .addOnSuccessListener { _ ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Kost Added Successfully", Toast.LENGTH_SHORT).show()
                    name.setText("")
                    price.setText("")
                    desc.setText("")
                    facility.setText("")
                    imageView.setImageResource(0)
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error adding kost: " + e.message, Toast.LENGTH_SHORT).show()
                    Log.w("KostAdd", "Error adding document", e)
                }
        }
    }
}
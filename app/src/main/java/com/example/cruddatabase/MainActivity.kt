package com.example.cruddatabase


import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView;
    private lateinit var add: FloatingActionButton;

    private lateinit var progressDialog: ProgressDialog
    private lateinit var myAdapter: AdapterList
    private lateinit var itemList: MutableList<FruitModel>
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        list = findViewById(R.id.recycleView)
        add = findViewById(R.id.screenAdd)
        progressDialog = ProgressDialog(this).apply {
            setTitle("Loading. . .")
        }

        add.setOnClickListener {
            startActivity(Intent(this, AddData::class.java))
        }

        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(this)
        itemList = ArrayList()
        myAdapter = AdapterList(itemList)
        list.adapter = myAdapter

        myAdapter.setOnItemClickListener(object : AdapterList.OnItemClickListener {
            override fun onItemClick(item: FruitModel) {
                Log.w("fruit image", item.img)
                val intent = Intent(this@MainActivity, DetailData::class.java).apply {
                    putExtra("id", item.id)
                    putExtra("name", item.name)
                    putExtra("color", item.color)
                    putExtra("imageUrl", item.img)
                }
                startActivity(intent)
            }
        })
    }

    fun main(args: Array<String>){
        getData()
    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    private fun getData() {
        db.collection("fruit")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    itemList.clear()
                    for (document in task.result) {
                        val item = FruitModel(
                            document.id,
                            document.getString("name") ?: "",
                            document.getString("color") ?: "",
                            document.getString("imageUrl") ?: ""
                        )
                        itemList.add(item)
                        Log.d("data", "${document.id} => ${document.data}")
                    }
                    myAdapter.notifyDataSetChanged()
                } else {
                    Log.w("data", "Error getting documents.", task.exception)
                }
            }
    }

}
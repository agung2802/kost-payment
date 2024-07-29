package com.example.cruddatabase


import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView;
    private lateinit var add: FloatingActionButton;

    private lateinit var progressDialog: ProgressDialog
    private lateinit var myAdapter: AdapterList
    private lateinit var itemList: MutableList<KostModel>
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var toolbar: Toolbar

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
        mAuth = FirebaseAuth.getInstance()

        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        list = findViewById(R.id.recycleView)
        add = findViewById(R.id.screenAdd)
        progressDialog = ProgressDialog(this).apply {
            setTitle("Loading. . .")
        }

        // Set up Toolbar
        setSupportActionBar(toolbar)

        add.setOnClickListener {
            startActivity(Intent(this, AddData::class.java))
        }

        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(this)
        itemList = ArrayList()
        myAdapter = AdapterList(itemList)
        list.adapter = myAdapter

        myAdapter.setOnItemClickListener(object : AdapterList.OnItemClickListener {
            override fun onItemClick(item: KostModel) {
                Log.w("kost main image", item.img)
                val intent = Intent(this@MainActivity, DetailData::class.java).apply {
                    putExtra("id", item.id)
                    putExtra("name", item.name)
                    putExtra("price", item.price)
                    putExtra("desc", item.desc)
                    putExtra("facility", item.facility)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_logout) {
            mAuth.signOut()
            Toast.makeText(this@MainActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, Login::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getData() {
        db.collection("kost")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    itemList.clear()
                    for (document in task.result) {
                        val item = KostModel(
                            document.id,
                            document.getString("title") ?: "",
                            document.getString("price") ?: "",
                            document.getString("desc") ?: "",
                            document.getString("facility") ?: "",
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
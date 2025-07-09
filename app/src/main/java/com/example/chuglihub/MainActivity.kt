package com.example.chuglihub

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.*

class MainActivity : AppCompatActivity() {

    private lateinit var inputMsg: EditText
    private lateinit var btnSend: FloatingActionButton
    private lateinit var btnImage: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var toolbar: Toolbar
    private lateinit var usernameDisplay: TextView
    private lateinit var adapter: GossipAdapter
    private val messages = mutableListOf<GossipModel>()
    private lateinit var currentUsername: String

    private val PICK_IMAGE = 101
    private val database = FirebaseDatabase.getInstance().getReference("gossip")

    private val token = ""
    private val repo = ""
    private val branch = ""
    private val path = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputMsg = findViewById(R.id.inputMsg)
        btnSend = findViewById(R.id.btnSend)
        btnImage = findViewById(R.id.btnImage)
        recyclerView = findViewById(R.id.recyclerView)
        toolbar = findViewById(R.id.toolbar)
        usernameDisplay = findViewById(R.id.usernameDisplay)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        currentUsername = SharedPrefHelper.getUsername(this)
        usernameDisplay.text = currentUsername

        adapter = GossipAdapter(messages, currentUsername)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter

        btnSend.setOnClickListener {
            val text = inputMsg.text.toString().trim()
            if (text.isNotEmpty()) {
                val msg = GossipModel(
                    senderName = currentUsername,
                    message = text
                )
                database.push().setValue(msg)
                    .addOnSuccessListener {
                        inputMsg.setText("")
                    }
                    .addOnFailureListener { e ->
                        showSnackbar("Failed to send message: ${e.message}")
                    }
            }
        }

        btnImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (snap in snapshot.children) {
                    val item = snap.getValue(GossipModel::class.java)
                    item?.let { messages.add(it) }
                }
                messages.sortBy { it.timestamp }
                adapter.notifyDataSetChanged()
                if (messages.isNotEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showSnackbar("Failed to load messages: ${error.message}")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val uri: Uri = data.data!!
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                showSnackbar("Uploading image...")
                val base64 = bitmapToBase64(bitmap)
                val fileName = "gossip_${System.currentTimeMillis()}.png"
                uploadToGitHub(base64, fileName)
            } catch (e: Exception) {
                showSnackbar("Failed to process image: ${e.message}")
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun uploadToGitHub(base64: String, fileName: String) {
        val url = "https://api.github.com/repos/$repo/contents/$path/$fileName"
        val json = """
            {
              "message": "Upload from app",
              "content": "$base64",
              "branch": "$branch"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "token $token")
            .addHeader("Accept", "application/vnd.github+json")
            .put(RequestBody.create("application/json".toMediaTypeOrNull(), json))
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    showSnackbar("Failed to upload image: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val rawUrl = "https://raw.githubusercontent.com/$repo/$branch/$path/$fileName"
                    val msg = GossipModel(
                        senderName = currentUsername,
                        imageUrl = rawUrl
                    )
                    database.push().setValue(msg)
                        .addOnSuccessListener {
                            runOnUiThread {
                                showSnackbar("Image shared successfully!")
                            }
                        }
                        .addOnFailureListener { e ->
                            runOnUiThread {
                                showSnackbar("Failed to share image: ${e.message}")
                            }
                        }
                } else {
                    runOnUiThread {
                        showSnackbar("Failed to upload image. Status code: ${response.code}")
                    }
                }
            }
        })
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}
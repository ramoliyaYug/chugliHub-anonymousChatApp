package com.example.chuglihub

import android.app.Activity
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import androidx.core.content.ContextCompat

class GossipAdapter(private val list: List<GossipModel>, private val currentUsername: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_TEXT = 1
    private val TYPE_IMAGE = 2

    override fun getItemViewType(position: Int): Int {
        return if (list[position].imageUrl != null) TYPE_IMAGE else TYPE_TEXT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_TEXT) {
            val view = inflater.inflate(R.layout.item_gossip_text, parent, false)
            TextViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_gossip_image, parent, false)
            ImageViewHolder(view)
        }
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
        val isFromCurrentUser = item.senderName == currentUsername

        if (holder is TextViewHolder) {
            // Set text and metadata
            holder.sender.text = item.senderName
            holder.message.text = item.message
            holder.time.text = time

            // Update bubble styling based on sender
            val container = holder.itemView.findViewById<LinearLayout>(R.id.messageContainer)
            if (isFromCurrentUser) {
                container.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bubble_self_background)
                (container.layoutParams as FrameLayout.LayoutParams).apply {
                    gravity = android.view.Gravity.END
                    marginStart = holder.itemView.resources.getDimensionPixelSize(R.dimen.message_margin_large)
                    marginEnd = holder.itemView.resources.getDimensionPixelSize(R.dimen.message_margin_small)
                }
            } else {
                container.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bubble_text_background)
                (container.layoutParams as FrameLayout.LayoutParams).apply {
                    gravity = android.view.Gravity.START
                    marginStart = holder.itemView.resources.getDimensionPixelSize(R.dimen.message_margin_small)
                    marginEnd = holder.itemView.resources.getDimensionPixelSize(R.dimen.message_margin_large)
                }
            }
        } else if (holder is ImageViewHolder) {
            // Set metadata
            holder.sender.text = item.senderName
            holder.time.text = time

            // Update bubble styling based on sender
            val container = holder.itemView.findViewById<LinearLayout>(R.id.messageContainer)
            if (isFromCurrentUser) {
                container.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bubble_self_background)
                (container.layoutParams as FrameLayout.LayoutParams).apply {
                    gravity = android.view.Gravity.END
                    marginStart = holder.itemView.resources.getDimensionPixelSize(R.dimen.message_margin_large)
                    marginEnd = holder.itemView.resources.getDimensionPixelSize(R.dimen.message_margin_small)
                }
            } else {
                container.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bubble_image_background)
                (container.layoutParams as FrameLayout.LayoutParams).apply {
                    gravity = android.view.Gravity.START
                    marginStart = holder.itemView.resources.getDimensionPixelSize(R.dimen.message_margin_small)
                    marginEnd = holder.itemView.resources.getDimensionPixelSize(R.dimen.message_margin_large)
                }
            }

            // Load image
            Executors.newSingleThreadExecutor().execute {
                try {
                    val stream = URL(item.imageUrl).openStream()
                    val bitmap = BitmapFactory.decodeStream(stream)
                    (holder.itemView.context as Activity).runOnUiThread {
                        holder.image.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    inner class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sender: TextView = view.findViewById(R.id.textSender)
        val message: TextView = view.findViewById(R.id.textGossip)
        val time: TextView = view.findViewById(R.id.textTime)
    }

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sender: TextView = view.findViewById(R.id.imageSender)
        val image: ImageView = view.findViewById(R.id.imageGossip)
        val time: TextView = view.findViewById(R.id.imageTime)
    }
}
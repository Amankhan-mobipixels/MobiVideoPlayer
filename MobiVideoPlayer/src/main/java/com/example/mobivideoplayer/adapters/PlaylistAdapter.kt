package com.example.mobivideoplayer.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobivideoplayer.R
import java.io.File

class PlaylistAdapter(private val context: Context, private val list: ArrayList<String>, private var currentPlaying: String, val listener : Selected) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.playlist_sample, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (list[position] == currentPlaying) holder.card.setCardBackgroundColor(Color.parseColor("#85346ADE"))
        else holder.card.setCardBackgroundColor(Color.parseColor("#000000"))

        holder.card.setOnClickListener{
            listener.Selected(list[position])
            currentPlaying = list[position]
            notifyDataSetChanged()
        }
        Glide.with(context).load(File(list[position]).path).into(  holder.thumbnail)
        holder.title.text = File(list[position]).name
    }
    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var card: CardView = itemView.findViewById(R.id.card)
        var thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        var title: TextView = itemView.findViewById(R.id.title)
    }
}

interface Selected {
  fun Selected(path:String)
}

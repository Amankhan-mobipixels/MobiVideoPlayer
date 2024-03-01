package com.example.mobivideoplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobivideoplayer.R
import com.example.mobivideoplayer.models.IconData

class IconsAdapter(private val context: Context, private val iconModelsList: ArrayList<IconData>) : RecyclerView.Adapter<IconsAdapter.ViewHolder>() {
    private var mListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.icons_layout, parent, false)
        return ViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.icon.setImageResource(iconModelsList[position].imageView)
        holder.iconName.text = iconModelsList[position].iconTitle
    }

    override fun getItemCount(): Int {
        return iconModelsList.size
    }

    inner class ViewHolder(itemView: View, listener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        var iconName: TextView = itemView.findViewById(R.id.icon_title)
        var icon: ImageView = itemView.findViewById(R.id.playback_icon)

        init {
            itemView.setOnClickListener {
                if (listener != null) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position)
                    }
                }
            }
        }
    }
}
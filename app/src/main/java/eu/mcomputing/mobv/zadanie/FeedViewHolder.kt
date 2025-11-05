package eu.mcomputing.mobv.zadanie

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val imageView: ImageView = view.findViewById(R.id.item_image)
    val textView: TextView = view.findViewById(R.id.item_text)
}
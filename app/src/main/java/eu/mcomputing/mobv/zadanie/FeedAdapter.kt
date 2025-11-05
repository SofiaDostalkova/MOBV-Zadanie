package eu.mcomputing.mobv.zadanie

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

data class FeedItem(
    val imageRes: Int,
    val text: String
)

class FeedAdapter(private val data: List<FeedItem>) : RecyclerView.Adapter<FeedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val item = data[position]
        holder.imageView.setImageResource(item.imageRes)
        holder.textView.text = item.text
    }

    override fun getItemCount(): Int = data.size

}

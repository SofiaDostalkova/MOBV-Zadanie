package eu.mcomputing.mobv.zadanie

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FeedAdapter(
    private var users: List<UserEntity>,
    private val onClick: (UserEntity) -> Unit
) : RecyclerView.Adapter<FeedAdapter.FeedHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_item, parent, false)
        return FeedHolder(view)
    }

    override fun onBindViewHolder(holder: FeedHolder, position: Int) {
        val user = users[position]

        holder.textView.text = user.name ?: "Anonymous"

        // Load image (same logic as map fragment)
        if (!user.photo.isNullOrEmpty()) {
            Glide.with(holder.imageView.context)
                .load("https://upload.mcomputing.eu/${user.photo}")
                .circleCrop()
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.outline_account_circle_24)
        }

        holder.itemView.setOnClickListener {
            onClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateItems(newUsers: List<UserEntity>) {
        users = newUsers
        notifyDataSetChanged()
    }

    class FeedHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.item_image)
        val textView: TextView = view.findViewById(R.id.item_text)
    }
}

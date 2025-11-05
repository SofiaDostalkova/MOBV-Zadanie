package eu.mcomputing.mobv.zadanie

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import eu.mcomputing.mobv.zadanie.R
import androidx.recyclerview.widget.LinearLayoutManager

class FeedFragment : Fragment(R.layout.fragment_feed) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomBar = view.findViewById<CustomBottomBar>(R.id.bottom_menu)
        val navController = findNavController()
        bottomBar.setupWithNavController(navController)

        val recyclerView: RecyclerView = view.findViewById(R.id.feedRecyclerView)
        val myDataList = listOf(
            FeedItem(R.drawable.ic_launcher_foreground, "Post 1"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 2"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 3"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 4"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 5"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 6"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 7"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 8"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 9"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 10"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 11"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 12"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 13"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 14"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 15"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 16"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 17"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 18"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 19"),
            FeedItem(R.drawable.ic_launcher_foreground, "Post 20")

        )
        recyclerView.adapter = FeedAdapter(myDataList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
}
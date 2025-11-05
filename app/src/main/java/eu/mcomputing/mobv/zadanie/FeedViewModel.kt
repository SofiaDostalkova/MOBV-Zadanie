package eu.mcomputing.mobv.zadanie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FeedViewModel : ViewModel() {
    private val _feedItems = MutableLiveData<List<FeedItem>>()
    val feedItems: LiveData<List<FeedItem>> get() = _feedItems

    fun loadFeed() {
        _feedItems.value = listOf(
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
    }
}

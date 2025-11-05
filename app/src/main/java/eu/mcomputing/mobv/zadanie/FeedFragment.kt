package eu.mcomputing.mobv.zadanie

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FeedFragment : Fragment(R.layout.fragment_feed) {
    private lateinit var viewModel: FeedViewModel
    private lateinit var adapter: FeedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomBar = view.findViewById<CustomBottomBar>(R.id.bottom_menu)
        val navController = findNavController()
        bottomBar.setupWithNavController(navController)

        val recyclerView = view.findViewById<RecyclerView>(R.id.feedRecyclerView)
        adapter = FeedAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val repository = DataRepository.getInstance(requireContext())
        viewModel = ViewModelProvider(this, object : Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(repository) as T
            }
        })[FeedViewModel::class.java]

        viewModel.users.observe(viewLifecycleOwner) { users ->
            users?.let {
                val items = it.map { user ->
                    FeedItem(
                        R.drawable.ic_launcher_foreground,
                        user?.username ?: "Unknown user"
                    )
                }
                adapter.updateItems(items)
            }
        }

        viewModel.refreshUsers()
    }
}

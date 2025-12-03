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

        // Set up bottom bar
        val bottomBar = view.findViewById<CustomBottomBar>(R.id.bottom_menu)
        bottomBar.setupWithNavController(findNavController())

        // Set up RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.feedRecyclerView)
        adapter = FeedAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up ViewModel
        val repository = DataRepository.getInstance(requireContext())
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(repository, requireContext()) as T
            }
        })[FeedViewModel::class.java]

        // Observe users
        viewModel.users.observe(viewLifecycleOwner) { users ->
            users?.let {
                val items = it.map { user ->
                    FeedItem(R.drawable.ic_launcher_foreground, user?.name ?: "Anonym")
                }
                adapter.updateItems(items)
            }
        }

        // Observe loading state (optional)
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Show a progress bar or pull-to-refresh if you have one
        }

        // Refresh users from API
        viewModel.refreshUsers()
    }
}

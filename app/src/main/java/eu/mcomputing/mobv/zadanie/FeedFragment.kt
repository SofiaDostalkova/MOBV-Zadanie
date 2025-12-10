package eu.mcomputing.mobv.zadanie

import android.os.Bundle
import android.view.View
import android.widget.TextView
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
        bottomBar.setupWithNavController(findNavController())

        val locationDisabledMessage = view.findViewById<TextView>(R.id.location_disabled_message)
        val sharing = PreferenceData.getInstance().getSharing(requireContext())
        locationDisabledMessage.visibility = if (sharing) View.GONE else View.VISIBLE

        val recyclerView = view.findViewById<RecyclerView>(R.id.feedRecyclerView)
        adapter = FeedAdapter(emptyList()) { user ->
            val bundle = Bundle().apply {
                putString("uid", user.uid)
                putString("name", user.name)
                putString("photoUrl", user.photo)
            }
            findNavController().navigate(R.id.userProfileFragment, bundle)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val repository = DataRepository.getInstance(requireContext())
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(repository, requireContext()) as T
            }
        })[FeedViewModel::class.java]

        viewModel.users.observe(viewLifecycleOwner) { users ->
            val currentUser = PreferenceData.getInstance().getUser(requireContext())

            users
                ?.filterNotNull()
                ?.filter { it.uid != currentUser?.id }
                ?.let { filtered ->
                    adapter.updateItems(filtered)
                }
        }


        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
        }

        //viewModel.refreshUsers()
    }
}

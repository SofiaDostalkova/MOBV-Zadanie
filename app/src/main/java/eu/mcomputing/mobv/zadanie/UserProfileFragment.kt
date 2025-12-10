package eu.mcomputing.mobv.zadanie

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import com.bumptech.glide.Glide

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomBar = view.findViewById<CustomBottomBar>(R.id.bottom_menu)
        bottomBar.setupWithNavController(findNavController())

        val uid = arguments?.getString("uid") ?: return
        val name = arguments?.getString("name") ?: "Unknown"
        val photoUrl = arguments?.getString("photoUrl")

        val imageView = view.findViewById<ImageView>(R.id.profile_image)
        val textView = view.findViewById<TextView>(R.id.profile_name)

        textView.text = name
        photoUrl?.let {
            Glide.with(this)
                .load("https://upload.mcomputing.eu/$it")
                .circleCrop()
                .into(imageView)
        }


    }
}

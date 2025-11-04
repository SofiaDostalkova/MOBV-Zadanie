package eu.mcomputing.mobv.zadanie

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import eu.mcomputing.mobv.zadanie.R

class FeedFragment : Fragment(R.layout.fragment_feed) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageView>(R.id.icon_map).setOnClickListener {
        }

        view.findViewById<ImageView>(R.id.icon_list).setOnClickListener {
        }

        view.findViewById<ImageView>(R.id.icon_profile).setOnClickListener {
        }

    }
}
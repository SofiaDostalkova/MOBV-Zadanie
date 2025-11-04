package eu.mcomputing.mobv.zadanie

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController

class CustomBottomBar(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.bottom_bar, this, true)
    }

    fun setupWithNavController(navController: NavController) {
        val iconMap = findViewById<ImageView>(R.id.icon_map)
        val iconFeed = findViewById<ImageView>(R.id.icon_list)
        val iconProfile = findViewById<ImageView>(R.id.icon_profile)

        iconMap.setOnClickListener {
            navController.navigate(R.id.MapFragment)
        }
        iconFeed.setOnClickListener {
            navController.navigate(R.id.FeedFragment)
        }
        iconProfile.setOnClickListener {
            navController.navigate(R.id.ProfileFragment)
        }
    }
}
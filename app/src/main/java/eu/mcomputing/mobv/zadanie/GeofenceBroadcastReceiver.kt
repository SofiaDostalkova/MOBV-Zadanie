package eu.mcomputing.mobv.zadanie

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofenceStatusCodes
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val event = GeofencingEvent.fromIntent(intent)
        if (event == null) {
            Log.e("GeofenceReceiver", "Null event")
            return
        }

        if (event.hasError()) {
            val msg = GeofenceStatusCodes.getStatusCodeString(event.errorCode)
            Log.e("GeofenceReceiver", "Error: $msg")
            return
        }

        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val newLocation: Location? = event.triggeringLocation
            if (newLocation != null) {
                Log.d("GeofenceReceiver", "Exited geofence → creating new one")
                GeofenceHelper.addGeofence(context, newLocation)

                val user = PreferenceData.getInstance().getUser(context) ?: return
                val repository = DataRepository.getInstance(context)
                val feedViewModel = FeedViewModel(repository, context)

                feedViewModel.viewModelScope.launch {
                    val success = repository.apiUpdateGeofence(
                        lat = newLocation.latitude,
                        lon = newLocation.longitude,
                        radius = 100,
                        accessToken = user.access
                    )
                    if (success) {
                        repository.clearCachedUsers()
                        feedViewModel.refreshUsers()
                        Log.d("GeofenceReceiver", "Server updated and users refreshed")
                    }
                }
            }
        }
    }
}


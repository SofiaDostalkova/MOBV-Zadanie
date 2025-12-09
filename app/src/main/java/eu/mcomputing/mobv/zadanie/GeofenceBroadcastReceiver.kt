package eu.mcomputing.mobv.zadanie

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofenceStatusCodes

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
            }
        }
    }
}

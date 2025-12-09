package eu.mcomputing.mobv.zadanie

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

object GeofenceHelper {

    fun createGeofence(location: Location): Geofence {
        return Geofence.Builder()
            .setRequestId("my-geofence")
            .setCircularRegion(location.latitude, location.longitude, 100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
    }

    fun createGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    fun getGeofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    fun addGeofence(context: Context, location: Location) {
        Log.d("GeofenceHelper", "Adding geofence at ${location.latitude}, ${location.longitude}")

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("GeofenceHelper", "Missing FINE_LOCATION permission")
            return
        }

        val geofencingClient = LocationServices.getGeofencingClient(context)
        val geofence = createGeofence(location)
        val request = createGeofencingRequest(geofence)
        val pendingIntent = getGeofencePendingIntent(context)

        geofencingClient.addGeofences(request, pendingIntent)
            .addOnSuccessListener {
                Log.d("GeofenceHelper", "New geofence successfully added")
            }
            .addOnFailureListener { e ->
                Log.e("GeofenceHelper", "Failed to add geofence", e)
            }
    }
}

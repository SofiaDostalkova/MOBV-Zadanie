package eu.mcomputing.mobv.zadanie

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel(private val dataRepository: DataRepository) : ViewModel() {

    // LiveData for location sharing toggle
    val sharingLocation = MutableLiveData<Boolean?>(null)

    // Helper to update sharing
    fun setSharing(enabled: Boolean) {
        sharingLocation.value = enabled
    }

    // Optional: helper to get repository if you want to send location later
    fun getDataRepository() = dataRepository
}
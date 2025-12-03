package eu.mcomputing.mobv.zadanie

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel(private val dataRepository: DataRepository) : ViewModel() {

    val sharingLocation = MutableLiveData<Boolean?>(null)

    fun setSharing(enabled: Boolean) {
        sharingLocation.value = enabled
    }

    fun getDataRepository() = dataRepository
}
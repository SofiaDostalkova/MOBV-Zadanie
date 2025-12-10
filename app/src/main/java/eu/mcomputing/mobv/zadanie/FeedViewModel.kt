package eu.mcomputing.mobv.zadanie

import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repository: DataRepository,
    private val context: Context
) : ViewModel() {

    val users: LiveData<List<UserEntity?>> = repository.getUsers()
    val loading = MutableLiveData(false)
    private val _message = MutableLiveData<Evento<String>>()
    val message: LiveData<Evento<String>> get() = _message

    init {
        refreshUsers()
    }

    fun refreshUsers() {
        viewModelScope.launch {
            loading.postValue(true)
            val user = PreferenceData.getInstance().getUser(context)
            val accessToken = user?.access
            val result = if (accessToken != null) {
                repository.apiListGeofence(accessToken)
            } else {
                "No logged-in user. Cannot fetch feed."
            }
            _message.postValue(Evento(result))
            loading.postValue(false)
        }
    }

    fun getDataRepository() = repository
}




package eu.mcomputing.mobv.zadanie

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class FeedViewModel(private val repository: DataRepository) : ViewModel() {

    val users: LiveData<List<UserEntity?>> = liveData {
        loading.postValue(true)
        repository.apiListGeofence()
        loading.postValue(false)
        emitSource(repository.getUsers())
    }

    val loading = MutableLiveData(false)
    private val _message = MutableLiveData<Evento<String>>()
    val message: LiveData<Evento<String>> get() = _message

    fun refreshUsers() {
        viewModelScope.launch {
            loading.postValue(true)
            _message.postValue(Evento(repository.apiListGeofence()))
            loading.postValue(false)
        }
    }
}


package eu.mcomputing.mobv.zadanie

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class FeedViewModel(private val repository: DataRepository) : ViewModel() {

    val users: LiveData<List<UserEntity?>> = repository.getUsers()

    val loading = MutableLiveData(false)
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun refreshUsers() {
        viewModelScope.launch {
            loading.postValue(true)
            val msg = repository.apiGetUsers()
            _message.postValue(msg)
            loading.postValue(false)
        }
    }
}

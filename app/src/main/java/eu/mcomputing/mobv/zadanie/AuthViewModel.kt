package eu.mcomputing.mobv.zadanie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mcomputing.mobv.zadanie.DataRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private val _registrationResult = MutableLiveData<Pair<String, User?>>()
    val registrationResult: LiveData<Pair<String, User?>> get() = _registrationResult

    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            val result = dataRepository.apiRegisterUser(username, email, password)
            _registrationResult.postValue(result)
        }
    }
}
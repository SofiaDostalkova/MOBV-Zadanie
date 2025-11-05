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

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    private val _loginResult = MutableLiveData<Pair<String, User?>>()
    val loginResult: LiveData<Pair<String, User?>> get() = _loginResult
    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registrationResult.postValue(dataRepository.apiRegisterUser(username, email, password))
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = dataRepository.apiLoginUser(email, password)
            _loginResult.postValue(result)
            if (result.second != null) {
                _currentUser.postValue(result.second)
            }
        }
    }

    fun logout() {
        _currentUser.postValue(null)
    }

}
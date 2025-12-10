package eu.mcomputing.mobv.zadanie

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.mcomputing.mobv.zadanie.DataRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val dataRepository: DataRepository) : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser
    private val _registrationResult = MutableLiveData<Pair<String, User?>>()
    val registrationResult: LiveData<Pair<String, User?>> get() = _registrationResult

    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            val result = dataRepository.apiRegisterUser(username, email, password)
            _registrationResult.postValue(result)
        }
    }

    private val _loginResult = MutableLiveData<Pair<String, User?>?>()
    val loginResult: LiveData<Pair<String, User?>?> = _loginResult

    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            val result = dataRepository.apiLoginUser(username, password)
            _loginResult.postValue(result)
            result.second?.let { _currentUser.postValue(it) }
        }
    }

    fun logout() {
        _currentUser.postValue(null)
        PreferenceData.getInstance().clearData(null)
    }

    fun clearLoginResult() {
        _loginResult.postValue(null)
    }

    private val _resetResult = MutableLiveData<Boolean>()
    val resetResult: LiveData<Boolean> = _resetResult

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            val success = dataRepository.apiRequestPasswordReset(email)
            _resetResult.postValue(success)
        }
    }
}
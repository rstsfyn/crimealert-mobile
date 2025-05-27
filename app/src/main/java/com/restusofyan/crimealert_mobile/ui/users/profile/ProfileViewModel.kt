package com.restusofyan.crimealert_mobile.ui.users.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restusofyan.crimealert_mobile.data.repository.CrimeAlertRepository
import com.restusofyan.crimealert_mobile.data.response.profile.MyProfileResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: CrimeAlertRepository
) : ViewModel() {

    private val _profile = MutableLiveData<MyProfileResult?>()
    val profile: LiveData<MyProfileResult?> = _profile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMyProfile(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getMyProfile(token)
                if (response.isSuccessful) {
                    _profile.value = response.body()?.myProfileResult
                } else {
                    _error.value = "Gagal memuat profil: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}

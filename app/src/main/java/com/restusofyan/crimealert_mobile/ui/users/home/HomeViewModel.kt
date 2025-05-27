package com.restusofyan.crimealert_mobile.ui.users.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restusofyan.crimealert_mobile.data.repository.CrimeAlertRepository
import com.restusofyan.crimealert_mobile.data.response.casesreports.ListReportsItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CrimeAlertRepository
) : ViewModel(){

    private val _reports = MutableLiveData<List<ListReportsItem>>()
    val reports: LiveData<List<ListReportsItem>> = _reports

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchReports(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getAllReports(token)
                if (response.isSuccessful) {
                    _reports.value = response.body()?.listReports?.filterNotNull()
                } else {
                    _error.value = "Gagal memuat berita: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

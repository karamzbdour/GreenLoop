package com.example.greenloop.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.greenloop.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    val livingSituation: StateFlow<String> = repository.livingSituation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Apartment")

    val allergies: StateFlow<String> = repository.allergies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun updateLivingSituation(situation: String) {
        viewModelScope.launch {
            repository.updateLivingSituation(situation)
        }
    }

    fun updateAllergies(allergies: String) {
        viewModelScope.launch {
            repository.updateAllergies(allergies)
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

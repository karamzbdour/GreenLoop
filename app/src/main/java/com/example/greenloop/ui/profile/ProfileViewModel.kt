package com.example.greenloop.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.greenloop.data.model.Ingredient
import com.example.greenloop.data.model.UpcycleHistory
import com.example.greenloop.data.repository.HistoryRepository
import com.example.greenloop.data.repository.IngredientRepository
import com.example.greenloop.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val historyRepository: HistoryRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    val history: StateFlow<List<UpcycleHistory>> = historyRepository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pastFiveRecipes: StateFlow<List<UpcycleHistory>> = history.map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined count: Recipes actually completed (history) + recipes generated via AI
    val recipesMadeCount: StateFlow<Int> = combine(
        history.map { it.size },
        userRepository.recipesGeneratedCount
    ) { completed, generated ->
        completed + generated
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inventoryItems: StateFlow<List<Ingredient>> = ingredientRepository.allIngredients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMoneySaved: StateFlow<Double> = combine(history, inventoryItems) { historyList, inventory ->
        val moneySavedFromRecipes = historyList.sumOf { it.moneySaved }
        
        // Money saved from not expiring food:
        // We consider food "saved" if it's currently in stock (quantity > 0)
        // and its value is represented by its price.
        val moneySavedFromActiveInventory = inventory.filter { 
            val qty = it.quantity?.removePrefix("x")?.toIntOrNull() ?: 0
            qty > 0 
        }.sumOf { 
            val qty = it.quantity?.removePrefix("x")?.toIntOrNull() ?: 1
            (it.price ?: 0.0) * qty
        }
        
        moneySavedFromRecipes + moneySavedFromActiveInventory
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    class Factory(
        private val userRepository: UserRepository,
        private val historyRepository: HistoryRepository,
        private val ingredientRepository: IngredientRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(userRepository, historyRepository, ingredientRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

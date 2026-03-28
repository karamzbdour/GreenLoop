package com.example.greenloop.ui.dashboard

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.greenloop.api.GeminiManager
import com.example.greenloop.data.model.WasteItem
import com.example.greenloop.data.repository.WasteRepository
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(private val repository: WasteRepository) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> = _scanResult.asStateFlow()

    val expiringItems: StateFlow<List<WasteItem>> = repository.getExpiringSoon(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }.timeInMillis
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun scanImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val prompt = "Identify the food items or biowaste in this image and their likely expiry dates (in days from now). Format the response as a JSON list of objects with 'name' and 'expiryDays' fields. Example: [{\"name\": \"Banana\", \"expiryDays\": 2}]"
                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }
                val response = GeminiManager.model.generateContent(inputContent)
                val resultText = response.text
                _scanResult.value = resultText
                
                // Simple parsing for demo purposes (ideally use Moshi)
                parseAndSaveResult(resultText)
            } catch (e: Exception) {
                _scanResult.value = "Error: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    private suspend fun parseAndSaveResult(resultText: String?) {
        if (resultText == null) return
        
        // Very basic extraction for the sake of the task
        // In a real app, I'd use a regex or a proper JSON parser
        val regex = Regex("""\"name\":\s*\"([^\"]+)\",\s*\"expiryDays\":\s*(\d+)""")
        val matches = regex.findAll(resultText)
        
        matches.forEach { match ->
            val name = match.groups[1]?.value ?: "Unknown Item"
            val expiryDays = match.groups[2]?.value?.toIntOrNull() ?: 3
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, expiryDays)
            
            val newItem = WasteItem(
                name = name,
                category = "Uncategorized",
                expiryDate = calendar.timeInMillis
            )
            repository.insertItem(newItem)
        }
    }

    class Factory(private val repository: WasteRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

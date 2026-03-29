package com.example.greenloop.ui.shoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.greenloop.data.model.Ingredient
import com.example.greenloop.data.repository.IngredientRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class ShoppingListViewModel(private val repository: IngredientRepository) : ViewModel() {
    
    private val _manualShoppingItems = MutableStateFlow<List<String>>(emptyList())
    val manualShoppingItems: StateFlow<List<String>> = _manualShoppingItems.asStateFlow()

    private val _ignoredSuggestions = MutableStateFlow<Set<String>>(emptySet())

    val suggestedItems: StateFlow<List<String>> = combine(
        repository.allIngredients,
        _ignoredSuggestions
    ) { ingredients, ignored ->
        val manuallyRemoved = ingredients.filter { it.wasRemovedManually }.map { it.name }
        val otherHistory = ingredients.map { it.name }
        (manuallyRemoved + otherHistory).distinct().filter { it !in ignored }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addManualItem(name: String) {
        if (name.isNotBlank()) {
            _manualShoppingItems.value += name
            // If we manually add something that was ignored, un-ignore it
            _ignoredSuggestions.value -= name
        }
    }

    fun removeManualItem(name: String) {
        _manualShoppingItems.value -= name
    }

    fun ignoreSuggestion(name: String) {
        _ignoredSuggestions.value += name
    }

    fun addIngredientToInventory(name: String) {
        viewModelScope.launch {
            val existingIngredient = repository.getIngredientByNameAndCategory(name, "Uncategorized")
            if (existingIngredient != null) {
                val currentQty = try {
                    existingIngredient.quantity?.removePrefix("x")?.toIntOrNull() ?: 0
                } catch (e: Exception) { 0 }
                
                repository.updateIngredient(existingIngredient.copy(
                    quantity = "x${currentQty + 1}",
                    wasRemovedManually = false
                ))
            } else {
                repository.insertIngredient(Ingredient(
                    name = name,
                    category = "Uncategorized",
                    expiryDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000),
                    quantity = "x1",
                    wasRemovedManually = false
                ))
            }
            // If it was a manually added shopping item, remove it after adding to inventory
            removeManualItem(name)
        }
    }

    class Factory(private val repository: IngredientRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ShoppingListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel) {
    val suggestedItems by viewModel.suggestedItems.collectAsStateWithLifecycle()
    val manualItems by viewModel.manualShoppingItems.collectAsStateWithLifecycle()
    val checkedItems = remember { mutableStateMapOf<String, Boolean>() }
    var showAllSuggestions by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }

    val displayedSuggestions = if (showAllSuggestions) suggestedItems else suggestedItems.take(10)
    val hasMoreSuggestions = suggestedItems.size > 10

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shopping List", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Add custom item section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text("Add item manually...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (newItemName.isNotBlank()) {
                            IconButton(onClick = { newItemName = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.addManualItem(newItemName)
                        newItemName = ""
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Manual items section
                if (manualItems.isNotEmpty()) {
                    item {
                        Text(
                            "My List",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(manualItems) { itemName ->
                        ShoppingListItem(
                            itemName = itemName,
                            isChecked = checkedItems[itemName] ?: false,
                            onCheckedChange = { isChecked ->
                                checkedItems[itemName] = isChecked
                                if (isChecked) viewModel.addIngredientToInventory(itemName)
                            },
                            onRemove = { viewModel.removeManualItem(itemName) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // Suggestions section
                if (suggestedItems.isNotEmpty()) {
                    item {
                        Text(
                            "Suggestions (from your fridge)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    items(displayedSuggestions) { itemName ->
                        ShoppingListItem(
                            itemName = itemName,
                            isChecked = checkedItems[itemName] ?: false,
                            onCheckedChange = { isChecked ->
                                checkedItems[itemName] = isChecked
                                if (isChecked) viewModel.addIngredientToInventory(itemName)
                            },
                            onRemove = { viewModel.ignoreSuggestion(itemName) }
                        )
                    }

                    if (hasMoreSuggestions && !showAllSuggestions) {
                        item {
                            TextButton(
                                onClick = { showAllSuggestions = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ExpandMore, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Show all suggestions (${suggestedItems.size})")
                                }
                            }
                        }
                    }
                }

                if (manualItems.isEmpty() && suggestedItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Your shopping list is empty.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListItem(
    itemName: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onRemove: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = itemName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (isChecked) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) 
                else 
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (onRemove != null && !isChecked) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

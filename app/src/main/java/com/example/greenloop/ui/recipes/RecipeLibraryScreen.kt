package com.example.greenloop.ui.recipes

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.greenloop.data.model.GeneratedRecipe
import com.example.greenloop.data.model.Ingredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeLibraryScreen(viewModel: RecipeViewModel) {
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIngredients.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val generatedRecipes by viewModel.generatedRecipes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zero-Waste Chef", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
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
            Text(
                text = "Select ingredients to rescue:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Selection List
            Box(modifier = Modifier.weight(0.4f)) {
                if (inventory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No ingredients in inventory. Scan a receipt first!")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(inventory) { ingredient ->
                            IngredientSelectionCard(
                                ingredient = ingredient,
                                isSelected = selectedIds.contains(ingredient.id),
                                onToggle = { viewModel.toggleIngredientSelection(ingredient.id) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.generateAiRecipes() },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedIds.isNotEmpty() && !isGenerating,
                shape = MaterialTheme.shapes.large,
                contentPadding = PaddingValues(16.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate 3 Recipes")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Generated Recipes List
            AnimatedVisibility(
                visible = generatedRecipes.isNotEmpty(),
                modifier = Modifier.weight(0.6f),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(generatedRecipes) { recipe ->
                        GeneratedRecipeCard(
                            recipe = recipe,
                            onComplete = { viewModel.completeRecipe(recipe) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientSelectionCard(
    ingredient: Ingredient,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ingredient.category,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
fun GeneratedRecipeCard(
    recipe: GeneratedRecipe,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.recipeName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                        Text(recipe.difficulty, modifier = Modifier.padding(4.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, modifier = Modifier.size(14.dp))
                        Text(" ${recipe.prepTimeMinutes}m", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text("Steps:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            
            recipe.steps.take(3).forEachIndexed { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp),
                    maxLines = 2
                )
            }
            if (recipe.steps.size > 3) Text("...", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Make Recipe")
            }
        }
    }
}

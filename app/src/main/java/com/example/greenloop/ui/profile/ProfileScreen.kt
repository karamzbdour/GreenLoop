package com.example.greenloop.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val livingSituation by viewModel.livingSituation.collectAsStateWithLifecycle()
    val allergies by viewModel.allergies.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("User Profile", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Personalization Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            LivingSituationSection(
                selected = livingSituation,
                onSelected = { viewModel.updateLivingSituation(it) }
            )

            AllergiesSection(
                currentAllergies = allergies,
                onUpdate = { viewModel.updateAllergies(it) }
            )

            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "These settings help us tailor upcycling recipes and tasks to your specific needs and environment.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun LivingSituationSection(selected: String, onSelected: (String) -> Unit) {
    val options = listOf("Apartment", "House with Garden", "Shared Living")
    
    Column {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Living Situation", style = MaterialTheme.typography.titleMedium)
        }
        
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selected),
                    onClick = { onSelected(option) }
                )
                Text(text = option, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun AllergiesSection(currentAllergies: String, onUpdate: (String) -> Unit) {
    var text by remember(currentAllergies) { mutableStateOf(currentAllergies) }

    Column {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Allergies / Sensitivities", style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = text,
            onValueChange = { 
                text = it
                onUpdate(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("e.g. Citrus, Vinegar, Essential Oils") },
            supportingText = { Text("Recipes containing these keywords will be filtered out.") }
        )
    }
}

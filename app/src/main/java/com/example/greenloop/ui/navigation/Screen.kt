package com.example.greenloop.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Rounded.Dashboard)
    object Ingredients : Screen("ingredients", "Inventory", Icons.Rounded.Kitchen)
    object ShoppingList : Screen("shopping_list", "Shopping List", Icons.Rounded.ShoppingBag)
    object Recipes : Screen("recipes", "Recipes", Icons.Rounded.RestaurantMenu)
    object Progress : Screen("progress", "Progress", Icons.Rounded.History)
    object Profile : Screen("profile", "Profile", Icons.Rounded.Person)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.ShoppingList,
    Screen.Recipes,
    Screen.Profile
)

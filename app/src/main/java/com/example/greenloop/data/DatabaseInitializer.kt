package com.example.greenloop.data

import com.example.greenloop.data.model.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseInitializer {
    fun populateDatabase(db: AppDatabase) {
        val recipeDao = db.recipeDao()
        CoroutineScope(Dispatchers.IO).launch {
            val recipes = listOf(
                Recipe(
                    title = "Orange Peel All-Purpose Cleaner",
                    description = "A natural, non-toxic cleaner that smells great and cuts through grease.",
                    biowasteType = "Orange Peels",
                    steps = "1. Fill a jar with orange peels. 2. Cover with white vinegar. 3. Let sit for 2 weeks. 4. Strain and use.",
                    difficulty = "Easy",
                    co2Saved = 0.5
                ),
                Recipe(
                    title = "Banana Peel Fertilizer",
                    description = "Potassium-rich fertilizer for your indoor plants.",
                    biowasteType = "Banana Peels",
                    steps = "1. Dry banana peels in the sun or oven. 2. Grind into a powder. 3. Mix into soil.",
                    difficulty = "Easy",
                    co2Saved = 0.2
                ),
                Recipe(
                    title = "Coffee Ground Body Scrub",
                    description = "Exfoliating body scrub made from used coffee grounds.",
                    biowasteType = "Coffee Grounds",
                    steps = "1. Mix dried coffee grounds with coconut oil. 2. Add a few drops of essential oil if desired. 3. Store in a jar.",
                    difficulty = "Medium",
                    co2Saved = 0.3
                ),
                Recipe(
                    title = "Eggshell Plant Food",
                    description = "Calcium boost for your garden tomatoes and peppers.",
                    biowasteType = "Eggshells",
                    steps = "1. Rinse and dry eggshells. 2. Crush into fine pieces. 3. Sprinkle around the base of plants.",
                    difficulty = "Easy",
                    co2Saved = 0.15
                )
            )
            
            recipes.forEach { recipe ->
                recipeDao.insertRecipe(recipe)
            }
        }
    }
}

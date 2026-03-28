# Project Plan

Build a sustainability app named "GreenLoop" that helps users upcycle biowaste into useful products using AI. The app features a dashboard for scanning receipts and tracking expiring items, an upcycling recipe library, a progress tracker for CO2 saved, and personalized settings. It uses Kotlin, Jetpack Compose, MVVM, Gemini SDK, and CameraX.

## Project Brief

# Project Brief: GreenLoop

GreenLoop is a sustainability-focused Android application designed to help users minimize waste by transforming biowaste into useful products through AI-driven upcycling recipes.

### Features
*   **Intelligent Dashboard**: A central hub featuring a "Scan Receipt" tool powered by Gemini AI to catalog potential waste and a card-based layout tracking "Items Expiring Soon."
*   **Upcycling Recipe Library**: A curated list of biowaste-to-product recipes (e.g., turning orange peels into cleaner) with detailed steps, difficulty ratings, and interactive "Start" buttons.
*   **Sustainability Progress Tracker**: A visual representation of environmental impact featuring a circular progress bar for "CO2 Saved" and a historical log of "Completed Upcycles."
*   **Personalized User Profiles**: Adaptive settings that tailor recipe suggestions and tasks based on the user's living situation (Apartment vs. House) and specific allergies.

### High-Level Tech Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material Design 3)
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Asynchronous Programming**: Kotlin Coroutines
*   **Generative AI**: Google Gemini SDK (GenAI)
*   **Hardware Integration**: CameraX (for receipt and waste scanning)
*   **Code Generation**: KSP (Kotlin Symbol Processing)

## Implementation Steps
**Total Duration:** 33m 23s

### Task_1_Core_Foundation: Setup core data models, Room database for tracking waste and recipes, and integrate Gemini SDK for AI processing.
- **Status:** COMPLETED
- **Updates:** 1.  **Data Layer (Room Database)**:
- **Acceptance Criteria:**
  - Room database and DAOs are functional for Items, Recipes, and History
  - Gemini SDK is integrated with API_KEY configuration
  - Navigation architecture for Dashboard, Recipes, Progress, and Profile is implemented
  - Project builds successfully
- **Duration:** 17m 22s

### Task_2_Dashboard_Scanning: Implement the Dashboard UI with 'Items Expiring Soon' and CameraX integration for scanning receipts/waste.
- **Status:** COMPLETED
- **Updates:** 1.  **Intelligent Dashboard**:
- **Acceptance Criteria:**
  - Dashboard displays expiring items from the database
  - CameraX integration allows capturing images of waste or receipts
  - Gemini AI logic processes images to identify items and save them to Room
  - UI adheres to Material 3 design principles
- **Duration:** 5m 43s

### Task_3_Recipes_Progress_Profile: Develop the Recipe Library with upcycling guides, the Sustainability Tracker (CO2 savings), and User Profile settings.
- **Status:** COMPLETED
- **Updates:** 1.  **Upcycling Recipe Library**:
- **Acceptance Criteria:**
  - Recipe library shows categorized upcycling recipes with difficulty ratings
  - Circular CO2 progress tracker and historical log are functional
  - User Profile (living situation, allergies) filters/personalizes recipes
  - All screens are accessible via navigation
- **Duration:** 4m 35s

### Task_4_Refinement_Verification: Apply final Material 3 visual polish, create an adaptive icon, and perform final verification.
- **Status:** COMPLETED
- **Updates:** 1.  **Material 3 Visual Polish**:
    *   Updated the color palette to a professionally curated **Material 3 Green scheme**, providing high contrast and a vibrant, energetic feel.
    *   Enhanced the `GreenLoopTheme` to support **Dynamic Color** (Android 12+) while providing a robust fallback.
    *   Refined the **Edge-to-Edge** implementation, ensuring the status bar and navigation bar colors seamlessly blend with the app's background.
2.  **Adaptive App Icon**:
    *   Designed and implemented a custom **adaptive icon**.
    *   The icon features a stylized leaf within a circular loop, symbolizing the app's core mission of sustainable upcycling.
    *   Ensured full compatibility with Android's adaptive icon requirements (background and foreground layers).
3.  **Stability & Verification**:
    *   Performed a final build check (`:app:assembleDebug`), which passed successfully.
    *   Verified all navigation transitions and screen layouts for Material 3 compliance.
    *   Confirmed that the app is stable and meets all the functional requirements outlined in the project brief.
- **Acceptance Criteria:**
  - Vibrant, energetic Material 3 color scheme and edge-to-edge display implemented
  - Adaptive app icon is present
  - Final build passes and app is stable (no crashes)
  - Verify application stability and alignment with user requirements; report any UI issues
- **Duration:** 5m 43s


# DigiFridge 📱:

**Turning your fridge from a "passive storage space" into a proactive, zero-waste ecosystem.**

---

## 🌟 Inspiration
We’ve all experienced that Friday evening disappointment: opening the fridge after a long week only to find ingredients we paid for heading straight for the bin. This isn't just a personal frustration; it’s a global crisis. 
* **Financial Impact:** UK households discard **£17 billion** worth of food annually. 
* **The "Oversight" Problem:** Nearly **40%** of that waste is caused simply by forgotten expiration dates.

**DigiFridge** was built to bridge the gap between "buying" and "consuming," ensuring no meal goes to waste just because life got busy.

## ✨ What It Does
DigiFridge transforms a paper receipt into a proactive kitchen assistant using a "Single-Touch" AI pipeline:
1. **AI Receipt Scanning:** A single photo digitises your entire grocery shop into a live inventory using Computer Vision.
2. **Smart Inventory:** Automatically estimates expiration windows for every perishable item.
3. **Waste-Reducing Recipes:** A dynamic generator (powered by Gemini) suggests custom dishes that **prioritise your "at-risk" ingredients**.
4. **CO2 Impact Tracking:** Visualises the environmental impact of every ingredient you rescue.

## 🛠️ Tech Stack
* **Frontend:** Kotlin, Jetpack Compose, Material 3, Room DB (Local Storage).
* **Backend:** OkHttp for networking.
* **AI/ML:** Google Gemini API for receipt parsing and recipe generation.
* **Architecture:** MVVM (Model-View-ViewModel) with Repository pattern.

## 🚀 Accomplishments We're Proud Of
* **One-Input Reality:** We successfully reduced a data-entry nightmare into a seamless, one-click experience.
* **Parsing Consistency:** Our backend consistently handles high-density receipts, delivering concise, error-free digital inventories.
* **Intuitive UX:** A minimalist design that makes sustainable living a frictionless part of a daily routine.

## 🔮 What's Next: The Smart Kitchen
Our roadmap takes GreenLoop from a mobile tool to an **embedded IoT standard**. We are developing a vision to move our tracking logic directly into the next generation of smart fridges. By utilising internal cameras, the fridge itself will autonomously manage your household's carbon footprint—no scanning required.

---

## ⚙️ Installation & Setup

### Prerequisites
* Android Studio (Koala or newer)
* A Gemini API Key
* **Optional** An Android device to run the app on


### Setup
```text
1. Clone the repository to your local machine.
2. Open the project in Android Studio.
4. Sync project with Gradle files.
5. Build and run the project using an emulated device in Android Studio (Or optionally add your physical Android device from the 'My devices' Tab).
```

---

**Developed with :heart: for BathHack 2026**

# Financial Planner – Native Android Application

## Project Overview
This is a full-featured personal finance management application developed as a final project for the Mobile Software Engineering II course at Vidzeme University of Applied Sciences. The app operates completely locally to ensure maximum user privacy and swift offline capabilities.

## Tech Stack & Architecture
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Declarative UI)
* **Architecture:** MVVM (Model-View-ViewModel) with structured data flow
* **Local Database:** Room DB (SQLite Object-Relational Mapping)
* **Reactive Data:** Kotlin Flow & LiveData
* **UI Widgets:** Jetpack Glance (Home Screen Widgets)
* **Hardware Integrations:** CameraX & Google ML Kit (OCR), GPS Location Services
* **Maps Integration:** OpenStreetMap data

## Key Features Implemented
* **Transaction Management & Budgeting:** Complete CRUD operations for income and expenses with automatic monthly limit tracking, reactive data visualization (pie charts/graphs), and custom visual budget alerts.
* **OCR Receipt Scanner:** Integrates the device camera to read text off paper receipts using Optical Character Recognition (OCR), parsing out cost amounts automatically to decrease manual typing.
* **Geolocation Context (Maps):** Leverages device GPS sensors to tag transactions with location data, visualizing expenses using custom-colored interactive map markers.
* **Home Screen Widget:** Built a minimalist Jetpack Glance widget to display real-time updates of current budget metrics directly on the Android home screen.
* **Secure Architecture:** Modern navigation graph separating Guest/Authenticated states, state preference tracking with DataStore Preferences, and a completely localized approach where data is never leaked to external services.

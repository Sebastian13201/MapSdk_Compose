# Custom Map Application 🗺️

A modern Android app built with **MVVM architecture**, showcasing Google Maps integration, custom markers, directions, and more. Built with best practices in mind, including dependency injection (Hilt), Retrofit, and Google Maps APIs.

---

## Features ✨

### Current Features
- **Google Maps Integration**: 
  - Display maps with custom styling and markers.
  - Search locations using **Google Places API**.
  - Render routes with **Google Directions API**.
- **Custom Markers**:
  - Personalized icons and animations for points of interest.
  - Interactive info windows with dynamic content.
- **Custom Directions**:
  - Polylines with custom colors and patterns.
  - Turn-by-turn navigation preview.
- **MVVM Architecture**:
  - Separation of concerns with ViewModel, Repository, and Data layers.
  - LiveData for reactive UI updates.
- **Dependency Injection**:
  - **Hilt** for managing dependencies (Retrofit, ViewModel, etc.).
- **Retrofit & Coroutines**:
  - Network calls to Google APIs using Retrofit.
  - Asynchronous operations with Kotlin Coroutines.
- **User-Friendly UI**:
  - Material Design components.
  - Responsive search and route planning.

---

## Tech Stack 🛠️

- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + Gson
- **Maps & Location**:
  - Google Maps SDK
  - Google Places API
  - Google Directions API
- **Image Loading**: Coil
- **Asynchronous**: Kotlin Coroutines + Flow
- **Database**: Room (if applicable)

---

## Installation 🚀

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/custom-map-app.git

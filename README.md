# 📸 Flickr Browser – Mobile Application

A modern Android application for **exploring and discovering stunning photos** from Flickr, built with **Material Design 3**, **Java**, and **Android Jetpack** components.

Developed by a student team from **University of Science and Technology of Hanoi (USTH)** as part of the *Mobile Application Development* course.

---

## 🌟 Overview

**Flickr Browser** allows users to explore trending and curated photos directly from Flickr or Pexels APIs.  
The app is designed to be fast, intuitive, and visually appealing — following the latest **Material 3** guidelines.

Users can:
- Browse high-quality photos in a responsive **grid layout**
- View full-size photo details with smooth transitions
- Add favorite photos to a personal list
- Search photos by keyword or tags
- Share, download, or open photos in browser
- Experience a unified light theme using the **Flickr Blue palette**

---

## 🧩 Features

| Feature | Description |
|----------|-------------|
| 🏠 **Home** | Displays curated or recent photos from the Flickr/Pexels API. |
| 🔍 **Search** | Allows searching by keyword or tags with real-time results. |
| 🖼️ **Explore Grid** | Infinite scrolling grid with smooth image loading and skeleton shimmer. |
| ❤️ **Favorites** | Save and manage your favorite photos locally. |
| 📄 **Detail View** | View photo metadata (title, owner, tags) and perform actions (Share, Download, Open). |
| ⚙️ **Design System** | Fully implemented with **Material Design 3** tokens, including colors, typography, and spacing. |
| 🌐 **Offline Handling** | Graceful fallback when API or network fails. |

---

## 🛠️ Tech Stack

| Category | Technology |
|-----------|-------------|
| **Language** | Java |
| **Framework** | Android Jetpack |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Networking** | Volley / Retrofit |
| **Image Loading** | Glide |
| **UI Components** | Material 3 (Material Components for Android) |
| **Data Handling** | LiveData, ViewModel, SharedPreferences |
| **API Integration** | Flickr REST API (and optional Pexels API fallback) |
| **Version Control** | Git & GitHub |

---

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/hoangdung0703/FlickrBrowser.git
cd FlickrBrowser
```
### 2. Open in Android Studio
- Use Android Studio Flamingo (or newer)
- Gradle version: 8.x
- SDK target: Android 14 (API 34)

### 3. Set up API Keys
- Add your Flickr API Key or Pexels API Key in:
```bash
app/src/main/java/vn/edu/usth/flickrbrowser/core/api/ApiConfig.java
```

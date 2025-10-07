Flickr Mobile App

A modern, native Android application for exploring, searching, and saving photos from Flickr. This project is built with a focus on a clean user interface, a robust architecture, and a seamless user experience, following the latest Material 3 design guidelines.

✨ Features
Our application provides a set of features designed for photo enthusiasts:

Explore Curated Photos: The home screen displays the latest curated photos from Pexels in a beautiful two-column grid. It includes a smooth shimmer effect while loading, pull-to-refresh functionality, and an elegant empty state view if no photos are available.

Powerful Search: Users can search for photos using keywords. The search results are displayed in the same reusable grid, with support for loading states, empty results, and network error handling.

Detailed Photo View: Tapping on any photo opens a detail screen where the image is displayed in high resolution. From here, users can easily navigate back, share the photo, or open it in an external browser.

Favorites Collection: Users can mark any photo as a favorite by tapping a heart icon. All favorite photos are saved locally and can be viewed on a dedicated Favorites screen, even without an internet connection.

Share & Open: Easily share a link to a photo with other apps or open it directly in a web browser. The app intelligently checks for network connectivity before performing these actions to prevent errors.

Robust Network Handling: The app gracefully handles network errors, such as a lack of internet or timeouts, by displaying informative toasts to the user instead of crashing.

🛠️ Tech Stack & Architecture
This project is built using modern Android development tools and practices.

Programming Language: Java

Architecture: MVVM (Model-View-ViewModel)

Asynchronous Programming: Asynchronous task handling for network calls using callbacks.

UI Toolkit:

Material 3: For a modern and consistent design system across the app.

View Binding: For safe and easy access to UI components.

RecyclerView: To efficiently display grids of photos.

Networking:

Retrofit & OkHttp: For making robust and efficient API calls to the Pexels service.

ConnectivityManager: To check for internet availability before making network requests.

Image Loading:

Glide: For loading, caching, and displaying images smoothly, with support for placeholders and error states.

🚀 Getting Started
To build and run this project locally, you will need to have Android Studio installed. Follow these steps:

1. Clone the repository
git clone [https://github.com/your-organization/pexels-app.git](https://github.com/your-organization/pexels-app.git)
cd pexels-app

2. Set up the API Key
The application uses the Pexels API to fetch photos. You will need to provide your own API key to build the project.

Open the local.properties file in the root directory of the project. If the file does not exist, create it.

Add your Pexels API key to the file as follows:

PEXELS_API_KEY="YOUR_PEXELS_API_KEY_HERE"

3. Build and Run the App
Open the project in Android Studio, let Gradle sync the dependencies, and then press the "Run" button to install the app on an emulator or a physical device.

🤝 Contribution
This project was developed by a dedicated team of students from USTH (University of Science and Technology of Hanoi):

Minh Đức

Minh Hiếu

Xuân Hiếu

Quang Dũng

Huỳnh Dũng

Hoàng Dũng

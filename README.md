# DNMotors

DNMotors is a mobile dealership app that allows car dealers to post and manage their vehicle listings. Authorized dealers from brands like Toyota, BMW, Audi, and others can log in with a dealer role and access a dealer panel where they can manage their inventory and communicate directly with users.

## 🚗 App Features

### 🔑 Authentication
- Login via Firebase Auth (email/password)
- Google sign-in support
- Role-based access:
  - user — Regular customer
  - dealer — Authorized dealer

---

## 👥 Roles & Functionality

### For Users
- Browse available cars in the car list
- View car details page:
  - Photo gallery
  - Specifications
  - 360° car view (swipe or tap left/right to see from every angle)
  - Test drive videos (watch inside the app or open in YouTube)
  - Call button (automatically opens the dialer with dealer's number)
  - Chat with dealer (supports text and voice messages)
  - Share car information with friends
- Use Loan Calculator:
  - Calculate loans for 12 to 84 months
  - Submit a loan application
- Compare cars side by side by specifications
- Add cars to Favorites
- Chat with dealers:
  - Chat shows dealer name, car image, car model, and latest message
  - Supports text and voice messages
  - Receive push notifications for new messages (tap to open chat instantly)
  - Each chat uses unique ID (val chatId = generateChatId(carId, dealerId, userId)) to ensure privacy
- Profile Management:
  - Edit avatar, background image
  - Update location, name, phone number
  - Change password
  - Switch language (Kazakh, Russian, English supported)
  - Logout

---

### For Dealers
- Access to Dealer Panel (built with Jetpack Compose)
- Manage car listings:
  - Add new cars (via input fields or JSON text)
  - Delete cars
  - View added cars (data saved in Firebase Firestore)
- Chat with users:
  - Supports text and voice messages
  - Chat shows dealer name, car image, car model, and latest message
  - Each chat uses unique ID (val chatId = generateChatId(carId, dealerId, userId)) to ensure privacy
  - Receive push notifications for new messages (tap to open chat instantly)
- Profile Management:
  - Edit avatar, background, location, name, phone
  - Change password
  - Switch language (Kazakh, Russian, English supported)
  - Logout

---

## 🔥 Technologies Used

### Core
- Kotlin
- Firebase Firestore — Stores car data and chat
- Firebase Auth — Authentication (Email & Google Sign-In)
- Firebase Cloud Messaging (FCM) — Push notifications
- Imgur API — Upload car images manually
- Glide — Image loading (for all photos,)
- Jetpack Compose — Dealer panel
- XML Layouts — User panel
- Change Password Library:
implementation("com.github.pp2-22B030488:changepasswordscreenlib:1.0.0")
### Android Jetpack
- ViewModel + LiveData
- Navigation Component
- WorkManager
- Media3 — For video playback
### Networking
- Retrofit + Gson — API calls (e.g., Imgur)
- OkHttp Logging Interceptor
### Image Libraries
- Glide — For dynamic image loading
- Picasso — Also used in some parts
### Dependency Injection
- Koin — Lightweight DI for Android

---

## 📄 JSON Example (Add Car)
Dealers can add cars using a JSON text format like this:

```json
{
  "vin": "WAUZZZF2XLN012345",
  "brand": "Audi",
  "model": "A7 Sportback",
  "year": 2020,
  "generation": "C8",
  "price": 28500000,
  "transmission": "Automatic",
  "driveType": "Quattro (AWD)",
  "fuelType": "Petrol",
  "location": "Astana, Kazakhstan",
  "description": "Audi A7 Sportback 2020 — elegant, practical, and powerful.",
  "previewUrl": "https://link_to_preview.jpg",
  "imageUrl": [
    "https://link1.jpg",
    "https://link2.jpg",
    "https://link3.jpg"
  ],
  "image360Url": [
    "https://link_to_360_1.jpg",
    "https://link_to_360_2.jpg"
  ],
  "testDriveUrl": "https://link_to_test_drive_form",
  "bodyType": "Liftback",
  "engineCapacity": "2.0L I4 TFSI",
  "mileageKm": 41000,
  "condition": "Used"
}

```

![photo_2025-05-03_09-40-52](https://github.com/user-attachments/assets/7c1dacee-9e6e-4bcc-a11a-4c369f6aa76b)
![photo_2025-05-03_09-41-04](https://github.com/user-attachments/assets/4bdab12c-77e3-41f7-86f5-69a26abd0be9)
![photo_2025-05-03_09-41-01](https://github.com/user-attachments/assets/e23a28b7-daeb-4f7b-88e5-df041f1faa9a)

🚀 Getting Started
Clone the repository: 

git clone https://github.com/pp2-22B030488/DNMotors-Android-.git

Open the project in Android Studio  

Install dependencies and run on emulator or physical device  
  

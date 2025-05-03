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
  - Call button (automatically opens the dialer with dealer's number)
  - Chat with dealer
  - Share car information with friends
- Use Loan Calculator:
  - Calculate loans for 12 to 84 months
  - Submit a loan application
- Compare cars side by side by specifications
- Add cars to Favorites
- Chat with dealers:
  - Supports text and voice messages
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
- Profile Management:
  - Edit avatar, background, location, name, phone
  - Change password
  - Switch language
  - Logout

---

## 🔥 Technologies Used
- Kotlin
- Firebase Firestore — stores car data
- Firebase Auth — user authentication
- Firebase Storage — stores media
- Jetpack Compose — Dealer panel
- XML Layouts — User panel
- Change Password Library:
implementation("com.github.pp2-22B030488:changepasswordscreenlib:1.0.0")

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
"description": "Audi A7 Sportback 2020 — a blend of coupe elegance and sedan practicality with Quattro AWD. Powered by 2.0 TFSI, 249 hp engine, 0-100 km/h in 6.2 sec. Unique with dynamic silhouette, LED matrix lights, and premium triple-screen interior. Perfect for those who value style, tech, and comfort in daily Kazakhstan drives.",
"previewUrl": "https://upload.wikimedia.org/wikipedia/commons/1/16/Audi_A7_Sportback_45_TFSI_quattro_S_line.jpg",
"imageUrl": [
  "https://upload.wikimedia.org/wikipedia/commons/1/16/Audi_A7_Sportback_45_TFSI_quattro_S_line.jpg",
  "https://upload.wikimedia.org/wikipedia/commons/3/3a/Audi_A7_Sportback_45_TFSI_quattro_S_line_Rear.jpg",
  "https://upload.wikimedia.org/wikipedia/commons/e/ea/Audi_A7_Sportback_C8_interior.jpg"
],
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

git clone https://github.com/DarkhanTastanov/AndroidAdvancedDNMotors.git  

Open the project in Android Studio  

Install dependencies and run on emulator or physical device  
  
  
    
Developers:  

Darkhan Tastanov    

Kuanysh Nursultan  

Kabylzhaparov Alinur  

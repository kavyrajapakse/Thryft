# Thryft

Thryft is a thrift shopping platform with two main parts:
- an Android mobile app for customers
- a web-based admin dashboard for store operations

The project combines Firebase services (Auth, Firestore, Storage, Analytics, Messaging) with a Java-based Android application and an optional Java Servlet backend endpoint for admin statistics.

## Table of Contents
- Overview
- Key Features
- Tech Stack
- Architecture
- Project Structure
- Getting Started
- Android App Setup
- Admin Dashboard Setup
- Optional Java Servlet Backend Setup
- Data Model (High Level)
- Security and Configuration Notes
- CV/Portfolio Summary
- Future Improvements

## Overview
Thryft helps users discover and purchase thrift products through a native Android app, while allowing administrators to manage products, users, and orders through a browser-based dashboard.

## Key Features
### Customer Mobile App (Android)
- User authentication (sign up/login)
- Browse products by category
- Product detail views with images
- Search functionality
- Cart and checkout flow
- Order history and order details
- Profile and alerts/notifications screens
- Location/maps integration
- Firebase Cloud Messaging support

### Admin Dashboard (Web)
- Secure login (Firebase Auth)
- Dashboard with totals and summary cards
- Product management
  - add products
  - update products
  - view product list
- User management with status control
- Order management with order status updates
- Product, user, and order report printing
- Firestore-backed real-time style data operations

### Backend (Optional Demo API)
- Java Servlet endpoint for admin statistics
- JSON response endpoint mapped to `/api/admin-stats`

## Tech Stack
### Mobile App
- Java 11
- Android SDK (minSdk 28, targetSdk 36)
- Gradle (Android Gradle Plugin)
- ViewBinding

### Firebase
- Firebase Authentication
- Cloud Firestore
- Firebase Storage
- Firebase Analytics
- Firebase Cloud Messaging

### Networking and Utilities
- Retrofit
- OkHttp
- Gson
- Glide
- Google Maps SDK + Maps Utils
- PayHere Android SDK

### Admin Panel
- HTML5
- CSS3
- JavaScript (ES Modules)
- Tailwind CSS (CDN)
- Firebase Web SDK

### Optional Backend
- Java Servlets (web.xml based)
- Apache Tomcat / GlassFish style deployment

## Architecture
- Mobile app communicates directly with Firebase services for authentication, product data, media, orders, and messaging.
- Admin dashboard uses Firebase Auth + Firestore + Storage for management tasks.
- Optional Java Servlet backend provides a separate HTTP endpoint for admin statistics demonstration.

## Project Structure
```text
Thryft/
├─ app/                  # Android customer app
├─ admin/                # Admin web dashboard
│  ├─ backend/           # Optional Java Servlet backend demo
│  ├─ index.html         # Admin login
│  ├─ dashboard.html     # Admin overview
│  ├─ products.html      # Product listing
│  ├─ add-product.html   # Add product form
│  ├─ update-product.html
│  ├─ users.html
│  ├─ orders.html
│  └─ js/
│     ├─ firebase.js
│     ├─ auth.js
│     └─ add-product.js
├─ build.gradle
├─ settings.gradle
└─ gradle/
```

## Data Model (High Level)
Firestore collections used by the admin and app include:
- products
- categories
- users
- orders
- -notifications

(Exact fields can evolve with feature updates.)

## Future Improvements
- Role-based access control for admin users
- CI/CD pipeline for Android and web deployments
- Better analytics dashboards and reporting exports
- Unit/UI test coverage expansion
- Dockerized backend deployment


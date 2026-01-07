# 🏋️ Messi GOAT GYM Management System

**A robust desktop application for managing fitness center operations, built with Java Swing and Hibernate.**

## 📖 About
The **Messi GOAT GYM System** is a CRUD application designed to streamline the administrative tasks of a modern gym. It utilizes the **MVC (Model-View-Controller)** architectural pattern to ensure clean code separation. The system manages the relationships between gym members, trainers, and fitness activities, providing real-time validation and statistical insights.

## ✨ Key Features

### 👥 Client Management
* **Full CRUD Operations:** Add, edit, and remove gym members.
* **Smart Validation:**
    * **Age Verification:** Automatically rejects users under 18 years old.
    * **ID/DNI Check:** Validates government ID formats (8 digits + char) and prevents duplicates.
* **Auto-ID Generation:** Systematically generates unique member codes (e.g., `S005` -> `S006`).

### 📅 Activity & Trainer Scheduling
* **Conflict Prevention:** The system performs logic checks to ensure a trainer cannot be assigned to two different activities at the same date and time.
* **Dynamic Rosters:** Assign trainers to specific classes.
* **Capacity Management:** Track activity details including pricing and descriptions.

### 🔗 Enrollment System
* **Class Sign-up:** Seamlessly enroll clients into activities (Many-to-Many relationship management) via a dedicated UI panel.
* **Search & Filter:** Real-time filtering of data tables to quickly find clients or activities.

### 📊 Analytics & Financials
* **Revenue Calculation:** automatically calculates projected revenue based on activity prices and member categories.
* **Discount Logic:** Applies category-based discounts (Category B: 10%, C: 20%, D: 30%).
* **Demographics:** Calculates the average age of participants for specific activities.

## 🛠️ Tech Stack
* **Language:** Java (JDK 17+)
* **GUI Framework:** Java Swing (Custom Table Models, Renderers, Event Listeners)
* **ORM:** Hibernate (JPA)
* **Database:** MariaDB / MySQL
* **Architecture:** MVC (Model-View-Controller)

## 🚀 Getting Started

1.  **Database Setup:** Ensure a MariaDB/MySQL instance is running on port `3306`.
2.  **Configuration:** Verify the `hibernate.cfg.xml` file is present in the classpath.
3.  **Run:** Launch the application via `ConnectionController.java`.
4.  **Login:** Enter your database credentials in the login window to establish the connection.

---
*Developed for the "Messi GOAT GYM" project.*

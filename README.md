# 🚢 Cargo Load Optimizer

A lightweight, high-performance web application that calculates the most valuable combination of items that can be loaded into a cargo container without exceeding its weight capacity.

Built using **Pure Java (no frameworks)** and **HTML/CSS** — no JavaScript, no external dependencies.

---

## ✨ Features

- 🧠 **Dynamic Programming Engine**
  - Implements the classic **0/1 Knapsack Algorithm** to guarantee optimal results.

- ⚡ **Pure Java Backend**
  - Uses `com.sun.net.httpserver` (built into JDK)
  - No Tomcat, no Spring, no frameworks

- 🌐 **Zero JavaScript UI**
  - Fully functional using plain HTML forms
  - Clean and simple user experience

- 📦 **Docker Support**
  - Easily deployable on cloud platforms like:
    - Render
    - Railway
    - AWS

---

## 🛠️ Tech Stack

- **Backend:** Java (JDK 17+)
- **Server:** Built-in Java HTTP Server
- **Frontend:** HTML, CSS
- **Algorithm:** 0/1 Knapsack (Dynamic Programming)
- **Deployment:** Docker

---

## 📂 Project Structure

```
Cargo-Load-Optimizer/
│
├── WebServer.java
├── Item.java
├── templates/
├── Dockerfile
└── README.md
```

---

## 🚀 How to Run Locally

### ✅ Prerequisites
- Java 17 or higher installed

### 1️⃣ Clone the Repository
git clone https://github.com/rohzyy/Cargo-Load-Optimizer.git  
cd Cargo-Load-Optimizer

### 2️⃣ Compile
javac WebServer.java

### 3️⃣ Run
java WebServer

### 4️⃣ Open
cargo-load-optimizer.onrender.com/

---

## 📜 License
MIT License

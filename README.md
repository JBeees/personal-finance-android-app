# Vault — Personal Finance Manager

A minimalist Android app to manage your personal finances.

## Features

- **Available Balance** — Track money available for daily use
- **Daily Expenses** — Log spending by category (food, transport, entertainment, others); balance updates automatically
- **Add Balance** — Top up your available funds anytime
- **Savings** — PIN-protected savings vault; separate from spendable balance
- **Weekly / Monthly / Yearly Summary** — Spending breakdown with bar chart and category analysis

## Tech Stack

- **Language:** Java
- **Database:** SQLite (local, on-device)
- **Charts:** [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- **UI:** Material Components, ConstraintLayout
- **Min SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 8+

### Installation

1. Clone the repo
   ```bash
   git clone https://github.com/JBeees/personal-finance-android-app.git
   ```
2. Open in Android Studio via **File → Open**
3. Wait for Gradle sync to finish
4. Run on an emulator or physical device (Android 7.0+)

## Project Structure

```
app/src/main/
├── java/com/financeapp/
│   ├── SplashActivity.java       # Intro screen
│   ├── MainActivity.java         # Dashboard
│   ├── PinActivity.java          # PIN entry & confirmation
│   ├── SetupPinActivity.java     # First-time PIN setup
│   ├── SavingsActivity.java      # PIN-protected savings screen
│   ├── SummaryActivity.java      # Spending summary & charts
│   ├── DatabaseHelper.java       # SQLite database layer
│   └── FormatUtils.java          # Currency & date formatting
└── res/
    ├── layout/                   # XML layouts
    ├── values/                   # Colors, strings, themes
    └── drawable/                 # Shapes & backgrounds
```

## Privacy

All data is stored **locally on your device**. No internet connection required, no data is sent to any server.

## License

```
MIT License

Copyright (c) 2026

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

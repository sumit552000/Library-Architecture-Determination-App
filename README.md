# 📖 Library Architecture Determination (Android App)

## 🎯 Objective
This Android app scans a user‑selected folder for native library files (`.so`), determines the **CPU architecture (ABI)** for each library by reading its **ELF header**, and displays the results in a table format.  

It works with any input folder selected by the user — no hardcoded paths.

---

## ✨ Features
- 📂 Select any folder using **Storage Access Framework (SAF)**.
- 🔎 Detects CPU **architecture type automatically** by parsing ELF headers.
- 🛠 Supports multiple architectures:
  - ARM (32-bit / armeabi-v7a)
  - ARM64 (AArch64 / arm64-v8a)
  - x86, x86-64
  - MIPS
  - RISC-V, PowerPC, IA-64 (and more from ELF spec).
- 📊 Displays results in a **formatted monospaced table**.
- 🚫 Ignores non-`.so` files.

---

## 📦 Installation Options

### ✅ Option 1: Run via APK (Recommended for quick test)
1. I have attached a **ready-to-install APK** with this project.
2. Copy the APK to your Android device.
3. Enable **Install from Unknown Sources** in device settings if required.
4. Tap the APK file to install it.
5. Open the app from your device’s launcher.

### ⚙️ Option 2: Build & Run via Android Studio
1. Open this project in **Android Studio**.
2. Connect an Android device (or start an emulator).
3. Run the project (`Run → Run 'app'` or press **Shift+F10**).
4. The app will install and launch automatically on the device/emulator.

---

## 🛠 How It Works
1. Tap **Pick Folder** → select the folder containing `.so` files (e.g., `Downloads/sample_libs`).
2. Tap **Check Libraries** → the app scans all `.so` files in the folder.
3. For each `.so` file:
   - Confirms ELF format (`0x7F 'E' 'L' 'F'` magic bytes).
   - Detects endianness from ELF header.
   - Reads the `e_machine` field to determine architecture.
   - Maps it to ABI name (or shows as `Unknown (e_machine=XYZ)` if not recognized).
4. Results are displayed in a formatted table on screen.

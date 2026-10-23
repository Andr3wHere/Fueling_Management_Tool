# Instrukce k sestavení a spuštění projektu

Tento dokument obsahuje technické požadavky a kroky potřebné pro naklonování, zkompilování a spuštění aplikace **Fueling Management Tool**.

---

## ⚙️ Požadavky na prostředí (Prerequisites)

* **Operační systém:** Windows 10/11, macOS, nebo Linux
* **Vývojové prostředí:** [Android Studio](https://developer.android.com/studio) (doporučena verze Ladybug / Meerkat 2024.2+ nebo novější)
* **Java Development Kit (JDK):** Java 17 (standardně součástí Android Studia jako Embedded JDK)
* **Android SDK:**
  * `compileSdk`: **35** (Android 15)
  * `minSdk`: **29** (Android 10.0 Q)
  * `targetSdk`: **35**

---

## 📂 Struktura repozitáře

* `App/` – Kořenový adresář Android projektu (obsahuje Gradle skripty a modul `app/`).
* `README.md` – Shrnutí vize, cílů a rozsahu projektu.
* `BUILD.md` – Tento návod k sestavení.

> **Důležité upozornění:** V Android Studiu vždy otevírejte podsložku **`App`**, nikoliv kořenový adresář celého repozitáře.

---

## 🚀 Postup zprovoznění v Android Studiu

1. **Naklonování repozitáře:**
   ```bash
   git clone https://github.com/Andr3wHere/Fueling_Management_Tool
   cd Fueling_Management_Tool
   ```

2. **Otevření v IDE:**
   * Spusťte Android Studio.
   * Zvolte **File -> Open...** (nebo na úvodní obrazovce tlačítko **Open**).
   * Vyberte podsložku **`App`** uvnitř repozitáře a potvrďte kliknutím na **OK**.

3. **Synchronizace Gradle:**
   * Android Studio po otevření automaticky spustí synchronizaci Gradle závislostí.
   * V případě potřeby lze synchronizaci vyvolat ručně kliknutím na ikonu **Sync Project with Gradle Files** (ikona slona v pravém horním panelu).

4. **Spuštění na emulátoru / fyzickém zařízení:**
   * V horní liště vyberte cílové zařízení (Android Virtual Device s API $\ge 29$ nebo připojený telefon s povoleným USB laděním).
   * Spusťte aplikaci stisknutím zeleného tlačítka **Run** (`Shift + F10`).

---

## 💻 Sestavení přes příkazovou řádku (CLI)

Před spuštěním Gradle příkazů se v terminálu přesuňte do podsložky `App`:

```bash
cd App
```

### 1. Sestavení Debug APK
* **Linux / macOS:**
  ```bash
  ./gradlew assembleDebug
  ```
* **Windows (PowerShell / CMD):**
  ```powershell
  .\gradlew.bat assembleDebug
  ```
Výsledný instalační balíček `.apk` bude vygenerován v cestě:  
`App/app/build/outputs/apk/debug/app-debug.apk`

### 2. Kompletní sestavení a kontrola testů
Pro ověření integrity projektu a spuštění jednotkových testů:
* **Linux / macOS:**
  ```bash
  ./gradlew build
  ```
* **Windows (PowerShell / CMD):**
  ```powershell
  .\gradlew.bat build
  ```

Příkaz musí skončit hlášením **`BUILD SUCCESSFUL`**.

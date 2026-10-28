# Specifikace referenčního zařízení (Reference Device Specification)

| **Typ zařízení** | Virtuální emulátor Android Virtual Device (AVD) |
| **Základní model** | Google Pixel 4 |
| **Cílová verze OS** | Android 10.0 (Q) |
| **API Level** | 29 (`minSdk`) |
| **Systémový obraz** | Google Play System Image |
| **Účel** | Jednotná norma prostředí pro vývoj a testování UI, CameraX a Fused Location Provider |

---

## 1. Technické parametry AVD profilu

### 1.1. Hardwarový profil (Hardware Definition)
* **Předloha zařízení:** Pixel 4
* **Úhlopříčka displeje:** 5.7"
* **Rozlišení:** 1080 × 2280 px (poměr stran 19:9, standardní moderní viewport)
* **Hustota pixelů:** 440 dpi (kategorie `xxhdpi`)
* **Senzory:** Akcelerometr, Gyroskop, GPS

### 1.2. Systémový obraz (System Image)
* **Release Name:** Q
* **API Level:** 29
* **Services:** **Google Play Store** (vyžadováno pro běh Google Play Services – Fused Location Provider a Google Drive API)

### 1.3. Pokročilá konfigurace (Advanced Settings)
* **Operační paměť (RAM):** `2 GB`
* **VM Heap:** `256 MB`
* **Interní úložiště (Internal Storage):** `6 GB`
* **Zadní fotoaparát (Rear Camera):**
  * **Možnost A (při existenci webkamery):** `Webcam0` (stream z fyzické webkamery počítače).
  * **Možnost B (bez fyzické kamery – výchozí fallback):** `VirtualScene` (emulovaná 3D virtuální místnost).
* **Přední fotoaparát (Front Camera):** `None` nebo `Emulated`
* **Síťové připojení:** Emulated Network

---

## 2. Návod na zprovoznění v Android Studiu (Krok za krokem)

1. **Otevření správce zařízení:**
   * V Android Studiu otevřete postranní panel **Device Manager** (`Tools -> Device Manager`).
   * Klikněte na tlačítko **Create Device** (ikona `+`).

2. **Výběr hardwarové definice:**
   * V kategorii **Phone** vyberte ze seznamu zařízení **Pixel 4**.
   * Zkontrolujte parametry: *Resolution: 1080 × 2280, Density: 440 dpi*.
   * Klikněte na **Next**.

3. **Výběr systémového obrazu:**
   * V záložce se systémovými obrazy vyhledejte položku:
     * Název zařízení nastavte na: `Pixel_4_API_29_Fueling_Ref`.
     * **API:** `API 29 "Q"; Android 10.0`
     * **Services** `Google Play Store`
     * **System Image:** `Google Play Intel x86 Atom System Image`
       * *V případě ARM zařízení zvolte `Google Play ARM 64 v8a System Image`*
   * Pokud není obraz stažen, klikněte na odkaz **Download** *(Ikona před názvem Image)*.
   * Po stažení obraz označte a klikněte nahoře na záložku **Additional settings**.

4. **Konfigurace pokročilých parametrů:**
   * V záložce pokročilého nastavení vyhledejte položku:
     * **Camera -> Rear:** zvolte `Webcam0` (při dostupné webkameře) nebo `VirtualScene` (pokud nemáte fyzickou kameru).
     * **Storage -> Internal Storage:** nastavte `6` GB.
     * **Emulated Performance -> RAM:** nastavte `2` GB.
   * Dokončete kliknutím na **Finish**.

---

## 3. Ověření připravenosti emulátoru (Verification)

Před zahájením implementace je nutné ověřit funkčnost klíčových subsystémů přímo v emulátoru:

### 3.1. Ověření fotoaparátu bez fyzické kamery (`VirtualScene`)
Pokud počítač nedisponuje webkamerou, emulátor využívá režim virtuální místnosti:
1. Spusťte emulátor `Pixel_4_API_29_Fueling_Ref`.
2. V systému emulátoru otevřete výchozí systémovou aplikaci **Camera**.
3. Obrazovka zobrazí renderovanou 3D místnost. Pohyb v prostoru a zaměřování se ovládá podržením klávesy `Shift` (nebo `Option` na macOS) v kombinaci s pohybem myši a klávesami `W`, `A`, `S`, `D`.
4. **Vložení vlastního testovacího obrázku stojanu do virtuální scény:**
   * V ovládacím panelu emulátoru (nahoře nad zařízením) klikněte na ikonu tří teček (**...** – *Extended controls*).
   * Přejděte do záložky **Camera**.
   * V sekci *Wall* nebo *Table* klikněte na **Add image** a nahrajte testovací fotografii displeje stojanu (formát PNG/JPG).
   * V aplikaci fotoaparátu namiřte pohled na příslušnou stěnu/stůl – vložený obrázek se zobrazí v hledáčku. Tímto je ověřena schopnost emulátoru poskytovat obrazový vstup pro CameraX *(Místnost s obrázky není stejná jako místnost, ve které se kamera otevře)*.

### 3.2. Ověření geolokačních služeb (GPS)
1. V plovoucím bočním panelu emulátoru klikněte na ikonu tří teček (**...** – *Extended controls*).
2. Přejděte do sekce **Location**.
3. Do pole Google Map zadejte testovací lokaci (např. `Ostravská univerzita`).
4. Klikněte na **Set Location**.
5. V emulátoru otevřete aplikaci Google Maps a ověřte, že se modrý bod polohy okamžitě přesunul na zadané souřadnice. Tímto je ověřena funkčnost Fused Location Provideru na úrovni systému.

### 3.3. Možné problémy
* Je možné že pro správnou funkci všech částí bude potřeba aktualizovovat služby Google Play a aplikaci Google Mapy. *(Tento krok vyžaduje validní Google účet)*

---

## 4. Důvod volby a princip referenčního zařízení

Pro zajištění reprodukovatelnosti chování aplikace, sjednocení vývojového prostředí v týmu a transparentní předávání díla zákazníkovi je stanoveno **právě jedno závazné referenční zařízení**. 

Tímto zařízením je virtuální emulátor (AVD) v prostředí Android Studio. Volba emulátoru garantuje:
* **Dostupnost:** Všichni členové vývojového týmu i zákazník mají k dispozici identické prostředí bez nutnosti nákupu či shánění specifického fyzického telefonu.
* **Soulad s `minSdk`:** Konfigurace cílí na minimální podporovanou verzi systému (**Android 10.0 / API 29**), což zaručuje spolehlivý běh na všech verzích od Android 10.0 až po Android 15+.
* **Standardizace hardware modulů:** Umožňuje jednotnou konfiguraci kamery pro CameraX a simulaci GPS souřadnic pro Fused Location Provider.

Důvod výběru modelu Pixel 4:
* Čistý referenční standard Google.
* Přirozená kompatibilita s API 29.
* Plná podpora Camera2 API pro potřebu knihovny CameraX.
* Nižší HW náročnost při virtualizaci (2GB RAM).

*(Poznámka: Testování na fyzických zařízeních členů týmu slouží pouze jako doplňková verifikace, AVD profil je jedinou formální normou pro akceptaci).*
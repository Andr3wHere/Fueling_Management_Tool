# Nástroj pro správu tankování (Fueling Management Tool)

Jednoduchá mobilní aplikace pro systém Android určená k evidenci a správě výdajů za tankování pohonných hmot. Aplikace odstraňuje nutnost zdlouhavého ručního přepisování údajů díky automatickému rozpoznávání načerpaného množství a ceny přímo z fotografie displeje stojanu a stavu tachometru.

Projekt je vyvíjen v pětičlenném týmu (Ondřej Dostál, Jakub Šproch, Jiří Vávra, Kateřina Lucie Vlachová, Marek Drábek).

---

## 🎯 Cíl projektu

* **Zjednodušit evidenci tankování:** Uživatel vyfotí displej stojanu a aplikace automaticky detekuje natankovaný objem a cenu s možností okamžité ruční kontroly či opravy.
* **Evidence stavu tachometru a spotřeby:** Záznam stavu tachometru vyfocením pro automatický výpočet ujetých kilometrů a statistik spotřeby.
* **Geolokační metadata a mapa:** Automatické získání GPS souřadnic místa tankování s možností ručního upřesnění místa přes vyskakovací Google Maps.
* **Grafy a statistiky:** Přehledná historie tankování s cenami, grafy měsíční spotřeby za sledované období a statistiky ujetých kilometrů.
* **Offline-first a zálohování na Google Drive:** Veškerá data se primárně ukládají do lokální databáze v zařízení, takže aplikace funguje plně offline. Pro ochranu a přenositelnost dat je integrováno cloudové zálohování a obnova přes uživatelský Google účet (Google Drive).
* **Ochrana soukromí a paměti:** Samotné fotografie stojanů ani tachometru se neukládají – ukládají se pouze vytěžená data, číselné hodnoty, datum, čas a souřadnice.

---

## 🏛️ Architektura systému

Architektura je navržena jako **offline-first** a dělí se na dvě klíčové části:

1. **Klientská aplikace (Android):**
   * **UI:** Kotlin + Jetpack Compose
   * **Lokální databáze:** Room (SQLite) – primární úložiště všech záznamů
   * **Pořizování a zpracování obrazu:** CameraX + Google ML Kit (lokální On-Device OCR běžící přímo v zařízení bez odesílání snímků na server)
   * **Lokalizace a mapy:** Android Location Services (GPS) + Google Maps SDK pro zobrazení a manuální upřesnění polohy čerpací stanice
2. **Cloudové zálohování:**
   * Google Drive API s přihlášením přes Google účet pro export a obnovu databáze při změně zařízení.

---

## 📋 Rozsah projektu (Scope)

### Co aplikace řeší (In Scope):
* Nativní aplikace pro mobilní zařízení se systémem Android (`minSdk 28`, `compileSdk 35`).
* Evidence a správa profilů vozidel (podpora více vozidel v jedné aplikaci).
* Pořizování fotografií stojanů (litry, cena) a tachometru (ujeté km) s OCR detekcí.
* Možnost manuální úpravy a validace rozpoznaných hodnot před uložením.
* Automatické zachycení GPS polohy s vyskakovací mapou Google Maps pro ruční upřesnění.
* Detailní historie tankování, přehled cen a výdajů.
* Grafy měsíční spotřeby a statistiky ujetých kilometrů.
* Lokální ukládání (Room / SQLite) s možností zálohy a obnovy databáze přes Google Drive.

### Co aplikace neřeší (Out of Scope):
* Podporu pro platformu iOS ani webové rozhraní.
* Firemní flotilovou správu vozidel a přiřazování řidičů.
* Vyhledávání nejlevnějších čerpacích stanic v okolí.

---

## 🗺️ Roadmapa vývoje

| Milník | Cíl a plánované funkce |
| :--- | :--- |
| **Do prosince**<br>*(Funkční základ aplikace)* | • Zdokumentování architektury aplikace.<br>• Základní struktura UI v Jetpack Compose.<br>• Pořizování fotografií stojanů přes CameraX.<br>• OCR rozpoznávání ceny a množství paliva pomocí Google ML Kit.<br>• Možnost manuální opravy hodnot před uložením.<br>• Lokální databáze Room (SQLite), ukládání a zobrazení záznamů tankování. |
| **Do března**<br>*(Správa vozidel a statistiky)* | • Profily vozidel (evidence více aut, přiřazení tankování k vozu).<br>• Zobrazení historie a detailu tankování vybraného vozu.<br>• Základní statistiky: celkové výdaje, načerpaný objem, průměrná cena za litr, počet tankování.<br>• Průběžné testování a opravy chyb. |
| **Do června**<br>*(Globální statistiky, zálohy a mapa)* | • Globální statistiky napříč všemi vozidly.<br>• Vizuální grafy vývoje výdajů a spotřeby paliva.<br>• Zpracování údajů z tachometru a statistiky kilometrů.<br>• Zálohování a obnova databáze na Google Drive přes Google účet.<br>• Vyskakovací mapa Google Maps s GPS polohou a ručním upřesněním stanice.<br>• Testování v reálném provozu, optimalizace a finální dokumentace. |

---

## 🛠️ Návod k sestavení a spuštění

Instrukce k instalaci prostředí, sestavení a spuštění projektu naleznete v samostatném souboru [BUILD.md](BUILD.md).

# Fueling_Management_Tool
Jednoduchá mobilní aplikace pro systém Android určená k evidenci a správě výdajů za tankování pohonných hmot. Aplikace odstraňuje nutnost zdlouhavého ručního přepisování údajů díky automatickému rozpoznání načerpaného množství a ceny přímo z fotografie displeje stojanu.

---

## 🎯 Cíl projektu

* **Zjednodušit evidenci tankování:** Uživatel vyfotí displej stojanu a aplikace automaticky detekuje natankovaný objem (l) a celkovou částku (Kč) s možností rychlé ruční kontroly či opravy.
* **Geolokační metadata:** K záznamu se automaticky přiřadí GPS souřadnice čerpací stanice.
* **Offline-first a přenositelnost:** Data se ukládají lokálně v telefonu i na cloudovém úložišti. Přihlášením k účtu je zajištěna synchronizace a dostupnost dat na jiném zařízení (samotné snímky se neukládají, pouze vytěžená data).

---

## 🏛️ Architektura systému

Projekt využívá **offline-first** přístup rozdělený do dvou hlavních vrstev:

1. **Mobilní klient (Android):**
   * Lokální databáze v zařízení (zajišťuje plnou funkčnost bez internetu).
   * Lokální zpracování obrazu (On-Device OCR) přímo v telefonu pro rychlou extrakci hodnot bez odesílání fotografií po síti.
   * Modul pro získání přesné polohy (GPS).
2. **Cloudové zázemí (Backend):**
   * Správa uživatelských účtů (autentizace).
   * Vzdálená databáze a synchronizační mechanismus pro zálohování a přenositelnost dat.

---

### Co aplikace řeší (In Scope):
* Nativní aplikace pro systém Android.
* Správa a evidence vozidel uživatele.
* Pořizování fotografií stojanů s OCR detekcí litrů a celkové ceny.
* Možnost manuální úpravy a potvrzení detekovaných hodnot.
* Ukládání metadat (datum, čas, GPS poloha).
* Historie tankování a základní přehled nákladů a výdajů.
* Lokální úložiště + cloudová synchronizace po přihlášení.

### Co aplikace neřeší (Out of Scope):
* Podporu pro iOS nebo webové rozhraní.
* Firemní flotilovou správu (přiřazování řidičů, správa firemních aut).
* Vyhledávání a porovnávání nejlevnějších čerpacích stanic na trhu.

---

## 🗺️ Roadmapa vývoje

| Fáze | Zaměření |
| :--- | :--- |
| **do Prosince** | Vytvořit funkční základ mobilní aplikace umožňující pořizování a evidenci záznamů o tankování. |
| **do Března** | Rozšířit funkční základ aplikace o správu vozidel a základní vyhodnocování údajů o tankování. |
| **do Června** | Dokončit pokročilé funkce aplikace, implementovat cloudové zálohování a připravit aplikaci k závěrečnému testování. |

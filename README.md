# MinimalLauncher 🚀

<p align="center">
  <strong>Un launcher minimalista per Android, ispirato a Olauncher.</strong><br>
  Leggero, veloce, senza pubblicità, open source.
</p>

---

## ✨ Caratteristiche

- **Orologio e data** — Mostra l'ora e la data nella home screen
- **Griglia app** — Tutte le app visibili subito, in ordine alfabetico
- **App drawer** — Scorri a sinistra per accedere a tutte le app
- **Ricerca rapida** — Cerca app per nome
- **Gesti personalizzabili** — Swipe e doppio tocco configurabili
- **App nascoste** — Nascondi le app che non usi
- **Temi** — Sistema, chiaro, scuro, sfondo trasparente
- **Immersive mode** — Nascondi la barra di stato per un'esperienza full-screen
- **Zero bloat** — Nessun servizio in background, nessuna tracciamento

## 📱 Schermate

| Home Screen | App Drawer | Impostazioni |
|---|---|---|
| Orologio + griglia app | Lista alfabetica | Aspetto, gesti, app nascoste |

## 🔧 Requisiti

- **Android SDK 26+** (Android 8.0 Oreo o superiore)
- **Kotlin 1.9.22**
- **Gradle 8.5**
- **JDK 17**

## 🚀 Come compilare

### Opzione 1: GitHub Actions (automatico)
1. Fai fork di questo repository
2. Push del codice sul branch `main`
3. Vai su **Actions** → seleziona il workflow **Build APK**
4. Scarica l'APK dagli **Artifacts**

### Opzione 2: Locale con Android Studio
1. Clona il repository:
   ```bash
   git clone https://github.com/TUO_UTENTE/MinimalLauncher.git
   cd MinimalLauncher
   ```
2. Apri il progetto in **Android Studio**
3. Seleziona **Build → Build Bundle(s) / APK(s) → Build APK(s)**
4. Trova l'APK in `app/build/outputs/apk/debug/`

### Opzione 3: Linea di comando
```bash
chmod +x gradlew
./gradlew assembleDebug
# APK in app/build/outputs/apk/debug/
```

## 🏗️ Struttura del progetto

```
MinimalLauncher/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml          # Intent HOME per launcher
│       ├── java/com/minillauncher/
│       │   ├── ui/
│       │   │   ├── HomeActivity.kt      # Schermata principale
│       │   │   ├── AppDrawerActivity.kt # Drawer tutte le app
│       │   │   ├── SettingsActivity.kt  # Impostazioni
│       │   │   ├── HiddenAppsActivity.kt # Gestione app nascoste
│       │   │   ├── HomeAppAdapter.kt    # Adapter griglia home
│       │   │   ├── DrawerAppAdapter.kt  # Adapter lista drawer
│       │   │   └── SearchAppAdapter.kt  # Adapter risultati ricerca
│       │   └── utils/
│       │       ├── AppInfo.kt           # Modello dati app
│       │       ├── AppUtils.kt          # Utility caricamento app
│       │       ├── ClockUtils.kt        # Formattazione orario
│       │       └── PreferencesManager.kt # Gestione preferenze
│       └── res/
│           ├── layout/                  # Layout XML
│           ├── values/                  # Stringhe, colori, temi
│           ├── drawable/                # Shape e background
│           ├── anim/                    # Animazioni transizione
│           └── menu/                    # Menu contestuale app
├── .github/workflows/
│   └── build.yml                       # CI/CD compilazione APK
├── build.gradle                         # Configurazione Gradle (root)
├── app/build.gradle                     # Configurazione Gradle (app)
└── gradle/wrapper/                      # Gradle wrapper
```

## ⚙️ Impostazioni Launcher

### Impostare come Launcher Predefinito
1. Installa l'APK sul tuo dispositivo
2. Premi il pulsante **Home**
3. Android ti chiederà di scegliere un launcher
4. Seleziona **MinimalLauncher** e premi "Sempre"

### Configurare i Gesti
- **Swipe sinistra** → Apri app drawer (default)
- **Swipe destra** → Mostra notifiche (default)
- **Doppio tocco** → Blocca schermo (default)
- Tutti i gesti sono personalizzabili dalle impostazioni

### Long-press su un'app
- **Info app** → Apre le impostazioni dell'app
- **Nascondi app** → Rimuove l'app dal launcher
- **Disinstalla** → Rimuove l'app dal dispositivo

## 🗺️ Roadmap (Espansioni Future)

### v1.1 — Meteo Widget
- [ ] Widget meteo nella home screen
- [ ] Dati da API OpenWeatherMap
- [ ] Aggiornamento automatico

### v1.2 — Widget Estesi
- [ ] Calendario widget
- [ ] Note rapide / widget testo
- [ ] Supporto widget Android nativi (AppWidgetHost)

### v1.3 — Personalizzazione Avanzata
- [ ] Griglia personalizzabile (numero colonne)
- [ ] Dimensione icone regolabile con slider
- [ ] Font personalizzati
- [ ] Colori personalizzabili

### v1.4 — Funzionalità Smart
- [ ] App suggerite (basate sull'uso)
- [ ] Ricerca web integrata
- [ ] Quick actions (WiFi, Bluetooth, Torcia)

### v2.0 — Launcher Intelligente
- [ ] Categorizzazione automatica app
- [ ] Tag personalizzati per app
- [ ] Multi-pagina con swipe orizzontale
- [ ] Temi dinamici (basati sul wallpaper)

## 🤝 Contribuire

1. Fai fork del progetto
2. Crea un branch feature (`git checkout -b feature/nome-feature`)
3. Fai commit delle modifiche (`git commit -m 'Aggiunta feature X'`)
4. Push al branch (`git push origin feature/nome-feature`)
5. Apri una Pull Request

## 📄 Licenza

Questo progetto è open source e ispirato a [Olauncher](https://github.com/nicholaschum/olauncher).
Puoi usarlo, modificarlo e distribuirlo liberamente.

---

<p align="center">
  Fatto con ❤️ per un Android più semplice e pulito
</p>

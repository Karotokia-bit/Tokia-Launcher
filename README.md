# Tokia

Ein schlanker, kostenloser Fabric-Client-Mod für Minecraft **26.3** (Java Edition).

## Funktionen
- **Custom Crosshair:** Stil (Kreuz, Punkt, Kreis, Quadrat), Farbe, Größe, Dicke, Abstand, Umrandung
- **Info-HUD:** FPS, Ping, Koordinaten, CPS, Uhrzeit (Ecke frei wählbar)
- **Keystrokes:** WASD, Leertaste, linke/rechte Maustaste mit CPS
- **Rüstungs-Anzeige:** Haltbarkeit in Prozent
- **Effekt-Anzeige:** aktive Tränke mit Restdauer
- **Einstellungsmenü:** Standard-Taste `Rechte Shift-Taste` (in den Steuerungs-Einstellungen änderbar)

Einstellungen werden in `config/tokia.json` gespeichert.

## Entwickeln
Voraussetzung: **Java 25** (JDK) und Internet beim ersten Build.

```
./gradlew runClient   # startet Minecraft mit Tokia zum Testen
./gradlew build       # erzeugt die Mod-Datei in build/libs/
```

Windows: `gradlew.bat` statt `./gradlew`. Empfohlen: IntelliJ IDEA (Projekt als Gradle-Projekt öffnen).

## Installieren
1. Fabric Loader für Minecraft 26.3 installieren
2. Fabric API (passend zu 26.3) in den `mods`-Ordner legen
3. `build/libs/tokia-0.1.0.jar` (nicht die `-sources`-Datei) ebenfalls in den `mods`-Ordner legen

## Hinweise
- Die Versionen in `gradle.properties` stammen aus dem offiziellen Fabric-Template. Aktuelle Werte stehen auf https://fabricmc.net/develop
- Das Icon in `src/main/resources/assets/tokia/icon.png` ist noch das Platzhalter-Icon von Fabric.
- CPS wird pro Tick (20x pro Sekunde) gemessen, die Anzeige ist daher auf max. 20 begrenzt.

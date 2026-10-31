# Begleiter-Mod (Fabric, Minecraft 26.3)

Ein Begleiter, der dir folgt, schläft, Angst hat und bei Monstern auf dich klettert.

## Bauen
Voraussetzung: Java 25, Gradle.
```
gradle build
```
Die Mod liegt danach in `build/libs/begleiter-0.1.0.jar` (nicht die `-sources`-Datei). Zusammen mit Fabric API 0.160.5+26.3 in den `mods`-Ordner.

## Eigenes Aussehen aus einem Bild
```
pip install pillow
python3 tools/bild_zu_skin.py foto.png begleiter.png
```
`begleiter.png` nach `src/main/resources/assets/begleiter/textures/entity/begleiter.png` kopieren, neu bauen. Das Foto sollte die Person von vorn, ganz im Bild, vor ruhigem Hintergrund zeigen. Ergebnis ist eine Pixel-Annäherung (Haut-, Haar-, Kleidungsfarben), kein echtes 3D-Scannen.

## Skin auswählen (ohne neu zu bauen)
1. Skin mit `tools/bild_zu_skin.py` erzeugen (oder einen beliebigen 64x64-Minecraft-Skin nehmen).
2. PNG in den Ordner `.minecraft/config/begleiter/skins/` legen, z. B. `lisa.png`. (Ordner beim ersten Mal selbst anlegen.)
3. Im Spiel: `/begleiter skin lisa`. Mit `/begleiter skin default` gibt es wieder den Standard-Skin.
Die Datei muss auf dem Rechner jedes Mitspielers liegen, der den Skin sehen soll.

## Befehle (nur du siehst die Nachrichten)
- `/begleiter skin <name>` – Skin aus config/begleiter/skins/<name>.png wählen
- `/begleiter spawn` – Begleiter erscheint
- `/begleiter weg` – Begleiter verschwindet
- `/b <text>` oder `/begleiter sag <text>` – mit ihm schreiben (Antworten nach Stichwort, keine KI)

## Verhalten
- Folgt dir, teleportiert sich bei über 24 Blöcken Abstand zu dir
- Schläft, solange du im Bett schläfst
- Monster in 14 Blöcken: er sagt, dass er Angst hat; unter 7 Blöcken klettert er auf dich und steigt 5 Sekunden nach Entwarnung wieder ab

## Bekannte Grenzen
- Nicht kompiliert/getestet: Die Mojang-/Fabric-Namen für 26.3 stammen aus meinem Wissen, nicht aus einem Build. Kleine Anpassungen bei Importen oder Methodensignaturen sind beim ersten Build wahrscheinlich.
- Begleiter wird nicht gespeichert; nach Neustart/Relog `/begleiter spawn`.
- Das Schlafen-Aussehen ohne Bett kann im Renderer komisch aussehen.

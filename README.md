# 3D STL Viewer (Java)

[![Java 17+](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-0078D6?style=for-the-badge&logo=windows&logoColor=white)](https://github.com/roberthoferzuerich-alt/java-stl-viewer)
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-Zero%20(Pure%20Java)-success?style=for-the-badge)](https://github.com/roberthoferzuerich-alt/java-stl-viewer)
[![Performance](https://img.shields.io/badge/Renderer-Multi--Thread%2060%20FPS-blueviolet?style=for-the-badge)](https://github.com/roberthoferzuerich-alt/java-stl-viewer)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

Ein moderner, leistungsstarker und vollständig eigenständiger **3D STL-Viewer**, entwickelt in reinem Java (Java 17+). Die Anwendung läuft ohne externe native Grafikbibliotheken (OpenGL, DirectX, Vulkan oder DLLs) und nutzt einen hocheffizienten, **multithreaded Software-Rasterizer mit Z-Buffering**, der auf allen Kernen moderner CPUs flüssige 60 FPS liefert.

---

## 📑 Inhaltsverzeichnis

- [✨ Highlights](#-highlights)
- [💻 Systemvoraussetzungen](#-systemvoraussetzungen)
- [📦 Installation & Start](#-installation--start)
  - [Option 1: Vorkompilierte JAR direkt starten (Empfohlen)](#option-1-vorkompilierte-jar-direkt-starten-empfohlen)
  - [Option 2: Windows-Startskripte nutzen](#option-2-windows-startskripte-nutzen)
  - [Option 3: Quellcode klonen und selbst kompilieren](#option-3-quellcode-klonen-und-selbst-kompilieren)
  - [Option 4: Windows-Dateiverknüpfung einrichten (.stl mit Viewer öffnen)](#option-4-windows-dateiverknüpfung-einrichten-stl-mit-viewer-öffnen)
- [📖 Benutzerhandbuch](#-benutzerhandbuch)
  - [Die Benutzeroberfläche (UI)](#die-benutzeroberfläche-ui)
  - [3D-Kamerasteuerung & Navigation](#3d-kamerasteuerung--navigation)
  - [Tastaturkürzel (Spickzettel)](#tastaturkürzel-spickzettel)
  - [Laden & Importieren von Modellen](#laden--importieren-von-modellen)
  - [Render-Modi & Darstellung](#render-modi--darstellung)
  - [Materialien & Farbschemata](#materialien--farbschemata)
  - [Modell-Statistiken & Messwerte](#modell-statistiken--messwerte)
  - [3D-Druck Vorbereitung (Druckbett-Ausrichtung)](#3d-druck-vorbereitung-druckbett-ausrichtung)
  - [Exportieren & Screenshots](#exportieren--screenshots)
- [🧩 Enthaltene Beispiel-Modelle](#-enthaltene-beispiel-modelle)
- [🏗 Architektur & Projektstruktur](#-architektur--projektstruktur)
- [❓ Problembehebung (FAQ & Troubleshooting)](#-problembehebung-faq--troubleshooting)
- [👨‍💻 Entwickler & Danksagung](#-entwickler--danksagung)
- [📄 Lizenz](#-lizenz)

---

## ✨ Highlights

- **Keine nativen Abhängigkeiten:** Reines Standard-Java (AWT/Swing), läuft überall sofort ohne Installation von Grafikkartentreibern, C++-Runtimes oder DLLs.
- **Multi-Thread Software-3D-Rasterizer:** Automatische Parallelisierung über alle verfügbaren CPU-Kerne für blitzschnelles Dreiecks-Rastering mit Tiefenpuffer (Z-Buffer).
- **Format-Unterstützung:** Erkennt und lädt sowohl **binäre STL** als auch **ASCII STL**-Dateien zuverlässig und fehlertolerant mit optimiertem Byte-Buffering.
- **Drag & Drop:** STL-Dateien können direkt per Maus aus dem Dateimanager in das 3D-Fenster gezogen werden.
- **Echtzeit-Analyse:** Präzise Berechnung von Dreiecksanzahl, Vertexanzahl, Modell-Abmessungen ($X \times Y \times Z$ in mm), Modellvolumen ($\text{cm}^3$) und Oberfläche ($\text{cm}^2$).
- **CAD & 3D-Druck Werkzeuge:** Automatische Druckbett-Ausrichtung ($Z=0$), Bounding-Box, 3D-Bodenraster, 3D-Achsen-Kompass (Gizmo) und Z-Gradienten-Höhenprofil.
- **Präsentationsmodus:** Sanfte Turntable-Auto-Rotation per Knopfdruck oder Leertaste.
- **Modernes Dark-UI:** Aufgeräumte Benutzeroberfläche mit dunklem Farbschema, klaren Schriftarten und Vektorsymbolen.

---

## 💻 Systemvoraussetzungen

| Komponente | Anforderung |
| :--- | :--- |
| **Betriebssystem** | Windows 10/11, Linux (Ubuntu, Debian, Fedora, Arch, etc.), macOS (Intel & Apple Silicon) |
| **Java** | Java Runtime Environment (JRE) oder Java Development Kit (JDK) **17 oder neuer** |
| **Arbeitsspeicher** | Mindestens 512 MB RAM (2 GB+ empfohlen für große STL-Netze mit > 500'000 Dreiecken) |
| **Grafikkarte** | Keine dedizierte GPU erforderlich (reiner CPU-Software-Renderer) |

> [!TIP]
> Um zu prüfen, ob Java auf Ihrem Rechner installiert ist, öffnen Sie ein Terminal (PowerShell / Eingabeaufforderung / Bash) und tippen Sie:
> ```bash
> java -version
> ```
> Falls noch kein Java installiert ist, empfehlen wir das kostenlose [Eclipse Temurin OpenJDK 17+](https://adoptium.net/).

---

## 📦 Installation & Start

### Option 1: Vorkompilierte JAR direkt starten (Empfohlen)

Die ausführbare Datei [`STLViewer.jar`](file:///c:/Dev/Java-Projekte/STLViewer.jar) ist im Projekt bereits fertig kompiliert und einsatzbereit.

1. **Per Doppelklick:** Klicken Sie im Explorer einfach doppelt auf [`STLViewer.jar`](file:///c:/Dev/Java-Projekte/STLViewer.jar).
2. **Per Terminal / Konsole:**
   ```bash
   java -jar STLViewer.jar
   ```
3. **Direkt mit einem Modell als Parameter starten:**
   ```bash
   java -jar STLViewer.jar beispiel_zahnrad.stl
   ```

---

### Option 2: Windows-Startskripte nutzen

Für Windows-Benutzer stehen komfortable Batch-Skripte bereit:

- **[`run.bat`](file:///c:/Dev/Java-Projekte/run.bat)**: Startet den Viewer im Hintergrund mit `javaw` (ohne störendes schwarzes Konsolenfenster).
  ```cmd
  run.bat
  ```
  Oder übergebe eine STL-Datei per Drag-and-Drop auf [`run.bat`](file:///c:/Dev/Java-Projekte/run.bat).

- **[`run.sh`](file:///c:/Dev/Java-Projekte/run.sh)** (Linux/macOS):
  ```bash
  chmod +x run.sh
  ./run.sh beispiel_wuerfel_20mm.stl
  ```

---

### Option 3: Quellcode klonen und selbst kompilieren

Möchten Sie den Quellcode modifizieren oder auf einem anderen System selbst bauen?

#### 1. Repository klonen
```bash
git clone https://github.com/roberthoferzuerich-alt/java-stl-viewer.git
cd java-stl-viewer
```

#### 2. Unter Windows kompilieren
Führen Sie einfach das Skript [`build.bat`](file:///c:/Dev/Java-Projekte/build.bat) aus:
```cmd
build.bat
```

#### 3. Unter Linux / macOS kompilieren
Führen Sie das Shell-Skript [`build.sh`](file:///c:/Dev/Java-Projekte/build.sh) aus:
```bash
chmod +x build.sh
./build.sh
```

#### 4. Manuelle Kompilierung per Terminal
Falls Sie kein Skript nutzen möchten:
```bash
# 1. Ausgabeverzeichnis erstellen
mkdir stl-viewer/bin

# 2. Alle Java-Quellcodedateien kompilieren
javac -encoding UTF-8 -d stl-viewer/bin $(find stl-viewer/src/main/java -name "*.java")

# 3. Ausführbares Manifest & JAR erstellen
jar --create --file STLViewer.jar --main-class com.stlviewer.Main -C stl-viewer/bin .
```

---

### Option 4: Windows-Dateiverknüpfung einrichten (.stl mit Viewer öffnen)

Sie können Windows so einstellen, dass jede `.stl`-Datei im Windows-Explorer per einfachem Doppelklick direkt in diesem Viewer geöffnet wird:

1. Rechtsklick auf eine beliebige `.stl`-Datei (z. B. [`beispiel_zahnrad.stl`](file:///c:/Dev/Java-Projekte/beispiel_zahnrad.stl)).
2. Wählen Sie **Öffnen mit** ➔ **Andere App auswählen**.
3. Scrollen Sie nach unten und klicken Sie auf **Weitere Apps** ➔ **Andere App auf diesem PC suchen**.
4. Wählen Sie entweder die Datei [`run.bat`](file:///c:/Dev/Java-Projekte/run.bat) oder navigieren Sie zu Ihrer `javaw.exe` mit dem Argument `-jar C:\Pfad\zu\STLViewer.jar`.
5. Setzen Sie den Haken bei **"Immer diese App zum Öffnen von .stl-Dateien verwenden"**.

---

## 📖 Benutzerhandbuch

### Die Benutzeroberfläche (UI)

Die Benutzeroberfläche ist ergonomisch in 5 Hauptbereiche gegliedert:

```
+-----------------------------------------------------------------------------------+
| Menüleiste: Datei | Ansicht | Beispiele | Hilfe | Über                           |
+-----------------------------------------------------------------------------------+
| Toolbar: [📂 Öffnen] [Beispiele ▼] | [Modus] [Druckbett] [🔄 Auto] [Zentrieren] | [📷] [❓] |
+-------------------------------------------------------------+---------------------+
|                                                             | SEITENLEISTE:       |
|                                                             | 📊 MODELL-STATS     |
|                                                             | - Dreiecke / Ecken  |
|                     3D VIEWPORT                             | - Abmessungen       |
|                                                             | - Volumen & Fläche  |
|            - Interaktive 3D-Szene                           | 🎨 RENDER-OPTIONEN  |
|            - 3D-Bodenraster                                 | - Shading Modus     |
|            - 3D-Koordinatenkompass (Gizmo)                  | - Material / Farbe  |
|            - Bounding-Box                                   | - Beleuchtung       |
|                                                             | 🧭 KAMERA           |
|                                                             | - Iso / Oben / ...  |
+-------------------------------------------------------------+---------------------+
| Statusleiste: Bereit / Ladezustand / FPS-Anzeige            | Java 17 Engine      |
+-----------------------------------------------------------------------------------+
```

1. **3D Viewport (Mitte):** Interaktiver Bereich zur Darstellung des 3D-Objekts mit Z-Puffer, Beleuchtung, Schatten und Gittern.
2. **Toolbar (Oben):** Schnellzugriff auf die wichtigsten Aktionen (Öffnen, Vorlagen, Druckbett-Ausrichtung, Kamera-Reset, Auto-Drehung, Screenshot).
3. **Seitenleiste (Rechts):**
   - **Modell-Statistiken:** Exakte Live-Geometriedaten des geladenen Netzes.
   - **Render-Optionen:** Auswahl des Shading-Verfahrens, Farbmaterialien, Bodenraster, Backface-Culling und Headlight.
   - **Kamera-Steuerung:** Schnellumschaltung auf orthogonale oder perspektivische Projektion sowie voreingestellte Blickwinkel.
   - **Tastenkürzel-Übersicht:** Schnelle Gedächtnisstütze im Programm.
4. **Menüleiste:** Umfassende Menüführung mit Shortcuts und Exportfunktionen.
5. **Statusleiste (Unten):** Anzeige von Ladefortschritt, Benachrichtigungen und Engine-Status.

---

### 3D-Kamerasteuerung & Navigation

Die Navigation orientiert sich an modernen CAD- und 3D-Programmen:

| Eingabe | Funktion | Beschreibung |
| :--- | :--- | :--- |
| **Linke Maustaste + Ziehen** | **3D-Orbit (Drehen)** | Rotiert das Modell frei um das Raumzentrum (Azimut und Elevation). |
| **Rechte Maustaste + Ziehen** | **Pan (Verschieben)** | Verschiebt das Modell parallel zur Bildebene (oben/unten/links/rechts). |
| **Mittlere Maustaste + Ziehen** | **Pan (Verschieben)** | Alternative Verschiebung für 3-Tasten-Mäuse. |
| **Mausrad vor / zurück** | **Zoom (Vergrößern/Verkleinern)** | Stufenloses Heranzoomen an feine Details oder Herauszoomen für Übersicht. |
| **Doppelklick (Links)** | **Ansicht zentrieren (Fit)** | Zentriert das Modell sofort mittig im Bild und skaliert es optimal ins Fenster. |

---

### Tastaturkürzel (Spickzettel)

Drücken Sie eine Taste direkt im Anwendungsfenster:

| Taste | Aktion | Beschreibung |
| :---: | :--- | :--- |
| <kbd>R</kbd> | **Ansicht zentrieren** | Setzt Kamera-Position, Rotation und Zoom auf das Modell zurück. |
| <kbd>D</kbd> | **Druckbett-Ausrichtung** | Platziert die tiefste Stelle des Modells auf $Z = 0$ (wie im Slicer). |
| <kbd>SPACE</kbd> | **Auto-Drehung** | Schaltet die 360°-Präsentationsdrehung (Turntable) an/aus. |
| <kbd>W</kbd> | **Render-Modus wechseln** | Schaltet zyklisch zwischen Schattiert, Gouraud, Wireframe, Z-Gradient etc. durch. |
| <kbd>G</kbd> | **Bodenraster** | Schaltet das millimetergenaue 3D-Grundraster ein oder aus. |
| <kbd>A</kbd> | **3D-Achsenkompass** | Blendet den Orientierungs-Gizmo in der Fensterecke ein/aus ($X$=Rot, $Y$=Grün, $Z$=Blau). |
| <kbd>B</kbd> | **Bounding-Box** | Blendet den quaderförmigen Hüllkörper (Grenzabmessungen) ein/aus. |
| <kbd>1</kbd> | **Isometrisch** | Voreingestellte schräge Standard-Perspektive ($45^\circ$). |
| <kbd>2</kbd> | **Ansicht von Oben** | Blick von oben (Top-Down, Draufsicht entlang der Z-Achse). |
| <kbd>3</kbd> | **Ansicht von Vorne** | Frontalansicht (Front-View). |
| <kbd>4</kbd> | **Ansicht von Rechts** | Seitenansicht von rechts (Right-View). |
| <kbd>Ctrl</kbd> + <kbd>O</kbd> | **Datei öffnen** | Öffnet den Dateidialog zur Auswahl einer `.stl`-Datei. |
| <kbd>Ctrl</kbd> + <kbd>S</kbd> | **Screenshot speichern** | Speichert ein hochauflösendes Bild des Viewports als PNG oder JPEG. |
| <kbd>F1</kbd> | **Hilfe-Dialog** | Ruft die integrierte Bedienungsanleitung auf. |

---

### Laden & Importieren von Modellen

Sie können STL-Dateien auf drei bequeme Weisen öffnen:
1. **Drag & Drop:** Ziehen Sie eine oder mehrere `.stl`-Dateien aus dem Dateimanager direkt in das schwarze 3D-Fenster.
2. **Toolbar-Button:** Klicken Sie auf **"Öffnen"** in der oberen Leiste oder nutzen Sie das Menü `Datei` ➔ `STL öffnen...` (<kbd>Strg</kbd> + <kbd>O</kbd>).
3. **Beispiel-Katalog:** Wählen Sie in der Toolbar oder im Menü `Beispiele` eines der integrierten Modelle (Zahnrad, Kalibrierwürfel, Torus, Sechskantmutter).

> [!NOTE]
> Der integrierte Parser unterstützt sowohl **ASCII** als auch **Binäre** STL-Dateien und erkennt das Format vollautomatisch. Große Dateien werden im Hintergrund asynchron geladen, sodass die Oberfläche zu keinem Zeitpunkt einfriert.

---

### Render-Modi & Darstellung

Über das Dropdown-Menü in der Seitenleiste oder die Schnelltaste <kbd>W</kbd> stehen 6 Renderverfahren zur Verfügung:

1. **Flächen-Schattiert (Flat Shading):**
   Klassische facettierte Darstellung. Jedes Dreieck wird einheitlich beleuchtet. Ideal, um die Rohgeometrie und Facetten des Netzes zu beurteilen.
2. **Geglättet (Gouraud Shading):**
   Weiche, runde Übergänge durch automatische Berechnung und Interpolation von Eckpunkt-Normalen (Vertex Normals). Perfekt für organische Formen und Kurven.
3. **Drahtgitter (Wireframe):**
   Rendert nur die 3D-Kantenlinien aller Dreiecke. Bietet tiefen Einblick in die Tessellierungsdichte und Topologie.
4. **Schattiert + Gitter (Shaded + Wireframe):**
   Kombiniert beleuchtete Oberflächen mit einem dunkleren Drahtgitter-Overlay. Beliebt in professionellen CAD-Werkzeugen.
5. **Höhenprofil (Z-Gradient):**
   Färbt das Modell über ein Farbspektrum basierend auf seiner Höhe ($Z$-Koordinate) ein. Besonders aufschlussreich für die Analyse von Schichten im 3D-Druck.
6. **Normalen-Farben (RGB Normals):**
   Mappt die Raumrichtung der Flächennormalen ($X, Y, Z$) direkt in den RGB-Farbraum. Zeigt geometrische Orientierungen und eventuell invertierte Normalen auf einen Blick.

---

### Materialien & Farbschemata

Wählen Sie in der Seitenleiste aus 6 abgestimmten Farbpaletten:
- 🪙 **Titanium Silber:** Neutraler industrieller CAD-Look mit hohem Kontrast.
- 💎 **Cyan High-Tech:** Brillantes Türkisblau für futuristische 3D-Präsentationen.
- 🏆 **Warmes Gold & Bernstein:** Warme Messing- und Goldtöne mit Glanzeffekt.
- 🌲 **Smaragdgrün:** Ausgewogene Grüntöne für hohe Plastizität.
- 🔴 **Rubinrot:** Kraftvolles Karminrot mit dunkler Tiefenschattierung.
- ⚪ **Weißes Harz / Keramik:** Matter Kunststoff-/Clay-Look, ideal für 3D-Druck-Harze.

---

### Modell-Statistiken & Messwerte

Sobald ein Modell geladen wird, führt der Viewer eine mathematische Geometrieanalyse durch und zeigt in der Seitenleiste folgende Werte an:

- **Dreiecke (Facets):** Gesamtzahl aller Polygone im Netz.
- **Eckpunkte (Vertices):** Gesamtzahl der generierten Vertices.
- **Abmessungen ($X \times Y \times Z$ in mm):** Exakte Außenmaße der Bounding-Box in Millimetern.
- **Volumen (in $\text{cm}^3$):** Das echte Festkörpervolumen, exakt berechnet über vorzeichenbehaftete Tetraeder-Divergenz-Integration:
  $$V = \frac{1}{6} \sum_{i=1}^{N} \mathbf{v}_1^{(i)} \cdot \left(\mathbf{v}_2^{(i)} \times \mathbf{v}_3^{(i)}\right)$$
- **Oberfläche (in $\text{cm}^2$):** Exakte Summe aller Dreiecksflächen:
  $$A = \frac{1}{2} \sum_{i=1}^{N} \|\left(\mathbf{v}_2^{(i)} - \mathbf{v}_1^{(i)}\right) \times \left(\mathbf{v}_3^{(i)} - \mathbf{v}_1^{(i)}\right)\|$$
- **Format:** Erkennt automatisch binäre STL (`Binary STL`) oder Text-STL (`ASCII STL`).

---

### 3D-Druck Vorbereitung (Druckbett-Ausrichtung)

Viele STL-Dateien sind im CAD-Ursprung zentriert, sodass die untere Hälfte unterhalb der Nulllinie liegt.
- Klicken Sie in der Toolbar auf **"Druckbett"** oder drücken Sie die Taste <kbd>D</kbd>.
- Das Modell wird automatisch so nach oben verschoben, dass seine tiefste Stelle exakt auf der Grundebene ($Z = 0$) aufliegt.
- Das 3D-Bodenraster fungiert dabei als optisches Druckbett (Build Plate).

---

### Exportieren & Screenshots

- **Binär-STL exportieren:** Über das Menü `Datei` ➔ `Als Binär-STL speichern...` können Sie jedes Modell (auch die intern generierten Beispielmodelle) als standardkonforme Binär-STL abspeichern.
- **Hochauflösende Screenshots:** Über das Menü `Datei` ➔ `Screenshot aufnehmen...` oder den Toolbar-Button mit Kamerasymbol (<kbd>Strg</kbd> + <kbd>S</kbd>) speichern Sie die aktuelle Ansicht verlustfrei als **PNG** oder **JPEG** ab.

---

## 🧩 Enthaltene Beispiel-Modelle

Im Projektverzeichnis und direkt in der Anwendung sind mehrere Testmodelle integriert:

| Modell | Dateiname / Menü | Beschreibung |
| :--- | :--- | :--- |
| **Präzisions-Zahnrad** | [`beispiel_zahnrad.stl`](file:///c:/Dev/Java-Projekte/beispiel_zahnrad.stl) | Stirnrad mit 16 Zähnen, Nabe und Wellenbohrung (ideal für Schattierungs- und Drehtests). |
| **Kalibrier-Würfel** | [`beispiel_wuerfel_20mm.stl`](file:///c:/Dev/Java-Projekte/beispiel_wuerfel_20mm.stl) | Standard 20 mm 3D-Druck Testwürfel zur Prüfung der Dimensions- und Volumenberechnung. |
| **Torus (Ring)** | *Menü: Beispiele ➔ Torus* | Mathematisch generierter Kreisring zur Demonstration von weichem Gouraud-Shading. |
| **Sechskantmutter M12** | *Menü: Beispiele ➔ Sechskantmutter M12* | Maschinenschrauben-Mutter mit hexagonalem Außenprofil und Innengewindebohrung. |

---

## 🏗 Architektur & Projektstruktur

Das Projekt ist modular nach dem MVC-Muster und Clean-Code-Prinzipien strukturiert:

```
c:\Dev\Java-Projekte\
├── README.md                  # Ausführliche Projektdokumentation & Anleitung
├── .gitignore                 # Git-Ausschlussregeln (bin, class, IDE-Files)
├── STLViewer.jar              # Fertig kompilierte, ausführbare JAR-Datei
├── run.bat                    # Start-Skript für Windows (ohne Konsolenfenster)
├── build.bat                  # Build-Skript für Windows (kompiliert JAR neu)
├── run.sh                     # Start-Skript für Linux & macOS
├── build.sh                   # Build-Skript für Linux & macOS
├── beispiel_zahnrad.stl       # Test-STL: Zahnrad (25 KB)
├── beispiel_wuerfel_20mm.stl  # Test-STL: 20mm Würfel
└── stl-viewer\
    └── src\main\java\com\stlviewer\
        ├── Main.java          # Programmeinstieg, High-DPI Skalierung & Look & Feel
        ├── math\
        │   ├── Vec3.java      # 3D-Vektormathematik (Skalarprodukt, Kreuzprodukt, Normalisierung)
        │   └── Mat4.java      # $4 \times 4$ Transformationsmatrizen (Projektion, Rotation, Skalierung)
        ├── model\
        │   ├── Triangle.java  # Repräsentation eines STL-Dreiecks mit Normalenvektor
        │   └── Mesh.java      # 3D-Dreiecksnetz mit Bounding-Box, Flächen- und Volumenberechnung
        ├── parser\
        │   ├── StlParser.java     # Robuster Binary- & ASCII-STL Parser
        │   ├── StlExporter.java   # Export von Meshes in standardisiertes Binär-STL
        │   └── SampleModels.java  # Prozedurale Geometriegeneratoren (Zahnrad, Würfel, Torus, Mutter)
        ├── renderer\
        │   ├── Camera.java            # Orbit-Kamera mit Azimut, Elevation, Zoom und Presets
        │   ├── RenderMode.java        # Enum aller 6 Darstellungsmodi
        │   ├── ColorTheme.java        # Definition der Material-Farbpaletten
        │   └── Software3DRenderer.java # Multi-Core Rasterizer mit Z-Puffer, Beleuchtung & Gizmo
        └── ui\
            ├── MainFrame.java         # Hauptfenster, Menüleiste, Tastatur-Dispatcher
            ├── ViewportPanel.java     # Interaktive 3D-Leinwand mit Drag & Drop und Turntable-Timer
            ├── ToolbarPanel.java      # Obere Werkzeugleiste mit Vektor-Buttons
            ├── SidebarPanel.java      # Rechte Seitenleiste für Statistiken und Einstellungen
            ├── ModernButton.java      # Gestylte Schaltflächen mit Hover-Effekten
            ├── ModernComboBox.java    # Gestylte Dropdown-Menüs im Dark-Design
            ├── VectorIcons.java       # Auflösungsunabhängige, gezeichnete Vektor-Icons
            └── AboutDialog.java       # Informationsfenster mit Versions- und Autorenangaben
```

---

## ❓ Problembehebung (FAQ & Troubleshooting)

### 1. `java` wird nicht als Befehl erkannt
- **Ursache:** Die Java-Laufzeitumgebung ist entweder nicht installiert oder der Pfad zu `bin` ist nicht in der Umgebungsvariablen `PATH` eingetragen.
- **Lösung:** Installieren Sie OpenJDK (z. B. von [Adoptium](https://adoptium.net/)) und stellen Sie sicher, dass bei der Installation der Haken **"Add to PATH"** gesetzt ist.

### 2. Doppelklick auf `STLViewer.jar` tut nichts
- **Ursache:** Unter Windows ist `.jar` manchmal mit einem Entpacker (wie WinRAR oder 7-Zip) statt mit `javaw.exe` verknüpft.
- **Lösung:** Nutzen Sie einfach die mitgelieferte [`run.bat`](file:///c:/Dev/Java-Projekte/run.bat), oder installieren Sie das Hilfsprogramm [Jarfix](https://johann.loefflmann.net/de/software/jarfix/index.html), um die Windows-JAR-Verknüpfung mit einem Klick zu reparieren.

### 3. Skalierung auf 4K / High-DPI Bildschirmen wirkt unscharf oder zu klein
- Das Programm aktiviert beim Start automatisch die systemeigene High-DPI Skalierung (`sun.java2d.uiScale.enabled=true`). Sollten Sie manuelle Skalierungsfaktoren wünschen, können Sie die Anwendung wie folgt starten:
  ```bash
  java -Dsun.java2d.uiScale=1.5 -jar STLViewer.jar
  ```

---

## 👨‍💻 Entwickler & Danksagung

- **Entwickler & Konzept:** Robert Hofer, Zürich ([@roberthoferzuerich-alt](https://github.com/roberthoferzuerich-alt))
- **KI-Co-Entwicklung:** Google DeepMind Antigravity

---

## 📄 Lizenz

Dieses Projekt steht unter der freien **MIT-Lizenz**. Sie dürfen den Code nach Belieben für private und kommerzielle Zwecke verwenden, modifizieren und weitergeben.

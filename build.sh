#!/usr/bin/env bash
set -e

echo "=============================================="
echo "      Kompiliere STL Viewer fuer Java 17"
echo "=============================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$SCRIPT_DIR"

mkdir -p stl-viewer/bin

echo "Kompiliere Java-Quellen..."
javac -encoding UTF-8 -d stl-viewer/bin $(find stl-viewer/src/main/java -name "*.java")

echo "Erstelle ausfuehrbare JAR-Datei..."
jar --create --file STLViewer.jar --main-class com.stlviewer.Main -C stl-viewer/bin .

echo "[ERFOLG] STLViewer.jar wurde erfolgreich erstellt!"

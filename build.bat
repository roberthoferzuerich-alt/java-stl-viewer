@echo off
echo ==============================================
echo       Kompiliere STL Viewer fuer Java 17
echo ==============================================

if not exist "stl-viewer\bin" mkdir "stl-viewer\bin"

echo Kompiliere Java-Quellen...
javac -encoding UTF-8 -d stl-viewer\bin stl-viewer\src\main\java\com\stlviewer\math\*.java stl-viewer\src\main\java\com\stlviewer\model\*.java stl-viewer\src\main\java\com\stlviewer\parser\*.java stl-viewer\src\main\java\com\stlviewer\renderer\*.java stl-viewer\src\main\java\com\stlviewer\ui\*.java stl-viewer\src\main\java\com\stlviewer\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [FEHLER] Kompilierung fehlgeschlagen!
    pause
    exit /b %ERRORLEVEL%
)

echo Erstelle ausfuehrbare JAR-Datei...
jar --create --file STLViewer.jar --main-class com.stlviewer.Main -C stl-viewer\bin com\stlviewer\Main.class -C stl-viewer\bin com\stlviewer\math -C stl-viewer\bin com\stlviewer\model -C stl-viewer\bin com\stlviewer\parser -C stl-viewer\bin com\stlviewer\renderer -C stl-viewer\bin com\stlviewer\ui

if %ERRORLEVEL% EQU 0 (
    echo [ERFOLG] STLViewer.jar wurde erfolgreich erstellt!
) else (
    echo [FEHLER] JAR-Erstellung fehlgeschlagen!
)

pause

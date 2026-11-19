@echo off
setlocal
cd /d "%~dp0target"
java -Dfile.encoding=UTF-8 "-Dloader.path=%~dp0target\lib" -jar "%~dp0target\SpringbootLibrary2026.jar"
endlocal
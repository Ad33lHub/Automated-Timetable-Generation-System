@echo off
echo Building Timetable System...
javac -encoding UTF-8 -cp "lib/*" -d bin -sourcepath src src/models/*.java src/database/*.java src/gui/*.java src/App.java
if %errorlevel% neq 0 (
    echo Compilation failed! Make sure Java is installed.
    pause
    exit /b
)
echo Starting Application...
java -cp "bin;lib/*" App
pause

@echo off
echo ======================================================
echo    COMPILING AUTOTIME SYSTEM AND INTEGRATED TESTS     
echo ======================================================
javac -encoding UTF-8 -cp "lib/*" -d bin -sourcepath src src/models/*.java src/database/*.java src/gui/*.java src/App.java
if %errorlevel% neq 0 (
    echo Compilation failed! Please check your JDK setup.
    pause
    exit /b
)

echo.
echo ======================================================
echo          STAGE 1: EXECUTING UNIT TESTS                
echo ======================================================
java -cp "bin;lib/*" database.UnitTestRunner
if %errorlevel% neq 0 (
    echo Unit Tests Failed!
    pause
    exit /b
)

echo.
echo ======================================================
echo        STAGE 2: EXECUTING BLACK BOX UI TESTS          
echo ======================================================
java -cp "bin;lib/*" database.BlackBoxTestRunner
if %errorlevel% neq 0 (
    echo Black Box UI Tests Failed!
    pause
    exit /b
)

echo.
echo ======================================================
echo        STAGE 3: EXECUTING INTEGRATION TESTS           
echo ======================================================
java -cp "bin;lib/*" database.IntegrationTestRunner
if %errorlevel% neq 0 (
    echo Integration Tests Failed!
    pause
    exit /b
)

echo.
echo ======================================================
echo        ALL SYSTEM TESTS EXECUTED SUCCESSFULLY!        
echo ======================================================
pause

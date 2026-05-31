@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   AutoTime — Automated Timetable Generation System
echo   FULL TEST SUITE EXECUTION (3 Categories)
echo   Generated: 2026-05-22
echo ============================================================
echo.

REM ── Step 1: Compile main source ──────────────────────────────
echo [1/4] Compiling main source (src/)...
javac -encoding UTF-8 -cp "lib\*" -d bin ^
  src\models\*.java ^
  src\database\*.java ^
  src\gui\*.java ^
  src\App.java

if %errorlevel% neq 0 (
    echo [ERROR] Main source compilation failed!
    pause & exit /b 1
)
echo [OK] Main source compiled.
echo.

REM ── Step 2: Compile tests from testing/ ──────────────────────
echo [2/4] Compiling test classes (testing/)...
if not exist testing-bin mkdir testing-bin

javac -encoding UTF-8 -cp "lib\*;bin" -d testing-bin ^
  testing\unit\models\AllModelsUnitTest.java ^
  testing\unit\generator\TimetableGeneratorUnitTest.java ^
  testing\integration\dao\TeacherDAOIntegrationTest.java ^
  testing\integration\dao\SubjectAndClassroomDAOIntegrationTest.java ^
  testing\integration\auth\AuthenticationIntegrationTest.java ^
  testing\integration\generator\GeneratorIntegrationTest.java ^
  testing\blackbox\login\AdminLoginBlackBoxTest.java ^
  testing\blackbox\login\StudentLoginBlackBoxTest.java ^
  testing\blackbox\workflow\TimetableWorkflowBlackBoxTest.java

if %errorlevel% neq 0 (
    echo [ERROR] Test compilation failed!
    pause & exit /b 1
)
echo [OK] All test classes compiled.
echo.

REM ── Step 3: Run with JUnit Platform Console Standalone ───────
set CONSOLE_JAR=lib\junit-platform-console-standalone-1.10.1.jar
set CP=%CONSOLE_JAR%;lib\*;bin;testing-bin

echo ============================================================
echo   CATEGORY 1: UNIT TESTS (testing/unit/)
echo   Tests: TC-UNIT-01 to TC-UNIT-32
echo   Scope: Pure in-memory logic — models + generator
echo ============================================================
java -cp "%CP%" org.junit.platform.console.ConsoleLauncher execute ^
  --select-package=unit.models ^
  --select-package=unit.generator ^
  --details=verbose ^
  --disable-banner
echo.

echo ============================================================
echo   CATEGORY 2: INTEGRATION TESTS (testing/integration/)
echo   Tests: TC-INTG-01 to TC-INTG-28
echo   Scope: DAO + DatabaseManager + SQLite
echo ============================================================
java -cp "%CP%" org.junit.platform.console.ConsoleLauncher execute ^
  --select-package=integration.dao ^
  --select-package=integration.auth ^
  --select-package=integration.generator ^
  --details=verbose ^
  --disable-banner
echo.

echo ============================================================
echo   CATEGORY 3: BLACK BOX TESTS (testing/blackbox/)
echo   Tests: TC-BBOX-01 to TC-BBOX-26
echo   Scope: LoginPanel GUI + Timetable Workflow
echo ============================================================
java -cp "%CP%" org.junit.platform.console.ConsoleLauncher execute ^
  --select-package=blackbox.login ^
  --select-package=blackbox.workflow ^
  --details=verbose ^
  --disable-banner
echo.

REM ── Step 4: Legacy custom test runners (original test/) ──────
echo ============================================================
echo   LEGACY RUNNERS (original test/ directory — UnitTestRunner,
echo   BlackBoxTestRunner, IntegrationTestRunner)
echo ============================================================
echo [UNIT]
java -cp "bin;lib\*" database.UnitTestRunner
echo.
echo [BLACKBOX]
java -cp "bin;lib\*" database.BlackBoxTestRunner
echo.
echo [INTEGRATION]
java -cp "bin;lib\*" database.IntegrationTestRunner

echo.
echo ============================================================
echo   ALL TESTS COMPLETE
echo ============================================================
pause

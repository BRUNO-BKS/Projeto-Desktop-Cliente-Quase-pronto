@echo off
setlocal enabledelayedexpansion

:: Configurações
set JAVA_HOME=C:\Program Files\Java\jdk-21
set JAVAFX_HOME=C:\javafx-sdk-21.0.2
set PATH=%JAVA_HOME%\bin;%PATH%

:: Limpa compilações anteriores
if exist target rmdir /s /q target
mkdir target\classes 2>nul

:: Compila o código
echo Compilando o código...
javac -d target/classes ^
     --module-path "%JAVAFX_HOME%\lib" ^
     --add-modules=javafx.controls,javafx.fxml ^
     -cp "%JAVAFX_HOME%\lib\*" ^
     src\main\java\com\buyo\adminfx\*.java ^
     src\main\java\com\buyo\adminfx\ui\*.java ^
     src\main\java\com\buyo\adminfx\ui\controllers\*.java

if errorlevel 1 (
    echo Erro ao compilar o código
    pause
    exit /b 1
)

:: Copia os recursos
xcopy /Y /E /I "src\main\resources\*" "target\classes\" >nul 2>&1

:: Executa o aplicativo
echo Iniciando o aplicativo...
java --module-path "%JAVAFX_HOME%\lib" ^
     --add-modules=javafx.controls,javafx.fxml ^
     -cp "target\classes;%JAVAFX_HOME%\lib\*" ^
     com.buyo.adminfx.Main

if errorlevel 1 (
    echo.
    echo Ocorreu um erro ao executar o aplicativo
    pause
    exit /b 1
)

pause

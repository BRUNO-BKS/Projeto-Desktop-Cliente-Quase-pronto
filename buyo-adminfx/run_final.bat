@echo off
setlocal enabledelayedexpansion

:: Configurações
set JAVA_HOME=C:\Program Files\Java\jdk-21
set JAVAFX_HOME=C:\javafx\javafx-sdk-21.0.2
set PATH=%JAVA_HOME%\bin;%PATH%

:: Limpa o diretório de compilação
echo Limpando diretório de compilação...
if exist target\classes rmdir /s /q target\classes
mkdir target\classes 2>nul

:: Compila o código-fonte
echo Compilando o código-fonte...
javac -d target/classes ^
     -cp "%JAVAFX_HOME%\lib\*" ^
     src/main/java/com/buyo/adminfx/Main.java ^
     src/main/java/com/buyo/adminfx/ui/MainApp.java

if errorlevel 1 (
    echo Erro ao compilar o código-fonte
    pause
    exit /b 1
)

:: Copia os recursos
if not exist target\classes\com\buyo\adminfx\ui mkdir target\classes\com\buyo\adminfx\ui
xcopy /Y /E /I "src\main\resources\com\buyo\adminfx\ui\*.fxml" "target\classes\com\buyo\adminfx\ui\" >nul 2>&1

:: Executa o aplicativo
echo Iniciando o aplicativo...
java ^
     -cp "%JAVAFX_HOME%\lib\*;target/classes" ^
     -Dprism.order=sw ^
     com.buyo.adminfx.Main

if errorlevel 1 (
    echo.
    echo Ocorreu um erro ao executar o aplicativo.
    echo Verifique se o JavaFX está instalado corretamente em %JAVAFX_HOME%
)

pause

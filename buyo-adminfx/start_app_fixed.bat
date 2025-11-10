@echo off
setlocal enabledelayedexpansion

:: Configurações do Java e JavaFX
set JAVA_HOME=C:\\Program Files\\Java\\jdk-21
set JAVAFX_HOME=C:\\javafx\\javafx-sdk-21.0.2
set PATH=%JAVA_HOME%\bin;%PATH%

:: Verifica se o JavaFX está instalado
if not exist "%JAVAFX_HOME%" (
    echo JavaFX não encontrado em %JAVAFX_HOME%
    echo Por favor, baixe o JavaFX 21.0.2 de https://gluonhq.com/products/javafx/
    echo e extraia para C:\\javafx\\
    pause
    exit /b 1
)

:: Executa o aplicativo
echo Iniciando o aplicativo...
java ^
    --module-path "%JAVAFX_HOME%\lib" ^
    --add-modules=javafx.controls,javafx.fxml ^
    -cp "target\classes;%JAVAFX_HOME%\lib\*" ^
    com.buyo.adminfx.Main

if errorlevel 1 (
    echo.
    echo Ocorreu um erro ao executar o aplicativo.
    pause
)

endlocal

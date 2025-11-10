@echo off
setlocal

:: Configuração do Java
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

:: Configuração do módulo JavaFX
set MODULE_PATH=C:\javafx\javafx-sdk-21.0.2\lib

:: Verifica se o diretório do JavaFX existe
if not exist "%MODULE_PATH%" (
    echo JavaFX não encontrado em %MODULE_PATH%
    echo Por favor, baixe o JavaFX 21.0.3 de https://gluonhq.com/products/javafx/
    echo e extraia para a pasta lib do projeto.
    pause
    exit /b 1
)

:: Executa o aplicativo
java --module-path "%MODULE_PATH%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -jar target\buyo-adminfx-1.0.0-shaded.jar

pause

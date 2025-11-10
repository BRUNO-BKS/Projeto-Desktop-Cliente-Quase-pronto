@echo off
setlocal

:: Configuração do Java
set JAVA_HOME="C:\Program Files\Java\jdk-21"
set PATH=%JAVA_HOME%\bin;%PATH%

:: Configuração do JavaFX
set JAVAFX_HOME=C:\javafx\javafx-sdk-21.0.2
set MODULE_PATH=%JAVAFX_HOME%\lib

echo Iniciando o aplicativo sem banco de dados...
java ^
     --module-path %MODULE_PATH% ^
     --add-modules javafx.controls,javafx.fxml ^
     -Dskip.database=true ^
     -Ddebug=true ^
     -jar target\buyo-adminfx-1.0.0-shaded.jar

pause

@echo off
setlocal

:: Configuração do Java
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

:: Configuração do JavaFX
set JAVAFX_LIB=C:\javafx\javafx-sdk-21.0.2\lib

:: Verifica se o diretório do JavaFX existe
if not exist "%JAVAFX_LIB%" (
    echo JavaFX não encontrado em %JAVAFX_LIB%
    echo Por favor, baixe o JavaFX 21.0.2 de https://gluonhq.com/products/javafx/
    echo e extraia para C:\javafx\
    pause
    exit /b 1
)

:: Compila o projeto
echo Compilando o projeto...
if not exist target\classes mkdir target\classes
javac -d target/classes -cp "%JAVAFX_LIB%\*" src/main/java/com/buyo/adminfx/Main.java src/main/java/com/buyo/adminfx/ui/MainApp.java

if errorlevel 1 (
    echo Erro ao compilar o projeto
    pause
    exit /b 1
)

:: Executa o aplicativo
echo Iniciando o aplicativo...
java -cp "%JAVAFX_LIB%\*;target/classes" -Dprism.order=sw com.buyo.adminfx.Main

pause

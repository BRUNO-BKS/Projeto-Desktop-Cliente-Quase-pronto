@echo off
setlocal

:: Configuração do Java
set JAVA_HOME="C:\Program Files\Java\jdk-21"
set PATH=%JAVA_HOME%\bin;%PATH%

:: Configuração do JavaFX
set JAVAFX_HOME="C:\Users\Usuario\javafx-sdk-21.0.3"
set MODULE_PATH=%JAVAFX_HOME%\lib

:: Compilar o projeto
echo Compilando o projeto...
call mvn clean package

if %ERRORLEVEL% NEQ 0 (
    echo Erro ao compilar o projeto.
    pause
    exit /b
)

:: Executar a aplicação
echo Iniciando a aplicação...
java --module-path "%MODULE_PATH%;target" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp "target/buyo-adminfx-1.0.0.jar;target/lib/*" ^
     com.buyo.adminfx.Main

:: Verificar erros
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Se encontrar erros, verifique:
    echo 1. Se o Java 21 está instalado
    echo 2. Se o Maven está instalado e no PATH
    echo 3. Se o JavaFX SDK 21.0.3 está em %JAVAFX_HOME%
    pause
)echo off
echo Iniciando o Buyo AdminFX...

set JAVA_HOME="C:\Program Files\Java\jdk-21"
set PATH=%JAVA_HOME%\bin;%PATH%

set JAVAFX_HOME="C:\Users\Usuario\javafx-sdk-21.0.3"

if not exist %JAVAFX_HOME% (
    echo JavaFX SDK não encontrado em %JAVAFX_HOME%
    echo Por favor, baixe o JavaFX 21.0.3 de https://gluonhq.com/products/javafx/
    echo E extraia para C:\Users\Usuario\javafx-sdk-21.0.3
    pause
    exit /b
)

echo Compilando o projeto...
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo Erro ao compilar o projeto.
    pause
    exit /b
)

echo Iniciando a aplicação...
java --module-path %JAVAFX_HOME%\lib --add-modules javafx.controls,javafx.fxml -cp "target/classes" com.buyo.adminfx.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Se encontrar erros, verifique:
    echo 1. Se o Java 21 está instalado
    echo 2. Se o Maven está instalado e no PATH
    echo 3. Se o JavaFX SDK 21.0.3 está em C:\Users\Usuario\javafx-sdk-21.0.3
    pause
)

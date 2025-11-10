@echo off
setlocal

:: Configuração do Java
set JAVA_HOME="C:\Program Files\Java\jdk-21"
set PATH=%JAVA_HOME%\bin;%PATH%

:: Configuração do JavaFX
set JAVAFX_HOME=C:\javafx\javafx-sdk-21.0.2
set MODULE_PATH=%JAVAFX_HOME%\lib

:: Lista de módulos JavaFX necessários
set JAVAFX_MODULES=javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.swing,javafx.web

echo Configuração do Java:
java -version
echo.

echo Iniciando o aplicativo...
echo Módulos JavaFX: %JAVAFX_MODULES%
echo Caminho do módulo: %MODULE_PATH%

:: Executa o aplicativo
java ^
     --module-path "%MODULE_PATH%" ^
     --add-modules %JAVAFX_MODULES% ^
     -Dskip.database=true ^
     -Ddebug=true ^
     -Dprism.verbose=true ^
     -Djavafx.verbose=true ^
     -Dprism.order=es2,es1,sw ^
     -Dprism.forceGPU=true ^
     -jar "target\buyo-adminfx-1.0.0-shaded.jar"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Ocorreu um erro ao iniciar o aplicativo. Código de erro: %ERRORLEVEL%
    echo Verifique se todas as dependências estão corretamente configuradas.
)

pause

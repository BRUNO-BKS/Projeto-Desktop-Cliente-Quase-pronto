@echo off
setlocal

:: ===============================
::  Configuração do Java
:: ===============================
set JAVA_HOME="C:\Program Files\Java\jdk-21"
set PATH=%JAVA_HOME%\bin;%PATH%

:: ===============================
::  Configuração do JavaFX
:: ===============================
set JAVAFX_HOME=C:\javafx\javafx-sdk-21.0.2
set MODULE_PATH=%JAVAFX_HOME%\lib
set JAVAFX_MODULES=javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.swing,javafx.web

echo ===============================
echo  Java / JavaFX
echo ===============================
java -version
echo.
echo Módulos JavaFX: %JAVAFX_MODULES%
echo Caminho do módulo: %MODULE_PATH%
echo.

:: ===============================
::  Build (Maven package)
:: ===============================
echo Rodando Maven package para gerar o JAR...

:: Tenta usar Maven embutido (tools\maven) se existir, senão usa mvn do sistema
set MVN_CMD=
if exist "tools\maven\apache-maven-3.9.9\bin\mvn.cmd" (
    set "MVN_CMD=tools\maven\apache-maven-3.9.9\bin\mvn.cmd"
) else (
    set "MVN_CMD=mvn"
)

echo Usando Maven: %MVN_CMD%
echo.

%MVN_CMD% -f pom.xml -q -DskipTests package
if errorlevel 1 (
    echo.
    echo ERRO: Maven falhou ao compilar o projeto.
    echo Verifique as mensagens acima.
    goto :EOF
)

echo.
echo Build concluido. Procurando JAR sombreado (*-shaded.jar) em target\...

set "APP_JAR="
for %%F in ("target\*-shaded.jar") do (
    set "APP_JAR=%%F"
)

if "%APP_JAR%"=="" (
    echo ERRO: Nenhum JAR sombreado (*-shaded.jar) foi encontrado em target\
    goto :EOF
)

echo Usando JAR: %APP_JAR%
echo.

:: ===============================
::  Executa o aplicativo
:: ===============================
echo Iniciando o aplicativo...

java ^
     --module-path "%MODULE_PATH%" ^
     --add-modules %JAVAFX_MODULES% ^
     -Dskip.database=true ^
     -Ddebug=true ^
     -Dprism.verbose=true ^
     -Djavafx.verbose=true ^
     -Dprism.order=es2,es1,sw ^
     -Dprism.forceGPU=true ^
     -jar %APP_JAR%

echo.
echo Execucao finalizada. Pressione qualquer tecla para fechar.
pause

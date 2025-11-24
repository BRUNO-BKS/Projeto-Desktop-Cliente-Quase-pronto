@echo off
setlocal enabledelayedexpansion

:: ===============================
::  Script de inicialização do StockRO AdminFX
:: ===============================
echo.
echo ========================================
echo   StockRO AdminFX - Iniciando...
echo ========================================
echo.

:: ===============================
::  Verifica e configura Java
:: ===============================
echo [1/4] Verificando Java...

:: Tenta detectar Java automaticamente
set JAVA_CMD=java
where java >nul 2>&1
if errorlevel 1 (
    echo ERRO: Java nao encontrado no PATH.
    echo Por favor, instale o Java 21 ou configure o JAVA_HOME.
    echo.
    pause
    exit /b 1
)

:: Verifica versão do Java
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
    set JAVA_VERSION=!JAVA_VERSION:"=!
    goto :java_version_found
)
:java_version_found

echo Java encontrado: !JAVA_VERSION!
echo.

:: ===============================
::  Configura Maven
:: ===============================
echo [2/4] Configurando Maven...

set MVN_CMD=
set MVN_FOUND=0

:: Primeiro, verifica se o Maven embutido existe (caminho relativo - pasta pai)
set "MVN_EMBEDDED=..\tools\maven\apache-maven-3.9.9\bin\mvn.cmd"
if exist "%MVN_EMBEDDED%" (
    set "MVN_CMD=%MVN_EMBEDDED%"
    set MVN_FOUND=1
    echo Usando Maven embutido: %MVN_CMD%
) else (
    :: Verifica se está na mesma pasta
    set "MVN_EMBEDDED=tools\maven\apache-maven-3.9.9\bin\mvn.cmd"
    if exist "%MVN_EMBEDDED%" (
        set "MVN_CMD=%MVN_EMBEDDED%"
        set MVN_FOUND=1
        echo Usando Maven embutido (mesma pasta): %MVN_CMD%
    ) else (
        :: Verifica se Maven está no PATH do sistema
        where mvn >nul 2>&1
        if not errorlevel 1 (
            set "MVN_CMD=mvn"
            set MVN_FOUND=1
            echo Usando Maven do sistema: %MVN_CMD%
        )
    )
)

if %MVN_FOUND%==0 (
    echo.
    echo ERRO: Maven nao encontrado!
    echo.
    echo Procurando em:
    echo   - ..\tools\maven\apache-maven-3.9.9\bin\mvn.cmd
    echo   - tools\maven\apache-maven-3.9.9\bin\mvn.cmd
    echo   - PATH do sistema
    echo.
    echo Diretorio atual: %CD%
    echo.
    echo Por favor, verifique se o Maven embutido existe ou instale o Maven.
    echo.
    pause
    exit /b 1
)
echo.

:: ===============================
::  Compila o projeto
:: ===============================
echo [3/4] Compilando o projeto...
echo.

%MVN_CMD% clean compile -DskipTests
if errorlevel 1 (
    echo.
    echo ERRO: Falha na compilacao do projeto.
    echo Verifique as mensagens de erro acima.
    echo.
    pause
    exit /b 1
)

echo.
echo Compilacao concluida com sucesso!
echo.

:: ===============================
::  Executa o aplicativo
:: ===============================
echo [4/4] Iniciando o aplicativo...
echo.

:: Usa o plugin JavaFX do Maven para executar
%MVN_CMD% javafx:run -DskipTests

if errorlevel 1 (
    echo.
    echo AVISO: Falha ao executar com javafx:run. Tentando metodo alternativo...
    echo.
    
    :: Método alternativo: executa diretamente a classe Main
    set MAIN_CLASS=com.buyo.adminfx.ui.MainApp
    
    :: Verifica se as classes compiladas existem
    if not exist "target\classes\com\buyo\adminfx\ui\MainApp.class" (
        echo ERRO: Classe principal nao encontrada.
        echo Por favor, compile o projeto primeiro.
        pause
        exit /b 1
    )
    
    :: Executa usando classpath
    java -cp "target\classes;target\dependency\*" %MAIN_CLASS%
    
    if errorlevel 1 (
        echo.
        echo ERRO: Falha ao executar o aplicativo.
        echo Verifique se todas as dependencias estao corretas.
        pause
        exit /b 1
    )
)

echo.
echo ========================================
echo   Aplicativo finalizado.
echo ========================================
echo.
pause

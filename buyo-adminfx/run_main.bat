@echo off
setlocal

cd /d "%~dp0"

set "MVN_CMD=..\tools\maven\apache-maven-3.9.9\bin\mvn.cmd"

if not exist "%MVN_CMD%" (
    echo ERRO: Maven embutido nao encontrado em:
    echo   %MVN_CMD%
    echo.
    echo Verifique se a pasta tools\maven existe ao lado do projeto.
    pause
    exit /b 1
)

"%MVN_CMD%" clean javafx:run -DskipTests -Pbuyo

if errorlevel 1 (
    echo.
    echo ERRO ao executar a aplicacao via Maven.
    echo Verifique as mensagens acima.
    echo.
    pause
    exit /b 1
)

echo.
echo Aplicacao finalizada.
pause

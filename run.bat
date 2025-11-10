@echo off
setlocal enabledelayedexpansion

:: Compila o projeto
echo Compilando o projeto...
call mvn clean package -f buyo-adminfx\pom.xml

if %ERRORLEVEL% neq 0 (
    echo Erro ao compilar o projeto.
    pause
    exit /b 1
)

:: Executa o aplicativo usando o Maven
echo Iniciando o aplicativo...
call mvn javafx:run -f buyo-adminfx\pom.xml

if %ERRORLEVEL% neq 0 (
    echo.
    echo Ocorreu um erro ao executar o aplicativo.
    echo Verifique se todas as dependências estão corretamente instaladas.
    pause
)

endlocal

@echo off
setlocal

:: Configuração do Java
set JAVA_HOME="C:\Program Files\Java\jdk-21"
set PATH=%JAVA_HOME%\bin;%PATH%

:: Configuração do JavaFX
set JAVAFX_HOME=C:\javafx\javafx-sdk-21.0.2
set MODULE_PATH=%JAVAFX_HOME%\lib

echo Executando o aplicativo em modo de depuração...
echo A saída será salva em debug_log.txt

java ^
     --module-path %MODULE_PATH% ^
     --add-modules javafx.controls,javafx.fxml ^
     -Dskip.database=true ^
     -Ddebug=true ^
     -jar target\buyo-adminfx-1.0.0-shaded.jar ^
     1> debug_stdout.txt 2> debug_stderr.txt

echo.
echo Execução concluída. Verifique os arquivos debug_stdout.txt e debug_stderr.txt para mais informações.
pause

@echo off
setlocal

:: Configuração do Java
set JAVA_HOME="C:\Program Files\Java\jdk-21"
set PATH=%JAVA_HOME%\bin;%PATH%

:: Configuração do JavaFX
set JAVAFX_HOME=C:\javafx\javafx-sdk-21.0.2
set MODULE_PATH=%JAVAFX_HOME%\lib

:: Configuração para desativar temporariamente o banco de dados
set SKIP_DATABASE=true

:: Executar o aplicativo com logs detalhados
echo Iniciando o aplicativo em modo de depuração...
java ^
     --module-path %MODULE_PATH% ^
     --add-modules javafx.controls,javafx.fxml ^
     -Djavafx.verbose=true ^
     -Dprism.verbose=true ^
     -Dprism.order=es2,es1,sw ^
     -Dprism.forceGPU=true ^
     -Dprism.lcdtext=false ^
     -Dprism.text=t2k ^
     -Dprism.vsync=false ^
     -Dprism.dirtyopts=true ^
     -Dprism.allowhidpi=true ^
     -Dprism.showdirtyregions=true ^
     -Dskip.database=true ^
     -Ddebug=true ^
     -Dlogging.level.com.buyo.adminfx=DEBUG ^
     -jar target\buyo-adminfx-1.0.0-shaded.jar

pause

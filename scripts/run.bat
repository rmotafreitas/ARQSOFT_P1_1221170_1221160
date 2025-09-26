@echo off
SETLOCAL
set "PORT=%~1"
if "%PORT%"=="" set "PORT=8080"
echo [run] Iniciando aplicação na porta %PORT%...

set "JAR="
if exist target\*.jar (
  for %%F in (target\*.jar) do (
    echo %%~nF | findstr /i "original" >nul
    if errorlevel 1 (
      set "JAR=%%~fF"
      goto :found
    )
  )
)
:found
if defined JAR (
  echo [run] Encontrado jar: %JAR%
  java -Dserver.port=%PORT% -jar "%JAR%"
  if ERRORLEVEL 1 exit /b %ERRORLEVEL%
) else (
  echo [run] Nenhum jar encontrado, executando 'mvn spring-boot:run'
  mvn -Dspring-boot.run.arguments="--server.port=%PORT%" spring-boot:run
)
ENDLOCAL
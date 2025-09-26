@echo off
SETLOCAL
echo [build] Iniciando build Maven (pulando testes)...
mvn -B -DskipTests clean package
if ERRORLEVEL 1 (
  echo [build] Maven retornou erro.
  exit /b %ERRORLEVEL%
)
echo [build] Build finalizado. Artefatos em target\:
dir target\*.jar 2>nul || echo [build] Nenhum .jar encontrado.
ENDLOCAL
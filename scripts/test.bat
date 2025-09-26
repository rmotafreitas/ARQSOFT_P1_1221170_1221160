@echo off
SETLOCAL
echo [test] Executando testes Maven...
mvn -B test
if ERRORLEVEL 1 (
  echo [test] Testes reportaram falha.
  exit /b %ERRORLEVEL%
)
echo [test] Testes finalizados. Veja target\surefire-reports\ para detalhes.
dir target\surefire-reports 2>nul || echo [test] Nenhum relatório encontrado.
ENDLOCAL
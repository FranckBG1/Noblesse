@echo off
echo ========================================
echo Migration Factures Depot et Clients
echo ========================================
echo.

set DB_PATH=inventaire.db

if not exist %DB_PATH% (
    echo ERREUR: Base de donnees %DB_PATH% introuvable!
    echo Verifiez que vous etes dans le bon repertoire.
    pause
    exit /b 1
)

echo Base de donnees trouvee: %DB_PATH%
echo.
echo Execution de la migration...
echo.

sqlite3 %DB_PATH% < migration_clients_depot.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Migration terminee avec succes!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo ERREUR lors de la migration!
    echo ========================================
)

echo.
pause

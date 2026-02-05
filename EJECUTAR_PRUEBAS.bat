@echo off
echo Ejecutando pruebas unitarias...
echo.

echo Opcion 1: Ejecutar todas las pruebas
gradlew.bat test
echo.

echo Opcion 2: Ejecutar pruebas unitarias debug
gradlew.bat testDebugUnitTest
echo.

echo Opcion 3: Limpiar y ejecutar pruebas
gradlew.bat clean test
echo.

echo Opcion 4: Ejecutar pruebas con informacion detallada
gradlew.bat test --info
echo.

echo Opcion 5: Ejecutar pruebas instrumentadas (necesita dispositivo/emulador)
gradlew.bat connectedAndroidTest
echo.

echo Pruebas completadas. Revisa los reportes en:
echo app\build\reports\tests\testDebugUnitTest\index.html
pause

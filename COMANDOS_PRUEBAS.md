# Comandos para Ejecutar Pruebas (Gradle 8.4)

## Para Windows (Android Studio Terminal)

### Ejecutar TODAS las pruebas unitarias:
```bash
gradlew.bat test
```

### Ejecutar pruebas de una clase específica:
```bash
gradlew.bat test --tests "*AuthManagerTest"
```

### Ejecutar pruebas con un patrón:
```bash
gradlew.bat test --tests "*Test"
```

### Ejecutar solo pruebas unitarias (no instrumentadas):
```bash
gradlew.bat testDebugUnitTest
```

### Ejecutar pruebas instrumentadas:
```bash
gradlew.bat connectedAndroidTest
```

## Si sigue sin funcionar, prueba estas alternativas:

### Opción 1: Usar Gradle directamente
```bash
gradle test
```

### Opción 2: Especificar el task completo
```bash
gradlew.bat :app:testDebugUnitTest
```

### Opción 3: Ejecutar desde Android Studio
1. Click derecho en la carpeta `app`
2. Seleccionar "Run Tests in 'app'"
3. O click derecho en una clase de prueba y "Run"

### Opción 4: Usar el panel de Gradle
1. Abre el panel "Gradle" (derecha)
2. Expande :app → Tasks → verification
3. Doble click en "test"

## Para ver reportes:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

## Si nada funciona, prueba esto:
```bash
gradlew.bat clean
gradlew.bat build
gradlew.bat test
```

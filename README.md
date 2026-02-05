Tikets app
Sistema de Gestión de Tickets
 Visión General
SoporteTickets es una aplicación móvil desarrollada en Kotlin para la gestión de tickets de soporte técnico, diseñada para facilitar la comunicación entre clientes y equipos de soporte, brindando facilidad en la gestión de soporte.
 Tecnologías Principales
Lenguajes y Plataformas
•	Lenguaje Principal: Kotlin 1.8.0+
•	Plataforma: Android (minSdk 24, targetSdk 34)
•	Arquitectura: MVVM (Model-View-ViewModel)
Componentes Principales
•	Autenticación: Firebase Authentication.
•	Base de Datos: Firestore.
•	Almacenamiento: Firebase Storage.
•	Notificaciones: Firebase Cloud Messaging (FCM).
•	Inyección de Dependencias: Hilt.
•	Corrutinas: Para operaciones asíncronas.
•	Navegación: Navigation Component.
 Estructura del Proyecto
app/
├── src/
│   ├── main/
│   │   ├── java/com/supportticketapp/
│   │   │   ├── data/          # Capa de datos
│   │   │   │   ├── model/     # Modelos de datos
│   │   │   │   └── repository # Repositorios
│   │   │   │
│   │   │   ├── di/            # Inyección de dependencias
│   │   │   │
│   │   │   ├── presentation/  # Capa de presentación
│   │   │   │   ├── auth/      # Autenticación
│   │   │   │   ├── screen/    # Pantallas
│   │   │   │   └── viewmodel/ # ViewModels
│   │   │   │
│   │   │   └── util/          # Utilidades
│   │   │
│   │   └── res/               # Recursos
│   │
│   └── test/                  # Pruebas unitarias
 Flujos Principales
Autenticación
1.	Inicio de Sesión de Soporte
•	Validación de credenciales con Firebase Auth.
•	Redirección según rol (soporte/cliente).
2.	Inicio de Sesión de Cliente
•	Autenticación con Google One Tap
•	Registro automático si es nuevo usuario
Gestión de Tickets
•	Creación de tickets con descripción y archivos adjuntos
•	Posibilidad de escaneo de código QR.
•	Seguimiento de estado en tiempo real.
•	Notificaciones push para actualizaciones de estados.
 Mejoras Futuras
Prioridad Alta
•	Soporte Multilenguaje
•	Internacionalización completa (inglés, español)
•	Detección automática de idioma del dispositivo
•	Mejoras en Notificaciones
•	Notificaciones en primer plano.
•	Acciones rápidas en notificaciones.
•	Agrupación de notificaciones.
•	Optimización de Imágenes
•	Compresión automática de imágenes
•	Vista previa de imágenes antes de subir
Prioridad Media
•	Chat en Tiempo Real
•	Integración con Firestore para mensajería.
•	Indicadores de estado en línea.
•	Notificaciones push para mensajes.
•	Dashboard de Analíticas
•	Métricas de rendimiento del equipo.
•	Tiempo promedio de respuesta.
•	Satisfacción del cliente.
•	Soporte Offline
•	Sincronización automática al recuperar conexión.
•	Indicador de estado de conexión.
•	Cache de datos recientes.
Prioridad Baja
•	Sistema de Encuestas
•	Valoración de la atención.
•	Comentarios post-soporte.
•	Métricas de satisfacción.
Configuración del Entorno
Requisitos
•	Android Studio Otter 3 Feature Drop | 2025.2.3
Runtime version: 21.0.8+-14196175-b1038.72 amd64VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
•	Windows 11.0
•	Memory: 2048M
•	Cores: 12
•	  ide.experimental.ui=trueJDK 17
•	Android SDK 34
•	Cuenta de Firebase configurada.
Configuración Inicial
1.	Clonar el repositorio
2.	Sincronizar proyecto con Gradle
3.	Archivo google-services.json con credenciales de Firebase incluido en el proyecto, sin exponer claves públicas.
4.	Ejecutar la aplicación en un emulador con api 34 o dispositivo físico mediante conexión USB o wifi, preferiblemente dispositivo físico para una mejor visualización.
5.	Elegir entre modo soporte ingresando con las siguientes credenciales
*correo: olamundo@gmail.com
*clave: 123456789
Y se abrirá la lista de tickets disponibles
6.	Para abrir modo cliente, hacer clic en “botón cliente” y aparecerá inicio de sesión mediante autenticación de Google sing-in, lo cual será redirigido a la pantalla de creación de ticket o visualización de tickets creados anteriormente por el usuario.
Nota importante : Para cambio entre pantallas de cliente a soporte es necesario oprimir botón de “cerrar sesión”
 Notas de Implementación
Patrones de Diseño
•	MVVM: Separación clara entre lógica y UI
•	Repository Pattern: Para el manejo de datos
•	Observer Pattern: Para actualizaciones en tiempo real
Convenciones de Código
•	Nombres descriptivos en inglés.
•	Comentarios en español.
•	Estructura de paquetes por funcionalidad.
 Métricas de Calidad
•	Cobertura de pruebas: 75%+.
•	Tiempo de compilación: < 2 min.


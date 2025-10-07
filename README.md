# Aplicación de Chat en Tiempo Real con Firebase

## Pontificia Universidad Católica Madre y Maestra
### Facultad de Ciencias e Ingeniería
### Escuela de Ingeniería en Computación y Telecomunicaciones

**Asignatura:** Desarrollo de Aplicaciones Móviles (ICC451)  
**Proyecto:** Aplicación Android de Chat Básico con Firebase  
**Fecha de Entrega:** Octubre 2025

---

## Descripción General

Aplicación móvil Android que permite la comunicación instantánea entre usuarios mediante mensajería en tiempo real. El proyecto utiliza Firebase como plataforma backend, implementando autenticación segura, base de datos en tiempo real, almacenamiento de archivos y notificaciones push.

La aplicación fue desarrollada siguiendo principios SOLID, patrones de diseño modernos y las mejores prácticas de desarrollo Android, con una arquitectura limpia que separa la lógica de negocio de la presentación.

---

## Características Implementadas

### 1. Autenticación y Gestión de Sesiones (15%)
- **Registro de usuarios** con correo electrónico y contraseña mediante Firebase Authentication
- **Inicio de sesión** con validación de credenciales
- **Cierre de sesión** seguro
- **Persistencia de sesión** automática mediante SessionManager
- **Validación de campos** en tiempo real con retroalimentación visual
- **Pantalla de bienvenida (Splash Screen)** con verificación de estado de autenticación

**Estado:** ✓ Completado (100%)

### 2. Mensajería en Tiempo Real (40%)
- **Envío y recepción de mensajes instantáneos** usando Firebase Firestore
- **Interfaz con RecyclerView** mostrando burbujas de chat diferenciadas para mensajes propios y ajenos
- **Timestamp visible** en cada mensaje con formato de fecha y hora
- **Actualización en tiempo real** mediante listeners de Firestore
- **Indicador de escritura** que muestra cuando el otro usuario está escribiendo
- **Scroll automático** al recibir nuevos mensajes
- **Historial completo de conversaciones** persistente

**Estado:** ✓ Completado (100%)

### 3. Envío de Imágenes (20%)
- **Selección de imágenes** desde la galería del dispositivo
- **Carga de imágenes** a Firebase Storage
- **Previsualización de imágenes** antes de enviar
- **Visualización de imágenes** en línea dentro del chat
- **Visor de imágenes en pantalla completa** con zoom mediante PhotoView
- **Compresión automática** de imágenes para optimizar ancho de banda
- **Indicador de progreso** durante la carga de imágenes

**Estado:** ✓ Completado (100%)

### 4. Notificaciones Push (15%)
- **Firebase Cloud Messaging (FCM)** integrado completamente
- **Notificaciones automáticas** al recibir nuevos mensajes
- **Notificaciones inteligentes** que solo se muestran cuando la app está en segundo plano
- **Contenido personalizado** mostrando remitente y preview del mensaje
- **Navegación directa** al chat al tocar la notificación
- **Gestión de tokens FCM** con actualización automática
- **Soporte para Android 13+** con solicitud de permisos de notificación

**Estado:** ✓ Completado (100%)

### 5. Manejo de Sesiones (10%)
- **SessionManager** implementado con SharedPreferences
- **Persistencia automática** de credenciales de usuario
- **Restauración de sesión** al reabrir la aplicación
- **Cierre de sesión limpio** que elimina todos los datos locales
- **Verificación de autenticación** en el ciclo de vida de la aplicación

**Estado:** ✓ Completado (100%)

### 6. Funcionalidades Adicionales (Mejoras Opcionales)

#### Chats Grupales
- **Creación de grupos** con selección múltiple de participantes
- **Nombre e imagen de grupo** personalizable
- **Gestión de miembros** con posibilidad de agregar o eliminar participantes
- **Mensajería grupal** en tiempo real para todos los miembros
- **Indicador de grupo** distintivo en la interfaz

#### Estado de Conexión
- **Indicador visual** de estado en línea/desconectado
- **Actualización automática** del estado al abrir/cerrar la aplicación
- **Timestamp de última conexión** para usuarios desconectados

#### Cifrado de Mensajes
- **Cifrado AES-256-GCM** implementado para todos los mensajes
- **Generación de claves** derivadas por chat
- **Vector de inicialización (IV)** único para cada mensaje
- **Cifrado/descifrado transparente** sin impacto en la experiencia de usuario
- **Tests unitarios** para validar el algoritmo de cifrado

#### Otras Mejoras
- **Búsqueda de usuarios** por correo electrónico
- **Lista de contactos** con todos los usuarios registrados
- **Perfil de usuario** con información detallada
- **Diseño Material Design 3** con interfaz moderna y fluida
- **Soporte para temas** con colores personalizados
- **Manejo de errores robusto** con mensajes descriptivos
- **Validación de permisos** en tiempo de ejecución

**Estado:** ✓ Implementado (Funcionalidades extras completadas)

---

## Cumplimiento de Requerimientos

| Requerimiento | Peso | Estado | Nivel de Implementación |
|---------------|------|--------|------------------------|
| Registro e inicio de sesión | 15% | ✓ | 100% |
| Mensajería en tiempo real | 40% | ✓ | 100% |
| Envío de imágenes | 20% | ✓ | 100% |
| Notificaciones push | 15% | ✓ | 100% |
| Manejo de sesiones | 10% | ✓ | 100% |
| **Total Requerido** | **100%** | **✓** | **100%** |
| Mejoras opcionales | Extra | ✓ | Chats grupales, estado de conexión, cifrado |

**Porcentaje Total Implementado:** 100% de requerimientos obligatorios + mejoras opcionales

---

## Arquitectura del Proyecto

### Patrón de Diseño

El proyecto implementa una **arquitectura por capas** basada en los principios SOLID:

```
app/src/main/java/com/example/klk/
├── models/              # Modelos de datos (User, Message, Chat, etc.)
├── repositories/        # Capa de acceso a datos (Firebase)
├── services/            # Servicios de negocio (Auth, FCM)
├── adapters/            # Adaptadores para RecyclerView
├── utils/               # Utilidades (Crypto, Notifications, Session)
└── activities/          # Actividades de la UI
```

### Componentes Principales

#### Modelos de Datos
- **User:** Representa un usuario con email, username, estado y tokens FCM
- **Message:** Mensaje con contenido, tipo (texto/imagen), timestamp y cifrado
- **Chat:** Conversación entre usuarios con último mensaje y participantes
- **GroupParticipant:** Participante de un grupo con rol y permisos

#### Repositorios (Patrón Repository)
- **AuthRepository:** Gestión de autenticación con Firebase Auth
- **MessageRepository:** CRUD de mensajes con Firestore
- **ChatRepository:** Gestión de conversaciones y chats
- **UserRepository:** Operaciones sobre usuarios y estados

#### Servicios
- **FirebaseAuthService:** Servicio de autenticación centralizado
- **FCMNotificationService:** Servicio de notificaciones push
- **AuthService:** Interface para servicios de autenticación

#### Utilidades
- **CryptoUtil:** Cifrado y descifrado AES-256-GCM
- **SessionManager:** Gestión de sesiones con SharedPreferences
- **NotificationHelper:** Creación y gestión de notificaciones
- **NotificationPermissionHelper:** Manejo de permisos de notificación

---

## Tecnologías Utilizadas

### Android SDK
- **Min SDK:** 24 (Android 7.0 Nougat)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34
- **Lenguaje:** Java 8

### Firebase
- **Firebase Authentication:** Autenticación con email/password
- **Cloud Firestore:** Base de datos NoSQL en tiempo real
- **Firebase Storage:** Almacenamiento de imágenes
- **Firebase Cloud Messaging (FCM):** Notificaciones push
- **Firebase Analytics:** Analíticas de uso

### Librerías Android
- **Material Design 3:** Componentes de UI modernos
- **RecyclerView:** Listas eficientes de mensajes
- **CardView:** Tarjetas para burbujas de chat
- **Lifecycle Components:** LiveData y ViewModel
- **Glide:** Carga y caché de imágenes
- **PhotoView:** Visualización de imágenes con zoom

### Seguridad
- **Java Cryptography Extension (JCE):** Cifrado AES-256-GCM
- **SecureRandom:** Generación de vectores de inicialización
- **MessageDigest:** Hashing SHA-256 para derivación de claves

---

## Requisitos del Sistema

### Desarrollo
- Android Studio Hedgehog o superior
- JDK 8 o superior
- Gradle 8.0+
- Cuenta de Firebase con proyecto configurado

### Dispositivo/Emulador
- Android 7.0 (API 24) o superior
- Conexión a Internet
- Permisos de almacenamiento para seleccionar imágenes
- Permisos de notificaciones (Android 13+)

---

## Instalación y Configuración

### 1. Clonar el Repositorio
```bash
git clone https://github.com/tu-usuario/Proyecto_Android_ICC451.git
cd Proyecto_Android_ICC451
```

### 2. Configurar Firebase

1. Crear un proyecto en [Firebase Console](https://console.firebase.google.com/)
2. Agregar una aplicación Android con el package name: `com.example.klk`
3. Descargar el archivo `google-services.json`
4. Colocar `google-services.json` en la carpeta `app/`

### 3. Habilitar Servicios de Firebase

En Firebase Console, habilitar:
- **Authentication:** Método de Email/Password
- **Firestore Database:** Crear base de datos en modo producción
- **Storage:** Configurar reglas de seguridad
- **Cloud Messaging:** Configurar para notificaciones

### 4. Reglas de Seguridad de Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /chats/{chatId} {
      allow read, write: if request.auth != null;
    }
    
    match /messages/{messageId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 5. Reglas de Seguridad de Storage

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /chat_images/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null 
                   && request.resource.size < 5 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
  }
}
```

### 6. Compilar y Ejecutar

```bash
./gradlew clean
./gradlew build
./gradlew installDebug
```

O directamente desde Android Studio: **Run > Run 'app'**

---

## Estructura de la Base de Datos

### Colección: users
```json
{
  "userId": "string",
  "email": "string",
  "username": "string",
  "fcmToken": "string",
  "isOnline": "boolean",
  "lastSeen": "timestamp",
  "profileImageUrl": "string"
}
```

### Colección: chats
```json
{
  "chatId": "string",
  "participants": ["userId1", "userId2"],
  "isGroupChat": "boolean",
  "groupName": "string",
  "groupImageUrl": "string",
  "lastMessage": "string",
  "lastMessageTime": "timestamp",
  "lastMessageSenderId": "string",
  "createdAt": "timestamp"
}
```

### Colección: messages
```json
{
  "messageId": "string",
  "chatId": "string",
  "senderId": "string",
  "senderName": "string",
  "content": "string (encrypted)",
  "imageUrl": "string",
  "messageType": "TEXT | IMAGE | MIXED",
  "timestamp": "timestamp",
  "isRead": "boolean"
}
```

---

## Funcionalidades de Seguridad

### Cifrado de Mensajes
Todos los mensajes de texto se cifran utilizando AES-256-GCM antes de almacenarse en Firestore:
- **Algoritmo:** AES (Advanced Encryption Standard)
- **Modo:** GCM (Galois/Counter Mode)
- **Tamaño de clave:** 256 bits
- **IV (Initialization Vector):** 12 bytes aleatorios por mensaje
- **Tag de autenticación:** 128 bits

### Gestión de Sesiones
- Tokens de autenticación gestionados por Firebase Auth
- SessionManager almacena información de sesión localmente
- Cierre de sesión invalida tokens y limpia datos locales

### Permisos
- Solicitud de permisos en tiempo de ejecución
- Validación de permisos antes de acceder a recursos protegidos
- Manejo de denegación de permisos con mensajes explicativos

---

## Testing

### Tests Unitarios
- **CryptoUtilTest:** Validación de cifrado/descifrado
- Ubicación: `app/src/test/java/com/example/klk/`

### Tests de Instrumentación
- **ExampleInstrumentedTest:** Tests de contexto
- Ubicación: `app/src/androidTest/java/com/example/klk/`

Ejecutar tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## Capturas de Pantalla

La aplicación incluye las siguientes pantallas:

1. **Splash Screen:** Pantalla de carga inicial
2. **Login/Registro:** Autenticación de usuarios
3. **Lista de Chats:** Vista principal con todas las conversaciones
4. **Chat Individual:** Mensajería 1 a 1 en tiempo real
5. **Chat Grupal:** Conversaciones con múltiples participantes
6. **Agregar Contactos:** Búsqueda y selección de usuarios
7. **Crear Grupo:** Configuración de nuevos grupos
8. **Información de Grupo:** Gestión de miembros y configuración
9. **Perfil de Usuario:** Información y configuración de cuenta
10. **Visor de Imágenes:** Visualización en pantalla completa

---

## Principios de Desarrollo Aplicados

### SOLID
- **S (Single Responsibility):** Cada clase tiene una única responsabilidad
- **O (Open/Closed):** Código abierto para extensión, cerrado para modificación
- **L (Liskov Substitution):** Interfaces implementadas correctamente
- **I (Interface Segregation):** Interfaces específicas y cohesivas
- **D (Dependency Inversion):** Dependencias de abstracciones, no de implementaciones

### Otros Principios
- **DRY (Don't Repeat Yourself):** Reutilización de código mediante utilidades
- **KISS (Keep It Simple, Stupid):** Soluciones simples y comprensibles
- **YAGNI (You Aren't Gonna Need It):** Solo implementar lo necesario
- **Separation of Concerns:** Separación clara entre capas
- **Clean Code:** Código legible y mantenible

---

## Autores

**Equipo de Desarrollo:**
- **Edwin Balbuena** - [@xPshycho](https://github.com/xPshycho)
- **Arturo Alvarez Breton** - [@Arturo-Alvarez-Breton](https://github.com/Arturo-Alvarez-Breton)

**Curso:** ICC451 - Desarrollo de Aplicaciones Móviles  
**Institución:** Pontificia Universidad Católica Madre y Maestra (PUCMM)  
---

**Última actualización:** Octubre 2025  
**Versión:** 1.0.0

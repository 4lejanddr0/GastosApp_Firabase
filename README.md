# 💰 GastosApp

Aplicación Android para registro y control de gastos personales, desarrollada con **Kotlin** y **Jetpack Compose**, e integrada con **Firebase** para autenticación y almacenamiento en la nube.

---

## 👥 Integrantes del proyecto

- Jesus Alejandro campos Landaverde CCL212345
- Devin Ivan Mendoza Ortiz MO190319
- Consuelo Astrid Landa Hernandez LH201536
- Arturo Jose Gomez Henriquez GH191748 

---

## 🧩 Tecnologías utilizadas

| Componente | Descripción |
|-------------|-------------|
| 🧠 **Lenguaje** | Kotlin |
| 🎨 **Interfaz** | Jetpack Compose (Material 3) |
| ☁️ **Backend** | Firebase (Auth / Firestore) |
| ⚙️ **Build System** | Gradle Wrapper (incluido en el proyecto) |
| 🧰 **IDE** | Android Studio (versión Giraffe o superior recomendada) |

---

## ⚙️ Clonar y abrir el proyecto


git clone https://github.com/4lejanddr0/GastosApp_Firebase.git
cd GastosApp_Firebase

1 Abrir Android Studio → Open → seleccionar la carpeta del proyecto.
2 Esperar la sincronización de Gradle.
3 Seleccionar un dispositivo o emulador y presionar Run ▶️.

🔐 Configuración de Firebase

⚠️ Importante: El archivo app/google-services.json está ignorado por seguridad y no se incluye en el repositorio.

En Firebase Console
- crea un nuevo proyecto.
- Registra tu aplicación Android (usa el applicationId que aparece en app/build.gradle.kts).
- Descarga el archivo google-services.json.
- Colócalo en la ruta: app/google-services.json
 (Puedes guiarte con el ejemplo incluido más abajo en este README).
- Sincroniza Gradle (File → Sync Project with Gradle Files).

  ▶️ Ejecución de la aplicación

1- Con el archivo google-services.json correcto, presiona Run ▶️ en Android Studio.
2- Si es la primera vez, acepta las licencias del SDK cuando lo solicite.
3- Si hay errores de Firebase, revisa que:
4- El applicationId coincida con el registrado en Firebase.
5- El archivo JSON esté ubicado correctamente en app/google-services.json.

🧪 Ejemplo de google-services.json

Este es un ejemplo de referencia
No contiene datos reales y no debe usarse en producción.

{
  "project_info": {
    "project_number": "000000000000",
    "project_id": "tu-proyecto-firebase",
    "storage_bucket": "tu-proyecto-firebase.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:000000000000:android:0000000000000000",
        "android_client_info": {
          "package_name": "com.tuempresa.gastos"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "AIzaSyXXXXXX-TU_API_KEY_DEMO"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}

```bash


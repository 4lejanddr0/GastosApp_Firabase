# 💰 GastosApp

Aplicación Android para registro y control de gastos personales, desarrollada con **Kotlin** y **Jetpack Compose**, e integrada con **Firebase** para autenticación y almacenamiento en la nube.

---

## 👥 Integrantes del proyecto

- **Jesús Alejandro Campos Landaverde** — CCL212345  
- **Devin Iván Mendoza Ortiz** — MO190319  
- **Consuelo Astrid Landa Hernández** — LH201536  
- **Arturo José Gómez Henríquez** — GH191748  

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

```bash
git clone https://github.com/4lejanddr0/GastosApp_Firebase.git
cd GastosApp_Firebase
```

1. Abrir **Android Studio** → **Open** → seleccionar la carpeta del proyecto.  
2. Esperar la **sincronización de Gradle**.  
3. Seleccionar un **dispositivo o emulador** y presionar **Run ▶️**.

---

## 🔐 Configuración de Firebase

⚠️ **Importante:** El archivo `app/google-services.json` está **ignorado por seguridad** y no se incluye en el repositorio.

1. En **Firebase Console**, crea un nuevo proyecto.  
2. Registra tu aplicación Android (usa el `applicationId` que aparece en `app/build.gradle.kts`).  
3. Descarga el archivo **`google-services.json`**.  
4. Colócalo en la ruta:  
   ```
   app/google-services.json
   ```
   *(Puedes guiarte con el ejemplo incluido más abajo en este README).*  
5. Sincroniza Gradle (**File → Sync Project with Gradle Files**).

---

## ▶️ Ejecución de la aplicación

1. Con el archivo **`google-services.json`** correcto, presiona **Run ▶️** en Android Studio.  
2. Si es la primera vez, acepta las licencias del SDK cuando lo solicite.  
3. Si hay errores de Firebase, revisa que:  
   - El `applicationId` coincida con el registrado en Firebase.  
   - El archivo JSON esté ubicado correctamente en `app/google-services.json`.

---

## 🧪 Ejemplo de `google-services.json`

> Este es un ejemplo de referencia.  
> No contiene datos reales y **no debe usarse en producción**.

```json
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
```

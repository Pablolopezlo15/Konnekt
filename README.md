# 📱 Konnekt

Konnekt es una red social nativa para Android, desarrollada con Kotlin y Jetpack Compose. Permite a los usuarios publicar contenido, interactuar mediante mensajes o comentarios, y generar comentarios automáticos usando inteligencia artificial.

---

## 🚀 Características

- Registro e inicio de sesión con validación
- Creación y edición de perfil de usuario
- Publicación de imágenes con descripciones
- Likes, comentarios y guardado de publicaciones
- Seguimiento entre usuarios
- Chat en tiempo real mediante WebSockets
- Generación de comentarios con IA (modelo local `minicpm-v` usando Ollama)
- Diseño moderno y adaptativo (tema claro/oscuro automático)

---

## 🧪 Capturas de Pantalla

> Aquí puedes añadir tus imágenes para mostrar la app funcionando.

### 🧍 Perfil de usuario
![Perfil](./screenshots/perfil.png)

### 📸 Publicación de post
![Publicación](./screenshots/publicacion.png)

### 💬 Chat en tiempo real
![Chat](./screenshots/chat.png)

### 🤖 Comentario generado por IA
![IA](./screenshots/comentario_ia.png)

---

## 🛠️ Tecnologías Utilizadas

| Tipo             | Tecnología                     |
|------------------|--------------------------------|
| Lenguaje         | Kotlin (Jetpack Compose)       |
| Backend          | Python (FastAPI + WebSockets)  |
| Base de datos    | MongoDB                        |
| IA               | Ollama + minicpm-v             |
| UI/UX            | Figma                          |
| Control de versiones | Git + GitHub              |

---

## ⚙️ Instalación

### App Android

1. Clona el repositorio:
   ```bash
   git clone https://github.com/tuusuario/Konnekt.git
   ```
2. Abre el proyecto en Android Studio.
3. Configura la IP del backend en `config/AppConfig.kt`.
4. Conecta un dispositivo Android o usa un emulador.
5. Ejecuta la app.

### Backend

#### Opción 1: Docker
```bash
cd Backend
docker compose build
docker compose up
```

#### Opción 2: Python
```bash
cd Backend
pip install -r requirements.txt
./start.sh    # Genera claves SSL
uvicorn main:app --host [TU_IP] --port 8000 \
--ssl-keyfile=certs/key.pem --ssl-certfile=certs/cert.pem
```

---

## 📦 Estructura de la Base de Datos (MongoDB)

```json
{
  "users": {...},
  "posts": {...},
  "comments": {...},
  "messages": {...},
  "likes": {...},
  "saved_posts": {...},
  "friends_requests": {...}
}
```

---

## 📌 Estado del Proyecto

✅ Funcional  
🔄 En desarrollo activo  
🚧 Pendientes: sistema de notificaciones, historias, publicación en Play Store

---

## 📚 Créditos y Bibliografía

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [FastAPI](https://fastapi.tiangolo.com/)
- [MongoDB](https://www.mongodb.com/docs/)
- [Ollama + minicpm-v](https://ollama.com/library/minicpm-v)
- [Figma](https://www.figma.com/)
- [ChatGPT (OpenAI)](https://chat.openai.com)
- [Grok (xAI)](https://x.ai)

---

## 🧑‍💻 Autor

**Pablo López Lozano**  
[GitHub](https://github.com/Pablolopezlo15)

---

## 📄 Licencia

Este proyecto está bajo la licencia MIT - consulta el archivo [LICENSE](LICENSE) para más detalles.

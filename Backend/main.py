from fastapi import FastAPI, WebSocket, WebSocketDisconnect, HTTPException
from motor.motor_asyncio import AsyncIOMotorClient
from typing import Dict, List
import json
from datetime import datetime
from dotenv import load_dotenv
import os
from pydantic import BaseModel
from passlib.context import CryptContext
from jose import jwt
from datetime import timedelta

# Cargar variables de entorno
load_dotenv()

app = FastAPI()

# Conexión con MongoDB
MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017")
client = AsyncIOMotorClient(MONGO_URI)
db = client.chat_db
messages_collection = db.messages

class ConnectionManager:
    def __init__(self):
        self.active_connections: Dict[str, WebSocket] = {}

    async def connect(self, websocket: WebSocket, user_id: str):
        if user_id in self.active_connections:
            print(f"Reconectando usuario: {user_id}")
            await self.disconnect(user_id)
        await websocket.accept()
        self.active_connections[user_id] = websocket
        print(f"Cliente conectado: {user_id}. Total conexiones: {len(self.active_connections)}")
        print(f"Conexiones activas: {list(self.active_connections.keys())}")

    async def disconnect(self, user_id: str):
        if user_id in self.active_connections:
            del self.active_connections[user_id]
            print(f"Cliente desconectado: {user_id}. Total conexiones: {len(self.active_connections)}")
            print(f"Conexiones activas: {list(self.active_connections.keys())}")

    async def send_message(self, message: dict, user_id: str):
        """Enviar mensaje a un usuario específico"""
        print(f"Intentando enviar mensaje a {user_id}")
        if user_id in self.active_connections:
            connection = self.active_connections[user_id]
            try:
                # Eliminar la verificación del estado
                await connection.send_json(message)
                print(f"Mensaje enviado exitosamente a {user_id}")
            except Exception as e:
                print(f"Error al enviar mensaje a {user_id}: {e}")
                await self.disconnect(user_id)
        else:
            print(f"Usuario {user_id} no tiene conexión activa")

    async def broadcast_to_chat(self, message: dict):
        """Enviar mensaje solo a los participantes del chat"""
        sender_id = message["sender_id"]
        recipient_id = message["recipient_id"]
        
        # Enviar al remitente
        await self.send_message(message, sender_id)
        # Enviar al destinatario
        await self.send_message(message, recipient_id)
        print(f"Mensaje enviado a {sender_id} y {recipient_id}: {message}")

manager = ConnectionManager()

@app.websocket("/ws/{user_id}")
async def websocket_endpoint(websocket: WebSocket, user_id: str):
    await manager.connect(websocket, user_id)
    try:
        while True:
            data = await websocket.receive_text()
            print(f"Mensaje recibido de {user_id}: {data}")

            try:
                json_data = json.loads(data)
                recipient_id = json_data["recipient_id"]
                message_text = json_data["message"]
                chat_id = "_".join(sorted([user_id, recipient_id]))  # Ordenar para evitar duplicados
                
                # Ajustar el formato del timestamp para que coincida con el cliente
                timestamp = datetime.utcnow().strftime("%a %b %d %H:%M:%S GMT %Y")
                
                message = {
                    "chat_id": chat_id,
                    "sender_id": user_id,
                    "recipient_id": recipient_id,
                    "message": message_text,
                    "timestamp": timestamp
                }
                insert_result = await messages_collection.insert_one(message)
                message["_id"] = str(insert_result.inserted_id)
                
                # Enviar el mensaje solo a los participantes del chat
                await manager.broadcast_to_chat(message)
            except json.JSONDecodeError:
                print("Error: Mensaje recibido no es un JSON válido")
            except KeyError as e:
                print(f"Error: Falta campo requerido en el mensaje: {e}")
    except WebSocketDisconnect:
        await manager.disconnect(user_id)
    except Exception as e:
        print(f"Error en WebSocket para {user_id}: {e}")
        await manager.disconnect(user_id)
    finally:
        await manager.disconnect(user_id)
        print(f"Conexión cerrada para {user_id}")

@app.get("/messages/{chat_id}")
async def get_messages(chat_id: str):
    chat_messages = await messages_collection.find({"chat_id": chat_id}).to_list(100)
    if not chat_messages:
        return []
        
    for message in chat_messages:
        message["_id"] = str(message["_id"])
        # Asegurar formato consistente del timestamp
        if "timestamp" in message:
            try:
                dt = datetime.strptime(message["timestamp"], "%a %b %d %H:%M:%S GMT %Y")
                message["timestamp"] = dt.strftime("%a %b %d %H:%M:%S GMT %Y")
            except ValueError:
                message["timestamp"] = datetime.utcnow().strftime("%a %b %d %H:%M:%S GMT %Y")
    
    return chat_messages

# Configuración de hashing de contraseñas y JWT (sin cambios)
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
SECRET_KEY = os.getenv("SECRET_KEY", "your-secret-key")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

# Modelos de datos (sin cambios)
class UserCreate(BaseModel):
    username: str
    password: str
    email: str
    phone: str | None = None
    birth_date: str | None = None
    profile_image_url: str | None = None

class UserResponse(BaseModel):
    id: str
    username: str
    email: str
    profile_image_url: str | None = None
    phone: str | None = None
    birth_date: str | None = None
    followers: List[str] = []
    following: List[str] = []

class UserLogin(BaseModel):
    username: str
    password: str

users_collection = db.users

# Endpoints de gestión de usuarios (sin cambios significativos)
@app.post("/register")
async def register(user: UserCreate):
    if await users_collection.find_one({"username": user.username}):
        raise HTTPException(status_code=400, detail="Username already registered")
    
    hashed_password = pwd_context.hash(user.password)
    
    user_doc = {
        "username": user.username,
        "password": hashed_password,
        "email": user.email,
        "phone": user.phone,
        "birth_date": user.birth_date,
        "profile_image_url": user.profile_image_url,
        "followers": [],
        "following": []
    }
    
    result = await users_collection.insert_one(user_doc)
    response_doc = {
        "id": str(result.inserted_id),
        "username": user_doc["username"],
        "email": user_doc["email"],
        "phone": user_doc["phone"],
        "birth_date": user_doc["birth_date"],
        "profile_image_url": user_doc["profile_image_url"],
        "followers": user_doc["followers"],
        "following": user_doc["following"]
    }
    
    return UserResponse(**response_doc)

@app.get("/users")
async def get_users():
    users = await users_collection.find().to_list(1000)
    response_users = []
    for user in users:
        response_users.append({
            "id": str(user["_id"]),
            "username": user["username"],
            "email": user["email"],
            "profile_image_url": user.get("profile_image_url"),
            "phone": user.get("phone"),
            "birth_date": user.get("birth_date"),
            "followers": user.get("followers", []),
            "following": user.get("following", [])
        })
    
    return [UserResponse(**user) for user in response_users]

@app.post("/login")
async def login(user: UserLogin):
    db_user = await users_collection.find_one({"username": user.username})
    if not db_user or not pwd_context.verify(user.password, db_user["password"]):
        raise HTTPException(status_code=400, detail="Incorrect username or password")
    
    access_token_expires = timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = jwt.encode(
        {
            "exp": datetime.utcnow() + access_token_expires,
            "user_id": str(db_user["_id"]),
            "profile_image_url": db_user.get("profile_image_url"),
            "phone": db_user.get("phone"),
            "birth_date": db_user.get("birth_date"),
            "followers": db_user.get("followers", []),
            "following": db_user.get("following", []),
            "email": db_user["email"],
            "username": db_user["username"]
        },
        SECRET_KEY,
        algorithm=ALGORITHM
    )
    
    return {"access_token": access_token, "token_type": "bearer"}
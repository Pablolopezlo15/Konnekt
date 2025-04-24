from fastapi import WebSocket
from typing import Dict

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
        print(f"Intentando enviar mensaje a {user_id}")
        if user_id in self.active_connections:
            connection = self.active_connections[user_id]
            try:
                await connection.send_json(message)
                print(f"Mensaje enviado exitosamente a {user_id}")
            except Exception as e:
                print(f"Error al enviar mensaje a {user_id}: {e}")
                await self.disconnect(user_id)
        else:
            print(f"Usuario {user_id} no tiene conexión activa")

    async def broadcast_to_chat(self, message: dict):
        sender_id = message["sender_id"]
        recipient_id = message["recipient_id"]
        await self.send_message(message, sender_id)
        await self.send_message(message, recipient_id)
        print(f"Mensaje enviado a {sender_id} y {recipient_id}: {message}")
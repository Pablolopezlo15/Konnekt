from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from ..utils.websocket_manager import ConnectionManager
from ..database import messages_collection
import json
from datetime import datetime

router = APIRouter()
manager = ConnectionManager()

@router.websocket("/ws/{user_id}")
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
                chat_id = "_".join(sorted([user_id, recipient_id]))
                
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

@router.get("/messages/{chat_id}")
async def get_messages(chat_id: str):
    chat_messages = await messages_collection.find({"chat_id": chat_id}).to_list(100)
    if not chat_messages:
        return []
        
    for message in chat_messages:
        message["_id"] = str(message["_id"])
        if "timestamp" in message:
            try:
                dt = datetime.strptime(message["timestamp"], "%a %b %d %H:%M:%S GMT %Y")
                message["timestamp"] = dt.strftime("%a %b %d %H:%M:%S GMT %Y")
            except ValueError:
                message["timestamp"] = datetime.utcnow().strftime("%a %b %d %H:%M:%S GMT %Y")
    
    return chat_messages
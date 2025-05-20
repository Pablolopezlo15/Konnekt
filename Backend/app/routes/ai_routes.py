from fastapi import APIRouter, HTTPException, Header, Depends
from typing import Dict
import os
from dotenv import load_dotenv
import requests
from ..utils.auth import decode_token

load_dotenv()

router = APIRouter()
OLLAMA_API_URL = "http://localhost:11434/api/generate"

@router.post("/generate-comment")
async def generate_ai_comment(
    image_url: Dict[str, str],
    authorization: str | None = Header(default=None, alias="Authorization")
):
    try:
        if not authorization:
            raise HTTPException(
                status_code=401, 
                detail="Se requiere el encabezado de autorización"
            )
        
        try:
            if authorization.startswith("Bearer "):
                token = authorization.split(" ")[1]
            else:
                token = authorization
                
            user_data = decode_token(token)
            
            # Configuración de la solicitud a Ollama
            ollama_request = {
                "model": "minicpm-v",
                "prompt": "Imagina que estas en una red social, genera un comentario para un post de otra persona, positivo y natural en español para esta imagen. Responde el comentario solamente sin ninguna introduccion como aqui tienes un posible comentario",
                "images": [image_url["url"]],
                "stream": False
            }
            
            # Agregar mejor manejo de errores y logging
            try:
                response = requests.post(
                    OLLAMA_API_URL,
                    json=ollama_request,
                    stream=False
                )
                
                if response.status_code == 200:
                    response_json = response.json()
                    comment = response_json.get("response", "").strip('"')
                    return {"comment": comment}
                else:
                    print(f"Error de Ollama - Status Code: {response.status_code}")
                    print(f"Error de Ollama - Response: {response.text}")
                    raise HTTPException(
                        status_code=500, 
                        detail=f"Error al generar el comentario: {response.text}"
                    )
            except requests.exceptions.RequestException as e:
                print(f"Error de conexión con Ollama: {str(e)}")
                raise HTTPException(
                    status_code=500,
                    detail="Error de conexión con el servicio de IA"
                )
                
        except HTTPException as he:
            raise he
        except Exception as e:
            raise HTTPException(status_code=401, detail="Token de autenticación inválido")
            
    except Exception as e:
        print(f"Error en generate_ai_comment: {str(e)}")
        raise HTTPException(
            status_code=500, 
            detail=f"Error en el servicio de IA: {str(e)}"
        )
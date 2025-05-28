from fastapi import APIRouter, HTTPException, Query, File, UploadFile
from typing import List
from ..models.user import UserCreate, UserResponse, UserLogin, FriendRequest, RequestStatus
from ..utils.auth import pwd_context, create_access_token
from ..database import friend_requests_collection, users_collection
from datetime import datetime
from bson import ObjectId
import os
import shutil
from app.models.user import UserUpdate
import re
from datetime import datetime

router = APIRouter()

@router.post("/register", response_model=UserResponse)
async def register(user: UserCreate):
    # Check if username already exists
    if await users_collection.find_one({"username": user.username}):
        raise HTTPException(status_code=400, detail="Username already registered")
    
    # Check if email already exists
    if await users_collection.find_one({"email": user.email}):
        raise HTTPException(status_code=400, detail="Email already registered")
    
    # Check if phone already exists
    if user.phone and await users_collection.find_one({"phone": user.phone}):
        raise HTTPException(status_code=400, detail="Phone number already registered")
    
    # Validate username format
    if not re.match(r'^[a-zA-Z0-9_]{3,30}$', user.username):
        raise HTTPException(status_code=400, detail="Username must be 3-30 characters long and can only contain letters, numbers, and underscores")
    
    # Validate email format
    if not re.match(r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$', user.email):
        raise HTTPException(status_code=400, detail="Invalid email format")
    
    # Validate phone format (if provided)
    if user.phone and not re.match(r'^\+?[1-9]\d{1,14}$', user.phone):
        raise HTTPException(status_code=400, detail="Invalid phone number format")
    
    # Validate birth date (if provided)
    if user.birth_date:
        today = datetime.now().date()
        age = today.year - user.birth_date.year - ((today.month, today.day) < (user.birth_date.month, user.birth_date.day))
        if age < 13:
            raise HTTPException(status_code=400, detail="User must be at least 13 years old")
    
    hashed_password = pwd_context.hash(user.password)
    
    user_doc = {
        "username": user.username,
        "password": hashed_password,
        "email": user.email,
        "phone": user.phone,
        "birth_date": user.birth_date,
        "profile_image_url": user.profile_image_url,
        "followers": [],
        "following": [],
        "private_account": user.private_account
    }
    
    result = await users_collection.insert_one(user_doc)
    
    # Create token data
    token_data = {
        "user_id": str(result.inserted_id),
        "username": user_doc["username"],
        "email": user_doc["email"],
        "phone": user_doc["phone"],
        "birth_date": user_doc["birth_date"],
        "profile_image_url": user_doc["profile_image_url"],
        "followers": user_doc["followers"],
        "following": user_doc["following"],
        "private_account": False
    }
    
    # Create non-expiring token
    access_token = create_access_token(token_data, no_expiry=True)
    
    response_doc = token_data.copy()
    response_doc["id"] = token_data["user_id"]
    response_doc["access_token"] = access_token
    response_doc["token_type"] = "bearer"
    return UserResponse(**response_doc)

@router.post("/login")
async def login(user: UserLogin):
    db_user = await users_collection.find_one({"username": user.username})
    if not db_user or not pwd_context.verify(user.password, db_user["password"]):
        raise HTTPException(status_code=400, detail="Incorrect username or password")
    
    token_data = {
        "user_id": str(db_user["_id"]),
        "profile_image_url": db_user.get("profile_image_url"),
        "phone": db_user.get("phone"),
        "birth_date": db_user.get("birth_date"),
        "followers": db_user.get("followers", []),
        "following": db_user.get("following", []),
        "email": db_user["email"],
        "username": db_user["username"],
        "private_account": db_user.get("private_account", False)
    }
    
    # Create non-expiring token
    access_token = create_access_token(token_data, no_expiry=True)
    return {"access_token": access_token, "token_type": "bearer"}

@router.get("/users", response_model=List[UserResponse])
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
            "following": user.get("following", []),
            "private_account": user.get("private_account", False)
        })
    
    return [UserResponse(**user) for user in response_users]

@router.get("/users/search", response_model=List[UserResponse])
async def search_users(username: str = Query(..., min_length=1)):
    try:
        search_pattern = f".*{username}.*"
        print(f"Searching with pattern: {search_pattern}")
        
        users = await users_collection.find({
            "username": {"$regex": search_pattern, "$options": "i"}
        }).limit(20).to_list(20)
        
        print(f"Found {len(users)} users")
        
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
    except Exception as e:
        print(f"Error in search: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/users/{user_id}", response_model=UserResponse)
async def get_user_profile(user_id: str):
    try:
        user = await users_collection.find_one({"_id": ObjectId(user_id)})
        
        if not user:
            raise HTTPException(status_code=404, detail="User not found")
        
        return UserResponse(
            id=str(user["_id"]),
            username=user["username"],
            email=user["email"],
            profile_image_url=user.get("profile_image_url"),
            phone=user.get("phone"),
            birth_date=user.get("birth_date"),
            followers=user.get("followers", []),
            following=user.get("following", []),
            private_account=user.get("private_account", False)
        )
    except Exception as e:
        print(f"Error getting user profile: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@router.put("/users/{user_id}/profile", response_model=UserResponse)
async def update_profile(user_id: str, user_data: UserUpdate):
    try:
        # Verify user exists
        user = await users_collection.find_one({"_id": ObjectId(user_id)})
        if not user:
            raise HTTPException(status_code=404, detail="User not found")
        
        # Create update document with only provided fields
        update_data = {
            k: v for k, v in user_data.dict(exclude_unset=True).items()
            if v is not None
        }
        
        if "private_account" in update_data:
            update_data["private_account"] = update_data["private_account"]
        
        if not update_data:
            raise HTTPException(status_code=400, detail="No data to update")

        # Handle profile image update
        if "profile_image_url" in update_data:
            # Asegurarse de que la ruta de la imagen comience con /uploads/
            if not update_data["profile_image_url"].startswith("/uploads/"):
                update_data["profile_image_url"] = f"/uploads/{update_data['profile_image_url'].split('/')[-1]}"

            # Solo intentar eliminar la imagen anterior si existe y es diferente a la nueva
            old_image_path = user.get("profile_image_url")
            if old_image_path and old_image_path != update_data["profile_image_url"]:
                try:
                    file_name = old_image_path.replace("/uploads/", "")
                    full_path = os.path.join("uploads", file_name)
                    if os.path.exists(full_path):
                        os.remove(full_path)
                except Exception as e:
                    print(f"Error al eliminar imagen anterior: {str(e)}")
                    # Continuamos con la actualización incluso si falla la eliminación

        # Validate email if provided
        if "email" in update_data:
            if not "@" in update_data["email"]:
                raise HTTPException(status_code=400, detail="Invalid email format")
            existing_user = await users_collection.find_one({
                "email": update_data["email"],
                "_id": {"$ne": ObjectId(user_id)}
            })
            if existing_user:
                raise HTTPException(status_code=409, detail="Email already registered")

        # Update user profile
        result = await users_collection.update_one(
            {"_id": ObjectId(user_id)},
            {"$set": update_data}
        )
        
        if result.modified_count == 0:
            # En lugar de lanzar una excepción, simplemente devolvemos los datos actuales del usuario
            updated_user = await users_collection.find_one({"_id": ObjectId(user_id)})
            return UserResponse(
                id=str(updated_user["_id"]),
                username=updated_user["username"],
                email=updated_user["email"],
                profile_image_url=updated_user.get("profile_image_url"),
                phone=updated_user.get("phone"),
                birth_date=updated_user.get("birth_date"),
                followers=updated_user.get("followers", []),
                following=updated_user.get("following", []),
                private_account=updated_user.get("private_account", False)
            )

        # Get updated user data
        updated_user = await users_collection.find_one({"_id": ObjectId(user_id)})
        
        return UserResponse(
            id=str(updated_user["_id"]),
            username=updated_user["username"],
            email=updated_user["email"],
            profile_image_url=updated_user.get("profile_image_url"),
            phone=updated_user.get("phone"),
            birth_date=updated_user.get("birth_date"),
            followers=updated_user.get("followers", []),
            following=updated_user.get("following", []),
            private_account=updated_user.get("private_account", False)
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/users/{user_id}/follow", response_model=UserResponse)
async def follow_user(user_id: str, current_user_id: str):
    try:
        user_to_follow = await users_collection.find_one({"_id": ObjectId(user_id)})
        if not user_to_follow:
            raise HTTPException(status_code=404, detail="User to follow not found")

        current_user = await users_collection.find_one({"_id": ObjectId(current_user_id)})
        if not current_user:
            raise HTTPException(status_code=404, detail="Current user not found")

        if user_id == current_user_id:
            raise HTTPException(status_code=400, detail="You cannot follow yourself")

        if user_to_follow.get("private_account", False):
            existing_request = await friend_requests_collection.find_one({
                "sender_id": current_user_id,
                "receiver_id": user_id,
                "status": RequestStatus.PENDING
            })
            
            if existing_request:
                raise HTTPException(status_code=400, detail="Follow request already sent")
            
            request_doc = {
                "sender_id": current_user_id,
                "receiver_id": user_id,
                "status": RequestStatus.PENDING,
                "created_at": datetime.utcnow().isoformat()
            }
            await friend_requests_collection.insert_one(request_doc)
        else:
            if current_user_id not in user_to_follow.get("followers", []):
                await users_collection.update_one(
                    {"_id": ObjectId(user_id)},
                    {"$addToSet": {"followers": current_user_id}}
                )
            if user_id not in current_user.get("following", []):
                await users_collection.update_one(
                    {"_id": ObjectId(current_user_id)},
                    {"$addToSet": {"following": user_id}}
                )

        updated_user = await users_collection.find_one({"_id": ObjectId(user_id)})
        return UserResponse(
            id=str(updated_user["_id"]),
            username=updated_user["username"],
            email=updated_user["email"],
            profile_image_url=updated_user.get("profile_image_url"),
            phone=updated_user.get("phone"),
            birth_date=updated_user.get("birth_date"),
            followers=updated_user.get("followers", []),
            following=updated_user.get("following", []),
            private_account=updated_user.get("private_account", False)
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/users/{user_id}/unfollow", response_model=UserResponse)
async def unfollow_user(user_id: str, current_user_id: str):
    try:
        # Verificar que el usuario a dejar de seguir existe
        user_to_unfollow = await users_collection.find_one({"_id": ObjectId(user_id)})
        if not user_to_unfollow:
            raise HTTPException(status_code=404, detail="User to unfollow not found")

        # Verificar que el usuario actual existe
        current_user = await users_collection.find_one({"_id": ObjectId(current_user_id)})
        if not current_user:
            raise HTTPException(status_code=404, detail="Current user not found")

        # Cancelar solicitud pendiente si existe
        await friend_requests_collection.delete_one({
            "sender_id": current_user_id,
            "receiver_id": user_id,
            "status": "pending"
        })

        # Actualizar la lista de seguidores y seguidos
        if current_user_id in user_to_unfollow.get("followers", []):
            await users_collection.update_one(
                {"_id": ObjectId(user_id)},
                {"$pull": {"followers": current_user_id}}
            )
        if user_id in current_user.get("following", []):
            await users_collection.update_one(
                {"_id": ObjectId(current_user_id)},
                {"$pull": {"following": user_id}}
            )

        # Retornar el perfil actualizado
        updated_user = await users_collection.find_one({"_id": ObjectId(user_id)})
        return UserResponse(
            id=str(updated_user["_id"]),
            username=updated_user["username"],
            email=updated_user["email"],
            profile_image_url=updated_user.get("profile_image_url"),
            phone=updated_user.get("phone"),
            birth_date=updated_user.get("birth_date"),
            followers=updated_user.get("followers", []),
            following=updated_user.get("following", []),
            private_account=updated_user.get("private_account", False)
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/users/{user_id}/follow-request")
async def check_follow_request(user_id: str, current_user_id: str = Query(...)):
    try:
        request = await friend_requests_collection.find_one({
            "sender_id": current_user_id,
            "receiver_id": user_id,
            "status": "pending"
        })
        
        if request:
            # Get complete sender user data
            sender = await users_collection.find_one({"_id": ObjectId(current_user_id)})
            request["id"] = str(request["_id"])
            request["sender"] = {
                "id": str(sender["_id"]),
                "username": sender["username"],
                "email": sender["email"],
                "profile_image_url": sender.get("profile_image_url"),
                "phone": sender.get("phone"),
                "birth_date": sender.get("birth_date"),
                "followers": sender.get("followers", []),
                "following": sender.get("following", []),
                "private_account": sender.get("private_account", False)
            }
            del request["_id"]
            
        return request
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/users/{user_id}/follow-request/accept")
async def accept_follow_request(user_id: str, request_id: str = Query(...)):
    try:
        # Update request status
        result = await friend_requests_collection.update_one(
            {"_id": ObjectId(request_id)},
            {"$set": {"status": "accepted"}}
        )
        
        if result.modified_count == 0:
            raise HTTPException(status_code=404, detail="Request not found")
        
        # Get request details
        request = await friend_requests_collection.find_one({"_id": ObjectId(request_id)})
        
        # Add follower relationship
        await users_collection.update_one(
            {"_id": ObjectId(user_id)},
            {"$addToSet": {"followers": request["sender_id"]}}
        )
        
        await users_collection.update_one(
            {"_id": ObjectId(request["sender_id"])},
            {"$addToSet": {"following": user_id}}
        )
        
        # Get updated user data
        updated_user = await users_collection.find_one({"_id": ObjectId(user_id)})
        return UserResponse(**{
            "id": str(updated_user["_id"]),
            "username": updated_user["username"],
            "email": updated_user["email"],
            "profile_image_url": updated_user.get("profile_image_url"),
            "phone": updated_user.get("phone"),
            "birth_date": updated_user.get("birth_date"),
            "followers": updated_user.get("followers", []),
            "following": updated_user.get("following", []),
            "private_account": updated_user.get("private_account", False)
        })
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/users/{user_id}/follow-request/reject")
async def reject_follow_request(user_id: str, request_id: str = Query(...)):
    try:
        result = await friend_requests_collection.update_one(
            {"_id": ObjectId(request_id)},
            {"$set": {"status": "rejected"}}
        )
        
        if result.modified_count == 0:
            raise HTTPException(status_code=404, detail="Request not found")
            
        # Get updated user data
        user = await users_collection.find_one({"_id": ObjectId(user_id)})
        return UserResponse(**{
            "id": str(user["_id"]),
            "username": user["username"],
            "email": user["email"],
            "profile_image_url": user.get("profile_image_url"),
            "phone": user.get("phone"),
            "birth_date": user.get("birth_date"),
            "followers": user.get("followers", []),
            "following": user.get("following", []),
            "private_account": user.get("private_account", False)
        })
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/users/{user_id}/follow-requests/received", response_model=List[FriendRequest])
async def get_received_follow_requests(user_id: str):
    try:
        requests = await friend_requests_collection.find({
            "receiver_id": user_id,
            "status": "pending"
        }).to_list(length=100)
        
        response = []
        for request in requests:
            # Obtener información del remitente
            sender = await users_collection.find_one({"_id": ObjectId(request["sender_id"])})
            # Obtener información del receptor
            receiver = await users_collection.find_one({"_id": ObjectId(request["receiver_id"])})
            
            request["id"] = str(request["_id"])
            # Agregar información del remitente
            request["senderUsername"] = sender["username"] if sender else "Unknown"
            request["senderProfileImage"] = sender.get("profile_image_url") if sender else None
            # Agregar información del receptor
            request["receiverUsername"] = receiver["username"] if receiver else "Unknown"
            request["receiverProfileImage"] = receiver.get("profile_image_url") if receiver else None
            del request["_id"]
            response.append(FriendRequest(**request))
            
        return response
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/users/{user_id}/follow-requests/sent", response_model=List[FriendRequest])
async def get_sent_follow_requests(user_id: str):
    try:
        requests = await friend_requests_collection.find({
            "sender_id": user_id,
            "status": "pending"
        }).to_list(length=100)
        
        response = []
        for request in requests:
            # Obtener información del remitente
            sender = await users_collection.find_one({"_id": ObjectId(request["sender_id"])})
            # Obtener información del receptor
            receiver = await users_collection.find_one({"_id": ObjectId(request["receiver_id"])})
            
            request["id"] = str(request["_id"])
            # Agregar información del remitente
            request["senderUsername"] = sender["username"] if sender else "Unknown"
            request["senderProfileImage"] = sender.get("profile_image_url") if sender else None
            # Agregar información del receptor
            request["receiverUsername"] = receiver["username"] if receiver else "Unknown"
            request["receiverProfileImage"] = receiver.get("profile_image_url") if receiver else None
            del request["_id"]
            response.append(FriendRequest(**request))
            print(request)
        return response
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/upload")
async def upload_image(image: UploadFile = File(...), user_id: str = Query(...)):
    try:
        # Crear el directorio de uploads si no existe
        UPLOAD_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "uploads")
        if not os.path.exists(UPLOAD_DIR):
            os.makedirs(UPLOAD_DIR)
        
        # Obtener la extensión del archivo original
        file_extension = os.path.splitext(image.filename)[1]
        
        # Generar el nombre del archivo usando el ID del usuario
        filename = f"{user_id}{file_extension}"
        file_path = os.path.join(UPLOAD_DIR, filename)
        
        # Si existe una imagen anterior con el mismo nombre, la eliminamos
        if os.path.exists(file_path):
            os.remove(file_path)
        
        # Guardar el archivo nuevo
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(image.file, buffer)
        
        # Devolver la URL relativa del archivo
        return {"imageUrl": f"/uploads/{filename}"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
        
from fastapi import APIRouter, HTTPException, Query
from typing import List
from ..models.user import UserCreate, UserResponse, UserLogin, FriendRequest
from ..utils.auth import pwd_context, create_access_token
from ..database import friend_requests_collection, users_collection
from datetime import datetime
from bson import ObjectId

router = APIRouter()

@router.post("/register", response_model=UserResponse)
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
    
    # Create token data
    token_data = {
        "user_id": str(result.inserted_id),
        "username": user_doc["username"],
        "email": user_doc["email"],
        "phone": user_doc["phone"],
        "birth_date": user_doc["birth_date"],
        "profile_image_url": user_doc["profile_image_url"],
        "followers": user_doc["followers"],
        "following": user_doc["following"]
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
        "username": db_user["username"]
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
            "following": user.get("following", [])
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
        "username": db_user["username"]
    }
    
    access_token = create_access_token(token_data)
    return {"access_token": access_token, "token_type": "bearer"}


@router.get("/users/{user_id}", response_model=UserResponse)
async def get_user_profile(user_id: str):
    try:
        from bson import ObjectId
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
            following=user.get("following", [])
        )
    except Exception as e:
        print(f"Error getting user profile: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/users/{user_id}/follow", response_model=UserResponse)
async def follow_user(user_id: str, current_user_id: str):
    try:
        # Verificar que el usuario a seguir existe
        user_to_follow = await users_collection.find_one({"_id": ObjectId(user_id)})
        if not user_to_follow:
            raise HTTPException(status_code=404, detail="User to follow not found")

        # Verificar que el usuario actual existe
        current_user = await users_collection.find_one({"_id": ObjectId(current_user_id)})
        if not current_user:
            raise HTTPException(status_code=404, detail="Current user not found")

        # Evitar seguir a sí mismo
        if user_id == current_user_id:
            raise HTTPException(status_code=400, detail="You cannot follow yourself")

        # Verificar si la cuenta es privada
        if user_to_follow.get("private_account", False):
            # Verificar si ya existe una solicitud pendiente
            existing_request = await friend_requests_collection.find_one({
                "sender_id": current_user_id,
                "receiver_id": user_id,
                "status": "pending"
            })
            
            if existing_request:
                raise HTTPException(status_code=400, detail="Follow request already sent")
            
            # Crear nueva solicitud de seguimiento
            request_doc = {
                "sender_id": current_user_id,
                "receiver_id": user_id,
                "status": "pending",
                "created_at": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S.%fZ")
            }
            await friend_requests_collection.insert_one(request_doc)
            
            return UserResponse(
                id=str(user_to_follow["_id"]),
                username=user_to_follow["username"],
                email=user_to_follow["email"],
                profile_image_url=user_to_follow.get("profile_image_url"),
                phone=user_to_follow.get("phone"),
                birth_date=user_to_follow.get("birth_date"),
                followers=user_to_follow.get("followers", []),
                following=user_to_follow.get("following", []),
                private_account=user_to_follow.get("private_account", False)
            )
        
        # Si la cuenta no es privada, seguir directamente
        if current_user_id not in user_to_follow.get("followers", []):
            await users_collection.update_one(
                {"_id": ObjectId(user_id)},
                {"$push": {"followers": current_user_id}}
            )
        if user_id not in current_user.get("following", []):
            await users_collection.update_one(
                {"_id": ObjectId(current_user_id)},
                {"$push": {"following": user_id}}
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

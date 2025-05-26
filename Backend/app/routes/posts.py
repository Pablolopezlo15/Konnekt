from fastapi import APIRouter, Header, HTTPException, UploadFile, File, Form
from typing import List
from ..models.post import PostCreate, PostResponse, CommentCreate, CommentResponse
from ..database import users_collection, posts_collection, comments_collection, likes_collection, saved_posts_collection
from ..utils.auth import decode_token 
from datetime import datetime
from bson import ObjectId
import os
import shutil

router = APIRouter()

# Create uploads directory if it doesn't exist
UPLOAD_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "uploads")
if not os.path.exists(UPLOAD_DIR):
    os.makedirs(UPLOAD_DIR)

from fastapi import APIRouter, Header, HTTPException, UploadFile, File, Form, Request  # Add Request import

@router.post("/posts", response_model=PostResponse)
async def create_post(
    request: Request,  # Add request parameter
    caption: str = Form(...),
    image: UploadFile = File(..., description="Image file (max 16MB)"),
    authorization: str | None = Header(default=None, alias="Authorization")
):
    try:
        # Verificar tamaño del archivo
        contents = await image.read()
        if len(contents) > 16 * 1024 * 1024:  # 10MB limit
            raise HTTPException(
                status_code=413,
                detail="El archivo es demasiado grande. El tamaño máximo permitido es 10MB"
            )
        
        # Restablecer el puntero del archivo para su posterior uso
        await image.seek(0)
        
        print("Received request with:")
        print(f"Caption: {caption}")
        print(f"Image filename: {image.filename}")
        print(f"Authorization header: {authorization}")
        print(f"Request headers: {dict(request.headers)}")
        
        if not authorization:
            raise HTTPException(
                status_code=401, 
                detail={
                    "message": "Authorization header is required",
                    "expected_format": "Bearer your_token_here"
                }
            )
        
        try:
            if authorization.startswith("Bearer "):
                token = authorization.split(" ")[1]
            else:
                token = authorization
            
            user_data = decode_token(token)
        except HTTPException as he:
            raise he  # Propagate 401 from decode_token
        except Exception as e:
            raise HTTPException(status_code=401, detail="Invalid authentication token")
            print(f"Successfully decoded token for user: {user_data['username']}")
        except Exception as e:
            print(f"Token validation error: {str(e)}")
            raise HTTPException(
                status_code=401,
                detail={
                    "message": "Invalid authentication token",
                    "error": str(e)
                }
            )
        print(f"Decoded user data: {user_data}")
        
        user_id = user_data["user_id"]
        username = user_data["username"]
        
        # Generar nombre único para la imagen
        timestamp = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
        file_extension = os.path.splitext(image.filename)[1]
        unique_filename = f"post_{user_id}_{timestamp}{file_extension}"
        
        file_path = os.path.join(UPLOAD_DIR, unique_filename)
        
        # Save the file
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(image.file, buffer)
        print(f"File saved at: {file_path}")
        # Create relative path for database
        relative_path = f"/uploads/{unique_filename}"
        
        post_doc = {
            "author_id": user_id,
            "author_username": username,
            "image_url": relative_path,
            "caption": caption,
            "timestamp": datetime.utcnow().isoformat(),
            "likes_count": 0,
            "comments_count": 0,
            "is_liked": False,
            "is_saved": False
        }
        
        result = await posts_collection.insert_one(post_doc)
        post_doc["id"] = str(result.inserted_id)
        
        return PostResponse(**post_doc)
    except Exception as e:
        # If there's an error, clean up the uploaded file if it exists
        if 'file_path' in locals() and os.path.exists(file_path):
            os.remove(file_path)
        raise HTTPException(status_code=500, detail=str(e))

@router.delete("/posts/{post_id}")
async def delete_post(post_id: str, authorization: str | None = Header(default=None, alias="Authorization")):
    try:
        if not authorization:
            raise HTTPException(
                status_code=401,
                detail="Authorization header is required. Use format: Bearer <token>"
            )

        if authorization.startswith("Bearer "):
            token = authorization.split(" ")[1]
        else:
            token = authorization

        user_data = decode_token(token)
        user_id = user_data["user_id"]

        # Verificar si el post existe
        post = await posts_collection.find_one({"_id": ObjectId(post_id)})
        if not post:
            raise HTTPException(status_code=404, detail="Post not found")

        # Verificar si el usuario es el autor del post
        if post["author_id"] != user_id:
            raise HTTPException(status_code=403, detail="Not authorized to delete this post")

        # Eliminar el archivo de imagen
        image_path = os.path.join("../", post["image_url"])  # Ajusta la ruta según sea necesario
        print(f"Attempting to delete image at: {image_path}")
        if os.path.exists(image_path):
            os.remove(image_path)

        # Eliminar el post
        await posts_collection.delete_one({"_id": ObjectId(post_id)})
        return {"message": "Post and image deleted successfully"}

    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/posts", response_model=List[PostResponse])
async def get_all_posts(authorization: str | None = Header(default=None, alias="Authorization")):
    try:
        if not authorization:
            raise HTTPException(
                status_code=401, 
                detail="Authorization header is required. Use format: Bearer <token>"
            )
        
        try:
            if authorization.startswith("Bearer "):
                token = authorization.split(" ")[1]
            else:
                token = authorization
                
            user_data = decode_token(token)
        except HTTPException as he:
            raise he
        except Exception as e:
            raise HTTPException(status_code=401, detail="Invalid authentication token")

        user_id = user_data["user_id"]
        
        # Obtener la lista de usuarios que el usuario actual sigue
        current_user = await users_collection.find_one({"_id": ObjectId(user_id)})
        following = current_user.get("following", [])
        
        # Obtener todos los posts y filtrar
        posts = await posts_collection.find().sort("timestamp", -1).to_list(100)
        
        response_posts = []
        for post in posts:
            try:
                # Obtener información del autor del post
                post_author = await users_collection.find_one({"_id": ObjectId(post["author_id"])})
                
                # Verificar si debemos incluir este post:
                # - Si la cuenta no es privada, incluir
                # - Si la cuenta es privada y el usuario sigue al autor, incluir
                # - Si el post es del usuario actual, incluir
                if (not post_author.get("private_account", False) or 
                    post["author_id"] in following or 
                    post["author_id"] == user_id):
                    
                    is_liked = await likes_collection.find_one({
                        "user_id": user_id,
                        "post_id": str(post["_id"])
                    }) is not None
                    
                    is_saved = await saved_posts_collection.find_one({
                        "user_id": user_id,
                        "post_id": str(post["_id"])
                    }) is not None
                    
                    post_dict = {
                        "id": str(post["_id"]),
                        "author_id": post.get("author_id", ""),
                        "author_username": post.get("author_username", ""),
                        "image_url": post.get("image_url", ""),
                        "caption": post.get("caption", ""),
                        "timestamp": post.get("timestamp", ""),
                        "likes_count": post.get("likes_count", 0),
                        "comments_count": post.get("comments_count", 0),
                        "is_liked": is_liked,
                        "is_saved": is_saved
                    }
                    response_posts.append(post_dict)
            except Exception as post_error:
                print(f"Error processing post {post.get('_id', 'unknown')}: {str(post_error)}")
                continue
            
        return response_posts
    except Exception as e:
        print(f"Error in get_all_posts: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")
        

@router.get("/posts/{user_id}", response_model=List[PostResponse])
async def get_user_posts(
    user_id: str,
    authorization: str | None = Header(default=None, alias="Authorization")
):
    try:
        if not authorization:
            raise HTTPException(
                status_code=401, 
                detail="Authorization header is required. Use format: Bearer <token>"
            )
        
        try:
            if authorization.startswith("Bearer "):
                token = authorization.split(" ")[1]
            else:
                token = authorization
                
            user_data = decode_token(token)
        except HTTPException as he:
            raise he
        except Exception as e:
            raise HTTPException(status_code=401, detail="Invalid authentication token")

        current_user_id = user_data["user_id"]
        posts = await posts_collection.find({"author_id": user_id}).sort("timestamp", -1).to_list(100)
        
        response_posts = []
        for post in posts:
            is_liked = await likes_collection.find_one({
                "user_id": current_user_id,
                "post_id": str(post["_id"])
            }) is not None
            
            
            is_saved = await saved_posts_collection.find_one({
                "user_id": current_user_id,
                "post_id": str(post["_id"])
            }) is not None
            
            post_dict = {
                "id": str(post["_id"]),
                "author_id": post["author_id"],
                "author_username": post["author_username"],
                "image_url": post["image_url"],
                "caption": post["caption"],
                "timestamp": post["timestamp"],
                "likes_count": post["likes_count"],
                "comments_count": post["comments_count"],
                "is_liked": is_liked,
                "is_saved": is_saved
            }
            response_posts.append(post_dict)
            
        return response_posts
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/getposts/{post_id}", response_model=PostResponse)
async def get_post(post_id: str, authorization: str | None = Header(default=None, alias="Authorization")):
    try:
        if not authorization:
            raise HTTPException(
                status_code=401,
                detail="Authorization header is required. Use format: Bearer <token>"
            )

        try:
            if authorization.startswith("Bearer "):
                token = authorization.split(" ")[1]
            else:
                token = authorization

            user_data = decode_token(token)
        except HTTPException as he:
            raise he
        except Exception as e:
            raise HTTPException(status_code=401, detail="Invalid authentication token")

        user_id = user_data["user_id"]

        # Check if post exists
        post = await posts_collection.find_one({"_id": ObjectId(post_id)})
        if not post:
            raise HTTPException(
                status_code=404,
                detail="Post not found"
            )

        # Check if current user liked this post
        is_liked = await likes_collection.find_one({
            "user_id": user_id,
            "post_id": post_id
        }) is not None

        # Check if current user saved this post
        is_saved = await saved_posts_collection.find_one({
            "user_id": user_id,
            "post_id": post_id
        }) is not None

        post_dict = {
            "id": str(post["_id"]),
            "author_id": post["author_id"],
            "author_username": post["author_username"],
            "image_url": post["image_url"],
            "caption": post["caption"],
            "timestamp": post["timestamp"],
            "likes_count": post["likes_count"],
            "comments_count": post["comments_count"],
            "is_liked": is_liked,
            "is_saved": is_saved
        }
        print(f"Post data: {post_dict}")
        return post_dict
    except HTTPException as e:
        # Re-raise HTTPException to ensure 401, 404, etc., are returned correctly
        raise e
    except Exception as e:
        # Handle unexpected errors
        print(f"Unexpected error in get_post: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")

@router.post("/posts/{post_id}/like")
async def like_post(
    post_id: str, 
    authorization: str | None = Header(default=None, alias="Authorization")
):
    try:
        if not authorization:
            raise HTTPException(
                status_code=401, 
                detail="Authorization header is required. Use format: Bearer <token>"
            )
        
        try:
            if authorization.startswith("Bearer "):
                token = authorization.split(" ")[1]
            else:
                token = authorization
                
            user_data = decode_token(token)
        except HTTPException as he:
            raise he  # Propagate 401 from decode_token
        except Exception as e:
            raise HTTPException(status_code=401, detail="Invalid authentication token")

        user_id = user_data["user_id"]
        
        # Check if post exists
        post = await posts_collection.find_one({"_id": ObjectId(post_id)})
        if not post:
            raise HTTPException(
                status_code=404,
                detail="Post not found"
            )
        
        # Check if already liked
        existing_like = await likes_collection.find_one({
            "user_id": user_id,
            "post_id": post_id
        })
        
        if existing_like:
            # Unlike
            await likes_collection.delete_one({"_id": existing_like["_id"]})
            await posts_collection.update_one(
                {"_id": ObjectId(post_id)},
                {"$inc": {"likes_count": -1}}
            )
            return {"message": "Post unliked"}
        else:
            # Like
            like_doc = {
                "user_id": user_id,
                "post_id": post_id,
                "timestamp": datetime.utcnow().isoformat()
            }
            await likes_collection.insert_one(like_doc)
            await posts_collection.update_one(
                {"_id": ObjectId(post_id)},
                {"$inc": {"likes_count": 1}}
            )
            return {"message": "Post liked"}
    except HTTPException as e:
        # Re-raise HTTPException to ensure 401, 404, etc., are returned correctly
        raise e
    except Exception as e:
        # Handle unexpected errors
        print(f"Unexpected error in like_post: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal server error")

@router.post("/posts/{post_id}/comments", response_model=CommentResponse)
async def add_comment(
    post_id: str, 
    comment: str = Form(...),
    authorization: str | None = Header(default=None, alias="Authorization")
):
    try:
        if not authorization:
            raise HTTPException(
                status_code=401, 
                detail="Authorization header is required. Use format: Bearer <token>"
            )
        
        try:
            if authorization.startswith("Bearer "):
                token = authorization.split(" ")[1]
            else:
                token = authorization
                
            user_data = decode_token(token)
        except HTTPException as he:
            raise he  # Propagate 401 from decode_token
        except Exception as e:
            raise HTTPException(status_code=401, detail="Invalid authentication token")

        user_id = user_data["user_id"]
        username = user_data["username"]
        
        comment_doc = {
            "post_id": post_id,
            "user_id": user_id,
            "username": username,
            "comment": comment,
            "timestamp": datetime.utcnow().isoformat()
        }
        
        result = await comments_collection.insert_one(comment_doc)
        await posts_collection.update_one(
            {"_id": ObjectId(post_id)},
            {"$inc": {"comments_count": 1}}
        )
        
        comment_doc["id"] = str(result.inserted_id)
        return CommentResponse(**comment_doc)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/posts/{post_id}/comments", response_model=List[CommentResponse])
async def get_post_comments(post_id: str):
    try:
        comments = await comments_collection.find({"post_id": post_id}).sort("timestamp", -1).to_list(100)
        return [CommentResponse(id=str(comment["_id"]), **{k:v for k,v in comment.items() if k != "_id"}) for comment in comments]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/posts/{post_id}/save")
async def save_post(post_id: str, authorization: str | None = Header(default=None, alias="Authorization")):
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
        except Exception as e:
            raise HTTPException(status_code=401, detail="Token de autenticación inválido")

        user_id = user_data["user_id"]
        
        # Verificar si el post existe
        post = await posts_collection.find_one({"_id": ObjectId(post_id)})
        if not post:
            raise HTTPException(status_code=404, detail="Post no encontrado")
        
        # Verificar si ya está guardado
        saved_post = await saved_posts_collection.find_one({
            "user_id": user_id,
            "post_id": post_id
        })
        
        if saved_post:
            # Si ya está guardado, lo eliminamos
            await saved_posts_collection.delete_one({"_id": saved_post["_id"]})
            return {"message": "Post eliminado de guardados", "is_saved": False}
        else:
            # Si no está guardado, lo guardamos
            saved_post_doc = {
                "user_id": user_id,
                "post_id": post_id,
                "saved_at": datetime.utcnow().isoformat()
            }
            await saved_posts_collection.insert_one(saved_post_doc)
            return {"message": "Post guardado exitosamente", "is_saved": True}
            
    except Exception as e:
        print(f"Error al guardar/eliminar post: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/posts/saved/{user_id}", response_model=List[PostResponse])
async def get_saved_posts(
    user_id: str,
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
        except Exception as e:
            raise HTTPException(status_code=401, detail="Token de autenticación inválido")

        current_user_id = user_data["user_id"]
        
        # Obtener los posts guardados del usuario
        saved_posts = await saved_posts_collection.find({"user_id": user_id}).to_list(100)
        saved_post_ids = [ObjectId(post["post_id"]) for post in saved_posts]
        
        # Obtener los detalles completos de los posts guardados
        posts = await posts_collection.find({"_id": {"$in": saved_post_ids}}).to_list(100)
        
        response_posts = []
        for post in posts:
            is_liked = await likes_collection.find_one({
                "user_id": current_user_id,
                "post_id": str(post["_id"])
            }) is not None
            
            is_saved = True  # Ya sabemos que está guardado
            
            post_dict = {
                "id": str(post["_id"]),
                "author_id": post["author_id"],
                "author_username": post["author_username"],
                "image_url": post["image_url"],
                "caption": post["caption"],
                "timestamp": post["timestamp"],
                "likes_count": post["likes_count"],
                "comments_count": post["comments_count"],
                "is_liked": is_liked,
                "is_saved": is_saved
            }
            response_posts.append(post_dict)
            
        return response_posts
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

from fastapi import APIRouter, Header, HTTPException, UploadFile, File, Form
from typing import List
from ..models.post import PostCreate, PostResponse, CommentCreate, CommentResponse
from ..database import posts_collection, comments_collection, likes_collection
from ..utils.auth import decode_token  # Add this import
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
    image: UploadFile = File(...),
    authorization: str | None = Header(default=None, alias="Authorization")
):
    try:
        print("Received request with:")
        print(f"Caption: {caption}")
        print(f"Image filename: {image.filename}")
        print(f"Authorization header: {authorization}")
        print(f"Request headers: {dict(request.headers)}")  # Now request is defined
        
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
        
        # Create unique filename
        timestamp = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
        filename = f"{user_id}_{timestamp}_{image.filename}"
        file_path = os.path.join(UPLOAD_DIR, filename)
        
        # Save the file
        with open(file_path, "wb") as buffer:
            shutil.copyfileobj(image.file, buffer)
        
        # Create relative path for database
        relative_path = f"/uploads/{filename}"
        
        post_doc = {
            "author_id": user_id,
            "author_username": username,
            "image_url": relative_path,
            "caption": caption,
            "timestamp": datetime.utcnow().isoformat(),
            "likes_count": 0,
            "comments_count": 0
        }
        
        result = await posts_collection.insert_one(post_doc)
        post_doc["id"] = str(result.inserted_id)
        
        return PostResponse(**post_doc)
    except Exception as e:
        # If there's an error, clean up the uploaded file if it exists
        if 'file_path' in locals() and os.path.exists(file_path):
            os.remove(file_path)
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/posts", response_model=List[PostResponse])
async def get_all_posts():
    try:
        posts = await posts_collection.find().sort("timestamp", -1).to_list(100)
        return [PostResponse(id=str(post["_id"]), **{k:v for k,v in post.items() if k!= "_id"}) for post in posts]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
        

@router.get("/posts/{user_id}", response_model=List[PostResponse])
async def get_user_posts(user_id: str):
    try:
        posts = await posts_collection.find({"author_id": user_id}).sort("timestamp", -1).to_list(100)
        return [PostResponse(id=str(post["_id"]), **{k:v for k,v in post.items() if k != "_id"}) for post in posts]
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

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
    comment: CommentCreate, 
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
            "comment": comment.comment,
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

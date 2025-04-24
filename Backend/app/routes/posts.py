from fastapi import APIRouter, Header, HTTPException
from typing import List
from ..models.post import PostCreate, PostResponse
from ..database import posts_collection
from ..utils.auth import jwt, SECRET_KEY, ALGORITHM
from bson import ObjectId
from datetime import datetime

router = APIRouter()

@router.post("/posts", response_model=PostResponse)
async def create_post(post: PostCreate, token: str = Header(...)):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id = payload["user_id"]
        username = payload["username"]
        
        post_doc = {
            "author_id": user_id,
            "author_username": username,
            "content": post.content,
            "image_url": post.image_url,
            "location": post.location,
            "likes": [],
            "comments": [],
            "created_at": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S.%fZ")
        }
        
        result = await posts_collection.insert_one(post_doc)
        post_doc["id"] = str(result.inserted_id)
        
        return PostResponse(**post_doc)
    except jwt.JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")

@router.get("/posts", response_model=List[PostResponse])
async def get_posts(skip: int = 0, limit: int = 10):
    posts = await posts_collection.find().skip(skip).limit(limit).sort("created_at", -1).to_list(limit)
    return [PostResponse(id=str(post["_id"]), **{k:v for k,v in post.items() if k != "_id"}) for post in posts]

@router.get("/posts/{user_id}", response_model=List[PostResponse])
async def get_user_posts(user_id: str):
    posts = await posts_collection.find({"author_id": user_id}).sort("created_at", -1).to_list(100)
    return [PostResponse(id=str(post["_id"]), **{k:v for k,v in post.items() if k != "_id"}) for post in posts]

@router.put("/posts/{post_id}/like")
async def like_post(post_id: str, token: str = Header(...)):
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id = payload["user_id"]
        
        post = await posts_collection.find_one({"_id": ObjectId(post_id)})
        if not post:
            raise HTTPException(status_code=404, detail="Post not found")
        
        if user_id in post["likes"]:
            await posts_collection.update_one(
                {"_id": ObjectId(post_id)},
                {"$pull": {"likes": user_id}}
            )
        else:
            await posts_collection.update_one(
                {"_id": ObjectId(post_id)},
                {"$push": {"likes": user_id}}
            )
        
        updated_post = await posts_collection.find_one({"_id": ObjectId(post_id)})
        return {"likes": updated_post["likes"]}
    except jwt.JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")
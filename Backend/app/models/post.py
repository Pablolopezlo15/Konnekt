from pydantic import BaseModel
from typing import Optional
from datetime import datetime

class PostCreate(BaseModel):
    caption: str

class PostResponse(BaseModel):
    id: str
    author_id: str
    author_username: str
    image_url: str
    caption: str
    timestamp: str
    likes_count: int
    comments_count: int

class CommentCreate(BaseModel):
    comment: str

class CommentResponse(BaseModel):
    id: str
    post_id: str
    user_id: str
    username: str
    comment: str
    timestamp: str
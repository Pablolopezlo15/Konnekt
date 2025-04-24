from pydantic import BaseModel
from typing import List, Dict, Optional

class PostCreate(BaseModel):
    content: str
    image_url: Optional[str] = None
    location: Optional[str] = None

class PostResponse(BaseModel):
    id: str
    author_id: str
    author_username: str
    content: str
    image_url: Optional[str] = None
    location: Optional[str] = None
    likes: List[str] = []
    comments: List[Dict] = []
    created_at: str
from pydantic import BaseModel
from typing import List, Optional

class UserCreate(BaseModel):
    username: str
    password: str
    email: str
    phone: Optional[str] = None
    birth_date: Optional[str] = None
    profile_image_url: Optional[str] = None

class UserResponse(BaseModel):
    id: str
    username: str
    email: str
    profile_image_url: Optional[str] = None
    phone: Optional[str] = None
    birth_date: Optional[str] = None
    followers: List[str] = []
    following: List[str] = []

class UserLogin(BaseModel):
    username: str
    password: str
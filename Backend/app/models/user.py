from pydantic import BaseModel, EmailStr
from typing import List, Optional
from enum import Enum

class RequestStatus(str, Enum):
    PENDING = "pending"
    ACCEPTED = "accepted"
    REJECTED = "rejected"

class UserCreate(BaseModel):
    username: str
    password: str
    email: str
    phone: Optional[str] = None
    birth_date: Optional[str] = None
    profile_image_url: Optional[str] = None
    private_account: bool = False

class UserUpdate(BaseModel):
    email: Optional[EmailStr] = None
    phone: Optional[str] = None
    birth_date: Optional[str] = None
    profile_image_url: Optional[str] = None
    username: Optional[str] = None
    private_account: Optional[bool] = None

    class Config:
        extra = "allow"

class UserResponse(BaseModel):
    id: str
    username: str
    email: str
    profile_image_url: Optional[str] = None
    phone: Optional[str] = None
    birth_date: Optional[str] = None
    followers: List[str] = []
    following: List[str] = []
    private_account: bool = False

class UserLogin(BaseModel):
    username: str
    password: str

class FriendRequest(BaseModel):
    id: str
    sender_id: str
    receiver_id: str
    status: str
    created_at: str
    senderUsername: str | None
    senderProfileImage: str | None
    receiverUsername: str | None
    receiverProfileImage: str | None

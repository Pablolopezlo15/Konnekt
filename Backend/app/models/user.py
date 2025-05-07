from pydantic import BaseModel
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
    private_account: Optional[bool] = False

class UserResponse(BaseModel):
    id: str
    username: str
    email: str
    profile_image_url: Optional[str] = None
    phone: Optional[str] = None
    birth_date: Optional[str] = None
    followers: List[str] = []
    following: List[str] = []
    private_account: Optional[bool] = False

class UserLogin(BaseModel):
    username: str
    password: str

class FriendRequest(BaseModel):
    id: str
    sender_id: str
    receiver_id: str
    status: RequestStatus = RequestStatus.PENDING
    created_at: str
    updated_at: Optional[str] = None
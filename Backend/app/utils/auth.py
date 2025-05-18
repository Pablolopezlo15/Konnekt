from passlib.context import CryptContext
from jose import jwt
from datetime import datetime, timedelta
import os
from dotenv import load_dotenv

load_dotenv()

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
SECRET_KEY = os.getenv("SECRET_KEY", "your-secret-key")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24 * 7

def create_access_token(data: dict, no_expiry: bool = False):
    to_encode = data.copy()
    if not no_expiry:
        expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
        to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

def decode_token(token: str) -> dict:
    try:
        # Don't verify expiration by default
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM], options={"verify_exp": False})
        
        if "user_id" not in payload:
            raise HTTPException(
                status_code=401,
                detail="Token missing user_id"
            )
        
        return {
            "user_id": payload.get("user_id"),
            "username": payload.get("username", ""),
            "email": payload.get("email", ""),
            "profile_image_url": payload.get("profile_image_url"),
            "phone": payload.get("phone"),
            "birth_date": payload.get("birth_date"),
            "followers": payload.get("followers", []),
            "following": payload.get("following", []),
            "private_account": payload.get("private_account", False)
        }
    except JWTError as e:
        print(f"JWT error: {str(e)}")
        raise HTTPException(
            status_code=401,
            detail="Invalid token"
        )
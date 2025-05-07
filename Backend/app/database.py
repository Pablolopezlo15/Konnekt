from motor.motor_asyncio import AsyncIOMotorClient
import os
from dotenv import load_dotenv

load_dotenv()

MONGO_URI = os.getenv("MONGO_URI", "mongodb://localhost:27017")
client = AsyncIOMotorClient(MONGO_URI)
db = client.Konnekt

# Collections
users_collection = db.users
messages_collection = db.messages
posts_collection = db.posts
friend_requests_collection = db.friends_requests
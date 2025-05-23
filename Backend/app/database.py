from motor.motor_asyncio import AsyncIOMotorClient
import os
from dotenv import load_dotenv

load_dotenv()

MONGO_URI = os.getenv("MONGO_URI", "mongodb://root:root@localhost:27017")
print(MONGO_URI)
client = AsyncIOMotorClient(MONGO_URI)
db = client.Konnekt

# Collections
users_collection = db.users
messages_collection = db.messages
posts_collection = db.posts
likes_collection = db.likes
comments_collection = db.comments
saved_posts_collection = db.saved_posts
friend_requests_collection = db.friends_requests
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routes import users, posts, messages
from fastapi.staticfiles import StaticFiles

app = FastAPI()

# Mount the uploads directory for serving images
app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, replace with your frontend domain
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(users.router)
app.include_router(posts.router)
app.include_router(messages.router)
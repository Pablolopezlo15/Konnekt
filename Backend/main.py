from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routes import users, posts, messages, ai_routes
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
app.include_router(ai_routes.router)

if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        ssl_keyfile="certs/key.pem",
        ssl_certfile="certs/cert.pem"
    )
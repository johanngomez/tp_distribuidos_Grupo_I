from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config.settings import load_settings
from app.routes.auth import router as auth_router
from app.routes.health import router as health_router
from app.routes.vehicles import router as vehicles_router


app = FastAPI(title="Rentar API Gateway")
app.state.settings = load_settings()
app.add_middleware(
    CORSMiddleware,
    allow_origins=[app.state.settings.frontend_origin],
    allow_credentials=False,
    allow_methods=["GET", "POST", "PUT", "DELETE", "OPTIONS"],
    allow_headers=["Content-Type", "Authorization"],
)
app.include_router(health_router)
app.include_router(auth_router)
app.include_router(vehicles_router)
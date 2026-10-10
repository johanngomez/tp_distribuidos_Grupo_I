from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routes import auth, customers, health, rentals, vehicles
from app.graphql.schema import router as graphql_router
from app.config.settings import load_settings

app = FastAPI(title="Rentar API Gateway")

settings = load_settings()
app.state.settings = settings

app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.frontend_origin],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router)
app.include_router(auth.router)
app.include_router(vehicles.router)
app.include_router(customers.router)
app.include_router(rentals.router)
app.include_router(graphql_router, prefix="/graphql")

import math
import os
from dataclasses import dataclass, field


@dataclass(frozen=True)
class Settings:
    frontend_origin: str
    vehicle_grpc_target: str
    customer_grpc_target: str
    rental_grpc_target: str
    grpc_timeout_seconds: float
    jwt_secret: str | None = field(repr=False)


def load_settings() -> Settings:
    """Lee la configuración del entorno; los puertos locales son provisorios."""

    frontend_origin = os.getenv(
        "FRONTEND_ORIGIN",
        "http://localhost:5173",
    ).strip()

    if not frontend_origin or frontend_origin == "*":
        raise ValueError(
            "FRONTEND_ORIGIN debe indicar un origen concreto, no vacío ni '*'"
        )

    targets = {}

    for name, default in (
        ("VEHICLE_GRPC_TARGET", "localhost:50051"),
        ("CUSTOMER_GRPC_TARGET", "localhost:50052"),
        ("RENTAL_GRPC_TARGET", "localhost:50053"),
    ):
        value = os.getenv(name, default).strip()

        if not value:
            raise ValueError(f"{name} no puede estar vacío")

        targets[name] = value

    try:
        timeout = float(
            os.getenv("GRPC_TIMEOUT_SECONDS", "5")
        )
    except ValueError as error:
        raise ValueError(
            "GRPC_TIMEOUT_SECONDS debe ser un número mayor que cero"
        ) from error

    if not math.isfinite(timeout) or timeout <= 0:
        raise ValueError(
            "GRPC_TIMEOUT_SECONDS debe ser un número finito mayor que cero"
        )

    return Settings(
        frontend_origin=frontend_origin,
        vehicle_grpc_target=targets["VEHICLE_GRPC_TARGET"],
        customer_grpc_target=targets["CUSTOMER_GRPC_TARGET"],
        rental_grpc_target=targets["RENTAL_GRPC_TARGET"],
        grpc_timeout_seconds=timeout,
        jwt_secret=os.getenv("JWT_SECRET") or None,
    )
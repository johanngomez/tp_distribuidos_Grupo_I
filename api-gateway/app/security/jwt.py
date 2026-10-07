from dataclasses import dataclass

import jwt


@dataclass(frozen=True)
class AuthenticatedUser:
    email: str
    es_admin: bool


def validate_token(token: str, secret: str) -> AuthenticatedUser:
    """Valida el contrato JWT de AuthService.java, sin consultar la base de datos."""
    claims = jwt.decode(
        token,
        secret.encode("utf-8"),
        algorithms=["HS256"],
        issuer="rentar",
        options={"require": ["iss", "sub", "iat", "exp", "esAdmin"]},
    )

    if not isinstance(claims["sub"], str) or not claims["sub"].strip():
        raise jwt.InvalidTokenError("El sujeto debe identificar al usuario")
    if type(claims["esAdmin"]) is not bool:
        raise jwt.InvalidTokenError("esAdmin debe ser booleano")

    return AuthenticatedUser(email=claims["sub"], es_admin=claims["esAdmin"])
